package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Table;
import java.time.LocalDate;
import jakarta.persistence.Id;
import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "exercise_metrics", indexes = {
        @Index(name = "idx_exercise_metric_user", columnList = "user_id"),
        @Index(name = "idx_exercise_metric_date", columnList = "date"),
        @Index(name = "idx_metric_user_period", columnList = "user_id, period")
})
public class ExerciseMetricEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "avg_value")
    private Double avgValue;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MetricPeriod period;

    @Column(name = "pr_delta")
    private Double prDelta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id") 
    private CustomParameterEntity parameter;
}