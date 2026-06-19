package com.example.clinic.dto.response;

import java.time.LocalDateTime;

public record NoteResponse(

        Long id,

        Long patientId,

        // Who wrote this note — shown in the UI timeline
        Long doctorId,
        String doctorFullName,

        // Which visit this note came from — nullable
        Long appointmentId,
        LocalDateTime appointmentDate,

        String content,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}