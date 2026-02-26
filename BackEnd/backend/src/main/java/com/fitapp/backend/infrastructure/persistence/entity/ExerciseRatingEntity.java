package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_ratings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_exercise_rating_user",
        columnNames = {"exercise_id", "user_id"}
    ),
    indexes = {
        @Index(name = "idx_rating_exercise", columnList = "exercise_id"),
        @Index(name = "idx_rating_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @CreationTimestamp
    @Column(name = "rated_at", nullable = false, updatable = false)
    private LocalDateTime ratedAt;
}