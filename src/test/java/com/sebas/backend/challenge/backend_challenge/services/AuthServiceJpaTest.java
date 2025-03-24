package com.sebas.backend.challenge.backend_challenge.services;

import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.repositories.UserRepository;
import com.sebas.backend.challenge.backend_challenge.util.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthServiceJpaTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceJpa authService;

    private final String email = "sebasburgo52@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";
    private final String jwtToken = "jwtToken123";

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail(email);
        testUser.setPassword(encodedPassword);
    }

    @Test
    public void testLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refreshToken");

        // Act
        Map<String, String> tokens = authService.login(email, password);

        // Assert
        assertEquals("accessToken", tokens.get("access_token"));
        assertEquals("refreshToken", tokens.get("refresh_token"));

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
    }

    @Test
    public void testLoginInvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, password);
        });

        // Assert
        assertEquals("Contraseña inválida", exception.getReason());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    public void testLoginUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, password);
        });

        // Assert
        assertEquals("Usuario no encontrado", exception.getReason());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    public void testRefreshTokenSuccess() {
        // Arrange
        when(jwtUtil.validateToken("refreshToken")).thenReturn(true);
        when(jwtUtil.getUsernameFromJWT("refreshToken")).thenReturn(email);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("newRefreshToken");

        // Act
        Map<String, String> tokens = authService.refreshToken("refreshToken");

        // Assert
        assertEquals("newAccessToken", tokens.get("access_token"));
        assertEquals("newRefreshToken", tokens.get("refresh_token"));

        // Verify interactions
        verify(jwtUtil).validateToken("refreshToken");
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
    }

    @Test
    public void testRefreshTokenInvalid() {
        // Arrange
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken("invalidToken");
        });

        // Assert
        assertEquals("Refresh token inválido", exception.getMessage());
    }
}
