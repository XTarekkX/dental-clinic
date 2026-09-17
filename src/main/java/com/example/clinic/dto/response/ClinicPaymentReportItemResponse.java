package com.example.clinic.dto.response;

import com.example.clinic.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClinicPaymentReportItemResponse(
        Long id,
        Long doctorId,
        String doctorFullName,
        Long patientId,
        String patientFullName,
        Long invoiceId,
        String invoiceNumber,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String referenceNumber,
        LocalDateTime paidAt,
        String notes,
        LocalDateTime createdAt
) {}
