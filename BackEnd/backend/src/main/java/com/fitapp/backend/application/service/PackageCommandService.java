package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.package_.request.*;
import com.fitapp.backend.application.dto.package_.response.*;
import com.fitapp.backend.application.ports.input.PackageCommandUseCase;
import com.fitapp.backend.application.ports.output.PackagePersistencePort;
import com.fitapp.backend.domain.model.package_.CreatorModel;
import com.fitapp.backend.domain.model.package_.PackageItemModel;
import com.fitapp.backend.domain.model.package_.PackageModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import com.fitapp.backend.infrastructure.persistence.converter.PackageConverter;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageCommandService implements PackageCommandUseCase {

    private final PackagePersistencePort persistencePort;
    private final PackageConverter packageConverter;
    private final SpringDataUserRepository userRepository;

    @Override
    @Transactional
    public PackageDetailResponse createPackage(CreatePackageRequest request, Long creatorUserId) {
        log.info("CREATE_PACKAGE | name={} | creator={}", request.getName(), creatorUserId);

        var creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + creatorUserId));

        String slug = generateUniqueSlug(request.getName());

        PackageModel packageModel = PackageModel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(slug)
                .packageType(request.getPackageType())
                .isFree(request.isFree())
                .price(request.isFree() ? null : request.getPrice())
                .currency(request.getCurrency())
                .version("1.0.0")
                .requiresSubscription(request.getRequiresSubscription())
                .status(PackageStatus.DRAFT)
                .downloadCount(0)
                .rating(null)
                .ratingCount(0)
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags())
                .createdByUserId(creatorUserId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (!packageModel.validatePrice()) {
            throw new RuntimeException("Invalid price for paid package");
        }

        PackageModel saved = persistencePort.save(packageModel);

        log.info("CREATE_PACKAGE_OK | packageId={} | slug={}", saved.getId(), saved.getSlug());

        return toDetailResponse(saved, creator.getId());
    }

    @Override
    @Transactional
    public PackageDetailResponse updatePackage(
            Long packageId,
            UpdatePackageRequest request,
            Long currentUserId) {
        log.info("UPDATE_PACKAGE | packageId={}", packageId);

        PackageModel existing = persistencePort.findById(packageId);
        if (existing == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!existing.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized to edit this package");
        }

        if (request.getName() != null)
            existing.setName(request.getName());
        if (request.getDescription() != null)
            existing.setDescription(request.getDescription());
        if (request.getIsFree() != null)
            existing.setFree(request.getIsFree());
        if (request.getPrice() != null)
            existing.setPrice(request.getPrice());
        if (request.getCurrency() != null)
            existing.setCurrency(request.getCurrency());
        if (request.getRequiresSubscription() != null)
            existing.setRequiresSubscription(request.getRequiresSubscription());
        if (request.getThumbnailUrl() != null)
            existing.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getTags() != null)
            existing.setTags(request.getTags());

        if (request.getChangelog() != null) {
            existing.setChangelog(request.getChangelog());
            existing.setVersion(incrementVersion(existing.getVersion()));
        }

        existing.setUpdatedAt(LocalDateTime.now());

        PackageModel updated = persistencePort.update(existing);
        log.info("UPDATE_PACKAGE_OK | packageId={}", packageId);

        return toDetailResponse(updated, currentUserId);
    }

    @Override
    @Transactional
    public PackageStatusChangeResponse publishPackage(Long packageId, Long currentUserId) {
        log.info("PUBLISH_PACKAGE | packageId={}", packageId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        List<String> errors = pkg.validateForPublishing();
        if (!errors.isEmpty()) {
            throw new RuntimeException("Package cannot be published: " + String.join(", ", errors));
        }

        PackageStatus oldStatus = pkg.getStatus();
        pkg.setStatus(PackageStatus.PUBLISHED);
        pkg.setUpdatedAt(LocalDateTime.now());

        persistencePort.update(pkg);

        log.info("PUBLISH_PACKAGE_OK | packageId={}", packageId);

        return PackageStatusChangeResponse.builder()
                .packageId(packageId)
                .packageName(pkg.getName())
                .oldStatus(oldStatus)
                .newStatus(PackageStatus.PUBLISHED)
                .changedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public PackageStatusChangeResponse deprecatePackage(Long packageId, String reason, Long currentUserId) {
        log.info("DEPRECATE_PACKAGE | packageId={}", packageId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        if (!pkg.canBeDeprecated()) {
            throw new RuntimeException("Package cannot be deprecated from status: " + pkg.getStatus());
        }

        PackageStatus oldStatus = pkg.getStatus();
        pkg.setStatus(PackageStatus.DEPRECATED);
        pkg.setUpdatedAt(LocalDateTime.now());

        persistencePort.update(pkg);

        log.info("DEPRECATE_PACKAGE_OK | packageId={}", packageId);

        return PackageStatusChangeResponse.builder()
                .packageId(packageId)
                .packageName(pkg.getName())
                .oldStatus(oldStatus)
                .newStatus(PackageStatus.DEPRECATED)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }

    @Override
    @Transactional
    public PackageStatusChangeResponse suspendPackage(Long packageId, String reason, Long moderatorUserId) {
        log.info("SUSPEND_PACKAGE | packageId={}", packageId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeSuspended()) {
            throw new RuntimeException("Package cannot be suspended from status: " + pkg.getStatus());
        }

        PackageStatus oldStatus = pkg.getStatus();
        pkg.setStatus(PackageStatus.SUSPENDED);
        pkg.setUpdatedAt(LocalDateTime.now());

        persistencePort.update(pkg);

        log.info("SUSPEND_PACKAGE_OK | packageId={}", packageId);

        return PackageStatusChangeResponse.builder()
                .packageId(packageId)
                .packageName(pkg.getName())
                .oldStatus(oldStatus)
                .newStatus(PackageStatus.SUSPENDED)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }

    @Override
    @Transactional
    public PackageStatusChangeResponse unsuspendPackage(Long packageId, Long moderatorUserId) {
        log.info("UNSUSPEND_PACKAGE | packageId={}", packageId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (pkg.getStatus() != PackageStatus.SUSPENDED) {
            throw new RuntimeException("Package is not suspended");
        }

        PackageStatus oldStatus = pkg.getStatus();
        pkg.setStatus(PackageStatus.DRAFT);
        pkg.setUpdatedAt(LocalDateTime.now());

        persistencePort.update(pkg);

        log.info("UNSUSPEND_PACKAGE_OK | packageId={}", packageId);

        return PackageStatusChangeResponse.builder()
                .packageId(packageId)
                .packageName(pkg.getName())
                .oldStatus(oldStatus)
                .newStatus(PackageStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void deletePackage(Long packageId, Long currentUserId) {
        log.info("DELETE_PACKAGE | packageId={}", packageId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        if (pkg.getStatus() != PackageStatus.DRAFT) {
            throw new RuntimeException("Can only delete DRAFT packages");
        }

        persistencePort.delete(packageId);
        log.info("DELETE_PACKAGE_OK | packageId={}", packageId);
    }

    @Override
    @Transactional
    public PackageItemResponse addItemToPackage(
            Long packageId,
            AddPackageItemRequest request,
            Long currentUserId) {
        log.info("ADD_ITEM_TO_PACKAGE | packageId={} | itemType={}", packageId, request.getItemType());

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        PackageItemModel itemModel = PackageItemModel.builder()
                .packageId(packageId)
                .itemType(request.getItemType())
                .sportId(request.getSportId())
                .parameterId(request.getParameterId())
                .routineId(request.getRoutineId())
                .exerciseId(request.getExerciseId())
                .categoryId(request.getCategoryId())
                .displayOrder(
                        request.getDisplayOrder() != null ? request.getDisplayOrder() : pkg.getNextItemDisplayOrder())
                .notes(request.getNotes())
                .build();

        List<String> errors = itemModel.validate();
        if (!errors.isEmpty()) {
            throw new RuntimeException("Invalid item: " + String.join(", ", errors));
        }

        PackageItemModel saved = persistencePort.saveItem(itemModel);
        log.info("ADD_ITEM_TO_PACKAGE_OK | itemId={}", saved.getId());

        return PackageItemResponse.builder()
                .id(saved.getId())
                .itemType(saved.getItemType())
                .displayOrder(saved.getDisplayOrder())
                .notes(saved.getNotes())
                .build();
    }

    @Override
    @Transactional
    public void removeItemFromPackage(Long packageId, Long itemId, Long currentUserId) {
        log.info("REMOVE_ITEM_FROM_PACKAGE | packageId={} | itemId={}", packageId, itemId);

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        persistencePort.deleteItem(itemId);
        log.info("REMOVE_ITEM_FROM_PACKAGE_OK | itemId={}", itemId);
    }

    @Override
    @Transactional
    public PackageDetailResponse reorderPackageItems(
            Long packageId,
            ReorderPackageItemsRequest request,
            Long currentUserId) {
        log.info("REORDER_PACKAGE_ITEMS | packageId={} | itemCount={}", packageId, request.getItemIds().size());

        PackageModel pkg = persistencePort.findById(packageId);
        if (pkg == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        if (!pkg.canBeEditedBy(currentUserId)) {
            throw new RuntimeException("Not authorized");
        }

        pkg.reorderItems(request.getItemIds());

        List<PackageItemModel> items = pkg.getItems();
        persistencePort.updateItemsOrder(items);

        log.info("REORDER_PACKAGE_ITEMS_OK | packageId={}", packageId);

        return toDetailResponse(pkg, currentUserId);
    }

    @Override
    @Transactional
    public void recordPackageDownload(Long packageId, Long userId) {
        log.info("RECORD_DOWNLOAD | packageId={} | userId={}", packageId, userId);
        persistencePort.incrementDownloadCount(packageId);
    }

    @Override
    @Transactional
    public void ratePackage(Long packageId, Double rating, Long userId) {
        log.info("RATE_PACKAGE | packageId={} | rating={}", packageId, rating);

        if (rating < 1.0 || rating > 5.0) {
            throw new RuntimeException("Rating must be between 1.0 and 5.0");
        }

        persistencePort.addRating(packageId, rating);
    }

    @Override
    @Transactional
    public void purchasePackage(Long packageId, Long buyerUserId) {
        log.info("PURCHASE_PACKAGE | packageId={} | buyerId={}", packageId, buyerUserId);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        String slug = baseSlug;
        int counter = 1;

        while (persistencePort.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String incrementVersion(String currentVersion) {
        if (currentVersion == null)
            currentVersion = "1.0.0";

        String[] parts = currentVersion.split("\\.");
        int patch = Integer.parseInt(parts[2]) + 1;
        return parts[0] + "." + parts[1] + "." + patch;
    }

    private PackageDetailResponse toDetailResponse(PackageModel pkg, Long userId) {
        CreatorModel creator = userRepository.findById(pkg.getCreatedByUserId())
                .map(u -> CreatorModel.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .build())
                .orElse(null);

        List<PackageItemModel> items = persistencePort.findItemsByPackageId(pkg.getId());

        return packageConverter.toDetailResponse(
                pkg,
                creator,
                items,
                pkg.canBeEditedBy(userId),
                pkg.isAccessibleByTier(SubscriptionType.PREMIUM),
                false);
    }
}