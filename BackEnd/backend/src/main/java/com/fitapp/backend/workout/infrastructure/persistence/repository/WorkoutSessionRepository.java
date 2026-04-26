package com.fitapp.backend.workout.infrastructure.persistence.repository;

import com.fitapp.backend.workout.infrastructure.persistence.entity.WorkoutSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository
        extends JpaRepository<WorkoutSessionEntity, Long>,
                JpaSpecificationExecutor<WorkoutSessionEntity> {

    // ── Fetch with full details ───────────────────────────────────────────────

    /**
     * Carga eager de todos los niveles para el detalle completo de sesión.
     */
    @EntityGraph(attributePaths = {
            "routine",
            "exercises", "exercises.exercise",
            "exercises.sets", "exercises.sets.parameters",
            "exercises.sets.parameters.parameter"
    })
    @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.id = :id")
    Optional<WorkoutSessionEntity> findByIdWithDetails(@Param("id") Long id);

    /**
     * Detalle completo filtrado por usuario.
     * userId es columna directa en workout_sessions, no navegación por routine.
     */
    @EntityGraph(attributePaths = {
            "routine",
            "exercises", "exercises.exercise",
            "exercises.sets", "exercises.sets.parameters",
            "exercises.sets.parameters.parameter"
    })
    @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.id = :id AND ws.userId = :userId")
    Optional<WorkoutSessionEntity> findByIdAndUserIdWithDetails(
            @Param("id") Long id,
            @Param("userId") Long userId);

    // ── Paged lists ───────────────────────────────────────────────────────────

    /** Lista paginada sin eager loading (para summaries). userId columna directa. */
    Page<WorkoutSessionEntity> findByUserId(Long userId, Pageable pageable);

    /** Lista paginada filtrada por rutina y usuario. */
    @Query("""
            SELECT ws FROM WorkoutSessionEntity ws
            WHERE ws.routine.id = :routineId AND ws.userId = :userId
            """)
    Page<WorkoutSessionEntity> findByRoutineIdAndUserId(
            @Param("routineId") Long routineId,
            @Param("userId") Long userId,
            Pageable pageable);

    // ── Date range ────────────────────────────────────────────────────────────

    @Query("""
            SELECT ws FROM WorkoutSessionEntity ws
            WHERE ws.userId = :userId
              AND ws.startTime >= :fromDate
              AND ws.startTime <= :toDate
            ORDER BY ws.startTime DESC
            """)
    List<WorkoutSessionEntity> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    // ── Counts ────────────────────────────────────────────────────────────────

    long countByUserId(Long userId);

    @Query("""
            SELECT COUNT(ws) FROM WorkoutSessionEntity ws
            WHERE ws.routine.id = :routineId AND ws.userId = :userId
            """)
    long countByRoutineIdAndUserId(
            @Param("routineId") Long routineId,
            @Param("userId") Long userId);

    // ── Recent ────────────────────────────────────────────────────────────────

    @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.userId = :userId ORDER BY ws.startTime DESC")
    List<WorkoutSessionEntity> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    default List<WorkoutSessionEntity> findRecentByUserId(Long userId, int limit) {
        return findRecentByUserId(userId, PageRequest.of(0, limit));
    }

    // ── Aggregates ────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(ws.totalVolume), 0) FROM WorkoutSessionEntity ws WHERE ws.userId = :userId")
    Double sumTotalVolumeByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(ws.totalVolume), 0) FROM WorkoutSessionEntity ws
            WHERE ws.userId = :userId
              AND ws.startTime >= :fromDate
              AND ws.startTime <= :toDate
            """)
    Double sumTotalVolumeByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}