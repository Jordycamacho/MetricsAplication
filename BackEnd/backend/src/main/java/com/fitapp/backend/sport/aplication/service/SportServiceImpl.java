package com.fitapp.backend.sport.aplication.service;

import com.fitapp.backend.auth.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.auth.domain.exception.UserNotFoundException;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;
import com.fitapp.backend.sport.aplication.dto.request.SportFilterRequest;
import com.fitapp.backend.sport.aplication.dto.request.SportRequest;
import com.fitapp.backend.sport.aplication.dto.response.SportPageResponse;
import com.fitapp.backend.sport.aplication.dto.response.SportResponse;
import com.fitapp.backend.sport.aplication.logging.SportServiceLogger;
import com.fitapp.backend.sport.aplication.port.input.SportUseCase;
import com.fitapp.backend.sport.aplication.port.output.SportPersistencePort;
import com.fitapp.backend.sport.domain.exception.PredefinedSportException;
import com.fitapp.backend.sport.domain.exception.SportNotFoundException;
import com.fitapp.backend.sport.domain.exception.SportOwnershipException;
import com.fitapp.backend.sport.domain.model.SportModel;
import com.fitapp.backend.suscription.aplication.service.SubscriptionLimitChecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SportServiceImpl implements SportUseCase {

    private final SportPersistencePort sportPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final SportServiceLogger sportLogger;
    private final SubscriptionLimitChecker limitChecker;

    @Override
    @Transactional(readOnly = true)
    public List<SportModel> getAllSports() {
        sportLogger.logServiceEntry("getAllSports");
        try {
            List<SportModel> sports = sportPersistencePort.findAll();
            sportLogger.logSportRetrieval("SYSTEM", sports.size(), "ALL");
            sportLogger.logServiceExit("getAllSports", sports.size() + " sports retrieved");
            return sports;
        } catch (Exception e) {
            sportLogger.logServiceError("getAllSports", "Error retrieving all sports", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SportPageResponse getAllSportsPaginated(SportFilterRequest filterRequest) {
        sportLogger.logServiceEntry("getAllSportsPaginated", filterRequest);

        try {
            Pageable pageable = createPageable(filterRequest);

            log.info("SERVICE_PAGINATION | page={} | size={} | sort={}",
                    pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

            Page<SportModel> page = sportPersistencePort.findByFilters(filterRequest, pageable);

            SportPageResponse response = buildPageResponse(page, filterRequest);

            sportLogger.logSportRetrieval("SYSTEM", page.getNumberOfElements(), "PAGINATED_ALL");
            sportLogger.logServiceExit("getAllSportsPaginated",
                    "Page " + page.getNumber() + " of " + page.getTotalPages());

            return response;
        } catch (Exception e) {
            sportLogger.logServiceError("getAllSportsPaginated", "Error retrieving paginated sports", e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "predefined-sports", key = "'all'")
    @Transactional(readOnly = true)
    public List<SportModel> getPredefinedSports() {
        log.debug("CACHE_MISS | predefined-sports");
        List<SportModel> sports = sportPersistencePort.findByIsPredefinedTrue();
        log.info("PREDEFINED_SPORTS_LOADED | count={}", sports.size());
        return sports;
    }

    @Override
    @Transactional(readOnly = true)
    public SportPageResponse getPredefinedSportsPaginated(SportFilterRequest filterRequest) {
        sportLogger.logServiceEntry("getPredefinedSportsPaginated", filterRequest);

        try {
            // Forzar que sea predefinido
            filterRequest.setIsPredefined(true);
            filterRequest.setSourceType(SportSourceType.OFFICIAL);

            Pageable pageable = createPageable(filterRequest);

            Page<SportModel> page = sportPersistencePort.findByFilters(filterRequest, pageable);

            SportPageResponse response = buildPageResponse(page, filterRequest);

            sportLogger.logSportRetrieval("SYSTEM", page.getNumberOfElements(), "PAGINATED_PREDEFINED");

            return response;
        } catch (Exception e) {
            sportLogger.logServiceError("getPredefinedSportsPaginated",
                    "Error retrieving paginated predefined sports", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SportPageResponse getUserSportsPaginated(String userEmail, SportFilterRequest filterRequest) {
        sportLogger.logServiceEntry("getUserSportsPaginated", userEmail, filterRequest);

        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PAGINATION | email={}", userEmail);
                        return new RuntimeException("User not found: " + userEmail);
                    });

            // Forzar que sea del usuario
            filterRequest.setIsPredefined(false);
            filterRequest.setSourceType(SportSourceType.USER_CREATED);
            filterRequest.setCreatedBy(user.getId());

            Pageable pageable = createPageable(filterRequest);

            Page<SportModel> page = sportPersistencePort.findByFilters(filterRequest, pageable);

            SportPageResponse response = buildPageResponse(page, filterRequest);

            sportLogger.logSportRetrieval(userEmail, page.getNumberOfElements(), "PAGINATED_USER");

            return response;
        } catch (Exception e) {
            sportLogger.logServiceError("getUserSportsPaginated",
                    "Error retrieving paginated user sports", e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "user-sports", key = "#userEmail")
    @Transactional(readOnly = true)
    public List<SportModel> getUserSports(String userEmail) {
        log.debug("CACHE_MISS | user-sports | user={}", userEmail);
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        return sportPersistencePort.findByCreatedBy(user.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "user-sports", key = "#userEmail"),
            @CacheEvict(value = "predefined-sports", key = "'all'", condition = "false")
    })
    @Transactional
    public SportModel createCustomSport(SportRequest sportRequest, String userEmail) {
        log.info("SPORT_CREATION_START | user={} | name={}", userEmail, sportRequest.getName());
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        long currentCount = sportPersistencePort.countByCreatedBy(user.getId());
        limitChecker.checkCustomSportLimit(userEmail, currentCount);

        SportModel model = new SportModel();
        model.setName(sportRequest.getName());
        model.setParameterTemplate(sportRequest.getParameterTemplate());
        model.setIsPredefined(false);
        model.setSourceType(sportRequest.getSourceType() != null
                ? sportRequest.getSourceType()
                : SportSourceType.USER_CREATED);
        model.setCreatedBy(user.getId());

        SportModel saved = sportPersistencePort.save(model);
        log.info("SPORT_CREATION_SUCCESS | sportId={} | user={}", saved.getId(), userEmail);
        return saved;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "sport-by-id", key = "#sportId"),
            @CacheEvict(value = "user-sports", allEntries = true)
    })
    @Transactional
    public void deleteCustomSport(Long sportId, String userEmail) {
        log.info("SPORT_DELETE_START | sportId={} | user={}", sportId, userEmail);
        var user = userPersistencePort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        var sport = sportPersistencePort.findById(sportId)
                .orElseThrow(() -> new SportNotFoundException(sportId));

        if (sport.getIsPredefined())
            throw new PredefinedSportException();

        if (!sport.getCreatedBy().equals(user.getId()))
            throw new SportOwnershipException(sportId);

        sportPersistencePort.delete(sportId);
        log.info("SPORT_DELETE_SUCCESS | sportId={} | user={}", sportId, userEmail);
    }

    private Pageable createPageable(SportFilterRequest filterRequest) {
        if (filterRequest.getSortFields() != null && !filterRequest.getSortFields().isEmpty()) {
            List<Sort.Order> orders = filterRequest.getSortFields().stream()
                    .map(field -> new Sort.Order(field.getDirection(), field.getField()))
                    .collect(Collectors.toList());

            return PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(orders));
        }

        return PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(filterRequest.getDirection(), filterRequest.getSortBy()));
    }

    private SportPageResponse buildPageResponse(Page<SportModel> page, SportFilterRequest filterRequest) {
        List<SportResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return SportPageResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .sort(page.getSort().toString())
                .build();
    }

    private SportResponse convertToResponse(SportModel model) {
        SportResponse response = new SportResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setIsPredefined(model.getIsPredefined());
        response.setSourceType(model.getSourceType());
        response.setParameterTemplate(model.getParameterTemplate());
        return response;
    }

    private void logDataFormatDetails(SportModel sport) {
        // Verificar formato camelCase/snake_case
        if (sport.getParameterTemplate() != null) {
            sport.getParameterTemplate().forEach((key, value) -> {
                boolean isCamelCase = key.matches("^[a-z]+([A-Z][a-z]*)*$");
                boolean isSnakeCase = key.matches("^[a-z]+(_[a-z]+)*$");

                log.debug("PARAMETER_FORMAT_CHECK | key={} | camelCase={} | snakeCase={}",
                        key, isCamelCase, isSnakeCase);

                if (!isCamelCase && !isSnakeCase) {
                    log.warn("PARAMETER_FORMAT_WARNING | key={} | format=UNKNOWN", key);
                }
            });
        }

        // Verificar formato del nombre
        if (sport.getName() != null) {
            boolean hasSpecialChars = sport.getName().matches(".*[^a-zA-Z0-9\\s].*");
            if (hasSpecialChars) {
                log.warn("NAME_FORMAT_WARNING | name={} | containsSpecialChars=true", sport.getName());
            }
        }
    }
}