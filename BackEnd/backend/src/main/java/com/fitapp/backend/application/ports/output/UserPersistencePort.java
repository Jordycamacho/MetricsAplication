package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.UserModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistencePort {
    UserModel save(UserModel user);
    Optional<UserModel> findById(UUID id);
    List<UserModel> findAll();
    void deleteById(UUID id);
    Optional<UserModel> findByEmail(String email);
}