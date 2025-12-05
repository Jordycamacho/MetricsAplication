package com.fitapp.backend.infrastructure.persistence.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "personal_records", indexes = {
    @Index(name = "idx_record_user_exercise", columnList = "user_id, exercise_id")
})
public class PersonalRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;
    
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
