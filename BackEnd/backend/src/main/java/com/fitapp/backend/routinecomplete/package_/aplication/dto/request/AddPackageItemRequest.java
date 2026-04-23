package com.fitapp.backend.routinecomplete.package_.aplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add an item to a package")
public class AddPackageItemRequest {
 
    @Schema(description = "Type of content: SPORT, PARAMETER, ROUTINE, EXERCISE, CATEGORY")
    @NotNull(message = "Item type is required")
    @JsonProperty("itemType")
    private String itemType;
 
    @Schema(description = "Sport ID (if itemType=SPORT)")
    @JsonProperty("sportId")
    private Long sportId;
 
    @Schema(description = "Parameter ID (if itemType=PARAMETER)")
    @JsonProperty("parameterId")
    private Long parameterId;
 
    @Schema(description = "Routine ID (if itemType=ROUTINE)")
    @JsonProperty("routineId")
    private Long routineId;
 
    @Schema(description = "Exercise ID (if itemType=EXERCISE)")
    @JsonProperty("exerciseId")
    private Long exerciseId;
 
    @Schema(description = "Category ID (if itemType=CATEGORY)")
    @JsonProperty("categoryId")
    private Long categoryId;

    @Schema(description = "Display order (auto-assigned if null)")
    @JsonProperty("displayOrder")
    private Integer displayOrder;

    @Schema(description = "Creator notes about this item")
    @Size(max = 500, message = "Notes must be max 500 characters")
    @JsonProperty("notes")
    private String notes;
}
