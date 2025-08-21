package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.Auth.LoginRequest;
import com.fitapp.backend.application.dto.Auth.RegisterRequest;
import com.fitapp.backend.application.dto.user.PasswordUpdateRequest;
import com.fitapp.backend.application.ports.input.AuthUseCase;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestión de autenticación")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;

    @Operation(summary = "Registrar nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authUseCase.register(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar contraseña")
    @PostMapping("/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {

        userUseCase.updatePassword(userDetails.getUserId(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refrescar token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuthResponse response = authUseCase.refreshToken(userDetails);
        return ResponseEntity.ok(response);
    }
}