package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import org.springframework.stereotype.Component;

@Component
public class SportConverter {
    public SportModel toDomain(SportEntity entity) {
        SportModel model = new SportModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setIsPredefined(entity.getIsPredefined());
        model.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
        model.setParameterTemplate(entity.getParameterTemplate());
        model.setIconUrl(entity.getIconUrl());
        model.setCategory(entity.getCategory());
        return model;
    }

    public SportEntity toEntity(SportModel model) {
        SportEntity entity = new SportEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setIsPredefined(model.getIsPredefined());
        entity.setParameterTemplate(model.getParameterTemplate());
        entity.setIconUrl(model.getIconUrl());
        entity.setCategory(model.getCategory());
        return entity;
    }
}