package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_sets")
public class ExerciseSetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_exercise_id")
    private SessionExerciseEntity sessionExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "set_type", nullable = false)
    private SetType setType; // NORMAL, DROP_SET, SUPER_SET, GIANT_SET, PYRAMID

    // Grupo para sets especiales (ej: series descendentes)
    @Column(name = "group_id")
    private String groupId;

    @Column(name = "sub_set_number")
    private Integer subSetNumber;

    // Múltiples valores por set
    @OneToMany(mappedBy = "exerciseSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SetParameterValueEntity> parameterValues;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Estado del set
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SetStatus status; // PLANNED, IN_PROGRESS, COMPLETED, SKIPPED

    // Notas del set
    @Column(name = "notes", length = 1000)
    private String notes;
}