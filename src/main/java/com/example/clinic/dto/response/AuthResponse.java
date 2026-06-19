package com.example.clinic.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String username,
        String role,
        String firstName,
        String lastName
) {}