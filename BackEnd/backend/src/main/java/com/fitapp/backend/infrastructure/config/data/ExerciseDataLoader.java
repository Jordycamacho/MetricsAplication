package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.*;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ExerciseType;
import com.fitapp.backend.infrastructure.persistence.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseDataLoader {
    
    private final ExerciseRepository exerciseRepository;
    private final SportRepository sportRepository;
    private final ExerciseCategoryRepository categoryRepository;
    private final CustomParameterRepository parameterRepository;
    
    @PostConstruct
    @Transactional
    public void loadPredefinedExercises() {
        log.info("=== EXERCISE DATA LOADER STARTING ===");
        
        try {
            // Verificar si ya existen ejercicios
            long existingCount = exerciseRepository.count();
            if (existingCount > 0) {
                log.info("Exercises already exist in database: {}", existingCount);
                return;
            }
            
            log.info("Creating predefined exercises...");
            
            // Obtener datos necesarios
            List<SportEntity> sports = sportRepository.findAll();
            List<ExerciseCategoryEntity> categories = categoryRepository.findAll();
            List<CustomParameterEntity> parameters = parameterRepository.findAll();
            
            if (sports.isEmpty() || categories.isEmpty()) {
                log.error("Cannot create exercises: missing sports or categories");
                return;
            }
            
            List<ExerciseEntity> allExercises = new ArrayList<>();
            
            // Ejercicios para Gimnasio
            SportEntity gymSport = sports.stream().filter(s -> s.getName().equals("Gimnasio")).findFirst().orElse(null);
            if (gymSport != null) {
                allExercises.addAll(createGymExercises(gymSport, categories, parameters));
            }
            
            // Ejercicios para CrossFit
            SportEntity crossfitSport = sports.stream().filter(s -> s.getName().equals("CrossFit")).findFirst().orElse(null);
            if (crossfitSport != null) {
                allExercises.addAll(createCrossFitExercises(crossfitSport, categories, parameters));
            }
            
            // Ejercicios para Running
            SportEntity runningSport = sports.stream().filter(s -> s.getName().equals("Running")).findFirst().orElse(null);
            if (runningSport != null) {
                allExercises.addAll(createRunningExercises(runningSport, categories, parameters));
            }
            
            // Ejercicios para Ciclismo
            SportEntity cyclingSport = sports.stream().filter(s -> s.getName().equals("Ciclismo")).findFirst().orElse(null);
            if (cyclingSport != null) {
                allExercises.addAll(createCyclingExercises(cyclingSport, categories, parameters));
            }
            
            // Ejercicios para Natación
            SportEntity swimmingSport = sports.stream().filter(s -> s.getName().equals("Natación")).findFirst().orElse(null);
            if (swimmingSport != null) {
                allExercises.addAll(createSwimmingExercises(swimmingSport, categories, parameters));
            }
            
            // Guardar todos los ejercicios
            if (!allExercises.isEmpty()) {
                exerciseRepository.saveAll(allExercises);
                log.info("Created {} predefined exercises", allExercises.size());
            }
            
        } catch (Exception e) {
            log.error("Error creating exercises: {}", e.getMessage(), e);
        }
        
        log.info("=== EXERCISE DATA LOADER FINISHED ===");
    }
    
    private List<ExerciseEntity> createGymExercises(SportEntity sport, 
                                                   List<ExerciseCategoryEntity> categories, 
                                                   List<CustomParameterEntity> parameters) {
        List<ExerciseEntity> exercises = new ArrayList<>();
        ExerciseCategoryEntity fuerza = findCategory(categories, "Fuerza");
        
        // Parámetros comunes para gym
        Set<CustomParameterEntity> gymParams = findParameters(parameters, 
            Arrays.asList("weight", "repetitions", "sets", "rest"));
        
        exercises.add(createSimpleExercise("Sentadilla", 
            "Ejercicio fundamental para piernas. Mantén la espalda recta.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Press de Banca", 
            "Ejercicio para pectorales, hombros y tríceps.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Peso Muerto", 
            "Ejercicio completo para espalda, glúteos y piernas.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Press Militar", 
            "Ejercicio para hombros con barra o mancuernas.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Curl de Bíceps", 
            "Ejercicio de aislamiento para bíceps.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Extensión de Tríceps", 
            "Ejercicio para tríceps con cuerda o barra.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Dominadas", 
            "Ejercicio de peso corporal para espalda y brazos.", 
            sport, fuerza, gymParams, ExerciseType.BODYWEIGHT));
        
        exercises.add(createSimpleExercise("Fondos en Paralelas", 
            "Ejercicio para tríceps y pectorales.", 
            sport, fuerza, gymParams, ExerciseType.BODYWEIGHT));
        
        exercises.add(createSimpleExercise("Remo con Barra", 
            "Ejercicio para espalda y bíceps.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Prensa de Piernas", 
            "Ejercicio para piernas en máquina.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Elevaciones Laterales", 
            "Ejercicio para hombros con mancuernas.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Encogimientos de Hombros", 
            "Ejercicio para trapecios con barra o mancuernas.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Curl de Antebrazo", 
            "Ejercicio para antebrazos con barra.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Pull Over", 
            "Ejercicio para pectorales y dorsales.", 
            sport, fuerza, gymParams, ExerciseType.WEIGHTED));
        
        log.info("Created {} gym exercises", exercises.size());
        return exercises;
    }
    
    private List<ExerciseEntity> createCrossFitExercises(SportEntity sport, 
                                                        List<ExerciseCategoryEntity> categories, 
                                                        List<CustomParameterEntity> parameters) {
        List<ExerciseEntity> exercises = new ArrayList<>();
        ExerciseCategoryEntity fuerza = findCategory(categories, "Fuerza");
        ExerciseCategoryEntity cardio = findCategory(categories, "Cardio");
        
        Set<CustomParameterEntity> crossfitParams = findParameters(parameters, 
            Arrays.asList("wod", "rounds", "weight", "repetitions"));
        
        exercises.add(createSimpleExercise("Thruster", 
            "Combinación de sentadilla frontal y press de hombros.", 
            sport, Set.of(fuerza, cardio), crossfitParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Burpees", 
            "Ejercicio completo que combina flexión, sentadilla y salto.", 
            sport, Set.of(fuerza, cardio), crossfitParams, ExerciseType.BODYWEIGHT));
        
        exercises.add(createSimpleExercise("Clean and Jerk", 
            "Movimiento olímpico completo.", 
            sport, fuerza, crossfitParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Snatch", 
            "Movimiento olímpico de arranque.", 
            sport, fuerza, crossfitParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Wall Ball", 
            "Lanzamiento de balón medicinal contra la pared.", 
            sport, Set.of(fuerza, cardio), crossfitParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Box Jump", 
            "Salto sobre caja para potencia y explosividad.", 
            sport, Set.of(fuerza, cardio), crossfitParams, ExerciseType.BODYWEIGHT));
        
        exercises.add(createSimpleExercise("Kettlebell Swing", 
            "Balanceo de kettlebell para cadena posterior.", 
            sport, Set.of(fuerza, cardio), crossfitParams, ExerciseType.WEIGHTED));
        
        exercises.add(createSimpleExercise("Double Under", 
            "Salto de cuerda doble.", 
            sport, cardio, crossfitParams, ExerciseType.TIMED));
        
        log.info("Created {} CrossFit exercises", exercises.size());
        return exercises;
    }
    
    private List<ExerciseEntity> createRunningExercises(SportEntity sport, 
                                                       List<ExerciseCategoryEntity> categories, 
                                                       List<CustomParameterEntity> parameters) {
        List<ExerciseEntity> exercises = new ArrayList<>();
        ExerciseCategoryEntity cardio = findCategory(categories, "Cardio");
        
        Set<CustomParameterEntity> runningParams = findParameters(parameters, 
            Arrays.asList("distance", "time"));
        
        exercises.add(createSimpleExercise("Carrera Continua", 
            "Correr a ritmo constante por tiempo o distancia.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Intervalos", 
            "Alternar períodos de alta y baja intensidad.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Fartlek", 
            "Entrenamiento de velocidad variable.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Cuestas", 
            "Correr en pendiente para fuerza.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Tempo Run", 
            "Carrera a ritmo de competición.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Sprints", 
            "Carreras cortas a máxima velocidad.", 
            sport, cardio, runningParams, ExerciseType.TIMED));
        
        log.info("Created {} running exercises", exercises.size());
        return exercises;
    }
    
    private List<ExerciseEntity> createCyclingExercises(SportEntity sport, 
                                                       List<ExerciseCategoryEntity> categories, 
                                                       List<CustomParameterEntity> parameters) {
        List<ExerciseEntity> exercises = new ArrayList<>();
        ExerciseCategoryEntity cardio = findCategory(categories, "Cardio");
        
        Set<CustomParameterEntity> cyclingParams = findParameters(parameters, 
            Arrays.asList("distance", "speed"));
        
        exercises.add(createSimpleExercise("Paseo en Bicicleta", 
            "Ciclismo recreativo a ritmo moderado.", 
            sport, cardio, cyclingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Entrenamiento por Intervalos", 
            "Alternar períodos de pedaleo intenso y suave.", 
            sport, cardio, cyclingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Subida de Montaña", 
            "Ciclismo en pendiente para fuerza.", 
            sport, cardio, cyclingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Spinning", 
            "Clase de ciclismo indoor.", 
            sport, cardio, cyclingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Sprints en Bicicleta", 
            "Pedaleo a máxima velocidad por corto tiempo.", 
            sport, cardio, cyclingParams, ExerciseType.TIMED));
        
        log.info("Created {} cycling exercises", exercises.size());
        return exercises;
    }
    
    private List<ExerciseEntity> createSwimmingExercises(SportEntity sport, 
                                                        List<ExerciseCategoryEntity> categories, 
                                                        List<CustomParameterEntity> parameters) {
        List<ExerciseEntity> exercises = new ArrayList<>();
        ExerciseCategoryEntity cardio = findCategory(categories, "Cardio");
        
        Set<CustomParameterEntity> swimmingParams = findParameters(parameters, 
            Arrays.asList("distance", "swimStyle"));
        
        exercises.add(createSimpleExercise("Crol", 
            "Estilo de nado más rápido y eficiente.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Espalda", 
            "Nado de espalda, bueno para la postura.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Braza", 
            "Estilo de nado simétrico y técnico.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Mariposa", 
            "Estilo de nado exigente y completo.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Series de Velocidad", 
            "Nado a máxima velocidad por distancias cortas.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        exercises.add(createSimpleExercise("Nado Continuo", 
            "Nado a ritmo constante por tiempo.", 
            sport, cardio, swimmingParams, ExerciseType.TIMED));
        
        log.info("Created {} swimming exercises", exercises.size());
        return exercises;
    }
    
    // Métodos helper
    private ExerciseCategoryEntity findCategory(List<ExerciseCategoryEntity> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(categories.get(0));
    }
    
    private Set<CustomParameterEntity> findParameters(List<CustomParameterEntity> allParameters, List<String> names) {
        Set<CustomParameterEntity> result = new HashSet<>();
        for (String name : names) {
            allParameters.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }
    
    private ExerciseEntity createSimpleExercise(String name, String description, SportEntity sport,
                                               ExerciseCategoryEntity category,
                                               Set<CustomParameterEntity> parameters,
                                               ExerciseType exerciseType) {
        return createSimpleExercise(name, description, sport, Set.of(category), parameters, exerciseType);
    }
    
    private ExerciseEntity createSimpleExercise(String name, String description, SportEntity sport,
                                               Set<ExerciseCategoryEntity> categories,
                                               Set<CustomParameterEntity> parameters,
                                               ExerciseType exerciseType) {
        ExerciseEntity exercise = new ExerciseEntity();
        exercise.setName(name);
        exercise.setDescription(description);
        exercise.setSport(sport);
        exercise.setCategories(new HashSet<>(categories));
        exercise.setSupportedParameters(new HashSet<>(parameters));
        exercise.setExerciseType(exerciseType);
        exercise.setIsActive(true);
        exercise.setIsPublic(true);
        exercise.setUsageCount(0);
        exercise.setRating(0.0);
        exercise.setRatingCount(0);
        exercise.setCreatedAt(LocalDateTime.now());
        exercise.setUpdatedAt(LocalDateTime.now());
        exercise.setLastUsedAt(null);
        exercise.setCreatedBy(null);
        
        return exercise;
    }
}