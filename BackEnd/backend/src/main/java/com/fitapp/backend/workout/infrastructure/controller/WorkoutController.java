package com.fitapp.backend.workout.infrastructure.controller;

import com.fitapp.backend.workout.aplication.dto.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.workout.aplication.dto.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.workout.aplication.dto.response.LastExerciseValuesResponse;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionResponse;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.workout.aplication.port.input.WorkoutHistoryUseCase;
import com.fitapp.backend.workout.aplication.port.input.WorkoutUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workout", description = "Workout session management endpoints")
public class WorkoutController {

    private final WorkoutUseCase workoutUseCase;
    private final WorkoutHistoryUseCase workoutHistoryUseCase;

    @PostMapping("/sessions")
    @Operation(summary = "Save completed workout session", description = "Saves a workout session with all executed sets and parameters")
    public ResponseEntity<WorkoutSessionResponse> saveWorkoutSession(
            @RequestBody SaveWorkoutSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_SAVE | userId={} | routineId={} | setCount={}", userId, request.getRoutineId(),
                request.getSetExecutions().size());

        WorkoutSessionResponse response = workoutUseCase.saveWorkoutSession(request, userId);

        log.info("WORKOUT_CONTROLLER_SAVE_SUCCESS | sessionId={} | userId={}",
                response.getId(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get workout session details")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionDetails(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_GET_DETAILS | sessionId={} | userId={}", sessionId, userId);

        WorkoutSessionResponse response = workoutUseCase.getWorkoutSessionDetails(sessionId, userId);

        log.info("WORKOUT_CONTROLLER_GET_DETAILS_SUCCESS | sessionId={}", sessionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get workout history")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getWorkoutHistory(
            @Parameter(hidden = true) WorkoutHistoryFilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_GET_HISTORY | userId={} | page={} | size={} | filters={}",
                userId, page, size, filters);

        Pageable pageable = PageRequest.of(page, size);
        Page<WorkoutSessionSummaryResponse> response = workoutUseCase.getWorkoutHistory(filters, userId, pageable);

        log.info("WORKOUT_CONTROLLER_GET_HISTORY_SUCCESS | userId={} | totalElements={}",
                userId, response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent workouts")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getRecentWorkouts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_GET_RECENT | userId={} | limit={}", userId, limit);

        Page<WorkoutSessionSummaryResponse> response = workoutUseCase.getRecentWorkouts(userId, limit);

        log.info("WORKOUT_CONTROLLER_GET_RECENT_SUCCESS | userId={} | count={}",
                userId, response.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/start/{routineId}/last-values")
    @Operation(
        summary = "Get last values before starting workout",
        description = "Returns last recorded values for all exercises in routine before starting a new session"
    )
    public ResponseEntity<Map<Long, LastExerciseValuesResponse>> getLastValuesBeforeWorkout(
            @PathVariable Long routineId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("GET_LAST_VALUES_BEFORE_WORKOUT | routineId={} | userId={}", routineId, userId);

        Map<Long, LastExerciseValuesResponse> response = workoutHistoryUseCase.getLastValuesForRoutine(routineId, userId);

        return ResponseEntity.
        ok(response);
    }
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete workout session")
    public ResponseEntity<Void> deleteWorkoutSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_DELETE | sessionId={} | userId={}", sessionId, userId);

        workoutUseCase.deleteWorkoutSession(sessionId, userId);

        log.info("WORKOUT_CONTROLLER_DELETE_SUCCESS | sessionId={}", sessionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/total-volume")
    @Operation(summary = "Get total volume")
    public ResponseEntity<Double> getTotalVolume(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }

        log.info("WORKOUT_CONTROLLER_GET_TOTAL_VOLUME | userId={}", userId);

        Double volume = workoutUseCase.getTotalVolume(userId);

        log.info("WORKOUT_CONTROLLER_GET_TOTAL_VOLUME_SUCCESS | userId={} | volume={}",
                userId, volume);

        return ResponseEntity.ok(volume);
    }
}