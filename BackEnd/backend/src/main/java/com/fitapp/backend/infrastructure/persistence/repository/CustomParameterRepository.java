package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomParameterRepository extends JpaRepository<CustomParameterEntity, Long>, 
                                                   JpaSpecificationExecutor<CustomParameterEntity> {
    
    // Métodos derivados
    Optional<CustomParameterEntity> findByNameAndOwnerIdAndSportId(String name, Long ownerId, Long sportId);
    
    Page<CustomParameterEntity> findByOwnerId(Long ownerId, Pageable pageable);
    
    Page<CustomParameterEntity> findBySportId(Long sportId, Pageable pageable);
    
    Page<CustomParameterEntity> findByIsGlobalTrue(Pageable pageable);
    
    Page<CustomParameterEntity> findByOwnerIdAndSportId(Long ownerId, Long sportId, Pageable pageable);
    
    Page<CustomParameterEntity> findByParameterType(ParameterType parameterType, Pageable pageable);
    
    Page<CustomParameterEntity> findByCategory(String category, Pageable pageable);
    
    Page<CustomParameterEntity> findByIsActiveTrue(Pageable pageable);
    
    // Consultas personalizadas
    @Query("SELECT cp FROM CustomParameterEntity cp WHERE " +
           "(:search IS NULL OR LOWER(cp.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cp.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:parameterType IS NULL OR cp.parameterType = :parameterType) AND " +
           "(:isGlobal IS NULL OR cp.isGlobal = :isGlobal) AND " +
           "(:isActive IS NULL OR cp.isActive = :isActive) AND " +
           "(:sportId IS NULL OR cp.sport.id = :sportId) AND " +
           "(:ownerId IS NULL OR cp.owner.id = :ownerId) AND " +
           "(:category IS NULL OR cp.category = :category)")
    Page<CustomParameterEntity> findByFilters(
            @Param("search") String search,
            @Param("parameterType") ParameterType parameterType,
            @Param("isGlobal") Boolean isGlobal,
            @Param("isActive") Boolean isActive,
            @Param("sportId") Long sportId,
            @Param("ownerId") Long ownerId,
            @Param("category") String category,
            Pageable pageable);
    
    @Query("SELECT DISTINCT cp.category FROM CustomParameterEntity cp WHERE cp.category IS NOT NULL")
    List<String> findAllDistinctCategories();
    
    @Query("SELECT DISTINCT cp.parameterType FROM CustomParameterEntity cp")
    List<ParameterType> findAllDistinctParameterTypes();
    
    @Modifying
    @Query("UPDATE CustomParameterEntity cp SET cp.usageCount = cp.usageCount + 1 WHERE cp.id = :parameterId")
    void incrementUsageCount(@Param("parameterId") Long parameterId);
    
    @Query("SELECT cp FROM CustomParameterEntity cp WHERE cp.isActive = true AND " +
           "(cp.isGlobal = true OR cp.owner.id = :userId) AND " +
           "(:sportId IS NULL OR cp.sport.id = :sportId)")
    Page<CustomParameterEntity> findAvailableForUser(
            @Param("userId") Long userId,
            @Param("sportId") Long sportId,
            Pageable pageable);
}