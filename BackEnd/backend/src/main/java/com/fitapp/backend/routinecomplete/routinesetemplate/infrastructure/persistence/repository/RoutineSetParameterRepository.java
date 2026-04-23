package com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fitapp.backend.routinecomplete.routinesetemplate.infrastructure.persistence.entity.RoutineSetParameterEntity;

import java.util.List;

@Repository
public interface RoutineSetParameterRepository extends JpaRepository<RoutineSetParameterEntity, Long> {

    List<RoutineSetParameterEntity> findBySetTemplateId(Long setTemplateId);

    @Modifying
    @Query("DELETE FROM RoutineSetParameterEntity p WHERE p.setTemplate.id = :setTemplateId")
    int deleteBySetTemplateId(@Param("setTemplateId") Long setTemplateId);

    @Modifying
    @Query("DELETE FROM RoutineSetParameterEntity p WHERE p.id IN :ids")
    int deleteAllById(@Param("ids") List<Long> ids);
}