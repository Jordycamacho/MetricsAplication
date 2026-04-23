package com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.converter;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.repository.RoutineExerciseRepository;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.entity.RoutineSetParameterEntity;
import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.entity.RoutineSetTemplateEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetConverter {
    
    private final RoutineExerciseRepository routineExerciseRepository;
    private final CustomParameterRepository customParameterRepository;
    
    public RoutineSetTemplateEntity toSetTemplateEntity(RoutineSetTemplateModel model) {
        if (model == null) {
            log.warn("Attempting to convert null SetTemplateModel to entity");
            return null;
        }
        
        log.debug("Converting SetTemplateModel to entity: id={}", model.getId());
        
        RoutineSetTemplateEntity entity = new RoutineSetTemplateEntity();
        entity.setId(model.getId());
        
        if (model.getRoutineExerciseId() != null) {
            RoutineExerciseEntity routineExercise = routineExerciseRepository.findById(model.getRoutineExerciseId())
                    .orElseThrow(() -> {
                        log.error("Routine exercise not found for SetTemplate conversion: id={}", 
                                model.getRoutineExerciseId());
                        return new RuntimeException("Routine exercise not found");
                    });
            entity.setRoutineExercise(routineExercise);
        }
        
        entity.setPosition(model.getPosition());
        entity.setSubSetNumber(model.getSubSetNumber());
        entity.setGroupId(model.getGroupId());
        
        if (model.getSetType() != null) {
            try {
                entity.setSetType(SetType.valueOf(model.getSetType()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid set type: {}, defaulting to NORMAL", model.getSetType());
                entity.setSetType(SetType.NORMAL);
            }
        } else {
            entity.setSetType(SetType.NORMAL);
        }
        
        entity.setRestAfterSet(model.getRestAfterSet());
        
        return entity;
    }
    
    public RoutineSetTemplateModel toSetTemplateModel(RoutineSetTemplateEntity entity) {
        if (entity == null) {
            log.warn("Attempting to convert null SetTemplateEntity to model");
            return null;
        }
        
        log.debug("Converting SetTemplateEntity to model: id={}", entity.getId());
        
        return RoutineSetTemplateModel.builder()
                .id(entity.getId())
                .routineExerciseId(entity.getRoutineExercise() != null ? entity.getRoutineExercise().getId() : null)
                .position(entity.getPosition())
                .subSetNumber(entity.getSubSetNumber())
                .groupId(entity.getGroupId())
                .setType(entity.getSetType() != null ? entity.getSetType().name() : SetType.NORMAL.name())
                .restAfterSet(entity.getRestAfterSet())
                .build();
    }
    
    public RoutineSetParameterEntity toSetParameterEntity(RoutineSetParameterModel model) {
        if (model == null) {
            log.warn("Attempting to convert null SetParameterModel to entity");
            return null;
        }
        
        log.debug("Converting SetParameterModel to entity: id={}", model.getId());
        
        RoutineSetParameterEntity entity = new RoutineSetParameterEntity();
        entity.setId(model.getId());
        
        if (model.getSetTemplateId() != null) {
            RoutineSetTemplateEntity setTemplate = new RoutineSetTemplateEntity();
            setTemplate.setId(model.getSetTemplateId());
            entity.setSetTemplate(setTemplate);
        }
        
        if (model.getParameterId() != null) {
            CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                    .orElseThrow(() -> {
                        log.error("Custom parameter not found for SetParameter conversion: id={}", 
                                model.getParameterId());
                        return new RuntimeException("Custom parameter not found");
                    });
            entity.setParameter(parameter);
        }
        
        entity.setNumericValue(model.getNumericValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setRepetitions(model.getRepetitions());

        return entity;
    }
    
    public RoutineSetParameterModel toSetParameterModel(RoutineSetParameterEntity entity) {
        if (entity == null) {
            log.warn("Attempting to convert null SetParameterEntity to model");
            return null;
        }
        
        log.debug("Converting SetParameterEntity to model: id={}", entity.getId());
        
        return RoutineSetParameterModel.builder()
                .id(entity.getId())
                .setTemplateId(entity.getSetTemplate() != null ? entity.getSetTemplate().getId() : null)
                .parameterId(entity.getParameter() != null ? entity.getParameter().getId() : null)
                .repetitions(entity.getRepetitions())
                .numericValue(entity.getNumericValue())
                .durationValue(entity.getDurationValue())
                .integerValue(entity.getIntegerValue())
                .build();
    }
    
    public RoutineSetTemplateModel toSetTemplateModelWithParameters(RoutineSetTemplateEntity entity) {
        RoutineSetTemplateModel model = toSetTemplateModel(entity);
        
        if (model != null && entity.getParameters() != null) {
            model.setParameters(entity.getParameters().stream()
                    .map(this::toSetParameterModel)
                    .collect(Collectors.toList()));
        }
        
        return model;
    }
}