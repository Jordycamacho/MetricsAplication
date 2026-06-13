package com.fitapp.backend.feedback.aplication.port.output;

import com.fitapp.backend.feedback.aplication.dto.request.AdminFeedbackFilterRequest;
import com.fitapp.backend.feedback.domain.model.FeedbackModel;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface FeedbackPersistencePort {

    FeedbackModel save(FeedbackModel feedback, Long userId);

    Optional<FeedbackModel> findActiveById(Long id);

    Optional<FeedbackModel> findActiveByIdAndUserId(Long id, Long userId);

    Page<FeedbackModel> findAllActiveFiltered(AdminFeedbackFilterRequest filter);

    FeedbackModel update(FeedbackModel feedback);
}
