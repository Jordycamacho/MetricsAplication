package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SetExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetExecutionRepository extends JpaRepository<SetExecutionEntity, Long> {
    
    /**
     * Encuentra todos los sets de un ejercicio de sesión.
     */
    @Query("SELECT se FROM SetExecutionEntity se WHERE se.sessionExercise.id = :sessionExerciseId ORDER BY se.position")
    List<SetExecutionEntity> findBySessionExerciseId(@Param("sessionExerciseId") Long sessionExerciseId);
    
    /**
     * Encuentra todos los sets de una sesión completa.
     */
    @Query("SELECT se FROM SetExecutionEntity se WHERE se.sessionExercise.session.id = :sessionId ORDER BY se.sessionExercise.id, se.position")
    List<SetExecutionEntity> findBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Cuenta sets completados en una sesión.
     */
    @Query("SELECT COUNT(se) FROM SetExecutionEntity se WHERE se.sessionExercise.session.id = :sessionId AND se.status = 'COMPLETED'")
    long countCompletedBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Suma volumen total de una sesión (peso × reps).
     */
    @Query("SELECT se FROM SetExecutionEntity se " +
           "JOIN FETCH se.parameters p " +
           "WHERE se.sessionExercise.session.id = :sessionId")
    List<SetExecutionEntity> findBySessionIdWithParameters(@Param("sessionId") Long sessionId);
}