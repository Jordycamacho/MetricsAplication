package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "exercises")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

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

    // Campos de auditoría
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        log.debug("EXERCISE_CREATING | name={} | type={} | sportId={} | createdBy={}", 
                 name, exerciseType, sport != null ? sport.getId() : "null", 
                 createdBy != null ? createdBy.getId() : "null");
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateRelationships();
    }

    @PreUpdate
    protected void onUpdate() {
        log.debug("EXERCISE_UPDATING | id={} | name={}", id, name);
        updatedAt = LocalDateTime.now();
        validateRelationships();
    }

    private void validateRelationships() {
        // Validar que el ejercicio tenga al menos un parámetro soportado
        if (supportedParameters == null || supportedParameters.isEmpty()) {
            log.warn("EXERCISE_NO_PARAMETERS | id={} | name={} | Exercise has no supported parameters", 
                    id, name);
        }
        
        // Validar que el tipo de ejercicio sea compatible con los parámetros
        validateExerciseTypeCompatibility();
    }

    private void validateExerciseTypeCompatibility() {
        // Lógica de validación de compatibilidad entre tipo y parámetros
        if (exerciseType != null && supportedParameters != null) {
            long weightParamCount = supportedParameters.stream()
                .filter(p -> p.getName().toLowerCase().contains("weight"))
                .count();
            
            long timeParamCount = supportedParameters.stream()
                .filter(p -> p.getName().toLowerCase().contains("time") || 
                            p.getName().toLowerCase().contains("duration"))
                .count();
            
            if (exerciseType == ExerciseType.WEIGHTED && weightParamCount == 0) {
                log.warn("EXERCISE_TYPE_MISMATCH | id={} | type=WEIGHTED but no weight parameters", id);
            }
            
            if (exerciseType == ExerciseType.TIMED && timeParamCount == 0) {
                log.warn("EXERCISE_TYPE_MISMATCH | id={} | type=TIMED but no time parameters", id);
            }
        }
    }

    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
        log.debug("EXERCISE_USAGE_INCREMENTED | id={} | count={}", id, usageCount);
    }

    public void addRating(Double newRating) {
        double totalRating = this.rating * this.ratingCount;
        this.ratingCount++;
        this.rating = (totalRating + newRating) / this.ratingCount;
        log.debug("EXERCISE_RATING_UPDATED | id={} | newRating={} | average={}", 
                 id, newRating, this.rating);
    }
}