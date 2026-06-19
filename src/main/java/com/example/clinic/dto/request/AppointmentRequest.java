package com.example.clinic.dto.request;

import com.example.clinic.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        //@NotNull(message = "Doctor ID is required")
        //Long doctorId,

        // @Future ensures nobody books an appointment in the past
        @NotNull(message = "Scheduled time is required")
        @Future(message = "Appointment must be scheduled in the future")
        LocalDateTime scheduledAt,

        // Duration must be at least 10 minutes
        @Min(value = 10, message = "Duration must be at least 10 minutes")
        Integer durationMinutes,

        String reason,

        // Status is optional on create — defaults to OPEN in the service
        AppointmentStatus status
) {}