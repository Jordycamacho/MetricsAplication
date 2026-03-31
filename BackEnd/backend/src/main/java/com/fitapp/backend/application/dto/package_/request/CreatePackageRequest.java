package com.fitapp.backend.application.dto.package_.request;
 
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.util.List;
 

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new package")
public class CreatePackageRequest {
 
    @NotBlank(message = "Package name is required")
    @Size(min = 3, max = 200, message = "Name must be 3-200 characters")
    @Schema(description = "Package name", example = "Powerlifting Starter")
    @JsonProperty("name")
    private String name;
 
    @Size(max = 2000, message = "Description must be max 2000 characters")
    @Schema(description = "Detailed package description")
    @JsonProperty("description")
    private String description;
 
    @NotNull(message = "Package type is required")
    @Schema(description = "Type of package content", example = "ROUTINE_PACK")
    @JsonProperty("packageType")
    private String packageType;

    @Builder.Default
    @Schema(description = "Is this package free?", example = "false")
    @JsonProperty("isFree")
    private boolean isFree = true;
 
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be > 0 if not free")
    @DecimalMax(value = "9999.99", message = "Price must be <= 9999.99")
    @Schema(description = "Price in currency (null if isFree=true)", example = "29.99")
    @JsonProperty("price")
    private BigDecimal price;
 
    @Size(min = 3, max = 3, message = "Currency must be ISO 4217 (3 chars)")
    @Builder.Default
    @Schema(description = "ISO currency code", example = "USD")
    @JsonProperty("currency")
    private String currency = "USD";

    @NotNull(message = "Subscription requirement is required")
    @Builder.Default
    @Schema(description = "Minimum subscription tier required", example = "FREE")
    @JsonProperty("requiresSubscription")
    private String requiresSubscription = SubscriptionType.FREE.name();

    @Size(max = 512, message = "Thumbnail URL must be max 512 characters")
    @Schema(description = "Thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;
 
    @Schema(description = "Search tags (JSON array as string)", example = "[\"powerlifting\",\"fuerza\",\"principiante\"]")
    @JsonProperty("tags")
    private String tags;
 
    @Schema(description = "Items to add to package (can add more later)")
    @JsonProperty("initialItems")
    private List<AddPackageItemRequest> initialItems;
}