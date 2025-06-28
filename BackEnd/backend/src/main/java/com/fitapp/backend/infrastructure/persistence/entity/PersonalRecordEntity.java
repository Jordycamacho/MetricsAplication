package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDate;

import com.fitapp.backend.infrastructure.persistence.entity.enums.MetricPeriod;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "personal_records", indexes = {
    @Index(name = "idx_record_user_exercise", columnList = "user_id, exercise_id"),
    @Index(name = "idx_record_type", columnList = "parameter_type")
})
public class PersonalRecordEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false)
    private ParameterType parameterType;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "previous_record")
    private Double previousRecord;
    
    @Column(name = "progress_percentage")
    private Double progressPercentage;
    
    @Enumerated(EnumType.STRING)
    private MetricPeriod period;
}
