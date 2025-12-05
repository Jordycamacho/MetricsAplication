package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.ErrorResponse;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routines", description = "Routine management endpoints")
public class RoutineController {
    private final RoutineUseCase routineUseCase;

    @Operation(summary = "Create a new routine", description = "Creates a personalized workout routine for the authenticated user", responses = {
            @ApiResponse(responseCode = "200", description = "Routine created successfully", content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Subscription limit reached")
    })
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