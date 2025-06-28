package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "routines", indexes = {
    @Index(name = "idx_routine_user", columnList = "user_id"),
    @Index(name = "idx_routine_sport", columnList = "sport_id")
})
public class RoutineEntity extends BaseEntity {
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;
    
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "position")
    @BatchSize(size = 15)
    private List<RoutineExerciseEntity> exercises = new ArrayList<>();
}