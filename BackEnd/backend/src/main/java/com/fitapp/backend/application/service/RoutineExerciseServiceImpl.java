package com.fitapp.backend.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

        // ── Mutaciones (invalidan caché de la rutina afectada) ────────────────────

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "routines", allEntries = true),
                        @CacheEvict(value = "userRoutines", allEntries = true),
                        @CacheEvict(value = "routineExercises", allEntries = true)
        })
        public RoutineExerciseResponse addExerciseToRoutine(
                        Long routineId, AddExerciseToRoutineRequest request, String userEmail) {
                log.info("ADD_EXERCISE | routineId={} | exerciseId={} | user={}", routineId, request.getExerciseId(),
                                userEmail);

                UserModel user = findUser(userEmail);

                RoutineEntity routineEntity = routineRepository.findById(routineId)
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                if (!routineEntity.getUser().getId().equals(user.getId())) {
                        log.error("UNAUTHORIZED | routineId={} | userId={}", routineId, user.getId());
                        throw new RuntimeException("Unauthorized access to routine: " + routineId);
                }

                // Validar que el ejercicio existe (y obtener el nombre en una sola query)
                String exerciseName = exercisePersistencePort.findNameById(request.getExerciseId());
                if (exerciseName == null) {
                        throw new RuntimeException("Exercise not found: " + request.getExerciseId());
                }

                RoutineExerciseEntity routineExercise = routineConverter.addExerciseToRoutine(
                                routineEntity, request, request.getExerciseId());

                routineEntity = routineRepository.save(routineEntity);

                RoutineExerciseEntity saved = routineEntity.getExercises().stream()
                                .filter(e -> e.getExercise().getId().equals(request.getExerciseId())
                                                && e.getPosition().equals(routineExercise.getPosition()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not found after save"));

                log.info("ADD_EXERCISE_OK | routineId={} | exerciseId={} | position={} | session={}",
                                routineId, request.getExerciseId(), saved.getPosition(), saved.getSessionNumber());

                return mapToResponse(routineConverter.convertRoutineExercise(saved), exerciseName);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "routines", allEntries = true),
                        @CacheEvict(value = "userRoutines", allEntries = true),
                        @CacheEvict(value = "routineExercises", allEntries = true)
        })
        public RoutineExerciseResponse updateExerciseInRoutine(
                        Long routineId, Long exerciseId, AddExerciseToRoutineRequest request, String userEmail) {
                log.info("UPDATE_EXERCISE | routineId={} | exerciseId={} | user={}", routineId, exerciseId, userEmail);

                UserModel user = findUser(userEmail);

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                RoutineExerciseModel exercise = routine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Exercise not in routine: " + exerciseId));

                if (request.getSessionNumber() != null)
                        exercise.setSessionNumber(request.getSessionNumber());
                if (request.getSessionOrder() != null)
                        exercise.setSessionOrder(request.getSessionOrder());
                if (request.getRestAfterExercise() != null)
                        exercise.setRestAfterExercise(request.getRestAfterExercise());

                if (request.getDayOfWeek() != null) {
                        try {
                                exercise.setDayOfWeek(DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                                log.warn("INVALID_DAY_OF_WEEK | value={} | routineId={} | exerciseId={}",
                                                request.getDayOfWeek(), routineId, exerciseId);
                        }
                }

                RoutineModel updated = routinePersistencePort.update(routine);

                RoutineExerciseModel updatedExercise = updated.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                                "Exercise not found after update: " + exerciseId));

                String exerciseName = exercisePersistencePort.findNameById(updatedExercise.getExerciseId());
                log.info("UPDATE_EXERCISE_OK | routineId={} | exerciseId={}", routineId, exerciseId);

                return mapToResponse(updatedExercise, exerciseName);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "routines", allEntries = true),
                        @CacheEvict(value = "userRoutines", allEntries = true),
                        @CacheEvict(value = "routineExercises", allEntries = true)
        })
        public void removeExerciseFromRoutine(Long routineId, Long exerciseId, String userEmail) {
                log.info("REMOVE_EXERCISE | routineId={} | exerciseId={} | user={}", routineId, exerciseId, userEmail);

                UserModel user = findUser(userEmail);

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                routineExercisePersistencePort.deleteByIdAndRoutineId(exerciseId, routineId);

                log.info("REMOVE_EXERCISE_OK | routineId={} | exerciseId={}", routineId, exerciseId);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "routines", allEntries = true),
                        @CacheEvict(value = "userRoutines", allEntries = true),
                        @CacheEvict(value = "routineExercises", allEntries = true)
        })
        public void reorderExercises(Long routineId, List<Long> exerciseIds, String userEmail) {
                log.info("REORDER_EXERCISES | routineId={} | count={} | user={}", routineId, exerciseIds.size(),
                                userEmail);

                UserModel user = findUser(userEmail);

                RoutineModel routine = routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                Set<Long> routineExerciseIds = routine.getExercises().stream()
                                .map(RoutineExerciseModel::getId)
                                .collect(Collectors.toSet());

                if (!routineExerciseIds.containsAll(exerciseIds)) {
                        List<Long> invalid = exerciseIds.stream()
                                        .filter(id -> !routineExerciseIds.contains(id))
                                        .collect(Collectors.toList());
                        log.error("REORDER_INVALID_IDS | routineId={} | invalidIds={}", routineId, invalid);
                        throw new IllegalArgumentException("Exercise IDs not in routine: " + invalid);
                }

                Map<Long, RoutineExerciseModel> exerciseMap = routine.getExercises().stream()
                                .collect(Collectors.toMap(RoutineExerciseModel::getId, Function.identity()));

                List<RoutineExerciseModel> reordered = new ArrayList<>();
                for (int i = 0; i < exerciseIds.size(); i++) {
                        RoutineExerciseModel ex = exerciseMap.get(exerciseIds.get(i));
                        ex.setPosition(i + 1);
                        reordered.add(ex);
                }

                // Añadir ejercicios que no estaban en la lista de reordenado (sin cambiar
                // posición)
                routine.getExercises().stream()
                                .filter(e -> !exerciseIds.contains(e.getId()))
                                .forEach(reordered::add);

                routine.setExercises(reordered);
                routinePersistencePort.update(routine);

                log.info("REORDER_EXERCISES_OK | routineId={} | reordered={}", routineId, exerciseIds.size());
        }

        // ── Consultas (con caché por routineId) ───────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "routineExercises", key = "#routineId + '_' + #userEmail")
        public List<RoutineExerciseResponse> getRoutineExercises(Long routineId, String userEmail) {
                log.info("GET_ROUTINE_EXERCISES | routineId={} | user={}", routineId, userEmail);

                UserModel user = findUser(userEmail);

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort.findByRoutineId(routineId);

                log.debug("GET_ROUTINE_EXERCISES_FOUND | routineId={} | count={}", routineId, exercises.size());

                // Batch load de nombres de ejercicio (evita N+1)
                return mapToResponseListBatch(exercises);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "routineExercisesBySession", key = "#routineId + '_s' + #sessionNumber + '_' + #userEmail")
        public List<RoutineExerciseResponse> getExercisesBySession(
                        Long routineId, Integer sessionNumber, String userEmail) {
                log.debug("GET_EXERCISES_BY_SESSION | routineId={} | session={} | user={}", routineId, sessionNumber,
                                userEmail);

                UserModel user = findUser(userEmail);

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort
                                .findByRoutineIdAndSessionNumber(routineId, sessionNumber);

                log.debug("GET_EXERCISES_BY_SESSION_FOUND | routineId={} | session={} | count={}",
                                routineId, sessionNumber, exercises.size());

                return mapToResponseListBatch(exercises);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "routineExercisesByDay", key = "#routineId + '_d' + #dayOfWeek + '_' + #userEmail")
        public List<RoutineExerciseResponse> getExercisesByDay(Long routineId, String dayOfWeek, String userEmail) {
                log.debug("GET_EXERCISES_BY_DAY | routineId={} | day={} | user={}", routineId, dayOfWeek, userEmail);

                UserModel user = findUser(userEmail);

                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new RuntimeException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort
                                .findByRoutineIdAndDayOfWeek(routineId, dayOfWeek);

                log.debug("GET_EXERCISES_BY_DAY_FOUND | routineId={} | day={} | count={}",
                                routineId, dayOfWeek, exercises.size());

                return mapToResponseListBatch(exercises);
        }

        // ── Batch mapping (evita N+1 en nombre de ejercicio) ─────────────────────

        private List<RoutineExerciseResponse> mapToResponseListBatch(List<RoutineExerciseModel> exercises) {
                if (exercises.isEmpty())
                        return new ArrayList<>();

                // Recoger todos los exerciseId únicos y cargar nombres en batch
                Set<Long> exerciseIds = exercises.stream()
                                .map(RoutineExerciseModel::getExerciseId)
                                .collect(Collectors.toSet());

                Map<Long, String> nameMap = exerciseIds.stream()
                                .collect(Collectors.toMap(
                                                Function.identity(),
                                                id -> {
                                                        String name = exercisePersistencePort.findNameById(id);
                                                        return name != null ? name : "Ejercicio " + id;
                                                }));

                return exercises.stream()
                                .map(ex -> mapToResponse(ex, nameMap.get(ex.getExerciseId())))
                                .collect(Collectors.toList());
        }

        // ── Mapeo ─────────────────────────────────────────────────────────────────

        private RoutineExerciseResponse mapToResponse(RoutineExerciseModel model, String exerciseName) {
                if (model == null)
                        return null;

                return RoutineExerciseResponse.builder()
                                .id(model.getId())
                                .exerciseId(model.getExerciseId())
                                .exerciseName(exerciseName)
                                .position(model.getPosition())
                                .sessionNumber(model.getSessionNumber())
                                .dayOfWeek(model.getDayOfWeek())
                                .sessionOrder(model.getSessionOrder())
                                .restAfterExercise(model.getRestAfterExercise())
                                .sets(model.getSets() != null ? model.getSets().size() : 0)
                                .targetParameters(mapParams(model.getTargetParameters()))
                                .setsTemplate(mapSets(model.getSets()))
                                .build();
        }

        private List<RoutineExerciseParameterResponse> mapParams(List<RoutineExerciseParameterModel> params) {
                if (params == null)
                        return new ArrayList<>();
                return params.stream()
                                .map(p -> RoutineExerciseParameterResponse.builder()
                                                .id(p.getId())
                                                .parameterId(p.getParameterId())
                                                .numericValue(p.getNumericValue())
                                                .integerValue(p.getIntegerValue())
                                                .durationValue(p.getDurationValue())
                                                .stringValue(p.getStringValue())
                                                .minValue(p.getMinValue())
                                                .maxValue(p.getMaxValue())
                                                .defaultValue(p.getDefaultValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetTemplateResponse> mapSets(List<RoutineSetTemplateModel> sets) {
                if (sets == null)
                        return new ArrayList<>();
                return sets.stream()
                                .map(s -> RoutineSetTemplateResponse.builder()
                                                .id(s.getId())
                                                .position(s.getPosition())
                                                .subSetNumber(s.getSubSetNumber())
                                                .groupId(s.getGroupId())
                                                .setType(s.getSetType())
                                                .restAfterSet(s.getRestAfterSet())
                                                .parameters(mapSetParams(s.getParameters()))
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<RoutineSetParameterResponse> mapSetParams(List<RoutineSetParameterModel> params) {
                if (params == null)
                        return new ArrayList<>();
                return params.stream()
                                .map(p -> RoutineSetParameterResponse.builder()
                                                .id(p.getId())
                                                .parameterId(p.getParameterId())
                                                .numericValue(p.getNumericValue())
                                                .durationValue(p.getDurationValue())
                                                .integerValue(p.getIntegerValue())
                                                .repetitions(p.getRepetitions())
                                                .build())
                                .collect(Collectors.toList());
        }

        // ── Utilidades ────────────────────────────────────────────────────────────

        private UserModel findUser(String email) {
                return userPersistencePort.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        }
}