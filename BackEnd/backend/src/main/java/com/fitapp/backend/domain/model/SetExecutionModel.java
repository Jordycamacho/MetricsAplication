package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetExecutionStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    // Helper methods
    public boolean isCompleted() {
        return status == SetExecutionStatus.COMPLETED;
    }
    
    public Long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return null;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
    
    /**
     * Calcula el volumen de este set (peso × reps).
     * Solo aplica si tiene parámetros de peso y repeticiones.
     */
    public double calculateVolume() {
        Double weight = getWeightValue();
        Integer reps = getRepetitions();
        
        if (weight != null && reps != null) {
            return weight * reps;
        }
        return 0.0;
    }
    
    private Double getWeightValue() {
        return parameters.stream()
            .filter(p -> p.getNumericValue() != null)
            .map(SetExecutionParameterModel::getNumericValue)
            .findFirst()
            .orElse(null);
    }
    
    private Integer getRepetitions() {
        return parameters.stream()
            .filter(p -> p.getIntegerValue() != null)
            .map(SetExecutionParameterModel::getIntegerValue)
            .findFirst()
            .orElse(null);
    }
}