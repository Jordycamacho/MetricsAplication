package com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.converter;

import com.fitapp.backend.Exercise.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.category.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageItemResponse;
import com.fitapp.backend.routinecomplete.package_.domain.model.CreatorModel;
import com.fitapp.backend.routinecomplete.package_.domain.model.PackageItemModel;
import com.fitapp.backend.routinecomplete.package_.domain.model.PackageModel;
import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity.PackageEntity;
import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity.PackageItemEntity;
import com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageConverter {

        public PackageModel toDomain(PackageEntity entity) {
                if (entity == null)
                        return null;

                return PackageModel.builder()
                                .id(entity.getId())
                                .name(entity.getName())
                                .description(entity.getDescription())
                                .slug(entity.getSlug())
                                .packageType(entity.getPackageType())
                                .status(entity.getStatus())
                                .isFree(entity.isFree())
                                .price(entity.getPrice())
                                .currency(entity.getCurrency())
                                .version(entity.getVersion())
                                .changelog(entity.getChangelog())
                                .requiresSubscription(entity.getRequiresSubscription())
                                .downloadCount(entity.getDownloadCount())
                                .rating(entity.getRating())
                                .ratingCount(entity.getRatingCount())
                                .thumbnailUrl(entity.getThumbnailUrl())
                                .tags(entity.getTags())
                                .createdByUserId(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                                .items(entity.getItems() != null
                                                ? entity.getItems().stream()
                                                                .map(this::toDomain)
                                                                .collect(Collectors.toList())
                                                : new ArrayList<>())
                                .createdAt(entity.getCreatedAt())
                                .updatedAt(entity.getUpdatedAt())
                                .build();
        }

        public PackageItemModel toDomain(PackageItemEntity entity) {
                if (entity == null)
                        return null;

                return PackageItemModel.builder()
                                .id(entity.getId())
                                .packageId(entity.getPack() != null ? entity.getPack().getId() : null)
                                .itemType(entity.getItemType().name())
                                .sportId(entity.getSport() != null ? entity.getSport().getId() : null)
                                .parameterId(entity.getParameter() != null ? entity.getParameter().getId() : null)
                                .routineId(entity.getRoutine() != null ? entity.getRoutine().getId() : null)
                                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                                .displayOrder(entity.getDisplayOrder())
                                .notes(entity.getNotes())
                                .build();
        }

        public PackageEntity toEntity(PackageModel domain, UserEntity creatorUser) {
                if (domain == null)
                        return null;

                PackageEntity entity = new PackageEntity();
                entity.setId(domain.getId());
                entity.setName(domain.getName());
                entity.setDescription(domain.getDescription());
                entity.setSlug(domain.getSlug());
                entity.setPackageType(domain.getPackageType());
                entity.setStatus(domain.getStatus());
                entity.setFree(domain.isFree());
                entity.setPrice(domain.getPrice());
                entity.setCurrency(domain.getCurrency());
                entity.setVersion(domain.getVersion());
                entity.setChangelog(domain.getChangelog());
                entity.setRequiresSubscription(domain.getRequiresSubscription());
                entity.setDownloadCount(domain.getDownloadCount());
                entity.setRating(domain.getRating());
                entity.setRatingCount(domain.getRatingCount());
                entity.setThumbnailUrl(domain.getThumbnailUrl());
                entity.setTags(domain.getTags());
                entity.setCreatedBy(creatorUser);
                entity.setCreatedAt(domain.getCreatedAt());
                entity.setUpdatedAt(domain.getUpdatedAt());

                // Items se manejan por cascada en la relación OneToMany
                if (domain.getItems() != null && !domain.getItems().isEmpty()) {
                        List<PackageItemEntity> itemEntities = domain.getItems().stream()
                                        .map(itemModel -> toEntity(itemModel, entity))
                                        .collect(Collectors.toList());
                        entity.setItems(itemEntities);
                } else {
                        entity.setItems(new ArrayList<>());
                }

                return entity;
        }

        public PackageEntity toEntity(PackageModel domain) {
                return toEntity(domain, null);
        }

        public PackageItemEntity toEntity(PackageItemModel domain, PackageEntity pack) {
                if (domain == null)
                        return null;

                PackageItemEntity entity = new PackageItemEntity();
                entity.setId(domain.getId());
                entity.setPack(pack);
                entity.setItemType(com.fitapp.backend.infrastructure.persistence.entity.enums.PackageItemType
                                .valueOf(domain.getNormalizedItemType()));
                entity.setDisplayOrder(domain.getDisplayOrder());
                entity.setNotes(domain.getNotes());

                // Las relaciones de contenido (sport, parameter, routine, exercise, category)
                // se cargan desde el repositorio en el adapter, aquí solo se establecen IDs
                // Para evitar lazy loading issues, no se cargan aquí.
                // El adapter es responsable de resolver estas entidades.

                return entity;
        }

        public PackageItemResponse toItemResponse(PackageItemEntity entity) {
                if (entity == null)
                        return null;

                PackageItemResponse.PackageItemResponseBuilder builder = PackageItemResponse.builder()
                                .id(entity.getId())
                                .itemType(entity.getItemType().name())
                                .displayOrder(entity.getDisplayOrder())
                                .notes(entity.getNotes());

                if (entity.getSport() != null) {
                        builder.sport(toSportResponse(entity.getSport()));
                }
                if (entity.getParameter() != null) {
                        builder.parameter(toParameterResponse(entity.getParameter()));
                }
                if (entity.getRoutine() != null) {
                        builder.routine(toRoutineResponse(entity.getRoutine()));
                }
                if (entity.getExercise() != null) {
                        builder.exercise(toExerciseResponse(entity.getExercise()));
                }

                return builder.build();
        }

        public com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageDetailResponse.CreatorInfo toCreatorInfo(
                        CreatorModel creatorModel) {
                if (creatorModel == null)
                        return null;

                return com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageDetailResponse.CreatorInfo.builder()
                                .id(creatorModel.getId())
                                .username(creatorModel.getUsername())
                                .reputationScore(creatorModel.getReputationScore())
                                .build();
        }

        public com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageSummaryResponse toSummaryResponse(
                        PackageModel domain,
                        CreatorModel creator) {
                if (domain == null)
                        return null;

                return com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageSummaryResponse.builder()
                                .id(domain.getId())
                                .name(domain.getName())
                                .description(domain.getDescription())
                                .slug(domain.getSlug())
                                .packageType(domain.getPackageType())
                                .status(domain.getStatus())
                                .isFree(domain.isFree())
                                .price(domain.getPrice())
                                .currency(domain.getCurrency())
                                .version(domain.getVersion())
                                .requiresSubscription(domain.getRequiresSubscription())
                                .downloadCount(domain.getDownloadCount())
                                .rating(domain.getRating())
                                .ratingCount(domain.getRatingCount())
                                .thumbnailUrl(domain.getThumbnailUrl())
                                .tags(domain.getTags())
                                .createdByName(creator != null ? creator.getUsername() : "FitApp Team")
                                .createdByName(creator != null ? creator.getUsername() : "FitApp Team")
                                .creatorId(creator != null ? creator.getId() : null)
                                .createdAt(domain.getCreatedAt())
                                .updatedAt(domain.getUpdatedAt())
                                .itemCount(domain.getItems() != null ? domain.getItems().size() : 0)
                                .build();
        }

        public com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageDetailResponse toDetailResponse(
                        PackageModel domain,
                        CreatorModel creator,
                        List<PackageItemModel> itemModels,
                        boolean canEdit,
                        boolean canAccess,
                        boolean isPurchased) {
                if (domain == null)
                        return null;

                List<com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageItemResponse> itemResponses = itemModels != null
                                ? itemModels.stream()
                                                .map(itemModel -> PackageItemResponse.builder()
                                                                .id(itemModel.getId())
                                                                .itemType(itemModel.getItemType())
                                                                .displayOrder(itemModel.getDisplayOrder())
                                                                .notes(itemModel.getNotes())
                                                                .build())
                                                .collect(Collectors.toList())
                                : new ArrayList<>();

                return com.fitapp.backend.routinecomplete.package_.aplication.dto.response.PackageDetailResponse.builder()
                                .id(domain.getId())
                                .name(domain.getName())
                                .description(domain.getDescription())
                                .slug(domain.getSlug())
                                .packageType(domain.getPackageType())
                                .status(domain.getStatus())
                                .isFree(domain.isFree())
                                .price(domain.getPrice())
                                .currency(domain.getCurrency())
                                .version(domain.getVersion())
                                .changelog(domain.getChangelog())
                                .requiresSubscription(domain.getRequiresSubscription())
                                .downloadCount(domain.getDownloadCount())
                                .rating(domain.getRating())
                                .ratingCount(domain.getRatingCount())
                                .thumbnailUrl(domain.getThumbnailUrl())
                                .tags(domain.getTags())
                                .createdBy(creator != null ? toCreatorInfo(creator) : null)
                                .items(itemResponses)
                                .canEdit(canEdit)
                                .canAccess(canAccess)
                                .isPurchased(isPurchased)
                                .createdAt(domain.getCreatedAt())
                                .updatedAt(domain.getUpdatedAt())
                                .build();
        }

        public com.fitapp.backend.sport.aplication.dto.response.SportResponse toSportResponse(SportEntity entity) {
                if (entity == null)
                        return null;

                com.fitapp.backend.sport.aplication.dto.response.SportResponse response = new com.fitapp.backend.sport.aplication.dto.response.SportResponse();
                response.setId(entity.getId());
                response.setName(entity.getName());
                response.setIsPredefined(entity.getIsPredefined());
                response.setSourceType(entity.getSourceType());
                response.setParameterTemplate(entity.getParameterTemplate());
                return response;
        }

        public com.fitapp.backend.parameter.application.dto.response.CustomParameterResponse toParameterResponse(
                        CustomParameterEntity entity) {
                if (entity == null)
                        return null;

                com.fitapp.backend.parameter.application.dto.response.CustomParameterResponse response = new com.fitapp.backend.parameter.application.dto.response.CustomParameterResponse();
                response.setId(entity.getId());
                response.setName(entity.getName());
                response.setDescription(entity.getDescription());
                response.setParameterType(entity.getParameterType());
                response.setUnit(entity.getUnit());
                response.setIsGlobal(entity.getIsGlobal());
                response.setIsActive(entity.getIsActive());
                response.setOwnerId(entity.getOwner() != null ? entity.getOwner().getId() : null);
                response.setOwnerName(entity.getOwner() != null ? entity.getOwner().getEmail() : null);
                response.setIsFavorite(entity.isFavorite());
                response.setCreatedAt(entity.getCreatedAt());
                response.setUpdatedAt(entity.getUpdatedAt());
                response.setUsageCount(entity.getUsageCount());
                return response;
        }

        public com.fitapp.backend.Exercise.aplication.dto.response.ExerciseResponse toExerciseResponse(
                        ExerciseEntity entity) {
                if (entity == null)
                        return null;

                com.fitapp.backend.Exercise.aplication.dto.response.ExerciseResponse response = new com.fitapp.backend.Exercise.aplication.dto.response.ExerciseResponse();
                response.setId(entity.getId());
                response.setName(entity.getName());
                response.setDescription(entity.getDescription());
                response.setExerciseType(entity.getExerciseType());
                response.setSports(entity.getSports().stream()
                                .collect(Collectors.toMap(SportEntity::getId, SportEntity::getName)));
                response.setCreatedById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
                response.setCategoryIds(entity.getCategories().stream()
                                .map(ExerciseCategoryEntity::getId)
                                .collect(Collectors.toSet()));
                response.setCategoryNames(entity.getCategories().stream()
                                .map(ExerciseCategoryEntity::getName)
                                .collect(Collectors.toSet()));
                response.setSupportedParameterIds(entity.getSupportedParameters().stream()
                                .map(CustomParameterEntity::getId)
                                .collect(Collectors.toSet()));
                response.setSupportedParameterNames(entity.getSupportedParameters().stream()
                                .map(CustomParameterEntity::getName)
                                .collect(Collectors.toSet()));
                response.setIsActive(entity.getIsActive());
                response.setIsPublic(entity.getIsPublic());
                response.setUsageCount(entity.getUsageCount());
                response.setRating(entity.getRating());
                response.setRatingCount(entity.getRatingCount());
                response.setCreatedAt(entity.getCreatedAt());
                response.setUpdatedAt(entity.getUpdatedAt());
                response.setLastUsedAt(entity.getLastUsedAt());
                return response;
        }

        public com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineResponse toRoutineResponse(
                        RoutineEntity entity) {
                if (entity == null)
                        return null;

                return com.fitapp.backend.routinecomplete.routine.aplication.dto.response.RoutineResponse.builder()
                                .id(entity.getId())
                                .name(entity.getName())
                                .description(entity.getDescription())
                                .sportId(entity.getSport() != null ? entity.getSport().getId() : null)
                                .sportName(entity.getSport() != null ? entity.getSport().getName() : null)
                                .isActive(entity.getIsActive())
                                .createdAt(entity.getCreatedAt())
                                .updatedAt(entity.getUpdatedAt())
                                .lastUsedAt(entity.getLastUsedAt())
                                .trainingDays(entity.getTrainingDays())
                                .goal(entity.getGoal())
                                .sessionsPerWeek(entity.getSessionsPerWeek())
                                .build();
        }
}