package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.application.dto.sport.request.SportFilterRequest;
import com.fitapp.backend.domain.model.SportModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface SportPersistencePort {
    Optional<SportModel> findById(Long id);
    List<SportModel> findAll();
    Page<SportModel> findAll(Pageable pageable);
    Page<SportModel> findByFilters(SportFilterRequest filters, Pageable pageable);
    Page<SportModel> searchSports(String search, String category, Pageable pageable);
    List<SportModel> findByIsPredefinedTrue();
    Page<SportModel> findPredefinedWithSearch(String search, Pageable pageable);
    List<SportModel> findByCreatedBy(Long userId);
    Page<SportModel> findByUserWithSearch(Long userId, String search, Pageable pageable);
    List<String> findAllDistinctCategories();
    SportModel save(SportModel sportModel);
    void delete(Long id);
}