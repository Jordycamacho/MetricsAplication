package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.UserModel;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPersistencePort {
    UserModel save(UserModel user);
    Optional<UserModel> findById(UUID id);
    Page<UserModel> findAll(Pageable pageable);
    void deleteById(UUID id);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findBySupabaseUid(String supabaseUid);
}