package com.fitapp.backend.workout.infrastructure.persistence.converter;

import com.fitapp.backend.application.dto.workout.response.SetExecutionResponse;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineSetTemplateRepository;
import com.fitapp.backend.workout.domain.model.SetExecutionModel;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SessionExerciseEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetExecutionConverter {

    private final RoutineSetTemplateRepository setTemplateRepository;
    private final SetExecutionParameterConverter parameterConverter;

    // ── Entity → Model ────────────────────────────────────────────────────────

    public SetExecutionModel toDomain(SetExecutionEntity entity) {
        if (entity == null) {
            log.warn("SET_EXECUTION_CONVERTER_NULL_ENTITY | Attempting to convert null SetExecutionEntity");
            return null;
        }

        log.debug("SET_EXECUTION_CONVERTER_TO_DOMAIN | setExecutionId={} | position={}", 
                  entity.getId(), entity.getPosition());

        SetExecutionModel model = SetExecutionModel.builder()
                .id(entity.getId())
                .sessionExerciseId(entity.getSessionExercise() != null ? entity.getSessionExercise().getId() : null)
                .setTemplateId(entity.getSetTemplate() != null ? entity.getSetTemplate().getId() : null)
                .position(entity.getPosition())
                .setType(entity.getSetType())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .actualRestSeconds(entity.getActualRestSeconds())
                .notes(entity.getNotes())
                .build();

        if (entity.getParameters() != null && !entity.getParameters().isEmpty()) {
            model.setParameters(entity.getParameters().stream()
                    .map(parameterConverter::toDomain)
                    .collect(Collectors.toList()));
        }

        return model;
    }

    // ── Model → Entity ────────────────────────────────────────────────────────

    public SetExecutionEntity toEntity(SetExecutionModel model, SessionExerciseEntity sessionExercise) {
        if (model == null) {
            log.warn("SET_EXECUTION_CONVERTER_NULL_MODEL | Attempting to convert null SetExecutionModel");
            return null;
        }

        log.debug("SET_EXECUTION_CONVERTER_TO_ENTITY | setExecutionId={} | position={}", 
                  model.getId(), model.getPosition());

        SetExecutionEntity entity = new SetExecutionEntity();
        entity.setId(model.getId());
        entity.setSessionExercise(sessionExercise);
        entity.setPosition(model.getPosition());
        entity.setSetType(model.getSetType() != null ? model.getSetType() : SetType.NORMAL);
        entity.setStatus(model.getStatus() != null ? model.getStatus() : SetExecutionStatus.COMPLETED);
        entity.setStartedAt(model.getStartedAt());
        entity.setCompletedAt(model.getCompletedAt());
        entity.setActualRestSeconds(model.getActualRestSeconds());
        entity.setNotes(model.getNotes());

        // Cargar setTemplate si existe
        if (model.getSetTemplateId() != null) {
            RoutineSetTemplateEntity template = setTemplateRepository.findById(model.getSetTemplateId())
                    .orElse(null);
            if (template != null) {
                entity.setSetTemplate(template);
                log.debug("SET_EXECUTION_CONVERTER_TEMPLATE_LINKED | setTemplateId={}", model.getSetTemplateId());
            } else {
                log.warn("SET_EXECUTION_CONVERTER_TEMPLATE_NOT_FOUND | setTemplateId={}", model.getSetTemplateId());
            }
        }

        // Convertir parameters
        if (model.getParameters() != null && !model.getParameters().isEmpty()) {
            entity.setParameters(model.getParameters().stream()
                    .map(paramModel -> parameterConverter.toEntity(paramModel, entity))
                    .collect(Collectors.toSet()));
        }

        return entity;
    }

    // ── Model → Response DTO ──────────────────────────────────────────────────

    public SetExecutionResponse toResponse(SetExecutionModel model) {
        if (model == null) {
            log.warn("SET_EXECUTION_CONVERTER_NULL_MODEL | Attempting to convert null model to response");
            return null;
        }

        log.debug("SET_EXECUTION_CONVERTER_TO_RESPONSE | setExecutionId={}", model.getId());

        return SetExecutionResponse.builder()
                .id(model.getId())
                .setTemplateId(model.getSetTemplateId())
                .position(model.getPosition())
                .setType(model.getSetType() != null ? model.getSetType().name() : null)
                .status(model.getStatus() != null ? model.getStatus().name() : null)
                .startedAt(model.getStartedAt())
                .completedAt(model.getCompletedAt())
                .durationSeconds(model.getDurationSeconds())
                .actualRestSeconds(model.getActualRestSeconds())
                .notes(model.getNotes())
                .volume(model.calculateVolume())
                .parameters(model.getParameters() != null 
                        ? model.getParameters().stream()
                                .map(parameterConverter::toResponse)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    // ── Entity → Response DTO (directo) ───────────────────────────────────────

    public SetExecutionResponse toResponseFromEntity(SetExecutionEntity entity) {
        if (entity == null) {
            log.warn("SET_EXECUTION_CONVERTER_NULL_ENTITY | Attempting to convert null entity to response");
            return null;
        }

        log.debug("SET_EXECUTION_CONVERTER_TO_RESPONSE_FROM_ENTITY | setExecutionId={}", entity.getId());

        return SetExecutionResponse.builder()
                .id(entity.getId())
                .setTemplateId(entity.getSetTemplate() != null ? entity.getSetTemplate().getId() : null)
                .position(entity.getPosition())
                .setType(entity.getSetType() != null ? entity.getSetType().name() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .durationSeconds(entity.getDurationSeconds())
                .actualRestSeconds(entity.getActualRestSeconds())
                .notes(entity.getNotes())
                .volume(calculateVolumeFromEntity(entity))
                .parameters(entity.getParameters() != null 
                        ? entity.getParameters().stream()
                                .map(parameterConverter::toResponseFromEntity)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Double calculateVolumeFromEntity(SetExecutionEntity entity) {
        if (entity.getParameters() == null || entity.getParameters().isEmpty()) {
            return 0.0;
        }

        Double weight = entity.getParameters().stream()
                .filter(p -> p.getNumericValue() != null)
                .map(p -> p.getNumericValue())
                .findFirst()
                .orElse(null);

        Integer reps = entity.getParameters().stream()
                .filter(p -> p.getIntegerValue() != null)
                .map(p -> p.getIntegerValue())
                .findFirst()
                .orElse(null);

        if (weight != null && reps != null) {
            return weight * reps;
        }

        return 0.0;
    }
}