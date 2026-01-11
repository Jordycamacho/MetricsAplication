package com.fitapp.backend.infrastructure.persistence.adapter.out;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fitapp.backend.application.ports.output.RoutineExercisePersistencePort;
import com.fitapp.backend.domain.model.RoutineExerciseModel;
import com.fitapp.backend.infrastructure.persistence.converter.RoutineConverter;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineEntity;
import com.fitapp.backend.infrastructure.persistence.entity.RoutineExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.repository.RoutineExerciseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineExercisePersistenceAdapter implements RoutineExercisePersistencePort {
    
    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineConverter routineConverter;
    
    @Override
    @Transactional
    public RoutineExerciseModel save(RoutineExerciseModel routineExercise) {
        log.info("Guardando ejercicio de rutina: ejercicio={}, rutina={}", 
                routineExercise.getExerciseId(), routineExercise.getRoutineId());
        
        // Primero obtener la entidad de rutina y ejercicio
        RoutineEntity routine = routineExerciseRepository.findRoutineById(routineExercise.getRoutineId())
                .orElseThrow(() -> {
                    log.error("Rutina no encontrada: {}", routineExercise.getRoutineId());
                    return new RuntimeException("Routine not found");
                });
        
        ExerciseEntity exercise = routineExerciseRepository.findExerciseById(routineExercise.getExerciseId())
                .orElseThrow(() -> {
                    log.error("Ejercicio no encontrado: {}", routineExercise.getExerciseId());
                    return new RuntimeException("Exercise not found");
                });
        
        // Crear entidad
        RoutineExerciseEntity entity = new RoutineExerciseEntity();
        entity.setRoutine(routine);
        entity.setExercise(exercise);
        entity.setPosition(routineExercise.getPosition());
        entity.setSessionNumber(routineExercise.getSessionNumber());
        entity.setDayOfWeek(routineExercise.getDayOfWeek());
        entity.setSessionOrder(routineExercise.getSessionOrder());
        entity.setRestAfterExercise(routineExercise.getRestAfterExercise());
        
        // Guardar
        RoutineExerciseEntity savedEntity = routineExerciseRepository.save(entity);
        log.info("Ejercicio de rutina guardado con ID: {}", savedEntity.getId());
        
        return routineConverter.convertRoutineExercise(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<RoutineExerciseModel> findByIdAndRoutineId(Long id, Long routineId) {
        log.debug("Buscando ejercicio de rutina: id={}, rutina={}", id, routineId);
        return routineExerciseRepository.findByIdAndRoutineId(id, routineId)
                .map(routineConverter::convertRoutineExercise);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineId(Long routineId) {
        log.debug("Buscando ejercicios para rutina: {}", routineId);
        return routineExerciseRepository.findByRoutineId(routineId).stream()
                .map(routineConverter::convertRoutineExercise)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteByIdAndRoutineId(Long id, Long routineId) {
        log.info("Eliminando ejercicio de rutina: id={}, rutina={}", id, routineId);
        int deleted = routineExerciseRepository.deleteByIdAndRoutineId(id, routineId);
        if (deleted > 0) {
            log.info("Ejercicio eliminado exitosamente");
        } else {
            log.warn("No se encontró el ejercicio para eliminar");
        }
    }
    
    @Override
    @Transactional
    public void deleteByRoutineId(Long routineId) {
        log.info("Eliminando todos los ejercicios de la rutina: {}", routineId);
        int deleted = routineExerciseRepository.deleteByRoutineId(routineId);
        log.info("Se eliminaron {} ejercicios", deleted);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineIdAndSessionNumber(Long routineId, Integer sessionNumber) {
        log.debug("Buscando ejercicios por sesión: rutina={}, sesión={}", routineId, sessionNumber);
        return routineExerciseRepository.findByRoutineIdAndSessionNumber(routineId, sessionNumber).stream()
                .map(routineConverter::convertRoutineExercise)
                .sorted(Comparator.comparing(RoutineExerciseModel::getSessionOrder))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseModel> findByRoutineIdAndDayOfWeek(Long routineId, String dayOfWeek) {
        log.debug("Buscando ejercicios por día: rutina={}, día={}", routineId, dayOfWeek);
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            return routineExerciseRepository.findByRoutineIdAndDayOfWeek(routineId, day).stream()
                    .map(routineConverter::convertRoutineExercise)
                    .sorted(Comparator.comparing(RoutineExerciseModel::getSessionOrder))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Día de la semana inválido: {}", dayOfWeek);
            return new ArrayList<>();
        }
    }
}