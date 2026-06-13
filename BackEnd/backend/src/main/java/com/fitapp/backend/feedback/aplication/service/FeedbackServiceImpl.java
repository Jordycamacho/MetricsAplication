package com.fitapp.backend.feedback.aplication.service;

import com.fitapp.backend.feedback.aplication.dto.request.CreateFeedbackRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackSubmitResponse;
import com.fitapp.backend.feedback.aplication.port.input.FeedbackUseCase;
import com.fitapp.backend.feedback.aplication.port.output.FeedbackPersistencePort;
import com.fitapp.backend.feedback.domain.exception.FeedbackAccessDeniedException;
import com.fitapp.backend.feedback.domain.exception.FeedbackNotFoundException;
import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackModel;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.notification.aplication.port.input.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackUseCase {

    private final FeedbackPersistencePort feedbackPersistencePort;
    private final NotificationUseCase notificationUseCase;

    @Override
    @Transactional
    public FeedbackSubmitResponse submit(CreateFeedbackRequest request, Long userId) {
        Map<String, String> technicalContext = new HashMap<>();
        if (Boolean.TRUE.equals(request.getIncludeTechnicalContext()) && request.getTechnicalContext() != null) {
            technicalContext.putAll(request.getTechnicalContext());
        }

        FeedbackModel model = FeedbackModel.builder()
                .type(request.getType())
                .category(request.getCategory() != null ? request.getCategory() : FeedbackCategory.OTHER)
                .title(trimToNull(request.getTitle()))
                .message(request.getMessage().trim())
                .stepsToReproduce(trimToNull(request.getStepsToReproduce()))
                .status(FeedbackStatus.RECEIVED)
                .isPublic(false)
                .technicalContext(technicalContext)
                .build();

        FeedbackModel saved = feedbackPersistencePort.save(model, userId);
        log.info("FEEDBACK_SUBMITTED | id={} | userId={} | type={}", saved.getId(), userId, saved.getType());

        notificationUseCase.sendFeedbackReceivedUserEmail(
                saved.getUserEmail(),
                saved.getUserFullName(),
                saved.getId(),
                saved.getType().name());
        notificationUseCase.sendFeedbackReceivedAdminEmail(
                saved.getId(),
                saved.getType().name(),
                saved.getCategory() != null ? saved.getCategory().name() : FeedbackCategory.OTHER.name(),
                saved.getUserEmail(),
                saved.getUserFullName(),
                saved.getMessage());

        return FeedbackSubmitResponse.builder()
                .id(saved.getId())
                .message("Hemos recibido tu envío correctamente. Te hemos enviado un correo de confirmación.")
                .build();
    }

    @Override
    @Transactional
    public void deleteOwn(Long feedbackId, Long userId) {
        FeedbackModel existing = feedbackPersistencePort.findActiveByIdAndUserId(feedbackId, userId)
                .orElseThrow(() -> {
                    if (feedbackPersistencePort.findActiveById(feedbackId).isPresent()) {
                        throw new FeedbackAccessDeniedException();
                    }
                    throw new FeedbackNotFoundException(feedbackId);
                });

        existing.setDeletedAt(LocalDateTime.now());
        feedbackPersistencePort.update(existing);
        log.info("FEEDBACK_DELETED | id={} | userId={}", feedbackId, userId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
