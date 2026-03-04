package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.FreeSubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {

    private final UserPersistencePort userPersistence;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ── Contraseñas ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        UserModel user = findOrThrow(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userPersistence.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserModel user = findOrThrow(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userPersistence.save(user);
    }

    // ── Eliminación de cuenta (soft delete) ─────────────────────────────────────

    @Override
    @Transactional
    public void softDeleteUser(Long userId) {
        UserModel user = findOrThrow(userId);
        user.softDelete();
        userPersistence.save(user);
        emailService.sendAccountDeletionConfirmation(user.getEmail(), user.getFullName());
    }

    // ── Sesión ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        UserModel user = findOrThrow(userId);
        user.updateLastLogin();
        userPersistence.save(user);
    }

    // ── CRUD ────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserModel createUser(UserCreationRequest request) {
        if (userPersistence.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        UserModel userModel = UserModel.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isActive(request.isActive())
                .maxRoutines(request.getMaxRoutines() != null ? request.getMaxRoutines() : 1)
                .emailVerified(false)
                .build();

        FreeSubscriptionModel freeSubscription = FreeSubscriptionModel.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .maxRoutines(1)
                .build();

        userModel.setSubscription(freeSubscription);
        return userPersistence.save(userModel);
    }

    @Override
    @Transactional
    public UserModel updateUser(Long id, UserUpdateRequest updateRequest) {
        UserModel user = findOrThrow(id);

        if (updateRequest.getFullName() != null)
            user.setFullName(updateRequest.getFullName());
        if (updateRequest.getRole() != null)
            user.setRole(updateRequest.getRole());
        if (updateRequest.getIsActive() != null)
            user.setActive(updateRequest.getIsActive());
        if (updateRequest.getMaxRoutines() != null)
            user.setMaxRoutines(updateRequest.getMaxRoutines());

        return userPersistence.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        findOrThrow(id);
        userPersistence.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserModel findById(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserModel> findAll(Pageable pageable) {
        return userPersistence.findAll(pageable);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        UserModel user = findOrThrow(id);
        user.activate();
        userPersistence.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        UserModel user = findOrThrow(id);
        user.deactivate();
        userPersistence.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean isActive) {
        UserModel user = findOrThrow(userId);
        if (isActive)
            user.activate();
        else
            user.deactivate();
        userPersistence.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }

    // ── Helper privado ──────────────────────────────────────────────────────────

    private UserModel findOrThrow(Long id) {
        return userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}