package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SessionExerciseEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionExerciseRepository extends JpaRepository<SessionExerciseEntity, Long> {

    /**
     * Encuentra todos los ejercicios de una sesión.
     */
    @Query("SELECT se FROM SessionExerciseEntity se WHERE se.session.id = :sessionId ORDER BY se.id")
    List<SessionExerciseEntity> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Cuenta ejercicios completados en una sesión.
     */
    @Query("SELECT COUNT(se) FROM SessionExerciseEntity se WHERE se.session.id = :sessionId AND se.status = 'COMPLETED'")
    long countCompletedBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Cuenta ejercicios saltados en una sesión.
     */
    @Query("SELECT COUNT(se) FROM SessionExerciseEntity se WHERE se.session.id = :sessionId AND se.status = 'SKIPPED'")
    long countSkippedBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Obtiene la última ejecución de un ejercicio específico para un usuario.
     * Retorna el SessionExercise más reciente con todos sus sets y parámetros.
     */
    @EntityGraph(attributePaths = { "exercise", "sets", "sets.parameters", "sets.parameters.parameter", "session" })
    @Query("""
                SELECT se FROM SessionExerciseEntity se WHERE se.session.userId = :userId
                AND se.exercise.id = :exerciseId
                AND se.status = 'COMPLETED'
                ORDER BY se.session.startTime DESC LIMIT 1
            """)
    Optional<SessionExerciseEntity> findLastCompletedByUserIdAndExerciseId(@Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId);

    /**
     * Obtiene las últimas ejecuciones de múltiples ejercicios.
     * Útil para pre-cargar todos los valores de una rutina de una vez.
     */
    @EntityGraph(attributePaths = { "exercise", "sets", "sets.parameters", "sets.parameters.parameter", "session" })
    @Query("""
                SELECT se FROM SessionExerciseEntity se
                WHERE se.id IN (
                    SELECT MAX(se2.id)
                    FROM SessionExerciseEntity se2
                    WHERE se2.session.userId = :userId
                    AND se2.exercise.id IN :exerciseIds
                    AND se2.status = 'COMPLETED'
                    GROUP BY se2.exercise.id
                )
                ORDER BY se.session.startTime DESC
            """)
    List<SessionExerciseEntity> findLastCompletedByUserIdAndExerciseIds(
            @Param("userId") Long userId,
            @Param("exerciseIds") List<Long> exerciseIds);
}