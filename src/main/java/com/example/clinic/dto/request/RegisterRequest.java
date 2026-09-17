package com.example.clinic.dto.request;

import com.example.clinic.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8 to 100 characters")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name is too long")
        String lastName,

        @Size(max = 20, message = "Phone is too long")
        String phone,

        @Size(max = 100, message = "Specialization is too long")
        String specialization,

        @Size(max = 50, message = "License number is too long")
        String licenseNumber
) {}
