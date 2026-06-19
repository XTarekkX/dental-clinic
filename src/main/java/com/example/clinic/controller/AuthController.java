package com.example.clinic.controller;

import com.example.clinic.dto.request.LoginRequest;
import com.example.clinic.dto.request.RefreshTokenRequest;
import com.example.clinic.dto.response.AuthResponse;
import com.example.clinic.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/login
    // @Valid triggers the validation annotations on LoginRequest
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/logout
    // @AuthenticationPrincipal Jwt — Spring injects the current user's JWT here
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal Jwt jwt) {

        authService.logout(jwt.getSubject());
        return ResponseEntity.ok("Logged out successfully");
    }

    //testing endpoint to get hashed password and store it in db directly
    @GetMapping("/test-hash")
    public String testHash() {
        return new BCryptPasswordEncoder().encode("password123");
    }
    // GET /api/v1/auth/me
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> response = new HashMap<>();
        response.put("username", jwt.getSubject());
        response.put("role", jwt.getClaim("roles"));
        return ResponseEntity.ok(response);
    }
}