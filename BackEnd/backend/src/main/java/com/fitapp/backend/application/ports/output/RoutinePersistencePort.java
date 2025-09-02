package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.RoutineModel;
import java.util.Optional;

public interface RoutinePersistencePort {
    RoutineModel save(RoutineModel routine);
    Optional<RoutineModel> findByIdAndUserId(Long id, Long userId);
}