package com.fitapp.backend.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

@Repository
public class UserRepository implements UserPersistencePort {

    private final SpringDataUserRepository jpaRepository;

    public UserRepository(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserModel save(UserModel user) {
        UserEntity entity = UserConverter.toEntity(user);
        entity.updateTimestamps();
        UserEntity savedEntity = jpaRepository.save(entity);
        return UserConverter.toDomain(jpaRepository.save(savedEntity));
    }

    @Override
    public Optional<UserModel> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserConverter::toDomain);
    }

    @Override
    public Page<UserModel> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(UserConverter::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserConverter::toDomain);
    }

    @Override
    public Optional<UserModel> findBySupabaseUid(String supabaseUid) {
        return jpaRepository.findBySupabaseUid(supabaseUid)
                .map(UserConverter::toDomain);
    }
}
