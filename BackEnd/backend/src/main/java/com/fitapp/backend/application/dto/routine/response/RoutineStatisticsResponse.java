package com.fitapp.backend.application.dto.routine.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineStatisticsResponse {
    private long totalRoutines;
    private long activeRoutines;
    private long inactiveRoutines;
}