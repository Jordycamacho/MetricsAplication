package com.fitapp.backend.routinecomplete.routine.aplication.service;

import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SetType;
import com.fitapp.backend.routinecomplete.routine.domain.model.RoutineModel;
import com.fitapp.backend.routinecomplete.routineexercise.domain.model.RoutineExerciseModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetParameterModel;
import com.fitapp.backend.routinecomplete.routinesetemplate.domain.model.RoutineSetTemplateModel;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Construye los modelos de dominio para las rutinas predefinidas opcionales.
 *
 * No toca la base de datos. Recibe todos los IDs resueltos desde
 * DefaultRoutineService
 * para no acoplar este componente a repositorios.
 *
 * exerciseIds → Map<nombreEjercicio, exerciseId>
 * paramIds → Map<nombreParámetro, parameterId>
 *
 * Regla de descanso:
 * - Sets intermedios → descanso definido por tipo (90s normal, 45s circuito,
 * etc.)
 * - Último set de un ejercicio → null (el descanso lo gestiona
 * restAfterExercise)
 * - restAfterExercise = 0 → se sustituye por 120s
 */
@Component
public class DefaultRoutineGenerator {

        // ── API pública ───────────────────────────────────────────────────────────

        public RoutineModel buildGymRoutine(Long userId, Long gymSportId,
                        Map<String, Long> exerciseIds,
                        Map<String, Long> paramIds) {
                Long repsId = paramIds.get("Repeticiones");
                Long pesoId = paramIds.get("Peso");
                Long duracionId = paramIds.get("Duración");

                RoutineModel routine = new RoutineModel();
                routine.setName("Push / Pull / Legs — Gym");
                routine.setDescription("Rutina clásica de 5 días dividida por grupos musculares. " +
                                "Ideal desde principiantes hasta nivel avanzado.");
                routine.setUserId(userId);
                routine.setSportId(gymSportId);
                routine.setIsActive(true);
                routine.setSessionsPerWeek(5);
                routine.setGoal("Hipertrofia y fuerza general con progresión lineal por grupos musculares");
                routine.setTrainingDays(Set.of(
                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

                List<RoutineExerciseModel> exercises = new ArrayList<>();

                // ── Lunes: Pecho + Tríceps ────────────────────────────────────────────
                exercises.add(ex(exerciseIds, "Press de Banca", DayOfWeek.MONDAY, 1, 90,
                                weightedSets(4, 6, 60.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Press Inclinado con Barra", DayOfWeek.MONDAY, 2, 90,
                                weightedSets(4, 8, 50.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Fondos en Paralelas", DayOfWeek.MONDAY, 3, 90, bodySets(3, 8, repsId)));
                exercises.add(ex(exerciseIds, "Fly con Mancuernas", DayOfWeek.MONDAY, 4, 60,
                                weightedSets(3, 10, 14.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Skull Crushers", DayOfWeek.MONDAY, 5, 75,
                                weightedSets(4, 8, 20.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Jalón de Tríceps con Cuerda", DayOfWeek.MONDAY, 6, 60,
                                dropSets(4, 12, 15.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Fondos en Banco", DayOfWeek.MONDAY, 7, 120, bodySets(3, 12, repsId)));

                // ── Martes: Pierna + Abdomen (circuito) ───────────────────────────────
                exercises.add(ex(exerciseIds, "Sentadilla con Barra", DayOfWeek.TUESDAY, 1, 120,
                                weightedSets(4, 6, 80.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Prensa de Pierna", DayOfWeek.TUESDAY, 2, 90,
                                weightedSets(4, 8, 120.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Zancadas Caminando", DayOfWeek.TUESDAY, 3, 75,
                                weightedSets(3, 10, 16.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Extensión de Piernas en Máquina", DayOfWeek.TUESDAY, 4, 60,
                                weightedSets(3, 12, 30.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Elevación de Talones de Pie", DayOfWeek.TUESDAY, 5, 45,
                                weightedSets(4, 15, 20.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Crunch Declinado con Peso", DayOfWeek.TUESDAY, 6, 90,
                                circuitWeightedSets(3, 15, 5.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Plancha", DayOfWeek.TUESDAY, 7, 60, plankSets(3, 60L, duracionId)));
                exercises.add(ex(exerciseIds, "Rueda Abdominal", DayOfWeek.TUESDAY, 8, 60,
                                circuitBodySets(3, 10, repsId)));
                exercises.add(ex(exerciseIds, "Crunch Lateral", DayOfWeek.TUESDAY, 9, 120,
                                circuitBodySets(3, 15, repsId)));

                // ── Miércoles: Espalda + Bíceps ───────────────────────────────────────
                exercises.add(ex(exerciseIds, "Dominadas Lastradas", DayOfWeek.WEDNESDAY, 1, 120,
                                weightedSets(4, 6, 10.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Remo con Barra", DayOfWeek.WEDNESDAY, 2, 90,
                                weightedSets(4, 6, 60.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Jalón al Pecho", DayOfWeek.WEDNESDAY, 3, 75,
                                dropSets(4, 10, 40.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Remo en Máquina T", DayOfWeek.WEDNESDAY, 4, 60,
                                weightedSets(3, 10, 50.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Curl con Barra", DayOfWeek.WEDNESDAY, 5, 75,
                                weightedSets(4, 8, 30.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Curl con Mancuernas Tipo Martillo", DayOfWeek.WEDNESDAY, 6, 60,
                                weightedSets(3, 10, 12.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Curl Inclinado con Mancuernas", DayOfWeek.WEDNESDAY, 7, 120,
                                weightedSets(3, 12, 10.0, repsId, pesoId)));

                // ── Jueves: Hombros + Abdomen (circuito) ──────────────────────────────
                exercises.add(ex(exerciseIds, "Press Militar con Barra", DayOfWeek.THURSDAY, 1, 90,
                                weightedSets(4, 6, 40.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Elevaciones Laterales", DayOfWeek.THURSDAY, 2, 60,
                                weightedSets(4, 10, 8.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Elevaciones Frontales", DayOfWeek.THURSDAY, 3, 60,
                                weightedSets(3, 12, 8.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Encogimientos de Hombros con Mancuernas", DayOfWeek.THURSDAY, 4, 45,
                                weightedSets(4, 15, 20.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Tijeras", DayOfWeek.THURSDAY, 5, 75, circuitBodySets(3, 20, repsId)));
                exercises.add(ex(exerciseIds, "Plancha Lateral", DayOfWeek.THURSDAY, 6, 60,
                                plankSets(3, 45L, duracionId)));
                exercises.add(ex(exerciseIds, "Elevación de Piernas Colgado", DayOfWeek.THURSDAY, 7, 120,
                                circuitBodySets(3, 10, repsId)));

                // ── Viernes: Pierna + Espalda ─────────────────────────────────────────
                exercises.add(ex(exerciseIds, "Sentadilla Búlgara", DayOfWeek.FRIDAY, 1, 90,
                                weightedSets(4, 8, 20.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Curl Femoral Acostado", DayOfWeek.FRIDAY, 2, 75,
                                weightedSets(4, 10, 25.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Prensa de Pierna", DayOfWeek.FRIDAY, 3, 90,
                                weightedSets(4, 8, 120.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Elevación de Talones de Pie", DayOfWeek.FRIDAY, 4, 45,
                                weightedSets(4, 15, 20.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Dominadas", DayOfWeek.FRIDAY, 5, 90, bodySets(4, 8, repsId)));
                exercises.add(ex(exerciseIds, "Remo con Barra", DayOfWeek.FRIDAY, 6, 75,
                                weightedSets(4, 6, 60.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Pullover con Mancuerna", DayOfWeek.FRIDAY, 7, 60,
                                weightedSets(3, 10, 18.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Remo en Máquina", DayOfWeek.FRIDAY, 8, 120,
                                weightedSets(3, 10, 40.0, repsId, pesoId)));

                routine.setExercises(exercises);
                return routine;
        }

        public RoutineModel buildBoxingRoutine(Long userId, Long boxeoSportId,
                        Map<String, Long> exerciseIds,
                        Map<String, Long> paramIds) {
                Long repsId = paramIds.get("Repeticiones");
                Long pesoId = paramIds.get("Peso");
                Long duracionId = paramIds.get("Duración");

                RoutineModel routine = new RoutineModel();
                routine.setName("Boxeo — Técnica y Acondicionamiento");
                routine.setDescription("Rutina de 4 días que combina técnica de boxeo, fuerza auxiliar y " +
                                "acondicionamiento. Válida para principiantes y nivel medio.");
                routine.setUserId(userId);
                routine.setSportId(boxeoSportId);
                routine.setIsActive(true);
                routine.setSessionsPerWeek(4);
                routine.setGoal("Desarrollar técnica de boxeo, potencia de golpe y resistencia específica");
                routine.setTrainingDays(Set.of(
                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

                List<RoutineExerciseModel> exercises = new ArrayList<>();

                // ── Lunes: Técnica + Fuerza Upper Body ───────────────────────────────
                exercises.add(ex(exerciseIds, "Salto a la Comba", DayOfWeek.MONDAY, 1, 60,
                                roundSets(3, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Shadowboxing", DayOfWeek.MONDAY, 2, 60, roundSets(3, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Jab – Cross", DayOfWeek.MONDAY, 3, 60, techniqueSets(4, 20, repsId)));
                exercises.add(ex(exerciseIds, "Combo 1-2-3-2 (Jab-Cross-Hook-Cross)", DayOfWeek.MONDAY, 4, 60,
                                techniqueSets(4, 15, repsId)));
                exercises.add(ex(exerciseIds, "Press de Banca con Mancuernas", DayOfWeek.MONDAY, 5, 75,
                                weightedSets(4, 10, 22.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Remo con Mancuernas (Boxeo)", DayOfWeek.MONDAY, 6, 60,
                                weightedSets(3, 12, 18.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Flexiones Explosivas", DayOfWeek.MONDAY, 7, 120,
                                circuitBodySets(3, 8, repsId)));

                // ── Martes: Acondicionamiento + Core ─────────────────────────────────
                exercises.add(ex(exerciseIds, "Salto a la Comba", DayOfWeek.TUESDAY, 1, 30,
                                roundSets(4, 120L, duracionId)));
                exercises.add(ex(exerciseIds, "Burpees", DayOfWeek.TUESDAY, 2, 60, hiitSets(4, 10, repsId)));
                exercises.add(ex(exerciseIds, "Rounds de Saco (Cardio)", DayOfWeek.TUESDAY, 3, 60,
                                roundSets(3, 120L, duracionId)));
                exercises.add(ex(exerciseIds, "Planchas con Rotación", DayOfWeek.TUESDAY, 4, 90,
                                circuitBodySets(3, 12, repsId)));
                exercises.add(ex(exerciseIds, "Medicine Ball Slam", DayOfWeek.TUESDAY, 5, 75,
                                weightedSets(3, 15, 6.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Mountain Climbers", DayOfWeek.TUESDAY, 6, 120,
                                timedCircuitSets(3, 30L, duracionId)));

                // ── Jueves: Técnica Avanzada + Saco ──────────────────────────────────
                exercises.add(ex(exerciseIds, "Salto a la Comba", DayOfWeek.THURSDAY, 1, 60,
                                roundSets(3, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Shadowboxing", DayOfWeek.THURSDAY, 2, 60,
                                roundSets(4, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Combinaciones en Saco", DayOfWeek.THURSDAY, 3, 60,
                                roundSets(4, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Defensa y Contraataque", DayOfWeek.THURSDAY, 4, 60,
                                roundSets(3, 120L, duracionId)));
                exercises.add(ex(exerciseIds, "Sprints de Velocidad", DayOfWeek.THURSDAY, 5, 90,
                                sprintSets(4, 30L, duracionId)));
                exercises.add(ex(exerciseIds, "Planchas con Rotación", DayOfWeek.THURSDAY, 6, 120,
                                circuitBodySets(3, 15, repsId)));

                // ── Viernes: Fuerza Full Body + Resistencia ───────────────────────────
                exercises.add(ex(exerciseIds, "Press Militar con Barra", DayOfWeek.FRIDAY, 1, 90,
                                weightedSets(4, 8, 35.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Dominadas", DayOfWeek.FRIDAY, 2, 90, bodySets(4, 8, repsId)));
                exercises.add(ex(exerciseIds, "Medicine Ball Slam", DayOfWeek.FRIDAY, 3, 60,
                                weightedSets(3, 12, 6.0, repsId, pesoId)));
                exercises.add(ex(exerciseIds, "Burpees", DayOfWeek.FRIDAY, 4, 60, hiitSets(3, 10, repsId)));
                exercises.add(ex(exerciseIds, "Rounds de Saco (Cardio)", DayOfWeek.FRIDAY, 5, 60,
                                roundSets(3, 180L, duracionId)));
                exercises.add(ex(exerciseIds, "Salto a la Comba", DayOfWeek.FRIDAY, 6, 120,
                                roundSets(2, 120L, duracionId)));

                routine.setExercises(exercises);
                return routine;
        }

        // ── Helper de ejercicio ───────────────────────────────────────────────────

        private RoutineExerciseModel ex(Map<String, Long> exerciseIds,
                        String name,
                        DayOfWeek day,
                        int position,
                        int restAfterExercise,
                        List<RoutineSetTemplateModel> sets) {
                Long exerciseId = exerciseIds.get(name);
                if (exerciseId == null) {
                        throw new IllegalStateException(
                                        "ID no encontrado para ejercicio: '" + name + "'. " +
                                                        "Verifica que ExerciseDataLoader se ejecutó correctamente.");
                }

                RoutineExerciseModel re = new RoutineExerciseModel();
                re.setExerciseId(exerciseId);
                re.setDayOfWeek(day);
                re.setPosition(position);
                re.setSessionNumber(1);
                re.setRestAfterExercise(restAfterExercise > 0 ? restAfterExercise : 120);
                re.setSets(sets);
                return re;
        }

        // ── Constructores de sets ─────────────────────────────────────────────────

        /**
         * Drop set descendente: sin descanso entre sets intermedios (esencia del drop).
         * Solo el último set deja null para que tome el restAfterExercise.
         */
        private List<RoutineSetTemplateModel> dropSets(int n, int reps, double kgInicial,
                        Long repsId, Long pesoId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                double kg = kgInicial;
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.DROP_SET.name(), i < n ? 0 : null);
                        s.setParameters(List.of(
                                        repsParam(repsId, reps),
                                        weightParam(pesoId, Math.round(kg * 2) / 2.0)));
                        list.add(s);
                        kg *= 0.80;
                }
                return list;
        }

        private List<RoutineSetTemplateModel> weightedSets(int n, int reps, double kg,
                        Long repsId, Long pesoId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 90 : null);
                        s.setParameters(List.of(repsParam(repsId, reps), weightParam(pesoId, kg)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> bodySets(int n, int reps, Long repsId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 90 : null);
                        s.setParameters(List.of(repsParam(repsId, reps)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> circuitWeightedSets(int n, int reps, double kg,
                        Long repsId, Long pesoId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 45 : null);
                        s.setParameters(List.of(repsParam(repsId, reps), weightParam(pesoId, kg)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> circuitBodySets(int n, int reps, Long repsId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 45 : null);
                        s.setParameters(List.of(repsParam(repsId, reps)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> plankSets(int n, long seconds, Long duracionId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.ISOMETRIC.name(), i < n ? 45 : null);
                        s.setParameters(List.of(durationParam(duracionId, seconds)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> roundSets(int n, long seconds, Long duracionId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 60 : null);
                        s.setParameters(List.of(durationParam(duracionId, seconds)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> techniqueSets(int n, int reps, Long repsId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 45 : null);
                        s.setParameters(List.of(repsParam(repsId, reps)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> hiitSets(int n, int reps, Long repsId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 30 : null);
                        s.setParameters(List.of(repsParam(repsId, reps)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> timedCircuitSets(int n, long seconds, Long duracionId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 45 : null);
                        s.setParameters(List.of(durationParam(duracionId, seconds)));
                        list.add(s);
                }
                return list;
        }

        private List<RoutineSetTemplateModel> sprintSets(int n, long seconds, Long duracionId) {
                List<RoutineSetTemplateModel> list = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                        RoutineSetTemplateModel s = set(i, SetType.NORMAL.name(), i < n ? 90 : null);
                        s.setParameters(List.of(durationParam(duracionId, seconds)));
                        list.add(s);
                }
                return list;
        }

        // ── Builders de modelos base ──────────────────────────────────────────────

        private RoutineSetTemplateModel set(int position, String type, Integer restAfterSet) {
                RoutineSetTemplateModel s = new RoutineSetTemplateModel();
                s.setPosition(position);
                s.setSetType(type);
                s.setRestAfterSet(restAfterSet);
                s.setSubSetNumber(1);
                return s;
        }

        private RoutineSetParameterModel repsParam(Long parameterId, int reps) {
                RoutineSetParameterModel p = new RoutineSetParameterModel();
                p.setParameterId(parameterId);
                p.setRepetitions(reps);
                return p;
        }

        private RoutineSetParameterModel weightParam(Long parameterId, double kg) {
                RoutineSetParameterModel p = new RoutineSetParameterModel();
                p.setParameterId(parameterId);
                p.setNumericValue(kg);
                return p;
        }

        private RoutineSetParameterModel durationParam(Long parameterId, long seconds) {
                RoutineSetParameterModel p = new RoutineSetParameterModel();
                p.setParameterId(parameterId);
                p.setDurationValue(seconds);
                return p;
        }
}