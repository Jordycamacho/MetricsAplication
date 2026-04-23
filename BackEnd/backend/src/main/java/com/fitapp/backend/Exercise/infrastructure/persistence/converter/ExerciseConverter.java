package com.fitapp.backend.Exercise.infrastructure.persistence.converter;

import com.fitapp.backend.Exercise.domain.model.ExerciseModel;
import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.category.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExerciseConverter {

    public ExerciseModel toDomain(ExerciseEntity entity) {
        if (entity == null) return null;

        ExerciseModel model = new ExerciseModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setExerciseType(entity.getExerciseType());
        model.setIsActive(entity.getIsActive());
        model.setIsPublic(entity.getIsPublic());
        model.setUsageCount(entity.getUsageCount() != null ? entity.getUsageCount() : 0);
        model.setRating(entity.getRating() != null ? entity.getRating() : 0.0);
        model.setRatingCount(entity.getRatingCount() != null ? entity.getRatingCount() : 0);
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setLastUsedAt(entity.getLastUsedAt());

        if (entity.getSports() != null && !entity.getSports().isEmpty()) {
            Map<Long, String> sports = entity.getSports().stream()
                .collect(Collectors.toMap(SportEntity::getId, SportEntity::getName));
            model.setSports(sports);
        }

        if (entity.getCreatedBy() != null) {
            model.setCreatedById(entity.getCreatedBy().getId());
            model.setCreatedByEmail(entity.getCreatedBy().getEmail());
        }

        if (entity.getCategories() != null) {
            model.setCategoryIds(entity.getCategories().stream()
                .map(ExerciseCategoryEntity::getId)
                .collect(Collectors.toSet()));
            model.setCategoryNames(entity.getCategories().stream()
                .map(ExerciseCategoryEntity::getName)
                .collect(Collectors.toSet()));
        }

        if (entity.getSupportedParameters() != null) {
            model.setSupportedParameterIds(entity.getSupportedParameters().stream()
                .map(CustomParameterEntity::getId)
                .collect(Collectors.toSet()));
            model.setSupportedParameterNames(entity.getSupportedParameters().stream()
                .map(CustomParameterEntity::getName)
                .collect(Collectors.toSet()));
        }

        return model;
    }

    public ExerciseEntity toEntity(ExerciseModel model) {
        if (model == null) return null;

        return ExerciseEntity.builder()
            .id(model.getId())
            .name(model.getName())
            .description(model.getDescription())
            .exerciseType(model.getExerciseType())
            .isActive(model.getIsActive() != null ? model.getIsActive() : true)
            .isPublic(model.getIsPublic() != null ? model.getIsPublic() : false)
            .usageCount(model.getUsageCount() != null ? model.getUsageCount() : 0)
            .rating(model.getRating() != null ? model.getRating() : 0.0)
            .ratingCount(model.getRatingCount() != null ? model.getRatingCount() : 0)
            .lastUsedAt(model.getLastUsedAt())
            .build();
    }
}