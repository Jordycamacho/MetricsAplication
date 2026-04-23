package com.fitapp.backend.auth.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank @JsonProperty("refreshToken") String refreshToken
) {}