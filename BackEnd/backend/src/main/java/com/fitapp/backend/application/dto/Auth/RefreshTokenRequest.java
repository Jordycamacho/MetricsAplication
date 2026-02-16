package com.fitapp.backend.application.dto.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank @JsonProperty("refreshToken") String refreshToken
) {}