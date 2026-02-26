package com.fitapp.backend.infrastructure.persistence.specification;

import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SportSpecification {
    
    public static Specification<SportEntity> withFilters(
            String search, 
            Boolean isPredefined, 
            SportSourceType sourceType,
            Long createdBy) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    searchPattern
                ));
            }
            
            if (isPredefined != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isPredefined"), 
                    isPredefined
                ));
            }
            
            if (sourceType != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("sourceType"), 
                    sourceType
                ));
            }
            
            if (createdBy != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("createdBy").get("id"), 
                    createdBy
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<SportEntity> searchInNameOrCategory(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern)
            );
        };
    }
}