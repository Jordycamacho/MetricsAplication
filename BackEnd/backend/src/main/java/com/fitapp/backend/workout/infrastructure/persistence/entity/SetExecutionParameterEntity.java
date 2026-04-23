package com.fitapp.backend.workout.infrastructure.persistence.entity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Valor real de un parámetro en un set ejecutado.
 *
 * El sistema de parámetros es completamente abierto: el usuario puede
 * tener parámetros custom (RPE, velocidad, potencia, calidad técnica...)
 * y todos se almacenan aquí de la misma forma.
 *
 * Solo uno de los campos de valor estará poblado, según el ParameterType
 * del CustomParameterEntity referenciado.
 *
 * También marca si este valor constituye un nuevo récord personal (PR),
 * lo que permite mostrar feedback inmediato al usuario durante la sesión.
 */
@Entity
@Table(name = "set_execution_parameters",
    indexes = {
        @Index(name = "idx_sep_execution",  columnList = "set_execution_id"),
        @Index(name = "idx_sep_parameter",  columnList = "parameter_id"),
        @Index(name = "idx_sep_pr",         columnList = "set_execution_id, is_personal_record")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetExecutionParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_execution_id", nullable = false)
    private SetExecutionEntity setExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private CustomParameterEntity parameter;

    // ── Valor real (uno solo poblado según ParameterType) ────────────────────

    /** Para ParameterType: NUMBER, DISTANCE, PERCENTAGE */
    @Column(name = "numeric_value")
    private Double numericValue;

    /** Para ParameterType: INTEGER (repeticiones, RPE entero, etc.) */
    @Column(name = "integer_value")
    private Integer integerValue;

    /** Para ParameterType: DURATION (en milisegundos) */
    @Column(name = "duration_value")
    private Long durationValue;

    /** Para ParameterType: TEXT (notas de técnica, etc.) */
    @Column(name = "string_value", columnDefinition = "TEXT")
    private String stringValue;

    // ── PR ───────────────────────────────────────────────────────────────────

    /**
     * True si este valor supera el récord personal previo del usuario
     * para este parámetro en este ejercicio.
     * Se calcula en el momento de guardar el set, consultando PersonalRecordEntity.
     */
    @Column(name = "is_personal_record", nullable = false)
    @Builder.Default
    private boolean isPersonalRecord = false;

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Devuelve el valor como Double para comparaciones uniformes,
     * independientemente del tipo almacenado.
     */
    public Double getValueAsDouble() {
        if (numericValue != null) return numericValue;
        if (integerValue != null) return integerValue.doubleValue();
        if (durationValue != null) return durationValue.doubleValue();
        return null;
    }
}