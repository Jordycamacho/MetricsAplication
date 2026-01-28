package com.fitapp.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
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
        System.out.println("RoutineSetTemplateModel " + operation + ":");
        System.out.println("ID: " + id);
        System.out.println("Routine Exercise ID: " + routineExerciseId);
        System.out.println("Position: " + position);
        System.out.println("Sub Set Number: " + subSetNumber);
        System.out.println("Group ID: " + groupId);
        System.out.println("Set Type: " + setType);
        System.out.println("Rest After Set: " + restAfterSet);
        System.out.println("Parameters Count: " + (parameters != null ? parameters.size() : 0));
    }
}