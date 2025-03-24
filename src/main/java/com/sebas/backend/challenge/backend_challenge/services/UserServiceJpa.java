package com.sebas.backend.challenge.backend_challenge.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sebas.backend.challenge.backend_challenge.dto.UserDto;
import com.sebas.backend.challenge.backend_challenge.entities.Role;
import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.repositories.RoleRepository;
import com.sebas.backend.challenge.backend_challenge.repositories.UserRepository;

@Service
public class UserServiceJpa implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
       List<User> users = userRepository.findAll();
       return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
       Optional<User> userOptional = userRepository.findById(id);
       return userOptional.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UserDto saveUser(UserDto userDto) {
        User user = convertToEntity(userDto);
    
        List<Role> roles = new ArrayList<>();
        if(user.isAdmin()){
            // Si es admin, asignar solo el rol ADMIN
            Role adminRole = roleRepository.findByName("ADMIN")
                               .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            roles.add(adminRole);
        } else {
            // Si no es admin, asignar solo el rol USER
            Role userRole = roleRepository.findByName("USER")
                               .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
            roles.add(userRole);
        }
        
        user.setRole(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
    
    @Override
    @Transactional
    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
       Optional<User> userOptional = userRepository.findById(id);
       if (userOptional.isPresent()){
           User userDb = userOptional.get();
           userDb.setName(userDto.getName());
           userDb.setLastname(userDto.getLastname());
           
           // Actualizamos los roles según el flag admin
           if (userDto.isAdmin()) {
               // Si no tiene el rol "ADMIN", lo agregamos
               roleRepository.findByName("ADMIN").ifPresent(role -> {
                   if (userDb.getRole().stream().noneMatch(r -> "ADMIN".equals(r.getName()))) {
                       userDb.getRole().add(role);
                   }
               });
           } else {
               // Si se desmarca admin, eliminamos el rol "ADMIN" si existe
               userDb.setRole(userDb.getRole().stream()
                   .filter(r -> !"ADMIN".equals(r.getName()))
                   .collect(Collectors.toList()));
           }
           
           User updatedUser = userRepository.save(userDb);
           return Optional.of(convertToDTO(updatedUser));
       }
       return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<UserDto> deleteUser(Long id) {
       Optional<User> userOptional = userRepository.findById(id);
       if (userOptional.isPresent()){
           User userToDelete = userOptional.get();
           userRepository.delete(userToDelete);
           return Optional.of(convertToDTO(userToDelete));
       }
       return Optional.empty();
    }

    @Override
    public UserDto convertToDTO(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        // Se deriva el flag admin a partir de los roles
        dto.setAdmin(user.getRole().stream().anyMatch(r -> "ADMIN".equals(r.getName())));
        return dto;
    }

    public User convertToEntity(UserDto dto) {
       User user = new User();
       user.setId(dto.getId());
       user.setName(dto.getName());
       user.setLastname(dto.getLastname());
       user.setEmail(dto.getEmail());
       user.setPassword(dto.getPassword());
       user.setAdmin(dto.isAdmin());
       return user;
    }
}
