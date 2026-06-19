package com.example.clinic.dto.response;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long id,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}