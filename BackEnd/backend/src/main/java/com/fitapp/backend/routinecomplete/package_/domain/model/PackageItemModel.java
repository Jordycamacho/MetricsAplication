package com.fitapp.backend.routinecomplete.package_.domain.model;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageItemModel {

    private Long id;
    private Long packageId;
    private String itemType;  // SPORT, PARAMETER, ROUTINE, EXERCISE, CATEGORY
    private Long sportId;
    private Long parameterId;
    private Long routineId;
    private Long exerciseId;
    private Long categoryId;
    private Integer displayOrder;
    private String notes;

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        int populated = 0;
        if (sportId != null) populated++;
        if (parameterId != null) populated++;
        if (routineId != null) populated++;
        if (exerciseId != null) populated++;
        if (categoryId != null) populated++;

        if (populated == 0) {
            errors.add("Must specify exactly one content type (sport, parameter, routine, exercise, or category)");
        } else if (populated > 1) {
            errors.add("Cannot specify multiple content types");
        }

        return errors;
    }

    public Long getContentId() {
        if (sportId != null) return sportId;
        if (parameterId != null) return parameterId;
        if (routineId != null) return routineId;
        if (exerciseId != null) return exerciseId;
        if (categoryId != null) return categoryId;
        throw new IllegalStateException("No content ID populated");
    }

    public String getNormalizedItemType() {
        if (sportId != null) return "SPORT";
        if (parameterId != null) return "PARAMETER";
        if (routineId != null) return "ROUTINE";
        if (exerciseId != null) return "EXERCISE";
        if (categoryId != null) return "CATEGORY";
        return itemType;
    }
}