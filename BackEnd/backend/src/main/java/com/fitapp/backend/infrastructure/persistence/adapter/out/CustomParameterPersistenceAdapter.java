package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.infrastructure.persistence.converter.CustomParameterConverter;
import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;
import com.fitapp.backend.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomParameterPersistenceAdapter implements CustomParameterPersistencePort {

    private final CustomParameterRepository customParameterRepository;
    private final SpringDataUserRepository userRepository;
    private final SportRepository sportRepository;
    private final CustomParameterConverter parameterConverter;

    @Override
    @Cacheable(value = "parameters", key = "#id")
    public Optional<CustomParameterModel> findById(Long id) {
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_ID | id={}", id);
        return customParameterRepository.findById(id)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'all'")
    public List<CustomParameterModel> findAll() {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS");
        return customParameterRepository.findAll().stream()
                .map(parameterConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "parameters", key = "'all_paged_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findAll(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS_PAGED | page={} | size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return customParameterRepository.findAll(pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'filters_' + #filters.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByFilters(CustomParameterFilterRequest filters, Pageable pageable) {
        log.info("PERSISTENCE_FIND_PARAMETERS_BY_FILTERS | search={} | parameterType={} | isGlobal={}",
                filters.getSearch(), filters.getParameterType(), filters.getIsGlobal());

        Specification<CustomParameterEntity> spec = buildSpecification(filters);

        Page<CustomParameterEntity> result = customParameterRepository.findAll(spec, pageable);

        log.debug("PERSISTENCE_FILTER_RESULT | totalElements={} | totalPages={}",
                result.getTotalElements(), result.getTotalPages());

        return result.map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'name_' + #name + '_owner_' + #ownerId")
    public Optional<CustomParameterModel> findByNameAndOwnerIdAndSportId(String name, Long ownerId) {
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_NAME | name={} | ownerId={}", name, ownerId);
        return customParameterRepository.findByNameAndOwnerId(name, ownerId)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'owner_' + #ownerId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_PARAMETERS_BY_OWNER | ownerId={}", ownerId);
        return customParameterRepository.findByOwnerId(ownerId, pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'global_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByIsGlobalTrue(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_GLOBAL_PARAMETERS");
        return customParameterRepository.findByIsGlobalTrue(pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'available_' + #userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findAvailableForUser(Long userId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_AVAILABLE_PARAMETERS_FOR_USER | userId={}", userId);
        return customParameterRepository.findAvailableForUser(userId, pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameterTypes")
    public List<ParameterType> findAllDistinctParameterTypes() {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETER_TYPES");
        return customParameterRepository.findAllDistinctParameterTypes();
    }

    @Override
    @Transactional
    @CacheEvict(value = { "parameters", "parameterTypes" }, allEntries = true)
    public CustomParameterModel save(CustomParameterModel parameterModel) {
        log.debug("PERSISTENCE_SAVE_PARAMETER | name={} | type={} | isGlobal={}",
                parameterModel.getName(), parameterModel.getParameterType(), parameterModel.getIsGlobal());

        CustomParameterEntity entity = parameterConverter.toEntity(parameterModel);

        if (parameterModel.getOwnerId() != null) {
            UserEntity userRef = userRepository.getReferenceById(parameterModel.getOwnerId());
            entity.setOwner(userRef);
        }

        CustomParameterEntity savedEntity = customParameterRepository.save(entity);
        log.info("PERSISTENCE_SAVE_PARAMETER_SUCCESS | id={} | name={}",
                savedEntity.getId(), savedEntity.getName());

        return parameterConverter.toDomain(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "parameters", "parameterTypes" }, allEntries = true)
    public void delete(Long id) {
        log.warn("PERSISTENCE_DELETE_PARAMETER | id={}", id);
        customParameterRepository.deleteById(id);
        log.info("PERSISTENCE_DELETE_PARAMETER_SUCCESS | id={}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "parameters", allEntries = true)
    public void incrementUsageCount(Long parameterId) {
        log.debug("PERSISTENCE_INCREMENT_USAGE_COUNT | parameterId={}", parameterId);
        customParameterRepository.incrementUsageCount(parameterId);
    }

    private Specification<CustomParameterEntity> buildSpecification(CustomParameterFilterRequest filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getSearch() != null && !filters.getSearch().isEmpty()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)));
            }

            if (filters.getParameterType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parameterType"), filters.getParameterType()));
            }

            if (filters.getIsGlobal() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isGlobal"), filters.getIsGlobal()));
            }

            if (filters.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filters.getIsActive()));
            }

            if (filters.getOwnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), filters.getOwnerId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}