package com.sebas.backend.challenge.backend_challenge.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sebas.backend.challenge.backend_challenge.entities.Post;

public interface PostRepository extends JpaRepository <Post, Long> {

    
}
