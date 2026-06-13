package com.fitapp.backend.feedback.aplication.port.input;

import com.fitapp.backend.feedback.aplication.dto.request.CreateFeedbackRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackSubmitResponse;

public interface FeedbackUseCase {
    FeedbackSubmitResponse submit(CreateFeedbackRequest request, Long userId);

    void deleteOwn(Long feedbackId, Long userId);
}
