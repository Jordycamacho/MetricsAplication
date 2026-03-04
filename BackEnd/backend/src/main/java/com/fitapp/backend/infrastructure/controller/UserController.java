package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.subscription.SubscriptionResponse;
import com.fitapp.backend.application.dto.user.ChangePasswordRequest;
import com.fitapp.backend.application.dto.user.UserResponse;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "Endpoints para gestión de usuarios")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    // ── Endpoints propios (/me) ─────────────────────────────────────────────

    @Operation(summary = "Obtener perfil propio")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(convertToResponse(userUseCase.findById(userId)));
    }

    @Operation(summary = "Actualizar perfil propio")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateRequest updateRequest) {
        Long userId = extractUserId(authentication);
        UserUpdateRequest safeRequest = new UserUpdateRequest();
        safeRequest.setFullName(updateRequest.getFullName());
        return ResponseEntity.ok(convertToResponse(userUseCase.updateUser(userId, safeRequest)));
    }

    @Operation(summary = "Eliminar cuenta propia (soft delete)")
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        userUseCase.softDeleteUser(extractUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar contraseña verificando la actual")
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userUseCase.changePassword(
                extractUserId(authentication),
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // ── Admin ───────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userUseCase.findAll(pageable).map(this::convertToResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(convertToResponse(userUseCase.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(convertToResponse(userUseCase.updateUser(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userUseCase.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userUseCase.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        userUseCase.toggleUserStatus(id, isActive);
        return ResponseEntity.noContent().build();
    }

    // ── Helper: extrae userId del claim JWT ─────────────────────────────────

    /**
     * Lee el claim "userId" del JWT directamente.
     * Evita depender de @AuthenticationPrincipal o details que pueden ser null
     * dependiendo de cómo Spring resuelva el principal en el resource server.
     */
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
        // Fallback para OAuth2 donde details sí es CustomUserDetails
        if (authentication.getDetails() instanceof CustomUserDetails cd) {
            return cd.getUserId();
        }
        throw new IllegalStateException("No se pudo obtener el userId del token");
    }

    // ── Conversores ──────────────────────────────────────────────────────────

    private UserResponse convertToResponse(UserModel u) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole())
                .isActive(u.isActive())
                .maxRoutines(u.getMaxRoutines())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt());
        if (u.getSubscription() != null) {
            builder.subscription(convertSubscriptionToResponse(u.getSubscription()));
        }
        return builder.build();
    }

    private SubscriptionResponse convertSubscriptionToResponse(SubscriptionModel s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .type(s.getType())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .maxRoutines(s.getMaxRoutines())
                .build();
    }
}