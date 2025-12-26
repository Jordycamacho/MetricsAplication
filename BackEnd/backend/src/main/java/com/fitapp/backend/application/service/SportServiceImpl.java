// com.fitapp.backend.application.service/SportServiceImpl.java
package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.sport.request.SportFilterRequest;
import com.fitapp.backend.application.dto.sport.request.SportPageResponse;
import com.fitapp.backend.application.dto.sport.request.SportRequest;
import com.fitapp.backend.application.dto.sport.response.SportResponse;
import com.fitapp.backend.application.logging.SportServiceLogger;
import com.fitapp.backend.application.ports.input.SportUseCase;
import com.fitapp.backend.application.ports.output.SportPersistencePort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.SportModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Transactional(readOnly = true)
    public List<SportModel> getPredefinedSports() {
        sportLogger.logServiceEntry("getPredefinedSports");
        try {
            List<SportModel> sports = sportPersistencePort.findByIsPredefinedTrue();
            sportLogger.logSportRetrieval("SYSTEM", sports.size(), "PREDEFINED");
            sportLogger.logServiceExit("getPredefinedSports", sports.size() + " predefined sports");
            return sports;
        } catch (Exception e) {
            sportLogger.logServiceError("getPredefinedSports", "Error retrieving predefined sports", e);
            throw e;
        }
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
    @Transactional(readOnly = true)
    public List<SportModel> getUserSports(String userEmail) {
        sportLogger.logServiceEntry("getUserSports", userEmail);
        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND | email={}", userEmail);
                        return new RuntimeException("User not found: " + userEmail);
                    });

            log.debug("USER_RETRIEVED | userId={} | email={}", user.getId(), userEmail);
            List<SportModel> sports = sportPersistencePort.findByCreatedBy(user.getId());

            sportLogger.logSportRetrieval(userEmail, sports.size(), "USER_CREATED");
            sportLogger.logServiceExit("getUserSports", sports.size() + " user sports");

            return sports;
        } catch (Exception e) {
            sportLogger.logServiceError("getUserSports", "Error retrieving user sports for: " + userEmail, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        sportLogger.logServiceEntry("getAllCategories");
        try {
            List<String> categories = sportPersistencePort.findAllDistinctCategories();
            sportLogger.logServiceExit("getAllCategories", categories.size() + " categories found");
            return categories;
        } catch (Exception e) {
            sportLogger.logServiceError("getAllCategories", "Error retrieving categories", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SportModel createCustomSport(SportRequest sportRequest, String userEmail) {
        sportLogger.logSportCreationStart(userEmail, sportRequest.getName(), sportRequest.getSourceType());
        sportLogger.logServiceEntry("createCustomSport", sportRequest.getName(), userEmail);

        try {
            sportRequest.logRequestData();

            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_SPORT_CREATION | email={}", userEmail);
                        return new RuntimeException("Usuario no encontrado: " + userEmail);
                    });

            log.debug("USER_FOUND_FOR_SPORT | userId={} | email={}", user.getId(), userEmail);

            SportModel sportModel = new SportModel();
            sportModel.setName(sportRequest.getName());
            sportModel.setParameterTemplate(sportRequest.getParameterTemplate());
            sportModel.setCategory(sportRequest.getCategory());
            sportModel.setIsPredefined(false);
            sportModel.setSourceType(
                    sportRequest.getSourceType() != null ? sportRequest.getSourceType() : SportSourceType.USER_CREATED);
            sportModel.setCreatedBy(user.getId());

            sportModel.logModelData("CREATING");

            logDataFormatDetails(sportModel);

            SportModel savedSport = sportPersistencePort.save(sportModel);

            sportLogger.logSportCreationSuccess(savedSport.getId(), userEmail, savedSport.getSourceType());
            sportLogger.logServiceExit("createCustomSport", savedSport.getId());

            return savedSport;
        } catch (Exception e) {
            sportLogger.logServiceError("createCustomSport",
                    "Error creating sport: " + sportRequest.getName(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCustomSport(Long sportId, String userEmail) {
        sportLogger.logSportDeletionStart(sportId, userEmail);
        sportLogger.logServiceEntry("deleteCustomSport", sportId, userEmail);

        try {
            var user = userPersistencePort.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_DELETION | email={}", userEmail);
                        return new RuntimeException("User not found: " + userEmail);
                    });

            log.debug("USER_FOUND_FOR_DELETION | userId={}", user.getId());

            var sport = sportPersistencePort.findById(sportId)
                    .orElseThrow(() -> {
                        log.error("SPORT_NOT_FOUND_FOR_DELETION | sportId={}", sportId);
                        return new RuntimeException("Sport not found: " + sportId);
                    });

            log.debug("SPORT_FOUND_FOR_DELETION | sportId={} | sourceType={} | createdBy={}",
                    sportId, sport.getSourceType(), sport.getCreatedBy());

            if (sport.getIsPredefined()) {
                log.error("DELETE_PREDEFINED_SPORT_ATTEMPT | sportId={} | user={}", sportId, userEmail);
                throw new RuntimeException("Cannot delete predefined sports");
            }

            if (!sport.getCreatedBy().equals(user.getId())) {
                log.error("UNAUTHORIZED_SPORT_DELETION | sportId={} | requester={} | owner={}",
                        sportId, user.getId(), sport.getCreatedBy());
                throw new RuntimeException("Cannot delete other users' sports");
            }

            sportPersistencePort.delete(sportId);

            sportLogger.logSportDeletionSuccess(sportId, userEmail);
            sportLogger.logServiceExit("deleteCustomSport", "Sport deleted successfully");

        } catch (Exception e) {
            sportLogger.logServiceError("deleteCustomSport",
                    "Error deleting sport: " + sportId, e);
            throw e;
        }
    }

    private Pageable createPageable(SportFilterRequest filterRequest) {
        // Si hay múltiples campos de ordenamiento
        if (filterRequest.getSortFields() != null && !filterRequest.getSortFields().isEmpty()) {
            List<Sort.Order> orders = filterRequest.getSortFields().stream()
                    .map(field -> new Sort.Order(field.getDirection(), field.getField()))
                    .collect(Collectors.toList());

            return PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(orders));
        }

        // Ordenamiento simple por un campo
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
        response.setCategory(model.getCategory());
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