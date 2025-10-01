package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExerciseConverter {
    private final SportRepository sportRepository;
    private final SpringDataUserRepository userRepository; 

    public ExerciseModel toDomain(ExerciseEntity entity) {
        return ExerciseModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .sportId(entity.getSport() != null ? entity.getSport().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .isPredefined(entity.getIsPredefined())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .parameterTemplates(entity.getParameterTemplates())
                .build();
    }

    public ExerciseEntity toEntity(ExerciseModel model) {
        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setIsPredefined(model.getIsPredefined());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setParameterTemplates(model.getParameterTemplates());

        if (model.getSportId() != null) {
            SportEntity sport = sportRepository.findById(model.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found with id: " + model.getSportId()));
            entity.setSport(sport);
        }

        if (model.getUserId() != null) {
            UserEntity user = userRepository.findById(model.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + model.getUserId()));
            entity.setUser(user);
        }

        return entity;
    }
}