package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "custom_parameters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_parameter_name_owner", columnNames = { "name", "owner_id" })
}, indexes = {
        @Index(name = "idx_parameter_owner_id", columnList = "owner_id"),
        @Index(name = "idx_parameter_global_active", columnList = "is_global, is_active"),
        @Index(name = "idx_parameter_type", columnList = "parameter_type"),
        @Index(name = "idx_parameter_favorite", columnList = "is_favorite"),
        @Index(name = "idx_parameter_created_at", columnList = "created_at"),
        @Index(name = "idx_parameter_usage_count", columnList = "usage_count")
})
@Slf4j
public class CustomParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false, length = 50)
    private ParameterType parameterType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "is_global", nullable = false)
    @Builder.Default
    private Boolean isGlobal = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private UserEntity owner;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @PrePersist
    protected void onCreate() {
        log.debug("CUSTOM_PARAMETER_CREATING | name={} | type={} | ownerId={}",
                name, parameterType, owner != null ? owner.getId() : "null");
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        logDataFormat();
    }

    @PreUpdate
    protected void onUpdate() {
        log.debug("CUSTOM_PARAMETER_UPDATING | id={} | name={}", id, name);
        updatedAt = LocalDateTime.now();
        logDataFormat();
    }

    private void logDataFormat() {
        if (name != null && !name.matches("^[a-z]+([A-Z][a-z]*)*$")) {
            log.warn("PARAMETER_NAME_FORMAT | name={} | format may cause frontend issues", name);
        }

    }

    public void incrementUsage() {
        this.usageCount++;
        log.debug("PARAMETER_USAGE_INCREMENTED | id={} | count={}", id, usageCount);
    }
}