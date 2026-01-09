package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.*;
import com.fitapp.backend.infrastructure.persistence.entity.*;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class RoutineConverter {

    private final SpringDataUserRepository springDataUserRepository;
    private final SportRepository sportRepository;
    private final ExerciseRepository exerciseRepository;
    private final CustomParameterRepository customParameterRepository;

    @Transactional(readOnly = true)
    public RoutineModel toDomain(RoutineEntity entity) {
        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive() != null ? entity.getIsActive() : true);
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setLastUsedAt(entity.getLastUsedAt());  // Nuevo campo
        routine.setUserId(entity.getUser().getId());

        if (entity.getSport() != null) {
            routine.setSportId(entity.getSport().getId());
        } else {
            routine.setSportId(null);
        }

        routine.setTrainingDays(entity.getTrainingDays() != null ? entity.getTrainingDays() : new HashSet<>());
        routine.setGoal(entity.getGoal() != null ? entity.getGoal() : "");
        routine.setSessionsPerWeek(entity.getSessionsPerWeek() != null ? entity.getSessionsPerWeek() : 3);

        // Convertir ejercicios
        if (entity.getExercises() != null) {
            List<RoutineExerciseModel> exerciseModels = entity.getExercises().stream()
                    .map(this::convertRoutineExercise)
                    .collect(Collectors.toList());
            routine.setExercises(exerciseModels);
        }

        return routine;
    }

    private RoutineExerciseModel convertRoutineExercise(RoutineExerciseEntity entity) {
        RoutineExerciseModel model = new RoutineExerciseModel();
        model.setId(entity.getId());
        model.setRoutineId(entity.getRoutine().getId());
        model.setExerciseId(entity.getExercise().getId());
        model.setPosition(entity.getPosition());
        model.setRestAfterExercise(entity.getRestAfterExercise());

        // Convertir parámetros objetivo
        if (entity.getTargetParameters() != null) {
            List<RoutineExerciseParameterModel> paramModels = entity.getTargetParameters().stream()
                    .map(this::convertRoutineExerciseParameter)
                    .collect(Collectors.toList());
            model.setTargetParameters(paramModels);
        }

        // Convertir sets
        if (entity.getSets() != null) {
            List<RoutineSetTemplateModel> setModels = entity.getSets().stream()
                    .map(this::convertRoutineSetTemplate)
                    .collect(Collectors.toList());
            model.setSets(setModels);
        }

        return model;
    }

    private RoutineExerciseParameterModel convertRoutineExerciseParameter(RoutineExerciseParameterEntity entity) {
        RoutineExerciseParameterModel model = new RoutineExerciseParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setDurationValue(entity.getDurationValue());
        model.setStringValue(entity.getStringValue());
        model.setMinValue(entity.getMinValue());
        model.setMaxValue(entity.getMaxValue());
        model.setDefaultValue(entity.getDefaultValue());
        return model;
    }

    private RoutineSetTemplateModel convertRoutineSetTemplate(RoutineSetTemplateEntity entity) {
        RoutineSetTemplateModel model = new RoutineSetTemplateModel();
        model.setId(entity.getId());
        model.setPosition(entity.getPosition());
        model.setSubSetNumber(entity.getSubSetNumber());
        model.setGroupId(entity.getGroupId());
        model.setSetType(entity.getSetType() != null ? entity.getSetType().name() : null);
        model.setRestAfterSet(entity.getRestAfterSet());

        // Convertir parámetros del set
        if (entity.getParameters() != null) {
            List<RoutineSetParameterModel> paramModels = entity.getParameters().stream()
                    .map(this::convertRoutineSetParameter)
                    .collect(Collectors.toList());
            model.setParameters(paramModels);
        }

        return model;
    }

    private RoutineSetParameterModel convertRoutineSetParameter(RoutineSetParameterEntity entity) {
        RoutineSetParameterModel model = new RoutineSetParameterModel();
        model.setId(entity.getId());
        model.setParameterId(entity.getParameter().getId());
        model.setNumericValue(entity.getNumericValue());
        model.setDurationValue(entity.getDurationValue());
        model.setIntegerValue(entity.getIntegerValue());
        model.setMinValue(entity.getMinValue());
        model.setMaxValue(entity.getMaxValue());
        return model;
    }

    public RoutineEntity toEntity(RoutineModel domain) {
        RoutineEntity entity = new RoutineEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : true);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setLastUsedAt(domain.getLastUsedAt());

        entity.setTrainingDays(domain.getTrainingDays() != null ? domain.getTrainingDays() : new HashSet<>());
        entity.setGoal(domain.getGoal() != null ? domain.getGoal() : "");
        entity.setSessionsPerWeek(domain.getSessionsPerWeek() != null ? domain.getSessionsPerWeek() : 3);

        UserEntity user = springDataUserRepository.findById(domain.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + domain.getUserId()));
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found with id: " + domain.getSportId()));
            entity.setSport(sport);
        } else {
            entity.setSport(null);
        }

        return entity;
    }
}