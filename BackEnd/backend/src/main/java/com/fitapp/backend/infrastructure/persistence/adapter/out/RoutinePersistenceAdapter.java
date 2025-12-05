package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@RequiredArgsConstructor
public class RoutinePersistenceAdapter implements RoutinePersistencePort {
    private final RoutineRepository routineRepository;
    private final RoutineConverter routineConverter;

    @Override
    public RoutineModel save(RoutineModel routine) {
        RoutineEntity entity = routineConverter.toEntity(routine);
        RoutineEntity savedEntity = routineRepository.save(entity);
        return routineConverter.toDomain(savedEntity);
    }

    @Override
    public Optional<RoutineModel> findByIdAndUserId(Long id, Long userId) {
        return routineRepository.findByIdAndUserId(id, userId)
                .map(routineConverter::toDomain);
    }
}