package com.sebas.backend.challenge.backend_challenge.services;

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
            String username = jwtUtil.getUsernameFromJWT(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", jwtUtil.generateAccessToken(user)); 
            tokens.put("refresh_token", jwtUtil.generateRefreshToken(user)); 
            return tokens;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
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
