package com.example.clinic.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InvoiceItemRequest(

        @NotBlank(message = "Item description is required")
        String description,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Unit price must be greater than zero")
        BigDecimal unitPrice
) {}