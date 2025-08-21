package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"subscription"})
    Optional<UserEntity> findWithSubscriptionById(Long id);
    
    @EntityGraph(attributePaths = {"subscription"})
    Optional<UserEntity> findWithSubscriptionByEmail(String email);
}