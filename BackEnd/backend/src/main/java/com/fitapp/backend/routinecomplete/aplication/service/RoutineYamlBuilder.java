package com.fitapp.backend.routinecomplete.aplication.service;

import com.fitapp.backend.Exercise.aplication.port.output.ExercisePersistencePort;
import com.fitapp.backend.auth.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.auth.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.parameter.infrastructure.persistence.adapter.CustomParameterPersistencePort;
import com.fitapp.backend.routinecomplete.aplication.config.RoutineYamlConfig;
import com.fitapp.backend.routinecomplete.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.domain.model.RoutineSetTemplateModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder escalable basado en YAML para generar rutinas predefinidas.
 *
 * Flujo:
 * 1. Cargar YAML desde archivo
 * 2. Parsear a RoutineYamlConfig
 * 3. Resolver todos los IDs (ejercicios, parámetros, deportes)
 * 4. Construir modelos de dominio
 *
 * Ventajas:
 * - Fácil de personalizar sin tocar código Java
 * - Cada parámetro es independiente
 * - Cada set tiene sus propios parámetros
 * - Escalable a n ejercicios y m sets por ejercicio
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoutineYamlBuilder {

    private final ExercisePersistencePort exercisePersistencePort;
    private final CustomParameterPersistencePort parameterPersistencePort;
    private final UserPersistencePort userPersistencePort;

    /**
     * Carga un YAML desde classpath y construye la rutina
     */
    public RoutineModel buildRoutineFromYaml(
            String yamlFileName,
            String userEmail,
            Long sportId) {

        log.info("BUILDING_ROUTINE_FROM_YAML | file={} | user={}", yamlFileName, userEmail);

        // Paso 1: Cargar y parsear YAML
        RoutineYamlConfig config = loadYamlConfig(yamlFileName);

        // Paso 2: Resolver IDs
        Long userId = resolveUserId(userEmail);
        Map<String, Long> exerciseIds = resolveExerciseIds(config);
        Map<String, Long> parameterIds = resolveParameterIds(config);

        // Paso 3: Construir rutina
        RoutineModel routine = buildRoutineModel(config, userId, sportId);

        // Paso 4: Construir ejercicios con sets y parámetros
        List<RoutineExerciseModel> exercises = config.getExercises().stream()
                .map(exConfig -> buildExerciseModel(exConfig, exerciseIds, parameterIds))
                .collect(Collectors.toList());

        routine.setExercises(exercises);

        log.info("ROUTINE_BUILT_FROM_YAML | name={} | exercises={} | totalSets={}",
                routine.getName(),
                exercises.size(),
                exercises.stream().mapToInt(e -> e.getSets().size()).sum());

        return routine;
    }

    /**
     * Carga YAML desde classpath
     */
    private RoutineYamlConfig loadYamlConfig(String yamlFileName) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("routines/" + yamlFileName);

            if (inputStream == null) {
                throw new IllegalStateException("YAML file not found: routines/" + yamlFileName);
            }

            Map<String, Object> yamlData = yaml.load(inputStream);
            
            // Parsear manualmente porque SnakeYAML es flexible
            @SuppressWarnings("unchecked")
            Map<String, Object> routineData = (Map<String, Object>) yamlData.values().iterator().next();

            RoutineYamlConfig config = new RoutineYamlConfig();
            config.setName((String) routineData.get("name"));
            config.setDescription((String) routineData.get("description"));
            config.setSessions_per_week((Integer) routineData.get("sessions_per_week"));
            config.setGoal((String) routineData.get("goal"));
            config.setTraining_days(new HashSet<>((Collection<?>) routineData.get("training_days"))
                    .stream().map(Object::toString).collect(Collectors.toSet()));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exercisesRaw = (List<Map<String, Object>>) routineData.get("exercises");
            config.setExercises(exercisesRaw.stream()
                    .map(this::parseExerciseConfig)
                    .collect(Collectors.toList()));

            return config;
        } catch (Exception e) {
            log.error("YAML_LOAD_FAILED | error={}", e.getMessage());
            throw new RuntimeException("Failed to load YAML config: " + yamlFileName, e);
        }
    }

    /**
     * Parsea un ejercicio del YAML
     */
    private RoutineYamlConfig.ExerciseConfig parseExerciseConfig(Map<String, Object> rawEx) {
        RoutineYamlConfig.ExerciseConfig ex = new RoutineYamlConfig.ExerciseConfig();
        ex.setName((String) rawEx.get("name"));
        ex.setDay((String) rawEx.get("day"));
        ex.setPosition((Integer) rawEx.get("position"));
        ex.setRest_after_exercise((Integer) rawEx.get("rest_after_exercise"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> setsRaw = (List<Map<String, Object>>) rawEx.get("sets");
        ex.setSets(setsRaw.stream()
                .map(this::parseSetConfig)
                .collect(Collectors.toList()));

        return ex;
    }

    /**
     * Parsea un set del YAML
     */
    private RoutineYamlConfig.SetConfig parseSetConfig(Map<String, Object> rawSet) {
        RoutineYamlConfig.SetConfig set = new RoutineYamlConfig.SetConfig();
        set.setPosition((Integer) rawSet.get("position"));
        set.setType((String) rawSet.get("type"));
        set.setRest_after_set((Integer) rawSet.get("rest_after_set"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> paramsRaw = (List<Map<String, Object>>) rawSet.get("parameters");
        set.setParameters(paramsRaw.stream()
                .map(this::parseParameterConfig)
                .collect(Collectors.toList()));

        return set;
    }

    /**
     * Parsea un parámetro del YAML
     */
    private RoutineYamlConfig.ParameterConfig parseParameterConfig(Map<String, Object> rawParam) {
        RoutineYamlConfig.ParameterConfig param = new RoutineYamlConfig.ParameterConfig();
        param.setName((String) rawParam.get("name"));
        param.setValue(rawParam.get("value"));
        return param;
    }

    /**
     * Construye el modelo de rutina base
     */
    private RoutineModel buildRoutineModel(
            RoutineYamlConfig config,
            Long userId,
            Long sportId) {

        RoutineModel routine = new RoutineModel();
        routine.setName(config.getName());
        routine.setDescription(config.getDescription());
        routine.setUserId(userId);
        routine.setSportId(sportId);
        routine.setIsActive(true);
        routine.setSessionsPerWeek(config.getSessions_per_week());
        routine.setGoal(config.getGoal());
        routine.setTrainingDays(config.getTraining_days().stream()
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet()));

        return routine;
    }

    /**
     * Construye un ejercicio con todos sus sets y parámetros
     */
    private RoutineExerciseModel buildExerciseModel(
            RoutineYamlConfig.ExerciseConfig exConfig,
            Map<String, Long> exerciseIds,
            Map<String, Long> parameterIds) {

        Long exerciseId = exerciseIds.get(exConfig.getName());
        if (exerciseId == null) {
            throw new IllegalStateException(
                    "Exercise ID not found: '" + exConfig.getName() + "'. " +
                    "Verify it exists in the database.");
        }

        RoutineExerciseModel exercise = new RoutineExerciseModel();
        exercise.setExerciseId(exerciseId);
        exercise.setDayOfWeek(DayOfWeek.valueOf(exConfig.getDay()));
        exercise.setPosition(exConfig.getPosition());
        exercise.setSessionNumber(1);
        exercise.setRestAfterExercise(
                exConfig.getRest_after_exercise() > 0 ?
                exConfig.getRest_after_exercise() : 120);

        // Construir sets
        List<RoutineSetTemplateModel> sets = exConfig.getSets().stream()
                .map(setConfig -> buildSetModel(setConfig, parameterIds))
                .collect(Collectors.toList());

        exercise.setSets(sets);
        return exercise;
    }

    /**
     * Construye un set con sus parámetros independientes
     */
    private RoutineSetTemplateModel buildSetModel(
            RoutineYamlConfig.SetConfig setConfig,
            Map<String, Long> parameterIds) {

        RoutineSetTemplateModel set = new RoutineSetTemplateModel();
        set.setPosition(setConfig.getPosition());
        set.setSetType(setConfig.getType());
        set.setRestAfterSet(setConfig.getRest_after_set());
        set.setSubSetNumber(1);

        // Cada parámetro es independiente
        List<RoutineSetParameterModel> parameters = setConfig.getParameters().stream()
                .map(paramConfig -> buildParameterModel(paramConfig, parameterIds))
                .collect(Collectors.toList());

        set.setParameters(parameters);
        return set;
    }

    /**
     * Construye un parámetro individual
     * El valor se mapea según el tipo de parámetro
     */
    private RoutineSetParameterModel buildParameterModel(
            RoutineYamlConfig.ParameterConfig paramConfig,
            Map<String, Long> parameterIds) {

        Long parameterId = parameterIds.get(paramConfig.getName());
        if (parameterId == null) {
            throw new IllegalStateException(
                    "Parameter ID not found: '" + paramConfig.getName() + "'. " +
                    "Verify it exists in global parameters.");
        }

        RoutineSetParameterModel param = new RoutineSetParameterModel();
        param.setParameterId(parameterId);

        // Mapear valor según nombre del parámetro
        Object value = paramConfig.getValue();
        String paramName = paramConfig.getName().toLowerCase();

        if (paramName.contains("repeticion") || paramName.equals("reps")) {
            param.setRepetitions(toInteger(value));
        } else if (paramName.contains("peso") || paramName.contains("weight")) {
            param.setNumericValue(toDouble(value));
        } else if (paramName.contains("distancia") || paramName.contains("distance")) {
            param.setNumericValue(toDouble(value));
        } else if (paramName.contains("porcentaje") || paramName.contains("percentage")) {
            param.setNumericValue(toDouble(value));
        } else if (paramName.contains("duración") || paramName.contains("duration")) {
            param.setDurationValue(toLong(value));
        } else {
            // Default: numérico
            param.setNumericValue(toDouble(value));
        }

        return param;
    }

    // ── Resolución de IDs ──────────────────────────────────────────────────

    private Long resolveUserId(String userEmail) {
        return userPersistencePort.findByEmail(userEmail)
                .map(UserModel::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    /**
     * Extrae todos los nombres de ejercicio del YAML y resuelve sus IDs
     */
    private Map<String, Long> resolveExerciseIds(RoutineYamlConfig config) {
        return config.getExercises().stream()
                .map(RoutineYamlConfig.ExerciseConfig::getName)
                .distinct()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> exercisePersistencePort.findIdByName(name)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Exercise not found in DB: '" + name + "'"))));
    }

    /**
     * Extrae todos los nombres de parámetro del YAML y resuelve sus IDs
     */
    private Map<String, Long> resolveParameterIds(RoutineYamlConfig config) {
        Set<String> paramNames = new HashSet<>();
        config.getExercises().stream()
                .flatMap(ex -> ex.getSets().stream())
                .flatMap(set -> set.getParameters().stream())
                .forEach(param -> paramNames.add(param.getName()));

        return paramNames.stream()
                .distinct()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> parameterPersistencePort.findIdByNameAndGlobal(name)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Global parameter not found: '" + name + "'"))));
    }

    // ── Type converters ────────────────────────────────────────────────────

    private Integer toInteger(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Double) return ((Double) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return 0;
    }

    private Double toDouble(Object value) {
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0.0;
    }

    private Long toLong(Object value) {
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Double) return ((Double) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return 0L;
    }
}