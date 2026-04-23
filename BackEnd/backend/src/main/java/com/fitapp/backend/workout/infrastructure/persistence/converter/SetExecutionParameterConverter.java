package com.fitapp.backend.workout.infrastructure.persistence.converter;

import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.workout.aplication.dto.response.SetExecutionParameterResponse;
import com.fitapp.backend.workout.domain.model.SetExecutionParameterModel;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionParameterEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetExecutionParameterConverter {

    private final CustomParameterRepository customParameterRepository;

    // ── Entity → Model ────────────────────────────────────────────────────────

    public SetExecutionParameterModel toDomain(SetExecutionParameterEntity entity) {
        if (entity == null) {
            log.warn("SET_EXECUTION_PARAM_CONVERTER_NULL_ENTITY | Attempting to convert null SetExecutionParameterEntity");
            return null;
        }

        log.debug("SET_EXECUTION_PARAM_CONVERTER_TO_DOMAIN | parameterId={} | value={}", 
                  entity.getParameter() != null ? entity.getParameter().getId() : null,
                  entity.getValueAsDouble());

        return SetExecutionParameterModel.builder()
                .id(entity.getId())
                .setExecutionId(entity.getSetExecution() != null ? entity.getSetExecution().getId() : null)
                .parameterId(entity.getParameter() != null ? entity.getParameter().getId() : null)
                .numericValue(entity.getNumericValue())
                .integerValue(entity.getIntegerValue())
                .durationValue(entity.getDurationValue())
                .stringValue(entity.getStringValue())
                .isPersonalRecord(entity.isPersonalRecord())
                .build();
    }

    // ── Model → Entity ────────────────────────────────────────────────────────

    public SetExecutionParameterEntity toEntity(SetExecutionParameterModel model, SetExecutionEntity setExecution) {
        if (model == null) {
            log.warn("SET_EXECUTION_PARAM_CONVERTER_NULL_MODEL | Attempting to convert null SetExecutionParameterModel");
            return null;
        }

        log.debug("SET_EXECUTION_PARAM_CONVERTER_TO_ENTITY | parameterId={} | value={}", 
                  model.getParameterId(), model.getValueAsDouble());

        SetExecutionParameterEntity entity = new SetExecutionParameterEntity();
        entity.setId(model.getId());
        entity.setSetExecution(setExecution);
        entity.setNumericValue(model.getNumericValue());
        entity.setIntegerValue(model.getIntegerValue());
        entity.setDurationValue(model.getDurationValue());
        entity.setStringValue(model.getStringValue());
        entity.setPersonalRecord(model.isPersonalRecord());

        // Cargar parameter
        if (model.getParameterId() != null) {
            CustomParameterEntity parameter = customParameterRepository.findById(model.getParameterId())
                    .orElseThrow(() -> {
                        log.error("SET_EXECUTION_PARAM_CONVERTER_PARAM_NOT_FOUND | parameterId={}", model.getParameterId());
                        return new RuntimeException("Custom parameter not found: " + model.getParameterId());
                    });
            entity.setParameter(parameter);
        }

        return entity;
    }

    // ── Model → Response DTO ──────────────────────────────────────────────────

    public SetExecutionParameterResponse toResponse(SetExecutionParameterModel model) {
        if (model == null) {
            log.warn("SET_EXECUTION_PARAM_CONVERTER_NULL_MODEL | Attempting to convert null model to response");
            return null;
        }

        log.debug("SET_EXECUTION_PARAM_CONVERTER_TO_RESPONSE | parameterId={}", model.getParameterId());

        // Obtener detalles del parámetro
        String parameterName = null;
        String parameterType = null;
        String unit = null;

        if (model.getParameterId() != null) {
            CustomParameterEntity param = customParameterRepository.findById(model.getParameterId())
                    .orElse(null);
            if (param != null) {
                parameterName = param.getName();
                parameterType = param.getParameterType() != null ? param.getParameterType().name() : null;
                unit = param.getUnit();
            }
        }

        return SetExecutionParameterResponse.builder()
                .id(model.getId())
                .parameterId(model.getParameterId())
                .parameterName(parameterName)
                .parameterType(parameterType)
                .unit(unit)
                .numericValue(model.getNumericValue())
                .integerValue(model.getIntegerValue())
                .durationValue(model.getDurationValue())
                .stringValue(model.getStringValue())
                .isPersonalRecord(model.isPersonalRecord())
                .build();
    }

    // ── Entity → Response DTO (directo) ───────────────────────────────────────

    public SetExecutionParameterResponse toResponseFromEntity(SetExecutionParameterEntity entity) {
        if (entity == null) {
            log.warn("SET_EXECUTION_PARAM_CONVERTER_NULL_ENTITY | Attempting to convert null entity to response");
            return null;
        }

        log.debug("SET_EXECUTION_PARAM_CONVERTER_TO_RESPONSE_FROM_ENTITY | parameterId={}", 
                  entity.getParameter() != null ? entity.getParameter().getId() : null);

        return SetExecutionParameterResponse.builder()
                .id(entity.getId())
                .parameterId(entity.getParameter() != null ? entity.getParameter().getId() : null)
                .parameterName(entity.getParameter() != null ? entity.getParameter().getName() : null)
                .parameterType(entity.getParameter() != null && entity.getParameter().getParameterType() != null
                        ? entity.getParameter().getParameterType().name() : null)
                .unit(entity.getParameter() != null ? entity.getParameter().getUnit() : null)
                .numericValue(entity.getNumericValue())
                .integerValue(entity.getIntegerValue())
                .durationValue(entity.getDurationValue())
                .stringValue(entity.getStringValue())
                .isPersonalRecord(entity.isPersonalRecord())
                .build();
    }
}