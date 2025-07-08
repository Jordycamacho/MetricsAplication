package com.fitapp.backend.application.dto.user;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    private String supabaseUid;
    private String email;
    private Role role;
    @Builder.Default
    private boolean isActive = true;
    private Integer maxRoutines;
}