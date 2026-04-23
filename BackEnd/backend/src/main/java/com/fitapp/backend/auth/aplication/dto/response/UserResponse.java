package com.fitapp.backend.auth.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.suscription.aplication.dto.response.SubscriptionResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("role")
    private Role role;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("subscription")
    private SubscriptionResponse subscription;
}