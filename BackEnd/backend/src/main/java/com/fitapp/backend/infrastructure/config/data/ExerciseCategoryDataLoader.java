package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ExerciseCategoryDataLoader implements ApplicationRunner {

    private final ExerciseCategoryRepository exerciseCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (exerciseCategoryRepository.count() > 0) {
            log.info("Categorías ya inicializadas, saltando seed.");
            return;
        }

        log.info("Inicializando categorías de ejercicio predefinidas...");

        List<ExerciseCategoryEntity> categories = List.of(

                // ── Musculación por grupo muscular ────────────────────────────────────
                category("Pecho", "Ejercicios para el pectoral mayor y menor", 1),
                category("Espalda", "Ejercicios para dorsales, trapecios y romboides", 2),
                category("Hombros", "Ejercicios para deltoides y manguito rotador", 3),
                category("Bíceps", "Ejercicios de flexión de codo con énfasis en bíceps", 4),
                category("Tríceps", "Ejercicios de extensión de codo con énfasis en tríceps", 5),
                category("Piernas", "Ejercicios para cuádriceps, isquiotibiales y glúteos", 6),
                category("Glúteos", "Ejercicios de aislamiento y compuestos para glúteos", 7),
                category("Core", "Abdominales, oblicuos y estabilizadores del tronco", 8),
                category("Antebrazos", "Ejercicios para flexores y extensores del antebrazo", 9),
                category("Gemelos", "Ejercicios para sóleo y gastrocnemio", 10),

                // ── Tipos de movimiento ───────────────────────────────────────────────
                category("Empuje", "Movimientos de empuje horizontal y vertical", 11),
                category("Tirón", "Movimientos de tracción horizontal y vertical", 12),
                category("Bisagra de Cadera", "Hip hinge: peso muerto, kettlebell swing, buenos días", 13),
                category("Sentadilla", "Patrones de sentadilla con variantes", 14),
                category("Cargada", "Movimientos olímpicos: cargada, arrancada y variantes", 15),
                category("Isométrico", "Ejercicios de tensión sin movimiento articular", 16),

                // ── Cardio y Resistencia ──────────────────────────────────────────────
                category("Cardio", "Ejercicios aeróbicos para mejorar la resistencia cardiovascular", 17),
                category("HIIT", "Intervalos de alta intensidad", 18),
                category("Resistencia Aeróbica", "Esfuerzos continuos de baja-media intensidad", 19),
                category("Resistencia Anaeróbica", "Esfuerzos de alta intensidad de corta duración", 20),
                category("Sprints", "Aceleraciones máximas en distancias cortas", 21),

                // ── Artes Marciales y Combate ─────────────────────────────────────────
                category("Técnica de Boxeo", "Golpes, combinaciones y defensas del boxeo", 22),
                category("Técnica de Patadas", "Patadas frontales, laterales, circulares y de cabeza", 23),
                category("Grappling", "Control, proyecciones y sumisiones en suelo", 24),
                category("Clinch", "Trabajo en cuerpo a cuerpo y Muay Thai clinch", 25),
                category("Saco y Manoplas", "Trabajo con saco de boxeo, manoplas y pao", 26),
                category("Defensa Personal", "Técnicas de defensa y contraataque", 27),

                // ── Atletismo y Carrera ───────────────────────────────────────────────
                category("Carrera de Velocidad", "Sprints y aceleraciones de corta distancia", 28),
                category("Carrera de Fondo", "Intervalos y tiradas largas", 29),
                category("Saltos Atléticos", "Salto de altura, longitud, triple y pértiga", 30),
                category("Lanzamientos", "Jabalina, disco, martillo y peso", 31),
                category("Pliometría", "Ejercicios explosivos: saltos, cajones, drop jumps", 32),

                // ── Natación ─────────────────────────────────────────────────────────
                category("Técnica de Natación", "Estilos y mecánica del nado", 33),
                category("Series de Natación", "Intervalos y bloques de distancia en piscina", 34),

                // ── Ciclismo ─────────────────────────────────────────────────────────
                category("Ciclismo Intervalos", "Series de potencia y VO2max en bici", 35),
                category("Ciclismo Resistencia", "Tiradas largas de baja intensidad", 36),

                // ── Escalada ─────────────────────────────────────────────────────────
                category("Fuerza de Agarre", "Ejercicios para mejorar la fuerza de manos y antebrazos", 37),
                category("Técnica de Escalada", "Movimientos y habilidades específicas de escalada", 38),

                // ── Movilidad y Recuperación ──────────────────────────────────────────
                category("Movilidad", "Trabajo de rango de movimiento articular", 39),
                category("Flexibilidad", "Estiramientos estáticos y dinámicos", 40),
                category("Recuperación Activa", "Ejercicios de bajo impacto para facilitar la recuperación", 41),
                category("Foam Rolling", "Automasaje con rodillo de espuma", 42),

                // ── Yoga y Pilates ────────────────────────────────────────────────────
                category("Yoga – Asanas", "Posturas de yoga", 43),
                category("Pilates", "Ejercicios del método Pilates", 44),

                // ── Funcional y Coordinación ──────────────────────────────────────────
                category("Funcional", "Movimientos multiarticulares aplicados a actividades reales", 45),
                category("Coordinación", "Ejercicios de agilidad, equilibrio y coordinación", 46),
                category("Calentamiento", "Rutinas de activación previas al entrenamiento", 47),
                category("Vuelta a la Calma", "Ejercicios de enfriamiento y relajación post-entrenamiento", 48));

        exerciseCategoryRepository.saveAll(categories);
        log.info("Categorías inicializadas correctamente: {} categorías", categories.size());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ExerciseCategoryEntity category(String name, String description, int order) {
        LocalDateTime now = LocalDateTime.now();
        return ExerciseCategoryEntity.builder()
                .name(name)
                .description(description)
                .isPredefined(true)
                .isPublic(true)
                .isActive(true)
                .displayOrder(order)
                .usageCount(0)
                .owner(null)
                .sport(null)
                .createdAt(now) // ← explícito: @CreatedDate no siempre se dispara en ApplicationRunner
                .updatedAt(now) // ← explícito: igual
                .build();
    }
}