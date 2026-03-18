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
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.slf4j.Logger;

@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistPort tokenBlacklistPort;
    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("[BLACKLIST_FILTER] Verificando token en request: {}", path);

            if (tokenBlacklistPort.isBlacklisted(token)) {
                log.warn("[BLACKLIST_FILTER] Token en blacklist bloqueado en path={}", path);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Token invalidado. Por favor inicia sesión de nuevo.\"}");
                return;
            }
            log.debug("[BLACKLIST_FILTER] Token OK (no está en blacklist) para path={}", path);
        } else {
            log.debug("[BLACKLIST_FILTER] Request sin Bearer token: {}", path);
        }

        filterChain.doFilter(request, response);
    }
}