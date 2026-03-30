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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Full package details with content items")
public class PackageDetailResponse {

    @Schema(description = "Package ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Package name")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Full description")
    @JsonProperty("description")
    private String description;

    @Schema(description = "URL-friendly slug")
    @JsonProperty("slug")
    private String slug;

    @Schema(description = "Package type")
    @JsonProperty("packageType")
    private PackageType packageType;

    @Schema(description = "Publication status")
    @JsonProperty("status")
    private PackageStatus status;

    @Schema(description = "Is free?")
    @JsonProperty("isFree")
    private Boolean isFree;

    @Schema(description = "Price (null if free)")
    @JsonProperty("price")
    private BigDecimal price;

    @Schema(description = "Currency code")
    @JsonProperty("currency")
    private String currency;

    @Schema(description = "Current version (semver)")
    @JsonProperty("version")
    private String version;

    @Schema(description = "Changelog from last update")
    @JsonProperty("changelog")
    private String changelog;

    @Schema(description = "Minimum subscription required")
    @JsonProperty("requiresSubscription")
    private SubscriptionType requiresSubscription;

    @Schema(description = "Download count")
    @JsonProperty("downloadCount")
    private Integer downloadCount;

    @Schema(description = "Average rating")
    @JsonProperty("rating")
    private Double rating;

    @Schema(description = "Number of ratings")
    @JsonProperty("ratingCount")
    private Integer ratingCount;

    @Schema(description = "Thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Search tags (JSON array string)")
    @JsonProperty("tags")
    private String tags;

    @Schema(description = "Creator info (null = official)")
    @JsonProperty("createdBy")
    private CreatorInfo createdBy;

    @Schema(description = "List of items in this package")
    @JsonProperty("items")
    private List<PackageItemResponse> items;

    @Schema(description = "Indicates if current user can edit this package")
    @JsonProperty("canEdit")
    private Boolean canEdit;

    @Schema(description = "Indicates if current user can see this package")
    @JsonProperty("canAccess")
    private Boolean canAccess;

    @Schema(description = "Indicates if current user has purchased/downloaded this package")
    @JsonProperty("isPurchased")
    private Boolean isPurchased;

    @Schema(description = "Creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Creator information")
    public static class CreatorInfo {
        @Schema(description = "Creator user ID")
        @JsonProperty("id")
        private Long id;

        @Schema(description = "Creator username")
        @JsonProperty("username")
        private String username;

        @Schema(description = "Creator reputation score")
        @JsonProperty("reputationScore")
        private Integer reputationScore;
    }
}