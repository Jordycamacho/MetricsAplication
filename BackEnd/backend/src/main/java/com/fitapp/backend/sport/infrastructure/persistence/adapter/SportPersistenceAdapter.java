package com.fitapp.backend.sport.infrastructure.persistence.adapter;

import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.auth.infrastructure.persistence.repository.SpringDataUserRepository;
import com.fitapp.backend.sport.aplication.dto.request.SportFilterRequest;
import com.fitapp.backend.sport.aplication.port.output.SportPersistencePort;
import com.fitapp.backend.sport.domain.model.SportModel;
import com.fitapp.backend.sport.infrastructure.persistence.converter.SportConverter;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.sport.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.sport.infrastructure.persistence.specification.SportSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SportPersistenceAdapter implements SportPersistencePort {

        private final SportRepository sportRepository;
        private final SpringDataUserRepository userRepository;
        private final SportConverter sportConverter;

        @Override
        public Optional<SportModel> findById(Long id) {
                log.debug("PERSISTENCE_FIND_BY_ID | id={}", id);
                return sportRepository.findById(id)
                                .map(sportConverter::toDomain);
        }

        @Override
        public List<SportModel> findAll() {
                log.debug("PERSISTENCE_FIND_ALL");
                return sportRepository.findAll().stream()
                                .map(sportConverter::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public Optional<Long> findIdByName(String name) {
                log.debug("PERSISTENCE_FIND_ID_BY_NAME | name={}", name);
                return sportRepository.findIdByName(name);
        }

        @Override
        public Page<SportModel> findAll(Pageable pageable) {
                log.debug("PERSISTENCE_FIND_ALL_PAGED | page={} | size={} | sort={}",
                                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

                return sportRepository.findAll(pageable)
                                .map(sportConverter::toDomain);
        }

        @Override
        public Page<SportModel> findByFilters(SportFilterRequest filters, Pageable pageable) {
                log.info("PERSISTENCE_FIND_BY_FILTERS | search={} | sourceType={} | page={}",
                                filters.getSearch(), filters.getSourceType(), pageable.getPageNumber());

                Specification<SportEntity> spec = SportSpecification.withFilters(
                                filters.getSearch(),
                                filters.getIsPredefined(),
                                filters.getSourceType(),
                                filters.getCreatedBy());

                Page<SportEntity> result = sportRepository.findAll(spec, pageable);

                log.debug("PERSISTENCE_FILTER_RESULT | totalElements={} | totalPages={}",
                                result.getTotalElements(), result.getTotalPages());

                return result.map(sportConverter::toDomain);
        }

        @Override
        public Page<SportModel> searchSports(String search, Pageable pageable) {
                log.debug("PERSISTENCE_SEARCH_SPORTS | search={}", search);

                return sportRepository.searchSports(search, pageable)
                                .map(sportConverter::toDomain);
        }

        @Override
        public List<SportModel> findByIsPredefinedTrue() {
                log.debug("PERSISTENCE_FIND_PREDEFINED");
                return sportRepository.findByIsPredefinedTrue().stream()
                                .map(sportConverter::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public Page<SportModel> findPredefinedWithSearch(String search, Pageable pageable) {
                log.debug("PERSISTENCE_FIND_PREDEFINED_WITH_SEARCH | search={}", search);

                return sportRepository.findPredefinedWithSearch(search, pageable)
                                .map(sportConverter::toDomain);
        }

        @Override
        public List<SportModel> findByCreatedBy(Long userId) {
                log.debug("PERSISTENCE_FIND_BY_USER | userId={}", userId);
                return sportRepository.findByCreatedById(userId).stream()
                                .map(sportConverter::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public Page<SportModel> findByUserWithSearch(Long userId, String search, Pageable pageable) {
                log.debug("PERSISTENCE_FIND_BY_USER_WITH_SEARCH | userId={} | search={}", userId, search);

                return sportRepository.findByUserWithSearch(userId, search, pageable)
                                .map(sportConverter::toDomain);
        }

        @Override
        public SportModel save(SportModel sportModel) {
                log.debug("PERSISTENCE_SAVE | sportName={} | sourceType={}",
                                sportModel.getName(), sportModel.getSourceType());

                SportEntity entity = sportConverter.toEntity(sportModel);

                if (sportModel.getCreatedBy() != null) {
                        UserEntity user = userRepository.findById(sportModel.getCreatedBy())
                                        .orElseThrow(() -> new RuntimeException("User not found"));
                        entity.setCreatedBy(user);
                }

                SportEntity savedEntity = sportRepository.save(entity);
                log.info("PERSISTENCE_SAVE_SUCCESS | sportId={} | name={}",
                                savedEntity.getId(), savedEntity.getName());

                return sportConverter.toDomain(savedEntity);
        }

        @Override
        public void delete(Long id) {
                log.warn("PERSISTENCE_DELETE | sportId={}", id);
                sportRepository.deleteById(id);
                log.info("PERSISTENCE_DELETE_SUCCESS | sportId={}", id);
        }

        @Override
        public List<SportModel> findAllById(Set<Long> sportIds) {
                log.debug("PERSISTENCE_FIND_ALL_BY_ID | sportIds={}", sportIds);
                return sportRepository.findAllById(sportIds).stream()
                                .map(sportConverter::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public long countByCreatedBy(Long userId) {
                log.debug("PERSISTENCE_COUNT_BY_CREATED_BY | userId={}", userId);
                return sportRepository.countByCreatedById(userId);
        }

}