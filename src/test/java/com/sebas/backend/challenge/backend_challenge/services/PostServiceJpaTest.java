package com.sebas.backend.challenge.backend_challenge.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sebas.backend.challenge.backend_challenge.entities.Post;
import com.sebas.backend.challenge.backend_challenge.entities.User;
import com.sebas.backend.challenge.backend_challenge.repositories.PostRepository;
import com.sebas.backend.challenge.backend_challenge.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PostServiceJpaTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceJpa postService;

    private User authenticatedUser;

    @BeforeEach
    public void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setEmail("user@example.com");
        authenticatedUser.setAdmin(false);

        // Configuramos el SecurityContext con el email del usuario autenticado
        TestingAuthenticationToken auth = new TestingAuthenticationToken(authenticatedUser.getEmail(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Marcamos el stub como lenient para evitar el error de UnnecessaryStubbingException
        lenient().when(userRepository.findByEmail(authenticatedUser.getEmail()))
                 .thenReturn(Optional.of(authenticatedUser));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSavePost() {
        Post post = new Post();
        post.setTitle("Título");
        post.setContent("Contenido");

        // Simular el guardado: se asigna ID, usuario y fecha de creación
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(100L);
            p.setUser(authenticatedUser);
            p.setCreatedAt(java.time.LocalDateTime.now());
            return p;
        });

        Post savedPost = postService.save(post);
        assertNotNull(savedPost.getId());
        assertEquals("Título", savedPost.getTitle());
        assertEquals("Contenido", savedPost.getContent());
        assertNotNull(savedPost.getCreatedAt());
        assertEquals(authenticatedUser.getId(), savedPost.getUser().getId());
    }

    @Test
    public void testUpdatePostAuthorized() {
        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setTitle("Título original");
        existingPost.setContent("Contenido original");
        existingPost.setUser(authenticatedUser);

        Post updateInfo = new Post();
        updateInfo.setTitle("Título actualizado");
        updateInfo.setContent("Contenido actualizado");

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Post> updatedPost = postService.update(1L, updateInfo);
        assertTrue(updatedPost.isPresent());
        assertEquals("Título actualizado", updatedPost.get().getTitle());
        assertEquals("Contenido actualizado", updatedPost.get().getContent());
    }

    @Test
    public void testUpdatePostUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("otro@example.com");

        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setTitle("Título original");
        existingPost.setContent("Contenido original");
        existingPost.setUser(anotherUser);

        Post updateInfo = new Post();
        updateInfo.setTitle("Título actualizado");
        updateInfo.setContent("Contenido actualizado");

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            postService.update(1L, updateInfo);
        });
        assertEquals("No tiene permisos para actualizar este post", exception.getMessage());
    }

    @Test
    public void testDeletePostAuthorized() {
        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setTitle("Título");
        existingPost.setContent("Contenido");
        existingPost.setUser(authenticatedUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        // Aseguramos que al llamar a delete, se haga nada (simulación de método void)
        doNothing().when(postRepository).delete(any(Post.class));

        Optional<Post> deleted = postService.delete(1L);
        assertTrue(deleted.isPresent());
        // Verificamos que se llamó a delete con el post esperado
        verify(postRepository, times(1)).delete(existingPost);
    }

    @Test
    public void testDeletePostUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("otro@example.com");

        Post existingPost = new Post();
        existingPost.setId(1L);
        existingPost.setTitle("Título");
        existingPost.setContent("Contenido");
        existingPost.setUser(anotherUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            postService.delete(1L);
        });
        assertEquals("No tiene permisos para eliminar este post", exception.getMessage());
    }
}
