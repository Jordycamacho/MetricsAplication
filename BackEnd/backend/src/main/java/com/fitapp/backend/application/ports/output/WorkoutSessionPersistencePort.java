package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.WorkoutSessionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutSessionPersistencePort {
    
    /**
     * Guarda una sesión de workout completa.
     */
    WorkoutSessionModel save(WorkoutSessionModel session);
    
    /**
     * Encuentra sesión por ID.
     */
    Optional<WorkoutSessionModel> findById(Long id);
    
    /**
     * Encuentra sesión por ID y usuario con detalles completos.
     */
    Optional<WorkoutSessionModel> findByIdAndUserIdWithDetails(Long id, Long userId);
    
    /**
     * Lista sesiones del usuario (paginado).
     */
    Page<WorkoutSessionModel> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Lista sesiones de una rutina específica.
     */
    Page<WorkoutSessionModel> findByRoutineIdAndUserId(Long routineId, Long userId, Pageable pageable);
    
    /**
     * Encuentra sesiones en un rango de fechas.
     */
    List<WorkoutSessionModel> findByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Cuenta sesiones del usuario.
     */
    long countByUserId(Long userId);
    
    /**
     * Cuenta sesiones de una rutina específica.
     */
    long countByRoutineIdAndUserId(Long routineId, Long userId);
    
    /**
     * Encuentra las últimas N sesiones del usuario.
     */
    List<WorkoutSessionModel> findRecentByUserId(Long userId, int limit);
    
    /**
     * Suma volumen total del usuario.
     */
    Double sumTotalVolumeByUserId(Long userId);
    
    /**
     * Suma volumen en rango de fechas.
     */
    Double sumTotalVolumeByUserIdAndDateRange(Long userId, LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Elimina una sesión.
     */
    void deleteById(Long id);
}