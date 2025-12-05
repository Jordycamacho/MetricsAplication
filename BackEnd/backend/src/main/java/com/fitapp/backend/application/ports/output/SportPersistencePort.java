package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.SportModel;
import java.util.List;
import java.util.Optional;

public interface SportPersistencePort {
    Optional<SportModel> findById(Long id);
    List<SportModel> findAll();
    List<SportModel> findByIsPredefinedTrue();
    List<SportModel> findByCreatedBy(Long userId);
    SportModel save(SportModel sportModel);
    void delete(Long id);
}