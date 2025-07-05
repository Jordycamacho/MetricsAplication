package com.fitapp.backend.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserUseCase {

    private final UserPersistencePort persistencePort;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserPersistencePort persistencePort, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.persistencePort = persistencePort;
    }

    @Override
    public UserModel createUser(UserCreationRequest request) {
        validateUserRequest(request);

        UserModel user = UserModel.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.FREE)
                .isActive(request.isActive())
                .maxRoutines(request.getMaxRoutines())
                .supabaseUid(request.getSupabaseUid())
                .build();

        return persistencePort.save(user);
    }

    @Override
    public void updatePassword(UUID userId, String newPassword) {
        UserModel user = persistencePort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        persistencePort.save(user);
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
    public UserModel updateUser(UUID id, UserUpdateRequest updateRequest) {
        UserModel existing = persistencePort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (updateRequest.getRole() != null) {
            existing.setRole(updateRequest.getRole());
        }
        if (updateRequest.getIsActive() != null) {
            existing.setActive(updateRequest.getIsActive());
        }
        if (updateRequest.getMaxRoutines() != null) {
            existing.setMaxRoutines(updateRequest.getMaxRoutines());
        }

        return persistencePort.save(existing);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public UserModel getUserById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
    }

    @Override
    public List<UserModel> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    @Override
    public void activateUser(UUID id) {
        UserModel user = persistencePort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(true);
        persistencePort.save(user);
    }

    @Override
    public UserModel deactivateUser(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deactivateUser'");
    }

    private void validateUserRequest(UserCreationRequest request) {
        if (request.getSupabaseUid() == null || request.getSupabaseUid().isBlank()) {
            throw new IllegalArgumentException("Supabase UID is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        
    }
}
