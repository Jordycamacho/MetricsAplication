package com.fitapp.backend.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @JsonProperty("maxRoutines")
    private Integer maxRoutines;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("role")
    private Role role;
}