package com.fitapp.backend.infrastructure.persistence.specification;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ExerciseCategorySpecification {

    private ExerciseCategorySpecification() {
    }

    public static Specification<ExerciseCategoryEntity> withFilters(
            String search,
            Boolean isPredefined,
            Boolean isActive,
            Boolean isPublic,
            Long sportId,
            Long ownerId,
            Boolean includePredefined) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.getResultType().equals(Long.class)
                    && !query.getResultType().equals(long.class)) {
                root.fetch("owner", JoinType.LEFT);
                root.fetch("sport", JoinType.LEFT);
                query.distinct(true);
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)));
            }

            if (isPredefined != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPredefined"), isPredefined));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (isPublic != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), isPublic));
            }

            if (sportId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sport").get("id"), sportId));
            }

            if (ownerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), ownerId));
            }

            if (Boolean.FALSE.equals(includePredefined)) {
                predicates.add(criteriaBuilder.equal(root.get("isPredefined"), false));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ExerciseCategoryEntity> availableForUser(Long userId, Long sportId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.getResultType().equals(Long.class)
                    && !query.getResultType().equals(long.class)) {
                root.fetch("owner", JoinType.LEFT);
                root.fetch("sport", JoinType.LEFT);
                query.distinct(true);
            }

            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            Predicate predefined = criteriaBuilder.equal(root.get("isPredefined"), true);
            Predicate ownedByUser = criteriaBuilder.equal(root.get("owner").get("id"), userId);
            Predicate isPublic = criteriaBuilder.equal(root.get("isPublic"), true);
            predicates.add(criteriaBuilder.or(predefined, ownedByUser, isPublic));

            if (sportId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sport").get("id"), sportId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}