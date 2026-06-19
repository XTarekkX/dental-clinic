package com.example.clinic.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialReportResponse(

        LocalDate startDate,
        LocalDate endDate,

        // Revenue side
        BigDecimal totalInvoiced,
        BigDecimal totalCollected,
        BigDecimal totalOutstanding,

        // Expense side
        BigDecimal totalExpenses,

        // Bottom line
        BigDecimal netProfit,

        // Invoice breakdown by status counts
        long draftInvoices,
        long issuedInvoices,
        long paidInvoices,
        long partiallyPaidInvoices,
        long cancelledInvoices
) {}