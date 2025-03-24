package com.sebas.backend.challenge.backend_challenge.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sebas.backend.challenge.backend_challenge.dto.UserDto;
import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.repositories.UserRepository;
import com.sebas.backend.challenge.backend_challenge.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class AuthServiceJpa implements AuthService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, String> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña inválida");
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", jwtUtil.generateAccessToken(user)); 
        tokens.put("refresh_token", jwtUtil.generateRefreshToken(user)); 
        return tokens;
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            Claims claims = jwtUtil.extractAllClaims(refreshToken);
            String username = claims.getSubject();
            Date refreshTokenIssuedAt = claims.getIssuedAt();

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

            // Verificar si el token fue emitido antes del último logout
            if (user.getLastLogoutTime() != null && refreshTokenIssuedAt.before(user.getLastLogoutTime())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
            }

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", jwtUtil.generateAccessToken(user)); 
            tokens.put("refresh_token", jwtUtil.generateRefreshToken(user)); 
            return tokens;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
    }

    @Override
    public Map<String, String> logout(String token) {
        String username;
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            username = claims.getSubject();
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setLastLogoutTime(new Date());
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout exitoso");
        return response;
    }

    public UserDto convertToDTO(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setAdmin(user.getRole().stream().anyMatch(r -> "ADMIN".equals(r.getName())));
        return dto;
    }
}