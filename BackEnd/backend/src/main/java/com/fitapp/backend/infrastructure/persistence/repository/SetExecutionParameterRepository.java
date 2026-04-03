package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.SetExecutionParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetExecutionParameterRepository extends JpaRepository<SetExecutionParameterEntity, Long> {
    
    /**
     * Encuentra todos los parámetros de un set ejecutado.
     */
    @Query("SELECT sep FROM SetExecutionParameterEntity sep WHERE sep.setExecution.id = :setExecutionId")
    List<SetExecutionParameterEntity> findBySetExecutionId(@Param("setExecutionId") Long setExecutionId);
    
    /**
     * Encuentra parámetros por IDs de sets (para batch loading).
     */
    @Query("SELECT sep FROM SetExecutionParameterEntity sep WHERE sep.setExecution.id IN :setIds")
    List<SetExecutionParameterEntity> findBySetExecutionIds(@Param("setIds") List<Long> setIds);
}