package com.example.clinic.dto.response;

import java.time.LocalDateTime;

public record XrayResponse(

        Long id,

        Long patientId,

        Long doctorId,
        String doctorFullName,

        Long appointmentId,

        // The frontend uses this URL to display the image
        // We return a clean URL, not the raw file path
        String fileUrl,
        String fileName,

        String toothNumber,
        String description,

        LocalDateTime takenAt,
        LocalDateTime createdAt
) {}