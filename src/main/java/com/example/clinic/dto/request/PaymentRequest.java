package com.example.clinic.dto.request;

import com.example.clinic.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRequest(

        @NotNull(message = "Invoice ID is required")
        Long invoiceId,

        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        // Optional — card transaction id or insurance claim number
        String referenceNumber,

        // Optional — defaults to now if not provided
        LocalDateTime paidAt,

        String notes
) {}