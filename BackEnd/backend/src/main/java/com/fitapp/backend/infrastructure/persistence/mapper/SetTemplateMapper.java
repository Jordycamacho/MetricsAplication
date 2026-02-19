package com.fitapp.backend.infrastructure.persistence.mapper;

import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.application.dto.RoutineSetParameter.response.RoutineSetParameterResponse;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetTemplateMapper {
    
    private final CustomParameterPersistencePort customParameterPersistencePort;
    
    public RoutineSetTemplateResponse toResponse(RoutineSetTemplateModel model) {
        if (model == null) {
            log.warn("Attempting to map null SetTemplateModel to response");
            return null;
        }
        
        log.debug("Mapping SetTemplateModel to response: id={}", model.getId());
        
        return RoutineSetTemplateResponse.builder()
                .id(model.getId())
                .routineExerciseId(model.getRoutineExerciseId())
                .position(model.getPosition())
                .subSetNumber(model.getSubSetNumber())
                .groupId(model.getGroupId())
                .setType(model.getSetType())
                .restAfterSet(model.getRestAfterSet())
                .parameters(model.getParameters() != null ? 
                        model.getParameters().stream()
                                .map(this::toParameterResponse)
                                .collect(Collectors.toList()) : null)
                .build();
    }
    
    public RoutineSetParameterResponse toParameterResponse(RoutineSetParameterModel model) {
        if (model == null) {
            return null;
        }
        
        RoutineSetParameterResponse response = new RoutineSetParameterResponse();
        response.setId(model.getId());
        response.setSetTemplateId(model.getSetTemplateId());
        response.setParameterId(model.getParameterId());
        response.setNumericValue(model.getNumericValue());
        response.setDurationValue(model.getDurationValue());
        response.setIntegerValue(model.getIntegerValue());
        response.setRepetitions(model.getRepetitions());
        
        try {
            var parameter = customParameterPersistencePort.findById(model.getParameterId());
            if (parameter.isPresent()) {
                response.setParameterName(parameter.get().getDisplayName());
                response.setParameterType(parameter.get().getParameterType() != null ? 
                        parameter.get().getParameterType().name() : null);
                response.setUnit(parameter.get().getUnit());
            }
        } catch (Exception e) {
            log.warn("Failed to enrich set parameter response: {}", e.getMessage());
        }
        
        return response;
    }
}