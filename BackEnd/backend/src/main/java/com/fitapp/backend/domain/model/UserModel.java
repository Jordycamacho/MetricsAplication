package com.fitapp.backend.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserModel {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private String supabaseUid;
    private String email;
    private String passwordHash;
    private Role role;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private Integer maxRoutines;
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
