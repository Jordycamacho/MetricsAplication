package com.fitapp.backend.parameter.infrastructure.persistence.converter;

import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomParameterConverter {
    
    public CustomParameterModel toDomain(CustomParameterEntity entity) {
        log.debug("CONVERTER_TO_DOMAIN_PARAMETER_START | entityId={} | entityName={}", 
                 entity.getId(), entity.getName());
        
        CustomParameterModel model = new CustomParameterModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setParameterType(entity.getParameterType());
        model.setUnit(entity.getUnit());
        model.setIsGlobal(entity.getIsGlobal());
        model.setIsActive(entity.getIsActive());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUsageCount(entity.getUsageCount());
        model.setFavorite(entity.isFavorite());
        model.setMetricAggregation(entity.getMetricAggregation());
        model.setTrackable(entity.isTrackable());

        if (entity.getOwner() != null) {
            model.setOwnerId(entity.getOwner().getId());
        }
        
        model.logModelData("CONVERTED_FROM_ENTITY");
        log.debug("CONVERTER_TO_DOMAIN_PARAMETER_END | modelId={}", model.getId());
        
        return model;
    }

    public CustomParameterEntity toEntity(CustomParameterModel model) {
        log.debug("CONVERTER_TO_ENTITY_PARAMETER_START | modelId={} | modelName={}", 
                 model.getId(), model.getName());
        
        model.logModelData("CONVERTING_TO_ENTITY");
        
        CustomParameterEntity entity = CustomParameterEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .parameterType(model.getParameterType())
                .unit(model.getUnit())
                .isGlobal(model.getIsGlobal())
                .isActive(model.getIsActive())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .usageCount(model.getUsageCount())
                .isFavorite(model.isFavorite())
                .metricAggregation(model.getMetricAggregation())
                .isTrackable(model.isTrackable())
                .build();
        
        log.debug("CONVERTER_TO_ENTITY_PARAMETER_END | entityId={} | isGlobal={} | trackable={}", 
                 entity.getId(), entity.getIsGlobal(), entity.isTrackable());
        
        return entity;
    }
}