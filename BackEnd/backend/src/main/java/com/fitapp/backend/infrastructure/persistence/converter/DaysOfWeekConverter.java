package com.fitapp.backend.infrastructure.persistence.converter;

import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitapp.backend.infrastructure.persistence.entity.enums.DayOfWeek;
import jakarta.persistence.AttributeConverter;

@Component
public class DaysOfWeekConverter implements AttributeConverter<Set<DayOfWeek>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> days) {
        try {
            return objectMapper.writeValueAsString(days);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting days to JSON", e);
        }
    }
    
    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        try {
            // Leer directamente como Set<DayOfWeek>
            return objectMapper.readValue(dbData, 
                new TypeReference<Set<DayOfWeek>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to days", e);
        }
    }
}