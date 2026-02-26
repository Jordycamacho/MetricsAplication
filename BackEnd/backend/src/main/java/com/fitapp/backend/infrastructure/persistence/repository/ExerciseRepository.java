package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
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

    Optional<ExerciseEntity> findByNameAndCreatedById(String name, Long createdById);

    @Query("SELECT DISTINCT e FROM ExerciseEntity e " +
           "LEFT JOIN FETCH e.sports " +
           "LEFT JOIN FETCH e.categories " +
           "LEFT JOIN FETCH e.supportedParameters " +
           "WHERE e.id = :id")
    Optional<ExerciseEntity> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT DISTINCT e FROM ExerciseEntity e " +
           "JOIN e.sports s WHERE s.id = :sportId AND e.isActive = true AND " +
           "(e.isPublic = true OR e.createdBy.id = :userId)")
    Page<ExerciseEntity> findAvailableForUserAndSport(
            @Param("userId") Long userId,
            @Param("sportId") Long sportId,
            Pageable pageable);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = true AND " +
           "(e.isPublic = true OR e.createdBy.id = :userId)")
    Page<ExerciseEntity> findAvailableForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.createdBy.id = :userId " +
           "AND e.lastUsedAt IS NOT NULL ORDER BY e.lastUsedAt DESC")
    Page<ExerciseEntity> findRecentlyUsedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = true AND e.isPublic = true " +
           "ORDER BY e.usageCount DESC")
    Page<ExerciseEntity> findMostPopular(Pageable pageable);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = true AND e.isPublic = true " +
           "AND e.ratingCount >= 5 ORDER BY e.rating DESC")
    Page<ExerciseEntity> findTopRated(Pageable pageable);

    @Query("SELECT COUNT(e) FROM ExerciseEntity e WHERE e.createdBy.id = :userId AND e.isActive = true")
    Long countByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM ExerciseRatingEntity r WHERE r.exercise.id = :exerciseId AND r.user.id = :userId")
    Long countUserRating(@Param("exerciseId") Long exerciseId, @Param("userId") Long userId);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.isActive = false AND e.updatedAt < :cutoffDate")
    List<ExerciseEntity> findInactiveBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE ExerciseEntity e SET e.usageCount = e.usageCount + 1, " +
           "e.lastUsedAt = CURRENT_TIMESTAMP WHERE e.id = :exerciseId")
    void incrementUsageCount(@Param("exerciseId") Long exerciseId);

    @Modifying
    @Query("UPDATE ExerciseEntity e SET " +
           "e.rating = (e.rating * e.ratingCount + :newRating) / (e.ratingCount + 1), " +
           "e.ratingCount = e.ratingCount + 1 " +
           "WHERE e.id = :exerciseId")
    void updateRating(@Param("exerciseId") Long exerciseId, @Param("newRating") Double newRating);

    Page<ExerciseEntity> findByCreatedById(Long createdById, Pageable pageable);

    boolean existsByNameAndCreatedByIdAndIdNot(String name, Long createdById, Long excludeId);
}