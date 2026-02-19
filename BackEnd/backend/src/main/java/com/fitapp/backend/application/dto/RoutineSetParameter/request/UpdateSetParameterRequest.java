package com.fitapp.backend.application.dto.RoutineSetParameter.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSetParameterRequest {

    @JsonProperty("id")
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
