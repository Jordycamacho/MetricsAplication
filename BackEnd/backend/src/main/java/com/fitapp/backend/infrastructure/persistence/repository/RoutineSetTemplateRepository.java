package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.RoutineSetTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineSetTemplateRepository extends JpaRepository<RoutineSetTemplateEntity, Long> {
}