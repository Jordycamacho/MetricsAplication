package com.fitapp.backend.application.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import com.fitapp.backend.domain.model.FreeSubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserUseCase {

    private final UserPersistencePort userPersistence;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void updatePassword(Long userId, String newPassword) {
        UserModel user = userPersistence.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userPersistence.save(user);
    }

    @Override
    public void updateLastLogin(Long userId) {
        UserModel user = userPersistence.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateLastLogin();
        userPersistence.save(user);
    }

    @Override
    @Transactional
    public UserModel createUser(UserCreationRequest request) {

        if (findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        UserModel userModel = UserModel.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isActive(request.isActive())
                .maxRoutines(request.getMaxRoutines() != null ? request.getMaxRoutines() : 1)
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
    public UserModel updateUser(Long id, UserUpdateRequest updateRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public UserModel findById(Long id) {
        return userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<UserModel> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public void activateUser(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'activateUser'");
    }

    @Override
    public void deactivateUser(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'deactivateUser'");
    }

    @Override
    public void toggleUserStatus(Long userId, boolean isActive) {
        throw new UnsupportedOperationException("Unimplemented method 'toggleUserStatus'");
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }
}
