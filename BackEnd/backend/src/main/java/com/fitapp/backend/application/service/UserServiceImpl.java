package com.fitapp.backend.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.adapter.out.supabase.SupabaseAuthClient;
import com.fitapp.backend.infrastructure.persistence.converter.UserConverter;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import jakarta.persistence.EntityNotFoundException;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserUseCase {

    private final UserPersistencePort persistencePort;
    private final PasswordEncoder passwordEncoder;
    private final SpringDataUserRepository jpaRepository;
    private final SupabaseAuthClient supabaseAuthClient;

    @Override
    @Transactional
    public UserModel createUser(UserCreationRequest request) {
        validateUserRequest(request);
        supabaseAuthClient.verifySupabaseUid(request.getSupabaseUid());

        UserModel user = UserModel.builder()
                .email(request.getEmail().toLowerCase().trim())
                .role(request.getRole() != null ? request.getRole() : Role.STANDARD)
                .isActive(request.isActive())
                .maxRoutines(request.getMaxRoutines())
                .supabaseUid(request.getSupabaseUid())
                .build();

        return persistencePort.save(user);
    }


    /**
     * Actualiza los datos básicos de un usuario
     * 
     * @param id            Identificador del usuario
     * @param updateRequest Datos a actualizar
     * @return Usuario actualizado
     * @throws UserNotFoundException Si el usuario no existe
     */

    @Override
    @Transactional
    public UserModel updateUser(UUID id, UserUpdateRequest updateRequest) {
        UserEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (updateRequest.getRole() != null) {
            entity.setRole(updateRequest.getRole());
        }
        if (updateRequest.getIsActive() != null) {
            entity.setActive(updateRequest.getIsActive());
        }
        if (updateRequest.getMaxRoutines() != null) {
            entity.setMaxRoutines(updateRequest.getMaxRoutines());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        return UserConverter.toDomain(entity);
    }

    @Override
    public void updateLastLogin(UUID userId) {
        UserModel user = persistencePort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.updateLastLogin();
        persistencePort.save(user);
    }

    @Override
    public void toggleUserStatus(UUID userId, boolean isActive) {
        UserModel user = persistencePort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (isActive) {
            user.activate();
        } else {
            user.deactivate();
        }

        persistencePort.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        persistencePort.deleteById(id);
    }

    @Override
    public UserModel getUserById(UUID id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<UserModel> findAll(Pageable pageable) {
        return persistencePort.findAll(pageable);
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        UserEntity userEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userEntity.setActive(true);
        userEntity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        UserEntity userEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userEntity.setActive(false);
        userEntity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        return persistencePort.findByEmail(email.toLowerCase().trim());
    }

    @Override
    public Optional<UserModel> findBySupabaseUid(String supabaseUid) {
        if (supabaseUid == null || supabaseUid.isBlank()) {
            throw new IllegalArgumentException("Supabase UID no puede estar vacío");
        }
        return persistencePort.findBySupabaseUid(supabaseUid);
    }

    private void validateUserRequest(UserCreationRequest request) {
        if (request.getSupabaseUid() == null || request.getSupabaseUid().isBlank()) {
            throw new IllegalArgumentException("Supabase UID is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

}
