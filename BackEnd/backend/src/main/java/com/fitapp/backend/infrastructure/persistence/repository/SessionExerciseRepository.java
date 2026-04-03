package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SessionExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}