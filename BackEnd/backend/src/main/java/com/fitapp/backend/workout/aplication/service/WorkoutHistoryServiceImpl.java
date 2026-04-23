package com.fitapp.backend.workout.aplication.service;

import com.fitapp.backend.application.dto.workout.response.LastExerciseValuesResponse;
import com.fitapp.backend.application.dto.workout.response.LastExerciseValuesResponse.LastSetValueResponse;
import com.fitapp.backend.application.dto.workout.response.LastExerciseValuesResponse.LastSetValueResponse.ParameterValue;
import com.fitapp.backend.application.ports.input.WorkoutHistoryUseCase;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.repository.RoutineRepository;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SessionExerciseEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionParameterEntity;
import com.fitapp.backend.workout.infrastructure.persistence.repository.SessionExerciseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutHistoryServiceImpl implements WorkoutHistoryUseCase {

    private final SessionExerciseRepository sessionExerciseRepository;
    private final RoutineRepository routineRepository;

    @Override
    @Transactional(readOnly = true)
    public LastExerciseValuesResponse getLastExerciseValues(Long exerciseId, Long userId) {
        log.info("GET_LAST_EXERCISE_VALUES | exerciseId={} | userId={}", exerciseId, userId);

        return sessionExerciseRepository
                .findLastCompletedByUserIdAndExerciseId(userId, exerciseId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, LastExerciseValuesResponse> getLastValuesForExercises(List<Long> exerciseIds, Long userId) {
        log.info("GET_LAST_VALUES_FOR_EXERCISES | exerciseIds={} | userId={}", exerciseIds.size(), userId);

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return new HashMap<>();
        }

        List<SessionExerciseEntity> lastExecutions = sessionExerciseRepository
                .findLastCompletedByUserIdAndExerciseIds(userId, exerciseIds);

        Map<Long, LastExerciseValuesResponse> result = lastExecutions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toMap(
                        LastExerciseValuesResponse::getExerciseId,
                        response -> response,
                        (existing, replacement) -> existing // En caso de duplicados, mantener el primero
                ));

        log.info("GET_LAST_VALUES_RESULT | found={} | requested={}", result.size(), exerciseIds.size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, LastExerciseValuesResponse> getLastValuesForRoutine(Long routineId, Long userId) {
        log.info("GET_LAST_VALUES_FOR_ROUTINE | routineId={} | userId={}", routineId, userId);

        // Obtener la rutina con sus ejercicios
        RoutineEntity routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> {
                    log.error("ROUTINE_NOT_FOUND | routineId={} | userId={}", routineId, userId);
                    return new RuntimeException("Routine not found: " + routineId);
                });

        // Extraer IDs de ejercicios de la rutina
        List<Long> exerciseIds = routine.getExercises().stream()
                .map(RoutineExerciseEntity::getExercise)
                .map(exercise -> exercise.getId())
                .distinct()
                .collect(Collectors.toList());

        log.debug("ROUTINE_EXERCISES_EXTRACTED | routineId={} | exerciseCount={}", routineId, exerciseIds.size());

        // Obtener últimos valores para todos esos ejercicios
        return getLastValuesForExercises(exerciseIds, userId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private LastExerciseValuesResponse convertToResponse(SessionExerciseEntity sessionExercise) {
        log.debug("CONVERT_TO_RESPONSE | sessionExerciseId={} | exerciseId={}", 
                  sessionExercise.getId(), 
                  sessionExercise.getExercise().getId());

        // Convertir sets
        List<LastSetValueResponse> sets = sessionExercise.getSets().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(this::convertSetToResponse)
                .collect(Collectors.toList());

        return LastExerciseValuesResponse.builder()
                .exerciseId(sessionExercise.getExercise().getId())
                .exerciseName(sessionExercise.getExercise().getName())
                .lastWorkoutDate(sessionExercise.getSession().getStartTime())
                .sessionId(sessionExercise.getSession().getId())
                .sets(sets)
                .build();
    }

    private LastSetValueResponse convertSetToResponse(SetExecutionEntity setExecution) {
        log.debug("CONVERT_SET_TO_RESPONSE | setId={} | position={}", 
                  setExecution.getId(), 
                  setExecution.getPosition());

        // Convertir parámetros
        List<ParameterValue> parameters = setExecution.getParameters().stream()
                .map(this::convertParameterToValue)
                .collect(Collectors.toList());

        return LastSetValueResponse.builder()
                .position(setExecution.getPosition())
                .setType(setExecution.getSetType() != null ? setExecution.getSetType().name() : null)
                .parameters(parameters)
                .build();
    }

    private ParameterValue convertParameterToValue(SetExecutionParameterEntity param) {
        log.debug("CONVERT_PARAMETER_TO_VALUE | parameterId={} | value={}", 
                  param.getParameter().getId(), 
                  param.getValueAsDouble());

        return ParameterValue.builder()
                .parameterId(param.getParameter().getId())
                .parameterName(param.getParameter().getName())
                .parameterType(param.getParameter().getParameterType() != null 
                        ? param.getParameter().getParameterType().name() 
                        : null)
                .unit(param.getParameter().getUnit())
                .numericValue(param.getNumericValue())
                .integerValue(param.getIntegerValue())
                .durationValue(param.getDurationValue())
                .stringValue(param.getStringValue())
                .build();
    }
}