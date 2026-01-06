package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseConverter {
    
    public ExerciseModel toDomain(ExerciseEntity entity) {
        log.debug("CONVERTER_TO_DOMAIN_EXERCISE_START | entityId={} | entityName={}", 
                 entity.getId(), entity.getName());
        
        ExerciseModel model = new ExerciseModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setExerciseType(entity.getExerciseType());
        model.setIsActive(entity.getIsActive());
        model.setIsPublic(entity.getIsPublic());
        model.setUsageCount(entity.getUsageCount());
        model.setRating(entity.getRating());
        model.setRatingCount(entity.getRatingCount());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setLastUsedAt(entity.getLastUsedAt());
        
        if (entity.getSport() != null) {
            model.setSportId(entity.getSport().getId());
            model.setSportName(entity.getSport().getName());
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
        
        model.logModelData("CONVERTED_FROM_ENTITY");
        log.debug("CONVERTER_TO_DOMAIN_EXERCISE_END | modelId={}", model.getId());
        
        return model;
    }

    public ExerciseEntity toEntity(ExerciseModel model) {
        log.debug("CONVERTER_TO_ENTITY_EXERCISE_START | modelId={} | modelName={}", 
                 model.getId(), model.getName());
        
        model.logModelData("CONVERTING_TO_ENTITY");
        
        ExerciseEntity entity = ExerciseEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .exerciseType(model.getExerciseType())
                .isActive(model.getIsActive())
                .isPublic(model.getIsPublic())
                .usageCount(model.getUsageCount())
                .rating(model.getRating())
                .ratingCount(model.getRatingCount())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .lastUsedAt(model.getLastUsedAt())
                .build();
        
        log.debug("CONVERTER_TO_ENTITY_EXERCISE_END | entityId={} | isPublic={}", 
                 entity.getId(), entity.getIsPublic());
        
        return entity;
    }
}