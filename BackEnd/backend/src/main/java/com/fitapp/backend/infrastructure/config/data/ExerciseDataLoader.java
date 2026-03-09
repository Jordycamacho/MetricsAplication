package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.infrastructure.persistence.entity.ExerciseEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import com.fitapp.backend.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseCategoryRepository;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Carga los ejercicios predefinidos del sistema.
 *
 * Cubre tres bloques:
 *   1. Musculación — ejercicios de la rutina Push/Pull/Legs
 *   2. Boxeo       — técnica, acondicionamiento y fuerza auxiliar
 *   3. Generales   — running, natación, atletismo y otros deportes medibles
 *
 * Todos: createdBy=null, isPublic=true, isActive=true.
 *
 * Depende de: SportDataLoader (@Order 2), ParameterDataLoader (@Order 3),
 *             ExerciseCategoryDataLoader (@Order 4)
 */
@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class ExerciseDataLoader implements ApplicationRunner {

    private final ExerciseRepository        exerciseRepository;
    private final SportRepository           sportRepository;
    private final ExerciseCategoryRepository categoryRepository;
    private final CustomParameterRepository parameterRepository;

    // ── Refs cargadas en init ──────────────────────────────────────────────────
    private SportEntity              gymSport;
    private SportEntity              boxeoSport;
    private SportEntity              runningSport;
    private SportEntity              natacionSport;
    private SportEntity              atletismoSport;
    private SportEntity              ciclismoSport;

    private ExerciseCategoryEntity   catPecho;
    private ExerciseCategoryEntity   catEspalda;
    private ExerciseCategoryEntity   catHombros;
    private ExerciseCategoryEntity   catBiceps;
    private ExerciseCategoryEntity   catTriceps;
    private ExerciseCategoryEntity   catPiernas;
    private ExerciseCategoryEntity   catGluteos;
    private ExerciseCategoryEntity   catCore;
    private ExerciseCategoryEntity   catGemelos;
    private ExerciseCategoryEntity   catEmpuje;
    private ExerciseCategoryEntity   catTiron;
    private ExerciseCategoryEntity   catBisagra;
    private ExerciseCategoryEntity   catSentadilla;
    private ExerciseCategoryEntity   catPliometria;
    private ExerciseCategoryEntity   catCardio;
    private ExerciseCategoryEntity   catHIIT;
    private ExerciseCategoryEntity   catResAerobica;
    private ExerciseCategoryEntity   catSprints;
    private ExerciseCategoryEntity   catTecBoxeo;
    private ExerciseCategoryEntity   catTecPatadas;
    private ExerciseCategoryEntity   catSacoManoplas;
    private ExerciseCategoryEntity   catFuncional;
    private ExerciseCategoryEntity   catCalentamiento;
    private ExerciseCategoryEntity   catMovilidad;
    private ExerciseCategoryEntity   catFlexibilidad;
    private ExerciseCategoryEntity   catRecuperacion;
    private ExerciseCategoryEntity   catTecNatacion;
    private ExerciseCategoryEntity   catSeriesNatacion;
    private ExerciseCategoryEntity   catCiclismoIntervalos;
    private ExerciseCategoryEntity   catCiclismoResistencia;
    private ExerciseCategoryEntity   catCarreraVelocidad;
    private ExerciseCategoryEntity   catCarreraFondo;

    private CustomParameterEntity    paramReps;
    private CustomParameterEntity    paramPeso;
    private CustomParameterEntity    paramDuracion;
    private CustomParameterEntity    paramDistancia;
    private CustomParameterEntity    paramVelocidad;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (exerciseRepository.count() > 0) {
            log.info("Ejercicios ya inicializados, saltando seed.");
            return;
        }

        log.info("Inicializando ejercicios predefinidos...");
        loadRefs();

        int total = 0;
        total += saveAll(buildGymChestTriceps());
        total += saveAll(buildGymLegsAbs());
        total += saveAll(buildGymBackBiceps());
        total += saveAll(buildGymShoulders());
        total += saveAll(buildBoxeo());
        total += saveAll(buildRunning());
        total += saveAll(buildNatacion());
        total += saveAll(buildCiclismo());
        total += saveAll(buildGeneralConditioning());

        log.info("Ejercicios predefinidos inicializados correctamente: {} ejercicios", total);
    }

    // ── Carga de referencias ──────────────────────────────────────────────────

    private void loadRefs() {
        // Deportes
        gymSport       = sport("Musculación");
        boxeoSport     = sport("Boxeo");
        runningSport   = sport("Running");
        natacionSport  = sport("Natación");
        atletismoSport = sport("Atletismo – Saltos");   // reutilizamos para atletismo general
        ciclismoSport  = sport("Ciclismo Indoor");

        // Categorías
        catPecho               = cat("Pecho");
        catEspalda             = cat("Espalda");
        catHombros             = cat("Hombros");
        catBiceps              = cat("Bíceps");
        catTriceps             = cat("Tríceps");
        catPiernas             = cat("Piernas");
        catGluteos             = cat("Glúteos");
        catCore                = cat("Core");
        catGemelos             = cat("Gemelos");
        catEmpuje              = cat("Empuje");
        catTiron               = cat("Tirón");
        catBisagra             = cat("Bisagra de Cadera");
        catSentadilla          = cat("Sentadilla");
        catPliometria          = cat("Pliometría");
        catCardio              = cat("Cardio");
        catHIIT                = cat("HIIT");
        catResAerobica         = cat("Resistencia Aeróbica");
        catSprints             = cat("Sprints");
        catTecBoxeo            = cat("Técnica de Boxeo");
        catTecPatadas          = cat("Técnica de Patadas");
        catSacoManoplas        = cat("Saco y Manoplas");
        catFuncional           = cat("Funcional");
        catCalentamiento       = cat("Calentamiento");
        catMovilidad           = cat("Movilidad");
        catFlexibilidad        = cat("Flexibilidad");
        catRecuperacion        = cat("Recuperación Activa");
        catTecNatacion         = cat("Técnica de Natación");
        catSeriesNatacion      = cat("Series de Natación");
        catCiclismoIntervalos  = cat("Ciclismo Intervalos");
        catCiclismoResistencia = cat("Ciclismo Resistencia");
        catCarreraVelocidad    = cat("Carrera de Velocidad");
        catCarreraFondo        = cat("Carrera de Fondo");

        // Parámetros
        paramReps      = param("Repeticiones");
        paramPeso      = param("Peso");
        paramDuracion  = param("Duración");
        paramDistancia = param("Distancia");
        paramVelocidad = param("Velocidad");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 1 — MUSCULACIÓN: PECHO + TRÍCEPS  (Lunes)
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildGymChestTriceps() {
        return List.of(

            // ── Pecho ────────────────────────────────────────────────────────
            exercise(
                "Press de Banca",
                "Ejercicio compuesto de empuje horizontal. Tumbado en banco, baja la barra hasta el pecho y empuja.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Press Inclinado con Barra",
                "Press de banca en banco inclinado 30-45°. Enfatiza la porción superior del pectoral.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Press Inclinado con Mancuernas",
                "Versión con mancuernas del press inclinado. Mayor rango de movimiento y trabajo de estabilizadores.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Fondos en Paralelas",
                "Ejercicio de peso corporal o lastrado. Baja controlando los codos y empuja hasta la extensión completa.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho, catTriceps, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Fly con Mancuernas",
                "Apertura con mancuernas en banco plano. Aislamiento del pectoral con énfasis en el estiramiento.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Crossover en Poleas",
                "Cruce de poleas para aislamiento del pectoral. Permite ajustar el ángulo de trabajo.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPecho),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Flexiones",
                "Empuje de peso corporal. Variante fundamental que trabaja pecho, hombros y tríceps.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catPecho, catEmpuje, catFuncional),
                params(paramReps)
            ),

            // ── Tríceps ───────────────────────────────────────────────────────
            exercise(
                "Skull Crushers",
                "Extensión de codos tumbado con barra EZ. Aislamiento intenso del tríceps. Controla la bajada.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catTriceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Jalón de Tríceps con Cuerda",
                "Extensión en polea alta con cuerda. Apertura al final del movimiento para mayor contracción.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catTriceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Fondos en Banco",
                "Extensión de tríceps apoyado en banco. Ajusta el nivel de dificultad elevando los pies.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catTriceps),
                params(paramReps)
            ),
            exercise(
                "Press Francés con Mancuerna",
                "Extensión de tríceps sobre la cabeza con una mancuerna a dos manos.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catTriceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Jalón de Tríceps con Barra",
                "Extensión en polea alta con barra recta o EZ. Variante clásica del jalón de tríceps.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catTriceps),
                params(paramReps, paramPeso)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 2 — MUSCULACIÓN: PIERNAS + ABDOMEN  (Martes y Viernes)
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildGymLegsAbs() {
        return List.of(

            // ── Piernas ───────────────────────────────────────────────────────
            exercise(
                "Sentadilla con Barra",
                "Ejercicio rey del tren inferior. Barra en trapecio, baja hasta paralelo o por debajo.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catSentadilla, catGluteos),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Prensa de Pierna",
                "Empuje en máquina de prensa. Permite trabajar alto volumen con menos carga en la columna.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catGluteos),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Zancadas Caminando",
                "Lunges dinámicos en movimiento. Trabaja cuádriceps, glúteos e isquios unilateralmente.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catGluteos, catFuncional),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Extensión de Piernas en Máquina",
                "Aislamiento del cuádriceps en máquina de extensión.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Elevación de Talones de Pie",
                "Trabajo de gemelos en máquina o libre. Sube en puntillas y baja con control.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catGemelos),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Sentadilla Búlgara",
                "Split squat con pie trasero elevado en banco. Alta demanda de fuerza unilateral y equilibrio.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catGluteos, catSentadilla),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Curl Femoral Acostado",
                "Flexión de rodilla en máquina. Aislamiento de isquiotibiales.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Peso Muerto Rumano",
                "Bisagra de cadera con barra o mancuernas. Énfasis en isquiotibiales y glúteos.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catGluteos, catBisagra),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Peso Muerto Convencional",
                "Ejercicio compuesto de cadena posterior. Barra desde el suelo hasta la extensión completa.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catEspalda, catGluteos, catBisagra),
                params(paramReps, paramPeso)
            ),

            // ── Core / Abdomen ────────────────────────────────────────────────
            exercise(
                "Crunch Declinado con Peso",
                "Flexión de tronco en banco declinado con disco o mancuerna en el pecho.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catCore),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Plancha",
                "Isométrico de core. Mantén la alineación neutra de columna durante el tiempo indicado.",
                ExerciseType.TIMED,
                sports(gymSport),
                cats(catCore),
                params(paramDuracion)
            ),
            exercise(
                "Rueda Abdominal",
                "Rollout con rueda abdominal. Alta demanda de core y hombros. Empieza desde rodillas.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catCore),
                params(paramReps)
            ),
            exercise(
                "Crunch Lateral",
                "Flexión lateral de tronco para trabajar oblicuos.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catCore),
                params(paramReps)
            ),
            exercise(
                "Tijeras",
                "Movimiento alterno de piernas en el suelo. Trabaja abdomen inferior y flexores de cadera.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catCore),
                params(paramReps)
            ),
            exercise(
                "Plancha Lateral",
                "Isométrico lateral. Trabaja oblicuos y estabilizadores laterales del tronco.",
                ExerciseType.TIMED,
                sports(gymSport),
                cats(catCore),
                params(paramDuracion)
            ),
            exercise(
                "Elevación de Piernas Colgado",
                "Colgado en barra, eleva las piernas hasta la horizontal o la vertical.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catCore),
                params(paramReps)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 3 — MUSCULACIÓN: ESPALDA + BÍCEPS  (Miércoles)
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildGymBackBiceps() {
        return List.of(

            // ── Espalda ───────────────────────────────────────────────────────
            exercise(
                "Dominadas",
                "Tirón vertical con peso corporal. Agarre prono o supino. Base del trabajo de espalda.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Dominadas Lastradas",
                "Dominadas con peso adicional en cinturón. Variante de progresión de fuerza.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Remo con Barra",
                "Tirón horizontal con barra. Torso inclinado, barra al abdomen. Trabaja toda la espalda media.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Jalón al Pecho",
                "Tirón vertical en polea alta. Baja la barra hasta el pecho con los codos hacia abajo.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Remo en Máquina T",
                "Remo en máquina con soporte de pecho. Permite mayor carga con menos riesgo lumbar.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Remo en Máquina",
                "Remo en máquina de palanca o polea. Variante guiada del remo horizontal.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Pullover con Mancuerna",
                "Tumbado en banco, lleva la mancuerna en arco por encima de la cabeza. Trabaja dorsal y serrato.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catPecho),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Remo con Mancuerna",
                "Remo unilateral apoyado en banco. Permite mayor rango de movimiento que el remo con barra.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),

            // ── Bíceps ────────────────────────────────────────────────────────
            exercise(
                "Curl con Barra",
                "Curl de bíceps con barra recta o EZ. Ejercicio base de bíceps.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBiceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Curl con Mancuernas Tipo Martillo",
                "Curl con agarre neutro. Trabaja bíceps y braquiorradial por igual.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBiceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Curl Inclinado con Mancuernas",
                "Curl en banco inclinado. Estiramiento máximo del bíceps al inicio del movimiento.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBiceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Curl en Predicador",
                "Curl con apoyo en banco predicador. Elimina el balanceo y aísla el bíceps.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBiceps),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Curl en Polea",
                "Curl de bíceps en polea baja. Tensión constante durante todo el recorrido.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBiceps),
                params(paramReps, paramPeso)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 4 — MUSCULACIÓN: HOMBROS  (Jueves)
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildGymShoulders() {
        return List.of(

            exercise(
                "Press Militar con Barra",
                "Empuje vertical de pie o sentado con barra. Ejercicio compuesto de hombros.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Press de Hombros con Mancuernas",
                "Empuje vertical con mancuernas. Mayor rango de movimiento y trabajo de estabilizadores.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Elevaciones Laterales",
                "Aislamiento del deltoides lateral. Brazos ligeramente flexionados, sube hasta la horizontal.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Elevaciones Frontales",
                "Elevación frontal con mancuerna o barra. Trabaja el deltoides anterior.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Encogimientos de Hombros con Mancuernas",
                "Shrugs con mancuernas. Trabaja trapecio superior. Sube el hombro sin rotar.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros, catEspalda),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Face Pull",
                "Tirón de polea alta hacia la cara. Trabaja deltoides posterior y manguito rotador.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros, catEspalda),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Pájaro con Mancuernas",
                "Apertura en posición inclinada. Aislamiento del deltoides posterior.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catHombros),
                params(paramReps, paramPeso)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 5 — BOXEO
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildBoxeo() {
        return List.of(

            // ── Técnica de golpes ──────────────────────────────────────────────
            exercise(
                "Shadowboxing",
                "Boxeo en el aire sin oponente. Trabaja técnica, fluidez, desplazamiento y guardia.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catTecBoxeo, catCardio),
                params(paramDuracion)
            ),
            exercise(
                "Combinaciones en Saco",
                "Series de golpes (jab, cross, hook, uppercut) sobre el saco de boxeo.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catSacoManoplas, catTecBoxeo),
                params(paramDuracion)
            ),
            exercise(
                "Trabajo con Manoplas",
                "Combinaciones dirigidas por el entrenador con manoplas. Mejora precisión y velocidad.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catSacoManoplas, catTecBoxeo),
                params(paramDuracion)
            ),
            exercise(
                "Jab – Cross",
                "Combinación básica 1-2. Enfatiza la correcta rotación de cadera y extensión del golpe.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catTecBoxeo),
                params(paramDuracion, paramReps)
            ),
            exercise(
                "Combo 1-2-3-2 (Jab-Cross-Hook-Cross)",
                "Combinación de cuatro golpes. Trabajo de fluidez y transición entre manos.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catTecBoxeo, catSacoManoplas),
                params(paramDuracion, paramReps)
            ),
            exercise(
                "Defensa y Contraataque",
                "Trabajo de esquivas, bloqueos y contragolpes. Mejora el timing defensivo.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catTecBoxeo),
                params(paramDuracion)
            ),

            // ── Acondicionamiento específico de boxeo ──────────────────────────
            exercise(
                "Salto a la Comba",
                "Ejercicio de coordinación, resistencia y pies rápidos. Fundamental en el boxeo.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catCardio, catCalentamiento, catFuncional),
                params(paramDuracion)
            ),
            exercise(
                "Rounds de Saco (Cardio)",
                "Rounds continuos sobre el saco a ritmo de combate. Construye resistencia específica.",
                ExerciseType.TIMED,
                sports(boxeoSport),
                cats(catSacoManoplas, catCardio, catHIIT),
                params(paramDuracion)
            ),
            exercise(
                "Sprints de Velocidad",
                "Aceleraciones cortas de 20-40 m. Trabajan la explosividad y el sistema anaeróbico.",
                ExerciseType.TIMED,
                sports(boxeoSport, runningSport),
                cats(catSprints, catCardio),
                params(paramDuracion, paramDistancia)
            ),

            // ── Fuerza auxiliar para boxeo ─────────────────────────────────────
            exercise(
                "Press de Banca con Mancuernas",
                "Empuje horizontal con mancuernas. Desarrollo de la fuerza de golpe horizontal.",
                ExerciseType.WEIGHTED,
                sports(boxeoSport, gymSport),
                cats(catPecho, catEmpuje),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Remo con Mancuernas (Boxeo)",
                "Remo unilateral que trabaja la cadena de tirón, clave para la recuperación del golpe.",
                ExerciseType.WEIGHTED,
                sports(boxeoSport, gymSport),
                cats(catEspalda, catTiron),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Medicine Ball Slam",
                "Lanzamiento de balón medicinal contra el suelo. Potencia, core y acondicionamiento.",
                ExerciseType.WEIGHTED,
                sports(boxeoSport),
                cats(catCore, catFuncional, catHIIT),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Burpees",
                "Ejercicio de cuerpo completo de alta intensidad. Resistencia y acondicionamiento general.",
                ExerciseType.BODYWEIGHT,
                sports(boxeoSport, gymSport),
                cats(catCardio, catHIIT, catFuncional),
                params(paramReps, paramDuracion)
            ),
            exercise(
                "Planchas con Rotación",
                "Plancha con giro del tronco. Trabaja el core rotacional, esencial para la potencia de golpe.",
                ExerciseType.BODYWEIGHT,
                sports(boxeoSport, gymSport),
                cats(catCore),
                params(paramReps)
            ),
            exercise(
                "Flexiones Explosivas",
                "Flexiones con impulso y despegue de manos. Desarrolla la explosividad del tren superior.",
                ExerciseType.BODYWEIGHT,
                sports(boxeoSport, gymSport),
                cats(catPecho, catEmpuje, catPliometria),
                params(paramReps)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 6 — RUNNING
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildRunning() {
        return List.of(

            exercise(
                "Rodaje Continuo",
                "Carrera continua a ritmo aeróbico suave (zona 2). Base de cualquier plan de running.",
                ExerciseType.DISTANCE,
                sports(runningSport),
                cats(catResAerobica, catCardio),
                params(paramDistancia, paramDuracion, paramVelocidad)
            ),
            exercise(
                "Intervalos de Velocidad",
                "Series cortas a ritmo alto (400-1000 m) con recuperación. Mejora VO2max y umbral anaeróbico.",
                ExerciseType.DISTANCE,
                sports(runningSport),
                cats(catCarreraVelocidad, catHIIT),
                params(paramDistancia, paramDuracion, paramVelocidad)
            ),
            exercise(
                "Tirada Larga",
                "Rodaje largo a ritmo suave. Desarrolla la resistencia aeróbica de base.",
                ExerciseType.DISTANCE,
                sports(runningSport),
                cats(catResAerobica, catCarreraFondo),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Fartlek",
                "Juego de ritmos: alternancia libre entre esfuerzos cortos de alta intensidad y recuperación.",
                ExerciseType.TIMED,
                sports(runningSport),
                cats(catCarreraFondo, catHIIT),
                params(paramDuracion, paramDistancia)
            ),
            exercise(
                "Progresivo",
                "Rodaje que empieza suave y termina al ritmo de carrera objetivo o más rápido.",
                ExerciseType.DISTANCE,
                sports(runningSport),
                cats(catCarreraFondo, catResAerobica),
                params(paramDistancia, paramDuracion, paramVelocidad)
            ),
            exercise(
                "Series de 400 m",
                "Repeticiones de 400 m a ritmo de 5K o más rápido con descanso entre series.",
                ExerciseType.DISTANCE,
                sports(runningSport),
                cats(catCarreraVelocidad, catSprints),
                params(paramDistancia, paramDuracion, paramVelocidad)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 7 — NATACIÓN
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildNatacion() {
        return List.of(

            exercise(
                "Crol (Freestyle)",
                "Nado libre a crol. Estilo más rápido y eficiente. Trabaja todo el cuerpo.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catTecNatacion, catResAerobica),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Espalda",
                "Natación de espalda. Menor carga cervical. Trabaja dorsales y hombros.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catTecNatacion, catResAerobica),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Braza",
                "Estilo braza. Alta técnica. Trabaja pecho, piernas y coordinación.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catTecNatacion),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Mariposa",
                "Estilo mariposa. Muy exigente. Trabaja hombros, espalda y core con movimiento ondulatorio.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catTecNatacion),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Series de Natación",
                "Bloques de distancia a ritmo objetivo con descanso controlado.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catSeriesNatacion, catHIIT),
                params(paramDistancia, paramDuracion)
            ),
            exercise(
                "Patada de Crol con Tabla",
                "Trabajo de piernas con tabla de natación. Aísla la patada y mejora la propulsión.",
                ExerciseType.DISTANCE,
                sports(natacionSport),
                cats(catTecNatacion),
                params(paramDistancia, paramDuracion)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 8 — CICLISMO
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildCiclismo() {
        return List.of(

            exercise(
                "Rodaje en Zona 2",
                "Ciclismo a baja intensidad aeróbica. Base de la resistencia en ciclismo.",
                ExerciseType.TIMED,
                sports(ciclismoSport),
                cats(catCiclismoResistencia, catResAerobica),
                params(paramDuracion, paramDistancia, paramVelocidad)
            ),
            exercise(
                "Intervalos de Potencia (VO2max)",
                "Esfuerzos cortos a máxima potencia (3-8 min) con recuperación. Mejora VO2max.",
                ExerciseType.TIMED,
                sports(ciclismoSport),
                cats(catCiclismoIntervalos, catHIIT),
                params(paramDuracion, paramVelocidad)
            ),
            exercise(
                "Sprints en Bicicleta",
                "Aceleraciones máximas de 10-30 segundos. Trabajan la potencia anaeróbica.",
                ExerciseType.TIMED,
                sports(ciclismoSport),
                cats(catCiclismoIntervalos, catSprints),
                params(paramDuracion, paramVelocidad)
            ),
            exercise(
                "Spinning (Clase Indoor)",
                "Sesión de ciclismo indoor con variación de intensidad y ritmo.",
                ExerciseType.TIMED,
                sports(ciclismoSport),
                cats(catCiclismoResistencia, catCardio),
                params(paramDuracion)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOQUE 9 — ACONDICIONAMIENTO GENERAL (transversal)
    // ══════════════════════════════════════════════════════════════════════════

    private List<ExerciseEntity> buildGeneralConditioning() {
        return List.of(

            exercise(
                "Sentadilla Goblet",
                "Sentadilla frontal con kettlebell o mancuerna. Accesible para principiantes.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catSentadilla, catFuncional),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Swing con Kettlebell",
                "Bisagra explosiva de cadera con kettlebell. Potencia, cardio y cadena posterior.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catBisagra, catCardio, catFuncional),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Turkish Get-Up",
                "Movimiento complejo desde el suelo a pie con kettlebell. Fuerza, movilidad y coordinación.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catFuncional, catCore, catHombros),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Box Jump",
                "Salto al cajón pliométrico. Desarrolla explosividad y potencia del tren inferior.",
                ExerciseType.BODYWEIGHT,
                sports(gymSport),
                cats(catPliometria, catPiernas),
                params(paramReps)
            ),
            exercise(
                "Step-Up al Cajón",
                "Subida al cajón con una pierna. Trabajo unilateral de cuádriceps y glúteo.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catPiernas, catGluteos, catFuncional),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Hip Thrust",
                "Empuje de cadera con barra sobre banco. Aislamiento máximo del glúteo.",
                ExerciseType.WEIGHTED,
                sports(gymSport),
                cats(catGluteos, catBisagra),
                params(paramReps, paramPeso)
            ),
            exercise(
                "Mountain Climbers",
                "Escalador de montaña. Trabaja core, coordinación y resistencia cardiovascular.",
                ExerciseType.TIMED,
                sports(gymSport, boxeoSport),
                cats(catCore, catCardio, catFuncional),
                params(paramDuracion, paramReps)
            ),
            exercise(
                "Battle Ropes",
                "Ondas con cuerdas de batalla. Alta intensidad cardiovascular y fuerza de brazos.",
                ExerciseType.TIMED,
                sports(gymSport, boxeoSport),
                cats(catCardio, catHIIT, catFuncional),
                params(paramDuracion)
            ),
            exercise(
                "Movilidad de Cadera",
                "Rutina de movilidad articular para la cadera. Prioridad en el calentamiento.",
                ExerciseType.TIMED,
                sports(gymSport),
                cats(catMovilidad, catCalentamiento),
                params(paramDuracion)
            ),
            exercise(
                "Estiramiento de Isquiotibiales",
                "Estiramiento estático de isquiotibiales. Fundamental en la vuelta a la calma.",
                ExerciseType.TIMED,
                sports(gymSport, runningSport),
                cats(catFlexibilidad, catRecuperacion),
                params(paramDuracion)
            )
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private int saveAll(List<ExerciseEntity> exercises) {
        exerciseRepository.saveAll(exercises);
        return exercises.size();
    }

    private ExerciseEntity exercise(
            String name,
            String description,
            ExerciseType type,
            Set<SportEntity> sports,
            Set<ExerciseCategoryEntity> categories,
            Set<CustomParameterEntity> parameters) {

        return ExerciseEntity.builder()
                .name(name)
                .description(description)
                .exerciseType(type)
                .sports(sports)
                .categories(categories)
                .supportedParameters(parameters)
                .isPublic(true)
                .isActive(true)
                .createdBy(null)
                .usageCount(0)
                .rating(0.0)
                .ratingCount(0)
                .build();
    }

    private Set<SportEntity> sports(SportEntity... list) {
        return new HashSet<>(java.util.Arrays.asList(list));
    }

    private Set<ExerciseCategoryEntity> cats(ExerciseCategoryEntity... list) {
        return new HashSet<>(java.util.Arrays.asList(list));
    }

    private Set<CustomParameterEntity> params(CustomParameterEntity... list) {
        return new HashSet<>(java.util.Arrays.asList(list));
    }

    // ── Lookups con fallo rápido si el seed anterior no se ejecutó ────────────

    private SportEntity sport(String name) {
        return sportRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Sport no encontrado: '" + name + "'. Verifica que SportDataLoader se ejecutó correctamente."));
    }

    private ExerciseCategoryEntity cat(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Categoría no encontrada: '" + name + "'. Verifica que ExerciseCategoryDataLoader se ejecutó correctamente."));
    }

    private CustomParameterEntity param(String name) {
        return parameterRepository.findByNameAndIsGlobalTrue(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Parámetro global no encontrado: '" + name + "'. Verifica que ParameterDataLoader se ejecutó correctamente."));
    }
}