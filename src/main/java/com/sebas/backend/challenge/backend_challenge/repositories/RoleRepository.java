package com.sebas.backend.challenge.backend_challenge.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sebas.backend.challenge.backend_challenge.entities.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional <Role> findByName(String Name);

}