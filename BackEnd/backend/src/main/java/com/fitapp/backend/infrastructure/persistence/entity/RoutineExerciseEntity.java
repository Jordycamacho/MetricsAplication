package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import lombok.*;

/**
 * Ejercicio dentro de una rutina, con toda la configuración de entrenamiento.
 *
 * CAMBIOS v2:
 *
 * CIRCUIT:
 * - circuitGroupId → "CIRCUIT_A", "CIRCUIT_B" — agrupa ejercicios del mismo
 * bloque
 * - circuitRoundCount → cuántas vueltas al circuito
 *
 * SUPER_SET / GIANT_SET:
 * - superSetGroupId → "SS_1", "GS_1" — agrupa ejercicios distintos del mismo
 * bloque
 * (diferente a groupId en RoutineSetTemplateEntity, que es para sub-sets del
 * mismo ejercicio)
 *
 * AMRAP:
 * - amrapDurationSeconds → duración total del bloque AMRAP
 *
 * EMOM:
 * - emomIntervalSeconds → duración de cada intervalo (suele ser 60s pero puede
 * variar)
 * - emomTotalRounds → número de rondas
 *
 * TABATA:
 * - tabataWorkSeconds → segundos de trabajo por intervalo (default 20)
 * - tabataRestSeconds → segundos de descanso por intervalo (default 10)
 * - tabataRounds → número de intervalos (default 8)
 *
 * TODOS:
 * - notes → instrucciones del coach para este ejercicio en concreto
 */
@Entity
@Table(name = "routine_exercises", indexes = {
        @Index(name = "idx_re_routine", columnList = "routine_id"),
        @Index(name = "idx_re_exercise", columnList = "exercise_id"),
        @Index(name = "idx_re_session", columnList = "routine_id, session_number"),
        @Index(name = "idx_re_circuit_group", columnList = "routine_id, circuit_group_id"),
        @Index(name = "idx_re_superset_group", columnList = "routine_id, super_set_group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineEntity routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private ExerciseEntity exercise;

    // ── Posicionamiento ──────────────────────────────────────────────────────

    /** Orden visual del ejercicio en la rutina. */
    @Column(name = "position", nullable = false)
    private Integer position;

    /** Número de sesión a la que pertenece (cuando hay varias sesiones/semana). */
    @Column(name = "session_number", nullable = false)
    @Builder.Default
    private Integer sessionNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    /**
     * Orden dentro de la sesión (para ordenación fina dentro del mismo
     * session_number).
     */
    @Column(name = "session_order")
    private Integer sessionOrder;

    // ── Descanso ─────────────────────────────────────────────────────────────

    /**
     * Descanso en segundos después de completar todos los sets de este ejercicio.
     */
    @Column(name = "rest_after_exercise")
    private Integer restAfterExercise;

    // ── Sets y parámetros objetivo ───────────────────────────────────────────

    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<RoutineSetTemplateEntity> sets = new ArrayList<>();

    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineExerciseParameterEntity> targetParameters = new ArrayList<>();

    // ── CIRCUIT ──────────────────────────────────────────────────────────────

    /**
     * Agrupa ejercicios del mismo circuito. Ej: "CIRCUIT_A".
     * Todos los RoutineExerciseEntity con el mismo circuitGroupId dentro
     * de la misma rutina/sesión forman un circuito.
     */
    @Column(name = "circuit_group_id", length = 50)
    private String circuitGroupId;// todavia no se usa

    /** Número de vueltas al circuito. NULL si no es CIRCUIT. */
    @Column(name = "circuit_round_count")
    private Integer circuitRoundCount;// todavia no se usa

    // ── SUPER_SET / GIANT_SET ────────────────────────────────────────────────

    /**
     * Agrupa ejercicios distintos en un superset o giant set.
     * Ej: "SS_1" (2 ejercicios), "GS_1" (3+ ejercicios).
     *
     * NOTA: distinto de groupId en RoutineSetTemplateEntity, que agrupa
     * sub-sets del MISMO ejercicio (drop sets, clusters, rest-pause).
     */
    @Column(name = "super_set_group_id", length = 50)
    private String superSetGroupId;// todavia no se usa
    // ── AMRAP ────────────────────────────────────────────────────────────────

    /**
     * Duración total del bloque AMRAP en segundos.
     * Los sets definen qué movimientos se repiten. El usuario hace
     * tantas rondas como pueda en este tiempo.
     */
    @Column(name = "amrap_duration_seconds")
    private Integer amrapDurationSeconds;// todavia no se usa

    // ── EMOM ─────────────────────────────────────────────────────────────────

    /**
     * Duración de cada intervalo EMOM en segundos.
     * Suele ser 60s pero puede ser diferente (ej: 90s EMOM).
     */
    @Column(name = "emom_interval_seconds")
    private Integer emomIntervalSeconds;// todavia no se usa

    /** Número total de intervalos EMOM. */
    @Column(name = "emom_total_rounds")
    private Integer emomTotalRounds;// todavia no se usa

    // ── TABATA ───────────────────────────────────────────────────────────────

    /** Segundos de trabajo por intervalo. Tabata clásico: 20. */
    @Column(name = "tabata_work_seconds")
    @Builder.Default
    private Integer tabataWorkSeconds = 20;// todavia no se usa

    /** Segundos de descanso por intervalo. Tabata clásico: 10. */
    @Column(name = "tabata_rest_seconds")
    @Builder.Default
    private Integer tabataRestSeconds = 10;// todavia no se usa

    /** Número de intervalos. Tabata clásico: 8. */
    @Column(name = "tabata_rounds")
    @Builder.Default
    private Integer tabataRounds = 8;// todavia no se usa

    // ── Notas ────────────────────────────────────────────────────────────────

    /** Instrucciones del coach específicas para este ejercicio en esta rutina. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // todavia no se usa

    // ── Equals / HashCode ────────────────────────────────────────────────────

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