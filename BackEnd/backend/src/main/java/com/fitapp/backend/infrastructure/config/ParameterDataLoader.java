package com.fitapp.backend.infrastructure.config;

import com.fitapp.backend.infrastructure.persistence.entity.CustomParameterEntity;
import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.ParameterType;
import com.fitapp.backend.infrastructure.persistence.repository.CustomParameterRepository;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParameterDataLoader {
    private final CustomParameterRepository customParameterRepository;
    private final SportRepository sportRepository;

    @PostConstruct
    @Transactional
    public void loadDefaultParameters() {
        // Verificar si ya existen parámetros globales usando un método que sí existe
        List<CustomParameterEntity> existingGlobalParams = 
            customParameterRepository.findByIsGlobalTrue(Pageable.unpaged()).getContent();
        
        if (existingGlobalParams.isEmpty()) {
            log.info("LOADING_DEFAULT_PARAMETERS | Starting to load default parameters");
            createDefaultParameters();
            log.info("LOADING_DEFAULT_PARAMETERS_SUCCESS | Default parameters loaded successfully");
        } else {
            log.debug("LOADING_DEFAULT_PARAMETERS | Default parameters already exist in database: {}", 
                     existingGlobalParams.size());
        }
    }

    private void createDefaultParameters() {
        try {
            // Obtener todos los deportes predefinidos
            List<SportEntity> predefinedSports = sportRepository.findByIsPredefinedTrue();

            // Para cada deporte, crear parámetros por defecto
            for (SportEntity sport : predefinedSports) {
                createParametersForSport(sport);
            }

            // Crear parámetros globales (no asociados a un deporte)
            createGlobalParameters();

            log.info("CREATED_DEFAULT_PARAMETERS | Total parameters created: {}", 
                    customParameterRepository.count());
        } catch (Exception e) {
            log.error("ERROR_LOADING_PARAMETERS | Error: {}", e.getMessage(), e);
            // No lanzar excepción para no bloquear la aplicación
        }
    }

    private void createParametersForSport(SportEntity sport) {
        String sportName = sport.getName();
        log.debug("CREATING_PARAMETERS_FOR_SPORT | sport={}", sportName);

        // Verificar si ya existen parámetros para este deporte
        List<CustomParameterEntity> existingParams = 
            customParameterRepository.findBySportId(sport.getId(), Pageable.unpaged()).getContent();
        
        if (!existingParams.isEmpty()) {
            log.debug("SKIPPING_SPORT_PARAMETERS | sport={} | params already exist: {}", 
                     sportName, existingParams.size());
            return;
        }

        switch (sportName) {
            case "Gimnasio":
                createGymParameters(sport);
                break;
            case "CrossFit":
                createCrossFitParameters(sport);
                break;
            case "Running":
                createRunningParameters(sport);
                break;
            case "Ciclismo":
                createCyclingParameters(sport);
                break;
            case "Natación":
                createSwimmingParameters(sport);
                break;
            case "Boxeo":
                createBoxingParameters(sport);
                break;
            case "Yoga":
                createYogaParameters(sport);
                break;
            case "Fútbol":
                createFootballParameters(sport);
                break;
            case "Baloncesto":
                createBasketballParameters(sport);
                break;
            case "Powerlifting":
                createPowerliftingParameters(sport);
                break;
            case "Calistenia":
                createCalisthenicsParameters(sport);
                break;
            case "Salto de Cuerda":
                createJumpRopeParameters(sport);
                break;
            case "MMA":
                createMMAParameters(sport);
                break;
            case "Judo":
                createJudoParameters(sport);
                break;
            case "Karate":
                createKarateParameters(sport);
                break;
            case "Pilates":
                createPilatesParameters(sport);
                break;
            case "Estiramientos":
                createStretchingParameters(sport);
                break;
            case "Voleibol":
                createVolleyballParameters(sport);
                break;
            case "Tenis":
                createTennisParameters(sport);
                break;
            default:
                log.warn("NO_PARAMETERS_DEFINED_FOR_SPORT | sport={}", sportName);
                break;
        }
    }

    private void createGymParameters(SportEntity sport) {
        CustomParameterEntity weight = CustomParameterEntity.builder()
                .name("weight")
                .displayName("Peso")
                .description("Peso utilizado en el ejercicio")
                .parameterType(ParameterType.NUMBER)
                .unit("kg")
                .validationRules(Map.of("min", "0", "max", "500", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity repetitions = CustomParameterEntity.builder()
                .name("repetitions")
                .displayName("Repeticiones")
                .description("Número de repeticiones por serie")
                .parameterType(ParameterType.INTEGER)
                .unit("rep")
                .validationRules(Map.of("min", "1", "max", "100", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity sets = CustomParameterEntity.builder()
                .name("sets")
                .displayName("Series")
                .description("Número de series del ejercicio")
                .parameterType(ParameterType.INTEGER)
                .unit("series")
                .validationRules(Map.of("min", "1", "max", "20", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rest = CustomParameterEntity.builder()
                .name("rest")
                .displayName("Descanso")
                .description("Tiempo de descanso entre series")
                .parameterType(ParameterType.DURATION)
                .unit("seg")
                .validationRules(Map.of("min", "0", "max", "600", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(weight, repetitions, sets, rest));
        log.debug("CREATED_GYM_PARAMETERS | sport={} | count=4", sport.getName());
    }

    private void createCrossFitParameters(SportEntity sport) {
        CustomParameterEntity wod = CustomParameterEntity.builder()
                .name("wod")
                .displayName("WOD")
                .description("Workout of the Day")
                .parameterType(ParameterType.TEXT)
                .unit("descripción") // Cadena descriptiva en lugar de null
                .validationRules(Map.of("maxLength", "500", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rounds = CustomParameterEntity.builder()
                .name("rounds")
                .displayName("Rounds")
                .description("Número de rondas completadas")
                .parameterType(ParameterType.INTEGER)
                .unit("rounds")
                .validationRules(Map.of("min", "1", "max", "50", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(wod, rounds));
        log.debug("CREATED_CROSSFIT_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createRunningParameters(SportEntity sport) {
        CustomParameterEntity distance = CustomParameterEntity.builder()
                .name("distance")
                .displayName("Distancia")
                .description("Distancia recorrida")
                .parameterType(ParameterType.DISTANCE)
                .unit("km")
                .validationRules(Map.of("min", "0", "max", "100", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity time = CustomParameterEntity.builder()
                .name("time")
                .displayName("Tiempo")
                .description("Tiempo total de entrenamiento")
                .parameterType(ParameterType.DURATION)
                .unit("min")
                .validationRules(Map.of("min", "1", "max", "300", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(distance, time));
        log.debug("CREATED_RUNNING_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createCyclingParameters(SportEntity sport) {
        CustomParameterEntity distance = CustomParameterEntity.builder()
                .name("distance")
                .displayName("Distancia")
                .description("Distancia recorrida")
                .parameterType(ParameterType.DISTANCE)
                .unit("km")
                .validationRules(Map.of("min", "0", "max", "200", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity speed = CustomParameterEntity.builder()
                .name("speed")
                .displayName("Velocidad")
                .description("Velocidad promedio")
                .parameterType(ParameterType.NUMBER)
                .unit("km/h")
                .validationRules(Map.of("min", "0", "max", "60", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(distance, speed));
        log.debug("CREATED_CYCLING_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createSwimmingParameters(SportEntity sport) {
        CustomParameterEntity distance = CustomParameterEntity.builder()
                .name("distance")
                .displayName("Distancia")
                .description("Distancia nadada")
                .parameterType(ParameterType.DISTANCE)
                .unit("m")
                .validationRules(Map.of("min", "0", "max", "5000", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity swimStyle = CustomParameterEntity.builder()
                .name("swimStyle")
                .displayName("Estilo")
                .description("Estilo de nado utilizado")
                .parameterType(ParameterType.TEXT)
                .unit("estilo")
                .validationRules(Map.of("allowedValues", "crol,espalda,braza,mariposa,combinado", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(distance, swimStyle));
        log.debug("CREATED_SWIMMING_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createBoxingParameters(SportEntity sport) {
        CustomParameterEntity rounds = CustomParameterEntity.builder()
                .name("rounds")
                .displayName("Rounds")
                .description("Número de rounds")
                .parameterType(ParameterType.INTEGER)
                .unit("rounds")
                .validationRules(Map.of("min", "1", "max", "12", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity roundTime = CustomParameterEntity.builder()
                .name("roundTime")
                .displayName("Tiempo por round")
                .description("Duración de cada round")
                .parameterType(ParameterType.DURATION)
                .unit("min")
                .validationRules(Map.of("min", "1", "max", "5", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(rounds, roundTime));
        log.debug("CREATED_BOXING_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createYogaParameters(SportEntity sport) {
        CustomParameterEntity pose = CustomParameterEntity.builder()
                .name("pose")
                .displayName("Postura")
                .description("Postura de yoga realizada")
                .parameterType(ParameterType.TEXT)
                .unit("postura")
                .validationRules(Map.of("maxLength", "100", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity duration = CustomParameterEntity.builder()
                .name("duration")
                .displayName("Duración")
                .description("Duración de la postura")
                .parameterType(ParameterType.DURATION)
                .unit("seg")
                .validationRules(Map.of("min", "10", "max", "300", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(pose, duration));
        log.debug("CREATED_YOGA_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createFootballParameters(SportEntity sport) {
        CustomParameterEntity goals = CustomParameterEntity.builder()
                .name("goals")
                .displayName("Goles")
                .description("Goles marcados")
                .parameterType(ParameterType.INTEGER)
                .unit("goles")
                .validationRules(Map.of("min", "0", "max", "20", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity assists = CustomParameterEntity.builder()
                .name("assists")
                .displayName("Asistencias")
                .description("Asistencias realizadas")
                .parameterType(ParameterType.INTEGER)
                .unit("asistencias")
                .validationRules(Map.of("min", "0", "max", "20", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(goals, assists));
        log.debug("CREATED_FOOTBALL_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createBasketballParameters(SportEntity sport) {
        CustomParameterEntity points = CustomParameterEntity.builder()
                .name("points")
                .displayName("Puntos")
                .description("Puntos anotados")
                .parameterType(ParameterType.INTEGER)
                .unit("puntos")
                .validationRules(Map.of("min", "0", "max", "100", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rebounds = CustomParameterEntity.builder()
                .name("rebounds")
                .displayName("Rebotes")
                .description("Rebotes capturados")
                .parameterType(ParameterType.INTEGER)
                .unit("rebotes")
                .validationRules(Map.of("min", "0", "max", "50", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(points, rebounds));
        log.debug("CREATED_BASKETBALL_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createPowerliftingParameters(SportEntity sport) {
        CustomParameterEntity squatWeight = CustomParameterEntity.builder()
                .name("squatWeight")
                .displayName("Peso sentadilla")
                .description("Peso máximo en sentadilla")
                .parameterType(ParameterType.NUMBER)
                .unit("kg")
                .validationRules(Map.of("min", "0", "max", "500", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity benchWeight = CustomParameterEntity.builder()
                .name("benchWeight")
                .displayName("Peso press banca")
                .description("Peso máximo en press banca")
                .parameterType(ParameterType.NUMBER)
                .unit("kg")
                .validationRules(Map.of("min", "0", "max", "300", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(squatWeight, benchWeight));
        log.debug("CREATED_POWERLIFTING_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createCalisthenicsParameters(SportEntity sport) {
        CustomParameterEntity repetitions = CustomParameterEntity.builder()
                .name("repetitions")
                .displayName("Repeticiones")
                .description("Número de repeticiones")
                .parameterType(ParameterType.INTEGER)
                .unit("rep")
                .validationRules(Map.of("min", "1", "max", "100", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity level = CustomParameterEntity.builder()
                .name("level")
                .displayName("Nivel")
                .description("Nivel de dificultad")
                .parameterType(ParameterType.TEXT)
                .unit("nivel")
                .validationRules(Map.of("allowedValues", "principiante,intermedio,avanzado", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(repetitions, level));
        log.debug("CREATED_CALISTHENICS_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createJumpRopeParameters(SportEntity sport) {
        CustomParameterEntity time = CustomParameterEntity.builder()
                .name("time")
                .displayName("Tiempo")
                .description("Tiempo de salto")
                .parameterType(ParameterType.DURATION)
                .unit("min")
                .validationRules(Map.of("min", "1", "max", "60", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(time));
        log.debug("CREATED_JUMP_ROPE_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createMMAParameters(SportEntity sport) {
        CustomParameterEntity rounds = CustomParameterEntity.builder()
                .name("rounds")
                .displayName("Rounds")
                .description("Número de rounds")
                .parameterType(ParameterType.INTEGER)
                .unit("rounds")
                .validationRules(Map.of("min", "1", "max", "5", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(rounds));
        log.debug("CREATED_MMA_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createJudoParameters(SportEntity sport) {
        CustomParameterEntity technique = CustomParameterEntity.builder()
                .name("technique")
                .displayName("Técnica")
                .description("Técnica utilizada")
                .parameterType(ParameterType.TEXT)
                .unit("técnica")
                .validationRules(Map.of("maxLength", "100", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(technique));
        log.debug("CREATED_JUDO_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createKarateParameters(SportEntity sport) {
        CustomParameterEntity katas = CustomParameterEntity.builder()
                .name("katas")
                .displayName("Katas")
                .description("Número de katas realizadas")
                .parameterType(ParameterType.INTEGER)
                .unit("katas")
                .validationRules(Map.of("min", "0", "max", "20", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(katas));
        log.debug("CREATED_KARATE_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createPilatesParameters(SportEntity sport) {
        CustomParameterEntity repetitions = CustomParameterEntity.builder()
                .name("repetitions")
                .displayName("Repeticiones")
                .description("Número de repeticiones")
                .parameterType(ParameterType.INTEGER)
                .unit("rep")
                .validationRules(Map.of("min", "1", "max", "50", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(repetitions));
        log.debug("CREATED_PILATES_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createStretchingParameters(SportEntity sport) {
        CustomParameterEntity duration = CustomParameterEntity.builder()
                .name("duration")
                .displayName("Duración")
                .description("Duración del estiramiento")
                .parameterType(ParameterType.DURATION)
                .unit("seg")
                .validationRules(Map.of("min", "10", "max", "300", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(duration));
        log.debug("CREATED_STRETCHING_PARAMETERS | sport={} | count=1", sport.getName());
    }

    private void createVolleyballParameters(SportEntity sport) {
        CustomParameterEntity sets = CustomParameterEntity.builder()
                .name("sets")
                .displayName("Sets")
                .description("Número de sets jugados")
                .parameterType(ParameterType.INTEGER)
                .unit("sets")
                .validationRules(Map.of("min", "1", "max", "5", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity points = CustomParameterEntity.builder()
                .name("points")
                .displayName("Puntos")
                .description("Puntos anotados")
                .parameterType(ParameterType.INTEGER)
                .unit("puntos")
                .validationRules(Map.of("min", "0", "max", "50", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(sets, points));
        log.debug("CREATED_VOLLEYBALL_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createTennisParameters(SportEntity sport) {
        CustomParameterEntity sets = CustomParameterEntity.builder()
                .name("sets")
                .displayName("Sets")
                .description("Número de sets jugados")
                .parameterType(ParameterType.INTEGER)
                .unit("sets")
                .validationRules(Map.of("min", "1", "max", "5", "required", "true"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity aces = CustomParameterEntity.builder()
                .name("aces")
                .displayName("Aces")
                .description("Aces realizados")
                .parameterType(ParameterType.INTEGER)
                .unit("aces")
                .validationRules(Map.of("min", "0", "max", "50", "required", "false"))
                .isGlobal(true)
                .isActive(true)
                .sport(sport)
                .category(sport.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(sets, aces));
        log.debug("CREATED_TENNIS_PARAMETERS | sport={} | count=2", sport.getName());
    }

    private void createGlobalParameters() {
        // Verificar si ya existen parámetros globales sin deporte
        List<CustomParameterEntity> existingGlobalParams = 
            customParameterRepository.findByFilters(null, null, true, true, null, null, null, Pageable.unpaged())
                .getContent();
        
        if (!existingGlobalParams.isEmpty()) {
            log.debug("SKIPPING_GLOBAL_PARAMETERS | already exist: {}", existingGlobalParams.size());
            return;
        }

        // Parámetros globales (no asociados a un deporte específico)
        List<CustomParameterEntity> globalParameters = List.of(
            CustomParameterEntity.builder()
                    .name("heartRate")
                    .displayName("Frecuencia cardíaca")
                    .description("Frecuencia cardíaca durante el ejercicio")
                    .parameterType(ParameterType.INTEGER)
                    .unit("bpm")
                    .validationRules(Map.of("min", "40", "max", "220", "required", "false"))
                    .isGlobal(true)
                    .isActive(true)
                    .sport(null)
                    .category("health")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("calories")
                    .displayName("Calorías")
                    .description("Calorías quemadas")
                    .parameterType(ParameterType.NUMBER)
                    .unit("kcal")
                    .validationRules(Map.of("min", "0", "max", "5000", "required", "false"))
                    .isGlobal(true)
                    .isActive(true)
                    .sport(null)
                    .category("health")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("intensity")
                    .displayName("Intensidad")
                    .description("Nivel de intensidad percibida")
                    .parameterType(ParameterType.PERCENTAGE)
                    .unit("%")
                    .validationRules(Map.of("min", "0", "max", "100", "required", "false"))
                    .isGlobal(true)
                    .isActive(true)
                    .sport(null)
                    .category("performance")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("notes")
                    .displayName("Notas")
                    .description("Observaciones adicionales")
                    .parameterType(ParameterType.TEXT)
                    .unit("texto")
                    .validationRules(Map.of("maxLength", "1000", "required", "false"))
                    .isGlobal(true)
                    .isActive(true)
                    .sport(null)
                    .category("general")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build()
        );

        saveParameters(globalParameters);
        log.debug("CREATED_GLOBAL_PARAMETERS | count={}", globalParameters.size());
    }

    private void saveParameters(List<CustomParameterEntity> parameters) {
        try {
            customParameterRepository.saveAll(parameters);
        } catch (Exception e) {
            log.error("ERROR_SAVING_PARAMETERS | Error: {}", e.getMessage(), e);
            // Continuar con otros parámetros
        }
    }
}