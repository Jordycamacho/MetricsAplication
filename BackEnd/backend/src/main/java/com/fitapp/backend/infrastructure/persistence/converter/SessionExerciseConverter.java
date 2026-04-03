package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.application.dto.workout.response.SessionExerciseResponse;
import com.fitapp.backend.domain.model.SessionExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SessionExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionExerciseConverter {

    private final ExerciseRepository exerciseRepository;
    private final SetExecutionConverter setExecutionConverter;

    // ── Entity → Model ────────────────────────────────────────────────────────

    public SessionExerciseModel toDomain(SessionExerciseEntity entity) {
        if (entity == null) {
            log.warn("SESSION_EXERCISE_CONVERTER_NULL_ENTITY | Attempting to convert null SessionExerciseEntity");
            return null;
        }

        log.debug("SESSION_EXERCISE_CONVERTER_TO_DOMAIN | sessionExerciseId={} | exerciseId={}", 
                  entity.getId(), entity.getExercise() != null ? entity.getExercise().getId() : null);

        SessionExerciseModel model = SessionExerciseModel.builder()
                .id(entity.getId())
                .sessionId(entity.getSession() != null ? entity.getSession().getId() : null)
                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .personalNotes(entity.getPersonalNotes())
                .build();

        if (entity.getSets() != null && !entity.getSets().isEmpty()) {
            model.setSets(entity.getSets().stream()
                    .map(setExecutionConverter::toDomain)
                    .collect(Collectors.toList()));
        }

        return model;
    }

    // ── Model → Entity ────────────────────────────────────────────────────────

    public SessionExerciseEntity toEntity(SessionExerciseModel model, WorkoutSessionEntity session) {
        if (model == null) {
            log.warn("SESSION_EXERCISE_CONVERTER_NULL_MODEL | Attempting to convert null SessionExerciseModel");
            return null;
        }

        log.debug("SESSION_EXERCISE_CONVERTER_TO_ENTITY | sessionExerciseId={} | exerciseId={}", 
                  model.getId(), model.getExerciseId());

        SessionExerciseEntity entity = new SessionExerciseEntity();
        entity.setId(model.getId());
        entity.setSession(session);
        entity.setStatus(model.getStatus() != null ? model.getStatus() : ExerciseStatus.PENDING);
        entity.setStartedAt(model.getStartedAt());
        entity.setCompletedAt(model.getCompletedAt());
        entity.setPersonalNotes(model.getPersonalNotes());

        // Cargar exercise
        if (model.getExerciseId() != null) {
            ExerciseEntity exercise = exerciseRepository.findById(model.getExerciseId())
                    .orElseThrow(() -> {
                        log.error("SESSION_EXERCISE_CONVERTER_EXERCISE_NOT_FOUND | exerciseId={}", model.getExerciseId());
                        return new RuntimeException("Exercise not found: " + model.getExerciseId());
                    });
            entity.setExercise(exercise);
        }

        // Convertir sets
        if (model.getSets() != null && !model.getSets().isEmpty()) {
            entity.setSets(model.getSets().stream()
                    .map(setModel -> setExecutionConverter.toEntity(setModel, entity))
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    // ── Model → Response DTO ──────────────────────────────────────────────────

    public SessionExerciseResponse toResponse(SessionExerciseModel model) {
        if (model == null) {
            log.warn("SESSION_EXERCISE_CONVERTER_NULL_MODEL | Attempting to convert null model to response");
            return null;
        }

        log.debug("SESSION_EXERCISE_CONVERTER_TO_RESPONSE | sessionExerciseId={}", model.getId());

        // Obtener nombre del ejercicio
        String exerciseName = null;
        if (model.getExerciseId() != null) {
            exerciseName = exerciseRepository.findById(model.getExerciseId())
                    .map(ExerciseEntity::getName)
                    .orElse("Unknown Exercise");
        }

        return SessionExerciseResponse.builder()
                .id(model.getId())
                .exerciseId(model.getExerciseId())
                .exerciseName(exerciseName)
                .status(model.getStatus() != null ? model.getStatus().name() : null)
                .startedAt(model.getStartedAt())
                .completedAt(model.getCompletedAt())
                .personalNotes(model.getPersonalNotes())
                .sets(model.getSets() != null 
                        ? model.getSets().stream()
                                .map(setExecutionConverter::toResponse)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    // ── Entity → Response DTO (directo) ───────────────────────────────────────

    public SessionExerciseResponse toResponseFromEntity(SessionExerciseEntity entity) {
        if (entity == null) {
            log.warn("SESSION_EXERCISE_CONVERTER_NULL_ENTITY | Attempting to convert null entity to response");
            return null;
        }

        log.debug("SESSION_EXERCISE_CONVERTER_TO_RESPONSE_FROM_ENTITY | sessionExerciseId={}", entity.getId());

        return SessionExerciseResponse.builder()
                .id(entity.getId())
                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                .exerciseName(entity.getExercise() != null ? entity.getExercise().getName() : "Unknown")
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .personalNotes(entity.getPersonalNotes())
                .sets(entity.getSets() != null 
                        ? entity.getSets().stream()
                                .map(setExecutionConverter::toResponseFromEntity)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }
}