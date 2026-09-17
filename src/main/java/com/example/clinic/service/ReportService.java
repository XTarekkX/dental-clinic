package com.example.clinic.service;

import com.example.clinic.dto.response.*;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Operation;
import com.example.clinic.enums.AppointmentStatus;
import com.example.clinic.enums.InvoiceStatus;
import com.example.clinic.enums.OperationStatus;
import com.example.clinic.repository.*;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final OperationRepository operationRepository;
    private final SecurityUtils securityUtils;

    // ── FINANCIAL REPORT ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public FinancialReportResponse getFinancialReport(
            LocalDate startDate, LocalDate endDate) {

        Doctor doctor = securityUtils.getLoggedInDoctor();
        log.info("Generating financial report for doctorId: {} "
                + "from {} to {}", doctor.getId(), startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Total amount on all non-cancelled invoices in period
        BigDecimal totalInvoiced = invoiceRepository
                .sumTotalInvoicedByDoctorAndDateRange(
                        doctor.getId(), start, end);

        // Total payments collected in period
        BigDecimal totalCollected = paymentRepository
                .sumPaymentsByDoctorAndDateRange(
                        doctor.getId(), start, end);

        // Outstanding = invoiced - collected
        BigDecimal totalOutstanding = totalInvoiced
                .subtract(totalCollected);

        // Total expenses in period
        BigDecimal totalExpenses = expenseRepository
                .sumExpensesByDoctorAndDateRange(
                        doctor.getId(), startDate, endDate);

        // Net profit = collected - expenses
        BigDecimal netProfit = totalCollected.subtract(totalExpenses);

        // Invoice status counts
        long draftCount = invoiceRepository
                .countByDoctorAndStatusAndDateRange(
                        doctor.getId(), InvoiceStatus.DRAFT, start, end);
        long issuedCount = invoiceRepository
                .countByDoctorAndStatusAndDateRange(
                        doctor.getId(), InvoiceStatus.ISSUED, start, end);
        long paidCount = invoiceRepository
                .countByDoctorAndStatusAndDateRange(
                        doctor.getId(), InvoiceStatus.PAID, start, end);
        long partialCount = invoiceRepository
                .countByDoctorAndStatusAndDateRange(
                        doctor.getId(), InvoiceStatus.PARTIALLY_PAID,
                        start, end);
        long cancelledCount = invoiceRepository
                .countByDoctorAndStatusAndDateRange(
                        doctor.getId(), InvoiceStatus.CANCELLED, start, end);

        log.info("Financial report — invoiced: {}, collected: {}, "
                        + "expenses: {}, profit: {}",
                totalInvoiced, totalCollected,
                totalExpenses, netProfit);

        return new FinancialReportResponse(
                startDate, endDate,
                totalInvoiced, totalCollected, totalOutstanding,
                totalExpenses, netProfit,
                draftCount, issuedCount, paidCount,
                partialCount, cancelledCount
        );
    }

    // ── APPOINTMENTS REPORT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public AppointmentReportResponse getAppointmentReport(
            LocalDate startDate, LocalDate endDate) {

        Doctor doctor = securityUtils.getLoggedInDoctor();
        log.info("Generating appointment report for doctorId: {}",
                doctor.getId());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndDateRange(
                        doctor.getId(), start, end);

        long total = appointments.size();
        long open = countAppointmentsByStatus(
                appointments, AppointmentStatus.OPEN);
        long checkedIn = countAppointmentsByStatus(
                appointments, AppointmentStatus.CHECKED_IN);
        long inProgress = countAppointmentsByStatus(
                appointments, AppointmentStatus.IN_PROGRESS);
        long completed = countAppointmentsByStatus(
                appointments, AppointmentStatus.COMPLETED);
        long cancelled = countAppointmentsByStatus(
                appointments, AppointmentStatus.CANCELLED);
        long noShow = countAppointmentsByStatus(
                appointments, AppointmentStatus.NO_SHOW);

        // Completion rate = completed / (total - cancelled - no_show)
        long eligible = total - cancelled - noShow;
        double completionRate = eligible > 0
                ? BigDecimal.valueOf((double) completed / eligible * 100)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue()
                : 0.0;

        return new AppointmentReportResponse(
                startDate, endDate,
                total, open, checkedIn, inProgress,
                completed, cancelled, noShow,
                completionRate
        );
    }

    // ── PATIENTS REPORT ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public PatientReportResponse getPatientReport(
            LocalDate startDate, LocalDate endDate) {

        Doctor doctor = securityUtils.getLoggedInDoctor();
        log.info("Generating patient report for doctorId: {}",
                doctor.getId());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        long newPatients = patientRepository
                .countNewPatientsBetween(start, end);

        long totalActive = patientRepository.countAllActive();

        return new PatientReportResponse(
                startDate, endDate,
                newPatients, totalActive
        );
    }

    // ── OPERATIONS REPORT ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public OperationReportResponse getOperationReport(
            LocalDate startDate, LocalDate endDate) {

        Doctor doctor = securityUtils.getLoggedInDoctor();
        log.info("Generating operation report for doctorId: {}",
                doctor.getId());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Operation> operations = operationRepository
                .findByDoctorIdAndDateRange(
                        doctor.getId(), start, end);

        long total = operations.size();
        long planned = countOperationsByStatus(
                operations, OperationStatus.PLANNED);
        long inProgress = countOperationsByStatus(
                operations, OperationStatus.IN_PROGRESS);
        long completed = countOperationsByStatus(
                operations, OperationStatus.COMPLETED);

        // Group by operation type — e.g. {"Root Canal": 12, "Filling": 8}
        Map<String, Long> byType = operations.stream()
                .collect(Collectors.groupingBy(
                        op -> op.getOperationType() != null
                                ? op.getOperationType() : "Unspecified",
                        Collectors.counting()
                ));

        return new OperationReportResponse(
                startDate, endDate,
                total, planned, inProgress, completed,
                byType
        );
    }


    // ── CLINIC-WIDE FINANCIAL REPORT ─────────────────────────────
    @Transactional(readOnly = true)
    public ClinicFinancialReportResponse getClinicFinancialReport(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating clinic-wide financial report from {} to {}",
                startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        BigDecimal totalCollected = paymentRepository
                .sumPaymentsByDateRange(start, end);
        BigDecimal totalExpenses = expenseRepository
                .sumExpensesByDateRange(startDate, endDate);
        BigDecimal netProfit = totalCollected.subtract(totalExpenses);
        long totalAppointments = appointmentRepository
                .countByDateRange(start, end);

        List<ClinicPaymentReportItemResponse> payments = paymentRepository
                .findAllByDateRangeWithDetails(start, end)
                .stream()
                .map(payment -> new ClinicPaymentReportItemResponse(
                        payment.getId(),
                        payment.getInvoice().getDoctor().getId(),
                        payment.getInvoice().getDoctor().getFirstName()
                                + " "
                                + payment.getInvoice().getDoctor().getLastName(),
                        payment.getPatient().getId(),
                        payment.getPatient().getFirstName()
                                + " " + payment.getPatient().getLastName(),
                        payment.getInvoice().getId(),
                        payment.getInvoice().getInvoiceNumber(),
                        payment.getAmount(),
                        payment.getPaymentMethod(),
                        payment.getReferenceNumber(),
                        payment.getPaidAt(),
                        payment.getNotes(),
                        payment.getCreatedAt()
                ))
                .toList();

        List<ClinicExpenseReportItemResponse> expenses = expenseRepository
                .findAllByDateRangeWithDoctor(startDate, endDate)
                .stream()
                .map(expense -> new ClinicExpenseReportItemResponse(
                        expense.getId(),
                        expense.getDoctor().getId(),
                        expense.getDoctor().getFirstName()
                                + " " + expense.getDoctor().getLastName(),
                        expense.getCategory(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getExpenseDate(),
                        expense.getCreatedAt()
                ))
                .toList();

        return new ClinicFinancialReportResponse(
                startDate, endDate, totalCollected, totalExpenses,
                netProfit, totalAppointments, payments, expenses
        );
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────
    private long countAppointmentsByStatus(
            List<Appointment> appointments,
            AppointmentStatus status) {
        return appointments.stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    private long countOperationsByStatus(
            List<Operation> operations,
            OperationStatus status) {
        return operations.stream()
                .filter(o -> o.getStatus() == status)
                .count();
    }
}