package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.application.dto.package_.request.PackageFilterRequest;
import com.fitapp.backend.application.ports.output.PackagePersistencePort;
import com.fitapp.backend.domain.model.package_.PackageItemModel;
import com.fitapp.backend.domain.model.package_.PackageModel;
import com.fitapp.backend.domain.model.package_.PackageStatisticsModel;
import com.fitapp.backend.infrastructure.persistence.converter.PackageConverter;
import com.fitapp.backend.infrastructure.persistence.entity.*;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageItemType;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageType;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.repository.RoutineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackagePersistenceAdapter implements PackagePersistencePort {

    private final PackageRepository packageRepository;
    private final PackageItemRepository packageItemRepository;
    private final PackageConverter packageConverter;
    private final SpringDataUserRepository userRepository;
    private final SportRepository sportRepository;
    private final CustomParameterRepository parameterRepository;
    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository categoryRepository;

    @Override
    @Transactional
    public PackageModel save(PackageModel packageModel) {
        log.info("SAVE_PACKAGE | name={} | creator={} | packageType={} | requiresSubscription={}",
                packageModel.getName(), packageModel.getCreatedByUserId(),
                packageModel.getPackageType(), packageModel.getRequiresSubscription());

        try {
            UserEntity creatorUser = null;
            if (packageModel.getCreatedByUserId() != null) {
                log.info("FINDING_CREATOR | userId={}", packageModel.getCreatedByUserId());
                creatorUser = userRepository.findById(packageModel.getCreatedByUserId())
                        .orElseThrow(() -> new RuntimeException(
                                "Creator user not found: " + packageModel.getCreatedByUserId()));
                log.info("CREATOR_FOUND | userId={}", creatorUser.getId());
            }

            log.info("CONVERTING_TO_ENTITY | packageModel={}", packageModel);
            PackageEntity entity = packageConverter.toEntity(packageModel, creatorUser);
            log.info("ENTITY_CONVERTED | entity={}", entity);

            log.info("SAVING_ENTITY");
            PackageEntity saved = packageRepository.save(entity);
            log.info("ENTITY_SAVED | packageId={} | itemCount={}", saved.getId(), saved.getItems().size());

            log.info("CONVERTING_TO_DOMAIN");
            PackageModel result = packageConverter.toDomain(saved);
            log.info("DOMAIN_CONVERTED | packageId={}", result.getId());

            return result;
        } catch (Exception e) {
            log.error("ERROR_SAVE_PACKAGE | error={} | message={}", e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public PackageModel update(PackageModel packageModel) {
        log.info("UPDATE_PACKAGE | packageId={} | name={}", packageModel.getId(), packageModel.getName());

        PackageEntity existing = packageRepository.findById(packageModel.getId())
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageModel.getId()));

        existing.setName(packageModel.getName());
        existing.setDescription(packageModel.getDescription());
        existing.setSlug(packageModel.getSlug());
        existing.setFree(packageModel.isFree());
        existing.setPrice(packageModel.getPrice());
        existing.setCurrency(packageModel.getCurrency());
        existing.setVersion(packageModel.getVersion());
        existing.setChangelog(packageModel.getChangelog());
        existing.setRequiresSubscription(packageModel.getRequiresSubscription());
        existing.setThumbnailUrl(packageModel.getThumbnailUrl());
        existing.setTags(packageModel.getTags());

        PackageEntity updated = packageRepository.save(existing);
        log.info("UPDATE_PACKAGE_OK | packageId={}", updated.getId());
        return packageConverter.toDomain(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageModel findById(Long id) {
        return packageRepository.findById(id)
                .map(packageConverter::toDomain)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageModel findWithItemsById(Long id) {
        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageModel findBySlug(String slug) {
        return packageRepository.findBySlug(slug)
                .map(packageConverter::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return packageRepository.existsBySlug(slug);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("DELETE_PACKAGE | packageId={}", id);

        PackageEntity pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found: " + id));

        packageRepository.deleteById(id);

        log.info("DELETE_PACKAGE_OK | packageId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageModel> searchPublished(PackageFilterRequest filters, Pageable pageable) {
        log.debug("SEARCH_MARKETPLACE | search={} | type={} | free={}",
                filters.getSearch(), filters.getPackageType(), filters.getIsFree());

        Specification<PackageEntity> spec = Specification
                .where((root, query, cb) -> cb.equal(root.get("status"), PackageStatus.PUBLISHED));

        if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
            String search = "%" + filters.getSearch().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), search),
                    cb.like(cb.lower(root.get("description")), search),
                    cb.like(cb.lower(root.get("tags")), search)));
        }

        if (filters.getPackageType() != null && !filters.getPackageType().isBlank()) {
            try {
                PackageType packageType = PackageType.valueOf(filters.getPackageType());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("packageType"), packageType));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid package type: {}", filters.getPackageType());
            }
        }

        if (filters.getIsFree() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isFree"), filters.getIsFree()));
        }

        if (filters.getMinRating() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rating"), filters.getMinRating()));
        }

        if (filters.getRequiresSubscription() != null && !filters.getRequiresSubscription().isBlank()) {
            try {
                SubscriptionType subscriptionType = SubscriptionType.valueOf(filters.getRequiresSubscription());
                spec = spec.and((root, query, cb) -> cb.or(
                        cb.equal(root.get("requiresSubscription"), SubscriptionType.FREE),
                        cb.equal(root.get("requiresSubscription"), subscriptionType)));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid subscription type: {}", filters.getRequiresSubscription());
            }
        }

        Page<PackageEntity> page = packageRepository.findAll(spec, pageable);
        return PageResponse.from(
                page.getContent().stream()
                        .map(packageConverter::toDomain)
                        .collect(Collectors.toList()),
                page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageModel> findByCreatorId(Long userId, Pageable pageable) {
        log.debug("FIND_BY_CREATOR | userId={}", userId);

        Page<PackageEntity> page = packageRepository.findByCreatedByIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(
                page.getContent().stream()
                        .map(packageConverter::toDomain)
                        .collect(Collectors.toList()),
                page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageModel> findOfficialPackages(Pageable pageable) {
        log.debug("FIND_OFFICIAL_PACKAGES");

        Page<PackageEntity> page = packageRepository
                .findByCreatedByIsNullAndStatus(PackageStatus.PUBLISHED, pageable);
        return PageResponse.from(
                page.getContent().stream()
                        .map(packageConverter::toDomain)
                        .collect(Collectors.toList()),
                page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageModel> findTrending(int limit) {
        log.debug("FIND_TRENDING_PACKAGES | limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return packageRepository.findTrending(pageable).stream()
                .map(packageConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageModel> findTopRated(int limit) {
        log.debug("FIND_TOP_RATED_PACKAGES | limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return packageRepository.findTopRated(pageable).stream()
                .map(packageConverter::toDomain)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // ITEMS
    // ══════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public PackageItemModel saveItem(PackageItemModel itemModel) {
        log.info("SAVE_PACKAGE_ITEM | packageId={} | itemType={}",
                itemModel.getPackageId(), itemModel.getItemType());

        PackageEntity pack = packageRepository.findById(itemModel.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found: " + itemModel.getPackageId()));

        PackageItemEntity entity = new PackageItemEntity();
        entity.setId(itemModel.getId());
        entity.setPack(pack);
        entity.setItemType(PackageItemType.valueOf(itemModel.getNormalizedItemType()));
        entity.setDisplayOrder(itemModel.getDisplayOrder());
        entity.setNotes(itemModel.getNotes());

        // Resolver las relaciones de contenido
        resolveItemContent(entity, itemModel);

        PackageItemEntity saved = packageItemRepository.save(entity);
        log.info("SAVE_PACKAGE_ITEM_OK | itemId={}", saved.getId());
        return packageConverter.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        log.info("DELETE_PACKAGE_ITEM | itemId={}", itemId);
        packageItemRepository.deleteById(itemId);
        log.info("DELETE_PACKAGE_ITEM_OK | itemId={}", itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageItemModel> findItemsByPackageId(Long packageId) {
        return packageItemRepository.findByPackageIdOrdered(packageId).stream()
                .map(packageConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateItemsOrder(List<PackageItemModel> items) {
        log.info("UPDATE_ITEMS_ORDER | itemCount={}", items.size());
        for (PackageItemModel item : items) {
            packageItemRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
        log.info("UPDATE_ITEMS_ORDER_OK");
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // ESTADÍSTICAS
    // ══════════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void incrementDownloadCount(Long packageId) {
        packageRepository.incrementDownloadCount(packageId);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageStatisticsModel getStatistics(Long packageId) {
        PackageEntity pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));

        return PackageStatisticsModel.builder()
                .packageId(packageId)
                .totalDownloads(pkg.getDownloadCount())
                .averageRating(pkg.getRating())
                .totalRatings(pkg.getRatingCount())
                .itemCount(pkg.getItems() != null ? pkg.getItems().size() : 0)
                .lastUpdated(pkg.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void addRating(Long packageId, Double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        PackageEntity pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId));

        // Recalcular media ponderada
        double currentTotal = (pkg.getRating() != null ? pkg.getRating() : 0.0) *
                (pkg.getRatingCount() != null ? pkg.getRatingCount() : 0);
        int newCount = (pkg.getRatingCount() != null ? pkg.getRatingCount() : 0) + 1;
        double newRating = (currentTotal + rating) / newCount;

        pkg.setRating(newRating);
        pkg.setRatingCount(newCount);
        packageRepository.save(pkg);

        log.info("ADD_RATING_OK | packageId={} | newRating={} | totalRatings={}",
                packageId, newRating, newCount);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Resuelve las relaciones de contenido de un item basado en su tipo.
     */
    private void resolveItemContent(PackageItemEntity entity, PackageItemModel itemModel) {
        switch (itemModel.getNormalizedItemType()) {
            case "SPORT":
                if (itemModel.getSportId() != null) {
                    SportEntity sport = sportRepository.findById(itemModel.getSportId())
                            .orElseThrow(() -> new RuntimeException("Sport not found: " + itemModel.getSportId()));
                    entity.setSport(sport);
                }
                break;

            case "PARAMETER":
                if (itemModel.getParameterId() != null) {
                    CustomParameterEntity parameter = parameterRepository.findById(itemModel.getParameterId())
                            .orElseThrow(
                                    () -> new RuntimeException("Parameter not found: " + itemModel.getParameterId()));
                    entity.setParameter(parameter);
                }
                break;

            case "ROUTINE":
                if (itemModel.getRoutineId() != null) {
                    RoutineEntity routine = routineRepository.findById(itemModel.getRoutineId())
                            .orElseThrow(() -> new RuntimeException("Routine not found: " + itemModel.getRoutineId()));
                    entity.setRoutine(routine);
                }
                break;

            case "EXERCISE":
                if (itemModel.getExerciseId() != null) {
                    ExerciseEntity exercise = exerciseRepository.findById(itemModel.getExerciseId())
                            .orElseThrow(
                                    () -> new RuntimeException("Exercise not found: " + itemModel.getExerciseId()));
                    entity.setExercise(exercise);
                }
                break;

            case "CATEGORY":
                if (itemModel.getCategoryId() != null) {
                    ExerciseCategoryEntity category = categoryRepository.findById(itemModel.getCategoryId())
                            .orElseThrow(
                                    () -> new RuntimeException("Category not found: " + itemModel.getCategoryId()));
                    entity.setCategory(category);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown item type: " + itemModel.getItemType());
        }
    }
}