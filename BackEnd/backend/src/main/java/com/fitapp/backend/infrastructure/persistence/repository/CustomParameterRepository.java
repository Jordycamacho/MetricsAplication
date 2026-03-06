package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomParameterRepository extends JpaRepository<CustomParameterEntity, Long>, JpaSpecificationExecutor<CustomParameterEntity> {

        long countByOwnerId(Long ownerId);

        @EntityGraph(attributePaths = "owner")
        Optional<CustomParameterEntity> findById(Long id);

        @EntityGraph(attributePaths = "owner")
        Optional<CustomParameterEntity> findByNameAndOwnerId(String name, Long ownerId);

        @EntityGraph(attributePaths = "owner")
        Page<CustomParameterEntity> findByOwnerId(Long ownerId, Pageable pageable);

        @EntityGraph(attributePaths = "owner")
        Page<CustomParameterEntity> findByIsGlobalTrue(Pageable pageable);

        @EntityGraph(attributePaths = "owner")
        Page<CustomParameterEntity> findByParameterType(ParameterType parameterType, Pageable pageable);

        @EntityGraph(attributePaths = "owner")
        Page<CustomParameterEntity> findByIsActiveTrue(Pageable pageable);

        @Query("SELECT cp FROM CustomParameterEntity cp " +
                        "LEFT JOIN FETCH cp.owner " +
                        "WHERE (:search IS NULL OR LOWER(cp.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(cp.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:parameterType IS NULL OR cp.parameterType = :parameterType) AND " +
                        "(:isGlobal IS NULL OR cp.isGlobal = :isGlobal) AND " +
                        "(:isActive IS NULL OR cp.isActive = :isActive) AND " +
                        "(:ownerId IS NULL OR cp.owner.id = :ownerId) AND " +
                        "(:isFavorite IS NULL OR cp.isFavorite = :isFavorite)")
        Page<CustomParameterEntity> findByFilters(
                        @Param("search") String search,
                        @Param("parameterType") ParameterType parameterType,
                        @Param("isGlobal") Boolean isGlobal,
                        @Param("isActive") Boolean isActive,
                        @Param("ownerId") Long ownerId,
                        @Param("isFavorite") Boolean isFavorite,
                        Pageable pageable);

        @Query("SELECT DISTINCT cp.parameterType FROM CustomParameterEntity cp")
        List<ParameterType> findAllDistinctParameterTypes();

        @Modifying
        @Query("UPDATE CustomParameterEntity cp SET cp.usageCount = cp.usageCount + 1 WHERE cp.id = :parameterId")
        void incrementUsageCount(@Param("parameterId") Long parameterId);

        @Query("SELECT cp FROM CustomParameterEntity cp LEFT JOIN FETCH cp.owner WHERE cp.isActive = true AND (cp.isGlobal = true OR cp.owner.id = :userId)")
        Page<CustomParameterEntity> findAvailableForUser(@Param("userId") Long userId, Pageable pageable);
}