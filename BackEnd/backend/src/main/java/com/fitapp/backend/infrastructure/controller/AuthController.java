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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            Authentication authentication) {

        if (bearerToken == null && authentication instanceof JwtAuthenticationToken jwtAuth) {
            bearerToken = "Bearer " + jwtAuth.getToken().getTokenValue();
        }

        if (bearerToken != null) {
            authUseCase.logout(bearerToken);
        }

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

    @Operation(summary = "Página puente OAuth2 → deep link Android")
    @GetMapping("/oauth2/success")
    public void oauth2Success(
            @RequestParam("token") String token,
            HttpServletResponse response) throws IOException {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Redirigiendo a AppFit...</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            background: #121212;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            height: 100vh;
                            font-family: sans-serif;
                            gap: 16px;
                        }
                        .dot {
                            width: 12px; height: 12px;
                            border-radius: 50%;
                            background: #78703F;
                            animation: pulse 1.2s ease-in-out infinite;
                        }
                        .dot:nth-child(2) { animation-delay: 0.2s; }
                        .dot:nth-child(3) { animation-delay: 0.4s; }
                        .dots { display: flex; gap: 8px; }
                        @keyframes pulse {
                            0%, 100% { opacity: 0.3; transform: scale(0.8); }
                            50% { opacity: 1; transform: scale(1.2); }
                        }
                        p { color: #78703F; font-size: 15px; letter-spacing: 0.05em; }
                        .hint { color: #444; font-size: 12px; margin-top: 8px; }
                    </style>
                </head>
                <body>
                    <div class="dots">
                        <div class="dot"></div>
                        <div class="dot"></div>
                        <div class="dot"></div>
                    </div>
                    <p>Abriendo AppFit...</p>
                    <span class="hint">Puedes cerrar este navegador</span>
                    <script>
                        // Abre el deep link — Android lo captura en onNewIntent()
                        window.location.href = 'fitapp://auth/callback?token=%s';
                    </script>
                </body>
                </html>
                """.formatted(token);

        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(html);
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