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
    
    // Helper methods
    public boolean isCompleted() {
        return endTime != null;
    }
    
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    public void calculateTotalVolume() {
        this.totalVolume = exercises.stream()
            .flatMap(ex -> ex.getSets().stream())
            .mapToDouble(SetExecutionModel::calculateVolume)
            .sum();
    }
}