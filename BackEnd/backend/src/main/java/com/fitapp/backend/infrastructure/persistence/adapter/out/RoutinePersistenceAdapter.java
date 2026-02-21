package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.routine.request.RoutineFilterRequest;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutinePersistenceAdapter implements RoutinePersistencePort {
    private final RoutineRepository routineRepository;
    private final RoutineConverter routineConverter;
    private final SportRepository sportRepository;

    @Override
    public RoutineModel save(RoutineModel routine) {
        RoutineEntity entity = routineConverter.toEntity(routine);
        RoutineEntity savedEntity = routineRepository.save(entity);
        return routineConverter.toDomain(savedEntity);
    }

    @Override
    public Optional<RoutineModel> findFullRoutineByIdAndUserId(Long id, Long userId) {

        Optional<RoutineEntity> routineOpt = routineRepository.findRoutineWithExercisesAndSets(id, userId);

        if (routineOpt.isEmpty())
            return Optional.empty();

        RoutineEntity routine = routineOpt.get();

        List<Long> setIds = routine.getExercises().stream()
                .flatMap(e -> e.getSets().stream())
                .map(RoutineSetTemplateEntity::getId)
                .toList();

        if (!setIds.isEmpty()) {
            List<RoutineSetParameterEntity> parameters = routineRepository.findParametersBySetIds(setIds);

            Map<Long, List<RoutineSetParameterEntity>> grouped = parameters.stream().collect(Collectors.groupingBy(
                    p -> p.getSetTemplate().getId()));

            routine.getExercises().forEach(
                    e -> e.getSets().forEach(s -> s.setParameters(grouped.getOrDefault(s.getId(), List.of()))));
        }

        return Optional.of(routineConverter.toDomain(routine));
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

    @Override
    public Page<RoutineModel> findByUserIdAndFilters(Long userId, RoutineFilterRequest filters, Pageable pageable) {
        @SuppressWarnings("removal")
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

        return routineRepository.findAll(spec, pageable)
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
    @Transactional
    public RoutineModel update(RoutineModel routine) {
        RoutineEntity existing = routineRepository.findByIdAndUserId(routine.getId(), routine.getUserId())
                .orElseThrow(() -> new RuntimeException("Routine not found"));

        System.out.println("DEBUG - Update Routine:");
        System.out.println("  - Name: " + routine.getName());
        System.out.println("  - SportId: " + routine.getSportId());
        System.out.println("  - TrainingDays: " + routine.getTrainingDays());
        System.out.println("  - SessionsPerWeek: " + routine.getSessionsPerWeek());
        System.out.println("  - Goal: " + routine.getGoal());

        existing.setName(routine.getName());
        existing.setDescription(routine.getDescription());
        existing.setTrainingDays(routine.getTrainingDays() != null ? routine.getTrainingDays() : new HashSet<>());
        existing.setGoal(routine.getGoal());
        existing.setSessionsPerWeek(routine.getSessionsPerWeek() != null ? routine.getSessionsPerWeek() : 3);
        existing.setIsActive(routine.getIsActive());
        existing.setLastUsedAt(routine.getLastUsedAt());

        if (routine.getSportId() != null) {
            SportEntity sport = sportRepository.findById(routine.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found with id: " + routine.getSportId()));
            existing.setSport(sport);
            System.out.println("  - Sport updated to: " + sport.getName());
        } else {
            existing.setSport(null);
            System.out.println("  - Sport set to null");
        }

        RoutineEntity updated = routineRepository.save(existing);
        System.out.println("DEBUG - After save:");
        System.out.println("  - Sport: " + (existing.getSport() != null ? existing.getSport().getId() : "null"));
        System.out.println("  - TrainingDays: " + existing.getTrainingDays());
        System.out.println("  - SessionsPerWeek: " + existing.getSessionsPerWeek());
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

    @Override
    public List<RoutineModel> findLastUsedByUserId(Long userId, int limit) {
        return routineRepository.findLastUsedByUserId(userId, limit).stream()
                .map(routineConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateLastUsedAt(Long id, Long userId, LocalDateTime lastUsedAt) {
        int updated = routineRepository.updateLastUsedAt(id, userId, lastUsedAt);
        if (updated == 0) {
            throw new RuntimeException("Routine not found or not authorized");
        }
    }
}