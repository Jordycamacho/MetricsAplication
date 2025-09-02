package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.ExerciseModel;
import java.util.Optional;

public interface ExercisePersistencePort {
    Optional<ExerciseModel> findById(Long id);
}