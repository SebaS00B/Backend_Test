package com.sebas.backend.challenge.backend_challenge.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SpringBootSecurity {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            // Endpoints públicos
            .requestMatchers("/api/users/register", "/api/users/login","/api/users/logout", "/api/posts/findall",
            "/api/users/refresh", "/error").permitAll()
            // Endpoints de usuarios que solo pueden ser gestionados por ADMIN
            .requestMatchers("/api/users/update/**",
                              "/api/users/delete/**",
                              "/api/users/finduser/**").hasRole("ADMIN")
            // Endpoints de posts: se requiere autenticación; en la lógica del negocio se valida si es dueño o admin
            .requestMatchers("/api/posts/findbyid/**",
                              "/api/posts/create",
                              "/api/posts/update/**",
                              "/api/posts/delete/**").authenticated()
            // Cualquier otra petición requiere autenticación
            .anyRequest().authenticated()
        );

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

}
