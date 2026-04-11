package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
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
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, Long>,
              JpaSpecificationExecutor<WorkoutSessionEntity> {

       /**
        * Encuentra sesión con carga eager de ejercicios y sets.
        */
       @EntityGraph(attributePaths = { "exercises", "exercises.sets", "exercises.sets.parameters" })
       @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.id = :id")
       Optional<WorkoutSessionEntity> findByIdWithDetails(@Param("id") Long id);

       Page<WorkoutSessionEntity> findByUserId(Long userId, Pageable pageable);

       long countByUserId(Long userId);

       /**
        * Encuentra sesión por ID y usuario (sin eager loading).
        */
       Optional<WorkoutSessionEntity> findByIdAndRoutine_User_Id(Long id, Long userId);

       /**
        * Encuentra sesión con detalles completos por ID y usuario.
        */
       @EntityGraph(attributePaths = { "routine", "exercises", "exercises.exercise", "exercises.sets",
                     "exercises.sets.parameters", "exercises.sets.parameters.parameter" })
       @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.id = :id AND ws.userId = :userId")
       Optional<WorkoutSessionEntity> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

       /**
        * Lista sesiones del usuario (sin detalles de ejercicios).
        */
       Page<WorkoutSessionEntity> findByRoutine_User_Id(Long userId, Pageable pageable);

       /**
        * Lista sesiones de una rutina específica.
        */
       @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.routine.id = :routineId AND ws.routine.user.id = :userId")
       Page<WorkoutSessionEntity> findByRoutineIdAndUserId(@Param("routineId") Long routineId,
                     @Param("userId") Long userId,
                     Pageable pageable);

       /**
        * Encuentra sesiones en un rango de fechas.
        */
       @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.routine.user.id = :userId " +
                     "AND ws.startTime >= :fromDate AND ws.endTime <= :toDate")
       List<WorkoutSessionEntity> findByUserIdAndDateRange(@Param("userId") Long userId,
                     @Param("fromDate") LocalDateTime fromDate,
                     @Param("toDate") LocalDateTime toDate);

       /**
        * Cuenta sesiones del usuario.
        */
       long countByRoutine_User_Id(Long userId);

       /**
        * Cuenta sesiones de una rutina específica.
        */
       @Query("SELECT COUNT(ws) FROM WorkoutSessionEntity ws WHERE ws.routine.id = :routineId AND ws.routine.user.id = :userId")
       long countByRoutineIdAndUserId(@Param("routineId") Long routineId, @Param("userId") Long userId);

       /**
        * Encuentra las últimas N sesiones del usuario.
        */
       @Query("SELECT ws FROM WorkoutSessionEntity ws WHERE ws.userId = :userId ORDER BY ws.startTime DESC")
       List<WorkoutSessionEntity> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

       default List<WorkoutSessionEntity> findRecentByUserId(Long userId, int limit) {
              return findRecentByUserId(userId, PageRequest.of(0, limit));
       }

       /**
        * Estadísticas: suma de volumen total del usuario.
        */
       @Query("SELECT COALESCE(SUM(ws.totalVolume), 0) FROM WorkoutSessionEntity ws WHERE ws.userId = :userId")
       Double sumTotalVolumeByUserId(@Param("userId") Long userId);

       /**
        * Estadísticas: suma de volumen en rango de fechas.
        */
       @Query("SELECT COALESCE(SUM(ws.totalVolume), 0) FROM WorkoutSessionEntity ws " +
                     "WHERE ws.routine.user.id = :userId AND ws.startTime >= :fromDate AND ws.endTime <= :toDate")
       Double sumTotalVolumeByUserIdAndDateRange(@Param("userId") Long userId,
                     @Param("fromDate") LocalDateTime fromDate,
                     @Param("toDate") LocalDateTime toDate);

}