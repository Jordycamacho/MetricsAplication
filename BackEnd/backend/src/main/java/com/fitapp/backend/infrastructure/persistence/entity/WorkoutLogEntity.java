package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "workout_logs", indexes = {
    @Index(name = "idx_workout_user_date", columnList = "user_id, start_time")
})
public class WorkoutLogEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private RoutineEntity routine;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "performance_score")
    private Integer performanceScore;
    
    @Column(name = "total_volume")
    private Double totalVolume;
    
    @Formula("EXTRACT(EPOCH FROM (end_time - start_time))")
    private Long durationSeconds;
}
