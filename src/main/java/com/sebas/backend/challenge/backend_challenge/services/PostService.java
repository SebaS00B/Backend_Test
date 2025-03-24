package com.sebas.backend.challenge.backend_challenge.services;

import java.util.List;
import java.util.Optional;


import com.sebas.backend.challenge.backend_challenge.entities.Post;


public interface PostService  {

    List<Post> findAll();
    Optional <Post> findById(Long id);
    Post save(Post post);
    Optional<Post> update(Long id, Post post);
    Optional<Post> delete(Long id);
}

