package com.example.clinic.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Creates a signed JWT containing the username and role
    public String generateAccessToken(String username, String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("clinic")                // who issued this token
                .issuedAt(now)                   // when it was created
                .expiresAt(now.plusMillis(accessTokenExpiration)) // when it expires
                .subject(username)               // who this token belongs to
                .claim("roles", role)            // the role — DOCTOR or ADMIN
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}