package com.fitapp.backend.parameter.infrastructure.persistence.entity;

import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricAggregation;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Parámetro personalizado para medir cualquier variable de entrenamiento.
 *
 * CAMBIOS v2:
 * - metricAggregation → indica al sistema cómo agregar este parámetro
 * para calcular métricas. Ej: reps=SUM+MAX, peso=MAX, RPE=AVG.
 * Un parámetro puede tener múltiples agregaciones relevantes, pero
 * metricAggregation define la PRINCIPAL (para PRs y gráficos por defecto).
 *
 * - isTrackable → si el sistema debe calcular métricas automáticamente
 * para este parámetro. False para parámetros de control (notas, técnica).
 */
@Entity
@Table(name = "custom_parameters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_parameter_name_owner", columnNames = { "name", "owner_id" })
}, indexes = {
        @Index(name = "idx_parameter_owner_id", columnList = "owner_id"),
        @Index(name = "idx_parameter_global_active", columnList = "is_global, is_active"),
        @Index(name = "idx_parameter_type", columnList = "parameter_type"),
        @Index(name = "idx_parameter_trackable", columnList = "is_trackable"),
        @Index(name = "idx_parameter_favorite", columnList = "is_favorite"),
        @Index(name = "idx_parameter_usage_count", columnList = "usage_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private UserEntity owner;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

    /**
     * Cómo se agrega este parámetro para calcular la métrica principal.
     * Determina qué cuenta como "récord personal" y cómo se grafican las
     * tendencias.
     *
     * Ejemplos predefinidos:
     * repeticiones → MAX (mejor serie)
     * peso → MAX (mayor peso)
     * RPE → AVG (esfuerzo medio)
     * distancia → MAX (mejor distancia) o SUM (total recorrido)
     * duración → MAX (tiempo máximo) o MIN (menor tiempo = mejor en velocidad)
     *
     * NULL = el sistema no calcula métricas automáticas para este parámetro.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_aggregation", length = 20)
    private MetricAggregation metricAggregation;

    /**
     * Si el sistema debe trackear y calcular métricas para este parámetro.
     * False para parámetros descriptivos (notas técnicas, categoría del set, etc.)
     */
    @Column(name = "is_trackable", nullable = false)
    @Builder.Default
    private boolean isTrackable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementUsage() {
        this.usageCount++;
    }
}