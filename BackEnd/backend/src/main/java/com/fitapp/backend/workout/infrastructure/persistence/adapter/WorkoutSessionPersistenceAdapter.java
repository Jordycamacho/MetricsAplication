package com.fitapp.backend.workout.infrastructure.persistence.adapter;

import com.fitapp.backend.workout.aplication.port.output.WorkoutSessionPersistencePort;
import com.fitapp.backend.workout.domain.model.WorkoutSessionModel;
import com.fitapp.backend.workout.infrastructure.persistence.converter.WorkoutConverter;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SessionExerciseEntity;
import com.fitapp.backend.workout.infrastructure.persistence.entity.SetExecutionEntity;
import com.fitapp.backend.workout.infrastructure.persistence.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkoutSessionPersistenceAdapter implements WorkoutSessionPersistencePort {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutConverter workoutConverter;

    @Override
    @Transactional
    public WorkoutSessionModel save(WorkoutSessionModel session) {
        log.info("WORKOUT_PERSISTENCE_SAVE_START | routineId={} | userId={}",
                session.getRoutineId(), session.getUserId());

        var entity = workoutConverter.toEntity(session);

        if (log.isDebugEnabled()) {
            logEntityBeforeSave(entity);
        }

        var saved = workoutSessionRepository.save(entity);

        log.info("WORKOUT_PERSISTENCE_SAVE_SUCCESS | sessionId={} | exerciseCount={} | totalVolume={}",
                saved.getId(),
                saved.getExercises() != null ? saved.getExercises().size() : 0,
                saved.getTotalVolume());

        return workoutConverter.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkoutSessionModel> findById(Long id) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_ID | sessionId={}", id);
        return workoutSessionRepository.findByIdWithDetails(id)
                .map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkoutSessionModel> findByIdAndUserIdWithDetails(Long id, Long userId) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_ID_AND_USER | sessionId={} | userId={}", id, userId);
        return workoutSessionRepository.findByIdAndUserIdWithDetails(id, userId)
                .map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutSessionModel> findByUserId(Long userId, Pageable pageable) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_USER | userId={} | page={} | size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return workoutSessionRepository.findByUserId(userId, pageable)
                .map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutSessionModel> findByRoutineIdAndUserId(Long routineId, Long userId, Pageable pageable) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_ROUTINE | routineId={} | userId={}", routineId, userId);
        return workoutSessionRepository.findByRoutineIdAndUserId(routineId, userId, pageable)
                .map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionModel> findByUserIdAndDateRange(Long userId,
            LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_DATE_RANGE | userId={} | from={} | to={}",
                userId, fromDate, toDate);
        return workoutSessionRepository.findByUserIdAndDateRange(userId, fromDate, toDate)
                .stream()
                .map(workoutConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return workoutSessionRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRoutineIdAndUserId(Long routineId, Long userId) {
        return workoutSessionRepository.countByRoutineIdAndUserId(routineId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionModel> findRecentByUserId(Long userId, int limit) {
        log.debug("WORKOUT_PERSISTENCE_FIND_RECENT | userId={} | limit={}", userId, limit);
        return workoutSessionRepository.findRecentByUserId(userId, limit)
                .stream()
                .map(workoutConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double sumTotalVolumeByUserId(Long userId) {
        return workoutSessionRepository.sumTotalVolumeByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double sumTotalVolumeByUserIdAndDateRange(Long userId,
            LocalDateTime fromDate, LocalDateTime toDate) {
        return workoutSessionRepository.sumTotalVolumeByUserIdAndDateRange(userId, fromDate, toDate);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("WORKOUT_PERSISTENCE_DELETE | sessionId={}", id);
        workoutSessionRepository.deleteById(id);
        log.info("WORKOUT_PERSISTENCE_DELETE_SUCCESS | sessionId={}", id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Log de diagnóstico antes del save. Solo se llama si DEBUG está activo
     * para evitar coste de iteración en producción.
     *
     * FIX: el original hacía set.getSetTemplate().getId() sin null-check.
     * SetExecutionEntity.setTemplate puede ser null (set ad-hoc).
     */
    private void logEntityBeforeSave(
            com.fitapp.backend.workout.infrastructure.persistence.entity.WorkoutSessionEntity entity) {
        log.debug("ENTITY_BEFORE_SAVE | sessionId={} | exerciseCount={}",
                entity.getId(),
                entity.getExercises() != null ? entity.getExercises().size() : 0);

        if (entity.getExercises() == null) return;

        for (SessionExerciseEntity ex : entity.getExercises()) {
            log.debug("  Exercise={} sets={}",
                    ex.getExercise() != null ? ex.getExercise().getId() : "null",
                    ex.getSets() != null ? ex.getSets().size() : 0);

            if (ex.getSets() == null) continue;
            for (SetExecutionEntity set : ex.getSets()) {
                // FIX: setTemplate puede ser null (set añadido libremente)
                Long templateId = set.getSetTemplate() != null ? set.getSetTemplate().getId() : null;
                log.debug("    setTemplateId={} params={}",
                        templateId,
                        set.getParameters() != null ? set.getParameters().size() : 0);
            }
        }
    }
}