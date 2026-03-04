package com.fitapp.backend.application.dto.RoutineSetTemplate.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


@Data
public class BulkUpdateSetParametersRequest {

    @NotEmpty(message = "setResults must not be empty")
    @Valid
    @JsonProperty("setResults")
    private List<SetResultRequest> setResults;

    @Data
    public static class SetResultRequest {

        @NotNull(message = "setTemplateId is required")
        @JsonProperty("setTemplateId")
        private Long setTemplateId;

        @NotEmpty(message = "parameters must not be empty")
        @Valid
        @JsonProperty("parameters")
        private List<ParameterResultRequest> parameters;
    }

    @Data
    public static class ParameterResultRequest {

        @NotNull(message = "parameterId is required")
        @JsonProperty("parameterId")
        private Long parameterId;

        @JsonProperty("repetitions")
        private Integer repetitions;

        @JsonProperty("numericValue")
        private Double  numericValue;

        @JsonProperty("durationValue")
        private Long    durationValue;

        @JsonProperty("integerValue")
        private Integer integerValue;
    }
}