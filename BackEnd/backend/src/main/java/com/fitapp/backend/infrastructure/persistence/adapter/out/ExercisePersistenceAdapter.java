package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.domain.exception.ExerciseNotFoundException;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.converter.ExerciseConverter;
import com.fitapp.backend.infrastructure.persistence.entity.*;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import com.fitapp.backend.infrastructure.persistence.specification.ExerciseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExercisePersistenceAdapter implements ExercisePersistencePort {

    private final ExerciseRepository exerciseRepository;
    private final SportRepository sportRepository;
    private final SpringDataUserRepository userRepository;
    private final ExerciseCategoryRepository categoryRepository;
    private final CustomParameterRepository parameterRepository;
    private final ExerciseRatingRepository exerciseRatingRepository;
    private final ExerciseConverter exerciseConverter;

    // ---- Queries ----

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseModel> findById(Long id) {
        return exerciseRepository.findByIdWithRelations(id).map(exerciseConverter::toDomain);
    }

    @Override
    public Optional<Long> findIdByName(String name) {
        return exerciseRepository.findIdByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAll(Pageable pageable) {
        return exerciseRepository.findAll(pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findByFilters(ExerciseFilterRequest filters, Pageable pageable) {
        Specification<ExerciseEntity> spec = ExerciseSpecification.withFilters(
                filters.getSearch(), filters.getExerciseType(), filters.getSportId(),
                filters.getCategoryId(), filters.getParameterId(), filters.getIsActive(),
                filters.getIsPublic(), filters.getCreatedBy(), null, filters.getMinRating());
        return exerciseRepository.findAll(spec, pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseModel> findByNameAndCreatedById(String name, Long createdById) {
        return exerciseRepository.findByNameAndCreatedById(name, createdById).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findByCreatedById(Long createdById, Pageable pageable) {
        return exerciseRepository.findByCreatedById(createdById, pageable).map(exerciseConverter::toDomain);
    }

    @Override
    public String findNameById(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .map(ExerciseEntity::getName)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAvailableForUser(Long userId, Pageable pageable) {
        return exerciseRepository.findAvailableForUser(userId, pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAvailableForUserAndSport(Long userId, Long sportId, Pageable pageable) {
        return exerciseRepository.findAvailableForUserAndSport(userId, sportId, pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findRecentlyUsedByUser(Long userId, Pageable pageable) {
        return exerciseRepository.findRecentlyUsedByUser(userId, pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findMostPopular(Pageable pageable) {
        return exerciseRepository.findMostPopular(pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findTopRated(Pageable pageable) {
        return exerciseRepository.findTopRated(pageable).map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseModel> findInactiveBefore(LocalDateTime cutoffDate) {
        return exerciseRepository.findInactiveBefore(cutoffDate).stream()
                .map(exerciseConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByUser(Long userId) {
        return exerciseRepository.countByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return exerciseRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndCreatedByIdExcluding(String name, Long createdById, Long excludeId) {
        return exerciseRepository.existsByNameAndCreatedByIdAndIdNot(name, createdById, excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserRated(Long exerciseId, Long userId) {
        return exerciseRatingRepository.existsByExerciseIdAndUserId(exerciseId, userId);
    }

    // ---- Commands ----

    @Override
    @Transactional
    public ExerciseModel save(ExerciseModel model) {
        ExerciseEntity entity;

        if (model.getId() != null) {
            entity = exerciseRepository.findByIdWithRelations(model.getId())
                    .orElseThrow(() -> new ExerciseNotFoundException(model.getId()));
            entity.setName(model.getName());
            entity.setDescription(model.getDescription());
            entity.setExerciseType(model.getExerciseType());
            entity.setIsActive(model.getIsActive());
            entity.setIsPublic(model.getIsPublic());
            entity.setLastUsedAt(model.getLastUsedAt());
        } else {
            entity = exerciseConverter.toEntity(model);
        }

        setRelations(entity, model);

        ExerciseEntity saved = exerciseRepository.save(entity);
        log.info("EXERCISE_SAVED | id={} | name={} | sports={}",
                saved.getId(), saved.getName(),
                saved.getSports().stream().map(SportEntity::getName).collect(Collectors.joining(",")));

        return exerciseConverter.toDomain(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!exerciseRepository.existsById(id))
            throw new ExerciseNotFoundException(id);
        exerciseRepository.deleteById(id);
        log.info("EXERCISE_DELETED | id={}", id);
    }

    @Override
    @Transactional
    public void incrementUsageCount(Long exerciseId) {
        exerciseRepository.incrementUsageCount(exerciseId);
    }

    @Override
    @Transactional
    public void saveRating(Long exerciseId, Long userId, Double rating) {
        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        exerciseRatingRepository.save(ExerciseRatingEntity.builder()
                .exercise(exercise).user(user).rating(rating).build());

        exerciseRepository.updateRating(exerciseId, rating);
        log.info("EXERCISE_RATED | exerciseId={} | userId={} | rating={}", exerciseId, userId, rating);
    }

    // ---- Helpers ----

    private void setRelations(ExerciseEntity entity, ExerciseModel model) {
        if (model.getSports() != null && !model.getSports().isEmpty()) {
            Set<SportEntity> sports = new HashSet<>(sportRepository.findAllById(model.getSports().keySet()));
            if (sports.size() != model.getSports().size()) {
                log.warn("SPORTS_NOT_FULLY_LOADED | expected={} | found={}", model.getSports().size(), sports.size());
            }
            entity.setSports(sports);
        }

        if (model.getCreatedById() != null && entity.getCreatedBy() == null) {
            userRepository.findById(model.getCreatedById()).ifPresent(entity::setCreatedBy);
        }

        entity.setCategories(model.getCategoryIds() != null && !model.getCategoryIds().isEmpty()
                ? new HashSet<>(categoryRepository.findAllById(model.getCategoryIds()))
                : new HashSet<>());

        entity.setSupportedParameters(
                model.getSupportedParameterIds() != null && !model.getSupportedParameterIds().isEmpty()
                        ? new HashSet<>(parameterRepository.findAllById(model.getSupportedParameterIds()))
                        : new HashSet<>());
    }
}