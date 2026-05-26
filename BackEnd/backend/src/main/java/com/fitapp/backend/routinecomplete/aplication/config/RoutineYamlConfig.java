package com.fitapp.backend.routinecomplete.aplication.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class RoutineYamlConfig {
    
    private String name;
    private String description;
    private Integer sessions_per_week;
    private String goal;
    private Set<String> training_days;
    private List<ExerciseConfig> exercises;
    
    @Data
    @NoArgsConstructor
    public static class ExerciseConfig {
        private String name;
        private String day;
        private Integer position;
        private Integer rest_after_exercise;
        private List<SetConfig> sets;
    }
    
    @Data
    @NoArgsConstructor
    public static class SetConfig {
        private Integer position;
        private String type;
        private Integer rest_after_set;
        private List<ParameterConfig> parameters;
    }
    
    @Data
    @NoArgsConstructor
    public static class ParameterConfig {
        private String name;
        private Object value;  // Puede ser Integer, Double, Long, String
    }
}