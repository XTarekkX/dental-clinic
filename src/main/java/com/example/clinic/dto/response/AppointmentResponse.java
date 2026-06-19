package com.example.clinic.dto.response;

import com.example.clinic.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(

        Long id,

        // We return patient info inline — frontend needs this to display the card
        Long patientId,
        String patientFullName,

        // Doctor info inline — calendar needs to show who is handling it
        Long doctorId,
        String doctorFullName,

        LocalDateTime scheduledAt,
        Integer durationMinutes,

        // Computed — the calendar needs to know when the slot ends
        LocalDateTime endsAt,

        AppointmentStatus status,
        String reason,
        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}