package com.example.clinic.service;

import com.example.clinic.dto.request.InvoiceItemRequest;
import com.example.clinic.dto.request.InvoiceRequest;
import com.example.clinic.dto.response.InvoiceResponse;
import com.example.clinic.entity.*;
import com.example.clinic.enums.InvoiceStatus;
import com.example.clinic.exception.*;
import com.example.clinic.mapper.InvoiceMapper;
import com.example.clinic.repository.*;
import com.example.clinic.util.InvoiceNumberGenerator;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final SecurityUtils securityUtils;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        log.info("Creating invoice for patientId: {} by doctorId: {}",
                request.patientId(), request.doctorId());

        Patient patient = patientRepository.findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.patientId()));

        Doctor doctor = securityUtils.getLoggedInDoctor();

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository
                    .findById(request.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Appointment not found with id: "
                                    + request.appointmentId()));

            if (!appointment.getPatient().getId()
                    .equals(request.patientId())) {
                throw new BusinessRuleException(
                        "Appointment does not belong to this patient");
            }
        }

        // Build invoice shell first — we need the id for items
        Invoice invoice = Invoice.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .invoiceNumber(invoiceNumberGenerator.generate())
                .discount(orZero(request.discount()))
                .tax(orZero(request.tax()))
                .dueDate(request.dueDate())
                .notes(request.notes())
                .status(InvoiceStatus.DRAFT)
                .items(new ArrayList<>())
                .build();

        // Build line items and attach them to the invoice
        List<InvoiceItem> items = buildItems(request.items(), invoice);
        invoice.getItems().addAll(items);

        // Calculate totals from line items
        BigDecimal total = items.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);

        // net = total - discount + tax
        BigDecimal net = total
                .subtract(orZero(request.discount()))
                .add(orZero(request.tax()));

        invoice.setNetAmount(net);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {} for patientId: {}",
                saved.getInvoiceNumber(), request.patientId());

        return invoiceMapper.toResponse(saved, BigDecimal.ZERO);
    }

    // ── GET ALL (paginated, for invoices list page) ───────────────
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAll(int page, int size,
                                        InvoiceStatus status) {
        Doctor doctor = securityUtils.getLoggedInDoctor();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Invoice> invoices;

        if (status != null) {
            invoices = invoiceRepository
                    .findByDoctorIdAndStatus(doctor.getId(), status, pageable);
        } else {
            invoices = invoiceRepository
                    .findByDoctorId(doctor.getId(), pageable);
        }

        return invoices.map(inv -> {
            BigDecimal paid = paymentRepository
                    .sumPaymentsByInvoiceId(inv.getId());
            return invoiceMapper.toResponse(inv, paid);
        });
    }

    // ── GET ONE ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        log.debug("Fetching invoice with id: {}", id);

        Invoice invoice = findInvoiceById(id);
        BigDecimal paid = paymentRepository
                .sumPaymentsByInvoiceId(id);

        return invoiceMapper.toResponse(invoice, paid);
    }

    // ── GET ALL FOR PATIENT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByPatientId(Long patientId) {
        log.debug("Fetching invoices for patientId: {}", patientId);

        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return invoiceRepository.findByPatientId(patientId)
                .stream()
                .map(inv -> {
                    BigDecimal paid = paymentRepository
                            .sumPaymentsByInvoiceId(inv.getId());
                    return invoiceMapper.toResponse(inv, paid);
                })
                .toList();
    }

    // ── UPDATE (only allowed when DRAFT) ──────────────────────────
    @Transactional
    public InvoiceResponse update(Long id, InvoiceRequest request) {
        log.info("Updating invoice id: {}", id);

        Invoice invoice = findInvoiceById(id);

        // Only DRAFT invoices can be edited
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvoiceNotEditableException(
                    "Invoice " + invoice.getInvoiceNumber()
                            + " cannot be edited because its status is: "
                            + invoice.getStatus());
        }

        // Clear existing items and rebuild
        invoice.getItems().clear();
        List<InvoiceItem> newItems = buildItems(request.items(), invoice);
        invoice.getItems().addAll(newItems);

        BigDecimal total = newItems.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setTotalAmount(total);
        invoice.setDiscount(orZero(request.discount()));
        invoice.setTax(orZero(request.tax()));
        invoice.setNetAmount(total
                .subtract(orZero(request.discount()))
                .add(orZero(request.tax())));
        invoice.setDueDate(request.dueDate());
        invoice.setNotes(request.notes());

        Invoice updated = invoiceRepository.save(invoice);
        log.info("Invoice id: {} updated", id);

        return invoiceMapper.toResponse(updated, BigDecimal.ZERO);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    @Transactional
    public InvoiceResponse updateStatus(Long id, InvoiceStatus newStatus) {
        log.info("Updating status of invoice id: {} to {}", id, newStatus);

        Invoice invoice = findInvoiceById(id);
        validateInvoiceStatusTransition(invoice.getStatus(), newStatus);

        invoice.setStatus(newStatus);
        Invoice updated = invoiceRepository.save(invoice);

        log.info("Invoice id: {} status changed to {}", id, newStatus);

        BigDecimal paid = paymentRepository.sumPaymentsByInvoiceId(id);
        return invoiceMapper.toResponse(updated, paid);
    }

    // ── DELETE (only DRAFT invoices) ──────────────────────────────
    @Transactional
    public void delete(Long id) {
        log.info("Deleting invoice id: {}", id);

        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvoiceNotEditableException(
                    "Only DRAFT invoices can be deleted. "
                            + "Current status: " + invoice.getStatus());
        }

        invoiceRepository.delete(invoice);
        log.info("Invoice id: {} deleted", id);
    }

    // ── CALLED BY PaymentService after payment saved ──────────────
    // Recalculates invoice status based on total payments
    @Transactional
    public void recalculateStatus(Long invoiceId) {
        Invoice invoice = findInvoiceById(invoiceId);

        BigDecimal paid = paymentRepository
                .sumPaymentsByInvoiceId(invoiceId);

        int comparison = paid.compareTo(invoice.getNetAmount());

        if (comparison >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            log.info("Invoice id: {} fully paid", invoiceId);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            log.info("Invoice id: {} partially paid — paid: {} / net: {}",
                    invoiceId, paid, invoice.getNetAmount());
        }

        invoiceRepository.save(invoice);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────
    private Invoice findInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id));
    }

    private List<InvoiceItem> buildItems(
            List<InvoiceItemRequest> requests, Invoice invoice) {
        return requests.stream().map(req -> {
            BigDecimal total = req.unitPrice()
                    .multiply(BigDecimal.valueOf(req.quantity()));
            return InvoiceItem.builder()
                    .invoice(invoice)
                    .description(req.description())
                    .quantity(req.quantity())
                    .unitPrice(req.unitPrice())
                    .totalPrice(total)
                    .build();
        }).toList();
    }

    private void validateInvoiceStatusTransition(
            InvoiceStatus current, InvoiceStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == InvoiceStatus.ISSUED
                    || next == InvoiceStatus.CANCELLED;
            case ISSUED -> next == InvoiceStatus.PAID
                    || next == InvoiceStatus.PARTIALLY_PAID
                    || next == InvoiceStatus.CANCELLED;
            case PARTIALLY_PAID -> next == InvoiceStatus.PAID
                    || next == InvoiceStatus.CANCELLED;
            case PAID, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(
                    current.name());
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}