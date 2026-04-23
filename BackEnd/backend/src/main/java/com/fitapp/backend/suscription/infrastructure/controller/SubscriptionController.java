package com.fitapp.backend.suscription.infrastructure.controller;

import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import com.fitapp.backend.suscription.aplication.dto.request.ChangePlanRequest;
import com.fitapp.backend.suscription.aplication.dto.response.SubscriptionResponse;
import com.fitapp.backend.suscription.aplication.port.input.SubscriptionUseCase;
import com.fitapp.backend.suscription.domain.model.SubscriptionModel;

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

@Tag(name = "Subscriptions", description = "Gestión de planes de suscripción")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;

    @Operation(summary = "Ver mi suscripción actual")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> getMySubscription(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(toResponse(subscriptionUseCase.getMySubscription(userId)));
    }

    @Operation(summary = "Cancelar mi suscripción")
    @PostMapping("/me/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> cancelMySubscription(
            Authentication authentication,
            @RequestParam(required = false) String reason) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(toResponse(
                subscriptionUseCase.cancelSubscription(userId, reason)));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "Cambiar plan de un usuario (admin)")
    @PostMapping("/admin/{userId}/change-plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> changePlan(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePlanRequest request) {
        return ResponseEntity.ok(toResponse(
                subscriptionUseCase.changePlan(
                        userId,
                        request.getNewType(),
                        "admin",
                        request.getNotes())));
    }

    // ── DTOs y helpers ───────────────────────────────────────────────────────

    private SubscriptionResponse toResponse(SubscriptionModel s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .type(s.getType())
                .status(s.getStatus())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .maxRoutines(s.getMaxRoutines())
                .canExportRoutines(s.canExportRoutines())
                .canAccessMarketplace(s.canAccessMarketplace())
                .autoRenew(s.isAutoRenew())
                .build();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaim("userId");
            if (claim instanceof Long l) return l;
            if (claim instanceof Integer i) return i.longValue();
            if (claim instanceof Number n) return n.longValue();
        }
        if (authentication.getDetails() instanceof CustomUserDetails cd) {
            return cd.getUserId();
        }
        throw new IllegalStateException("No se pudo obtener el userId del token");
    }
}