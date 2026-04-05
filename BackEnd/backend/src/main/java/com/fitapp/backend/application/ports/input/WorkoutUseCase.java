package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.workout.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutUseCase {
    WorkoutSessionResponse saveWorkoutSession(SaveWorkoutSessionRequest request, Long userId);
    WorkoutSessionResponse getWorkoutSessionDetails(Long sessionId, Long userId);
    Page<WorkoutSessionSummaryResponse> getWorkoutHistory(WorkoutHistoryFilterRequest filters, Long userId, Pageable pageable);
    Page<WorkoutSessionSummaryResponse> getRecentWorkouts(Long userId, int limit);
    void deleteWorkoutSession(Long sessionId, Long userId);
    Double getTotalVolume(Long userId);
}