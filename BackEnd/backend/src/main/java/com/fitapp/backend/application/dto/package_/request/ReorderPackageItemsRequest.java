package com.fitapp.backend.application.dto.package_.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to reorder package items")
public class ReorderPackageItemsRequest {
 
    @NotEmpty(message = "Item order list cannot be empty")
    @Schema(description = "List of item IDs in desired order", example = "[5, 2, 8, 1]")
    @JsonProperty("itemIds")
    private List<Long> itemIds;
}
 
