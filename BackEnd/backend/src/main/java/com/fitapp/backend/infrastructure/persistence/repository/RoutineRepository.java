package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface RoutineRepository extends JpaRepository<RoutineEntity, Long>, JpaSpecificationExecutor<RoutineEntity> {

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"sport", "exercises", "exercises.exercise"})
    Optional<RoutineEntity> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"sport", "exercises", "exercises.exercise"})
    Page<RoutineEntity> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"sport", "exercises", "exercises.exercise"})
    @Override
    Page<RoutineEntity> findAll(Specification<RoutineEntity> spec, Pageable pageable);
    
    List<RoutineEntity> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Page<RoutineEntity> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);

    Page<RoutineEntity> findByUserIdAndSportId(Long userId, Long sportId, Pageable pageable);

    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<RoutineEntity> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    default List<RoutineEntity> findRecentByUserId(Long userId, int limit) {
        return findRecentByUserId(userId, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<RoutineEntity> findByUserIdAndNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name,
            Pageable pageable);

    @Modifying
    @Query("UPDATE RoutineEntity r SET r.isActive = :isActive WHERE r.id = :id AND r.user.id = :userId")
    int updateActiveStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("isActive") Boolean isActive);

    @Query("SELECT r FROM RoutineEntity r WHERE r.user.id = :userId AND r.createdAt BETWEEN :startDate AND :endDate")
    List<RoutineEntity> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}