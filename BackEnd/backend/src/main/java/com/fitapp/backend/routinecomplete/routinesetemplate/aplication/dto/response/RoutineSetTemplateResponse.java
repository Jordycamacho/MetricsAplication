package com.fitapp.backend.routinecomplete.routinesetemplate.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.routinecomplete.routineexercise.aplication.dto.response.RoutineSetParameterResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoutineSetTemplateResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("routineExerciseId")
    private Long routineExerciseId;
    
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
    private List<RoutineSetParameterResponse> parameters;
}