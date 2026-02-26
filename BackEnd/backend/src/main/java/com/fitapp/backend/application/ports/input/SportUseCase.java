package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.sport.request.SportFilterRequest;
import com.fitapp.backend.application.dto.sport.request.SportPageResponse;
import com.fitapp.backend.application.dto.sport.request.SportRequest;
import com.fitapp.backend.domain.model.SportModel;
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