package com.fitapp.backend.feedback.infrastructure.persistence.repository;

import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import com.fitapp.backend.feedback.infrastructure.persistence.entity.UserFeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFeedbackRepository extends JpaRepository<UserFeedbackEntity, Long> {

    Optional<UserFeedbackEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<UserFeedbackEntity> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            SELECT f FROM UserFeedbackEntity f
            WHERE f.deletedAt IS NULL
            AND (:type IS NULL OR f.type = :type)
            AND (:status IS NULL OR f.status = :status)
            AND (:category IS NULL OR f.category = :category)
            ORDER BY f.createdAt DESC
            """)
    Page<UserFeedbackEntity> findAllActiveFiltered(
            @Param("type") FeedbackType type,
            @Param("status") FeedbackStatus status,
            @Param("category") FeedbackCategory category,
            Pageable pageable);
}
