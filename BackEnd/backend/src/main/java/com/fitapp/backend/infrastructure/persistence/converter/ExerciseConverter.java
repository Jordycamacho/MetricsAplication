package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import org.springframework.stereotype.Component;

@Component
public class ExerciseConverter {
    public ExerciseModel toDomain(ExerciseEntity entity) {
        ExerciseModel model = new ExerciseModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setSportId(entity.getSport() != null ? entity.getSport().getId() : null);
        model.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        return model;
    }

    public ExerciseEntity toEntity(ExerciseModel model) {
        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        return entity;
    }
}