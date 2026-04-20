package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.domain.model.WorkoutSessionModel;
import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.repository.RoutineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkoutConverter {

    private final RoutineRepository routineRepository;
    private final SessionExerciseConverter sessionExerciseConverter;

    // ── Entity → Model ────────────────────────────────────────────────────────

    public WorkoutSessionModel toDomain(WorkoutSessionEntity entity) {
        if (entity == null) {
            log.warn("WORKOUT_CONVERTER_NULL_ENTITY | Attempting to convert null WorkoutSessionEntity");
            return null;
        }

        log.debug("WORKOUT_CONVERTER_TO_DOMAIN | sessionId={} | routineId={}",
                entity.getId(), entity.getRoutine() != null ? entity.getRoutine().getId() : null);

        WorkoutSessionModel model = WorkoutSessionModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .routineId(entity.getRoutine() != null ? entity.getRoutine().getId() : null)
                .userId(entity.getRoutine() != null && entity.getRoutine().getUser() != null
                        ? entity.getRoutine().getUser().getId()
                        : null)
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .performanceScore(entity.getPerformanceScore())
                .totalVolume(entity.getTotalVolume())
                .durationSeconds(entity.getDurationSeconds())
                .build();

        if (entity.getExercises() != null && !entity.getExercises().isEmpty()) {
            model.setExercises(entity.getExercises().stream()
                    .map(sessionExerciseConverter::toDomain)
                    .collect(Collectors.toList()));
        }

        return model;
    }

    // ── Model → Entity ────────────────────────────────────────────────────────

    public WorkoutSessionEntity toEntity(WorkoutSessionModel model) {
        if (model == null) {
            log.warn("WORKOUT_CONVERTER_NULL_MODEL | Attempting to convert null WorkoutSessionModel");
            return null;
        }

        log.debug("WORKOUT_CONVERTER_TO_ENTITY | sessionId={} | routineId={}",
                model.getId(), model.getRoutineId());

        WorkoutSessionEntity entity = new WorkoutSessionEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setStartTime(model.getStartTime());
        entity.setEndTime(model.getEndTime());
        entity.setPerformanceScore(model.getPerformanceScore());
        entity.setTotalVolume(model.getTotalVolume());

        // Cargar routine
        if (model.getRoutineId() != null && model.getUserId() != null) {
            RoutineEntity routine = routineRepository.findByIdAndUserId(model.getRoutineId(), model.getUserId())
                    .orElseThrow(() -> {
                        log.error("WORKOUT_CONVERTER_ROUTINE_NOT_FOUND_OR_UNAUTHORIZED | routineId={} | userId={}",
                                model.getRoutineId(), model.getUserId());
                        return new RuntimeException(
                                "Routine not found or unauthorized: routineId=" + model.getRoutineId() +
                                        ", userId=" + model.getUserId());
                    });
            entity.setRoutine(routine);
        }

        // Convertir ejercicios
        if (model.getExercises() != null && !model.getExercises().isEmpty()) {
            entity.setExercises(model.getExercises().stream()
                    .map(exerciseModel -> sessionExerciseConverter.toEntity(exerciseModel, entity))
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    // ── Model → Response DTO ──────────────────────────────────────────────────

    public WorkoutSessionResponse toResponse(WorkoutSessionModel model, String routineName) {
        if (model == null) {
            log.warn("WORKOUT_CONVERTER_NULL_MODEL | Attempting to convert null model to response");
            return null;
        }

        log.debug("WORKOUT_CONVERTER_TO_RESPONSE | sessionId={} | routineName={}",
                model.getId(), routineName);

        return WorkoutSessionResponse.builder()
                .id(model.getId())
                .routineId(model.getRoutineId())
                .routineName(routineName)
                .userId(model.getUserId())
                .startTime(model.getStartTime())
                .endTime(model.getEndTime())
                .durationSeconds(model.getDurationSeconds())
                .performanceScore(model.getPerformanceScore())
                .totalVolume(model.getTotalVolume())
                .exercises(model.getExercises() != null
                        ? model.getExercises().stream()
                                .map(sessionExerciseConverter::toResponse)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    public WorkoutSessionResponse toResponse(WorkoutSessionModel model) {
        return toResponse(model, null);
    }

    // ── Entity → Summary Response ─────────────────────────────────────────────

    public WorkoutSessionSummaryResponse toSummaryResponse(WorkoutSessionEntity entity) {
        if (entity == null) {
            log.warn("WORKOUT_CONVERTER_NULL_ENTITY | Attempting to convert null entity to summary");
            return null;
        }

        log.debug("WORKOUT_CONVERTER_TO_SUMMARY | sessionId={} | routineId={}",
                entity.getId(), entity.getRoutine() != null ? entity.getRoutine().getId() : null);

        int exerciseCount = entity.getExercises() != null ? entity.getExercises().size() : 0;
        int setCount = entity.getExercises() != null
                ? entity.getExercises().stream()
                        .mapToInt(ex -> ex.getSets() != null ? ex.getSets().size() : 0)
                        .sum()
                : 0;

        return WorkoutSessionSummaryResponse.builder()
                .id(entity.getId())
                .routineId(entity.getRoutine() != null ? entity.getRoutine().getId() : null)
                .routineName(entity.getRoutine() != null ? entity.getRoutine().getName() : "Unknown")
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationSeconds(entity.getDurationSeconds())
                .performanceScore(entity.getPerformanceScore())
                .totalVolume(entity.getTotalVolume())
                .exerciseCount(exerciseCount)
                .setCount(setCount)
                .build();
    }

    // ── Model → Summary Response ──────────────────────────────────────────────

    public WorkoutSessionSummaryResponse toSummaryResponse(WorkoutSessionModel model, String routineName) {
        if (model == null) {
            log.warn("WORKOUT_CONVERTER_NULL_MODEL | Attempting to convert null model to summary");
            return null;
        }

        log.debug("WORKOUT_CONVERTER_TO_SUMMARY_FROM_MODEL | sessionId={}", model.getId());

        int exerciseCount = model.getExercises() != null ? model.getExercises().size() : 0;
        int setCount = model.getExercises() != null
                ? model.getExercises().stream()
                        .mapToInt(ex -> ex.getSets() != null ? ex.getSets().size() : 0)
                        .sum()
                : 0;

        return WorkoutSessionSummaryResponse.builder()
                .id(model.getId())
                .routineId(model.getRoutineId())
                .routineName(routineName != null ? routineName : "Unknown")
                .startTime(model.getStartTime())
                .endTime(model.getEndTime())
                .durationSeconds(model.getDurationSeconds())
                .performanceScore(model.getPerformanceScore())
                .totalVolume(model.getTotalVolume())
                .exerciseCount(exerciseCount)
                .setCount(setCount)
                .build();
    }
}