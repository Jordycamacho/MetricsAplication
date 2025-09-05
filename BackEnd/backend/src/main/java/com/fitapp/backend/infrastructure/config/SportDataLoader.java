package com.fitapp.backend.infrastructure.config;

import com.fitapp.backend.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.infrastructure.persistence.repository.SportRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SportDataLoader {
    private final SportRepository sportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void loadPredefinedSports() {
        if (sportRepository.count() == 0) {
            createPredefinedSports();
        }
    }

    private void createPredefinedSports() {
        try {
            // Deportes de fuerza
            createSport("Gimnasio", "strength", createGymParameters());
            createSport("CrossFit", "strength", createCrossfitParameters());
            createSport("Calistenia", "strength", createCalisthenicsParameters());

            // Deportes de cardio
            createSport("Running", "cardio", createRunningParameters());
            createSport("Ciclismo", "cardio", createCyclingParameters());
            createSport("Natación", "cardio", createSwimmingParameters());

            // Deportes de combate
            createSport("Boxeo", "combat", createBoxingParameters());
            createSport("MMA", "combat", createMMAParameters());
            createSport("Judo", "combat", createJudoParameters());

            // Deportes de flexibilidad
            createSport("Yoga", "flexibility", createYogaParameters());
            createSport("Pilates", "flexibility", createPilatesParameters());
        } catch (Exception e) {
            System.err.println("Error loading predefined sports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSport(String name, String category, Map<String, String> parameters) {
        SportEntity sport = new SportEntity();
        sport.setName(name);
        sport.setCategory(category);
        sport.setIsPredefined(true);
        sport.setIconUrl("/icons/sports/" + name.toLowerCase() + ".png");
        sport.setParameterTemplate(parameters);

        sportRepository.save(sport);
    }

    // Los métodos create...Parameters() se mantienen igual
    private Map<String, String> createGymParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("peso", "kg");
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("descanso", "seg");
        return params;
    }

    private Map<String, String> createCrossfitParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("peso", "kg");
        params.put("repeticiones", "rep");
        params.put("rounds", "rounds");
        params.put("tiempo", "min");
        params.put("descanso", "seg");
        return params;
    }

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
        params.put("elevación", "m");
        return params;
    }

    private Map<String, String> createCyclingParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("distancia", "km");
        params.put("tiempo", "min");
        params.put("velocidad", "km/h");
        params.put("elevación", "m");
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
        params.put("tiempo_round", "min");
        params.put("descanso", "seg");
        params.put("intensidad", "%");
        return params;
    }

    private Map<String, String> createMMAParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("rounds", "rounds");
        params.put("tiempo_round", "min");
        params.put("descanso", "seg");
        params.put("técnica", "text");
        return params;
    }

    private Map<String, String> createJudoParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("rounds", "rounds");
        params.put("tiempo_round", "min");
        params.put("descanso", "seg");
        params.put("técnica", "text");
        return params;
    }

    private Map<String, String> createYogaParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("postura", "text");
        params.put("duración", "seg");
        params.put("repeticiones", "rep");
        params.put("nivel", "text");
        return params;
    }

    private Map<String, String> createPilatesParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("repeticiones", "rep");
        params.put("series", "series");
        params.put("duración", "seg");
        params.put("nivel", "text");
        return params;
    }
}