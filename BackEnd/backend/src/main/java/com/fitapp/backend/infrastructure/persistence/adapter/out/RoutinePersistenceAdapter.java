package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.routine.request.RoutineFilterRequest;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Page<RoutineModel> findByUserId(Long userId, Pageable pageable) {
        return routineRepository.findByUserId(userId, pageable)
                .map(routineConverter::toDomain);
    }


    //===========================ERROR AQUI===========================
    @Override
    public Page<RoutineModel> findByUserIdAndFilters(Long userId, RoutineFilterRequest filters, Pageable pageable) {
        
        Specification<RoutineEntity> spec = Specification
                .where((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));

        if (filters.getSportId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sport").get("id"), filters.getSportId()));
        }

        if (filters.getIsActive() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), filters.getIsActive()));
        }

        if (filters.getName() != null && !filters.getName().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")),
                    "%" + filters.getName().toLowerCase() + "%"));
        }

        //return routineRepository.findAll(pageable, spec)
        return routineRepository.findAll(pageable)
                .map(routineConverter::toDomain);
    }

    @Override
    public List<RoutineModel> findRecentByUserId(Long userId, int limit) {
        return routineRepository.findRecentByUserId(userId, limit).stream()
                .map(routineConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoutineModel> findActiveRoutinesByUserId(Long userId) {
        return routineRepository.findByUserIdAndIsActive(userId, true).stream()
                .map(routineConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(Long userId) {
        return routineRepository.countByUserId(userId);
    }

    @Override
    public RoutineModel update(RoutineModel routine) {
        RoutineEntity existing = routineRepository.findByIdAndUserId(routine.getId(), routine.getUserId())
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        
        // Actualizar campos
        existing.setName(routine.getName());
        existing.setDescription(routine.getDescription());
        existing.setTrainingDays(routine.getTrainingDays());
        existing.setGoal(routine.getGoal());
        existing.setSessionsPerWeek(routine.getSessionsPerWeek());
        existing.setIsActive(routine.getIsActive());
        
        if (routine.getSportId() != null) {
            // El sport se actualiza en el converter
        }
        
        RoutineEntity updated = routineRepository.save(existing);
        return routineConverter.toDomain(updated);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(Long id, Long userId) {
        RoutineEntity routine = routineRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        routineRepository.delete(routine);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Long id, Long userId, boolean isActive) {
        int updated = routineRepository.updateActiveStatus(id, userId, isActive);
        if (updated == 0) {
            throw new RuntimeException("Routine not found or not authorized");
        }
    }
}