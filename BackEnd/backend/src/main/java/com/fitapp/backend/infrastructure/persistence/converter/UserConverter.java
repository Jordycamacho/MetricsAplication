package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public UserModel toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserModel.UserModelBuilder builder = UserModel.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .profileImage(entity.getProfileImage())
                .password(entity.getPassword())
                .maxRoutines(entity.getMaxRoutines())
                .role(entity.getRole())
                .lastLogin(entity.getLastLogin())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion());

        if (entity.getSubscription() != null) {
            //sin suscripcion por el momento
        }

        return builder.build();
    }

    public UserEntity toEntity(UserModel model) {
        if (model == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setEmail(model.getEmail());
        entity.setFullName(model.getFullName());
        entity.setProfileImage(model.getProfileImage());
        entity.setPassword(model.getPassword());
        entity.setRole(model.getRole());
        entity.setLastLogin(model.getLastLogin());
        entity.setActive(model.isActive());
        entity.setMaxRoutines(model.getMaxRoutines());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setVersion(model.getVersion());

        return entity;
    }
}