package com.example.clinic.dto.request;

import com.example.clinic.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PatientRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        // Optional fields — no @NotBlank, but we still validate format if provided
        LocalDate dateOfBirth,

        Gender gender,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Email(message = "Email must be a valid email address")
        @Size(max = 100)
        String email,

        String address,

        @Size(max = 5, message = "Blood type must not exceed 5 characters")
        String bloodType,

        String allergies,

        @Size(max = 100)
        String emergencyContact,

        @Size(max = 20)
        String emergencyPhone
) {}