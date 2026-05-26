package com.agropulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${agropulse.security.jwt-secret}")
    private String jwtSecret;

    @Value("${agropulse.security.jwt-expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey key() {
        byte[] raw = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 requires at least 32 bytes
        if (raw.length < 32) {
            raw = Arrays.copyOf(raw, 32);
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String generateToken(int userId, String username, String email, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("email",  email)
                .claim("role",   role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
