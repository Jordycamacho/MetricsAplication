package com.fitapp.backend.parameter.application.port.output;

import com.fitapp.backend.parameter.application.dto.request.CustomParameterFilterRequest;
import com.fitapp.backend.parameter.domain.model.CustomParameterModel;
import com.fitapp.backend.parameter.infrastructure.persistence.adapter.CustomParameterPersistencePort;
import com.fitapp.backend.parameter.infrastructure.persistence.converter.CustomParameterConverter;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.parameter.infrastructure.persistence.entity.ParameterType;
import com.fitapp.backend.parameter.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.user.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.user.infrastructure.persistence.repository.SpringDataUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

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
    private final CustomParameterConverter parameterConverter;

    @Override
    @Cacheable(value = "parameters", key = "#id")
    public Optional<CustomParameterModel> findById(Long id) {
        StopWatch watch = new StopWatch("findById");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_ID | id={}", id);
        
        Optional<CustomParameterModel> result = customParameterRepository.findById(id)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_BY_ID_COMPLETED | id={} | found={} | timeMs={}", 
                id, result.isPresent(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'all'")
    public List<CustomParameterModel> findAll() {
        StopWatch watch = new StopWatch("findAll");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS");
        
        List<CustomParameterModel> result = customParameterRepository.findAll().stream()
                .map(parameterConverter::toDomain)
                .collect(Collectors.toList());
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_ALL_COMPLETED | count={} | timeMs={}", 
                result.size(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'all_paged_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findAll(Pageable pageable) {
        StopWatch watch = new StopWatch("findAllPaged");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS_PAGED | page={} | size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<CustomParameterModel> result = customParameterRepository.findAll(pageable)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_ALL_PAGED_COMPLETED | totalElements={} | timeMs={}", 
                result.getTotalElements(), watch.getTotalTimeMillis());

        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'filters_' + #filters.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByFilters(CustomParameterFilterRequest filters, Pageable pageable) {
        StopWatch watch = new StopWatch("findByFilters");
        watch.start();
        
        log.info("PERSISTENCE_FIND_PARAMETERS_BY_FILTERS | search={} | parameterType={} | isGlobal={} | isTrackable={} | ownerId={}",
                filters.getSearch(), filters.getParameterType(), filters.getIsGlobal(), 
                filters.getIsTrackable(), filters.getOwnerId());

        Specification<CustomParameterEntity> spec = buildSpecification(filters);

        Page<CustomParameterEntity> result = customParameterRepository.findAll(spec, pageable);

        watch.stop();
        log.info("PERSISTENCE_FILTER_RESULT | totalElements={} | totalPages={} | timeMs={}",
                result.getTotalElements(), result.getTotalPages(), watch.getTotalTimeMillis());

        return result.map(parameterConverter::toDomain);
    }

    @Override
    @Cacheable(value = "parameters", key = "'name_' + #name + '_owner_' + #ownerId")
    public Optional<CustomParameterModel> findByNameAndOwnerIdAndSportId(String name, Long ownerId) {
        StopWatch watch = new StopWatch("findByNameAndOwner");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_NAME | name={} | ownerId={}", name, ownerId);
        
        Optional<CustomParameterModel> result = customParameterRepository.findByNameAndOwnerId(name, ownerId)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_BY_NAME_COMPLETED | name={} | found={} | timeMs={}", 
                name, result.isPresent(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'owner_' + #ownerId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByOwnerId(Long ownerId, Pageable pageable) {
        StopWatch watch = new StopWatch("findByOwnerId");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_PARAMETERS_BY_OWNER | ownerId={}", ownerId);
        
        Page<CustomParameterModel> result = customParameterRepository.findByOwnerId(ownerId, pageable)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_BY_OWNER_COMPLETED | ownerId={} | totalElements={} | timeMs={}", 
                ownerId, result.getTotalElements(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'global_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findByIsGlobalTrue(Pageable pageable) {
        StopWatch watch = new StopWatch("findGlobal");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_GLOBAL_PARAMETERS");
        
        Page<CustomParameterModel> result = customParameterRepository.findByIsGlobalTrue(pageable)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_GLOBAL_COMPLETED | totalElements={} | timeMs={}", 
                result.getTotalElements(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameters", key = "'available_' + #userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CustomParameterModel> findAvailableForUser(Long userId, Pageable pageable) {
        StopWatch watch = new StopWatch("findAvailableForUser");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_AVAILABLE_PARAMETERS_FOR_USER | userId={}", userId);
        
        // FIX CRÍTICO: Solo parámetros globales O del usuario actual
        Page<CustomParameterModel> result = customParameterRepository.findAvailableForUser(userId, pageable)
                .map(parameterConverter::toDomain);
        
        watch.stop();
        log.info("PERSISTENCE_FIND_AVAILABLE_COMPLETED | userId={} | totalElements={} | timeMs={}", 
                userId, result.getTotalElements(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Cacheable(value = "parameterTypes")
    public List<ParameterType> findAllDistinctParameterTypes() {
        StopWatch watch = new StopWatch("findParameterTypes");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_ALL_PARAMETER_TYPES");
        
        List<ParameterType> result = customParameterRepository.findAllDistinctParameterTypes();
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_TYPES_COMPLETED | count={} | timeMs={}", 
                result.size(), watch.getTotalTimeMillis());
        
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = { "parameters", "parameterTypes" }, allEntries = true)
    public CustomParameterModel save(CustomParameterModel parameterModel) {
        StopWatch watch = new StopWatch("saveParameter");
        watch.start();
        
        log.info("PERSISTENCE_SAVE_PARAMETER_START | name={} | type={} | isGlobal={} | trackable={} | aggregation={}",
                parameterModel.getName(), parameterModel.getParameterType(), parameterModel.getIsGlobal(),
                parameterModel.isTrackable(), parameterModel.getMetricAggregation());

        try {
            CustomParameterEntity entity = parameterConverter.toEntity(parameterModel);

            if (parameterModel.getOwnerId() != null) {
                UserEntity userRef = userRepository.getReferenceById(parameterModel.getOwnerId());
                entity.setOwner(userRef);
            }

            CustomParameterEntity savedEntity = customParameterRepository.save(entity);
            
            watch.stop();
            log.info("PERSISTENCE_SAVE_PARAMETER_SUCCESS | id={} | name={} | timeMs={}",
                    savedEntity.getId(), savedEntity.getName(), watch.getTotalTimeMillis());

            return parameterConverter.toDomain(savedEntity);
            
        } catch (Exception e) {
            watch.stop();
            log.error("PERSISTENCE_SAVE_PARAMETER_ERROR | name={} | error={} | timeMs={}", 
                    parameterModel.getName(), e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "parameters", "parameterTypes" }, allEntries = true)
    public void delete(Long id) {
        StopWatch watch = new StopWatch("deleteParameter");
        watch.start();
        
        log.warn("PERSISTENCE_DELETE_PARAMETER_START | id={}", id);
        
        try {
            customParameterRepository.deleteById(id);
            
            watch.stop();
            log.info("PERSISTENCE_DELETE_PARAMETER_SUCCESS | id={} | timeMs={}", 
                    id, watch.getTotalTimeMillis());
                    
        } catch (Exception e) {
            watch.stop();
            log.error("PERSISTENCE_DELETE_PARAMETER_ERROR | id={} | error={} | timeMs={}", 
                    id, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "parameters", allEntries = true)
    public void incrementUsageCount(Long parameterId) {
        StopWatch watch = new StopWatch("incrementUsage");
        watch.start();
        
        log.debug("PERSISTENCE_INCREMENT_USAGE_COUNT | parameterId={}", parameterId);
        
        try {
            customParameterRepository.incrementUsageCount(parameterId);
            
            watch.stop();
            log.debug("PERSISTENCE_INCREMENT_USAGE_COMPLETED | parameterId={} | timeMs={}", 
                    parameterId, watch.getTotalTimeMillis());
                    
        } catch (Exception e) {
            watch.stop();
            log.error("PERSISTENCE_INCREMENT_USAGE_ERROR | parameterId={} | error={} | timeMs={}", 
                    parameterId, e.getMessage(), watch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    /**
     * Construye la especificación de filtros con validación de permisos
     */
    private Specification<CustomParameterEntity> buildSpecification(CustomParameterFilterRequest filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Búsqueda por texto
            if (filters.getSearch() != null && !filters.getSearch().isEmpty()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)));
            }

            // Filtro por tipo
            if (filters.getParameterType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parameterType"), filters.getParameterType()));
            }

            // Filtro por global
            if (filters.getIsGlobal() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isGlobal"), filters.getIsGlobal()));
            }

            // Filtro por activo
            if (filters.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filters.getIsActive()));
            }

            // Filtro por trackable (v2)
            if (filters.getIsTrackable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isTrackable"), filters.getIsTrackable()));
            }

            // Filtro por favorito
            if (filters.getIsFavorite() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFavorite"), filters.getIsFavorite()));
            }

            // FIX CRÍTICO: Filtro por dueño
            if (filters.getOwnerId() != null) {
                if (Boolean.TRUE.equals(filters.getOnlyMine())) {
                    // Solo los del usuario actual
                    predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), filters.getOwnerId()));
                } else {
                    // Globales O del usuario actual (nunca de otros usuarios)
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("isGlobal"), true),
                        criteriaBuilder.equal(root.get("owner").get("id"), filters.getOwnerId())
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public long countByOwnerId(Long userId) {
        StopWatch watch = new StopWatch("countByOwner");
        watch.start();
        
        log.debug("PERSISTENCE_COUNT_PARAMETERS_BY_OWNER | ownerId={}", userId);
        
        long count = customParameterRepository.countByOwnerId(userId);
        
        watch.stop();
        log.debug("PERSISTENCE_COUNT_COMPLETED | ownerId={} | count={} | timeMs={}", 
                userId, count, watch.getTotalTimeMillis());
        
        return count;
    }

    @Override
    public Optional<Long> findIdByNameAndGlobal(String name) {
        StopWatch watch = new StopWatch("findIdByNameAndGlobal");
        watch.start();
        
        log.debug("PERSISTENCE_FIND_PARAMETER_ID_BY_NAME_AND_GLOBAL | name={}", name);
        
        Optional<Long> result = customParameterRepository.findIdByNameAndIsGlobalTrue(name);
        
        watch.stop();
        log.debug("PERSISTENCE_FIND_ID_COMPLETED | name={} | found={} | timeMs={}", 
                name, result.isPresent(), watch.getTotalTimeMillis());
        
        return result;
    }
}