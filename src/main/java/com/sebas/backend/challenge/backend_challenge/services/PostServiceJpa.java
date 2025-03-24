package com.sebas.backend.challenge.backend_challenge.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import com.sebas.backend.challenge.backend_challenge.entities.Post;
import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.repositories.PostRepository;
import com.sebas.backend.challenge.backend_challenge.repositories.UserRepository;

@Service
public class PostServiceJpa  implements PostService {

    @Autowired
    private PostRepository repository; 
    
    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("No se pudo obtener el usuario autenticado");
        }
        
        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            email = userDetails.getUsername(); // <-- Aquí está el fix
        } else {
            email = authentication.getPrincipal().toString();
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setAdmin(
            user.getRole().stream().anyMatch(role -> "ADMIN".equals(role.getName()))
        );
        
        return user;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Post save(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(getAuthenticatedUser());
        return repository.save(post);
    }

    @Override
    @Transactional
    public Optional<Post> update(Long id, Post post) {
        Optional<Post> postOptional = repository.findById(id);
        if (postOptional.isPresent()) {
            Post postDb = postOptional.get();
            User authenticatedUser = getAuthenticatedUser();

            if (!postDb.getUser().getId().equals(authenticatedUser.getId()) && !authenticatedUser.isAdmin()) {
                throw new RuntimeException("No tiene permisos para actualizar este post");
            }
            postDb.setTitle(post.getTitle());
            postDb.setContent(post.getContent());
            return Optional.of(repository.save(postDb));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Post> delete(Long id) {
        Optional<Post> postOptional = repository.findById(id);
        if (postOptional.isPresent()){
            Post post = postOptional.get();
            User authenticatedUser = getAuthenticatedUser();
            // Permitir borrar si es el autor o si es administrador
            if (post.getUser() != null && (post.getUser().getEmail().equals(authenticatedUser.getEmail()) || authenticatedUser.isAdmin())) {
                repository.delete(post);
            } else {
                throw new RuntimeException("No tiene permisos para eliminar este post");
            }
        }
        return postOptional;
    }
}
