package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategoryEntity, Long>, 
                                                    JpaSpecificationExecutor<ExerciseCategoryEntity> {
    
    // Métodos para categorías predefinidas
    Page<ExerciseCategoryEntity> findByIsPredefinedTrue(Pageable pageable);
    List<ExerciseCategoryEntity> findByIsPredefinedTrueAndIsActiveTrue();
    
    // Métodos para categorías personales
    Page<ExerciseCategoryEntity> findByOwnerId(Long ownerId, Pageable pageable);
    Page<ExerciseCategoryEntity> findByOwnerIdAndIsActiveTrue(Long ownerId, Pageable pageable);
    
    // Métodos mixtos
    Page<ExerciseCategoryEntity> findByIsPublicTrue(Pageable pageable);
    
    // Consulta con filtros avanzados
    @Query("SELECT ec FROM ExerciseCategoryEntity ec WHERE " +
           "(:search IS NULL OR LOWER(ec.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(ec.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isPredefined IS NULL OR ec.isPredefined = :isPredefined) AND " +
           "(:isActive IS NULL OR ec.isActive = :isActive) AND " +
           "(:isPublic IS NULL OR ec.isPublic = :isPublic) AND " +
           "(:sportId IS NULL OR ec.sport.id = :sportId) AND " +
           "(:ownerId IS NULL OR ec.owner.id = :ownerId) AND " +
           "(:includePredefined = true OR ec.isPredefined = false)")
    Page<ExerciseCategoryEntity> findByFilters(
            @Param("search") String search,
            @Param("isPredefined") Boolean isPredefined,
            @Param("isActive") Boolean isActive,
            @Param("isPublic") Boolean isPublic,
            @Param("sportId") Long sportId,
            @Param("ownerId") Long ownerId,
            @Param("includePredefined") Boolean includePredefined,
            Pageable pageable);
    
    // Para validación de unicidad
    Optional<ExerciseCategoryEntity> findByNameAndOwnerId(String name, Long ownerId);
    Optional<ExerciseCategoryEntity> findByNameAndIsPredefinedTrue(String name);
    Optional<ExerciseCategoryEntity> findByName(String name);
    
    // Categorías disponibles para un usuario
    @Query("SELECT ec FROM ExerciseCategoryEntity ec WHERE " +
           "ec.isActive = true AND " +
           "(ec.isPredefined = true OR ec.owner.id = :userId OR ec.isPublic = true) AND " +
           "(:sportId IS NULL OR ec.sport.id = :sportId)")
    Page<ExerciseCategoryEntity> findAvailableForUser(
            @Param("userId") Long userId,
            @Param("sportId") Long sportId,
            Pageable pageable);
    
    // Estadísticas
    @Query("SELECT COUNT(ec) FROM ExerciseCategoryEntity ec WHERE ec.owner.id = :ownerId")
    Long countByOwnerId(@Param("ownerId") Long ownerId);
    
    // Categorías más usadas
    @Query("SELECT ec FROM ExerciseCategoryEntity ec WHERE ec.usageCount > 0 ORDER BY ec.usageCount DESC")
    Page<ExerciseCategoryEntity> findMostUsedCategories(Pageable pageable);
}