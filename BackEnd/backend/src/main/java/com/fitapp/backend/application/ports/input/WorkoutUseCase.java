package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.workout.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutUseCase {
    
    /**
     * Guarda una sesión de workout completada.
     * 
     * @param request Datos de la sesión ejecutada
     * @param userId ID del usuario autenticado
     * @return Sesión guardada con detalles
     */
    WorkoutSessionResponse saveWorkoutSession(SaveWorkoutSessionRequest request, Long userId);
    
    /**
     * Obtiene detalles completos de una sesión.
     * 
     * @param sessionId ID de la sesión
     * @param userId ID del usuario autenticado
     * @return Detalles de la sesión
     */
    WorkoutSessionResponse getWorkoutSessionDetails(Long sessionId, Long userId);
    
    /**
     * Obtiene historial de workouts del usuario con filtros.
     * 
     * @param filters Filtros de búsqueda
     * @param userId ID del usuario autenticado
     * @param pageable Paginación
     * @return Página de sesiones
     */
    Page<WorkoutSessionSummaryResponse> getWorkoutHistory(WorkoutHistoryFilterRequest filters, Long userId, Pageable pageable);
    
    /**
     * Obtiene las últimas N sesiones del usuario.
     * 
     * @param userId ID del usuario
     * @param limit Número máximo de sesiones
     * @return Lista de sesiones recientes
     */
    Page<WorkoutSessionSummaryResponse> getRecentWorkouts(Long userId, int limit);
    
    /**
     * Elimina una sesión de workout.
     * 
     * @param sessionId ID de la sesión
     * @param userId ID del usuario autenticado
     */
    void deleteWorkoutSession(Long sessionId, Long userId);
    
    /**
     * Obtiene estadísticas de volumen del usuario.
     * 
     * @param userId ID del usuario
     * @return Volumen total acumulado
     */
    Double getTotalVolume(Long userId);
}