package com.fitapp.backend.domain.model.package_;

import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageType;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"items"})
public class PackageModel {

    private Long id;
    private String name;
    private String description;
    private String slug;
    private PackageType packageType;
    private PackageStatus status;
    private boolean isFree;
    private BigDecimal price;
    private String currency;
    private String version;
    private String changelog;
    private SubscriptionType requiresSubscription;
    private Integer downloadCount;
    private Double rating;
    private Integer ratingCount;
    private String thumbnailUrl;
    private String tags;
    private Long createdByUserId; 
    private List<PackageItemModel> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public List<String> validateForPublishing() {
        List<String> errors = new ArrayList<>();

        if (status != PackageStatus.DRAFT) {
            errors.add("Package must be in DRAFT status to publish");
        }

        if (name == null || name.isBlank()) {
            errors.add("Package name is required");
        }

        if (items == null || items.isEmpty()) {
            errors.add("Package must have at least one item");
        }

        if (!isFree && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            errors.add("Paid packages must have a valid price > 0");
        }

        return errors;
    }

    public boolean validatePrice() {
        if (isFree) {
            return true;
        }
        return price != null && price.compareTo(BigDecimal.ZERO) > 0 && currency != null && !currency.isBlank();
    }

    public List<String> validateItems() {
        List<String> errors = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            errors.add("Package must have at least one item");
            return errors;
        }

        for (int i = 0; i < items.size(); i++) {
            PackageItemModel item = items.get(i);
            List<String> itemErrors = item.validate();
            if (!itemErrors.isEmpty()) {
                errors.add("Item " + (i + 1) + ": " + String.join(", ", itemErrors));
            }
        }

        return errors;
    }

    public boolean isAccessibleByTier(SubscriptionType userTier) {
        if (status != PackageStatus.PUBLISHED) {
            return false; 
        }

        if (requiresSubscription == SubscriptionType.FREE) {
            return true;
        }
        if (requiresSubscription == SubscriptionType.STANDARD) {
            return userTier == SubscriptionType.STANDARD || userTier == SubscriptionType.PREMIUM;
        }
        return userTier == SubscriptionType.PREMIUM;
    }


    public boolean canBeEditedBy(Long userId) {
        if (createdByUserId == null) {
            return false; 
        }
        return createdByUserId.equals(userId);
    }

    public void recordDownload() {
        if (downloadCount == null) {
            downloadCount = 0;
        }
        downloadCount++;
    }

    public void addRating(Double ratingValue) {
        if (ratingValue < 1.0 || ratingValue > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        if (rating == null) {
            rating = 0.0;
        }
        if (ratingCount == null) {
            ratingCount = 0;
        }

        double currentTotal = rating * ratingCount;
        ratingCount++;
        rating = (currentTotal + ratingValue) / ratingCount;
    }

    public Integer getNextItemDisplayOrder() {
        if (items == null || items.isEmpty()) {
            return 1;
        }
        return items.stream()
                .mapToInt(i -> i.getDisplayOrder() != null ? i.getDisplayOrder() : 0)
                .max()
                .orElse(0) + 1;
    }

    public void reorderItems(List<Long> itemIds) {
        if (items == null) {
            throw new IllegalStateException("Items list is null");
        }

        Map<Long, PackageItemModel> itemMap = new HashMap<>();
        for (PackageItemModel item : items) {
            itemMap.put(item.getId(), item);
        }

        List<PackageItemModel> reordered = new ArrayList<>();
        for (int i = 0; i < itemIds.size(); i++) {
            Long id = itemIds.get(i);
            PackageItemModel item = itemMap.get(id);
            if (item == null) {
                throw new IllegalArgumentException("Item not found: " + id);
            }
            item.setDisplayOrder(i + 1);
            reordered.add(item);
        }

        this.items = reordered;
    }

    public boolean canBeDeprecated() {
        return status == PackageStatus.PUBLISHED;
    }


    public boolean canBeSuspended() {
        return status == PackageStatus.PUBLISHED || status == PackageStatus.DRAFT;
    }
}
