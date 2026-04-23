package com.fitapp.backend.routinecomplete.package_.aplication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Package statistics")
public class PackageStatisticsResponse {

    @Schema(description = "Package ID")
    @JsonProperty("packageId")
    private Long packageId;

    @Schema(description = "Total downloads")
    @JsonProperty("totalDownloads")
    private Integer totalDownloads;

    @Schema(description = "Total purchases (paid packs)")
    @JsonProperty("totalPurchases")
    private Integer totalPurchases;

    @Schema(description = "Average rating")
    @JsonProperty("averageRating")
    private Double averageRating;

    @Schema(description = "Total ratings count")
    @JsonProperty("totalRatings")
    private Integer totalRatings;

    @Schema(description = "Number of items in package")
    @JsonProperty("itemCount")
    private Integer itemCount;

    @Schema(description = "Download trend (last 30 days)")
    @JsonProperty("downloadsLast30Days")
    private Integer downloadsLast30Days;

    @Schema(description = "Revenue generated (for paid packs)")
    @JsonProperty("revenueGenerated")
    private BigDecimal revenueGenerated;

    @Schema(description = "Last updated timestamp")
    @JsonProperty("lastUpdated")
    private LocalDateTime lastUpdated;
}