package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routine_set_templates")
public class RoutineSetTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RoutineExerciseEntity routineExercise;

    private Integer position;        // Set visual (1,2,3)
    private Integer subSetNumber;     // Drop interno
    private String groupId;           // DROP_1, CLUSTER_A, etc.

    @Enumerated(EnumType.STRING)
    private SetType setType;

    private Integer restAfterSet;     // opcional

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL)
    private List<RoutineSetParameterEntity> parameters;
}
