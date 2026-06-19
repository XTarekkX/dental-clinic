package com.example.clinic.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record XrayRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        // Optional — can link to a specific appointment
        Long appointmentId,

        @Size(max = 10, message = "Tooth number must not exceed 10 characters")
        String toothNumber,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {}