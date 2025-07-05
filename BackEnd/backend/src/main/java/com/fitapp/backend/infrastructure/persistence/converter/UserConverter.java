package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.application.dto.user.UserResponse;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;

public class UserConverter {

    public static UserModel toDomain(UserEntity entity) {
        return UserModel.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .supabaseUid(entity.getSupabaseUid())
                .email(entity.getEmail())
                .role(entity.getRole())
                .lastLogin(entity.getLastLogin())
                .isActive(entity.isActive())
                .maxRoutines(entity.getMaxRoutines())
                .build();
    }

    public static UserResponse toResponse(UserModel model) {
        return UserResponse.builder()
                .id(model.getId())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .email(model.getEmail())
                .role(model.getRole())
                .isActive(model.isActive())
                .maxRoutines(model.getMaxRoutines())
                .build();
    }

    public static UserEntity toEntity(UserModel domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .supabaseUid(domain.getSupabaseUid())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .role(domain.getRole())
                .lastLogin(domain.getLastLogin())
                .isActive(domain.isActive())
                .maxRoutines(domain.getMaxRoutines())
                .build();
    }
}