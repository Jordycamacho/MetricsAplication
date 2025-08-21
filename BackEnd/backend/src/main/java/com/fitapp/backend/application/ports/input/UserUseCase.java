package com.fitapp.backend.application.ports.input;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.domain.model.UserModel;

public interface UserUseCase {
    UserModel createUser(UserCreationRequest  request);
    UserModel updateUser(Long id, UserUpdateRequest updateRequest);
    void deleteUser(Long id);
    UserModel findById(Long id);
    Page<UserModel> findAll(Pageable pageable);
    void activateUser(Long id);
    void deactivateUser(Long id);
    public void updateLastLogin(Long userId);
    public void toggleUserStatus(Long userId, boolean isActive);
    Optional<UserModel> findByEmail(String email);
    void updatePassword(Long userId, String newPassword);
}