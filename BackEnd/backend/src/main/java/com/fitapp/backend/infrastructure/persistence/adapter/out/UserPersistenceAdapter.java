package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {
    
    private final SpringDataUserRepository springDataUserRepository;
    private final UserConverter userConverter;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(userConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserModel> findAll(Pageable pageable) {
        return springDataUserRepository.findAll(pageable)
                .map(userConverter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userConverter::toDomain);
    }
    
    @Override
    @Transactional
    public UserModel save(UserModel userModel) {
        UserEntity entity;
        
        if (userModel.getId() != null) {
            entity = springDataUserRepository.findById(userModel.getId())
                    .orElseThrow(() -> new UserNotFoundException(userModel.getId()));
            
            updateEntityFromModel(entity, userModel);
        } else {
            entity = userConverter.toEntity(userModel);
        }
        
        UserEntity savedEntity = springDataUserRepository.save(entity);
        return userConverter.toDomain(savedEntity);
    }
    
    private void updateEntityFromModel(UserEntity entity, UserModel model) {
        entity.setEmail(model.getEmail());
        entity.setFullName(model.getFullName());
        entity.setProfileImage(model.getProfileImage());
        entity.setPassword(model.getPassword());
        entity.setRole(model.getRole());
        entity.setLastLogin(model.getLastLogin());
        entity.setActive(model.isActive());
        entity.setMaxRoutines(model.getMaxRoutines());
        entity.setUpdatedAt(model.getUpdatedAt());
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }
}