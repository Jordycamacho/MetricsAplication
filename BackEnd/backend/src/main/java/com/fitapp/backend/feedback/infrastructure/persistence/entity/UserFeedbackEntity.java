package com.fitapp.backend.feedback.infrastructure.persistence.entity;

import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.feedback.domain.model.FeedbackCategory;
import com.fitapp.backend.feedback.domain.model.FeedbackStatus;
import com.fitapp.backend.feedback.domain.model.FeedbackType;
import com.fitapp.backend.infrastructure.config.StringMapConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_feedback", indexes = {
        @Index(name = "idx_user_feedback_user_deleted", columnList = "user_id, deleted_at"),
        @Index(name = "idx_user_feedback_status", columnList = "status"),
        @Index(name = "idx_user_feedback_type", columnList = "type"),
        @Index(name = "idx_user_feedback_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class UserFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FeedbackCategory category;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "steps_to_reproduce", columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackStatus status = FeedbackStatus.RECEIVED;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Convert(converter = StringMapConverter.class)
    @Column(name = "technical_context", columnDefinition = "TEXT")
    private Map<String, String> technicalContext = new HashMap<>();

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (technicalContext == null) {
            technicalContext = new HashMap<>();
        }
        if (status == null) {
            status = FeedbackStatus.RECEIVED;
        }
        if (isPublic == null) {
            isPublic = false;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
