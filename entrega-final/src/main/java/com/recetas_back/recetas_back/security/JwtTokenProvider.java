package com.recetas_back.recetas_back.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public enum TokenType { ACCESS, REFRESH }

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private Key key;

    @PostConstruct
    void init() {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return buildToken(userPrincipal.getUsername(), TokenType.ACCESS, accessTokenExpirationMs);
    }

    public String generateAccessToken(String username) {
        return buildToken(username, TokenType.ACCESS, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, TokenType.REFRESH, refreshTokenExpirationMs);
    }

    private String buildToken(String subject, TokenType type, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .claim("type", type.name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Object type = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().get("type");
            return TokenType.REFRESH.name().equals(type);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
