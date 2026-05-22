package com.fitapp.backend.routinecomplete.aplication.dto.routine.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineStatisticsResponse {
    @JsonProperty("totalRoutines")
    private long totalRoutines;
    
    @JsonProperty("activeRoutines")
    private long activeRoutines;
    
    @JsonProperty("inactiveRoutines")
    private long inactiveRoutines;
}