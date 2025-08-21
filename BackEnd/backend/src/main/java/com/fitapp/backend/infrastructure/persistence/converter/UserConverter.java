package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

public class UserConverter {

    public static UserModel toDomain(UserEntity entity) {
        return UserModel.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .profileImage(entity.getProfileImage())
                .maxRoutines(entity.getMaxRoutines())
                .role(entity.getRole())
                .lastLogin(entity.getLastLogin())
                .isActive(entity.isActive())
                .maxRoutines(entity.getMaxRoutines())
                .subscription(SubscriptionConverter.toDomain(entity.getSubscription()))
                .build();
    }

    public static UserEntity toEntity(UserModel model) {
        UserEntity entity = new UserEntity();
        if (model.getId() != null) {
            entity.setId(model.getId());
        }
        entity.setEmail(model.getEmail());
        entity.setFullName(model.getFullName());
        entity.setProfileImage(model.getProfileImage());
        entity.setPassword(model.getPassword());
        entity.setRole(model.getRole());
        entity.setLastLogin(model.getLastLogin());
        entity.setActive(model.isActive());
        entity.setMaxRoutines(model.getMaxRoutines());
        return entity;
    }
}