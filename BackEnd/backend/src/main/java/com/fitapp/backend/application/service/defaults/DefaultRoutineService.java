package com.fitapp.backend.application.service.defaults;

import com.fitapp.backend.application.ports.output.*;
import com.fitapp.backend.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orquesta la generación y persistencia de las rutinas predefinidas opcionales.
 *
 * Resuelve todos los IDs (ejercicios, parámetros, deportes) ANTES de llamar
 * al generator — así el modelo de dominio solo trabaja con IDs, sin nombres.
 *
 * Flujo (igual que el manual del usuario):
 *   1. Crear rutina base              → RoutinePersistencePort.save
 *   2. Crear cada RoutineExercise     → RoutineExercisePersistencePort.save
 *   3. Crear sets + parámetros        → setTemplatePersistencePort + setParameterPersistencePort
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRoutineService {

    private final RoutinePersistencePort             routinePersistencePort;
    private final RoutineExercisePersistencePort     routineExercisePersistencePort;
    private final RoutineSetTemplatePersistencePort  setTemplatePersistencePort;
    private final RoutineSetParameterPersistencePort setParameterPersistencePort;
    private final ExercisePersistencePort            exercisePersistencePort;
    private final SportPersistencePort               sportPersistencePort;
    private final CustomParameterPersistencePort     parameterPersistencePort;
    private final UserPersistencePort                userPersistencePort;
    private final DefaultRoutineGenerator            generator;

    // ── API pública ───────────────────────────────────────────────────────────

    @Transactional
    public RoutineModel generateGymRoutine(String userEmail) {
        log.info("GENERATE_GYM_ROUTINE | userEmail={}", userEmail);

        Long userId = resolveUserId(userEmail);
        Long gymSportId = sportPersistencePort.findIdByName("Musculación")
                .orElseThrow(() -> new IllegalStateException("Sport 'Musculación' no encontrado"));

        Map<String, Long> paramIds    = resolveParamIds("Repeticiones", "Peso", "Duración");
        Map<String, Long> exerciseIds = resolveExerciseIds(GYM_EXERCISE_NAMES);

        RoutineModel saved = persistRoutine(
                generator.buildGymRoutine(userId, gymSportId, exerciseIds, paramIds));

        log.info("GENERATE_GYM_ROUTINE_OK | userId={} | routineId={}", userId, saved.getId());
        return saved;
    }

    @Transactional
    public RoutineModel generateBoxingRoutine(String userEmail) {
        log.info("GENERATE_BOXING_ROUTINE | userEmail={}", userEmail);

        Long userId = resolveUserId(userEmail);
        Long boxeoSportId = sportPersistencePort.findIdByName("Boxeo")
                .orElseThrow(() -> new IllegalStateException("Sport 'Boxeo' no encontrado"));

        Map<String, Long> paramIds    = resolveParamIds("Repeticiones", "Peso", "Duración");
        Map<String, Long> exerciseIds = resolveExerciseIds(BOXING_EXERCISE_NAMES);

        RoutineModel saved = persistRoutine(
                generator.buildBoxingRoutine(userId, boxeoSportId, exerciseIds, paramIds));

        log.info("GENERATE_BOXING_ROUTINE_OK | userId={} | routineId={}", userId, saved.getId());
        return saved;
    }

    // ── Persistencia en 3 pasos ───────────────────────────────────────────────

    private RoutineModel persistRoutine(RoutineModel model) {
        List<RoutineExerciseModel> exercises = model.getExercises();
        model.setExercises(null);

        // Paso 1 — rutina base
        RoutineModel savedRoutine = routinePersistencePort.save(model);
        Long routineId = savedRoutine.getId();
        log.debug("ROUTINE_CREATED | routineId={}", routineId);

        // Pasos 2 + 3 — ejercicios, sets y parámetros
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

        // Paso 2 — ejercicio en la rutina
        RoutineExerciseModel savedExercise = routineExercisePersistencePort.save(exerciseModel);
        Long routineExerciseId = savedExercise.getId();
        log.debug("EXERCISE_ADDED | routineId={} | exerciseId={} | reId={}",
                routineId, exerciseModel.getExerciseId(), routineExerciseId);

        // Paso 3 — sets y parámetros
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

        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(p -> p.setSetTemplateId(setTemplateId));
            setParameterPersistencePort.saveAll(parameters);
        }
    }

    // ── Resolución de IDs en batch ────────────────────────────────────────────

    private Long resolveUserId(String userEmail) {
        return userPersistencePort.findByEmail(userEmail)
                .map(UserModel::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }


    private Map<String, Long> resolveParamIds(String... names) {
        return java.util.Arrays.stream(names).collect(Collectors.toMap(
                name -> name,
                name -> parameterPersistencePort.findIdByNameAndGlobal(name)
                        .orElseThrow(() -> new IllegalStateException(
                                "Parámetro global no encontrado: '" + name + "'"))));
    }

    private Map<String, Long> resolveExerciseIds(List<String> names) {
        return names.stream().collect(Collectors.toMap(
                name -> name,
                name -> exercisePersistencePort.findIdByName(name)
                        .orElseThrow(() -> new IllegalStateException(
                                "Ejercicio no encontrado: '" + name + "'"))));
    }

    // ── Listas de nombres de ejercicio por rutina ─────────────────────────────

    private static final List<String> GYM_EXERCISE_NAMES = List.of(
            "Press de Banca", "Press Inclinado con Barra", "Fondos en Paralelas",
            "Fly con Mancuernas", "Skull Crushers", "Jalón de Tríceps con Cuerda", "Fondos en Banco",
            "Sentadilla con Barra", "Prensa de Pierna", "Zancadas Caminando",
            "Extensión de Piernas en Máquina", "Elevación de Talones de Pie",
            "Crunch Declinado con Peso", "Plancha", "Rueda Abdominal", "Crunch Lateral",
            "Dominadas Lastradas", "Remo con Barra", "Jalón al Pecho", "Remo en Máquina T",
            "Curl con Barra", "Curl con Mancuernas Tipo Martillo", "Curl Inclinado con Mancuernas",
            "Press Militar con Barra", "Elevaciones Laterales", "Elevaciones Frontales",
            "Encogimientos de Hombros con Mancuernas",
            "Tijeras", "Plancha Lateral", "Elevación de Piernas Colgado",
            "Sentadilla Búlgara", "Curl Femoral Acostado", "Dominadas",
            "Pullover con Mancuerna", "Remo en Máquina"
    );

    private static final List<String> BOXING_EXERCISE_NAMES = List.of(
            "Salto a la Comba", "Shadowboxing", "Jab – Cross",
            "Combo 1-2-3-2 (Jab-Cross-Hook-Cross)", "Press de Banca con Mancuernas",
            "Remo con Mancuernas (Boxeo)", "Flexiones Explosivas",
            "Burpees", "Rounds de Saco (Cardio)", "Planchas con Rotación",
            "Medicine Ball Slam", "Mountain Climbers",
            "Combinaciones en Saco", "Defensa y Contraataque", "Sprints de Velocidad",
            "Press Militar con Barra", "Dominadas"
    );
}