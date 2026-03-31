package com.fitapp.backend.application.dto.package_.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageType;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
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
@Schema(description = "Marketplace package summary")
public class PackageSummaryResponse {

    @Schema(description = "Package ID", example = "1")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Package name", example = "Powerlifting Starter")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Short description")
    @JsonProperty("description")
    private String description;

    @Schema(description = "URL-friendly slug", example = "powerlifting-starter-v1")
    @JsonProperty("slug")
    private String slug;

    @Schema(description = "Package type", example = "ROUTINE_PACK")
    @JsonProperty("packageType")
    private PackageType packageType;

    @Schema(description = "Publication status", example = "PUBLISHED")
    @JsonProperty("status")
    private PackageStatus status;

    @Schema(description = "Is free?", example = "false")
    @JsonProperty("isFree")
    private Boolean isFree;

    @Schema(description = "Price (null if free)")
    @JsonProperty("price")
    private BigDecimal price;

    @Schema(description = "Currency code", example = "USD")
    @JsonProperty("currency")
    private String currency;

    @Schema(description = "Current version", example = "1.0.0")
    @JsonProperty("version")
    private String version;

    @Schema(description = "Minimum subscription required", example = "STANDARD")
    @JsonProperty("requiresSubscription")
    private SubscriptionType requiresSubscription;

    @Schema(description = "Number of downloads", example = "342")
    @JsonProperty("downloadCount")
    private Integer downloadCount;

    @Schema(description = "Average rating", example = "4.5")
    @JsonProperty("rating")
    private Double rating;

    @Schema(description = "Number of ratings", example = "25")
    @JsonProperty("ratingCount")
    private Integer ratingCount;

    @Schema(description = "Thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Search tags")
    @JsonProperty("tags")
    private String tags;

    @Schema(description = "Creator name (null = official FitApp pack)")
    @JsonProperty("createdByName")
    private String createdByName;

    @Schema(description = "Creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @Schema(description = "Number of items in package", example = "8")
    @JsonProperty("itemCount")
    private Integer itemCount;

    @Schema(description = "Creator username (alias para createdByName)")
    @JsonProperty("creatorUsername")
    private String creatorUsername;

    @Schema(description = "Creator ID")
    @JsonProperty("creatorId")
    private Long creatorId;
}