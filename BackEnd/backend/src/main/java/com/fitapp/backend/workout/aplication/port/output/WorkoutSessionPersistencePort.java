package com.fitapp.backend.workout.aplication.port.output;

import com.fitapp.backend.workout.domain.model.WorkoutSessionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutSessionPersistencePort {

    WorkoutSessionModel save(WorkoutSessionModel session);

    Optional<WorkoutSessionModel> findById(Long id);

    Optional<WorkoutSessionModel> findByIdAndUserIdWithDetails(Long id, Long userId);

    Page<WorkoutSessionModel> findByUserId(Long userId, Pageable pageable);

    Page<WorkoutSessionModel> findByRoutineIdAndUserId(Long routineId, Long userId, Pageable pageable);

    List<WorkoutSessionModel> findByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);

    long countByUserId(Long userId);

    long countByRoutineIdAndUserId(Long routineId, Long userId);

    List<WorkoutSessionModel> findRecentByUserId(Long userId, int limit);

    Double sumTotalVolumeByUserId(Long userId);

    Double sumTotalVolumeByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);

    void deleteById(Long id);
}