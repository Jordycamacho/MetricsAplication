package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.exercise.request.ExerciseFilterRequest;
import com.fitapp.backend.application.dto.exercise.request.ExerciseRequest;
import com.fitapp.backend.application.dto.exercise.response.ExercisePageResponse;
import com.fitapp.backend.application.dto.exercise.response.ExerciseResponse;
import com.fitapp.backend.application.ports.input.ExerciseUseCase;
import com.fitapp.backend.application.ports.output.ExercisePersistencePort;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.*;
import com.fitapp.backend.domain.model.ExerciseModel;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.domain.model.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseServiceImpl implements ExerciseUseCase {

    private final ExercisePersistencePort exercisePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final SportPersistencePort sportPersistencePort;
    private final SubscriptionLimitChecker limitChecker;

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getAllExercisesPaginated(ExerciseFilterRequest filters) {
        return toPageResponse(exercisePersistencePort.findByFilters(filters, buildPageable(filters)));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getMyExercisesPaginated(String userEmail, ExerciseFilterRequest filters) {
        filters.setCreatedBy(getUser(userEmail).getId());
        return toPageResponse(exercisePersistencePort.findByFilters(filters, buildPageable(filters)));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getAvailableExercisesPaginated(String userEmail, ExerciseFilterRequest filters) {
        return toPageResponse(
                exercisePersistencePort.findAvailableForUser(getUser(userEmail).getId(), buildPageable(filters)));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getExercisesBySport(String userEmail, Long sportId, ExerciseFilterRequest filters) {
        return toPageResponse(exercisePersistencePort.findAvailableForUserAndSport(getUser(userEmail).getId(), sportId,
                buildPageable(filters)));
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseModel getExerciseById(Long id) {
        return exercisePersistencePort.findById(id).orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getRecentlyUsedExercises(String userEmail, Pageable pageable) {
        return toPageResponse(exercisePersistencePort.findRecentlyUsedByUser(getUser(userEmail).getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getMostPopularExercises(Pageable pageable) {
        return toPageResponse(exercisePersistencePort.findMostPopular(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisePageResponse getTopRatedExercises(Pageable pageable) {
        return toPageResponse(exercisePersistencePort.findTopRated(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserExerciseCount(String userEmail) {
        return exercisePersistencePort.countByUser(getUser(userEmail).getId());
    }

    @Override
    @Transactional
    public ExerciseModel createExercise(ExerciseRequest request, String userEmail) {
        UserModel user = getUser(userEmail);

        long currentCount = exercisePersistencePort.countByUser(user.getId());
        limitChecker.checkCustomExerciseLimit(userEmail, currentCount);

        exercisePersistencePort.findByNameAndCreatedById(request.getName(), user.getId())
                .ifPresent(e -> {
                    throw new ExerciseAlreadyExistsException(request.getName());
                });

        ExerciseModel model = new ExerciseModel();
        model.setName(request.getName().trim());
        model.setDescription(request.getDescription());
        model.setExerciseType(request.getExerciseType());
        model.setSports(validateSports(request.getSportIds()));
        model.setCreatedById(user.getId());
        model.setCreatedByEmail(user.getEmail());
        model.setCategoryIds(request.getCategoryIds() != null ? request.getCategoryIds() : new HashSet<>());
        model.setSupportedParameterIds(
                request.getSupportedParameterIds() != null ? request.getSupportedParameterIds() : new HashSet<>());
        model.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        model.setIsActive(true);
        model.setUsageCount(0);
        model.setRating(0.0);
        model.setRatingCount(0);

        ExerciseModel saved = exercisePersistencePort.save(model);
        log.info("EXERCISE_CREATED | id={} | name={} | user={}", saved.getId(), saved.getName(), userEmail);
        return saved;
    }

    @Override
    @Transactional
    public ExerciseModel updateExercise(Long id, ExerciseRequest request, String userEmail) {
        UserModel user = getUser(userEmail);
        ExerciseModel existing = exercisePersistencePort.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        checkOwnership(existing, user.getId());

        if (StringUtils.hasText(request.getName()) && !request.getName().equalsIgnoreCase(existing.getName())
                && exercisePersistencePort.existsByNameAndCreatedByIdExcluding(request.getName(), user.getId(), id)) {
            throw new ExerciseAlreadyExistsException(request.getName());
        }

        if (StringUtils.hasText(request.getName()))
            existing.setName(request.getName().trim());
        if (request.getDescription() != null)
            existing.setDescription(request.getDescription());
        if (request.getExerciseType() != null)
            existing.setExerciseType(request.getExerciseType());
        if (request.getIsPublic() != null)
            existing.setIsPublic(request.getIsPublic());
        if (request.getSportIds() != null && !request.getSportIds().isEmpty())
            existing.setSports(validateSports(request.getSportIds()));
        if (request.getCategoryIds() != null)
            existing.setCategoryIds(request.getCategoryIds());
        if (request.getSupportedParameterIds() != null)
            existing.setSupportedParameterIds(request.getSupportedParameterIds());

        ExerciseModel updated = exercisePersistencePort.save(existing);
        log.info("EXERCISE_UPDATED | id={} | user={}", id, userEmail);
        return updated;
    }

    @Override
    @Transactional
    public void deleteExercise(Long id, String userEmail) {
        UserModel user = getUser(userEmail);
        ExerciseModel exercise = exercisePersistencePort.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        checkOwnership(exercise, user.getId());

        if (exercise.getUsageCount() != null && exercise.getUsageCount() > 0) {
            exercise.setIsActive(false);
            exercisePersistencePort.save(exercise);
            log.info("EXERCISE_DEACTIVATED | id={} | usageCount={}", id, exercise.getUsageCount());
        } else {
            exercisePersistencePort.delete(id);
            log.info("EXERCISE_DELETED | id={} | user={}", id, userEmail);
        }
    }

    @Override
    @Transactional
    public void toggleExerciseStatus(Long id, String userEmail) {
        UserModel user = getUser(userEmail);
        ExerciseModel exercise = exercisePersistencePort.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        checkOwnership(exercise, user.getId());
        exercise.setIsActive(!Boolean.TRUE.equals(exercise.getIsActive()));
        exercisePersistencePort.save(exercise);
        log.info("EXERCISE_TOGGLED | id={} | isActive={}", id, exercise.getIsActive());
    }

    @Override
    @Transactional
    public void incrementExerciseUsage(Long exerciseId) {
        if (!exercisePersistencePort.existsById(exerciseId))
            throw new ExerciseNotFoundException(exerciseId);
        exercisePersistencePort.incrementUsageCount(exerciseId);
    }

    @Override
    @Transactional
    public void rateExercise(Long exerciseId, Double rating, String userEmail) {
        if (rating == null || rating < 1.0 || rating > 5.0)
            throw new ExerciseRatingException("Rating must be between 1 and 5");
        UserModel user = getUser(userEmail);
        if (!exercisePersistencePort.existsById(exerciseId))
            throw new ExerciseNotFoundException(exerciseId);
        if (exercisePersistencePort.hasUserRated(exerciseId, user.getId()))
            throw new ExerciseRatingException("You have already rated this exercise");
        exercisePersistencePort.saveRating(exerciseId, user.getId(), rating);
    }

    @Override
    @Transactional
    public ExerciseModel duplicateExercise(Long exerciseId, String userEmail) {
        UserModel user = getUser(userEmail);
        ExerciseModel original = exercisePersistencePort.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        ExerciseModel copy = new ExerciseModel();
        copy.setName(original.getName() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setExerciseType(original.getExerciseType());
        copy.setSports(new HashMap<>(original.getSports()));
        copy.setCreatedById(user.getId());
        copy.setCreatedByEmail(user.getEmail());
        copy.setCategoryIds(new HashSet<>(original.getCategoryIds()));
        copy.setSupportedParameterIds(new HashSet<>(original.getSupportedParameterIds()));
        copy.setIsPublic(false);
        copy.setIsActive(true);
        copy.setUsageCount(0);
        copy.setRating(0.0);
        copy.setRatingCount(0);

        ExerciseModel saved = exercisePersistencePort.save(copy);
        log.info("EXERCISE_DUPLICATED | originalId={} | copyId={}", exerciseId, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExerciseModel makeExercisePublic(Long exerciseId, String userEmail) {
        UserModel user = getUser(userEmail);
        ExerciseModel exercise = exercisePersistencePort.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        checkOwnership(exercise, user.getId());
        exercise.setIsPublic(true);
        return exercisePersistencePort.save(exercise);
    }

    @Override
    @Transactional
    public void cleanupInactiveExercises(int daysInactive) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysInactive);
        List<ExerciseModel> inactive = exercisePersistencePort.findInactiveBefore(cutoff);
        inactive.forEach(e -> {
            exercisePersistencePort.delete(e.getId());
            log.info("CLEANUP_EXERCISE | id={} | name={}", e.getId(), e.getName());
        });
        log.info("CLEANUP_DONE | deleted={} | daysInactive={}", inactive.size(), daysInactive);
    }

    // ---- Helpers ----

    private UserModel getUser(String email) {
        return userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private void checkOwnership(ExerciseModel exercise, Long userId) {
        if (!userId.equals(exercise.getCreatedById()))
            throw new ExerciseOwnershipException(exercise.getId());
    }

    private Map<Long, String> validateSports(Set<Long> sportIds) {
        List<SportModel> found = sportPersistencePort.findAllById(sportIds);
        if (found.size() != sportIds.size()) {
            Set<Long> foundIds = found.stream().map(SportModel::getId).collect(Collectors.toSet());
            Set<Long> missing = sportIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
            throw new RuntimeException("Sports not found: " + missing);
        }
        return found.stream().collect(Collectors.toMap(SportModel::getId, SportModel::getName));
    }

    private Pageable buildPageable(ExerciseFilterRequest f) {
        if (f == null)
            return PageRequest.of(0, 20, Sort.by("name").ascending());
        Sort sort;
        if (Boolean.TRUE.equals(f.getSortByPopularity()))
            sort = Sort.by(Sort.Direction.DESC, "usageCount");
        else if (Boolean.TRUE.equals(f.getSortByRating()))
            sort = Sort.by(Sort.Direction.DESC, "rating");
        else if (f.getSortFields() != null && !f.getSortFields().isEmpty())
            sort = Sort.by(f.getSortFields().stream().map(sf -> new Sort.Order(sf.getDirection(), sf.getField()))
                    .collect(Collectors.toList()));
        else
            sort = Sort.by(f.getDirection() != null ? f.getDirection() : Sort.Direction.ASC,
                    StringUtils.hasText(f.getSortBy()) ? f.getSortBy() : "name");
        return PageRequest.of(f.getPage() != null ? f.getPage() : 0, f.getSize() != null ? f.getSize() : 20, sort);
    }

    private ExercisePageResponse toPageResponse(Page<ExerciseModel> page) {
        return ExercisePageResponse.builder()
                .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).numberOfElements(page.getNumberOfElements())
                .build();
    }

    private ExerciseResponse toResponse(ExerciseModel model) {
        ExerciseResponse r = new ExerciseResponse();
        r.setId(model.getId());
        r.setName(model.getName());
        r.setDescription(model.getDescription());
        r.setExerciseType(model.getExerciseType());
        r.setSports(model.getSports());
        r.setCreatedById(model.getCreatedById());
        r.setCategoryIds(model.getCategoryIds());
        r.setCategoryNames(model.getCategoryNames());
        r.setSupportedParameterIds(model.getSupportedParameterIds());
        r.setSupportedParameterNames(model.getSupportedParameterNames());
        r.setIsActive(model.getIsActive());
        r.setIsPublic(model.getIsPublic());
        r.setUsageCount(model.getUsageCount());
        r.setRating(model.getRating());
        r.setRatingCount(model.getRatingCount());
        r.setCreatedAt(model.getCreatedAt());
        r.setUpdatedAt(model.getUpdatedAt());
        r.setLastUsedAt(model.getLastUsedAt());
        return r;
    }
}