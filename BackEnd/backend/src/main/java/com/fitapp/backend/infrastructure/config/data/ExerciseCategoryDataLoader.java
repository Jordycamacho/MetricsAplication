package com.fitapp.backend.infrastructure.config.data;

import com.fitapp.backend.infrastructure.persistence.entity.ExerciseCategoryEntity;
import com.fitapp.backend.infrastructure.persistence.repository.ExerciseCategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseCategoryDataLoader {
    
    private final ExerciseCategoryRepository exerciseCategoryRepository;
    
    @PostConstruct
    @Transactional
    public void loadDefaultCategories() {
        long count = exerciseCategoryRepository.count();
        
        if (count == 0) {
            log.info("Creating default categories...");
            
            try {
                // Solo categorías generales, sin deporte específico
                ExerciseCategoryEntity fuerza = ExerciseCategoryEntity.builder()
                        .name("Fuerza")
                        .description("Ejercicios para desarrollar fuerza muscular")
                        .isPredefined(true)
                        .isPublic(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                ExerciseCategoryEntity cardio = ExerciseCategoryEntity.builder()
                        .name("Cardio")
                        .description("Ejercicios para mejorar la resistencia cardiovascular")
                        .isPredefined(true)
                        .isPublic(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                ExerciseCategoryEntity flexibilidad = ExerciseCategoryEntity.builder()
                        .name("Flexibilidad")
                        .description("Ejercicios para mejorar la flexibilidad y movilidad")
                        .isPredefined(true)
                        .isPublic(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                exerciseCategoryRepository.saveAll(Arrays.asList(fuerza, cardio, flexibilidad));
                log.info("Created {} default categories", 3);
                
            } catch (Exception e) {
                log.error("Error creating categories: {}", e.getMessage());
            }
        } else {
            log.info("Categories already exist: {}", count);
        }
    }
}