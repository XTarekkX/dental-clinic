package com.example.clinic.service;

import com.example.clinic.dto.request.LoginRequest;
import com.example.clinic.dto.request.RefreshTokenRequest;
import com.example.clinic.dto.request.RegisterRequest;
import com.example.clinic.dto.response.AuthResponse;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.RefreshToken;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.session-refresh-token-expiration}")
    private long sessionRefreshTokenExpiration;

    // ── REGISTER ───────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.username());
        String username = request.username().trim();
        String email = request.email().trim();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .isActive(true)
                .build();

        Doctor doctor = Doctor.builder()
                .user(user)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .phone(trimToNull(request.phone()))
                .specialization(trimToNull(request.specialization()))
                .licenseNumber(trimToNull(request.licenseNumber()))
                .build();

        user.setDoctor(doctor);
        User savedUser = userRepository.save(user);

        log.info("Registration successful for username: {}", savedUser.getUsername());
        return issueAuthResponse(savedUser, doctor, true);
    }

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

        // Step 4: revoke any old refresh tokens for this user
        // so they can't have multiple sessions at once
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        // Step 5: return everything the frontend needs
        log.info("Login successful for username: {}", user.getUsername());
        return issueAuthResponse(user, doctor, Boolean.TRUE.equals(request.rememberMe()));
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

        // Issue a brand new refresh token and revoke the old one
        boolean rememberMe = Boolean.TRUE.equals(stored.getRememberMe());
        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);

        log.info("Token refreshed for username: {}", user.getUsername());
        return issueAuthResponse(user, doctor, rememberMe);
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

    private AuthResponse issueAuthResponse(
            User user,
            Doctor doctor,
            boolean rememberMe) {

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole().name()
        );
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(resolveRefreshTokenExpiration(rememberMe) / 1000))
                .isRevoked(false)
                .rememberMe(rememberMe)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                user.getUsername(),
                user.getRole().name(),
                doctor.getFirstName(),
                doctor.getLastName()
        );
    }

    private long resolveRefreshTokenExpiration(boolean rememberMe) {
        return rememberMe ? refreshTokenExpiration : sessionRefreshTokenExpiration;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
