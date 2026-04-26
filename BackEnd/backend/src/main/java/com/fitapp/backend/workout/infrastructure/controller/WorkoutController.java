package com.fitapp.backend.workout.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fitapp.backend.workout.aplication.dto.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.workout.aplication.dto.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionResponse;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.workout.aplication.port.input.WorkoutUseCase;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workout", description = "Workout session management")
public class WorkoutController {

    private final WorkoutUseCase workoutUseCase;

    // ── Save ──────────────────────────────────────────────────────────────────

    @PostMapping("/sessions")
    @Operation(summary = "Save a completed workout session")
    public ResponseEntity<WorkoutSessionResponse> saveWorkoutSession(
            @Valid @RequestBody SaveWorkoutSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);

        log.info("SAVE_SESSION | userId={} | routineId={} | setCount={}",
                userId, request.getRoutineId(), request.getSetExecutions().size());

        WorkoutSessionResponse response = workoutUseCase.saveWorkoutSession(request, userId);

        log.info("SAVE_SESSION_SUCCESS | sessionId={} | userId={}", response.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get full workout session details")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionDetails(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("GET_SESSION_DETAILS | sessionId={} | userId={}", sessionId, userId);

        return ResponseEntity.ok(workoutUseCase.getWorkoutSessionDetails(sessionId, userId));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated workout history with filters")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getWorkoutHistory(
            @Parameter(hidden = true) @Valid WorkoutHistoryFilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("GET_HISTORY | userId={} | page={} | size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<WorkoutSessionSummaryResponse> response = workoutUseCase.getWorkoutHistory(filters, userId, pageable);

        log.info("GET_HISTORY_SUCCESS | userId={} | totalElements={}", userId, response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent workouts (paginated)")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getRecentWorkouts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("GET_RECENT | userId={} | limit={}", userId, limit);

        return ResponseEntity.ok(workoutUseCase.getRecentWorkouts(userId, limit));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete a workout session")
    public ResponseEntity<Void> deleteWorkoutSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("DELETE_SESSION | sessionId={} | userId={}", sessionId, userId);

        workoutUseCase.deleteWorkoutSession(sessionId, userId);

        log.info("DELETE_SESSION_SUCCESS | sessionId={}", sessionId);
        return ResponseEntity.noContent().build();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats/total-volume")
    @Operation(summary = "Get total accumulated volume for user")
    public ResponseEntity<Double> getTotalVolume(@AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        return ResponseEntity.ok(workoutUseCase.getTotalVolume(userId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long extractUserId(Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId missing in token");
        }
        return userId;
    }
}