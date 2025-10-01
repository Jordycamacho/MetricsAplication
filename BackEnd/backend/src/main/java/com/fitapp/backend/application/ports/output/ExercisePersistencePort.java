package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.ExerciseModel;
import java.util.List;
import java.util.Optional;

public interface ExercisePersistencePort {
    Optional<ExerciseModel> findById(Long id);
    List<ExerciseModel> findBySportId(Long sportId);
    List<ExerciseModel> findByUserId(Long userId);
    List<ExerciseModel> findPredefinedExercises();
    ExerciseModel save(ExerciseModel exercise);
    void deleteById(Long id);
}