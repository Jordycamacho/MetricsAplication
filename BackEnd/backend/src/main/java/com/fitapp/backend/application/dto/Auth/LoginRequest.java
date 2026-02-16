package com.fitapp.backend.application.dto.Auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank @JsonProperty("email") String email,
    @NotBlank @JsonProperty("password") String password
) {}