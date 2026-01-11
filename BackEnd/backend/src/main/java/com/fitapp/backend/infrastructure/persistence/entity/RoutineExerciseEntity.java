package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "routine_exercises")
public class RoutineExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RoutineEntity routine;

    @ManyToOne
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private Integer position;

    //Para separar por sesión
    @Column(name = "session_number", nullable = false)
    private Integer sessionNumber = 1; // 1, 2, 3... basado en sessionsPerWeek
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek; // Día específico (opcional)

    @Column(name = "session_order")
    private Integer sessionOrder; // Orden dentro de la sesión

    // Descanso después del ejercicio
    private Integer restAfterExercise; // segundos

    // Objetivos generales del ejercicio (opcional)
    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL)
    private List<RoutineExerciseParameterEntity> targetParameters;

    // Sets planificados
    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private List<RoutineSetTemplateEntity> sets;
}
