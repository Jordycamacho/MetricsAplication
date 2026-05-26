package com.fitapp.backend.routinecomplete.aplication.service;

import com.fitapp.backend.routinecomplete.aplication.port.output.RoutineExercisePersistencePort;
import com.fitapp.backend.routinecomplete.aplication.port.output.RoutinePersistencePort;
import com.fitapp.backend.routinecomplete.aplication.port.output.RoutineSetParameterPersistencePort;
import com.fitapp.backend.routinecomplete.aplication.port.output.RoutineSetTemplatePersistencePort;
import com.fitapp.backend.routinecomplete.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineSetTemplateModel;
import com.fitapp.backend.sport.aplication.port.output.SportPersistencePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de rutinas predefinidas.
 *
 * Ahora usa RoutineYamlBuilder para cargar configuraciones desde YAML,
 * lo que permite personalizar rutinas sin tocar código Java.
 *
 * Flujo:
 * 1. Builder carga YAML
 * 2. Builder resuelve IDs y construye modelos
 * 3. Este servicio persiste en 3 pasos (rutina → ejercicios → sets + parámetros)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRoutineService {

    private final RoutinePersistencePort routinePersistencePort;
    private final RoutineExercisePersistencePort routineExercisePersistencePort;
    private final RoutineSetTemplatePersistencePort setTemplatePersistencePort;
    private final RoutineSetParameterPersistencePort setParameterPersistencePort;
    private final SportPersistencePort sportPersistencePort;
    private final RoutineYamlBuilder yamlBuilder;

    // ── API pública ────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true)
    })
    public RoutineModel generateGymRoutine(String userEmail) {
        log.info("GENERATE_GYM_ROUTINE | userEmail={}", userEmail);

        Long gymSportId = sportPersistencePort.findIdByName("Musculación")
                .orElseThrow(() -> new IllegalStateException("Sport 'Musculación' not found"));

        RoutineModel routine = yamlBuilder.buildRoutineFromYaml("gym_routine.yaml", userEmail, gymSportId);
        RoutineModel saved = persistRoutine(routine);

        log.info("GENERATE_GYM_ROUTINE_OK | userId={} | routineId={}", 
                routine.getUserId(), saved.getId());
        return saved;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true)
    })
    public RoutineModel generateBoxingRoutine(String userEmail) {
        log.info("GENERATE_BOXING_ROUTINE | userEmail={}", userEmail);

        Long boxeoSportId = sportPersistencePort.findIdByName("Boxeo")
                .orElseThrow(() -> new IllegalStateException("Sport 'Boxeo' not found"));

        RoutineModel routine = yamlBuilder.buildRoutineFromYaml("boxing_routine.yaml", userEmail, boxeoSportId);
        RoutineModel saved = persistRoutine(routine);

        log.info("GENERATE_BOXING_ROUTINE_OK | userId={} | routineId={}", 
                routine.getUserId(), saved.getId());
        return saved;
    }

    /**
     * Método genérico para generar cualquier rutina desde YAML
     * Útil para futuras rutinas sin tocar código Java
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routines", allEntries = true),
            @CacheEvict(value = "userRoutines", allEntries = true),
            @CacheEvict(value = "recentRoutines", allEntries = true),
            @CacheEvict(value = "routineStats", allEntries = true)
    })
    public RoutineModel generateRoutineFromYaml(String yamlFileName, String userEmail, Long sportId) {
        log.info("GENERATE_ROUTINE_FROM_YAML | file={} | userEmail={}", yamlFileName, userEmail);

        RoutineModel routine = yamlBuilder.buildRoutineFromYaml(yamlFileName, userEmail, sportId);
        RoutineModel saved = persistRoutine(routine);

        log.info("ROUTINE_GENERATED_FROM_YAML | routineId={} | name={}", saved.getId(), saved.getName());
        return saved;
    }

    // ── Persistencia en 3 pasos ────────────────────────────────────────────────

    /**
     * Paso 1: Guardar rutina base
     * Paso 2: Guardar ejercicios
     * Paso 3: Guardar sets y parámetros
     */
    private RoutineModel persistRoutine(RoutineModel model) {
        List<RoutineExerciseModel> exercises = model.getExercises();
        model.setExercises(null);

        // Paso 1 — Rutina base
        RoutineModel savedRoutine = routinePersistencePort.save(model);
        Long routineId = savedRoutine.getId();
        log.debug("ROUTINE_CREATED | routineId={}", routineId);

        // Pasos 2 + 3 — Ejercicios, sets y parámetros
        if (exercises != null) {
            for (RoutineExerciseModel ex : exercises) {
                persistExerciseWithSets(ex, routineId);
            }
        }

        return savedRoutine;
    }

    private void persistExerciseWithSets(RoutineExerciseModel exerciseModel, Long routineId) {
        List<RoutineSetTemplateModel> sets = exerciseModel.getSets();
        exerciseModel.setSets(null);
        exerciseModel.setRoutineId(routineId);

        // Paso 2 — Ejercicio en la rutina
        RoutineExerciseModel savedExercise = routineExercisePersistencePort.save(exerciseModel);
        Long routineExerciseId = savedExercise.getId();
        log.debug("EXERCISE_ADDED | routineId={} | exerciseId={} | reId={}",
                routineId, exerciseModel.getExerciseId(), routineExerciseId);

        // Paso 3 — Sets y parámetros
        if (sets != null) {
            for (RoutineSetTemplateModel setModel : sets) {
                persistSetWithParameters(setModel, routineExerciseId);
            }
        }
    }

    private void persistSetWithParameters(RoutineSetTemplateModel setModel, Long routineExerciseId) {
        List<RoutineSetParameterModel> parameters = setModel.getParameters();
        setModel.setParameters(null);
        setModel.setRoutineExerciseId(routineExerciseId);

        RoutineSetTemplateModel savedSet = setTemplatePersistencePort.save(setModel);
        Long setTemplateId = savedSet.getId();

        // Cada parámetro se guarda independientemente
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(p -> p.setSetTemplateId(setTemplateId));
            setParameterPersistencePort.saveAll(parameters);
            
            log.debug("SET_PARAMETERS_SAVED | setId={} | paramCount={}",
                    setTemplateId, parameters.size());
        }
    }
}