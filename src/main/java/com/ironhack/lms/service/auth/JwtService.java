package com.ironhack.lms.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret; // at least 32 chars for HS256

    @Value("${app.jwt.expiration-minutes:120}")
    private long expirationMin;

    private SecretKey key;

    @PostConstruct
    void init() {
        // If you prefer Base64 secrets, decode here instead of getBytes()
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMin * 60)))
                .signWith(key)            // 0.12.x: alg inferred from key (HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()         // 0.12.x
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            return extractUsername(token).equals(user.getUsername());
        } catch (Exception e) {
            return false;
        }
    }
}
