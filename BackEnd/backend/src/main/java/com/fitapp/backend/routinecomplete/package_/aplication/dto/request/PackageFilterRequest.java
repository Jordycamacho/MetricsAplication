package com.fitapp.backend.routinecomplete.package_.aplication.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filters for marketplace search")
public class PackageFilterRequest {
 
    @Schema(description = "Search in name/description")
    @JsonProperty("search")
    private String search;
 
    @Schema(description = "Filter by package type")
    @JsonProperty("packageType")
    private String packageType;
 
    @Schema(description = "Filter by free/paid")
    @JsonProperty("isFree")
    private Boolean isFree;
 
    @Schema(description = "Filter by minimum subscription requirement")
    @JsonProperty("requiresSubscription")
    private String requiresSubscription;
 
    @Schema(description = "Filter by rating (minimum)")
    @JsonProperty("minRating")
    private Double minRating;
 
    @Schema(description = "Filter by creator (user ID, null = official packs)")
    @JsonProperty("createdByUserId")
    private Long createdByUserId;

    @Builder.Default
    @Schema(description = "Sort field: createdAt, rating, downloadCount, name")
    @JsonProperty("sortBy")
    private String sortBy = "createdAt";
 
    @Builder.Default
    @Schema(description = "Sort direction: ASC, DESC")
    @JsonProperty("sortDirection")
    private String sortDirection = "DESC";
}