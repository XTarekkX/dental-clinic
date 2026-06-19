package com.example.clinic.util;

import com.example.clinic.entity.Doctor;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.exception.UnauthorizedException;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    // ── GET LOGGED-IN USERNAME ────────────────────────────────────
    // Reads the username directly from the JWT in the security context
    // This is set automatically by OAuth2 Resource Server on every request
    public String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found in security context");
            throw new UnauthorizedException(
                    "No authenticated user found");
        }

        // The principal is a Jwt object when using OAuth2 Resource Server
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        throw new UnauthorizedException(
                "Cannot extract username from authentication token");
    }

    // ── GET LOGGED-IN DOCTOR ──────────────────────────────────────
    // The most useful method — returns the full Doctor entity
    // of whoever is currently making the request
    public Doctor getLoggedInDoctor() {
        String username = getLoggedInUsername();

        log.debug("Loading doctor profile for username: {}", username);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor profile not found for user: " + username));
    }
}