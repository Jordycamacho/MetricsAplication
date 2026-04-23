package com.fitapp.backend.user.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.user.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"subscription"})
    Optional<UserEntity> findByEmail(String email);

    @EntityGraph(attributePaths = {"subscription"})
    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByEmailVerificationToken(String token);

    Optional<UserEntity> findByGoogleId(String googleId);
}