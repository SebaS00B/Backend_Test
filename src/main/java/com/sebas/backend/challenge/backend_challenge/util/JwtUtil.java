package com.sebas.backend.challenge.backend_challenge.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sebas.backend.challenge.backend_challenge.entities.Role;
import com.sebas.backend.challenge.backend_challenge.entities.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.access.expiration:86400000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:86300000}")
    private long refreshTokenExpiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.secret = Base64.getEncoder().encodeToString(key.getEncoded());
            logger.warn("Clave JWT generada automáticamente.");
        } else {
            logger.info("Usando clave JWT configurada.");
        }

        logger.info("Clave secreta antes de decodificar: '{}'", secret);
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.strip()));
    }

    // Método para extraer todos los claims del token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRole().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(User user) {
        List<String> roles = user.getRole().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = request.getParameter("Authorization");
        }
        if (StringUtils.hasText(token)) {
            token = token.replaceAll("(?i)bearer\\s*", "").trim();
            logger.info("Token extraído: '{}'", token);
            return token;
        }
        return null;
    }

    // Validación básica del token (firma y expiración)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.error("Token expirado: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("Token inválido: {}", ex.getMessage());
        }
        return false;
    }
}