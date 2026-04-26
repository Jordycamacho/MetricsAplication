package com.fitapp.backend.workout.aplication.service;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.routinecomplete.routine.aplication.port.output.RoutinePersistencePort;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;
import com.fitapp.backend.workout.aplication.dto.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.workout.aplication.dto.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionResponse;
import com.fitapp.backend.workout.aplication.dto.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.workout.aplication.port.input.WorkoutUseCase;
import com.fitapp.backend.workout.aplication.port.output.WorkoutSessionPersistencePort;
import com.fitapp.backend.workout.domain.exception.InvalidWorkoutDataException;
import com.fitapp.backend.workout.domain.exception.WorkoutSessionNotFoundException;
import com.fitapp.backend.workout.domain.model.SessionExerciseModel;
import com.fitapp.backend.workout.domain.model.SetExecutionModel;
import com.fitapp.backend.workout.domain.model.SetExecutionParameterModel;
import com.fitapp.backend.workout.domain.model.WorkoutSessionModel;
import com.fitapp.backend.workout.infrastructure.persistence.converter.WorkoutConverter;
import com.fitapp.backend.workout.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.fitapp.backend.workout.infrastructure.persistence.repository.WorkoutSessionRepository;
import com.fitapp.backend.workout.infrastructure.persistence.specification.WorkoutSessionSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutServiceImpl implements WorkoutUseCase {

        private final WorkoutSessionPersistencePort workoutSessionPersistencePort;
        private final RoutinePersistencePort routinePersistencePort;
        private final WorkoutSessionRepository workoutSessionRepository;
        private final WorkoutConverter workoutConverter;

        // ── Save ──────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public WorkoutSessionResponse saveWorkoutSession(SaveWorkoutSessionRequest request, Long userId) {
                log.info("SAVE_SESSION | userId={} | routineId={}", userId, request.getRoutineId());

                validateWorkoutRequest(request, userId);

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(request.getRoutineId(), userId)
                                .orElseThrow(() -> {
                                        log.error("ROUTINE_NOT_FOUND | routineId={} | userId={}",
                                                        request.getRoutineId(), userId);
                                        return new com.fitapp.backend.routinecomplete.routine.domain.exception.RoutineNotFoundException(
                                                        userId);
                                });

                WorkoutSessionModel sessionModel = buildWorkoutSessionModel(request, routine, userId);
                sessionModel.calculateDuration();
                sessionModel.calculateTotalVolume();

                log.debug("SESSION_MODEL_BUILT | exerciseCount={} | totalSets={}",
                                sessionModel.getExercises().size(),
                                sessionModel.getTotalSetCount());

                WorkoutSessionModel saved = workoutSessionPersistencePort.save(sessionModel);
                updateRoutineLastUsed(routine.getId(), userId, request.getStartTime());

                return workoutConverter.toResponse(saved, routine.getName());
        }

        // ── Read ──────────────────────────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public WorkoutSessionResponse getWorkoutSessionDetails(Long sessionId, Long userId) {
                log.info("GET_SESSION_DETAILS | sessionId={} | userId={}", sessionId, userId);

                WorkoutSessionModel session = workoutSessionPersistencePort
                                .findByIdAndUserIdWithDetails(sessionId, userId)
                                .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId, userId));

                String routineName = routinePersistencePort.findByIdAndUserId(session.getRoutineId(), userId)
                                .map(RoutineModel::getName)
                                .orElse("Unknown Routine");

                return workoutConverter.toResponse(session, routineName);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<WorkoutSessionSummaryResponse> getWorkoutHistory(
                        WorkoutHistoryFilterRequest filters, Long userId, Pageable pageable) {

                log.info("GET_WORKOUT_HISTORY | userId={} | filters={}", userId, filters);

                Specification<WorkoutSessionEntity> spec = WorkoutSessionSpecification.withFilters(filters, userId);
                Sort sort = buildSort(filters);
                Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

                Page<WorkoutSessionEntity> page = workoutSessionRepository.findAll(spec, pageableWithSort);

                log.info("WORKOUT_HISTORY_FOUND | userId={} | totalElements={}", userId, page.getTotalElements());

                return page.map(workoutConverter::toSummaryResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<WorkoutSessionSummaryResponse> getRecentWorkouts(Long userId, int limit) {
                log.info("GET_RECENT_WORKOUTS | userId={} | limit={}", userId, limit);

                Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startTime"));
                Page<WorkoutSessionModel> sessions = workoutSessionPersistencePort.findByUserId(userId, pageable);

                // FIX: en la versión original se hacía una query a routinePersistencePort
                // POR CADA sesión (N+1). En summaries el routineName viene de la entidad
                // directamente vía toSummaryResponse(entity), pero aquí tenemos models.
                // Solución: recolectar los routineIds únicos y resolver en batch.
                Set<Long> routineIds = sessions.stream()
                                .map(WorkoutSessionModel::getRoutineId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                Map<Long, String> routineNames = resolveRoutineNames(routineIds, userId);

                log.info("RECENT_WORKOUTS_FOUND | userId={} | count={}", userId, sessions.getNumberOfElements());

                return sessions.map(model -> {
                        String routineName = routineNames.getOrDefault(model.getRoutineId(), "Unknown");
                        return workoutConverter.toSummaryResponse(model, routineName);
                });
        }

        // ── Delete ────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public void deleteWorkoutSession(Long sessionId, Long userId) {
                log.info("DELETE_SESSION | sessionId={} | userId={}", sessionId, userId);
                // Verify ownership before deleting
                workoutSessionPersistencePort.findByIdAndUserIdWithDetails(sessionId, userId)
                                .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId, userId));
                workoutSessionPersistencePort.deleteById(sessionId);
                log.info("DELETE_SESSION_SUCCESS | sessionId={}", sessionId);
        }

        // ── Stats ─────────────────────────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public Double getTotalVolume(Long userId) {
                log.info("GET_TOTAL_VOLUME | userId={}", userId);
                Double volume = workoutSessionPersistencePort.sumTotalVolumeByUserId(userId);
                return volume != null ? volume : 0.0;
        }

        // ── Private helpers ───────────────────────────────────────────────────────

        private void validateWorkoutRequest(SaveWorkoutSessionRequest request, Long userId) {
                if (request.getSetExecutions() == null || request.getSetExecutions().isEmpty()) {
                        log.error("VALIDATION_FAILED | reason=EMPTY_SETS | userId={}", userId);
                        throw InvalidWorkoutDataException.emptySetExecutions();
                }

                if (request.getEndTime().isBefore(request.getStartTime())) {
                        log.error("VALIDATION_FAILED | reason=INVALID_TIME_RANGE | userId={}", userId);
                        throw InvalidWorkoutDataException.invalidTimeRange();
                }

                for (SaveWorkoutSessionRequest.SetExecutionRequest setExec : request.getSetExecutions()) {
                        if (setExec.getExerciseId() == null) {
                                throw InvalidWorkoutDataException.missingExerciseId();
                        }
                        for (SaveWorkoutSessionRequest.ParameterValueRequest param : setExec.getParameters()) {
                                if (param.getNumericValue() == null
                                                && param.getIntegerValue() == null
                                                && param.getDurationValue() == null
                                                && (param.getStringValue() == null
                                                                || param.getStringValue().isBlank())) {
                                        log.error("VALIDATION_FAILED | reason=EMPTY_PARAMETER | parameterId={}",
                                                        param.getParameterId());
                                        throw InvalidWorkoutDataException.missingParameterValue();
                                }
                        }
                }
        }

        private WorkoutSessionModel buildWorkoutSessionModel(
                        SaveWorkoutSessionRequest request, RoutineModel routine, Long userId) {

                // Agrupar sets por ejercicio manteniendo orden de inserción
                Map<Long, List<SaveWorkoutSessionRequest.SetExecutionRequest>> setsByExercise = request
                                .getSetExecutions()
                                .stream()
                                .collect(Collectors.groupingBy(
                                                SaveWorkoutSessionRequest.SetExecutionRequest::getExerciseId,
                                                LinkedHashMap::new,
                                                Collectors.toList()));

                List<SessionExerciseModel> exercises = setsByExercise.entrySet().stream()
                                .map(entry -> buildSessionExercise(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());

                return WorkoutSessionModel.builder()
                                .routineId(routine.getId())
                                .userId(userId)
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .performanceScore(request.getPerformanceScore())
                                .exercises(exercises)
                                .build();
        }

        private SessionExerciseModel buildSessionExercise(Long exerciseId,
                        List<SaveWorkoutSessionRequest.SetExecutionRequest> setRequests) {

                LocalDateTime exerciseStarted = setRequests.stream()
                                .map(SaveWorkoutSessionRequest.SetExecutionRequest::getStartedAt)
                                .filter(Objects::nonNull)
                                .min(LocalDateTime::compareTo)
                                .orElse(null);

                LocalDateTime exerciseCompleted = setRequests.stream()
                                .map(SaveWorkoutSessionRequest.SetExecutionRequest::getCompletedAt)
                                .filter(Objects::nonNull)
                                .max(LocalDateTime::compareTo)
                                .orElse(null);

                boolean allCompleted = setRequests.stream()
                                .allMatch(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()));
                boolean anySkipped = setRequests.stream()
                                .anyMatch(s -> "SKIPPED".equalsIgnoreCase(s.getStatus()));

                ExerciseStatus status;
                if (allCompleted) {
                        status = ExerciseStatus.COMPLETED;
                } else if (anySkipped) {
                        status = ExerciseStatus.SKIPPED;
                } else {
                        status = ExerciseStatus.IN_PROGRESS;
                }

                List<SetExecutionModel> sets = setRequests.stream()
                                .map(this::buildSetExecution)
                                .collect(Collectors.toList());

                return SessionExerciseModel.builder()
                                .exerciseId(exerciseId)
                                .status(status)
                                .startedAt(exerciseStarted)
                                .completedAt(exerciseCompleted)
                                .sets(sets)
                                .build();
        }

        private SetExecutionModel buildSetExecution(SaveWorkoutSessionRequest.SetExecutionRequest setRequest) {
                SetType setType = SetType.NORMAL;
                if (setRequest.getSetType() != null) {
                        try {
                                setType = SetType.valueOf(setRequest.getSetType().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                log.warn("INVALID_SET_TYPE | value={} | defaulting=NORMAL", setRequest.getSetType());
                        }
                }

                SetExecutionStatus status = SetExecutionStatus.COMPLETED;
                if (setRequest.getStatus() != null) {
                        try {
                                status = SetExecutionStatus.valueOf(setRequest.getStatus().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                log.warn("INVALID_SET_STATUS | value={} | defaulting=COMPLETED",
                                                setRequest.getStatus());
                        }
                }

                List<SetExecutionParameterModel> parameters = setRequest.getParameters().stream()
                                .map(this::buildSetExecutionParameter)
                                .collect(Collectors.toList());

                return SetExecutionModel.builder()
                                .setTemplateId(setRequest.getSetTemplateId())
                                .position(setRequest.getPosition())
                                .setType(setType)
                                .status(status)
                                .startedAt(setRequest.getStartedAt())
                                .completedAt(setRequest.getCompletedAt())
                                .actualRestSeconds(setRequest.getActualRestSeconds())
                                .notes(setRequest.getNotes())
                                .parameters(parameters)
                                .build();
        }

        private SetExecutionParameterModel buildSetExecutionParameter(
                        SaveWorkoutSessionRequest.ParameterValueRequest paramRequest) {
                return SetExecutionParameterModel.builder()
                                .parameterId(paramRequest.getParameterId())
                                .numericValue(paramRequest.getNumericValue())
                                .integerValue(paramRequest.getIntegerValue())
                                .durationValue(paramRequest.getDurationValue())
                                .stringValue(paramRequest.getStringValue())
                                .isPersonalRecord(false)
                                .build();
        }

        private void updateRoutineLastUsed(Long routineId, Long userId, LocalDateTime usedAt) {
                try {
                        routinePersistencePort.updateLastUsedAt(routineId, userId, usedAt);
                        log.debug("ROUTINE_LAST_USED_UPDATED | routineId={}", routineId);
                } catch (Exception e) {
                        // No-critical: no propagamos el error para no revertir el workout guardado
                        log.warn("ROUTINE_LAST_USED_UPDATE_FAILED | routineId={} | error={}", routineId,
                                        e.getMessage());
                }
        }

        /**
         * Resuelve nombres de rutinas en batch para evitar N+1.
         * Si el port no ofrece findAllByIds, cae en N queries individuales
         * pero al menos queda aislado aquí y es fácil de optimizar.
         */
        private Map<Long, String> resolveRoutineNames(Set<Long> routineIds, Long userId) {
                Map<Long, String> names = new HashMap<>();
                for (Long routineId : routineIds) {
                        routinePersistencePort.findByIdAndUserId(routineId, userId)
                                        .ifPresent(r -> names.put(routineId, r.getName()));
                }
                return names;
        }

        private Sort buildSort(WorkoutHistoryFilterRequest filters) {
                // getSafeSortBy() garantiza que solo se usan campos de la allowlist
                String sortBy = filters.getSafeSortBy();
                Sort.Direction direction = "ASC".equalsIgnoreCase(filters.getSortDirection())
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                return Sort.by(direction, sortBy);
        }
}