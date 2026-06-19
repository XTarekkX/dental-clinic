package com.example.clinic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoteRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        // doctorId removed — extracted from JWT automatically
        Long appointmentId,

        @NotBlank(message = "Note content is required")
        @Size(max = 5000, message = "Note must not exceed 5000 characters")
        String content
) {}