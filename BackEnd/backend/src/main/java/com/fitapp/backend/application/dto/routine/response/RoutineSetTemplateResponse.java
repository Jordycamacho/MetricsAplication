package com.fitapp.backend.application.dto.routine.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutineSetTemplateResponse {
    private Long id;
    private Integer position;
    private Integer subSetNumber;
    private String groupId;
    private String setType;
    private Integer restAfterSet;
    private List<RoutineSetParameterResponse> parameters;
}