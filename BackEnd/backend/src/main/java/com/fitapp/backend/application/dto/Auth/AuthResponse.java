package com.fitapp.backend.application.dto.Auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    @JsonProperty("access_token")
    String accessToken;
    
    @JsonProperty("token_type")
    String tokenType;
    
    @JsonProperty("user")
    UserData user;
    
    @Value
    public static class UserData {
        String id;
        String email;
    }
    
    public String getUserId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
}