package com.fitapp.backend.feedback.aplication.port.input;

import com.fitapp.backend.feedback.aplication.dto.request.AdminFeedbackFilterRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackAdminRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackStatusRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackPageResponse;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackResponse;

public interface AdminFeedbackUseCase {
    FeedbackPageResponse list(AdminFeedbackFilterRequest filter);

    FeedbackResponse getById(Long id);

    FeedbackResponse updateStatus(Long id, UpdateFeedbackStatusRequest request);

    FeedbackResponse update(Long id, UpdateFeedbackAdminRequest request);
}
