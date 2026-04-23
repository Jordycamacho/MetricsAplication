package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;
import com.fitapp.backend.sport.infrastructure.persistence.entity.SportEntity;
import com.fitapp.backend.sport.infrastructure.persistence.repository.SportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SportDataLoader implements ApplicationRunner {

    private final SportRepository sportRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sportRepository.count() > 0) {
            log.info("Sports ya inicializados, saltando seed.");
            return;
        }

        log.info("Inicializando deportes predefinidos...");

        List<SportEntity> sports = List.of(

                // ── Musculación / Fitness ────────────────────────────────────────────
                sport("Musculación", "Entrenamiento de fuerza con pesas y máquinas"),
                sport("Powerlifting", "Disciplina centrada en sentadilla, press banca y peso muerto"),
                sport("Weightlifting", "Halterofilia olímpica: arrancada y dos tiempos"),
                sport("Calistenia", "Entrenamiento con el peso corporal"),
                sport("CrossFit", "Entrenamiento funcional de alta intensidad"),
                sport("Strongman", "Pruebas de fuerza extrema con implementos"),
                sport("Fitness Funcional", "Movimientos funcionales aplicados al deporte y vida diaria"),
                sport("Kettlebell", "Entrenamiento con pesas rusas"),
                sport("TRX / Suspensión", "Entrenamiento con bandas de suspensión"),

                // ── Artes Marciales y Combate ────────────────────────────────────────
                sport("Boxeo", "Arte marcial de puños con técnica y estrategia"),
                sport("Kickboxing", "Boxeo con técnica de piernas"),
                sport("Muay Thai", "Arte marcial tailandesa con puños, codos, rodillas y piernas"),
                sport("MMA", "Artes marciales mixtas"),
                sport("Judo", "Arte marcial de proyecciones y control en suelo"),
                sport("BJJ", "Brazilian Jiu-Jitsu, grappling en suelo"),
                sport("Lucha Libre", "Lucha olímpica y grecorromana"),
                sport("Karate", "Arte marcial japonesa de golpes y bloqueos"),
                sport("Taekwondo", "Arte marcial coreana con énfasis en patadas"),
                sport("Capoeira", "Arte marcial brasileña con elementos de danza"),
                sport("Esgrima", "Deporte de combate con armas blancas"),

                // ── Atletismo y Carrera ──────────────────────────────────────────────
                sport("Running", "Carrera en distancias variadas"),
                sport("Trail Running", "Carrera por montaña y terreno natural"),
                sport("Maratón", "Carrera de 42,195 km"),
                sport("Sprints", "Velocidad en distancias cortas"),
                sport("Atletismo – Saltos", "Salto de altura, longitud, triple y pértiga"),
                sport("Atletismo – Lanzamientos", "Jabalina, disco, martillo y peso"),
                sport("Marcha Atlética", "Caminar competitivo con técnica reglamentada"),

                // ── Ciclismo ─────────────────────────────────────────────────────────
                sport("Ciclismo de Ruta", "Ciclismo en carretera"),
                sport("Ciclismo de Montaña", "MTB en terreno off-road"),
                sport("Ciclismo Indoor", "Bicicleta estática o spinning"),
                sport("BMX", "Ciclismo acrobático y de velocidad"),
                sport("Triatlón", "Natación + ciclismo + carrera"),

                // ── Natación y Deportes Acuáticos ────────────────────────────────────
                sport("Natación", "Nado competitivo en piscina o aguas abiertas"),
                sport("Waterpolo", "Deporte acuático de equipo con marcador medible"),
                sport("Surf", "Deslizamiento sobre olas con tabla"),
                sport("Kayak / Piragüismo", "Navegación con remo en kayak o canoa"),
                sport("Remo", "Remo en embarcación o ergómetro"),
                sport("Natación en Aguas Abiertas", "Nado en ríos, lagos o mar"),

                // ── Deportes de Raqueta ───────────────────────────────────────────────
                sport("Tenis", "Deporte de raqueta individual o dobles"),
                sport("Pádel", "Tenis en pista cerrada con paredes"),
                sport("Bádminton", "Raqueta con volante"),
                sport("Squash", "Raqueta en pista cerrada"),
                sport("Ping Pong", "Tenis de mesa"),

                // ── Gimnasia y Acrobacia ──────────────────────────────────────────────
                sport("Gimnasia Artística", "Gimnasia con aparatos y suelo"),
                sport("Gimnasia Rítmica", "Gimnasia con implementos como cinta o pelota"),
                sport("Parkour", "Movimiento libre por el entorno urbano"),
                sport("Pole Fitness", "Fuerza y acrobacia en barra vertical"),

                // ── Yoga, Movilidad y Mente-Cuerpo ───────────────────────────────────
                sport("Yoga", "Práctica de posturas, respiración y meditación"),
                sport("Pilates", "Método de ejercicio centrado en el core"),
                sport("Movilidad", "Trabajo específico de rango de movimiento"),
                sport("Stretching", "Estiramientos estáticos y dinámicos"),

                // ── Escalada ──────────────────────────────────────────────────────────
                sport("Escalada en Roca", "Escalada en exterior sobre roca natural"),
                sport("Boulder", "Escalada de bloques sin cuerda"),
                sport("Escalada en Rocódromo", "Escalada en instalación indoor"),

                // ── Deportes de Aventura y Outdoors ──────────────────────────────────
                sport("Senderismo", "Caminatas largas en naturaleza"),
                sport("Alpinismo", "Ascenso a montañas de alta dificultad"),
                sport("Paracaidismo", "Salto en caída libre"),
                sport("Remo Indoor (Ergómetro)", "Remo con máquina de remo"),
                
                // ── Otros deportes individuales medibles ──────────────────────────────
                sport("Golf", "Deporte de precisión con palos en campo"),
                sport("Tiro con Arco", "Precisión con arco y flechas"),
                sport("Tiro Deportivo", "Precisión con armas de fuego deportivas"),
                sport("Hípica", "Equitación y saltos con caballo"),
                sport("Patinaje en Línea", "Patinaje sobre ruedas en línea"));

        sportRepository.saveAll(sports);
        log.info("Sports inicializados correctamente: {} deportes", sports.size());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private SportEntity sport(String name, String description) {
        SportEntity s = new SportEntity();
        s.setName(name);
        s.setIsPredefined(true);
        s.setSourceType(SportSourceType.OFFICIAL);
        s.setParameterTemplate(new java.util.HashMap<>());
        s.setCreatedBy(null); // predefinido → sin dueño
        return s;
    }
}