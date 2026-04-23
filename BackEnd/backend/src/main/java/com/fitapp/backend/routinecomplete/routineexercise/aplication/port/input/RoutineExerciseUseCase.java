package com.fitapp.backend.routinecomplete.routineexercise.aplication.port.input;

import java.util.List;

import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response.RoutineExerciseResponse;

public interface RoutineExerciseUseCase {
    RoutineExerciseResponse addExerciseToRoutine(Long routineId, AddExerciseToRoutineRequest request, String userEmail);
    RoutineExerciseResponse updateExerciseInRoutine(Long routineId, Long exerciseId, AddExerciseToRoutineRequest request, String userEmail);
    void removeExerciseFromRoutine(Long routineId, Long exerciseId, String userEmail);
    List<RoutineExerciseResponse> getExercisesBySession(Long routineId, Integer sessionNumber, String userEmail);
    List<RoutineExerciseResponse> getExercisesByDay(Long routineId, String dayOfWeek, String userEmail);
    void reorderExercises(Long routineId, List<Long> exerciseIds, String userEmail);
    List<RoutineExerciseResponse> getRoutineExercises(Long routineId, String userEmail);
}