package com.sebas.backend.challenge.backend_challenge.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sebas.backend.challenge.backend_challenge.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Inyecta el servicio que carga los detalles completos del usuario
    @Autowired
    private UserDetailsService customUserDetailsService; 

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        boolean shouldNot = antPathMatcher.match("/api/users/login", path) 
                || antPathMatcher.match("/api/users/register", path)
                || antPathMatcher.match("/api/posts/findall", path)
                || antPathMatcher.match("/api/users/refresh", path) 
                || antPathMatcher.match("/error", path); 
        logger.info("Ruta {} excluida del filtro: {}", path, shouldNot);
        return shouldNot;
    }

    @Override
protected void doFilterInternal(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull FilterChain filterChain)
                                throws ServletException, IOException {
    // Usamos el método público de JwtUtil para extraer el token
    String token = jwtUtil.getJwtFromRequest(request);

    if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
        String username = jwtUtil.getUsernameFromJWT(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
}
}
