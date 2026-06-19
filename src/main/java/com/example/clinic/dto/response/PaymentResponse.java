package com.example.clinic.dto.response;

import com.example.clinic.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,

        Long invoiceId,
        String invoiceNumber,

        Long patientId,
        String patientFullName,

        BigDecimal amount,
        PaymentMethod paymentMethod,
        String referenceNumber,

        LocalDateTime paidAt,
        String notes,

        LocalDateTime createdAt
) {}