package com.fitapp.backend.application.ports.input;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.domain.model.UserModel;

public interface UserUseCase {
    UserModel createUser(UserCreationRequest  request);
    UserModel updateUser(UUID id, UserUpdateRequest updateRequest);
    void deleteUser(UUID id);
    UserModel getUserById(UUID id);
    Page<UserModel> findAll(Pageable pageable);
    void activateUser(UUID id);
    void deactivateUser(UUID id);
    public void updateLastLogin(UUID userId);
    public void toggleUserStatus(UUID userId, boolean isActive);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findBySupabaseUid(String supabaseUid);
}