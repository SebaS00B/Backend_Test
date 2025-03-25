package com.sebas.backend.challenge.backend_challenge.services;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String email, String password);
    Map<String, String> refreshToken(String token);
    Map<String, String> logout(String token);
}
