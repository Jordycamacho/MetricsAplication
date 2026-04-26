package com.fitapp.backend.workout.aplication.port.input;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fitapp.backend.workout.aplication.dto.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.workout.aplication.dto.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionResponse;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionSummaryResponse;

public interface WorkoutUseCase {

    WorkoutSessionResponse saveWorkoutSession(SaveWorkoutSessionRequest request, Long userId);

    WorkoutSessionResponse getWorkoutSessionDetails(Long sessionId, Long userId);

    Page<WorkoutSessionSummaryResponse> getWorkoutHistory(WorkoutHistoryFilterRequest filters, Long userId, Pageable pageable);

    Page<WorkoutSessionSummaryResponse> getRecentWorkouts(Long userId, int limit);

    void deleteWorkoutSession(Long sessionId, Long userId);

    Double getTotalVolume(Long userId);
}