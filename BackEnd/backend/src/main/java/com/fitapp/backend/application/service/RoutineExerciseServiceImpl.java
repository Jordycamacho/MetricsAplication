package com.fitapp.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.application.dto.RoutineSetParameter.response.RoutineSetParameterResponse;
import com.fitapp.backend.application.dto.RoutineSetTemplate.response.RoutineSetTemplateResponse;
import com.fitapp.backend.application.dto.routine.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseParameterResponse;
import com.fitapp.backend.application.dto.routine.response.RoutineExerciseResponse;
import com.fitapp.backend.application.ports.input.RoutineExerciseUseCase;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutineExercisePersistencePort;
import com.fitapp.backend.application.ports.output.RoutinePersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineExerciseServiceImpl implements RoutineExerciseUseCase {

        private final RoutinePersistencePort routinePersistencePort;
        private final RoutineExercisePersistencePort routineExercisePersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final ExercisePersistencePort exercisePersistencePort;
        private final RoutineConverter routineConverter;
        private final RoutineRepository routineRepository;

        @Override
        @Transactional
        public RoutineExerciseResponse addExerciseToRoutine(Long routineId, AddExerciseToRoutineRequest request,
                        String userEmail) {
                log.info("ADD_EXERCISE_TO_ROUTINE | routineId={} | exerciseId={} | user={}",
                                routineId, request.getExerciseId(), userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                RoutineEntity routineEntity = routineRepository.findById(routineId)
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                if (!routineEntity.getUser().getId().equals(user.getId())) {
                        log.error("UNAUTHORIZED_ROUTINE_ACCESS | routineId={} | userId={}", routineId, user.getId());
                        throw new RuntimeException("Unauthorized");
                }

                String exerciseName = exercisePersistencePort.findNameById(request.getExerciseId());
                if (exerciseName == null) {
                        throw new RuntimeException("Exercise not found: " + request.getExerciseId());
                }

                RoutineExerciseEntity routineExercise = routineConverter.addExerciseToRoutine(
                                routineEntity, request, request.getExerciseId());

                routineEntity = routineRepository.save(routineEntity);

                RoutineExerciseEntity savedExercise = routineEntity.getExercises().stream()
                                .filter(e -> e.getExercise().getId().equals(request.getExerciseId())
                                                && e.getPosition().equals(routineExercise.getPosition()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not found after save"));

                log.info("EXERCISE_ADDED | routineId={} | exerciseId={} | position={}",
                                routineId, request.getExerciseId(), savedExercise.getPosition());

                return mapToResponse(routineConverter.convertRoutineExercise(savedExercise), exerciseName);
        }

        @Override
        @Transactional
        public RoutineExerciseResponse updateExerciseInRoutine(Long routineId, Long exerciseId,
                        AddExerciseToRoutineRequest request, String userEmail) {
                log.info("UPDATE_EXERCISE_IN_ROUTINE | routineId={} | exerciseId={} | user={}",
                                routineId, exerciseId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                RoutineExerciseModel existingExercise = routine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                                "Exercise not found in routine: " + exerciseId));

                if (request.getSessionNumber() != null)
                        existingExercise.setSessionNumber(request.getSessionNumber());
                if (request.getSessionOrder() != null)
                        existingExercise.setSessionOrder(request.getSessionOrder());
                if (request.getRestAfterExercise() != null)
                        existingExercise.setRestAfterExercise(request.getRestAfterExercise());
                if (request.getDayOfWeek() != null) {
                        try {
                                existingExercise.setDayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek()));
                        } catch (IllegalArgumentException e) {
                                log.warn("INVALID_DAY_OF_WEEK | value={}", request.getDayOfWeek());
                        }
                }

                RoutineModel updatedRoutine = routinePersistencePort.update(routine);

                RoutineExerciseModel updatedExercise = updatedRoutine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not found after update"));

                String exerciseName = exercisePersistencePort.findNameById(updatedExercise.getExerciseId());

                log.info("EXERCISE_UPDATED | routineId={} | exerciseId={}", routineId, exerciseId);

                return mapToResponse(updatedExercise, exerciseName);
        }

        @Override
        @Transactional
        public void removeExerciseFromRoutine(Long routineId, Long exerciseId, String userEmail) {
                log.info("REMOVE_EXERCISE_FROM_ROUTINE | routineId={} | exerciseId={} | user={}",
                                routineId, exerciseId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                routineExercisePersistencePort.deleteByIdAndRoutineId(exerciseId, routineId);

                log.info("EXERCISE_REMOVED | routineId={} | exerciseId={}", routineId, exerciseId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getExercisesBySession(Long routineId, Integer sessionNumber,
                        String userEmail) {
                log.debug("GET_EXERCISES_BY_SESSION | routineId={} | session={} | user={}",
                                routineId, sessionNumber, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                return routineExercisePersistencePort
                                .findByRoutineIdAndSessionNumber(routineId, sessionNumber)
                                .stream()
                                .map(exercise -> mapToResponse(
                                                exercise,
                                                exercisePersistencePort.findNameById(exercise.getExerciseId())))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getExercisesByDay(Long routineId, String dayOfWeek, String userEmail) {
                log.debug("GET_EXERCISES_BY_DAY | routineId={} | day={} | user={}",
                                routineId, dayOfWeek, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                return routineExercisePersistencePort
                                .findByRoutineIdAndDayOfWeek(routineId, dayOfWeek)
                                .stream()
                                .map(exercise -> mapToResponse(
                                                exercise,
                                                exercisePersistencePort.findNameById(exercise.getExerciseId())))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void reorderExercises(Long routineId, List<Long> exerciseIds, String userEmail) {
                log.info("REORDER_EXERCISES | routineId={} | count={} | user={}",
                                routineId, exerciseIds.size(), userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                Set<Long> routineExerciseIds = routine.getExercises().stream()
                                .map(RoutineExerciseModel::getId)
                                .collect(Collectors.toSet());

                if (!routineExerciseIds.containsAll(exerciseIds)) {
                        throw new IllegalArgumentException("Some exercise IDs do not belong to routine: " + routineId);
                }

                Map<Long, RoutineExerciseModel> exerciseMap = routine.getExercises().stream()
                                .collect(Collectors.toMap(RoutineExerciseModel::getId, Function.identity()));

                List<RoutineExerciseModel> reordered = new ArrayList<>();
                for (int i = 0; i < exerciseIds.size(); i++) {
                        RoutineExerciseModel exercise = exerciseMap.get(exerciseIds.get(i));
                        exercise.setPosition(i + 1);
                        reordered.add(exercise);
                }

                routine.getExercises().stream()
                                .filter(e -> !exerciseIds.contains(e.getId()))
                                .forEach(reordered::add);

                routine.setExercises(reordered);
                routinePersistencePort.update(routine);

                log.info("EXERCISES_REORDERED | routineId={} | reordered={}", routineId, exerciseIds.size());
        }

        @Override
        @Transactional(readOnly = true)
        public List<RoutineExerciseResponse> getRoutineExercises(Long routineId, String userEmail) {
                log.info("GET_ROUTINE_EXERCISES | routineId={} | user={}", routineId, userEmail);

                UserModel user = userPersistencePort.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort.findByRoutineId(routineId);

                log.debug("ROUTINE_EXERCISES_FOUND | routineId={} | count={}", routineId, exercises.size());

                return exercises.stream()
                                .map(exercise -> mapToResponse(
                                                exercise,
                                                exercisePersistencePort.findNameById(exercise.getExerciseId())))
                                .collect(Collectors.toList());
        }

        // =========================================================
        // Helpers privados
        // =========================================================

        private RoutineExerciseResponse mapToResponse(RoutineExerciseModel model, String exerciseName) {
                if (model == null)
                        return null;

                int setsCount = model.getSets() != null ? model.getSets().size() : 0;

                return RoutineExerciseResponse.builder()
                                .id(model.getId())
                                .exerciseId(model.getExerciseId())
                                .exerciseName(exerciseName)
                                .position(model.getPosition())
                                .sessionNumber(model.getSessionNumber())
                                .dayOfWeek(model.getDayOfWeek())
                                .sessionOrder(model.getSessionOrder())
                                .restAfterExercise(model.getRestAfterExercise())
                                .sets(setsCount)
                                .targetParameters(mapToParameterResponses(model.getTargetParameters()))
                                .setsTemplate(mapToSetTemplateResponses(model.getSets()))
                                .build();
        }

        private List<RoutineExerciseParameterResponse> mapToParameterResponses(
                        List<RoutineExerciseParameterModel> parameters) {
                if (parameters == null)
                        return new ArrayList<>();

                return parameters.stream()
                                .map(param -> RoutineExerciseParameterResponse.builder()
                                                .id(param.getId())
                                                .parameterId(param.getParameterId())
                                                .numericValue(param.getNumericValue())
                                                .integerValue(param.getIntegerValue())
                                                .durationValue(param.getDurationValue())
                                                .stringValue(param.getStringValue())
                                                .minValue(param.getMinValue())
                                                .maxValue(param.getMaxValue())
                                                .defaultValue(param.getDefaultValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetTemplateResponse> mapToSetTemplateResponses(List<RoutineSetTemplateModel> sets) {
                if (sets == null)
                        return new ArrayList<>();

                return sets.stream()
                                .map(set -> RoutineSetTemplateResponse.builder()
                                                .id(set.getId())
                                                .position(set.getPosition())
                                                .subSetNumber(set.getSubSetNumber())
                                                .groupId(set.getGroupId())
                                                .setType(set.getSetType())
                                                .restAfterSet(set.getRestAfterSet())
                                                .parameters(mapToSetParameterResponses(set.getParameters()))
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetParameterResponse> mapToSetParameterResponses(
                        List<RoutineSetParameterModel> parameters) {
                if (parameters == null)
                        return new ArrayList<>();

                return parameters.stream()
                                .map(param -> RoutineSetParameterResponse.builder()
                                                .id(param.getId())
                                                .parameterId(param.getParameterId())
                                                .numericValue(param.getNumericValue())
                                                .durationValue(param.getDurationValue())
                                                .integerValue(param.getIntegerValue())
                                                .build())
                                .collect(Collectors.toList());
        }
}