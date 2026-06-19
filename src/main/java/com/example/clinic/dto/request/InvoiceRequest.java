package com.example.clinic.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        Long doctorId,

        Long appointmentId,

        // At least one line item is required
        @NotEmpty(message = "Invoice must have at least one item")
        @Valid
        List<InvoiceItemRequest> items,

        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        BigDecimal discount,

        @DecimalMin(value = "0.0", message = "Tax cannot be negative")
        BigDecimal tax,

        LocalDate dueDate,

        String notes
) {}