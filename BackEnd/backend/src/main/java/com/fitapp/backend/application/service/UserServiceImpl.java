package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.user.UserUpdateRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.FreeSubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import com.fitapp.backend.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        UserModel user = userPersistence.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userPersistence.save(user);
    }
    
    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        UserModel user = userPersistence.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.updateLastLogin();
        userPersistence.save(user);
    }
    
    @Override
    @Transactional
    public UserModel createUser(UserCreationRequest request) {
        // Verificar si el email ya existe
        if (userPersistence.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        // Crear el modelo de usuario
        UserModel userModel = UserModel.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isActive(request.isActive())
                .maxRoutines(request.getMaxRoutines() != null ? request.getMaxRoutines() : 1)
                .build();
        
        // Crear suscripción gratis
        FreeSubscriptionModel freeSubscription = FreeSubscriptionModel.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .maxRoutines(1)
                .build();
        
        userModel.setSubscription(freeSubscription);
        
        // Guardar usuario
        return userPersistence.save(userModel);
    }
    
    @Override
    @Transactional
    public UserModel updateUser(Long id, UserUpdateRequest updateRequest) {
        UserModel user = userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        
        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }
        
        if (updateRequest.getIsActive() != null) {
            user.setActive(updateRequest.getIsActive());
        }
        
        if (updateRequest.getMaxRoutines() != null) {
            user.setMaxRoutines(updateRequest.getMaxRoutines());
        }
        
        return userPersistence.save(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        userPersistence.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserModel findById(Long id) {
        return userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserModel> findAll(Pageable pageable) {
        return userPersistence.findAll(pageable);
    }
    
    @Override
    @Transactional
    public void activateUser(Long id) {
        UserModel user = userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.activate();
        userPersistence.save(user);
    }
    
    @Override
    @Transactional
    public void deactivateUser(Long id) {
        UserModel user = userPersistence.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.deactivate();
        userPersistence.save(user);
    }
    
    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean isActive) {
        UserModel user = userPersistence.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (isActive) {
            user.activate();
        } else {
            user.deactivate();
        }
        
        userPersistence.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> findByEmail(String email) {
        return userPersistence.findByEmail(email);
    }
}