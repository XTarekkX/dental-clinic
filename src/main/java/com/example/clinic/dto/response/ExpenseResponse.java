package com.example.clinic.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        Long doctorId,
        String doctorFullName,
        String category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        LocalDateTime createdAt
) {}