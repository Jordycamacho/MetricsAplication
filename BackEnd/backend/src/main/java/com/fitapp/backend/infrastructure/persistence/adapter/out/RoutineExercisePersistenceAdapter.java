package com.fitapp.backend.infrastructure.persistence.adapter.out;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.application.ports.output.RoutineExercisePersistencePort;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineExerciseRepository;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineExercisePersistenceAdapter implements RoutineExercisePersistencePort {

    private final RoutineExerciseRepository routineExerciseRepository;
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
        int deleted = routineExerciseRepository.deleteByIdAndRoutineId(id, routineId);
        if (deleted > 0) {
            log.info("DELETE_ROUTINE_EXERCISE_OK | id={} | routineId={}", id, routineId);
        } else {
            log.warn("DELETE_ROUTINE_EXERCISE_NOT_FOUND | id={} | routineId={}", id, routineId);
            throw new RuntimeException("Exercise not found in routine: id=" + id + " routineId=" + routineId);
        }
    }

    @Override
    @Transactional
    public void deleteByRoutineId(Long routineId) {
        log.info("DELETE_ALL_EXERCISES | routineId={}", routineId);
        int deleted = routineExerciseRepository.deleteByRoutineId(routineId);
        log.info("DELETE_ALL_EXERCISES_OK | routineId={} | deleted={}", routineId, deleted);
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