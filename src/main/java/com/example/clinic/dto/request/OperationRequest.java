package com.example.clinic.dto.request;

import com.example.clinic.enums.OperationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OperationRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        Long appointmentId,

        @NotBlank(message = "Operation type is required")
        @Size(max = 100, message = "Operation type must not exceed 100 characters")
        String operationType,

        // The actual diagnosis — what is wrong with this tooth
        @NotBlank(message = "Dental problem description is required")
        String dentalProblem,

        // What the doctor plans to do — may span multiple visits
        String treatmentPlan,

        @Size(max = 10)
        String toothNumber,

        // Defaults to PLANNED if not provided
        OperationStatus status,

        String notes
) {}