package com.fitapp.backend.infrastructure.config.data;
/* 
import com.fitapp.backend.infrastructure.persistence.entity.*;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
*/
/**
 * Crea la rutina de gimnasio predefinida: Push / Pull / Legs (5 días).
 *
 * Estructura:
 *   LUNES    → Pecho + Tríceps
 *   MARTES   → Pierna + Abdomen
 *   MIÉRCOLES→ Espalda + Bíceps
 *   JUEVES   → Hombros + Abdomen
 *   VIERNES  → Pierna + Espalda
 *
 * Restricciones FREE respetadas:
 *   - max 30 ejercicios por rutina  ✓ (25 ejercicios)
 *   - max 4 sets por ejercicio      ✓
 *
 * Sets de abdomen configurados como circuito:
 *   - Sin descanso entre sets (restAfterSet = 0)
 *   - Descanso al terminar todos los sets del ejercicio (restAfterExercise)
 *
 * Depende de: ExerciseDataLoader (@Order 5)

@Slf4j
@Component
@Order(6)
@RequiredArgsConstructor*/
public class GymRoutineDataLoader //implements ApplicationRunner 
{

/*  private final RoutineRepository      routineRepository;
    private final ExerciseRepository     exerciseRepository;
    private final SportRepository        sportRepository;
    private final CustomParameterRepository paramRepository;

    // ── Refs ──────────────────────────────────────────────────────────────────
    private CustomParameterEntity paramReps;
    private CustomParameterEntity paramPeso;
    private CustomParameterEntity paramDuracion;

    private static final String ROUTINE_NAME = "Push / Pull / Legs — Gym";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (routineRepository.existsByName(ROUTINE_NAME)) {
            log.info("Rutina '{}' ya existe, saltando seed.", ROUTINE_NAME);
            return;
        }

        log.info("Creando rutina predefinida: {}", ROUTINE_NAME);
        loadRefs();

        SportEntity gym = sportRepository.findByName("Musculación")
                .orElseThrow(() -> new IllegalStateException("Sport 'Musculación' no encontrado"));

        RoutineEntity routine = RoutineEntity.builder()
                .name(ROUTINE_NAME)
                .description("Rutina clásica de 5 días dividida por grupos musculares. " +
                             "Ideal desde principiantes hasta nivel avanzado. " +
                             "Pecho y tríceps, pierna, espalda y bíceps, hombros, pierna y espalda.")
                .sport(gym)
                .user(null)          // rutina predefinida del sistema
                .isActive(true)
                .isTemplate(false)
                .isPublic(false)
                .sessionsPerWeek(5)
                .trainingDays(new HashSet<>(Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                )))
                .goal("Hipertrofia y fuerza general con progresión lineal por grupos musculares")
                .build();

        // ── Lunes: Pecho + Tríceps ────────────────────────────────────────────
        addExercise(routine, "Press de Banca",               DayOfWeek.MONDAY, 1,  90,
                    weightedSets(4, 6, 60.0));
        addExercise(routine, "Press Inclinado con Barra",    DayOfWeek.MONDAY, 2,  90,
                    weightedSets(4, 8, 50.0));
        addExercise(routine, "Fondos en Paralelas",          DayOfWeek.MONDAY, 3,  90,
                    bodySets(3, 8));
        addExercise(routine, "Fly con Mancuernas",           DayOfWeek.MONDAY, 4,  60,
                    weightedSets(3, 10, 14.0));
        addExercise(routine, "Skull Crushers",               DayOfWeek.MONDAY, 5,  75,
                    weightedSets(4, 8, 20.0));
        addExercise(routine, "Jalón de Tríceps con Cuerda",  DayOfWeek.MONDAY, 6,  60,
                    weightedSets(4, 10, 15.0));
        addExercise(routine, "Fondos en Banco",              DayOfWeek.MONDAY, 7,   0,
                    bodySets(3, 12));

        // ── Martes: Pierna + Abdomen ──────────────────────────────────────────
        addExercise(routine, "Sentadilla con Barra",         DayOfWeek.TUESDAY, 1, 120,
                    weightedSets(4, 6, 80.0));
        addExercise(routine, "Prensa de Pierna",             DayOfWeek.TUESDAY, 2,  90,
                    weightedSets(4, 8, 120.0));
        addExercise(routine, "Zancadas Caminando",           DayOfWeek.TUESDAY, 3,  75,
                    weightedSets(3, 10, 16.0));
        addExercise(routine, "Extensión de Piernas en Máquina", DayOfWeek.TUESDAY, 4, 60,
                    weightedSets(3, 12, 30.0));
        addExercise(routine, "Elevación de Talones de Pie",  DayOfWeek.TUESDAY, 5,  45,
                    weightedSets(4, 15, 20.0));
        // Circuito de abdomen — sin descanso entre sets, descanso al terminar el ejercicio
        addExercise(routine, "Crunch Declinado con Peso",    DayOfWeek.TUESDAY, 6,  90,
                    circuitSets(3, 15, 5.0));
        addExercise(routine, "Plancha",                      DayOfWeek.TUESDAY, 7,  60,
                    plankSets(3, 60L));
        addExercise(routine, "Rueda Abdominal",              DayOfWeek.TUESDAY, 8,  60,
                    circuitBodySets(3, 10));
        addExercise(routine, "Crunch Lateral",               DayOfWeek.TUESDAY, 9,   0,
                    circuitBodySets(3, 15));

        // ── Miércoles: Espalda + Bíceps ───────────────────────────────────────
        addExercise(routine, "Dominadas Lastradas",          DayOfWeek.WEDNESDAY, 1, 120,
                    weightedSets(4, 6, 10.0));
        addExercise(routine, "Remo con Barra",               DayOfWeek.WEDNESDAY, 2,  90,
                    weightedSets(4, 6, 60.0));
        addExercise(routine, "Jalón al Pecho",               DayOfWeek.WEDNESDAY, 3,  75,
                    weightedSets(4, 8, 40.0));
        addExercise(routine, "Remo en Máquina T",            DayOfWeek.WEDNESDAY, 4,  60,
                    weightedSets(3, 10, 50.0));
        addExercise(routine, "Curl con Barra",               DayOfWeek.WEDNESDAY, 5,  75,
                    weightedSets(4, 8, 30.0));
        addExercise(routine, "Curl con Mancuernas Tipo Martillo", DayOfWeek.WEDNESDAY, 6, 60,
                    weightedSets(3, 10, 12.0));
        addExercise(routine, "Curl Inclinado con Mancuernas", DayOfWeek.WEDNESDAY, 7,  0,
                    weightedSets(3, 12, 10.0));

        // ── Jueves: Hombros + Abdomen ─────────────────────────────────────────
        addExercise(routine, "Press Militar con Barra",      DayOfWeek.THURSDAY, 1, 90,
                    weightedSets(4, 6, 40.0));
        addExercise(routine, "Elevaciones Laterales",        DayOfWeek.THURSDAY, 2, 60,
                    weightedSets(4, 10, 8.0));
        addExercise(routine, "Elevaciones Frontales",        DayOfWeek.THURSDAY, 3, 60,
                    weightedSets(3, 12, 8.0));
        addExercise(routine, "Encogimientos de Hombros con Mancuernas", DayOfWeek.THURSDAY, 4, 45,
                    weightedSets(4, 15, 20.0));
        // Circuito de abdomen
        addExercise(routine, "Tijeras",                      DayOfWeek.THURSDAY, 5, 75,
                    circuitBodySets(3, 20));
        addExercise(routine, "Plancha Lateral",              DayOfWeek.THURSDAY, 6, 60,
                    plankSets(3, 45L));
        addExercise(routine, "Elevación de Piernas Colgado", DayOfWeek.THURSDAY, 7,  0,
                    circuitBodySets(3, 10));

        // ── Viernes: Pierna + Espalda ─────────────────────────────────────────
        addExercise(routine, "Sentadilla Búlgara",           DayOfWeek.FRIDAY, 1, 90,
                    weightedSets(4, 8, 20.0));
        addExercise(routine, "Curl Femoral Acostado",        DayOfWeek.FRIDAY, 2, 75,
                    weightedSets(4, 10, 25.0));
        addExercise(routine, "Prensa de Pierna",             DayOfWeek.FRIDAY, 3, 90,
                    weightedSets(4, 8, 120.0));
        addExercise(routine, "Elevación de Talones de Pie",  DayOfWeek.FRIDAY, 4, 45,
                    weightedSets(4, 15, 20.0));
        addExercise(routine, "Dominadas",                    DayOfWeek.FRIDAY, 5, 90,
                    bodySets(4, 8));
        addExercise(routine, "Remo con Barra",               DayOfWeek.FRIDAY, 6, 75,
                    weightedSets(4, 6, 60.0));
        addExercise(routine, "Pullover con Mancuerna",       DayOfWeek.FRIDAY, 7, 60,
                    weightedSets(3, 10, 18.0));
        addExercise(routine, "Remo en Máquina",              DayOfWeek.FRIDAY, 8,  0,
                    weightedSets(3, 10, 40.0));

        routineRepository.save(routine);
        log.info("Rutina '{}' creada con {} ejercicios.", ROUTINE_NAME,
                routine.getExercises().size());
    }


    private void addExercise(RoutineEntity routine,
                             String exerciseName,
                             DayOfWeek day,
                             int position,
                             int restAfterExercise,
                             List<RoutineSetTemplateEntity> sets) {

        ExerciseEntity exercise = exerciseRepository.findByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException(
                        "Ejercicio no encontrado: '" + exerciseName + "'"));

        RoutineExerciseEntity re = RoutineExerciseEntity.builder()
                .routine(routine)
                .exercise(exercise)
                .position(position)
                .sessionNumber(1) 
                .dayOfWeek(day)
                .restAfterExercise(restAfterExercise > 0 ? restAfterExercise : null)
                .build();

        sets.forEach(s -> {
            s.setRoutineExercise(re);
            if (s.getParameters() != null) {
                s.getParameters().forEach(p -> p.setSetTemplate(s));
            }
        });
        re.getSets().addAll(sets);
        routine.getExercises().add(re);
    }

    private List<RoutineSetTemplateEntity> weightedSets(int numSets, int reps, double kg) {
        List<RoutineSetTemplateEntity> list = new ArrayList<>();
        for (int i = 1; i <= numSets; i++) {
            boolean isLast = (i == numSets);
            RoutineSetTemplateEntity set = buildSet(i, SetType.NORMAL, isLast ? null : 0);
            set.setParameters(List.of(
                repsParam(set, reps),
                weightParam(set, kg)
            ));
            list.add(set);
        }
        return list;
    }

    private List<RoutineSetTemplateEntity> bodySets(int numSets, int reps) {
        List<RoutineSetTemplateEntity> list = new ArrayList<>();
        for (int i = 1; i <= numSets; i++) {
            boolean isLast = (i == numSets);
            RoutineSetTemplateEntity set = buildSet(i, SetType.NORMAL, isLast ? null : 0);
            set.setParameters(List.of(repsParam(set, reps)));
            list.add(set);
        }
        return list;
    }


    private List<RoutineSetTemplateEntity> circuitSets(int numSets, int reps, double kg) {
        List<RoutineSetTemplateEntity> list = new ArrayList<>();
        for (int i = 1; i <= numSets; i++) {
            RoutineSetTemplateEntity set = buildSet(i, SetType.NORMAL, null);
            set.setParameters(List.of(
                repsParam(set, reps),
                weightParam(set, kg)
            ));
            list.add(set);
        }
        return list;
    }

    private List<RoutineSetTemplateEntity> circuitBodySets(int numSets, int reps) {
        List<RoutineSetTemplateEntity> list = new ArrayList<>();
        for (int i = 1; i <= numSets; i++) {
            RoutineSetTemplateEntity set = buildSet(i, SetType.NORMAL, null);
            set.setParameters(List.of(repsParam(set, reps)));
            list.add(set);
        }
        return list;
    }

    private List<RoutineSetTemplateEntity> plankSets(int numSets, long seconds) {
        List<RoutineSetTemplateEntity> list = new ArrayList<>();
        for (int i = 1; i <= numSets; i++) {
            RoutineSetTemplateEntity set = buildSet(i, SetType.ISOMETRIC, null);
            set.setParameters(List.of(durationParam(set, seconds)));
            list.add(set);
        }
        return list;
    }


    private RoutineSetTemplateEntity buildSet(int position, SetType type, Integer restAfterSet) {
        RoutineSetTemplateEntity set = new RoutineSetTemplateEntity();
        set.setPosition(position);
        set.setSetType(type);
        set.setRestAfterSet(restAfterSet);
        set.setParameters(new ArrayList<>());
        return set;
    }

    private RoutineSetParameterEntity repsParam(RoutineSetTemplateEntity set, int reps) {
        RoutineSetParameterEntity p = new RoutineSetParameterEntity();
        p.setSetTemplate(set);
        p.setParameter(paramReps);
        p.setRepetitions(reps);
        return p;
    }

    private RoutineSetParameterEntity weightParam(RoutineSetTemplateEntity set, double kg) {
        RoutineSetParameterEntity p = new RoutineSetParameterEntity();
        p.setSetTemplate(set);
        p.setParameter(paramPeso);
        p.setNumericValue(kg);
        return p;
    }

    private RoutineSetParameterEntity durationParam(RoutineSetTemplateEntity set, long seconds) {
        RoutineSetParameterEntity p = new RoutineSetParameterEntity();
        p.setSetTemplate(set);
        p.setParameter(paramDuracion);
        p.setDurationValue(seconds);
        return p;
    }

    private CustomParameterEntity param(String name) {
        return paramRepository.findByNameAndIsGlobalTrue(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Parámetro global no encontrado: '" + name + "'"));
    } */  
}