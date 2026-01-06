package com.fitapp.backend.infrastructure.persistence.specification;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSpecification {
    
    public static Specification<ExerciseEntity> withFilters(
            String search, 
            ExerciseType exerciseType, 
            Long sportId,
            Long categoryId,
            Long parameterId,
            Boolean isActive,
            Boolean isPublic,
            Long createdById,
            Integer maxDuration,
            Double minRating) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Búsqueda por texto en nombre o descripción
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    searchPattern
                );
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    searchPattern
                );
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }
            
            // Filtro por tipo de ejercicio
            if (exerciseType != null) {
                predicates.add(criteriaBuilder.equal(root.get("exerciseType"), exerciseType));
            }
            
            // Filtro por deporte
            if (sportId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sport").get("id"), sportId));
            }
            
            // Filtro por categoría (join optimizado)
            if (categoryId != null) {
                Join<Object, Object> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), categoryId));
                
                // Para evitar duplicados en paginación
                query.distinct(true);
            }
            
            // Filtro por parámetro (join optimizado)
            if (parameterId != null) {
                Join<Object, Object> paramJoin = root.join("supportedParameters", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(paramJoin.get("id"), parameterId));
                
                // Para evitar duplicados en paginación
                query.distinct(true);
            }
            
            // Filtro por activo
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
            
            // Filtro por público
            if (isPublic != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), isPublic));
            }
            
            // Filtro por creador
            if (createdById != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy").get("id"), createdById));
            }
            
            // Filtro por rating mínimo
            if (minRating != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("rating"), 
                    minRating
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<ExerciseEntity> searchInNameOrDescription(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
        };
    }
    
    public static Specification<ExerciseEntity> isAvailableForUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            Predicate isPublic = criteriaBuilder.equal(root.get("isPublic"), true);
            Predicate isOwner = criteriaBuilder.equal(root.get("createdBy").get("id"), userId);
            Predicate isActive = criteriaBuilder.equal(root.get("isActive"), true);
            
            return criteriaBuilder.and(
                isActive,
                criteriaBuilder.or(isPublic, isOwner)
            );
        };
    }
}