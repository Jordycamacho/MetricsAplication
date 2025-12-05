package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fitapp.backend.application.dto.exercise.AddExercisesToRoutineRequest;
import com.fitapp.backend.application.dto.routine.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.RoutineResponse;
import com.fitapp.backend.application.ports.input.RoutineUseCase;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Slf4j
public class RoutineController {
    private final RoutineUseCase routineUseCase;

    @PostMapping("/create")
    public ResponseEntity<RoutineResponse> createRoutine(
            @Valid @RequestBody CreateRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Received request: {}", request);
        log.info("Sport ID: {}", request.getSportId());
        log.info("Sessions Per Week: {}", request.getSessionsPerWeek());

        if (request.getTrainingDays() != null) {
            for (String day : request.getTrainingDays()) {
                try {
                    DayOfWeek.valueOf(day.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid day of week: " + day);
                }
            }
        }

        String userEmail = jwt.getClaimAsString("email");
        RoutineResponse response = routineUseCase.createRoutine(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exercises")
    public ResponseEntity<RoutineResponse> addExercisesToRoutine(
            @Valid @RequestBody AddExercisesToRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        RoutineResponse response = routineUseCase.addExercisesToRoutine(request, userEmail);
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