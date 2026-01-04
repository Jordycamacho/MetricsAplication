// com.fitapp.backend.infrastructure.config/SportDataLoader.java
package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SportDataLoader {
    private final SportRepository sportRepository;

    @PostConstruct
    @Transactional
    public void loadPredefinedSports() {
        if (sportRepository.count() == 0) {
            log.info("LOADING_PREDEFINED_SPORTS | Starting to load predefined sports");
            createPredefinedSports();
            log.info("LOADING_PREDEFINED_SPORTS_SUCCESS | Predefined sports loaded successfully");
        } else {
            log.debug("LOADING_PREDEFINED_SPORTS | Sports already exist in database");
        }
    }

    private void createPredefinedSports() {
        try {
            // Deportes de fuerza
            createSport("Gimnasio", "strength", createGymParameters(), SportSourceType.OFFICIAL);
            createSport("CrossFit", "strength", createCrossfitParameters(), SportSourceType.OFFICIAL);
            createSport("Calistenia", "strength", createCalisthenicsParameters(), SportSourceType.OFFICIAL);
            createSport("Powerlifting", "strength", createPowerliftingParameters(), SportSourceType.OFFICIAL);

            // Deportes de cardio
            createSport("Running", "cardio", createRunningParameters(), SportSourceType.OFFICIAL);
            createSport("Ciclismo", "cardio", createCyclingParameters(), SportSourceType.OFFICIAL);
            createSport("Natación", "cardio", createSwimmingParameters(), SportSourceType.OFFICIAL);
            createSport("Salto de Cuerda", "cardio", createJumpRopeParameters(), SportSourceType.OFFICIAL);

            // Deportes de combate
            createSport("Boxeo", "combat", createBoxingParameters(), SportSourceType.OFFICIAL);
            createSport("MMA", "combat", createMMAParameters(), SportSourceType.OFFICIAL);
            createSport("Judo", "combat", createJudoParameters(), SportSourceType.OFFICIAL);
            createSport("Karate", "combat", createKarateParameters(), SportSourceType.OFFICIAL);

            // Deportes de flexibilidad
            createSport("Yoga", "flexibility", createYogaParameters(), SportSourceType.OFFICIAL);
            createSport("Pilates", "flexibility", createPilatesParameters(), SportSourceType.OFFICIAL);
            createSport("Estiramientos", "flexibility", createStretchingParameters(), SportSourceType.OFFICIAL);

            // Deportes de equipo
            createSport("Fútbol", "team", createFootballParameters(), SportSourceType.OFFICIAL);
            createSport("Baloncesto", "team", createBasketballParameters(), SportSourceType.OFFICIAL);
            createSport("Voleibol", "team", createVolleyballParameters(), SportSourceType.OFFICIAL);
            createSport("Tenis", "team", createTennisParameters(), SportSourceType.OFFICIAL);

            log.info("CREATED_PREDEFINED_SPORTS | Total sports created: {}", sportRepository.count());
        } catch (Exception e) {
            log.error("ERROR_LOADING_SPORTS | Error: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading predefined sports", e);
        }
    }

    private void createSport(String name, String category, Map<String, String> parameters, 
                             SportSourceType sourceType) {
        SportEntity sport = new SportEntity();
        sport.setName(name);
        sport.setCategory(category);
        sport.setIsPredefined(true);
        sport.setSourceType(sourceType);
        sport.setParameterTemplate(parameters);

        sportRepository.save(sport);
        log.debug("CREATED_SPORT | name={} | category={} | params={}", 
                 name, category, parameters.size());
    }

    private Map<String, String> createGymParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("peso", "kg");
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("descanso", "seg");
        params.put("intensidad", "%");
        return params;
    }

    private Map<String, String> createCrossfitParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("peso", "kg");
        params.put("repeticiones", "rep");
        params.put("rounds", "rounds");
        params.put("tiempo", "min");
        params.put("descanso", "seg");
        params.put("wod", "text");
        return params;
    }

    private Map<String, String> createPowerliftingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("pesoSentadilla", "kg");
        params.put("pesoPressBanca", "kg");
        params.put("pesoPesoMuerto", "kg");
        params.put("repeticiones", "rep");
        return params;
    }

    private Map<String, String> createJumpRopeParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("tiempo", "min");
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("descanso", "seg");
        return params;
    }

    private Map<String, String> createKarateParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("katas", "number");
        params.put("tiempo", "min");
        params.put("intensidad", "%");
        return params;
    }

    private Map<String, String> createStretchingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("duracion", "seg");
        params.put("repeticiones", "rep");
        params.put("series", "series");
        return params;
    }

    private Map<String, String> createFootballParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("tiempo", "min");
        params.put("goles", "number");
        params.put("asistencias", "number");
        params.put("distancia", "km");
        return params;
    }

    private Map<String, String> createBasketballParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("tiempo", "min");
        params.put("puntos", "number");
        params.put("rebotes", "number");
        params.put("asistencias", "number");
        return params;
    }

    private Map<String, String> createVolleyballParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("sets", "number");
        params.put("puntos", "number");
        params.put("saques", "number");
        params.put("bloqueos", "number");
        return params;
    }

    private Map<String, String> createTennisParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("sets", "number");
        params.put("games", "number");
        params.put("aces", "number");
        params.put("winners", "number");
        return params;
    }

    // Mantener los métodos anteriores...
    private Map<String, String> createCalisthenicsParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("nivel", "text");
        params.put("descanso", "seg");
        return params;
    }

    private Map<String, String> createRunningParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("distancia", "km");
        params.put("tiempo", "min");
        params.put("ritmo", "min/km");
        params.put("elevacion", "m");
        return params;
    }

    private Map<String, String> createCyclingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("distancia", "km");
        params.put("tiempo", "min");
        params.put("velocidad", "km/h");
        params.put("elevacion", "m");
        return params;
    }

    private Map<String, String> createSwimmingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("distancia", "m");
        params.put("tiempo", "min");
        params.put("estilo", "text");
        params.put("series", "series");
        return params;
    }

    private Map<String, String> createBoxingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("rounds", "rounds");
        params.put("tiempoRound", "min");
        params.put("descanso", "seg");
        params.put("intensidad", "%");
        return params;
    }

    private Map<String, String> createMMAParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("rounds", "rounds");
        params.put("tiempoRound", "min");
        params.put("descanso", "seg");
        params.put("tecnica", "text");
        return params;
    }

    private Map<String, String> createJudoParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("rounds", "rounds");
        params.put("tiempoRound", "min");
        params.put("descanso", "seg");
        params.put("tecnica", "text");
        return params;
    }

    private Map<String, String> createYogaParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("postura", "text");
        params.put("duracion", "seg");
        params.put("repeticiones", "rep");
        params.put("nivel", "text");
        return params;
    }

    private Map<String, String> createPilatesParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("duracion", "seg");
        params.put("nivel", "text");
        return params;
    }
}