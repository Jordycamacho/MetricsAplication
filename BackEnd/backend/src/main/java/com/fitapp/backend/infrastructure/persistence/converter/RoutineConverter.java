package com.fitapp.backend.infrastructure.persistence.converter;

import com.fitapp.backend.domain.model.RoutineModel;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.UserEntity;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class RoutineConverter {

    private final SpringDataUserRepository springDataUserRepository;
    private final SportRepository sportRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional(readOnly = true)
    public RoutineModel toDomain(RoutineEntity entity) {
        RoutineModel routine = new RoutineModel();
        routine.setId(entity.getId());
        routine.setName(entity.getName());
        routine.setDescription(entity.getDescription());
        routine.setIsActive(entity.getIsActive() != null ? entity.getIsActive() : true);
        routine.setCreatedAt(entity.getCreatedAt());
        routine.setUpdatedAt(entity.getUpdatedAt());
        routine.setUserId(entity.getUser().getId());

        if (entity.getSport() != null) {
            routine.setSportId(entity.getSport().getId());
        } else {
            routine.setSportId(null);
        }

        routine.setTrainingDays(entity.getTrainingDays() != null ? entity.getTrainingDays() : new HashSet<>());
        routine.setGoal(entity.getGoal() != null ? entity.getGoal() : "");
        routine.setSessionsPerWeek(entity.getSessionsPerWeek() != null ? entity.getSessionsPerWeek() : 3);

        return routine;
    }

    public RoutineEntity toEntity(RoutineModel domain) {
        RoutineEntity entity = new RoutineEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : true);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        entity.setTrainingDays(domain.getTrainingDays() != null ? domain.getTrainingDays() : new HashSet<>());
        entity.setGoal(domain.getGoal() != null ? domain.getGoal() : "");
        entity.setSessionsPerWeek(domain.getSessionsPerWeek() != null ? domain.getSessionsPerWeek() : 3);

        UserEntity user = springDataUserRepository.findById(domain.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + domain.getUserId()));
        entity.setUser(user);

        if (domain.getSportId() != null) {
            SportEntity sport = sportRepository.findById(domain.getSportId())
                    .orElseThrow(() -> new RuntimeException("Sport not found with id: " + domain.getSportId()));
            entity.setSport(sport);
        } else {
            entity.setSport(null);
        }

        return entity;
    }

}