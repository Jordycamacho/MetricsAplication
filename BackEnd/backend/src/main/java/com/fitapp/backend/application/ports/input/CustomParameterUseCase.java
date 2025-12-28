package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.dto.parameter.request.CustomParameterRequest;
import com.fitapp.backend.application.dto.parameter.response.CustomParameterPageResponse;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import java.util.List;

public interface CustomParameterUseCase {
    CustomParameterPageResponse getAllParametersPaginated(CustomParameterFilterRequest filterRequest);
    CustomParameterPageResponse getMyParametersPaginated(String userEmail, CustomParameterFilterRequest filterRequest);
    CustomParameterPageResponse getAvailableParametersPaginated(String userEmail, Long sportId, CustomParameterFilterRequest filterRequest);
    CustomParameterModel getParameterById(Long id);
    CustomParameterModel createParameter(CustomParameterRequest request, String userEmail);
    CustomParameterModel updateParameter(Long id, CustomParameterRequest request, String userEmail);
    void deleteParameter(Long id, String userEmail);
    void toggleParameterStatus(Long id, String userEmail);
    List<String> getAllCategories();
    List<ParameterType> getAllParameterTypes();
    void incrementParameterUsage(Long parameterId);
}