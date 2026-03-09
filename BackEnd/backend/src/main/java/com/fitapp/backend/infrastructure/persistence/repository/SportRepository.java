package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<SportEntity, Long>, JpaSpecificationExecutor<SportEntity> {

        @Query("SELECT COUNT(s) FROM SportEntity s WHERE s.createdBy.id = :createdById")
        long countByCreatedById(Long createdById);

        @Query("SELECT s.id FROM SportEntity s WHERE s.name = :name")
        Optional<Long> findIdByName(@Param("name") String name);

        @Query("SELECT s FROM SportEntity s WHERE " +
                        "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:isPredefined IS NULL OR s.isPredefined = :isPredefined) AND " +
                        "(:sourceType IS NULL OR s.sourceType = :sourceType)")
        Page<SportEntity> findByFilters(
                        @Param("search") String search,
                        @Param("isPredefined") Boolean isPredefined,
                        @Param("sourceType") SportSourceType sourceType,
                        Pageable pageable);

        @Query("SELECT s FROM SportEntity s WHERE " +
                        "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<SportEntity> searchSports(
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT s FROM SportEntity s WHERE " +
                        "s.isPredefined = true AND " +
                        "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<SportEntity> findPredefinedWithSearch(
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT s FROM SportEntity s WHERE " +
                        "s.createdBy.id = :userId AND " +
                        "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<SportEntity> findByUserWithSearch(
                        @Param("userId") Long userId,
                        @Param("search") String search,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "createdBy", "createdBy.subscription" })
        List<SportEntity> findByIsPredefinedTrue();

        @EntityGraph(attributePaths = { "createdBy", "createdBy.subscription" })
        List<SportEntity> findByCreatedById(Long userId);

        @EntityGraph(attributePaths = { "createdBy", "createdBy.subscription" })
        Optional<SportEntity> findByName(String name);

        @EntityGraph(attributePaths = { "createdBy", "createdBy.subscription" })
        @Override
        Page<SportEntity> findAll(Pageable pageable);

        @EntityGraph(attributePaths = { "createdBy", "createdBy.subscription" })
        Optional<SportEntity> findById(Long id);
}