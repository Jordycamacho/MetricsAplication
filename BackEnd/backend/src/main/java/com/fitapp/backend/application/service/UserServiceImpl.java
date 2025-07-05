package com.fitapp.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
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
    public UserModel updateUser(UUID id, UserModel user) {
        return persistencePort.findById(id)
                .map(existing -> {
                    UserModel updated = UserModel.builder()
                            .id(existing.getId())
                            .createdAt(existing.getCreatedAt())
                            .updatedAt(LocalDateTime.now())
                            .version(existing.getVersion())
                            .supabaseUid(existing.getSupabaseUid())
                            .email(user.getEmail() != null ? user.getEmail() : existing.getEmail())
                            .role(user.getRole() != null ? user.getRole() : existing.getRole())
                            .lastLogin(user.getLastLogin() != null ? user.getLastLogin() : existing.getLastLogin())
                            .isActive(user.isActive())
                            .maxRoutines(
                                    user.getMaxRoutines() != null ? user.getMaxRoutines() : existing.getMaxRoutines())
                            .build();
                    return persistencePort.save(updated);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
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
    public UserModel activateUser(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'activateUser'");
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
