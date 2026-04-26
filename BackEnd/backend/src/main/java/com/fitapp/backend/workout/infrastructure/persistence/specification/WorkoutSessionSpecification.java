package com.fitapp.backend.workout.infrastructure.persistence.specification;

import com.fitapp.backend.workout.aplication.dto.request.WorkoutHistoryFilterRequest;
import com.fitapp.backend.workout.infrastructure.persistence.entity.WorkoutSessionEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification builder for WorkoutSessionEntity queries.
 *
 * <p>Do NOT add SLF4J log calls inside the Specification lambda — it executes
 * on every row evaluation by the JPA criteria engine, flooding the log.
 * Logging belongs in the service layer before/after the query.
 */
@Slf4j
public class WorkoutSessionSpecification {

    private WorkoutSessionSpecification() {
        // utility class
    }

    public static Specification<WorkoutSessionEntity> withFilters(
            WorkoutHistoryFilterRequest filters, Long userId) {

        log.debug("WORKOUT_SPEC_BUILD | userId={} | filters={}", userId, filters);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (filters.getRoutineId() != null) {
                predicates.add(cb.equal(root.get("routine").get("id"), filters.getRoutineId()));
            }

            if (filters.getFromDate() != null) {
                LocalDateTime from = filters.getFromDate().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), from));
            }

            if (filters.getToDate() != null) {
                LocalDateTime to = filters.getToDate().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), to));
            }

            if (filters.getMinPerformanceScore() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("performanceScore"), filters.getMinPerformanceScore()));
            }

            if (filters.getMaxPerformanceScore() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("performanceScore"), filters.getMaxPerformanceScore()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}