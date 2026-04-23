package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.routinecomplete.routineexercise.infrastructure.persistence.entity.RoutineExerciseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import java.util.List;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routine_set_templates", indexes = {
        @Index(name = "idx_set_template_routine_exercise", columnList = "routine_exercise_id"),
        @Index(name = "idx_set_template_position", columnList = "routine_exercise_id, position"),
        @Index(name = "idx_set_template_group", columnList = "routine_exercise_id, groupId")
})
public class RoutineSetTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false)
    private RoutineExerciseEntity routineExercise;

    private Integer position; // Set visual (1,2,3)
    private Integer subSetNumber; // Drop interno
    private String groupId; // DROP_1, CLUSTER_A, etc.

    @Enumerated(EnumType.STRING)
    private SetType setType;

    private Integer restAfterSet; // opcional

    @OneToMany(mappedBy = "setTemplate", cascade = CascadeType.ALL)
    private List<RoutineSetParameterEntity> parameters;
}
