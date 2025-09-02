package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.routine.CreateRoutineRequest;
import com.fitapp.backend.application.dto.routine.RoutineResponse;

public interface RoutineUseCase {
    RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail);
    RoutineResponse getRoutineById(Long id, String userEmail);
}