package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoutineConverter {
    public RoutineModel toDomain(RoutineEntity entity) {
        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive());
        routine.setEstimatedDuration(entity.getEstimatedDuration());
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setUserId(entity.getUser().getId());
        routine.setSportId(entity.getSport() != null ? entity.getSport().getId() : null);
        
        if (entity.getExercises() != null) {
            routine.setExercises(entity.getExercises().stream()
                    .map(this::toDomainExercise)
                    .collect(Collectors.toList()));
        }
        
        return routine;
    }

    public RoutineEntity toEntity(RoutineModel domain) {
        RoutineEntity entity = new RoutineEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive());
        entity.setEstimatedDuration(domain.getEstimatedDuration());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        // Nota: user y sport se deben establecer mediante sus IDs usando los servicios correspondientes
        
        if (domain.getExercises() != null) {
            entity.setExercises(domain.getExercises().stream()
                    .map(this::toEntityExercise)
                    .collect(Collectors.toList()));
        }
        
        return entity;
    }

    private RoutineExerciseModel toDomainExercise(RoutineExerciseEntity entity) {
        RoutineExerciseModel exercise = new RoutineExerciseModel();
        exercise.setId(entity.getId());
        exercise.setExerciseId(entity.getExercise().getId());
        exercise.setSets(entity.getSets());
        exercise.setTargetReps(entity.getTargetReps());
        exercise.setTargetWeight(entity.getTargetWeight());
        exercise.setRestIntervalSeconds(entity.getRestInterval() != null ? 
                (int) entity.getRestInterval().getSeconds() : null);
        return exercise;
    }

    private RoutineExerciseEntity toEntityExercise(RoutineExerciseModel domain) {
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setId(domain.getId());
        entity.setSets(domain.getSets());
        entity.setTargetReps(domain.getTargetReps());
        entity.setTargetWeight(domain.getTargetWeight());
        entity.setRestInterval(domain.getRestIntervalSeconds() != null ? 
        java.time.Duration.ofSeconds(domain.getRestIntervalSeconds()) : null);
        return entity;
    }
}