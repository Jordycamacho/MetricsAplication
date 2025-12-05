package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.domain.model.SportModel;
import java.util.List;

public interface SportUseCase {
    List<SportModel> getAllSports();
    List<SportModel> getPredefinedSports();
    List<SportModel> getUserSports(String userEmail);
    SportModel createCustomSport(SportModel sportModel, String userEmail);
    void deleteCustomSport(Long sportId, String userEmail);
}
