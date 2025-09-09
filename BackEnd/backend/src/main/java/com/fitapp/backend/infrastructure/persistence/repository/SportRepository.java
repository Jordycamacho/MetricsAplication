package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<SportEntity, Long> {
    
    @EntityGraph(attributePaths = {"createdBy", "createdBy.subscription"})
    List<SportEntity> findByIsPredefinedTrue();
    
    @EntityGraph(attributePaths = {"createdBy", "createdBy.subscription"})
    List<SportEntity> findByCreatedById(Long userId);
    
    @EntityGraph(attributePaths = {"createdBy", "createdBy.subscription"})
    @Override
    List<SportEntity> findAll();
}