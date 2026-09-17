package com.example.clinic.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ClinicFinancialReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalCollected,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        long totalAppointments,
        List<ClinicPaymentReportItemResponse> payments,
        List<ClinicExpenseReportItemResponse> expenses
) {}
