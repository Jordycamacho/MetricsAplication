package com.fitapp.backend.feedback.aplication.service;

import com.fitapp.backend.feedback.aplication.dto.request.AdminFeedbackFilterRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackAdminRequest;
import com.fitapp.backend.feedback.aplication.dto.request.UpdateFeedbackStatusRequest;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackPageResponse;
import com.fitapp.backend.feedback.aplication.dto.response.FeedbackResponse;
import com.fitapp.backend.feedback.aplication.port.input.AdminFeedbackUseCase;
import com.fitapp.backend.feedback.aplication.port.output.FeedbackPersistencePort;
import com.fitapp.backend.feedback.domain.exception.FeedbackNotFoundException;
import com.fitapp.backend.feedback.domain.model.FeedbackModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFeedbackServiceImpl implements AdminFeedbackUseCase {

    private final FeedbackPersistencePort feedbackPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public FeedbackPageResponse list(AdminFeedbackFilterRequest filter) {
        Page<FeedbackModel> page = feedbackPersistencePort.findAllActiveFiltered(filter);
        List<FeedbackResponse> content = page.getContent().stream()
                .map(FeedbackResponseMapper::toResponse)
                .toList();

        return FeedbackPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getById(Long id) {
        FeedbackModel model = feedbackPersistencePort.findActiveById(id)
                .orElseThrow(() -> new FeedbackNotFoundException(id));
        return FeedbackResponseMapper.toResponse(model);
    }

    @Override
    @Transactional
    public FeedbackResponse updateStatus(Long id, UpdateFeedbackStatusRequest request) {
        FeedbackModel model = feedbackPersistencePort.findActiveById(id)
                .orElseThrow(() -> new FeedbackNotFoundException(id));
        model.setStatus(request.getStatus());
        FeedbackModel updated = feedbackPersistencePort.update(model);
        log.info("FEEDBACK_STATUS_UPDATED | id={} | status={}", id, request.getStatus());
        return FeedbackResponseMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public FeedbackResponse update(Long id, UpdateFeedbackAdminRequest request) {
        FeedbackModel model = feedbackPersistencePort.findActiveById(id)
                .orElseThrow(() -> new FeedbackNotFoundException(id));

        if (request.getAdminNotes() != null) {
            model.setAdminNotes(request.getAdminNotes());
        }
        if (request.getIsPublic() != null) {
            model.setIsPublic(request.getIsPublic());
        }

        FeedbackModel updated = feedbackPersistencePort.update(model);
        log.info("FEEDBACK_ADMIN_UPDATED | id={}", id);
        return FeedbackResponseMapper.toResponse(updated);
    }
}
