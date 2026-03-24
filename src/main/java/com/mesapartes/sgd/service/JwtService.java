package com.mesapartes.sgd.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // 🔒 Validación CRÍTICA
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres (256 bits). " +
                            "Actual: " + keyBytes.length
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    // ===== GENERAR TOKEN =====

    public String generarToken(String email, String rol) {

        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .issuer("SGD-MESAPARTES")
                .claims(Map.of("rol", rol))
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // ===== OBTENER EMAIL =====

    public String obtenerEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ===== OBTENER ROL =====

    public String obtenerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // ===== VALIDAR TOKEN =====

    public TokenValidationResult validarToken(String token) {

        try {
            getClaims(token);
            return TokenValidationResult.VALID;

        } catch (ExpiredJwtException e) {
            return TokenValidationResult.EXPIRED;

        } catch (JwtException e) {
            return TokenValidationResult.INVALID;
        }
    }

    public enum TokenValidationResult {
        VALID,
        EXPIRED,
        INVALID
    }

    // ===== MÉTODO PRIVADO =====

    private Claims getClaims(String token) {

        Claims claims = Jwts.parser()
                .requireIssuer("SGD-MESAPARTES")
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims;
    }
}