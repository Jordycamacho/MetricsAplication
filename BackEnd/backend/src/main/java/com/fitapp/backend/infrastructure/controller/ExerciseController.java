package com.fitapp.backend.infrastructure.controller;

import java.util.List;
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

import com.fitapp.backend.application.dto.exercise.CreateExerciseRequest;
import com.fitapp.backend.application.dto.exercise.ExerciseResponse;
import com.fitapp.backend.application.ports.input.ExerciseUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {
    private final ExerciseUseCase exerciseUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExercise(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userEmail = jwt.getClaimAsString("email");
            log.info("Received request to get exercise with ID: {} from user: {}", id, userEmail);
            
            ExerciseResponse response = exerciseUseCase.getExerciseById(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting exercise with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/sport/{sportId}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesBySport(
            @PathVariable Long sportId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userEmail = jwt.getClaimAsString("email");
            log.info("Received request to get exercises for sport ID: {} from user: {}", sportId, userEmail);
            
            List<ExerciseResponse> responses = exerciseUseCase.getExercisesBySport(sportId, userEmail);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting exercises for sport ID: {}", sportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody CreateExerciseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String userEmail = jwt.getClaimAsString("email");
            log.info("Received request to create exercise from user: {}", userEmail);
            
            ExerciseResponse response = exerciseUseCase.createExercise(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating exercise", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}