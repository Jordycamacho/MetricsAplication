package com.fitapp.backend.routinecomplete.routine.aplication.port.input;

import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.CreateRoutineRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.RoutineFilterRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.UpdateRoutineRequest;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineResponse;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineStatisticsResponse;
import com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineSummaryResponse;
import com.fitapp.backend.application.dto.page.PageResponse;
import java.util.List;
import java.util.UUID;

public interface RoutineUseCase {
    // CRUD
    RoutineResponse createRoutine(CreateRoutineRequest request, String userEmail);
    RoutineResponse getRoutineById(Long id, String userEmail);
    RoutineResponse updateRoutine(Long id, UpdateRoutineRequest request, String userEmail);
    void deleteRoutine(Long id, String userEmail);
    void toggleRoutineActiveStatus(Long id, boolean isActive, String userEmail);
    
    // Listados
    PageResponse<RoutineSummaryResponse> getUserRoutines(String userEmail, int page, int size, String sortBy, String sortDirection);
    PageResponse<RoutineSummaryResponse> getUserRoutinesWithFilters(String userEmail, RoutineFilterRequest filters, int page, int size);
    List<RoutineSummaryResponse> getLastUsedRoutines(String userEmail, int limit);
    List<RoutineSummaryResponse> getRecentRoutines(String userEmail, int limit);
    List<RoutineSummaryResponse> getActiveRoutines(String userEmail);
    RoutineResponse getRoutineForTraining(Long id, String userEmail);
    
    //Actualizacion
    void markRoutineAsUsed(Long id, String userEmail);

    // Estadísticas
    RoutineStatisticsResponse getRoutineStatistics(String userEmail);
    
    RoutineResponse getRoutineByExportKey(UUID exportKey);
    RoutineResponse importRoutineFromExportKey(UUID exportKey, String userEmail);
    void registerRoutinePurchase(Long routineId);
}