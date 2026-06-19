package com.example.clinic.dto.response;

import com.example.clinic.enums.OperationStatus;

import java.time.LocalDateTime;

public record OperationResponse(

        Long id,

        Long patientId,

        Long doctorId,
        String doctorFullName,

        Long appointmentId,

        String operationType,
        String dentalProblem,
        String treatmentPlan,
        String toothNumber,

        OperationStatus status,
        String notes,

        LocalDateTime performedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}