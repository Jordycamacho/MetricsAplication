package com.fitapp.backend.sport.aplication.port.output;

import com.fitapp.backend.sport.aplication.dto.request.SportFilterRequest;
import com.fitapp.backend.sport.domain.model.SportModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SportPersistencePort {
    Optional<SportModel> findById(Long id);
    long countByCreatedBy(Long userId);
    List<SportModel> findAll();
    Page<SportModel> findAll(Pageable pageable);
    Optional<Long> findIdByName(String name);
    Page<SportModel> findByFilters(SportFilterRequest filters, Pageable pageable);
    Page<SportModel> searchSports(String search, Pageable pageable);
    List<SportModel> findByIsPredefinedTrue();
    Page<SportModel> findPredefinedWithSearch(String search, Pageable pageable);
    List<SportModel> findByCreatedBy(Long userId);
    Page<SportModel> findByUserWithSearch(Long userId, String search, Pageable pageable);
    SportModel save(SportModel sportModel);
    void delete(Long id);
    List<SportModel> findAllById(Set<Long> sportIds);
}