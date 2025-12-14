package com.fitapp.backend.application.dto.user;

import java.time.LocalDateTime;
import com.fitapp.backend.application.dto.subscription.SubscriptionResponse;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String email;
    private String fullName;
    private Role role;
    private boolean isActive;
    private Integer maxRoutines;
    private SubscriptionResponse subscription;
}