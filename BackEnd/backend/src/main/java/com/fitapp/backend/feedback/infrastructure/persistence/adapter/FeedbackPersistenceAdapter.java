package com.fitapp.backend.feedback.infrastructure.persistence.adapter;

import com.fitapp.backend.auth.domain.exception.UserNotFoundException;
import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.auth.infrastructure.persistence.repository.SpringDataUserRepository;
import com.fitapp.backend.feedback.aplication.dto.request.AdminFeedbackFilterRequest;
import com.fitapp.backend.feedback.aplication.port.output.FeedbackPersistencePort;
import com.fitapp.backend.feedback.domain.model.FeedbackModel;
import com.fitapp.backend.feedback.infrastructure.persistence.converter.FeedbackConverter;
import com.fitapp.backend.feedback.infrastructure.persistence.entity.UserFeedbackEntity;
import com.fitapp.backend.feedback.infrastructure.persistence.repository.UserFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackPersistenceAdapter implements FeedbackPersistencePort {

    private final UserFeedbackRepository feedbackRepository;
    private final SpringDataUserRepository userRepository;
    private final FeedbackConverter feedbackConverter;

    @Override
    public FeedbackModel save(FeedbackModel feedback, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        UserFeedbackEntity entity = feedbackConverter.toEntity(feedback);
        entity.setUser(user);
        UserFeedbackEntity saved = feedbackRepository.save(entity);
        log.info("FEEDBACK_SAVED | id={} | userId={} | type={}", saved.getId(), userId, saved.getType());
        return feedbackConverter.toDomain(saved);
    }

    @Override
    public Optional<FeedbackModel> findActiveById(Long id) {
        return feedbackRepository.findByIdAndDeletedAtIsNull(id)
                .map(feedbackConverter::toDomain);
    }

    @Override
    public Optional<FeedbackModel> findActiveByIdAndUserId(Long id, Long userId) {
        return feedbackRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(feedbackConverter::toDomain);
    }

    @Override
    public Page<FeedbackModel> findAllActiveFiltered(AdminFeedbackFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        return feedbackRepository.findAllActiveFiltered(
                        filter.getType(),
                        filter.getStatus(),
                        filter.getCategory(),
                        pageable)
                .map(feedbackConverter::toDomain);
    }

    @Override
    public FeedbackModel update(FeedbackModel feedback) {
        UserFeedbackEntity entity = feedbackRepository.findByIdAndDeletedAtIsNull(feedback.getId())
                .orElseThrow(() -> new com.fitapp.backend.feedback.domain.exception.FeedbackNotFoundException(feedback.getId()));

        entity.setStatus(feedback.getStatus());
        entity.setAdminNotes(feedback.getAdminNotes());
        if (feedback.getIsPublic() != null) {
            entity.setIsPublic(feedback.getIsPublic());
        }
        entity.setDeletedAt(feedback.getDeletedAt());

        UserFeedbackEntity saved = feedbackRepository.save(entity);
        return feedbackConverter.toDomain(saved);
    }
}
