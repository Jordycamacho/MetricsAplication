package com.fitapp.backend.infrastructure.persistence.adapter.out.supabase;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.domain.exception.SupabaseUserNotFoundException;

import org.springframework.beans.factory.annotation.Value;

@Component
public class SupabaseAuthClient {

    private final WebClient webClient;
    private final String serviceKey;

    public SupabaseAuthClient(WebClient webClient,
                            @Value("${supabase.service-key}") String serviceKey,
                            @Value("${supabase.url}") String supabaseUrl) {
        this.webClient = webClient.mutate()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceKey)
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .build();
        this.serviceKey = serviceKey;
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
}