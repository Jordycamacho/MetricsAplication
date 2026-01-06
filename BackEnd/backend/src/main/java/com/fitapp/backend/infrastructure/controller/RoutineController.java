package com.fitapp.backend.infrastructure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.routine.request.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.request.RoutineFilterRequest;
import com.fitapp.backend.application.dto.routine.request.UpdateRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineStatisticsResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineSummaryResponse;
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

    @Operation(summary = "Get user routines", description = "Get paginated list of routines")
    @GetMapping
    public ResponseEntity<PageResponse<RoutineSummaryResponse>> getUserRoutines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        PageResponse<RoutineSummaryResponse> response = routineUseCase.getUserRoutines(
                userEmail, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent routines", description = "Get recently created routines")
    @GetMapping("/recent")
    public ResponseEntity<List<RoutineSummaryResponse>> getRecentRoutines(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        List<RoutineSummaryResponse> response = routineUseCase.getRecentRoutines(userEmail, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active routines", description = "Get all active routines")
    @GetMapping("/active")
    public ResponseEntity<List<RoutineSummaryResponse>> getActiveRoutines(
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        List<RoutineSummaryResponse> response = routineUseCase.getActiveRoutines(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<PageResponse<RoutineSummaryResponse>> getRoutinesWithFilters(
            @ModelAttribute RoutineFilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        PageResponse<RoutineSummaryResponse> response = routineUseCase.getUserRoutinesWithFilters(
                userEmail, filters, page, size);
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

    @Operation(summary = "Update routine", description = "Update an existing routine")
    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> updateRoutine(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoutineRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        RoutineResponse response = routineUseCase.updateRoutine(id, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete routine", description = "Delete a routine")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutine(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        routineUseCase.deleteRoutine(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle routine active status", description = "Activate or deactivate a routine")
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> toggleRoutineActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        routineUseCase.toggleRoutineActiveStatus(id, active, userEmail);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get routine statistics", description = "Get statistics about user routines")
    @GetMapping("/statistics")
    public ResponseEntity<RoutineStatisticsResponse> getRoutineStatistics(
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        RoutineStatisticsResponse response = routineUseCase.getRoutineStatistics(userEmail);
        return ResponseEntity.ok(response);
    }
}