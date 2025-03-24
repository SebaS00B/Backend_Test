package com.sebas.backend.challenge.backend_challenge.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.entities.Role;

public interface UserRepository extends JpaRepository <User, Long> {

    Optional<Role> findByName(String name);
    Optional<User> findByEmail(String email);


}
