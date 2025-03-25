package com.sebas.backend.challenge.backend_challenge.controllers;

import com.sebas.backend.challenge.backend_challenge.dto.LoginRequestDto;
import com.sebas.backend.challenge.backend_challenge.dto.RefreshTokenRequestDto;
import com.sebas.backend.challenge.backend_challenge.dto.UserDto;
import com.sebas.backend.challenge.backend_challenge.services.AuthService;
import com.sebas.backend.challenge.backend_challenge.services.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private  AuthService authService;

    @GetMapping("/findall")
    public ResponseEntity<List<UserDto>> list() {
       List<UserDto> users = userService.findAll();
       return ResponseEntity.ok(users);
    }

    @GetMapping("/finduser/{id}")
    public ResponseEntity<UserDto> viewUser(@PathVariable Long id) {
       Optional<UserDto> userOptional = userService.findById(id);
       return userOptional.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> create( @RequestBody UserDto userDto) {
       UserDto savedUser = userService.saveUser(userDto);
       return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody LoginRequestDto loginRequest) {
        Map<String, Object> response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); 
        return ResponseEntity.ok(authService.logout(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Validated @RequestBody RefreshTokenRequestDto refreshRequest) {
        Map<String, String> tokens = authService.refreshToken(refreshRequest.getRefreshToken()); // <-- Usa getRefreshToken()
        return ResponseEntity.ok(tokens);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Validated @RequestBody UserDto userDto) {
       Optional<UserDto> updatedUser = userService.updateUser(id, userDto);
       return updatedUser.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long id) {
       Optional<UserDto> deletedUser = userService.deleteUser(id);
       return deletedUser.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

}
