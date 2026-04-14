package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.workout.request.SaveWorkoutSessionRequest;
import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionResponse;
import com.fitapp.backend.application.dto.workout.response.WorkoutSessionSummaryResponse;
import com.fitapp.backend.application.ports.input.WorkoutUseCase;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.application.ports.output.WorkoutSessionPersistencePort;
import com.fitapp.backend.domain.exception.InvalidWorkoutDataException;
import com.fitapp.backend.domain.exception.RoutineNotFoundException;
import com.fitapp.backend.domain.exception.WorkoutSessionNotFoundException;
import com.fitapp.backend.domain.model.*;
import com.fitapp.backend.infrastructure.persistence.converter.WorkoutConverter;
import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.infrastructure.persistence.repository.WorkoutSessionRepository;
import com.fitapp.backend.infrastructure.persistence.specification.WorkoutSessionSpecification;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

        @Override
        @Transactional
        public WorkoutSessionResponse saveWorkoutSession(SaveWorkoutSessionRequest request, Long userId) {
                log.info("SAVE_SESSION | userId={} | routineId={}", userId, request.getRoutineId());

                validateWorkoutRequest(request, userId);

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(request.getRoutineId(), userId)
                                .orElseThrow(() -> {
                                        log.error("ROUTINE_NOT_FOUND | routineId={} | userId={}",
                                                        request.getRoutineId(), userId);
                                        return new RoutineNotFoundException(userId);
                                });

                WorkoutSessionModel sessionModel = buildWorkoutSessionModel(request, routine, userId);
                sessionModel.calculateDuration();
                sessionModel.calculateTotalVolume();

                log.info("SESSION_MODEL_SETS | exerciseCount={}", sessionModel.getExercises().size());
                for (SessionExerciseModel ex : sessionModel.getExercises()) {
                        log.info("  Exercise {} has {} sets", ex.getExerciseId(), ex.getSets().size());
                        for (SetExecutionModel set : ex.getSets()) {
                                log.info("    setTemplateId={}, params={}", set.getSetTemplateId(),
                                                set.getParameters().size());
                        }
                }

                WorkoutSessionModel saved = workoutSessionPersistencePort.save(sessionModel);
                updateRoutineLastUsed(routine.getId(), userId, request.getStartTime());

                return workoutConverter.toResponse(saved, routine.getName());
        }

        @Override
        @Transactional(readOnly = true)
        public WorkoutSessionResponse getWorkoutSessionDetails(Long sessionId, Long userId) {
                log.info("WORKOUT_SERVICE_GET_DETAILS | sessionId={} | userId={}", sessionId, userId);

                WorkoutSessionModel session = workoutSessionPersistencePort
                                .findByIdAndUserIdWithDetails(sessionId, userId)
                                .orElseThrow(() -> {
                                        log.error("WORKOUT_SERVICE_SESSION_NOT_FOUND | sessionId={} | userId={}",
                                                        sessionId, userId);
                                        return new WorkoutSessionNotFoundException(sessionId, userId);
                                });

                log.debug("WORKOUT_SERVICE_SESSION_FOUND | sessionId={} | exerciseCount={}",
                                sessionId, session.getExercises().size());

                // Obtener nombre de la rutina
                String routineName = routinePersistencePort.findByIdAndUserId(session.getRoutineId(), userId)
                                .map(RoutineModel::getName)
                                .orElse("Unknown Routine");

                return workoutConverter.toResponse(session, routineName);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<WorkoutSessionSummaryResponse> getWorkoutHistory(
                        WorkoutHistoryFilterRequest filters, Long userId, Pageable pageable) {

                log.info("WORKOUT_SERVICE_GET_HISTORY | userId={} | filters={}", userId, filters);

                // Construir specification
                Specification<WorkoutSessionEntity> spec = WorkoutSessionSpecification.withFilters(filters, userId);

                // Aplicar sort
                Sort sort = buildSort(filters);
                Pageable pageableWithSort = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                sort);

                // Consultar
                Page<WorkoutSessionEntity> page = workoutSessionRepository.findAll(spec, pageableWithSort);

                log.info("WORKOUT_SERVICE_HISTORY_FOUND | userId={} | totalElements={} | page={}",
                                userId, page.getTotalElements(), page.getNumber());

                // Convertir a summary
                return page.map(workoutConverter::toSummaryResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<WorkoutSessionSummaryResponse> getRecentWorkouts(Long userId, int limit) {
                log.info("WORKOUT_SERVICE_GET_RECENT | userId={} | limit={}", userId, limit);

                Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startTime"));
                Page<WorkoutSessionModel> sessions = workoutSessionPersistencePort.findByUserId(userId, pageable);

                log.info("WORKOUT_SERVICE_RECENT_FOUND | userId={} | count={}", userId, sessions.getNumberOfElements());

                return sessions.map(model -> {
                        String routineName = routinePersistencePort.findByIdAndUserId(model.getRoutineId(), userId)
                                        .map(RoutineModel::getName)
                                        .orElse("Unknown");
                        return workoutConverter.toSummaryResponse(model, routineName);
                });
        }

        @Override
        @Transactional
        public void deleteWorkoutSession(Long sessionId, Long userId) {
                log.info("WORKOUT_SERVICE_DELETE | sessionId={} | userId={}", sessionId, userId);

                WorkoutSessionModel session = workoutSessionPersistencePort
                                .findByIdAndUserIdWithDetails(sessionId, userId)
                                .orElseThrow(() -> {
                                        log.error("WORKOUT_SERVICE_DELETE_NOT_FOUND | sessionId={} | userId={}",
                                                        sessionId, userId);
                                        return new WorkoutSessionNotFoundException(sessionId, userId);
                                });

                workoutSessionPersistencePort.deleteById(sessionId);

                log.info("WORKOUT_SERVICE_DELETE_SUCCESS | sessionId={}", sessionId);
        }

        @Override
        @Transactional(readOnly = true)
        public Double getTotalVolume(Long userId) {
                log.info("WORKOUT_SERVICE_GET_TOTAL_VOLUME | userId={}", userId);

                Double volume = workoutSessionPersistencePort.sumTotalVolumeByUserId(userId);

                log.info("WORKOUT_SERVICE_TOTAL_VOLUME | userId={} | volume={}", userId, volume);

                return volume != null ? volume : 0.0;
        }

        // ── Private helpers ───────────────────────────────────────────────────────

        private void validateWorkoutRequest(SaveWorkoutSessionRequest request, Long userId) {
                log.debug("WORKOUT_SERVICE_VALIDATE_REQUEST | userId={} | routineId={}", userId,
                                request.getRoutineId());

                if (request.getSetExecutions() == null || request.getSetExecutions().isEmpty()) {
                        log.error("WORKOUT_SERVICE_VALIDATION_FAILED | reason=EMPTY_SETS | userId={}", userId);
                        throw InvalidWorkoutDataException.emptySetExecutions();
                }

                if (request.getEndTime().isBefore(request.getStartTime())) {
                        log.error("WORKOUT_SERVICE_VALIDATION_FAILED | reason=INVALID_TIME_RANGE | userId={}", userId);
                        throw InvalidWorkoutDataException.invalidTimeRange();
                }

                // Validar que cada parámetro tenga al menos un valor
                for (SaveWorkoutSessionRequest.SetExecutionRequest setExec : request.getSetExecutions()) {
                        for (SaveWorkoutSessionRequest.ParameterValueRequest param : setExec.getParameters()) {
                                if (param.getNumericValue() == null &&
                                                param.getIntegerValue() == null &&
                                                param.getDurationValue() == null &&
                                                (param.getStringValue() == null || param.getStringValue().isBlank())) {

                                        log.error("WORKOUT_SERVICE_VALIDATION_FAILED | reason=EMPTY_PARAMETER | userId={} | parameterId={}",
                                                        userId, param.getParameterId());
                                        throw InvalidWorkoutDataException.missingParameterValue();
                                }
                        }
                }

                log.debug("WORKOUT_SERVICE_VALIDATION_SUCCESS | userId={} | setCount={}",
                                userId, request.getSetExecutions().size());
        }

        // ── PARTE 2: Métodos helper para construcción del modelo ─────────────────

        private WorkoutSessionModel buildWorkoutSessionModel(
                        SaveWorkoutSessionRequest request, RoutineModel routine, Long userId) {

                log.debug("WORKOUT_SERVICE_BUILD_MODEL_START | userId={} | routineId={}", userId, routine.getId());

                // Agrupar sets por ejercicio
                Map<Long, List<SaveWorkoutSessionRequest.SetExecutionRequest>> setsByExercise = request
                                .getSetExecutions()
                                .stream()
                                .collect(Collectors.groupingBy(
                                                SaveWorkoutSessionRequest.SetExecutionRequest::getExerciseId,
                                                LinkedHashMap::new,
                                                Collectors.toList()));

                log.debug("WORKOUT_SERVICE_EXERCISES_GROUPED | exerciseCount={}", setsByExercise.size());

                // Construir ejercicios de sesión
                List<SessionExerciseModel> exercises = new ArrayList<>();
                for (Map.Entry<Long, List<SaveWorkoutSessionRequest.SetExecutionRequest>> entry : setsByExercise
                                .entrySet()) {
                        Long exerciseId = entry.getKey();
                        List<SaveWorkoutSessionRequest.SetExecutionRequest> sets = entry.getValue();

                        SessionExerciseModel exerciseModel = buildSessionExercise(exerciseId, sets);
                        exercises.add(exerciseModel);
                }

                // Construir sesión
                WorkoutSessionModel sessionModel = WorkoutSessionModel.builder()
                                .routineId(routine.getId())
                                .userId(userId)
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .performanceScore(request.getPerformanceScore())
                                .exercises(exercises)
                                .build();

                log.debug("WORKOUT_SERVICE_BUILD_MODEL_SUCCESS | exerciseCount={} | totalSets={}",
                                exercises.size(),
                                exercises.stream().mapToInt(e -> e.getSets().size()).sum());

                return sessionModel;
        }

        private SessionExerciseModel buildSessionExercise(
                        Long exerciseId, List<SaveWorkoutSessionRequest.SetExecutionRequest> setRequests) {

                log.debug("WORKOUT_SERVICE_BUILD_EXERCISE | exerciseId={} | setCount={}",
                                exerciseId, setRequests.size());

                // Determinar timestamps del ejercicio
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

                // Determinar status general
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

                // Construir sets
                List<SetExecutionModel> sets = setRequests.stream()
                                .map(this::buildSetExecution)
                                .collect(Collectors.toList());

                SessionExerciseModel exerciseModel = SessionExerciseModel.builder()
                                .exerciseId(exerciseId)
                                .status(status)
                                .startedAt(exerciseStarted)
                                .completedAt(exerciseCompleted)
                                .sets(sets)
                                .build();

                log.debug("WORKOUT_SERVICE_EXERCISE_BUILT | exerciseId={} | status={} | setCount={}",
                                exerciseId, status, sets.size());

                return exerciseModel;
        }

        private SetExecutionModel buildSetExecution(SaveWorkoutSessionRequest.SetExecutionRequest setRequest) {
                log.debug("WORKOUT_SERVICE_BUILD_SET | position={} | setTemplateId={}",
                                setRequest.getPosition(), setRequest.getSetTemplateId());

                // Parse SetType
                SetType setType = SetType.NORMAL;
                if (setRequest.getSetType() != null) {
                        try {
                                setType = SetType.valueOf(setRequest.getSetType().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                log.warn("WORKOUT_SERVICE_INVALID_SET_TYPE | value={} | defaulting=NORMAL",
                                                setRequest.getSetType());
                        }
                }

                // Parse SetExecutionStatus
                SetExecutionStatus status = SetExecutionStatus.COMPLETED;
                if (setRequest.getStatus() != null) {
                        try {
                                status = SetExecutionStatus.valueOf(setRequest.getStatus().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                log.warn("WORKOUT_SERVICE_INVALID_STATUS | value={} | defaulting=COMPLETED",
                                                setRequest.getStatus());
                        }
                }

                // Construir parámetros
                List<SetExecutionParameterModel> parameters = setRequest.getParameters().stream()
                                .map(this::buildSetExecutionParameter)
                                .collect(Collectors.toList());

                SetExecutionModel setModel = SetExecutionModel.builder()
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

                log.debug("WORKOUT_SERVICE_SET_BUILT | position={} | paramCount={} | volume={}",
                                setRequest.getPosition(), parameters.size(), setModel.calculateVolume());

                return setModel;
        }

        private SetExecutionParameterModel buildSetExecutionParameter(
                        SaveWorkoutSessionRequest.ParameterValueRequest paramRequest) {

                log.debug("WORKOUT_SERVICE_BUILD_PARAM | parameterId={}", paramRequest.getParameterId());

                return SetExecutionParameterModel.builder()
                                .parameterId(paramRequest.getParameterId())
                                .numericValue(paramRequest.getNumericValue())
                                .integerValue(paramRequest.getIntegerValue())
                                .durationValue(paramRequest.getDurationValue())
                                .stringValue(paramRequest.getStringValue())
                                .isPersonalRecord(false) // Por ahora, PRs deshabilitados
                                .build();
        }

        private void updateRoutineLastUsed(Long routineId, Long userId, LocalDateTime usedAt) {
                try {
                        log.debug("WORKOUT_SERVICE_UPDATE_ROUTINE_LAST_USED | routineId={} | userId={}",
                                        routineId, userId);

                        routinePersistencePort.updateLastUsedAt(routineId, userId, usedAt);

                        log.debug("WORKOUT_SERVICE_ROUTINE_LAST_USED_UPDATED | routineId={}", routineId);
                } catch (Exception e) {
                        log.warn("WORKOUT_SERVICE_ROUTINE_UPDATE_FAILED | routineId={} | error={}",
                                        routineId, e.getMessage());
                }
        }

        private Sort buildSort(WorkoutHistoryFilterRequest filters) {
                String sortBy = filters.getSortBy() != null ? filters.getSortBy() : "startTime";
                String sortDirection = filters.getSortDirection() != null ? filters.getSortDirection() : "DESC";

                Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                log.debug("WORKOUT_SERVICE_BUILD_SORT | sortBy={} | direction={}", sortBy, direction);

                return Sort.by(direction, sortBy);
        }

        private Long getUserIdFromAuth() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                        throw new SecurityException("Usuario no autenticado");
                }

                // Caso 1: Principal es CustomUserDetails (si algún día lo usas)
                if (auth.getPrincipal() instanceof CustomUserDetails) {
                        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
                }

                // Caso 2: Principal es Jwt (tu configuración actual)
                if (auth.getPrincipal() instanceof Jwt) {
                        Jwt jwt = (Jwt) auth.getPrincipal();
                        // Extrae el userId del claim
                        Long userId = jwt.getClaim("userId");
                        if (userId == null) {
                                // Fallback: intenta con "user_id" o "sub" si es numérico
                                userId = jwt.getClaim("user_id");
                                if (userId == null) {
                                        String sub = jwt.getSubject();
                                        try {
                                                userId = Long.parseLong(sub);
                                        } catch (NumberFormatException e) {
                                                throw new SecurityException("No se pudo extraer userId del JWT");
                                        }
                                }
                        }
                        return userId;
                }

                // Caso 3: Es un JwtAuthenticationToken (a veces el principal es el token mismo)
                if (auth instanceof JwtAuthenticationToken) {
                        Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
                        return jwt.getClaim("userId");
                }

                throw new SecurityException("Tipo de autenticación no soportado: " + auth.getClass());
        }
}