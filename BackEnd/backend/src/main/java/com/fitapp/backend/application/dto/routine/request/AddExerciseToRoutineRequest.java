package com.fitapp.backend.application.dto.routine.request;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotNull;

@Data
public class AddExerciseToRoutineRequest {
    
    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;
    
    private Integer sessionNumber = 1; // Sesión a la que pertenece
    
    private String dayOfWeek; // Día específico (opcional)

    private Integer sessionOrder; // Orden dentro de la sesión
    
    private Integer restAfterExercise; // Descanso después del ejercicio
    
    private List<ExerciseParameterRequest> targetParameters;
    
    private List<SetTemplateRequest> sets;
    
    @Data
    public static class ExerciseParameterRequest {
        @NotNull private Long parameterId;
        private Double numericValue;
        private Integer integerValue;
        private Long durationValue;
        private String stringValue;
        private Double minValue;
        private Double maxValue;
        private Double defaultValue;
    }
    
    @Data
    public static class SetTemplateRequest {
        @NotNull private Integer position;
        private String setType = "NORMAL";
        private Integer restAfterSet;
        private Integer subSetNumber;
        private String groupId;
        private List<SetParameterRequest> parameters;
    }
    
    @Data
    public static class SetParameterRequest {
        @NotNull private Long parameterId;
        private Double numericValue;
        private Long durationValue;
        private Integer integerValue;
        private Double minValue;
        private Double maxValue;
    }
}