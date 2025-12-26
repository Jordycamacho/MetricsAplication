// com.fitapp.backend.infrastructure.config/HashMapConverter.java
package com.fitapp.backend.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Converter
@Slf4j
public class HashMapConverter implements AttributeConverter<Map<String, String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                log.debug("CONVERTER_TO_DB | attribute is null or empty");
                return "{}";
            }
            
            String json = objectMapper.writeValueAsString(attribute);
            log.debug("CONVERTER_TO_DB_SUCCESS | size={} | jsonLength={}", 
                     attribute.size(), json.length());
            return json;
        } catch (Exception e) {
            log.error("CONVERTER_TO_DB_ERROR | error={}", e.getMessage(), e);
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty() || dbData.equals("{}")) {
                log.debug("CONVERTER_TO_ENTITY | dbData is null or empty");
                return new HashMap<>();
            }
            
            Map<String, String> map = objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
            log.debug("CONVERTER_TO_ENTITY_SUCCESS | size={}", map.size());
            
            // Validar formato de keys
            map.keySet().forEach(key -> {
                if (!isValidKeyFormat(key)) {
                    log.warn("CONVERTER_KEY_FORMAT_WARNING | key={} | format may cause issues", key);
                }
            });
            
            return map;
        } catch (Exception e) {
            log.error("CONVERTER_TO_ENTITY_ERROR | dbData={} | error={}", 
                     dbData, e.getMessage(), e);
            // Retornar mapa vacío en caso de error para no bloquear la aplicación
            return new HashMap<>();
        }
    }
    
    private boolean isValidKeyFormat(String key) {
        // Aceptar camelCase, snake_case y kebab-case
        return key.matches("^[a-z]+([A-Z][a-z]*)*$") ||  // camelCase
               key.matches("^[a-z]+(_[a-z]+)*$") ||      // snake_case
               key.matches("^[a-z]+(-[a-z]+)*$");        // kebab-case
    }
}