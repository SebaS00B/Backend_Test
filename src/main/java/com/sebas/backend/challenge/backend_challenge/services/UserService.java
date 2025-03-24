package com.sebas.backend.challenge.backend_challenge.services;

import java.util.List;
import java.util.Optional;


import com.sebas.backend.challenge.backend_challenge.dto.UserDto;
import com.sebas.backend.challenge.backend_challenge.entities.User;


public interface UserService {
    List<UserDto> findAll();
    Optional<UserDto> findById(Long id);
    UserDto saveUser(UserDto userDto);
    Optional<UserDto> updateUser(Long id, UserDto userDto);
    Optional<UserDto> deleteUser(Long id);
    UserDto convertToDTO(User user);
}
