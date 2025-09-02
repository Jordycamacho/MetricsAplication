package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.ports.input.SportUseCase;
import com.fitapp.backend.domain.model.SportModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {
    private final SportUseCase sportService;

    @GetMapping
    public ResponseEntity<List<SportModel>> getAllSports() {
        return ResponseEntity.ok(sportService.getAllSports());
    }

    @GetMapping("/predefined")
    public ResponseEntity<List<SportModel>> getPredefinedSports() {
        return ResponseEntity.ok(sportService.getPredefinedSports());
    }

    @GetMapping("/custom")
    public ResponseEntity<List<SportModel>> getUserSports(@AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok(sportService.getUserSports(userEmail));
    }

    @PostMapping("/custom")
    public ResponseEntity<SportModel> createCustomSport(
            @RequestBody SportModel sportModel,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok(sportService.createCustomSport(sportModel, userEmail));
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteCustomSport(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        sportService.deleteCustomSport(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}