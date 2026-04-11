package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.workout.response.LastExerciseValuesResponse;
import com.fitapp.backend.application.ports.input.WorkoutHistoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workout History", description = "Workout history and last values endpoints")
public class WorkoutHistoryController {

    private final WorkoutHistoryUseCase workoutHistoryUseCase;

    @GetMapping("/exercises/{exerciseId}/last-values")
    @Operation(
        summary = "Get last values for exercise",
        description = "Returns the last recorded values for a specific exercise"
    )
    public ResponseEntity<LastExerciseValuesResponse> getLastExerciseValues(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("GET_LAST_EXERCISE_VALUES | exerciseId={} | userId={}", exerciseId, userId);

        LastExerciseValuesResponse response = workoutHistoryUseCase.getLastExerciseValues(exerciseId, userId);

        if (response == null) {
            log.info("NO_PREVIOUS_VALUES | exerciseId={} | userId={}", exerciseId, userId);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/exercises/last-values")
    @Operation(
        summary = "Get last values for multiple exercises",
        description = "Returns last values for a list of exercises (batch operation)"
    )
    public ResponseEntity<Map<Long, LastExerciseValuesResponse>> getLastValuesForExercises(
            @RequestBody List<Long> exerciseIds,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("GET_LAST_VALUES_BATCH | exerciseCount={} | userId={}", exerciseIds.size(), userId);

        Map<Long, LastExerciseValuesResponse> response = 
                workoutHistoryUseCase.getLastValuesForExercises(exerciseIds, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/routines/{routineId}/last-values")
    @Operation(
        summary = "Get last values for routine",
        description = "Returns last values for all exercises in a routine"
    )
    public ResponseEntity<Map<Long, LastExerciseValuesResponse>> getLastValuesForRoutine(
            @PathVariable Long routineId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("GET_LAST_VALUES_ROUTINE | routineId={} | userId={}", routineId, userId);

        Map<Long, LastExerciseValuesResponse> response = 
                workoutHistoryUseCase.getLastValuesForRoutine(routineId, userId);

        return ResponseEntity.ok(response);
    }
}