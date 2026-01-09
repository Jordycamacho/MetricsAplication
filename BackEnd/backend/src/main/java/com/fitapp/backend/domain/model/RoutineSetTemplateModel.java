package com.fitapp.backend.domain.model;

import lombok.Data;
import java.util.List;

@Data
public class RoutineSetTemplateModel {
    private Long id;
    private Integer position;
    private Integer subSetNumber;
    private String groupId;
    private String setType;
    private Integer restAfterSet;
    private List<RoutineSetParameterModel> parameters;
}