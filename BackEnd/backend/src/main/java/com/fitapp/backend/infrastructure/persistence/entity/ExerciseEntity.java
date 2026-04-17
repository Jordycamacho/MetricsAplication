package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(name = "exercises", indexes = {
    @Index(name = "idx_exercise_created_by", columnList = "created_by"),
    @Index(name = "idx_exercise_type", columnList = "exercise_type"),
    @Index(name = "idx_exercise_active", columnList = "is_active"),
    @Index(name = "idx_exercise_public", columnList = "is_public"),
    @Index(name = "idx_exercise_usage", columnList = "usage_count"),
    @Index(name = "idx_exercise_rating", columnList = "rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exercise_sports",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "sport_id"),
        indexes = {
            @Index(name = "idx_exercise_sports_exercise", columnList = "exercise_id"),
            @Index(name = "idx_exercise_sports_sport", columnList = "sport_id")
        }
    )
    @Builder.Default
    private Set<SportEntity> sports = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exercise_category_mapping",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<ExerciseCategoryEntity> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exercise_supported_parameters",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "parameter_id")
    )
    @Builder.Default
    private Set<CustomParameterEntity> supportedParameters = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 50)
    private ExerciseType exerciseType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseEntity)) return false;
        ExerciseEntity that = (ExerciseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}