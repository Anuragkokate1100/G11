package com.example.trackbill.AIBackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret:TrackBillAISecretKeyForJWTSigningAndVerificationMustBeAtLeast256BitsLongForHS256Security!}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, Long ownerId, String name) {
        return generateToken(email, ownerId, name, "TRACTOR_OWNER");
    }

    public String generateToken(String email, Long userId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        // Keep ownerId for compatibility with existing owner-side code and clients.
        if ("TRACTOR_OWNER".equals(role)) claims.put("ownerId", userId);
        claims.put("name", name);
        claims.put("role", role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractOwnerId(String token) {
        Object ownerId = extractAllClaims(token).get("ownerId");
        if (ownerId instanceof Number) return ((Number) ownerId).longValue();
        return null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role instanceof String ? (String) role : null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
