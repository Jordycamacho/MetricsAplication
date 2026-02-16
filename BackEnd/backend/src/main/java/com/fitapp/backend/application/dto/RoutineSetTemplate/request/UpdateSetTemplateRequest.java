package com.fitapp.backend.application.dto.RoutineSetTemplate.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        
        @JsonProperty("minValue")
        private Double minValue;
        
        @JsonProperty("maxValue")
        private Double maxValue;
    }
    
    public void validate() {
        if (setType != null) {
            try {
                SetType.valueOf(setType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid set type: " + setType);
            }
        }
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
    }
}