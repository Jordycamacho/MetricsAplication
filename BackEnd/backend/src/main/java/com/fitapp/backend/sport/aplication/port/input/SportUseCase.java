package com.fitapp.backend.sport.aplication.port.input;

import com.fitapp.backend.sport.aplication.dto.request.SportFilterRequest;
import com.fitapp.backend.sport.aplication.dto.request.SportRequest;
import com.fitapp.backend.sport.aplication.dto.response.SportPageResponse;
import com.fitapp.backend.sport.domain.model.SportModel;

import java.util.List;

public interface SportUseCase {
    List<SportModel> getAllSports();
    SportPageResponse getAllSportsPaginated(SportFilterRequest filterRequest);
    SportPageResponse getPredefinedSportsPaginated(SportFilterRequest filterRequest);
    SportPageResponse getUserSportsPaginated(String userEmail, SportFilterRequest filterRequest);
    List<SportModel> getPredefinedSports();
    List<SportModel> getUserSports(String userEmail);
    SportModel createCustomSport(SportRequest sportRequest, String userEmail);
    void deleteCustomSport(Long sportId, String userEmail);
}