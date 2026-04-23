package com.fitapp.backend.auth.aplication.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    @JsonProperty("token")
    private String token;
    @JsonProperty("refreshToken")
    private String refreshToken;
    @JsonProperty("expiresAt")
    private Instant expiresAt;
}