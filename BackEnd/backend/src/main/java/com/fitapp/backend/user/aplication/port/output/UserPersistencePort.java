package com.fitapp.backend.user.aplication.port.output;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fitapp.backend.user.domain.model.UserModel;

public interface UserPersistencePort {
    Optional<UserModel> findById(Long id);
    Optional<UserModel> findByGoogleId(String googleId);
    Page<UserModel> findAll(Pageable pageable);
    void deleteById(Long id);
    Optional<UserModel> findByEmail(String email);
    UserModel save(UserModel user);
    Optional<UserModel> findByEmailVerificationToken(String token);
}