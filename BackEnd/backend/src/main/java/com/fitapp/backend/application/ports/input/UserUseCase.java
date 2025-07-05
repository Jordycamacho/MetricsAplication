package com.fitapp.backend.application.ports.input;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.domain.model.UserModel;

public interface UserUseCase {
    UserModel createUser(UserCreationRequest  request);
    UserModel updateUser(UUID id, UserUpdateRequest updateRequest);
    void updatePassword(UUID userId, String newPassword);
    void deleteUser(UUID id);
    UserModel getUserById(UUID id);
    List<UserModel> getAllUsers();
    void activateUser(UUID id);
    void deactivateUser(UUID id);
    public void updateLastLogin(UUID userId);
    public void toggleUserStatus(UUID userId, boolean isActive);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findBySupabaseUid(String supabaseUid);
}