package com.fitapp.backend.domain.model;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseStatus;
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
public class SessionExerciseModel {
    private Long id;
    private Long sessionId;
    private Long exerciseId;
    private ExerciseStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String personalNotes;
    
    @Builder.Default
    private List<SetExecutionModel> sets = new ArrayList<>();
    
    // Helper methods
    public boolean isCompleted() {
        return status == ExerciseStatus.COMPLETED;
    }
    
    public boolean isSkipped() {
        return status == ExerciseStatus.SKIPPED;
    }
    
    public Long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return null;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}