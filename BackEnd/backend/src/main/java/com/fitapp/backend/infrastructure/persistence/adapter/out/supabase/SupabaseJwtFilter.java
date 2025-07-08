package com.fitapp.backend.infrastructure.persistence.adapter.out.supabase;

import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.UserModel;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Component
public class SupabaseJwtFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserPersistencePort userRepository;
    private final SupabaseAuthClient supabaseAuthClient;

    public SupabaseJwtFilter(@Value("${supabase.jwt-secret}") String jwtSecret, UserPersistencePort userRepository,
            SupabaseAuthClient supabaseAuthClient) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = new SecretKeySpec(keyBytes, "HMACSHA256");
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
        this.userRepository = userRepository;
        this.supabaseAuthClient = supabaseAuthClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException, java.io.IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {

            if (!supabaseAuthClient.isValidToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Token revocado o inválido\"}");
                return;
            }

            Jwt jwt = jwtDecoder.decode(token);
            String uid = jwt.getSubject();

            UserModel user = userRepository.findBySupabaseUid(uid)
                    .orElseThrow(() -> new UsernameNotFoundException(uid));

            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(uid, null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Autenticación fallida: " + e.getMessage() + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}