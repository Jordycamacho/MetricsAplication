package com.fitapp.backend.infrastructure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitapp.backend.application.dto.routine.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.ports.input.RoutineExerciseUseCase;
import com.fitapp.backend.application.ports.input.RoutineUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/routines/{routineId}/exercises")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routine Exercises", description = "Routine exercise management endpoints")
public class RoutineExerciseController {
    
    private final RoutineExerciseUseCase routineExerciseUseCase;
    private final RoutineUseCase routineUseCase;
    
    @Operation(summary = "Add exercise to routine", description = "Adds an exercise to a routine with session and order configuration")
    @PostMapping
    public ResponseEntity<RoutineExerciseResponse> addExerciseToRoutine(
            @PathVariable Long routineId,
            @Valid @RequestBody AddExerciseToRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Received add exercise request for routine {}: {}", routineId, request);
        String userEmail = jwt.getClaimAsString("email");
        
        RoutineExerciseResponse response = routineExerciseUseCase.addExerciseToRoutine(
                routineId, request, userEmail);
        
        routineUseCase.markRoutineAsUsed(routineId, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Update exercise in routine", description = "Updates an exercise configuration in a routine")
    @PutMapping("/{exerciseId}")
    public ResponseEntity<RoutineExerciseResponse> updateExerciseInRoutine(
            @PathVariable Long routineId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody AddExerciseToRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Received update exercise request for routine {}, exercise {}: {}", 
                routineId, exerciseId, request);
        String userEmail = jwt.getClaimAsString("email");
        
        RoutineExerciseResponse response = routineExerciseUseCase.updateExerciseInRoutine(
                routineId, exerciseId, request, userEmail);
        
        routineUseCase.markRoutineAsUsed(routineId, userEmail);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Remove exercise from routine", description = "Removes an exercise from a routine")
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> removeExerciseFromRoutine(
            @PathVariable Long routineId,
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Received remove exercise request for routine {}, exercise {}", routineId, exerciseId);
        String userEmail = jwt.getClaimAsString("email");
        
        routineExerciseUseCase.removeExerciseFromRoutine(routineId, exerciseId, userEmail);

        routineUseCase.markRoutineAsUsed(routineId, userEmail);
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get exercises by session", description = "Get exercises for a specific session number")
    @GetMapping("/session/{sessionNumber}")
    public ResponseEntity<List<RoutineExerciseResponse>> getExercisesBySession(
            @PathVariable Long routineId,
            @PathVariable Integer sessionNumber,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.debug("Getting exercises for routine {}, session {}", routineId, sessionNumber);
        String userEmail = jwt.getClaimAsString("email");
        
        List<RoutineExerciseResponse> response = routineExerciseUseCase
                .getExercisesBySession(routineId, sessionNumber, userEmail);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get exercises by day", description = "Get exercises for a specific day of week")
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<RoutineExerciseResponse>> getExercisesByDay(
            @PathVariable Long routineId,
            @PathVariable String dayOfWeek,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.debug("Getting exercises for routine {}, day {}", routineId, dayOfWeek);
        String userEmail = jwt.getClaimAsString("email");
        
        List<RoutineExerciseResponse> response = routineExerciseUseCase
                .getExercisesByDay(routineId, dayOfWeek, userEmail);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Reorder exercises", description = "Reorder exercises in a routine")
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderExercises(
            @PathVariable Long routineId,
            @RequestBody List<Long> exerciseIds,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Reordering exercises for routine {}: {}", routineId, exerciseIds);
        String userEmail = jwt.getClaimAsString("email");
        
        routineExerciseUseCase.reorderExercises(routineId, exerciseIds, userEmail);
        routineUseCase.markRoutineAsUsed(routineId, userEmail);
        
        return ResponseEntity.noContent().build();
    }
}
