package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineSetTemplateModel {
    private Long id;
    private Long routineExerciseId;
    private Integer position;
    private Integer subSetNumber;
    private String groupId;
    private String setType;
    private Integer restAfterSet;
    private List<RoutineSetParameterModel> parameters;

    public void validate() {
        if (position == null || position < 0) {
            throw new IllegalArgumentException("Position must be a positive integer");
        }
        if (setType == null) {
            setType = "NORMAL";
        }
        if (subSetNumber == null) {
            subSetNumber = 1;
        }
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
    }

    public void logModelData(String operation) {
        log.debug("RoutineSetTemplateModel {} | id={} | routineExerciseId={} | position={} | "
                + "subSetNumber={} | groupId={} | setType={} | restAfterSet={} | paramCount={}",
                operation, id, routineExerciseId, position, subSetNumber,
                groupId, setType, restAfterSet,
                parameters != null ? parameters.size() : 0);
    }
}