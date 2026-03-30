package com.fitapp.backend.application.dto.package_.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
 

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an existing package")
public class UpdatePackageRequest {
 
    @Size(min = 3, max = 200, message = "Name must be 3-200 characters")
    @Schema(description = "Updated package name")
    @JsonProperty("name")
    private String name;
 
    @Size(max = 2000, message = "Description must be max 2000 characters")
    @Schema(description = "Updated description")
    @JsonProperty("description")
    private String description;

    @Builder.Default
    @Schema(description = "Is this package free?")
    @JsonProperty("isFree")
    private Boolean isFree = null;
 
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be > 0 if not free")
    @DecimalMax(value = "9999.99", message = "Price must be <= 9999.99")
    @Schema(description = "Updated price")
    @JsonProperty("price")
    private BigDecimal price;
 
    @Size(min = 3, max = 3, message = "Currency must be ISO 4217 (3 chars)")
    @Schema(description = "Updated currency")
    @JsonProperty("currency")
    private String currency;
 
    @Schema(description = "Minimum subscription tier required")
    @JsonProperty("requiresSubscription")
    private SubscriptionType requiresSubscription;
 
    @Size(max = 512, message = "Thumbnail URL must be max 512 characters")
    @Schema(description = "Updated thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Updated search tags")
    @JsonProperty("tags")
    private String tags;

    @Schema(description = "Changelog entry describing what changed in this version")
    @Size(max = 1000, message = "Changelog must be max 1000 characters")
    @JsonProperty("changelog")
    private String changelog;
}