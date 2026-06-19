package com.example.clinic.dto.response;

import com.example.clinic.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(

        Long id,
        String firstName,
        String lastName,

        // Computed field — age calculated from dateOfBirth
        // We calculate this in the mapper, not in the entity
        Integer age,

        LocalDate dateOfBirth,
        Gender gender,
        String phone,
        String email,
        String address,
        String bloodType,
        String allergies,
        String emergencyContact,
        String emergencyPhone,

        // These come from BaseEntity — useful for the frontend to display
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}