package com.fitapp.backend.workout.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetExecutionModel {

    private Long id;
    private Long sessionExerciseId;
    private Long setTemplateId;
    private Integer position;
    private SetType setType;

    @Builder.Default
    private SetExecutionStatus status = SetExecutionStatus.COMPLETED;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer actualRestSeconds;
    private String notes;

    @Builder.Default
    private List<SetExecutionParameterModel> parameters = new ArrayList<>();

    public boolean isCompleted() {
        return status == SetExecutionStatus.COMPLETED;
    }

    public Long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return null;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }

    /**
     * Calcula el volumen de este set (peso × reps).
     * Toma el primer parámetro numeric (peso) y el primer parámetro integer (reps).
     * Devuelve 0.0 si no hay suficientes datos.
     *
     * <p>NOTA: Este cálculo es una heurística basada en el tipo de dato. Si en el futuro
     * los parámetros tienen un campo semántico (e.g. WEIGHT, REPS), usar ese campo
     * para mayor precisión.</p>
     */
    public double calculateVolume() {
        if (parameters == null || parameters.isEmpty()) return 0.0;

        Double weight = parameters.stream()
                .filter(p -> p.getNumericValue() != null)
                .map(SetExecutionParameterModel::getNumericValue)
                .findFirst()
                .orElse(null);

        Integer reps = parameters.stream()
                .filter(p -> p.getIntegerValue() != null)
                .map(SetExecutionParameterModel::getIntegerValue)
                .findFirst()
                .orElse(null);

        if (weight != null && reps != null) {
            return weight * reps;
        }
        return 0.0;
    }
}