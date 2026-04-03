package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.ports.output.WorkoutSessionPersistencePort;
import com.fitapp.backend.domain.model.WorkoutSessionModel;
import com.fitapp.backend.infrastructure.persistence.converter.WorkoutConverter;
import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
import com.fitapp.backend.infrastructure.persistence.repository.WorkoutSessionRepository;
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

        WorkoutSessionEntity entity = workoutConverter.toEntity(session);
        WorkoutSessionEntity saved = workoutSessionRepository.save(entity);

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
                .map(entity -> {
                    log.debug("WORKOUT_PERSISTENCE_FOUND | sessionId={}", id);
                    return workoutConverter.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkoutSessionModel> findByIdAndUserIdWithDetails(Long id, Long userId) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_ID_AND_USER | sessionId={} | userId={}", id, userId);

        return workoutSessionRepository.findByIdAndUserIdWithDetails(id, userId)
                .map(entity -> {
                    log.debug("WORKOUT_PERSISTENCE_FOUND_WITH_DETAILS | sessionId={} | exerciseCount={}", 
                              id, entity.getExercises() != null ? entity.getExercises().size() : 0);
                    return workoutConverter.toDomain(entity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutSessionModel> findByUserId(Long userId, Pageable pageable) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_USER | userId={} | page={} | size={}", 
                  userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<WorkoutSessionEntity> page = workoutSessionRepository.findByRoutine_User_Id(userId, pageable);

        log.debug("WORKOUT_PERSISTENCE_FOUND_PAGE | userId={} | totalElements={}", 
                  userId, page.getTotalElements());

        return page.map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutSessionModel> findByRoutineIdAndUserId(Long routineId, Long userId, Pageable pageable) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_ROUTINE | routineId={} | userId={} | page={}", 
                  routineId, userId, pageable.getPageNumber());

        Page<WorkoutSessionEntity> page = workoutSessionRepository.findByRoutineIdAndUserId(routineId, userId, pageable);

        log.debug("WORKOUT_PERSISTENCE_FOUND_BY_ROUTINE | routineId={} | totalElements={}", 
                  routineId, page.getTotalElements());

        return page.map(workoutConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionModel> findByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("WORKOUT_PERSISTENCE_FIND_BY_DATE_RANGE | userId={} | from={} | to={}", 
                  userId, fromDate, toDate);

        List<WorkoutSessionEntity> sessions = workoutSessionRepository.findByUserIdAndDateRange(userId, fromDate, toDate);

        log.debug("WORKOUT_PERSISTENCE_FOUND_IN_RANGE | userId={} | count={}", 
                  userId, sessions.size());

        return sessions.stream()
                .map(workoutConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        log.debug("WORKOUT_PERSISTENCE_COUNT_BY_USER | userId={}", userId);

        long count = workoutSessionRepository.countByRoutine_User_Id(userId);

        log.debug("WORKOUT_PERSISTENCE_COUNT_RESULT | userId={} | count={}", userId, count);

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRoutineIdAndUserId(Long routineId, Long userId) {
        log.debug("WORKOUT_PERSISTENCE_COUNT_BY_ROUTINE | routineId={} | userId={}", routineId, userId);

        long count = workoutSessionRepository.countByRoutineIdAndUserId(routineId, userId);

        log.debug("WORKOUT_PERSISTENCE_COUNT_BY_ROUTINE_RESULT | routineId={} | count={}", routineId, count);

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionModel> findRecentByUserId(Long userId, int limit) {
        log.debug("WORKOUT_PERSISTENCE_FIND_RECENT | userId={} | limit={}", userId, limit);

        List<WorkoutSessionEntity> sessions = workoutSessionRepository.findRecentByUserId(userId, limit);

        log.debug("WORKOUT_PERSISTENCE_FOUND_RECENT | userId={} | count={}", userId, sessions.size());

        return sessions.stream()
                .map(workoutConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double sumTotalVolumeByUserId(Long userId) {
        log.debug("WORKOUT_PERSISTENCE_SUM_VOLUME | userId={}", userId);

        Double volume = workoutSessionRepository.sumTotalVolumeByUserId(userId);

        log.debug("WORKOUT_PERSISTENCE_SUM_VOLUME_RESULT | userId={} | totalVolume={}", userId, volume);

        return volume;
    }

    @Override
    @Transactional(readOnly = true)
    public Double sumTotalVolumeByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("WORKOUT_PERSISTENCE_SUM_VOLUME_RANGE | userId={} | from={} | to={}", 
                  userId, fromDate, toDate);

        Double volume = workoutSessionRepository.sumTotalVolumeByUserIdAndDateRange(userId, fromDate, toDate);

        log.debug("WORKOUT_PERSISTENCE_SUM_VOLUME_RANGE_RESULT | userId={} | volume={}", userId, volume);

        return volume;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("WORKOUT_PERSISTENCE_DELETE | sessionId={}", id);

        workoutSessionRepository.deleteById(id);

        log.info("WORKOUT_PERSISTENCE_DELETE_SUCCESS | sessionId={}", id);
    }
}