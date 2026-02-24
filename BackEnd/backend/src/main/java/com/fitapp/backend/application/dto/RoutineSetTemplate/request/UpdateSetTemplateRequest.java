package com.fitapp.backend.application.dto.RoutineSetTemplate.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UpdateSetTemplateRequest {

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

        @JsonProperty("id")
        private Long id;

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

        public boolean isNew() {
            return id == null;
        }
    }

    public void validate() {
        log.debug("VALIDATE_UPDATE_SET_TEMPLATE_REQUEST | position={} | setType={} | parameterCount={}",
                position, setType, parameters != null ? parameters.size() : 0);

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
            log.debug("PARAMETERS_INITIALIZED_EMPTY");
        }

        log.debug("VALIDATE_UPDATE_SET_TEMPLATE_REQUEST_OK | parameterCount={}", parameters.size());
    }
}