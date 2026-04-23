package com.fitapp.backend.routinecomplete.routineexercise.aplication.port.output;

import java.util.Optional;

import com.fitapp.backend.routinecomplete.routineexercise.domain.model.RoutineExerciseModel;

import java.util.List;

public interface RoutineExercisePersistencePort {
    RoutineExerciseModel save(RoutineExerciseModel routineExercise);
    Optional<RoutineExerciseModel> findByIdAndRoutineId(Long id, Long routineId);
    List<RoutineExerciseModel> findByRoutineId(Long routineId);
    Optional<RoutineExerciseModel> findById(Long id);
    void deleteByIdAndRoutineId(Long id, Long routineId);
    void deleteByRoutineId(Long routineId);
    List<RoutineExerciseModel> findByRoutineIdAndSessionNumber(Long routineId, Integer sessionNumber);
    List<RoutineExerciseModel> findByRoutineIdAndDayOfWeek(Long routineId, String dayOfWeek);
}