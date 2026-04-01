package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.package_.request.*;
import com.fitapp.backend.application.dto.package_.response.*;
import com.fitapp.backend.application.ports.input.PackageQueryUseCase;
import com.fitapp.backend.application.ports.output.PackagePersistencePort;
import com.fitapp.backend.domain.model.package_.CreatorModel;
import com.fitapp.backend.domain.model.package_.PackageItemModel;
import com.fitapp.backend.domain.model.package_.PackageModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import com.fitapp.backend.infrastructure.persistence.converter.PackageConverter;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageQueryService implements PackageQueryUseCase {

    private final PackagePersistencePort persistencePort;
    private final PackageConverter packageConverter;
    private final SpringDataUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PackageDetailResponse getPackageById(Long packageId, Long currentUserId) {
        log.info("GET_PACKAGE_BY_ID | packageId={} | userId={}", packageId, currentUserId);

        PackageModel packageModel = persistencePort.findById(packageId);
        if (packageModel == null) {
            throw new RuntimeException("Package not found: " + packageId);
        }

        boolean isCreator = packageModel.canBeEditedBy(currentUserId);

        if (!isCreator) {
            SubscriptionType userTier = getUserTier(currentUserId);
            if (!packageModel.isAccessibleByTier(userTier)) {
                throw new RuntimeException("Access denied for package: " + packageId);
            }
        }

        CreatorModel creator = getCreatorInfo(packageModel.getCreatedByUserId());
        boolean isPurchased = false;

        return packageConverter.toDetailResponse(
                packageModel,
                creator,
                persistencePort.findItemsByPackageId(packageId),
                isCreator,
                true,
                isPurchased);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageDetailResponse getPackageBySlug(String slug, Long currentUserId) {
        log.info("GET_PACKAGE_BY_SLUG | slug={}", slug);

        PackageModel packageModel = persistencePort.findBySlug(slug);
        if (packageModel == null) {
            throw new RuntimeException("Package not found: " + slug);
        }

        return getPackageById(packageModel.getId(), currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageSummaryResponse> searchMarketplace(
            PackageFilterRequest filters,
            Pageable pageable,
            Long currentUserId) {
        log.info("SEARCH_MARKETPLACE | filters={}", filters);

        SubscriptionType userTier = getUserTier(currentUserId);

        PageResponse<PackageModel> packages = persistencePort.searchPublished(filters, pageable);

        List<PackageSummaryResponse> summaries = packages.getContent().stream()
                .filter(p -> p.isAccessibleByTier(userTier))
                .map(p -> {
                    CreatorModel creator = getCreatorInfo(p.getCreatedByUserId());
                    return packageConverter.toSummaryResponse(p, creator);
                })
                .collect(Collectors.toList());

        return PageResponse.<PackageSummaryResponse>builder()
                .content(summaries)
                .page(packages.getPage())
                .pageSize(packages.getPageSize())
                .totalElements(packages.getTotalElements())
                .totalPages(packages.getTotalPages())
                .hasNext(packages.isHasNext())
                .hasPrevious(packages.isHasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageSummaryResponse> getUserPackages(
            Long userId,
            Pageable pageable,
            Long currentUserId) {
        log.info("GET_USER_PACKAGES | userId={}", userId);

        PageResponse<PackageModel> packages = persistencePort.findByCreatorId(userId, pageable);

        List<PackageSummaryResponse> summaries = packages.getContent().stream()
                .map(p -> {
                    CreatorModel creator = getCreatorInfo(p.getCreatedByUserId());
                    return packageConverter.toSummaryResponse(p, creator);
                })
                .collect(Collectors.toList());

        return PageResponse.<PackageSummaryResponse>builder()
                .content(summaries)
                .page(packages.getPage())
                .pageSize(packages.getPageSize())
                .totalElements(packages.getTotalElements())
                .totalPages(packages.getTotalPages())
                .hasNext(packages.isHasNext())
                .hasPrevious(packages.isHasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageSummaryResponse> getUserPurchasedPackages(
            Long currentUserId,
            Pageable pageable) {
        log.info("GET_USER_PURCHASED_PACKAGES | userId={}", currentUserId);
        return PageResponse.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageSummaryResponse> getOfficialPackages(Pageable pageable) {
        log.info("GET_OFFICIAL_PACKAGES");

        PageResponse<PackageModel> packages = persistencePort.findOfficialPackages(pageable);

        List<PackageSummaryResponse> summaries = packages.getContent().stream()
                .map(p -> packageConverter.toSummaryResponse(p, null))
                .collect(Collectors.toList());

        return PageResponse.<PackageSummaryResponse>builder()
                .content(summaries)
                .page(packages.getPage())
                .pageSize(packages.getPageSize())
                .totalElements(packages.getTotalElements())
                .totalPages(packages.getTotalPages())
                .hasNext(packages.isHasNext())
                .hasPrevious(packages.isHasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageSummaryResponse> getRecommendedPackages(Long currentUserId, int limit) {
        log.info("GET_RECOMMENDED_PACKAGES | userId={} | limit={}", currentUserId, limit);
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public PackageStatisticsResponse getPackageStatistics(Long packageId, Long currentUserId) {
        log.info("GET_PACKAGE_STATISTICS | packageId={}", packageId);

        var stats = persistencePort.getStatistics(packageId);
        return PackageStatisticsResponse.builder()
                .packageId(stats.getPackageId())
                .totalDownloads(stats.getTotalDownloads())
                .averageRating(stats.getAverageRating())
                .totalRatings(stats.getTotalRatings())
                .itemCount(stats.getItemCount())
                .lastUpdated(stats.getLastUpdated())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageSummaryResponse> getTrendingPackages(int limit) {
        log.info("GET_TRENDING_PACKAGES | limit={}", limit);

        return persistencePort.findTrending(limit).stream()
                .map(p -> {
                    CreatorModel creator = getCreatorInfo(p.getCreatedByUserId());
                    return packageConverter.toSummaryResponse(p, creator);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageSummaryResponse> getTopRatedPackages(int limit) {
        log.info("GET_TOP_RATED_PACKAGES | limit={}", limit);

        return persistencePort.findTopRated(limit).stream()
                .map(p -> {
                    CreatorModel creator = getCreatorInfo(p.getCreatedByUserId());
                    return packageConverter.toSummaryResponse(p, creator);
                })
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    private SubscriptionType getUserTier(Long userId) {
        if (userId == null) {
            return SubscriptionType.FREE;
        }

        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getSubscription() != null) {
                        return user.getSubscription().getSubscriptionType();
                    }
                    return SubscriptionType.FREE;
                })
                .orElse(SubscriptionType.FREE);
    }

    private CreatorModel getCreatorInfo(Long userId) {
        if (userId == null)
            return null;
        return userRepository.findById(userId)
                .map(user -> CreatorModel.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .build())
                .orElse(null);
    }

    private PackageItemModel convertItemModel(PackageItemModel item) {
        return item;
    }

    private Object mapItemToEntity(PackageItemModel item) {
        return null;
    }
}