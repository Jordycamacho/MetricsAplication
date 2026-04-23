package com.fitapp.backend.routinecomplete.routine.infrastructure.persistence.converter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import jakarta.persistence.AttributeConverter;

@Component
public class DaysOfWeekConverter implements AttributeConverter<Set<DayOfWeek>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Configurar para ignorar mayúsculas/minúsculas
    static {
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }
    
    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(days);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting days to JSON", e);
        }
    }
    
    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || dbData.trim().equals("[]")) {
            return new HashSet<>(); 
        }
        try {
            return objectMapper.readValue(dbData, 
                new TypeReference<Set<DayOfWeek>>() {});
        } catch (IOException e) {
            System.err.println("⚠️ Error parsing training_days JSON: " + dbData);
            System.err.println("Error: " + e.getMessage());
            return new HashSet<>();
        }
    }
}