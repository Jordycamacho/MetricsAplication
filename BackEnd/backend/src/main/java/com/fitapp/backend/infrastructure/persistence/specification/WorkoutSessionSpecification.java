package com.fitapp.backend.infrastructure.persistence.specification;

import com.fitapp.backend.application.dto.workout.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.infrastructure.persistence.entity.WorkoutSessionEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WorkoutSessionSpecification {

    public static Specification<WorkoutSessionEntity> withFilters(
            WorkoutHistoryFilterRequest filters, Long userId) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // OBLIGATORIO: filtrar por usuario
            predicates.add(criteriaBuilder.equal(root.get("routine").get("user").get("id"), userId));
            
            log.debug("WORKOUT_SPEC_USER_FILTER | userId={}", userId);

            // Filtro por rutina específica
            if (filters.getRoutineId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("routine").get("id"), filters.getRoutineId()));
                log.debug("WORKOUT_SPEC_ROUTINE_FILTER | routineId={}", filters.getRoutineId());
            }

            // Filtro por rango de fechas
            if (filters.getFromDate() != null) {
                LocalDateTime fromDateTime = LocalDateTime.of(filters.getFromDate(), LocalTime.MIN);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), fromDateTime));
                log.debug("WORKOUT_SPEC_FROM_DATE_FILTER | fromDate={}", fromDateTime);
            }

            if (filters.getToDate() != null) {
                LocalDateTime toDateTime = LocalDateTime.of(filters.getToDate(), LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), toDateTime));
                log.debug("WORKOUT_SPEC_TO_DATE_FILTER | toDate={}", toDateTime);
            }

            // Filtro por performance score mínimo
            if (filters.getMinPerformanceScore() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("performanceScore"), filters.getMinPerformanceScore()));
                log.debug("WORKOUT_SPEC_MIN_SCORE_FILTER | minScore={}", filters.getMinPerformanceScore());
            }

            // Filtro por performance score máximo
            if (filters.getMaxPerformanceScore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("performanceScore"), filters.getMaxPerformanceScore()));
                log.debug("WORKOUT_SPEC_MAX_SCORE_FILTER | maxScore={}", filters.getMaxPerformanceScore());
            }

            log.debug("WORKOUT_SPEC_BUILT | predicateCount={}", predicates.size());

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}