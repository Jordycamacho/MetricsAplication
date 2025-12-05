package com.fitapp.backend.application.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String email;
    private Role role;
    private boolean isActive;
    private Integer maxRoutines;
}