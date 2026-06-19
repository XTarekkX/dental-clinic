package com.example.clinic.service;

import com.example.clinic.dto.request.LoginRequest;
import com.example.clinic.dto.request.RefreshTokenRequest;
import com.example.clinic.dto.response.AuthResponse;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.RefreshToken;
import com.example.clinic.entity.User;
import com.example.clinic.exception.InvalidCredentialsException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.RefreshTokenRepository;
import com.example.clinic.repository.UserRepository;
import com.example.clinic.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ── LOGIN ───────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Step 1: verify username + password using Spring Security
        // If wrong, it throws BadCredentialsException automatically
        log.info("Login attempt for username: {}", request.username());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Step 2: load the user from the database
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Step 3: load the doctor profile linked to this user
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        // Step 4: create the access token (JWT, 15 minutes)
        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name()
        );

        // Step 5: create a refresh token (random string, 7 days)
        String refreshTokenValue = UUID.randomUUID().toString();

        // Step 6: revoke any old refresh tokens for this user
        // so they can't have multiple sessions at once
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        // Step 7: save the new refresh token to the database
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Step 8: return everything the frontend needs
        log.info("Login successful for username: {}", user.getUsername());
        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                user.getUsername(),
                user.getRole().name(),
                doctor.getFirstName(),
                doctor.getLastName()
        );
    }

    // ── REFRESH ─────────────────────────────────────────────────
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.debug("Token refresh requested");
        // Find the refresh token in the database
        RefreshToken stored = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid refresh token"));

        // Check it hasn't been revoked or expired
        if (stored.getIsRevoked()) {
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        // Load user and doctor
        User user = stored.getUser();
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        // Issue a brand new access token
        String newAccessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name()
        );

        // Issue a brand new refresh token and revoke the old one
        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);

        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);
        log.info("Token refreshed for username: {}", user.getUsername());
        return new AuthResponse(
                newAccessToken,
                newRefreshTokenValue,
                user.getUsername(),
                user.getRole().name(),
                doctor.getFirstName(),
                doctor.getLastName()
        );
    }

    // ── LOGOUT ──────────────────────────────────────────────────
    @Transactional
    public void logout(String username) {
        log.info("Logout requested for username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Revoke all refresh tokens — session is fully ended
        refreshTokenRepository.revokeAllUserTokens(user.getId());
        log.info("Logout successful for username: {}", username);
    }
}