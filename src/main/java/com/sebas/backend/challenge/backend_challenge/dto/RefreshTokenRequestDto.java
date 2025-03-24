package com.sebas.backend.challenge.backend_challenge.dto;

import jakarta.validation.constraints.NotBlank;

// RefreshTokenRequestDto.java
public class RefreshTokenRequestDto {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken; // Renombra el campo

    // Getters y Setters
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}