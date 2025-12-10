package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<RoutineEntity, Long> {

    long countByUserId(Long userId);

    Optional<RoutineEntity> findByIdAndUserId(Long id, Long userId);

    Page<RoutineEntity> findByUserId(Long userId, Pageable pageable);

    List<RoutineEntity> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Page<RoutineEntity> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    Page<RoutineEntity> findByUserIdAndSportId(Long userId, Long sportId, Pageable pageable);

    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<RoutineEntity> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    default List<RoutineEntity> findRecentByUserId(Long userId, int limit) {
        return findRecentByUserId(userId, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    // Para búsqueda por nombre
    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<RoutineEntity> findByUserIdAndNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name,
            Pageable pageable);

    // Para búsqueda combinada
    @Query("SELECT r FROM RoutineEntity r WHERE " +
            "r.user.id = :userId AND " +
            "(:sportId IS NULL OR r.sport.id = :sportId) AND " +
            "(:isActive IS NULL OR r.isActive = :isActive) AND " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<RoutineEntity> findByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("sportId") Long sportId,
            @Param("isActive") Boolean isActive,
            @Param("name") String name,
            Pageable pageable);

    // Actualizar estado activo
    @Modifying
    @Query("UPDATE RoutineEntity r SET r.isActive = :isActive WHERE r.id = :id AND r.user.id = :userId")
    int updateActiveStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("isActive") Boolean isActive);

    // Obtener rutinas creadas en un rango de fechas
    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId AND r.createdAt BETWEEN :startDate AND :endDate")
    List<RoutineEntity> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}