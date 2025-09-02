package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitapp.backend.application.dto.routine.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.RoutineResponse;
import com.fitapp.backend.application.ports.input.RoutineUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineUseCase routineUseCase;

    @PostMapping("/create")
    public ResponseEntity<RoutineResponse> createRoutine(
            @Valid @RequestBody CreateRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        RoutineResponse response = routineUseCase.createRoutine(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> getRoutine(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        RoutineResponse response = routineUseCase.getRoutineById(id, userEmail);
        return ResponseEntity.ok(response);
    }
}