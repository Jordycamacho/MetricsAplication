package com.fitapp.backend.infrastructure.controller;

import com.fitapp.backend.application.dto.workout.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.application.ports.input.WorkoutUseCase;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workout", description = "Workout session management endpoints")
public class WorkoutController {

    private final WorkoutUseCase workoutUseCase;

    @PostMapping("/sessions")
    @Operation(summary = "Save completed workout session", 
               description = "Saves a workout session with all executed sets and parameters")
    public ResponseEntity<WorkoutSessionResponse> saveWorkoutSession(
            @Valid @RequestBody SaveWorkoutSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_SAVE | userId={} | routineId={} | setCount={}", 
                 userId, request.getRoutineId(), request.getSetExecutions().size());

        WorkoutSessionResponse response = workoutUseCase.saveWorkoutSession(request, userId);

        log.info("WORKOUT_CONTROLLER_SAVE_SUCCESS | sessionId={} | userId={}", 
                 response.getId(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get workout session details", 
               description = "Retrieves complete details of a workout session")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionDetails(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_GET_DETAILS | sessionId={} | userId={}", sessionId, userId);

        WorkoutSessionResponse response = workoutUseCase.getWorkoutSessionDetails(sessionId, userId);

        log.info("WORKOUT_CONTROLLER_GET_DETAILS_SUCCESS | sessionId={}", sessionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get workout history", 
               description = "Retrieves paginated workout history with filters")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getWorkoutHistory(
            @Parameter(hidden = true) WorkoutHistoryFilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_GET_HISTORY | userId={} | page={} | size={} | filters={}", 
                 userId, page, size, filters);

        Pageable pageable = PageRequest.of(page, size);
        Page<WorkoutSessionSummaryResponse> response = workoutUseCase.getWorkoutHistory(filters, userId, pageable);

        log.info("WORKOUT_CONTROLLER_GET_HISTORY_SUCCESS | userId={} | totalElements={}", 
                 userId, response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent workouts", 
               description = "Retrieves the most recent workout sessions")
    public ResponseEntity<Page<WorkoutSessionSummaryResponse>> getRecentWorkouts(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_GET_RECENT | userId={} | limit={}", userId, limit);

        Page<WorkoutSessionSummaryResponse> response = workoutUseCase.getRecentWorkouts(userId, limit);

        log.info("WORKOUT_CONTROLLER_GET_RECENT_SUCCESS | userId={} | count={}", 
                 userId, response.getNumberOfElements());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete workout session", 
               description = "Deletes a workout session and all its data")
    public ResponseEntity<Void> deleteWorkoutSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_DELETE | sessionId={} | userId={}", sessionId, userId);

        workoutUseCase.deleteWorkoutSession(sessionId, userId);

        log.info("WORKOUT_CONTROLLER_DELETE_SUCCESS | sessionId={}", sessionId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/total-volume")
    @Operation(summary = "Get total volume", 
               description = "Returns the user's accumulated total volume (weight × reps)")
    public ResponseEntity<Double> getTotalVolume(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUserId();
        
        log.info("WORKOUT_CONTROLLER_GET_TOTAL_VOLUME | userId={}", userId);

        Double volume = workoutUseCase.getTotalVolume(userId);

        log.info("WORKOUT_CONTROLLER_GET_TOTAL_VOLUME_SUCCESS | userId={} | volume={}", 
                 userId, volume);

        return ResponseEntity.ok(volume);
    }
}