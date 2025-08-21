package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.UserModel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPersistencePort {
    Optional<UserModel> findById(Long id);
    Page<UserModel> findAll(Pageable pageable);
    void deleteById(Long id);
    Optional<UserModel> findByEmail(String email);
    UserModel save(UserModel user);
}