package com.fitapp.backend.application.dto.user;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private Role role;
    @Builder.Default
    private boolean isActive = true;
    private Integer maxRoutines;
}