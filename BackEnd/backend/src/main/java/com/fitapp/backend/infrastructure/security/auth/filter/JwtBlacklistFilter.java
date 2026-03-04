package com.fitapp.backend.infrastructure.security.auth.filter;

import com.fitapp.backend.application.ports.output.TokenBlacklistPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepta cada request y rechaza tokens que estén en la blacklist de Redis.
 * Debe registrarse en SecurityConfig antes del filtro de JWT resource server.
 */
@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenBlacklistPort.isBlacklisted(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Token invalidado. Por favor inicia sesión de nuevo.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}