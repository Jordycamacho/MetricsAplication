package com.fitapp.backend.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("role")
    private Role role;
    
    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;
    
    @JsonProperty("maxRoutines")
    private Integer maxRoutines;
}