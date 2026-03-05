package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public UserModel toDomain(UserEntity entity) {
        if (entity == null) return null;

        return UserModel.builder()
                .id(entity.getId())
                .googleId(entity.getGoogleId())
                .provider(entity.getProvider())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .profileImage(entity.getProfileImage())
                .password(entity.getPassword())
                .role(entity.getRole())
                .lastLogin(entity.getLastLogin())
                .isActive(entity.isActive())
                .emailVerified(entity.isEmailVerified())
                .emailVerificationToken(entity.getEmailVerificationToken())
                .emailVerificationTokenExpiresAt(entity.getEmailVerificationTokenExpiresAt())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .build();
    }

    public UserEntity toEntity(UserModel model) {
        if (model == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setGoogleId(model.getGoogleId());
        entity.setProvider(model.getProvider());
        entity.setEmail(model.getEmail());
        entity.setFullName(model.getFullName());
        entity.setProfileImage(model.getProfileImage());
        entity.setPassword(model.getPassword());
        entity.setRole(model.getRole());
        entity.setLastLogin(model.getLastLogin());
        entity.setActive(model.isActive());
        entity.setEmailVerified(model.isEmailVerified());
        entity.setEmailVerificationToken(model.getEmailVerificationToken());
        entity.setEmailVerificationTokenExpiresAt(model.getEmailVerificationTokenExpiresAt());
        entity.setDeletedAt(model.getDeletedAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setVersion(model.getVersion());

        return entity;
    }

    /** Actualiza una entidad existente desde un modelo (para evitar perder relaciones JPA). */
    public void updateEntityFromModel(UserEntity entity, UserModel model) {
        entity.setEmail(model.getEmail());
        entity.setGoogleId(model.getGoogleId());
        entity.setProvider(model.getProvider());
        entity.setFullName(model.getFullName());
        entity.setProfileImage(model.getProfileImage());
        entity.setPassword(model.getPassword());
        entity.setRole(model.getRole());
        entity.setLastLogin(model.getLastLogin());
        entity.setActive(model.isActive());
        entity.setEmailVerified(model.isEmailVerified());
        entity.setEmailVerificationToken(model.getEmailVerificationToken());
        entity.setEmailVerificationTokenExpiresAt(model.getEmailVerificationTokenExpiresAt());
        entity.setDeletedAt(model.getDeletedAt());
    }
}