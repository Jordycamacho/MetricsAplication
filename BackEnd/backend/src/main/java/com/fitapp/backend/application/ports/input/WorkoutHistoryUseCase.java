package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.workout.response.LastExerciseValuesResponse;

import java.util.List;
import java.util.Map;

public interface WorkoutHistoryUseCase {
    
    /**
     * Obtiene los últimos valores registrados para un ejercicio específico.
     *
     * @param exerciseId ID del ejercicio
     * @param userId ID del usuario
     * @return Últimos valores del ejercicio, o null si nunca se ha ejecutado
     */
    LastExerciseValuesResponse getLastExerciseValues(Long exerciseId, Long userId);
    
    /**
     * Obtiene los últimos valores de múltiples ejercicios de una vez.
     * Útil para pre-cargar valores de toda una rutina.
     *
     * @param exerciseIds Lista de IDs de ejercicios
     * @param userId ID del usuario
     * @return Map con exerciseId → LastExerciseValuesResponse
     */
    Map<Long, LastExerciseValuesResponse> getLastValuesForExercises(List<Long> exerciseIds, Long userId);
    
    /**
     * Obtiene los últimos valores de todos los ejercicios de una rutina.
     *
     * @param routineId ID de la rutina
     * @param userId ID del usuario
     * @return Map con exerciseId → LastExerciseValuesResponse
     */
    Map<Long, LastExerciseValuesResponse> getLastValuesForRoutine(Long routineId, Long userId);
}