package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.converter.ExerciseConverter;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import com.fitapp.backend.infrastructure.persistence.specification.ExerciseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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
    private final ExerciseConverter exerciseConverter;

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseModel> findById(Long id) {
        log.debug("PERSISTENCE_FIND_BY_ID | id={}", id);
        return exerciseRepository.findById(id)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseModel> findByIdWithRelations(Long id) {
        log.debug("PERSISTENCE_FIND_BY_ID_WITH_RELATIONS | id={}", id);
        return exerciseRepository.findByIdWithRelations(id)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAll(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_ALL | page={} | size={} | sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return exerciseRepository.findAll(pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findByFilters(ExerciseFilterRequest filters, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_BY_FILTERS | page={} | size={} | filters={}",
                pageable.getPageNumber(), pageable.getPageSize(), filters);

        Specification<ExerciseEntity> spec = buildSpecification(filters);
        Page<ExerciseEntity> page = exerciseRepository.findAll(spec, pageable);

        log.debug("PERSISTENCE_FIND_BY_FILTERS_RESULT | totalElements={} | totalPages={}",
                page.getTotalElements(), page.getTotalPages());

        return page.map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExerciseModel> findByNameAndCreatedById(String name, Long createdById) {
        log.debug("PERSISTENCE_FIND_BY_NAME_AND_CREATOR | name={} | createdById={}", name, createdById);
        return exerciseRepository.findByNameAndCreatedById(name, createdById)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findByCreatedById(Long createdById, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_BY_CREATED_BY | createdById={} | page={} | size={}",
                createdById, pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findByCreatedById(createdById, pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findBySportId(Long sportId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_BY_SPORT | sportId={} | page={} | size={}",
                sportId, pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findBySportId(sportId, pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAvailableForUser(Long userId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_AVAILABLE_FOR_USER | userId={} | page={} | size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findAvailableForUser(userId, pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findAvailableForUserAndSport(Long userId, Long sportId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_AVAILABLE_FOR_USER_AND_SPORT | userId={} | sportId={} | page={} | size={}",
                userId, sportId, pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findAvailableForUserAndSport(userId, sportId, pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findRecentlyUsed(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_RECENTLY_USED | page={} | size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findRecentlyUsed(pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findMostPopular(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_MOST_POPULAR | page={} | size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findMostPopular(pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseModel> findTopRated(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_TOP_RATED | page={} | size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return exerciseRepository.findTopRated(pageable)
                .map(exerciseConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseModel> findInactiveBefore(LocalDateTime cutoffDate) {
        log.debug("PERSISTENCE_FIND_INACTIVE_BEFORE | cutoffDate={}", cutoffDate);
        return exerciseRepository.findInactiveBefore(cutoffDate).stream()
                .map(exerciseConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllDistinctDifficultyLevels() {
        log.debug("PERSISTENCE_FIND_ALL_DISTINCT_DIFFICULTY_LEVELS");
        // Asumiendo que tienes un campo 'difficultyLevel' en ExerciseEntity
        // Si no, puedes remover este método o implementar lógica alternativa
        return List.of(); // Placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllDistinctEquipment() {
        log.debug("PERSISTENCE_FIND_ALL_DISTINCT_EQUIPMENT");
        // Asumiendo que tienes un campo 'equipment' en ExerciseEntity
        return List.of(); // Placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByUser(Long userId) {
        log.debug("PERSISTENCE_COUNT_BY_USER | userId={}", userId);
        return exerciseRepository.countByUser(userId);
    }

    @Override
    @Transactional
    public ExerciseModel save(ExerciseModel exerciseModel) {
        log.debug("PERSISTENCE_SAVE | exerciseId={} | exerciseName={} | sportId={}",
                exerciseModel.getId(), exerciseModel.getName(), exerciseModel.getSportId());

        try {
            ExerciseEntity entity;

            if (exerciseModel.getId() != null) {
                // Update existing entity
                entity = exerciseRepository.findById(exerciseModel.getId())
                        .orElseThrow(() -> {
                            log.error("EXERCISE_NOT_FOUND_FOR_UPDATE | id={}", exerciseModel.getId());
                            return new RuntimeException("Exercise not found with id: " + exerciseModel.getId());
                        });

                // Update fields
                entity.setName(exerciseModel.getName());
                entity.setDescription(exerciseModel.getDescription());
                entity.setExerciseType(exerciseModel.getExerciseType());
                entity.setIsActive(exerciseModel.getIsActive());
                entity.setIsPublic(exerciseModel.getIsPublic());
                entity.setUsageCount(exerciseModel.getUsageCount());
                entity.setRating(exerciseModel.getRating());
                entity.setRatingCount(exerciseModel.getRatingCount());
                entity.setLastUsedAt(exerciseModel.getLastUsedAt());

                log.debug("PERSISTENCE_UPDATE_EXISTING | id={}", exerciseModel.getId());
            } else {
                // Create new entity
                entity = exerciseConverter.toEntity(exerciseModel);
                entity.setCreatedAt(LocalDateTime.now());
                log.debug("PERSISTENCE_CREATE_NEW | name={}", exerciseModel.getName());
            }

            // Set relations
            setEntityRelations(entity, exerciseModel);

            // Save entity
            ExerciseEntity savedEntity = exerciseRepository.save(entity);
            log.info("PERSISTENCE_SAVE_SUCCESS | id={} | name={} | sport={}",
                    savedEntity.getId(), savedEntity.getName(),
                    savedEntity.getSport() != null ? savedEntity.getSport().getName() : "null");

            return exerciseConverter.toDomain(savedEntity);

        } catch (Exception e) {
            log.error("PERSISTENCE_SAVE_ERROR | exerciseName={} | error={}",
                    exerciseModel.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save exercise: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("PERSISTENCE_DELETE | id={}", id);

        try {
            if (!exerciseRepository.existsById(id)) {
                log.warn("PERSISTENCE_DELETE_NOT_FOUND | id={}", id);
                throw new RuntimeException("Exercise not found with id: " + id);
            }

            exerciseRepository.deleteById(id);
            log.info("PERSISTENCE_DELETE_SUCCESS | id={}", id);

        } catch (Exception e) {
            log.error("PERSISTENCE_DELETE_ERROR | id={} | error={}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete exercise: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void incrementUsageCount(Long exerciseId) {
        log.debug("PERSISTENCE_INCREMENT_USAGE_COUNT | exerciseId={}", exerciseId);

        try {
            exerciseRepository.incrementUsageCount(exerciseId);
            log.debug("PERSISTENCE_INCREMENT_USAGE_COUNT_SUCCESS | exerciseId={}", exerciseId);
        } catch (Exception e) {
            log.error("PERSISTENCE_INCREMENT_USAGE_COUNT_ERROR | exerciseId={} | error={}",
                    exerciseId, e.getMessage(), e);
            throw new RuntimeException("Failed to increment usage count: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void addRating(Long exerciseId, Double rating) {
        log.debug("PERSISTENCE_ADD_RATING | exerciseId={} | rating={}", exerciseId, rating);

        try {
            exerciseRepository.addRating(exerciseId, rating);
            log.debug("PERSISTENCE_ADD_RATING_SUCCESS | exerciseId={} | rating={}", exerciseId, rating);
        } catch (Exception e) {
            log.error("PERSISTENCE_ADD_RATING_ERROR | exerciseId={} | rating={} | error={}",
                    exerciseId, rating, e.getMessage(), e);
            throw new RuntimeException("Failed to add rating: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("PERSISTENCE_EXISTS_BY_ID | id={}", id);
        return exerciseRepository.existsById(id);
    }

    @Override
    public Optional<ExerciseEntity> findEntityById(Long id) {
        return exerciseRepository.findById(id);
    }

    private Specification<ExerciseEntity> buildSpecification(ExerciseFilterRequest filters) {
        return ExerciseSpecification.withFilters(
                filters.getSearch(),
                filters.getExerciseType(),
                filters.getSportId(),
                filters.getCategoryId(),
                filters.getParameterId(),
                filters.getIsActive(),
                filters.getIsPublic(),
                filters.getCreatedBy(),
                null,
                filters.getMinRating());
    }

    private void setEntityRelations(ExerciseEntity entity, ExerciseModel model) {
        if (model.getSportId() != null) {
            SportEntity sport = sportRepository.findById(model.getSportId())
                    .orElseThrow(() -> {
                        log.error("SPORT_NOT_FOUND | sportId={}", model.getSportId());
                        return new RuntimeException("Sport not found with id: " + model.getSportId());
                    });
            entity.setSport(sport);
        }

        if (model.getCreatedById() != null) {
            UserEntity user = userRepository.findById(model.getCreatedById())
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND | userId={}", model.getCreatedById());
                        return new UserNotFoundException(model.getCreatedById());
                    });
            entity.setCreatedBy(user);
        }

        if (model.getCategoryIds() != null && !model.getCategoryIds().isEmpty()) {
            Set<ExerciseCategoryEntity> categories = new HashSet<>(
                    categoryRepository.findAllById(model.getCategoryIds()));
            if (categories.size() != model.getCategoryIds().size()) {
                log.warn("CATEGORIES_NOT_FULLY_LOADED | expected={} | found={}",
                        model.getCategoryIds().size(), categories.size());
            }
            entity.setCategories(categories);
        }

        if (model.getSupportedParameterIds() != null && !model.getSupportedParameterIds().isEmpty()) {
            Set<CustomParameterEntity> parameters = new HashSet<>(
                    parameterRepository.findAllById(model.getSupportedParameterIds()));
            if (parameters.size() != model.getSupportedParameterIds().size()) {
                log.warn("PARAMETERS_NOT_FULLY_LOADED | expected={} | found={}",
                        model.getSupportedParameterIds().size(), parameters.size());
            }
            entity.setSupportedParameters(parameters);
        }
    }
}