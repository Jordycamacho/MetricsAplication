package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.List;
import java.util.Objects;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "routine", "exercise", "targetParameters", "sets" })
@Entity
@Table(name = "routine_exercises")
public class RoutineExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private RoutineEntity routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "session_number", nullable = false)
    private Integer sessionNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "session_order")
    private Integer sessionOrder;

    private Integer restAfterExercise;

    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL)
    private List<RoutineExerciseParameterEntity> targetParameters;

    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private List<RoutineSetTemplateEntity> sets;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RoutineExerciseEntity))
            return false;
        RoutineExerciseEntity that = (RoutineExerciseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
