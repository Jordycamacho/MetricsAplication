package com.fitapp.backend.infrastructure.persistence.adapter.out.supabase;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.domain.exception.SupabaseUserNotFoundException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

@Component
public class SupabaseAuthClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final Cache<String, Boolean> tokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    public SupabaseAuthClient(
            @Value("${supabase.service-key}") String serviceKey,
            @Value("${supabase.url}") String supabaseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceKey)
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .build();
        this.serviceKey = serviceKey;
    }

    public AuthResponse signIn(String email, String password) {
        Map<String, Object> body = Map.of(
                "email", email,
                "password", password);

        return webClient.post()
                .uri("/auth/v1/token?grant_type=password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .block();
    }

    public boolean isValidToken(String jwt) {
        // Verificar en caché primero
        Boolean cached = tokenCache.getIfPresent(jwt);
        if (cached != null)
            return cached;

        try {
            Map<String, Object> body = Map.of("token", jwt);
            webClient.post()
                    .uri("/auth/v1/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            tokenCache.put(jwt, true);
            return true;
        } catch (WebClientResponseException e) {
            tokenCache.put(jwt, false);
            return false;
        }
    }

    public void verifySupabaseUid(String uid) {
        try {
            UserResponse user = webClient.get()
                    .uri("/admin/users/{id}", uid)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .blockOptional()
                    .orElseThrow(() -> new SupabaseUserNotFoundException(uid));

            if (!user.isConfirmed()) {
                throw new IllegalStateException("El usuario no está confirmado en Supabase");
            }
        } catch (WebClientResponseException.NotFound ex) {
            throw new SupabaseUserNotFoundException(uid);
        }
    }

    @lombok.Value
    private static class UserResponse {
        String id;
        String email;
        @JsonProperty("confirmed_at")
        Instant confirmedAt;

        public boolean isConfirmed() {
            return confirmedAt != null;
        }
    }

    public void signOut(String token) {
        webClient.post()
                .uri("/auth/v1/logout")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}