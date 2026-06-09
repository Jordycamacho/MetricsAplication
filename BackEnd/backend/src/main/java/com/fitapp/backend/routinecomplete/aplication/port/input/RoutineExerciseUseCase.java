package com.fitapp.backend.routinecomplete.aplication.port.input;

import java.util.List;

import com.fitapp.backend.routinecomplete.aplication.dto.routineexercise.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.routinecomplete.aplication.dto.routineexercise.request.ReorderSessionExercisesRequest;
import com.fitapp.backend.routinecomplete.aplication.dto.routineexercise.response.RoutineExerciseResponse;

public interface RoutineExerciseUseCase {
    RoutineExerciseResponse addExerciseToRoutine(Long routineId, AddExerciseToRoutineRequest request, String userEmail);
    RoutineExerciseResponse updateExerciseInRoutine(Long routineId, Long exerciseId, AddExerciseToRoutineRequest request, String userEmail);
    void removeExerciseFromRoutine(Long routineId, Long exerciseId, String userEmail);
    List<RoutineExerciseResponse> getExercisesBySession(Long routineId, Integer sessionNumber, String userEmail);
    List<RoutineExerciseResponse> getExercisesByDay(Long routineId, String dayOfWeek, String userEmail);
    void reorderExercises(Long routineId, List<Long> exerciseIds, String userEmail);
    void reorderSessionExercises(Long routineId, ReorderSessionExercisesRequest request, String userEmail);
    List<RoutineExerciseResponse> getRoutineExercises(Long routineId, String userEmail);
}