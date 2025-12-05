package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.domain.model.UserModel;

public interface JwtService {

    String generateToken(UserModel user);    
    String generateRefreshToken(UserModel user);
    
}