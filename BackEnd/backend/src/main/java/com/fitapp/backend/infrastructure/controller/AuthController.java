package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.Auth.LoginRequest;
import com.fitapp.backend.application.dto.Auth.RefreshTokenRequest;
import com.fitapp.backend.application.dto.Auth.RegisterRequest;
import com.fitapp.backend.application.dto.user.PasswordUpdateRequest;
import com.fitapp.backend.application.ports.input.AuthUseCase;
import com.fitapp.backend.application.ports.input.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(authUseCase.register(request));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @Operation(summary = "Cerrar sesión — invalida el token en Redis")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        authUseCase.logout(bearerToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verificar correo electrónico")
    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authUseCase.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reenviar correo de verificación")
    @PostMapping("/resend-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resendVerification(Authentication authentication) {
        Long userId = extractUserId(authentication);
        authUseCase.resendVerificationEmail(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar contraseña (admin/reset)")
    @PostMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request) {
        Long userId = extractUserId(authentication);
        userUseCase.updatePassword(userId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refrescar token de acceso")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authUseCase.refreshAccessToken(request.refreshToken()));
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private Long extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaim("userId");
            if (claim instanceof Long l)
                return l;
            if (claim instanceof Integer i)
                return i.longValue();
            if (claim instanceof Number n)
                return n.longValue();
        }
        throw new IllegalStateException("No se pudo obtener el userId del token");
    }
}