package com.fitapp.backend.application.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    private String email;
    private String password;
    private String fullName;
    private Role role;
    @Builder.Default
    private boolean isActive = true;
    private Integer maxRoutines;
}