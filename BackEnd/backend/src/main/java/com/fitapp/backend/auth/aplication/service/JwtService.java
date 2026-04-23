package com.fitapp.backend.auth.aplication.service;

import com.fitapp.backend.auth.domain.model.UserModel;

public interface JwtService {

    String generateToken(UserModel user);    
    String generateRefreshToken(UserModel user);
    
}