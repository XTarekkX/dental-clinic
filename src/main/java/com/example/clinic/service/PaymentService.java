package com.example.clinic.service;

import com.example.clinic.dto.request.PaymentRequest;
import com.example.clinic.dto.response.PaymentResponse;
import com.example.clinic.entity.Invoice;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.Payment;
import com.example.clinic.enums.InvoiceStatus;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.PaymentExceedsBalanceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.PaymentMapper;
import com.example.clinic.repository.InvoiceRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final InvoiceService invoiceService;
    private final PaymentMapper paymentMapper;

    // ── RECORD PAYMENT ────────────────────────────────────────────
    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        log.info("Recording payment of {} for invoiceId: {}",
                request.amount(), request.invoiceId());

        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + request.invoiceId()));

        // Cannot pay a cancelled invoice
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot record payment for a cancelled invoice");
        }

        // Cannot pay an already fully paid invoice
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessRuleException(
                    "Invoice is already fully paid");
        }

        Patient patient = patientRepository
                .findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: "
                                + request.patientId()));

        // Check payment does not exceed the balance due
        BigDecimal alreadyPaid = paymentRepository
                .sumPaymentsByInvoiceId(invoice.getId());
        BigDecimal balanceDue = invoice.getNetAmount()
                .subtract(alreadyPaid);

        if (request.amount().compareTo(balanceDue) > 0) {
            throw new PaymentExceedsBalanceException(
                    "Payment amount " + request.amount()
                            + " exceeds balance due of " + balanceDue);
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .patient(patient)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .referenceNumber(request.referenceNumber())
                .paidAt(request.paidAt() != null
                        ? request.paidAt() : LocalDateTime.now())
                .notes(request.notes())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment id: {} recorded for invoiceId: {}",
                saved.getId(), request.invoiceId());

        // After saving, recalculate invoice status
        // This may flip invoice to PARTIALLY_PAID or PAID automatically
        invoiceService.recalculateStatus(invoice.getId());

        return paymentMapper.toResponse(saved);
    }

    // ── GET ALL FOR PATIENT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByPatientId(Long patientId) {
        log.debug("Fetching payments for patientId: {}", patientId);

        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return paymentRepository.findByPatientId(patientId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    // ── GET ALL FOR INVOICE ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByInvoiceId(Long invoiceId) {
        log.debug("Fetching payments for invoiceId: {}", invoiceId);

        invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + invoiceId));

        return paymentRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    // ── REVERSE PAYMENT ───────────────────────────────────────────
    @Transactional
    public void delete(Long paymentId) {
        log.info("Reversing payment id: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));

        Long invoiceId = payment.getInvoice().getId();

        paymentRepository.delete(payment);
        log.info("Payment id: {} reversed", paymentId);

        // Recalculate invoice status after reversal
        invoiceService.recalculateStatus(invoiceId);
    }
}