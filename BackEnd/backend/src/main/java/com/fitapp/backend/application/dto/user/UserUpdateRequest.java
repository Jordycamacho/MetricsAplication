package com.fitapp.backend.application.dto.user;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private Role role;
    private Boolean isActive;
    private Integer maxRoutines;
}