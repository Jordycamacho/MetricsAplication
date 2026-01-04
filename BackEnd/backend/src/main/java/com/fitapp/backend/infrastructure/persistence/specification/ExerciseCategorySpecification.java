package com.fitapp.backend.infrastructure.persistence.specification;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ExerciseCategorySpecification {
    
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
            
            // Búsqueda por texto
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
            
            // Filtro por predefinido
            if (isPredefined != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isPredefined"), 
                    isPredefined
                ));
            }
            
            // Filtro por activo
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isActive"), 
                    isActive
                ));
            }
            
            // Filtro por público
            if (isPublic != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isPublic"), 
                    isPublic
                ));
            }
            
            // Filtro por deporte
            if (sportId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("sport").get("id"), 
                    sportId
                ));
            }
            
            // Filtro por dueño
            if (ownerId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("owner").get("id"), 
                    ownerId
                ));
            }
            
            // Incluir predefinidos
            if (Boolean.FALSE.equals(includePredefined)) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isPredefined"), 
                    false
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<ExerciseCategoryEntity> availableForUser(Long userId, Long sportId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            
            // Predefinidas O del usuario O públicas
            Predicate predefined = criteriaBuilder.equal(root.get("isPredefined"), true);
            Predicate ownedByUser = criteriaBuilder.equal(root.get("owner").get("id"), userId);
            Predicate isPublic = criteriaBuilder.equal(root.get("isPublic"), true);
            
            predicates.add(criteriaBuilder.or(predefined, ownedByUser, isPublic));
            
            // Filtro por deporte
            if (sportId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("sport").get("id"), 
                    sportId
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}