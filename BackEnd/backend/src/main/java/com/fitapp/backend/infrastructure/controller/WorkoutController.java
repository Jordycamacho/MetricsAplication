package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.workout.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.application.ports.input.WorkoutUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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