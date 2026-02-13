package com.fitapp.backend.application.dto.Auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {}