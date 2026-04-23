package com.fitapp.backend.workout.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Last workout values for a specific exercise")
public class LastExerciseValuesResponse {

    @JsonProperty("exerciseId")
    @Schema(description = "ID of the exercise", example = "789")
    private Long exerciseId;

    @JsonProperty("exerciseName")
    @Schema(description = "Name of the exercise", example = "Bench Press")
    private String exerciseName;

    @JsonProperty("lastWorkoutDate")
    @Schema(description = "When this exercise was last performed")
    private LocalDateTime lastWorkoutDate;

    @JsonProperty("sessionId")
    @Schema(description = "ID of the workout session", example = "123")
    private Long sessionId;

    @Builder.Default
    @JsonProperty("sets")
    @Schema(description = "Last set values performed")
    private List<LastSetValueResponse> sets = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Last values for a specific set")
    public static class LastSetValueResponse {

        @JsonProperty("position")
        @Schema(description = "Set position/number", example = "1")
        private Integer position;

        @JsonProperty("setType")
        @Schema(description = "Type of set", example = "NORMAL")
        private String setType;

        @Builder.Default
        @JsonProperty("parameters")
        @Schema(description = "Parameter values from last execution")
        private List<ParameterValue> parameters = new ArrayList<>();

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Parameter value from last execution")
        public static class ParameterValue {

            @JsonProperty("parameterId")
            @Schema(description = "ID of the parameter", example = "1")
            private Long parameterId;

            @JsonProperty("parameterName")
            @Schema(description = "Name of the parameter", example = "Weight")
            private String parameterName;

            @JsonProperty("parameterType")
            @Schema(description = "Type of parameter", example = "NUMBER")
            private String parameterType;

            @JsonProperty("unit")
            @Schema(description = "Unit of measurement", example = "kg")
            private String unit;

            @JsonProperty("numericValue")
            @Schema(description = "Numeric value", example = "80.0")
            private Double numericValue;

            @JsonProperty("integerValue")
            @Schema(description = "Integer value", example = "12")
            private Integer integerValue;

            @JsonProperty("durationValue")
            @Schema(description = "Duration in milliseconds", example = "60000")
            private Long durationValue;

            @JsonProperty("stringValue")
            @Schema(description = "Text value")
            private String stringValue;
        }
    }
}