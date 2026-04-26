package com.fitapp.backend.workout.domain.model;

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
public class WorkoutSessionModel {

    private Long id;
    private Long routineId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer performanceScore;
    private Double totalVolume;
    private Long durationSeconds;

    @Builder.Default
    private List<SessionExerciseModel> exercises = new ArrayList<>();

    public boolean isCompleted() {
        return endTime != null;
    }

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }

    /**
     * Suma el volumen de todos los sets de todos los ejercicios.
     * Solo considera sets completados.
     */
    public void calculateTotalVolume() {
        this.totalVolume = exercises.stream()
                .flatMap(ex -> ex.getSets().stream())
                .filter(SetExecutionModel::isCompleted)
                .mapToDouble(SetExecutionModel::calculateVolume)
                .sum();
    }

    public int getTotalSetCount() {
        return exercises.stream()
                .mapToInt(ex -> ex.getSets().size())
                .sum();
    }
}