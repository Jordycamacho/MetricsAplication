package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_categories", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_category_name_owner",
               columnNames = {"name", "owner_id", "is_predefined"}
           )
       },
       indexes = {
           @Index(name = "idx_category_predefined", columnList = "is_predefined"),
           @Index(name = "idx_category_active", columnList = "is_active"),
           @Index(name = "idx_category_owner", columnList = "owner_id"),
           @Index(name = "idx_category_sport", columnList = "sport_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Slf4j
public class ExerciseCategoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_predefined", nullable = false)
    @Builder.Default
    private Boolean isPredefined = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        log.debug("EXERCISE_CATEGORY_CREATING | name={} | isPredefined={} | ownerId={} | isPublic={}", 
                 name, isPredefined, owner != null ? owner.getId() : "null", isPublic);
        
        // Validar reglas de negocio
        if (isPredefined && owner != null) {
            log.error("EXERCISE_CATEGORY_VALIDATION | Predefined category cannot have an owner");
            throw new IllegalStateException("Predefined category cannot have an owner");
        }
        
        if (!isPredefined && owner == null) {
            log.error("EXERCISE_CATEGORY_VALIDATION | Personal category must have an owner");
            throw new IllegalStateException("Personal category must have an owner");
        }
        
        if (isPredefined) {
            isPublic = true; // Categorías predefinidas siempre son públicas
        }
    }

    @PreUpdate
    protected void onUpdate() {
        log.debug("EXERCISE_CATEGORY_UPDATING | id={} | name={}", id, name);
        
        // Validar que no se modifiquen campos protegidos
        if (isPredefined) {
            if (Boolean.FALSE.equals(isPublic)) {
                log.warn("EXERCISE_CATEGORY_PROTECTED_FIELD | Predefined category cannot be set to private");
                isPublic = true;
            }
        }
    }

    public void incrementUsage() {
        this.usageCount++;
        log.debug("CATEGORY_USAGE_INCREMENTED | id={} | count={}", id, usageCount);
    }
}