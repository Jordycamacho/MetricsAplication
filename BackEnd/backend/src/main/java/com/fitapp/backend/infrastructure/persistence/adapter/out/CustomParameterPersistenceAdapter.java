package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.dto.parameter.request.CustomParameterFilterRequest;
import com.fitapp.backend.application.ports.output.CustomParameterPersistencePort;
import com.fitapp.backend.domain.model.CustomParameterModel;
import com.fitapp.backend.infrastructure.persistence.converter.CustomParameterConverter;
import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;
import com.fitapp.backend.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomParameterPersistenceAdapter implements CustomParameterPersistencePort {
    
    private final CustomParameterRepository customParameterRepository;
    private final SpringDataUserRepository userRepository;
    private final SportRepository sportRepository;
    private final CustomParameterConverter parameterConverter;

    @Override
    public Optional<CustomParameterModel> findById(Long id) {
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_ID | id={}", id);
        return customParameterRepository.findById(id)
                .map(parameterConverter::toDomain);
    }

    @Override
    public List<CustomParameterModel> findAll() {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS");
        return customParameterRepository.findAll().stream()
                .map(parameterConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CustomParameterModel> findAll(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETERS_PAGED | page={} | size={}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        return customParameterRepository.findAll(pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
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
    public Optional<CustomParameterModel> findByNameAndOwnerIdAndSportId(String name, Long ownerId, Long sportId) {
        log.debug("PERSISTENCE_FIND_PARAMETER_BY_NAME | name={} | ownerId={} | sportId={}", 
                 name, ownerId, sportId);
        
        return customParameterRepository.findByNameAndOwnerIdAndSportId(name, ownerId, sportId)
                .map(parameterConverter::toDomain);
    }

    @Override
    public Page<CustomParameterModel> findByOwnerId(Long ownerId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_PARAMETERS_BY_OWNER | ownerId={}", ownerId);
        
        return customParameterRepository.findByOwnerId(ownerId, pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    public Page<CustomParameterModel> findBySportId(Long sportId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_PARAMETERS_BY_SPORT | sportId={}", sportId);
        
        return customParameterRepository.findBySportId(sportId, pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    public Page<CustomParameterModel> findByIsGlobalTrue(Pageable pageable) {
        log.debug("PERSISTENCE_FIND_GLOBAL_PARAMETERS");
        
        return customParameterRepository.findByIsGlobalTrue(pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    public Page<CustomParameterModel> findAvailableForUser(Long userId, Long sportId, Pageable pageable) {
        log.debug("PERSISTENCE_FIND_AVAILABLE_PARAMETERS_FOR_USER | userId={} | sportId={}", 
                 userId, sportId);
        
        return customParameterRepository.findAvailableForUser(userId, sportId, pageable)
                .map(parameterConverter::toDomain);
    }

    @Override
    public List<String> findAllDistinctCategories() {
        log.debug("PERSISTENCE_FIND_ALL_CATEGORIES");
        return customParameterRepository.findAllDistinctCategories();
    }

    @Override
    public List<ParameterType> findAllDistinctParameterTypes() {
        log.debug("PERSISTENCE_FIND_ALL_PARAMETER_TYPES");
        return customParameterRepository.findAllDistinctParameterTypes();
    }

    @Override
    public CustomParameterModel save(CustomParameterModel parameterModel) {
        log.debug("PERSISTENCE_SAVE_PARAMETER | name={} | type={} | isGlobal={}", 
                 parameterModel.getName(), parameterModel.getParameterType(), parameterModel.getIsGlobal());
        
        CustomParameterEntity entity = parameterConverter.toEntity(parameterModel);
        
        // Establecer relaciones
        if (parameterModel.getOwnerId() != null) {
            UserEntity user = userRepository.findById(parameterModel.getOwnerId())
                    .orElseThrow(() -> {
                        log.error("USER_NOT_FOUND_FOR_PARAMETER | userId={}", parameterModel.getOwnerId());
                        return new RuntimeException("User not found");
                    });
            entity.setOwner(user);
        }
        
        if (parameterModel.getSportId() != null) {
            SportEntity sport = sportRepository.findById(parameterModel.getSportId())
                    .orElseThrow(() -> {
                        log.error("SPORT_NOT_FOUND_FOR_PARAMETER | sportId={}", parameterModel.getSportId());
                        return new RuntimeException("Sport not found");
                    });
            entity.setSport(sport);
        }
        
        CustomParameterEntity savedEntity = customParameterRepository.save(entity);
        log.info("PERSISTENCE_SAVE_PARAMETER_SUCCESS | id={} | name={}", 
                savedEntity.getId(), savedEntity.getName());
        
        return parameterConverter.toDomain(savedEntity);
    }

    @Override
    public void delete(Long id) {
        log.warn("PERSISTENCE_DELETE_PARAMETER | id={}", id);
        customParameterRepository.deleteById(id);
        log.info("PERSISTENCE_DELETE_PARAMETER_SUCCESS | id={}", id);
    }

    @Override
    public void incrementUsageCount(Long parameterId) {
        log.debug("PERSISTENCE_INCREMENT_USAGE_COUNT | parameterId={}", parameterId);
        customParameterRepository.incrementUsageCount(parameterId);
    }

    private Specification<CustomParameterEntity> buildSpecification(CustomParameterFilterRequest filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Búsqueda por texto
            if (filters.getSearch() != null && !filters.getSearch().isEmpty()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("displayName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                ));
            }
            
            // Filtro por tipo de parámetro
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
            
            // Filtro por deporte
            if (filters.getSportId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sport").get("id"), filters.getSportId()));
            }
            
            // Filtro por dueño
            if (filters.getOwnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), filters.getOwnerId()));
            }
            
            // Filtro por categoría
            if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filters.getCategory()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}