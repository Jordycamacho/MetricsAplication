package com.fitapp.backend.application.dto.user;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private Integer maxRoutines;
    private Boolean isActive;
    private String fullName;
    private Role role;
}