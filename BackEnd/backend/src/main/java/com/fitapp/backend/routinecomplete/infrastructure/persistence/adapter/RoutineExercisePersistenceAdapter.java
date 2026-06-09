package com.fitapp.backend.routinecomplete.infrastructure.persistence.adapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.routinecomplete.aplication.port.output.RoutineExercisePersistencePort;
import com.fitapp.backend.routinecomplete.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.repository.RoutineExerciseParameterRepository;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.repository.RoutineExerciseRepository;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.repository.RoutineSetParameterRepository;
import com.fitapp.backend.routinecomplete.infrastructure.persistence.repository.RoutineSetTemplateRepository;
import com.fitapp.backend.workout.infrastructure.persistence.repository.SetExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineExercisePersistenceAdapter implements RoutineExercisePersistencePort {

    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineSetTemplateRepository setTemplateRepository;
    private final RoutineSetParameterRepository setParameterRepository;
    private final RoutineExerciseParameterRepository routineExerciseParameterRepository;
    private final SetExecutionRepository setExecutionRepository;
    private final RoutineConverter routineConverter;

    @Override
    @Transactional
    public RoutineExerciseModel save(RoutineExerciseModel model) {
        log.debug("SAVE_ROUTINE_EXERCISE | exerciseId={} | routineId={}",
                model.getExerciseId(), model.getRoutineId());

        RoutineEntity routine = routineExerciseRepository.findRoutineById(model.getRoutineId())
                .orElseThrow(() -> new RuntimeException("Routine not found: " + model.getRoutineId()));

        ExerciseEntity exercise = routineExerciseRepository.findExerciseById(model.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + model.getExerciseId()));

        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setRoutine(routine);
        entity.setExercise(exercise);
        entity.setPosition(model.getPosition());
        entity.setSessionNumber(model.getSessionNumber() != null ? model.getSessionNumber() : 1);
        entity.setDayOfWeek(model.getDayOfWeek());
        entity.setSessionOrder(model.getSessionOrder());
        entity.setRestAfterExercise(model.getRestAfterExercise());
        entity.setCircuitGroupId(model.getCircuitGroupId());
        entity.setCircuitRoundCount(model.getCircuitRoundCount());
        entity.setSuperSetGroupId(model.getSuperSetGroupId());
        entity.setAmrapDurationSeconds(model.getAmrapDurationSeconds());
        entity.setEmomIntervalSeconds(model.getEmomIntervalSeconds());
        entity.setEmomTotalRounds(model.getEmomTotalRounds());
        entity.setTabataWorkSeconds(model.getTabataWorkSeconds());
        entity.setTabataRestSeconds(model.getTabataRestSeconds());
        entity.setTabataRounds(model.getTabataRounds());
        entity.setNotes(model.getNotes());

        RoutineExerciseEntity saved = routineExerciseRepository.save(entity);
        log.info("SAVE_ROUTINE_EXERCISE_OK | id={} | position={}", saved.getId(), saved.getPosition());

        return routineConverter.convertRoutineExercise(saved);
    }

    @Override
    @Transactional
    public RoutineExerciseModel update(RoutineExerciseModel model) {
        log.debug("UPDATE_ROUTINE_EXERCISE | id={} | routineId={}", model.getId(), model.getRoutineId());

        RoutineExerciseEntity entity = routineExerciseRepository
                .findByIdAndRoutineId(model.getId(), model.getRoutineId())
                .orElseThrow(() -> new RuntimeException(
                        "RoutineExercise not found: id=" + model.getId() + ", routineId=" + model.getRoutineId()));

        if (model.getExerciseId() != null
                && (entity.getExercise() == null || !model.getExerciseId().equals(entity.getExercise().getId()))) {
            ExerciseEntity exercise = routineExerciseRepository.findExerciseById(model.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercise not found: " + model.getExerciseId()));
            entity.setExercise(exercise);
        }
        if (model.getSessionNumber() != null) {
            entity.setSessionNumber(model.getSessionNumber());
        }
        if (model.getDayOfWeek() != null) {
            entity.setDayOfWeek(model.getDayOfWeek());
        }
        if (model.getSessionOrder() != null) {
            entity.setSessionOrder(model.getSessionOrder());
        }
        if (model.getRestAfterExercise() != null) {
            entity.setRestAfterExercise(model.getRestAfterExercise());
        }
        if (model.getPosition() != null) {
            entity.setPosition(model.getPosition());
        }
        if (model.getCircuitGroupId() != null) {
            entity.setCircuitGroupId(model.getCircuitGroupId());
        }
        if (model.getCircuitRoundCount() != null) {
            entity.setCircuitRoundCount(model.getCircuitRoundCount());
        }
        if (model.getSuperSetGroupId() != null) {
            entity.setSuperSetGroupId(model.getSuperSetGroupId());
        }
        if (model.getAmrapDurationSeconds() != null) {
            entity.setAmrapDurationSeconds(model.getAmrapDurationSeconds());
        }
        if (model.getEmomIntervalSeconds() != null) {
            entity.setEmomIntervalSeconds(model.getEmomIntervalSeconds());
        }
        if (model.getEmomTotalRounds() != null) {
            entity.setEmomTotalRounds(model.getEmomTotalRounds());
        }
        if (model.getTabataWorkSeconds() != null) {
            entity.setTabataWorkSeconds(model.getTabataWorkSeconds());
        }
        if (model.getTabataRestSeconds() != null) {
            entity.setTabataRestSeconds(model.getTabataRestSeconds());
        }
        if (model.getTabataRounds() != null) {
            entity.setTabataRounds(model.getTabataRounds());
        }
        if (model.getNotes() != null) {
            entity.setNotes(model.getNotes());
        }

        RoutineExerciseEntity saved = routineExerciseRepository.save(entity);
        log.info("UPDATE_ROUTINE_EXERCISE_OK | id={} | exerciseId={}", saved.getId(),
                saved.getExercise() != null ? saved.getExercise().getId() : null);
        return routineConverter.convertRoutineExercise(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoutineExerciseModel> findByIdAndRoutineId(Long id, Long routineId) {
        log.debug("FIND_EXERCISE_BY_ID_AND_ROUTINE | id={} | routineId={}", id, routineId);
        return routineExerciseRepository.findByIdAndRoutineId(id, routineId)
                .map(routineConverter::convertRoutineExercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineId(Long routineId) {
        log.debug("FIND_EXERCISES_BY_ROUTINE | routineId={}", routineId);
        List<RoutineExerciseModel> result = routineExerciseRepository.findByRoutineId(routineId).stream()
                .map(routineConverter::convertRoutineExercise)
                .collect(Collectors.toList());
        log.debug("FIND_EXERCISES_BY_ROUTINE_FOUND | routineId={} | count={}", routineId, result.size());
        return result;
    }

    @Override
    @Transactional
    public void deleteByIdAndRoutineId(Long id, Long routineId) {
        log.info("DELETE_ROUTINE_EXERCISE | id={} | routineId={}", id, routineId);
        if (!routineExerciseRepository.findByIdAndRoutineId(id, routineId).isPresent()) {
            throw new RuntimeException("RoutineExercise not found: id=" + id + ", routineId=" + routineId);
        }

        // Detach historical workout metrics, then bulk-delete template graph (no bag fetch).
        int detachedExecutions = setExecutionRepository.detachSetTemplateByRoutineExerciseId(id);
        int deletedSetParams = setParameterRepository.deleteByRoutineExerciseId(id);
        int deletedSets = setTemplateRepository.deleteByRoutineExerciseId(id);
        int deletedTargetParams = routineExerciseParameterRepository.deleteByRoutineExerciseId(id);
        int deleted = routineExerciseRepository.deleteRowByIdAndRoutineId(id, routineId);
        if (deleted == 0) {
            throw new RuntimeException("RoutineExercise not found: id=" + id + ", routineId=" + routineId);
        }

        log.info("DELETE_ROUTINE_EXERCISE_OK | id={} | routineId={} | detachedExecutions={} | sets={} | setParams={} | targetParams={}",
                id, routineId, detachedExecutions, deletedSets, deletedSetParams, deletedTargetParams);
    }

    @Override
    @Transactional
    public void deleteByRoutineId(Long routineId) {
        log.info("DELETE_ALL_EXERCISES | routineId={}", routineId);
        List<RoutineExerciseEntity> exercises = routineExerciseRepository.findByRoutineId(routineId);
        routineExerciseRepository.deleteAll(exercises);
        log.info("DELETE_ALL_EXERCISES_OK | routineId={} | deleted={}", routineId, exercises.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineIdAndSessionNumber(Long routineId, Integer sessionNumber) {
        log.debug("FIND_EXERCISES_BY_SESSION | routineId={} | session={}", routineId, sessionNumber);
        return routineExerciseRepository.findByRoutineIdAndSessionNumber(routineId, sessionNumber).stream()
                .map(routineConverter::convertRoutineExercise)
                .sorted(Comparator.comparing(RoutineExerciseModel::getSessionOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineIdAndDayOfWeek(Long routineId, String dayOfWeek) {
        log.debug("FIND_EXERCISES_BY_DAY | routineId={} | day={}", routineId, dayOfWeek);
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            return routineExerciseRepository.findByRoutineIdAndDayOfWeek(routineId, day).stream()
                    .map(routineConverter::convertRoutineExercise)
                    .sorted(Comparator.comparing(RoutineExerciseModel::getSessionOrder,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("INVALID_DAY_OF_WEEK | routineId={} | day={}", routineId, dayOfWeek);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoutineExerciseModel> findById(Long id) {
        log.debug("FIND_EXERCISE_BY_ID | id={}", id);
        return routineExerciseRepository.findById(id)
                .map(routineConverter::convertRoutineExercise);
    }
}