package com.fitapp.backend.routinecomplete.routineexercise.aplication.service;

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

import com.fitapp.backend.Exercise.aplication.port.output.ExercisePersistencePort;
import com.fitapp.backend.auth.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.auth.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.routinecomplete.routine.aplication.port.output.RoutinePersistencePort;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.repository.RoutineRepository;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.request.AddExerciseToRoutineRequest;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response.RoutineExerciseParameterResponse;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response.RoutineExerciseResponse;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response.RoutineSetParameterResponse;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.port.input.RoutineExerciseUseCase;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.port.output.RoutineExercisePersistencePort;
import com.fitapp.backend.routinecomplete.routineexercise.domain.exception.BusinessException;
import com.fitapp.backend.routinecomplete.routineexercise.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.routineexercise.domain.model.RoutineExerciseParameterModel;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.routinecomplete.routinesetemplate.aplication.dto.response.RoutineSetTemplateResponse;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.suscription.aplication.service.SubscriptionLimitChecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutineExerciseServiceImpl implements RoutineExerciseUseCase {

        private final RoutinePersistencePort routinePersistencePort;
        private final RoutineExercisePersistencePort routineExercisePersistencePort;
        private final UserPersistencePort userPersistencePort;
        private final SubscriptionLimitChecker limitChecker;
        private final ExercisePersistencePort exercisePersistencePort;
        private final RoutineConverter routineConverter;
        private final RoutineRepository routineRepository; 

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

                validateExerciseRequest(request);

                // 2. Obtener usuario y rutina
                UserModel user = findUser(userEmail);
                RoutineEntity routineEntity = routineRepository.findById(routineId)
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

                if (!routineEntity.getUser().getId().equals(user.getId())) {
                        log.error("UNAUTHORIZED | routineId={} | userId={}", routineId, user.getId());
                        throw new BusinessException("Unauthorized access to routine: " + routineId);
                }

                // 3. Validar existencia del ejercicio
                String exerciseName = exercisePersistencePort.findNameById(request.getExerciseId());
                if (exerciseName == null) {
                        throw new BusinessException("Exercise not found: " + request.getExerciseId());
                }

                // 4. Verificar límite de ejercicios por rutina
                long exerciseCount = routineEntity.getExercises() != null ? routineEntity.getExercises().size() : 0;
                limitChecker.checkExercisesPerRoutineLimit(userEmail, exerciseCount);

                // 5. Agregar ejercicio usando el converter (que ya maneja todos los campos v2 y
                // sets)
                RoutineExerciseEntity routineExercise = routineConverter.addExerciseToRoutine(
                                routineEntity, request, request.getExerciseId());

                // 6. Persistir la rutina con el nuevo ejercicio
                routineEntity = routineRepository.save(routineEntity);

                // 7. Buscar el ejercicio recién guardado para devolverlo
                RoutineExerciseEntity saved = routineEntity.getExercises().stream()
                                .filter(e -> e.getExercise().getId().equals(request.getExerciseId())
                                                && e.getPosition().equals(routineExercise.getPosition()))
                                .findFirst()
                                .orElseThrow(() -> new BusinessException("Exercise not found after save"));

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
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

                RoutineExerciseModel existingExercise = routine.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new BusinessException("Exercise not in routine: " + exerciseId));

                AddExerciseToRoutineRequest mergedRequest = mergeWithExisting(existingExercise, request);
                validateExerciseRequest(mergedRequest);
                updateExerciseModel(existingExercise, request);
                RoutineModel updated = routinePersistencePort.update(routine);

                RoutineExerciseModel updatedExercise = updated.getExercises().stream()
                                .filter(e -> e.getId().equals(exerciseId))
                                .findFirst()
                                .orElseThrow(() -> new BusinessException(
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

                // Verificar que la rutina existe y pertenece al usuario
                routinePersistencePort.findByIdAndUserId(routineId, user.getId())
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

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
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

                Set<Long> routineExerciseIds = routine.getExercises().stream()
                                .map(RoutineExerciseModel::getId)
                                .collect(Collectors.toSet());

                if (!routineExerciseIds.containsAll(exerciseIds)) {
                        List<Long> invalid = exerciseIds.stream()
                                        .filter(id -> !routineExerciseIds.contains(id))
                                        .collect(Collectors.toList());
                        log.error("REORDER_INVALID_IDS | routineId={} | invalidIds={}", routineId, invalid);
                        throw new BusinessException("Exercise IDs not in routine: " + invalid);
                }

                Map<Long, RoutineExerciseModel> exerciseMap = routine.getExercises().stream()
                                .collect(Collectors.toMap(RoutineExerciseModel::getId, Function.identity()));

                List<RoutineExerciseModel> reordered = new ArrayList<>();
                for (int i = 0; i < exerciseIds.size(); i++) {
                        RoutineExerciseModel ex = exerciseMap.get(exerciseIds.get(i));
                        ex.setPosition(i + 1);
                        reordered.add(ex);
                }

                // Añadir los ejercicios no listados al final (manteniendo su orden relativo)
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
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort.findByRoutineId(routineId);

                log.debug("GET_ROUTINE_EXERCISES_FOUND | routineId={} | count={}", routineId, exercises.size());

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
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

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
                                .orElseThrow(() -> new BusinessException("Routine not found: " + routineId));

                List<RoutineExerciseModel> exercises = routineExercisePersistencePort
                                .findByRoutineIdAndDayOfWeek(routineId, dayOfWeek);

                log.debug("GET_EXERCISES_BY_DAY_FOUND | routineId={} | day={} | count={}",
                                routineId, dayOfWeek, exercises.size());

                return mapToResponseListBatch(exercises);
        }

        // ── Métodos privados de soporte ───────────────────────────────────────────

        /**
         * Valida las reglas de negocio para los campos v2 (circuit, superset, AMRAP,
         * EMOM, Tabata).
         * Lanza BusinessException si alguna regla se incumple.
         */
        private void validateExerciseRequest(AddExerciseToRoutineRequest request) {
                // 1. Circuito: si tiene circuitGroupId, debe tener circuitRoundCount > 0
                if (request.getCircuitGroupId() != null && !request.getCircuitGroupId().isBlank()) {
                        if (request.getCircuitRoundCount() == null || request.getCircuitRoundCount() <= 0) {
                                throw new BusinessException(
                                                "Circuit round count must be > 0 when circuitGroupId is provided");
                        }
                }

                // 2. Superset vs Circuito: no puede pertenecer a ambos
                if (request.getSuperSetGroupId() != null && !request.getSuperSetGroupId().isBlank()
                                && request.getCircuitGroupId() != null && !request.getCircuitGroupId().isBlank()) {
                        throw new BusinessException(
                                        "An exercise cannot belong to both a circuit and a superset/giant set");
                }

                // 3. Determinar qué modos especiales están activos
                boolean isAmrap = request.getAmrapDurationSeconds() != null && request.getAmrapDurationSeconds() > 0;
                boolean isEmom = (request.getEmomIntervalSeconds() != null && request.getEmomIntervalSeconds() > 0)
                                || (request.getEmomTotalRounds() != null && request.getEmomTotalRounds() > 0);
                boolean isTabata = (request.getTabataWorkSeconds() != null && request.getTabataWorkSeconds() > 0)
                                || (request.getTabataRestSeconds() != null && request.getTabataRestSeconds() > 0)
                                || (request.getTabataRounds() != null && request.getTabataRounds() > 0);

                // 4. Modos mutuamente excluyentes
                int modeCount = (isAmrap ? 1 : 0) + (isEmom ? 1 : 0) + (isTabata ? 1 : 0);
                if (modeCount > 1) {
                        throw new BusinessException("AMRAP, EMOM and TABATA are mutually exclusive. Use only one.");
                }

                // 5. Validaciones específicas por modo
                if (isAmrap) {
                        if (request.getAmrapDurationSeconds() <= 0) {
                                throw new BusinessException("AMRAP duration must be positive");
                        }
                }

                if (isEmom) {
                        if (request.getEmomIntervalSeconds() == null || request.getEmomIntervalSeconds() <= 0) {
                                throw new BusinessException("EMOM interval seconds must be > 0");
                        }
                        if (request.getEmomTotalRounds() == null || request.getEmomTotalRounds() <= 0) {
                                throw new BusinessException("EMOM total rounds must be > 0");
                        }
                }

                if (isTabata) {
                        if (request.getTabataWorkSeconds() == null || request.getTabataWorkSeconds() <= 0) {
                                throw new BusinessException("Tabata work seconds must be > 0");
                        }
                        if (request.getTabataRestSeconds() == null || request.getTabataRestSeconds() <= 0) {
                                throw new BusinessException("Tabata rest seconds must be > 0");
                        }
                        if (request.getTabataRounds() == null || request.getTabataRounds() <= 0) {
                                throw new BusinessException("Tabata rounds must be > 0");
                        }
                }
        }

        /**
         * Fusiona los valores existentes de un ejercicio con los campos enviados en la
         * request.
         * Útil para validar el estado completo después de una actualización parcial.
         */
        private AddExerciseToRoutineRequest mergeWithExisting(RoutineExerciseModel existing,
                        AddExerciseToRoutineRequest request) {
                AddExerciseToRoutineRequest merged = new AddExerciseToRoutineRequest();
                // Campos básicos
                merged.setExerciseId(existing.getExerciseId());
                merged.setSessionNumber(request.getSessionNumber() != null ? request.getSessionNumber()
                                : existing.getSessionNumber());
                merged.setDayOfWeek(request.getDayOfWeek() != null ? request.getDayOfWeek()
                                : (existing.getDayOfWeek() != null ? existing.getDayOfWeek().name() : null));
                merged.setSessionOrder(request.getSessionOrder() != null ? request.getSessionOrder()
                                : existing.getSessionOrder());
                merged.setRestAfterExercise(request.getRestAfterExercise() != null ? request.getRestAfterExercise()
                                : existing.getRestAfterExercise());

                // Campos v2
                merged.setCircuitGroupId(request.getCircuitGroupId() != null ? request.getCircuitGroupId()
                                : existing.getCircuitGroupId());
                merged.setCircuitRoundCount(request.getCircuitRoundCount() != null ? request.getCircuitRoundCount()
                                : existing.getCircuitRoundCount());
                merged.setSuperSetGroupId(request.getSuperSetGroupId() != null ? request.getSuperSetGroupId()
                                : existing.getSuperSetGroupId());
                merged.setAmrapDurationSeconds(
                                request.getAmrapDurationSeconds() != null ? request.getAmrapDurationSeconds()
                                                : existing.getAmrapDurationSeconds());
                merged.setEmomIntervalSeconds(
                                request.getEmomIntervalSeconds() != null ? request.getEmomIntervalSeconds()
                                                : existing.getEmomIntervalSeconds());
                merged.setEmomTotalRounds(request.getEmomTotalRounds() != null ? request.getEmomTotalRounds()
                                : existing.getEmomTotalRounds());
                merged.setTabataWorkSeconds(request.getTabataWorkSeconds() != null ? request.getTabataWorkSeconds()
                                : existing.getTabataWorkSeconds());
                merged.setTabataRestSeconds(request.getTabataRestSeconds() != null ? request.getTabataRestSeconds()
                                : existing.getTabataRestSeconds());
                merged.setTabataRounds(request.getTabataRounds() != null ? request.getTabataRounds()
                                : existing.getTabataRounds());
                merged.setNotes(request.getNotes() != null ? request.getNotes() : existing.getNotes());

                // Para sets y parámetros, no los fusionamos porque la validación solo necesita
                // los campos escalares.
                // Si en el futuro se valida algo basado en sets, se puede extender.
                merged.setSets(request.getSets()); // Usar los nuevos si vienen, o null
                merged.setTargetParameters(request.getTargetParameters());

                return merged;
        }

        /**
         * Aplica los cambios de la request al modelo existente.
         */
        private void updateExerciseModel(RoutineExerciseModel exercise, AddExerciseToRoutineRequest request) {
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
                                                request.getDayOfWeek(), exercise.getRoutineId(), exercise.getId());
                                throw new BusinessException("Invalid day of week: " + request.getDayOfWeek());
                        }
                }

                // Campos v2
                if (request.getCircuitGroupId() != null)
                        exercise.setCircuitGroupId(request.getCircuitGroupId());
                if (request.getCircuitRoundCount() != null)
                        exercise.setCircuitRoundCount(request.getCircuitRoundCount());
                if (request.getSuperSetGroupId() != null)
                        exercise.setSuperSetGroupId(request.getSuperSetGroupId());
                if (request.getAmrapDurationSeconds() != null)
                        exercise.setAmrapDurationSeconds(request.getAmrapDurationSeconds());
                if (request.getEmomIntervalSeconds() != null)
                        exercise.setEmomIntervalSeconds(request.getEmomIntervalSeconds());
                if (request.getEmomTotalRounds() != null)
                        exercise.setEmomTotalRounds(request.getEmomTotalRounds());
                if (request.getTabataWorkSeconds() != null)
                        exercise.setTabataWorkSeconds(request.getTabataWorkSeconds());
                if (request.getTabataRestSeconds() != null)
                        exercise.setTabataRestSeconds(request.getTabataRestSeconds());
                if (request.getTabataRounds() != null)
                        exercise.setTabataRounds(request.getTabataRounds());
                if (request.getNotes() != null)
                        exercise.setNotes(request.getNotes());

                // Nota: sets y targetParameters se actualizan a través del converter en la
                // entidad,
                // pero si la request los trae, deberían ser reemplazados. El converter en
                // addExerciseToRoutine
                // ya los crea. En update, como se usa RoutinePersistencePort.update, el modelo
                // se convierte a entidad
                // y se maneja cascade. Si quieres permitir actualización de sets/parámetros,
                // tendrías que
                // mapearlos aquí también.
                // Por ahora asumimos que esos no se actualizan parcialmente en este endpoint.
        }

        // ── Batch mapping (evita N+1 en nombre de ejercicio) ─────────────────────

        private List<RoutineExerciseResponse> mapToResponseListBatch(List<RoutineExerciseModel> exercises) {
                if (exercises.isEmpty())
                        return new ArrayList<>();

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

        // ── Mapeo individual ─────────────────────────────────────────────────────

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
                                .circuitGroupId(model.getCircuitGroupId())
                                .circuitRoundCount(model.getCircuitRoundCount())
                                .superSetGroupId(model.getSuperSetGroupId())
                                .amrapDurationSeconds(model.getAmrapDurationSeconds())
                                .emomIntervalSeconds(model.getEmomIntervalSeconds())
                                .emomTotalRounds(model.getEmomTotalRounds())
                                .tabataWorkSeconds(model.getTabataWorkSeconds())
                                .tabataRestSeconds(model.getTabataRestSeconds())
                                .tabataRounds(model.getTabataRounds())
                                .notes(model.getNotes())
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

        private UserModel findUser(String email) {
                return userPersistencePort.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("User not found: " + email));
        }
}