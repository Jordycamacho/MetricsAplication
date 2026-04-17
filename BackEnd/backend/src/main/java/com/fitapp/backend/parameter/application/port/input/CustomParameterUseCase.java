package com.fitapp.backend.parameter.application.port.input;

import com.fitapp.backend.parameter.application.dto.request.CustomParameterFilterRequest;
import com.fitapp.backend.parameter.application.dto.request.CustomParameterRequest;
import com.fitapp.backend.parameter.application.dto.response.CustomParameterPageResponse;
import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;

import java.util.List;

public interface CustomParameterUseCase {
    CustomParameterPageResponse getAllParametersPaginated(CustomParameterFilterRequest filterRequest);
    CustomParameterPageResponse getMyParametersPaginated(String userEmail, CustomParameterFilterRequest filterRequest);
    CustomParameterPageResponse getAvailableParametersPaginated(String userEmail, CustomParameterFilterRequest filterRequest);
    CustomParameterModel getParameterById(Long id);
    CustomParameterModel createParameter(CustomParameterRequest request, String userEmail);
    CustomParameterModel updateParameter(Long id, CustomParameterRequest request, String userEmail);
    void deleteParameter(Long id, String userEmail);
    void toggleParameterStatus(Long id, String userEmail);
    List<ParameterType> getAllParameterTypes();
    void incrementParameterUsage(Long parameterId);
    void toggleFavorite(Long id, String userEmail);
}