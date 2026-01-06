package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long>, 
                                           JpaSpecificationExecutor<ExerciseEntity> {
    
    // Consultas derivadas con índices
    Optional<ExerciseEntity> findByNameAndCreatedById(String name, Long createdById);
    
    Page<ExerciseEntity> findByCreatedById(Long createdById, Pageable pageable);
    
    Page<ExerciseEntity> findBySportId(Long sportId, Pageable pageable);
    
    Page<ExerciseEntity> findByIsPublicTrue(Pageable pageable);
    
    Page<ExerciseEntity> findByIsActiveTrue(Pageable pageable);
    
    Page<ExerciseEntity> findByExerciseType(ExerciseType exerciseType, Pageable pageable);
        
    // Consultas con joins optimizados
    @Query("SELECT DISTINCT e FROM ExerciseEntity e " +
           "LEFT JOIN FETCH e.categories " +
           "LEFT JOIN FETCH e.supportedParameters " +
           "WHERE e.id = :id")
    Optional<ExerciseEntity> findByIdWithRelations(@Param("id") Long id);
    
    @Query("SELECT e FROM ExerciseEntity e " +
           "JOIN e.categories c WHERE c.id = :categoryId")
    Page<ExerciseEntity> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e " +
           "JOIN e.supportedParameters p WHERE p.id = :parameterId")
    Page<ExerciseEntity> findByParameterId(@Param("parameterId") Long parameterId, Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e WHERE " +
           "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:exerciseType IS NULL OR e.exerciseType = :exerciseType) AND " +
           "(:sportId IS NULL OR e.sport.id = :sportId) AND " +
           "(:isActive IS NULL OR e.isActive = :isActive) AND " +
           "(:isPublic IS NULL OR e.isPublic = :isPublic) AND " +
           "(:createdById IS NULL OR e.createdBy.id = :createdById)")
    Page<ExerciseEntity> findByFilters(
            @Param("search") String search,
            @Param("exerciseType") ExerciseType exerciseType,
            @Param("sportId") Long sportId,
            @Param("isActive") Boolean isActive,
            @Param("isPublic") Boolean isPublic,
            @Param("createdById") Long createdById,
            Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = true AND " +
           "(e.isPublic = true OR e.createdBy.id = :userId)")
    Page<ExerciseEntity> findAvailableForUser(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e WHERE " +
           "e.isActive = true AND " +
           "(e.isPublic = true OR e.createdBy.id = :userId) AND " +
           "(:sportId IS NULL OR e.sport.id = :sportId)")
    Page<ExerciseEntity> findAvailableForUserAndSport(
            @Param("userId") Long userId,
            @Param("sportId") Long sportId,
            Pageable pageable);
    
    // Consultas para estadísticas
    @Query("SELECT COUNT(e) FROM ExerciseEntity e WHERE e.createdBy.id = :userId")
    Long countByUser(@Param("userId") Long userId);
    
    @Query("SELECT e FROM ExerciseEntity e WHERE e.lastUsedAt IS NOT NULL " +
           "ORDER BY e.lastUsedAt DESC")
    Page<ExerciseEntity> findRecentlyUsed(Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e ORDER BY e.usageCount DESC")
    Page<ExerciseEntity> findMostPopular(Pageable pageable);
    
    @Query("SELECT e FROM ExerciseEntity e ORDER BY e.rating DESC")
    Page<ExerciseEntity> findTopRated(Pageable pageable);
    
    // Consultas para limpieza de datos
    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = false AND e.updatedAt < :cutoffDate")
    List<ExerciseEntity> findInactiveBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Actualizaciones por lote
    @Modifying
    @Query("UPDATE ExerciseEntity e SET e.usageCount = e.usageCount + 1, " +
           "e.lastUsedAt = CURRENT_TIMESTAMP WHERE e.id = :exerciseId")
    void incrementUsageCount(@Param("exerciseId") Long exerciseId);
    
    @Modifying
    @Query("UPDATE ExerciseEntity e SET e.rating = " +
           "(e.rating * e.ratingCount + :newRating) / (e.ratingCount + 1), " +
           "e.ratingCount = e.ratingCount + 1 WHERE e.id = :exerciseId")
    void addRating(@Param("exerciseId") Long exerciseId, @Param("newRating") Double newRating);
}