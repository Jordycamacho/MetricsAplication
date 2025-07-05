package com.fitapp.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return UserConverter.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UserModel> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserConverter::toDomain);
    }

    @Override
    public List<UserModel> findAll() {
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public void deleteById(UUID id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByEmail'");
    }

}
