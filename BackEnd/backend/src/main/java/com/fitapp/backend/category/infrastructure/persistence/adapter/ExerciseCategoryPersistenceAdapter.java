package com.fitapp.backend.category.infrastructure.persistence.adapter;

import com.fitapp.backend.category.aplication.dto.request.ExerciseCategoryFilterRequest;
import com.fitapp.backend.category.aplication.port.output.ExerciseCategoryPersistencePort;
import com.fitapp.backend.category.domain.model.ExerciseCategoryModel;
import com.fitapp.backend.category.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.category.infrastructure.persistence.repository.ExerciseCategoryRepository;
import com.fitapp.backend.infrastructure.persistence.specification.ExerciseCategorySpecification;
import com.fitapp.backend.sport.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.user.infrastructure.persistence.repository.SpringDataUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseCategoryPersistenceAdapter implements ExerciseCategoryPersistencePort {

    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final SpringDataUserRepository userRepository;
    private final SportRepository sportRepository;

    @Override
    public Optional<ExerciseCategoryModel> findById(Long id) {
        log.debug("PERSISTENCE_ADAPTER_FIND_CATEGORY_BY_ID | id={}", id);
        return exerciseCategoryRepository.findById(id)
                .map(this::convertToModel);
    }

    @Override
    public ExerciseCategoryModel save(ExerciseCategoryModel categoryModel) {
        log.debug("PERSISTENCE_ADAPTER_SAVE_CATEGORY | id={} | name={}",
                categoryModel.getId(), categoryModel.getName());

        ExerciseCategoryEntity entity;

        if (categoryModel.getId() != null) {
            entity = exerciseCategoryRepository.findById(categoryModel.getId())
                    .orElseThrow(() -> {
                        log.error("CATEGORY_NOT_FOUND_FOR_UPDATE | id={}", categoryModel.getId());
                        return new RuntimeException("Category not found");
                    });
            updateEntityFromModel(entity, categoryModel);
        } else {
            entity = convertToEntity(categoryModel);
        }

        ExerciseCategoryEntity savedEntity = exerciseCategoryRepository.save(entity);
        log.info("PERSISTENCE_ADAPTER_SAVE_CATEGORY_SUCCESS | id={}", savedEntity.getId());

        return convertToModel(savedEntity);
    }

    @Override
    public void delete(Long id) {
        log.debug("PERSISTENCE_ADAPTER_DELETE_CATEGORY | id={}", id);

        if (!exerciseCategoryRepository.existsById(id)) {
            log.warn("CATEGORY_NOT_FOUND_FOR_DELETE | id={}", id);
            throw new RuntimeException("Category not found");
        }

        exerciseCategoryRepository.deleteById(id);
        log.info("PERSISTENCE_ADAPTER_DELETE_CATEGORY_SUCCESS | id={}", id);
    }

    @Override
    public Optional<ExerciseCategoryModel> findByNameAndOwnerId(String name, Long ownerId) {
        log.debug("PERSISTENCE_ADAPTER_FIND_BY_NAME_AND_OWNER | name={} | ownerId={}", name, ownerId);
        return exerciseCategoryRepository.findByNameAndOwnerId(name, ownerId)
                .map(this::convertToModel);
    }

    @Override
    public Optional<ExerciseCategoryModel> findByNameAndIsPredefined(String name) {
        log.debug("PERSISTENCE_ADAPTER_FIND_PREDEFINED_BY_NAME | name={}", name);
        return exerciseCategoryRepository.findByNameAndIsPredefinedTrue(name)
                .map(this::convertToModel);
    }

    @Override
    public Page<ExerciseCategoryModel> findByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("PERSISTENCE_ADAPTER_FIND_BY_OWNER | ownerId={}", ownerId);
        return exerciseCategoryRepository.findByOwnerId(ownerId, pageable)
                .map(this::convertToModel);
    }

    @Override
    public Page<ExerciseCategoryModel> findByIsPredefined(boolean isPredefined, Pageable pageable) {
        log.debug("PERSISTENCE_ADAPTER_FIND_BY_PREDEFINED | isPredefined={}", isPredefined);

        if (isPredefined) {
            return exerciseCategoryRepository.findByIsPredefinedTrue(pageable)
                    .map(this::convertToModel);
        } else {
            Specification<ExerciseCategoryEntity> spec = (root, query, cb) -> cb.equal(root.get("isPredefined"), false);
            return exerciseCategoryRepository.findAll(spec, pageable)
                    .map(this::convertToModel);
        }
    }

    @Override
    public Page<ExerciseCategoryModel> findByIsActive(boolean isActive, Pageable pageable) {
        log.debug("PERSISTENCE_ADAPTER_FIND_BY_ACTIVE | isActive={}", isActive);

        Specification<ExerciseCategoryEntity> spec = (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
        return exerciseCategoryRepository.findAll(spec, pageable)
                .map(this::convertToModel);
    }

    @Override
    public Page<ExerciseCategoryModel> findByFilters(ExerciseCategoryFilterRequest filters, Pageable pageable) {
        log.info("PERSISTENCE_ADAPTER_FIND_BY_FILTERS | search={} | isPredefined={}",
                filters.getSearch(), filters.getIsPredefined());

        Specification<ExerciseCategoryEntity> spec = ExerciseCategorySpecification.withFilters(
                filters.getSearch(),
                filters.getIsPredefined(),
                filters.getIsActive(),
                filters.getIsPublic(),
                filters.getSportId(),
                filters.getOwnerId(),
                filters.getIncludePredefined());

        Page<ExerciseCategoryEntity> result = exerciseCategoryRepository.findAll(spec, pageable);

        log.debug("PERSISTENCE_ADAPTER_FILTER_RESULT | totalElements={}", result.getTotalElements());
        return result.map(this::convertToModel);
    }

    @Override
    public Page<ExerciseCategoryModel> findAvailableForUser(Long userId, Long sportId, Pageable pageable) {
        log.debug("PERSISTENCE_ADAPTER_FIND_AVAILABLE | userId={} | sportId={}", userId, sportId);

        Specification<ExerciseCategoryEntity> spec = ExerciseCategorySpecification.availableForUser(userId, sportId);
        Page<ExerciseCategoryEntity> result = exerciseCategoryRepository.findAll(spec, pageable);

        log.debug("PERSISTENCE_ADAPTER_AVAILABLE_RESULT | totalElements={}", result.getTotalElements());
        return result.map(this::convertToModel);
    }

    @Override
    public Long countByOwnerId(Long ownerId) {
        log.debug("PERSISTENCE_ADAPTER_COUNT_BY_OWNER | ownerId={}", ownerId);
        return exerciseCategoryRepository.countByOwnerId(ownerId);
    }

    @Override
    public Page<ExerciseCategoryModel> findMostUsedCategories(Pageable pageable) {
        log.debug("PERSISTENCE_ADAPTER_FIND_MOST_USED");
        return exerciseCategoryRepository.findMostUsedCategories(pageable)
                .map(this::convertToModel);
    }

    @Override
    public void incrementUsageCount(Long categoryId) {
        log.debug("PERSISTENCE_ADAPTER_INCREMENT_USAGE | categoryId={}", categoryId);

        exerciseCategoryRepository.findById(categoryId).ifPresent(entity -> {
            entity.incrementUsage();
            exerciseCategoryRepository.save(entity);
            log.debug("PERSISTENCE_ADAPTER_INCREMENT_USAGE_SUCCESS | categoryId={}", categoryId);
        });
    }

    // ── Conversión ────────────────────────────────────────────────────────────

    private ExerciseCategoryModel convertToModel(ExerciseCategoryEntity entity) {
        ExerciseCategoryModel model = new ExerciseCategoryModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setIsPredefined(entity.getIsPredefined());
        model.setIsActive(entity.getIsActive());
        model.setIsPublic(entity.getIsPublic());
        model.setUsageCount(entity.getUsageCount());
        model.setParentCategoryId(entity.getParentCategoryId());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getOwner() != null) {
            model.setOwnerId(entity.getOwner().getId());
        }

        if (entity.getSport() != null) {
            model.setSportId(entity.getSport().getId());
        }

        log.debug("CONVERTED_ENTITY_TO_MODEL | id={} | name={}", model.getId(), model.getName());
        return model;
    }

    private ExerciseCategoryEntity convertToEntity(ExerciseCategoryModel model) {
        ExerciseCategoryEntity entity = new ExerciseCategoryEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setIsPredefined(model.getIsPredefined());
        entity.setIsActive(model.getIsActive());
        entity.setIsPublic(model.getIsPublic());
        entity.setUsageCount(model.getUsageCount());
        entity.setParentCategoryId(model.getParentCategoryId());
        entity.setCreatedAt(model.getCreatedAt() != null ? model.getCreatedAt() : LocalDateTime.now());
        entity.setUpdatedAt(model.getUpdatedAt() != null ? model.getUpdatedAt() : LocalDateTime.now());

        if (model.getOwnerId() != null) {
            userRepository.findById(model.getOwnerId()).ifPresent(entity::setOwner);
        }

        if (model.getSportId() != null) {
            sportRepository.findById(model.getSportId()).ifPresent(entity::setSport);
        }

        log.debug("CONVERTED_MODEL_TO_ENTITY | id={} | name={}", entity.getId(), entity.getName());
        return entity;
    }

    private void updateEntityFromModel(ExerciseCategoryEntity entity, ExerciseCategoryModel model) {
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setIsActive(model.getIsActive());
        entity.setIsPublic(model.getIsPublic());
        entity.setParentCategoryId(model.getParentCategoryId());
        entity.setUpdatedAt(LocalDateTime.now());

        if (model.getSportId() != null) {
            boolean sportChanged = entity.getSport() == null
                    || !model.getSportId().equals(entity.getSport().getId());
            if (sportChanged) {
                sportRepository.findById(model.getSportId()).ifPresent(entity::setSport);
            }
        } else {
            entity.setSport(null);
        }
    }
}