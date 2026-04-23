package com.fitapp.backend.user.aplication.port.input;

import com.fitapp.backend.user.aplication.dto.request.UserCreationRequest;
import com.fitapp.backend.user.aplication.dto.request.UserUpdateRequest;
import com.fitapp.backend.user.domain.model.UserModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserUseCase {
    // ── CRUD (admin) ────────────────────────────────────────────────────────────
    UserModel createUser(UserCreationRequest request);
    UserModel updateUser(Long id, UserUpdateRequest updateRequest);
    void deleteUser(Long id);
    UserModel findById(Long id);
    Page<UserModel> findAll(Pageable pageable);
    void activateUser(Long id);
    void deactivateUser(Long id);
    void toggleUserStatus(Long userId, boolean isActive);
    Optional<UserModel> findByEmail(String email);

    // ── Acciones de cuenta ──────────────────────────────────────────────────────
    void updateLastLogin(Long userId);
    void updatePassword(Long userId, String newPassword);

    /**
     * Cambia la contraseña del usuario verificando primero la contraseña actual.
     * Lanza BadCredentialsException si currentPassword no coincide.
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Soft-delete: marca deletedAt y desactiva la cuenta.
     * Envía correo de confirmación al usuario.
     */
    void softDeleteUser(Long userId);
}