package com.example.clinic.dto.response;

import java.time.LocalDate;
import java.util.Map;

public record OperationReportResponse(

        LocalDate startDate,
        LocalDate endDate,

        long totalOperations,
        long plannedOperations,
        long inProgressOperations,
        long completedOperations,

        // Breakdown by type — e.g. {"Root Canal": 12, "Extraction": 8}
        Map<String, Long> byOperationType
) {}