package com.sebas.backend.challenge.backend_challenge.controllers;


import org.springframework.web.bind.annotation.RestController;

import com.sebas.backend.challenge.backend_challenge.entities.Post;
import com.sebas.backend.challenge.backend_challenge.services.PostService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/findall")
    public ResponseEntity<List<Post>> list() {
       List<Post> posts = postService.findAll();
       return ResponseEntity.ok(posts);
    }

    @GetMapping("/findbyid/{id}")
    public ResponseEntity<Post> findById(@PathVariable Long id) {
       Optional<Post> postOptional = postService.findById(id);
       return postOptional.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<Post> create(@Validated @RequestBody Post post) {
       // Aquí puedes asignar el usuario autenticado y la fecha de creación.
       Post createdPost = postService.save(post);
       return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Post> update(@PathVariable Long id, @Validated @RequestBody Post post) {
       Optional<Post> updatedPost = postService.update(id, post);
       return updatedPost.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Post> delete(@Validated @PathVariable Long id) {
         Optional<Post> deletedPost = postService.delete(id);
         if (deletedPost.isPresent()) {
            return ResponseEntity.ok(deletedPost.get());
         } else {
            // Aquí se podría devolver NOT_FOUND o INTERNAL_SERVER_ERROR según la lógica de negocio
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
         }
      }


}
