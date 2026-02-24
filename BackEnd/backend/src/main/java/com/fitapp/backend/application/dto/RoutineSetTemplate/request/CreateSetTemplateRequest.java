package com.fitapp.backend.application.dto.RoutineSetTemplate.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class CreateSetTemplateRequest {

    @NotNull(message = "Routine exercise ID is required")
    @JsonProperty("routineExerciseId")
    private Long routineExerciseId;

    @NotNull(message = "Position is required")
    @JsonProperty("position")
    private Integer position;

    @JsonProperty("subSetNumber")
    private Integer subSetNumber;

    @JsonProperty("groupId")
    private String groupId;

    @JsonProperty("setType")
    private String setType;

    @JsonProperty("restAfterSet")
    private Integer restAfterSet;

    @JsonProperty("parameters")
    private List<SetParameterRequest> parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetParameterRequest {

        @NotNull
        @JsonProperty("parameterId")
        private Long parameterId;

        @JsonProperty("numericValue")
        private Double numericValue;

        @JsonProperty("durationValue")
        private Long durationValue;

        @JsonProperty("integerValue")
        private Integer integerValue;

        @JsonProperty("repetitions")
        private Integer repetitions;

        public boolean hasValue() {
            return numericValue != null || durationValue != null
                    || integerValue != null || repetitions != null;
        }
    }

    public void validate() {
        log.debug("VALIDATE_CREATE_SET_TEMPLATE_REQUEST | routineExerciseId={} | position={} | setType={}",
                routineExerciseId, position, setType);

        if (setType != null) {
            try {
                SetType.valueOf(setType);
                log.debug("SET_TYPE_VALID | setType={}", setType);
            } catch (IllegalArgumentException e) {
                log.warn("INVALID_SET_TYPE | setType={}", setType);
                throw new IllegalArgumentException("Invalid set type: " + setType);
            }
        }

        if (parameters == null) {
            parameters = new ArrayList<>();
            log.debug("PARAMETERS_INITIALIZED_EMPTY | routineExerciseId={}", routineExerciseId);
        }

        log.debug("VALIDATE_CREATE_SET_TEMPLATE_REQUEST_OK | parameterCount={}", parameters.size());
    }

    public void logRequestData() {
        log.info("CREATE_SET_TEMPLATE_REQUEST | routineExerciseId={} | position={} | setType={} | "
                + "restAfterSet={} | parameterCount={}",
                routineExerciseId, position, setType, restAfterSet,
                parameters != null ? parameters.size() : 0);

        if (parameters != null) {
            parameters.forEach(p -> log.debug(
                    "  PARAMETER | parameterId={} | repetitions={} | integerValue={} | "
                            + "numericValue={} | durationValue={}",
                    p.getParameterId(), p.getRepetitions(), p.getIntegerValue(),
                    p.getNumericValue(), p.getDurationValue()));
        }
    }
}