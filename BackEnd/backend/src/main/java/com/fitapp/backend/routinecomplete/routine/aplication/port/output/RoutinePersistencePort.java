package com.fitapp.backend.routinecomplete.routine.aplication.port.output;

import com.fitapp.backend.routinecomplete.routine.aplication.dto.request.RoutineFilterRequest;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

public interface RoutinePersistencePort {
    RoutineModel save(RoutineModel routine);

    // Metodos de consulta
    Optional<RoutineModel> findByIdAndUserId(Long id, Long userId);

    Page<RoutineModel> findByUserId(Long userId, Pageable pageable);

    Page<RoutineModel> findByUserIdAndFilters(Long userId, RoutineFilterRequest filters, Pageable pageable);

    List<RoutineModel> findRecentByUserId(Long userId, int limit);

    List<RoutineModel> findActiveRoutinesByUserId(Long userId);

    List<RoutineModel> findLastUsedByUserId(Long userId, int limit);

    Optional<RoutineModel> findFullRoutineByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    Optional<RoutineModel> findByExportKey(UUID exportKey);

    Optional<RoutineModel> findFullByExportKey(UUID exportKey);

    void incrementPurchaseCount(Long routineId);

    // Métodos de actualización
    RoutineModel update(RoutineModel routine);

    void deleteByIdAndUserId(Long id, Long userId);

    void toggleActiveStatus(Long id, Long userId, boolean isActive);

    void updateLastUsedAt(Long id, Long userId, LocalDateTime lastUsedAt);
}