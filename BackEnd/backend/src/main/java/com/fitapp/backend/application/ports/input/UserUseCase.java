package com.fitapp.backend.application.ports.input;

import java.util.List;
import java.util.UUID;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.domain.model.UserModel;

public interface UserUseCase {
    UserModel createUser(UserCreationRequest  request);
    UserModel updateUser(UUID id, UserModel user);
    void deleteUser(UUID id);
    UserModel getUserById(UUID id);
    List<UserModel> getAllUsers();
    UserModel activateUser(UUID id);
    UserModel deactivateUser(UUID id);
    public void updateLastLogin(UUID userId);
    public void toggleUserStatus(UUID userId, boolean isActive);
}