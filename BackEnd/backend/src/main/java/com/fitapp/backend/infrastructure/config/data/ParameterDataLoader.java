package com.fitapp.backend.infrastructure.config.data;

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
            List<SportEntity> predefinedSports = sportRepository.findByIsPredefinedTrue();

            for (SportEntity sport : predefinedSports) {
                createParametersForSport(sport);
            }

            createGlobalParameters();

            log.info("CREATED_DEFAULT_PARAMETERS | Total parameters created: {}", 
                    customParameterRepository.count());
        } catch (Exception e) {
            log.error("ERROR_LOADING_PARAMETERS | Error: {}", e.getMessage(), e);
        }
    }

    private void createParametersForSport(SportEntity sport) {
        String sportName = sport.getName();
        log.debug("CREATING_PARAMETERS_FOR_SPORT | sport={}", sportName);
        

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
            default:
                log.warn("NO_PARAMETERS_DEFINED_FOR_SPORT | sport={}", sportName);
                break;
        }
    }

    private void createGymParameters(SportEntity sport) {
        CustomParameterEntity weight = CustomParameterEntity.builder()
                .name("weight")
                .description("Peso utilizado en el ejercicio")
                .parameterType(ParameterType.NUMBER)
                .unit("kg")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity repetitions = CustomParameterEntity.builder()
                .name("repetitions")
                .description("Número de repeticiones por serie")
                .parameterType(ParameterType.INTEGER)
                .unit("rep")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity sets = CustomParameterEntity.builder()
                .name("sets")
                .description("Número de series del ejercicio")
                .parameterType(ParameterType.INTEGER)
                .unit("series")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rest = CustomParameterEntity.builder()
                .name("rest")
                .description("Tiempo de descanso entre series")
                .parameterType(ParameterType.DURATION)
                .unit("seg")
                .isGlobal(true)
                .isActive(true)
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
                .description("Workout of the Day")
                .parameterType(ParameterType.TEXT)
                .unit("descripción")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rounds = CustomParameterEntity.builder()
                .name("rounds")
                .description("Número de rondas completadas")
                .parameterType(ParameterType.INTEGER)
                .unit("rounds")
                .isGlobal(true)
                .isActive(true)
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
                .description("Distancia recorrida")
                .parameterType(ParameterType.DISTANCE)
                .unit("km")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity time = CustomParameterEntity.builder()
                .name("time")
                .description("Tiempo total de entrenamiento")
                .parameterType(ParameterType.DURATION)
                .unit("min")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
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
                .description("Distancia recorrida")
                .parameterType(ParameterType.DISTANCE)
                .unit("km")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity speed = CustomParameterEntity.builder()
                .name("speed")
                .description("Velocidad promedio")
                .parameterType(ParameterType.NUMBER)
                .unit("km/h")
                .isGlobal(true)
                .isActive(true)
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
                .description("Distancia nadada")
                .parameterType(ParameterType.DISTANCE)
                .unit("m")
                .isGlobal(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity swimStyle = CustomParameterEntity.builder()
                .name("swimStyle")
                .description("Estilo de nado utilizado")
                .parameterType(ParameterType.TEXT)
                .unit("estilo")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
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
                .description("Número de rounds")
                .parameterType(ParameterType.INTEGER)
                .unit("rounds")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity roundTime = CustomParameterEntity.builder()
                .name("roundTime")
                .description("Duración de cada round")
                .parameterType(ParameterType.DURATION)
                .unit("min")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
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
                .description("Postura de yoga realizada")
                .parameterType(ParameterType.TEXT)
                .unit("postura")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity duration = CustomParameterEntity.builder()
                .name("duration")
                .description("Duración de la postura")
                .parameterType(ParameterType.DURATION)
                .unit("seg")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
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
                .description("Goles marcados")
                .parameterType(ParameterType.INTEGER)
                .unit("goles")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity assists = CustomParameterEntity.builder()
                .name("assists")
                .description("Asistencias realizadas")
                .parameterType(ParameterType.INTEGER)
                .unit("asistencias")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
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
                .description("Puntos anotados")
                .parameterType(ParameterType.INTEGER)
                .unit("puntos")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        CustomParameterEntity rebounds = CustomParameterEntity.builder()
                .name("rebounds")
                .description("Rebotes capturados")
                .parameterType(ParameterType.INTEGER)
                .unit("rebotes")
                .isGlobal(true)
                .isActive(true)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .usageCount(0)
                .build();

        saveParameters(List.of(points, rebounds));
        log.debug("CREATED_BASKETBALL_PARAMETERS | sport={} | count=2", sport.getName());
    }


    private void createGlobalParameters() {
        // Verificar si ya existen parámetros globales sin deporte
        List<CustomParameterEntity> existingGlobalParams = 
            customParameterRepository.findByFilters(null, null, true, true, null, null, null)
                .getContent();
        
        if (!existingGlobalParams.isEmpty()) {
            log.debug("SKIPPING_GLOBAL_PARAMETERS | already exist: {}", existingGlobalParams.size());
            return;
        }

        // Parámetros globales (no asociados a un deporte específico)
        List<CustomParameterEntity> globalParameters = List.of(
            CustomParameterEntity.builder()
                    .name("heartRate")
                    .description("Frecuencia cardíaca durante el ejercicio")
                    .parameterType(ParameterType.INTEGER)
                    .unit("bpm")
                    .isGlobal(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("calories")
                    .description("Calorías quemadas")
                    .parameterType(ParameterType.NUMBER)
                    .unit("kcal")
                    .isGlobal(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("intensity")
                    .description("Nivel de intensidad percibida")
                    .parameterType(ParameterType.PERCENTAGE)
                    .unit("%")
                    .isGlobal(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .usageCount(0)
                    .build(),
            CustomParameterEntity.builder()
                    .name("notes")
                    .description("Observaciones adicionales")
                    .parameterType(ParameterType.TEXT)
                    .unit("texto")
                    .isGlobal(true)
                    .isActive(true)
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