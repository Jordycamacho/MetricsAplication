package com.fitapp.backend.application.dto.sport.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Data
@Slf4j
public class SportRequest {
    
    @NotBlank(message = "El nombre del deporte es obligatorio")
    @Schema(description = "Nombre del deporte", example = "Fútbol")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Template de parámetros para el deporte", example = "{\"duration\": \"minutes\", \"distance\": \"km\"}")
    @JsonProperty("parameterTemplate")
    private Map<String, String> parameterTemplate;
    
    @Schema(description = "Categoría del deporte", example = "Team Sports")
    @JsonProperty("category")
    private String category;
    
    @Schema(description = "Tipo de origen del deporte", example = "USER_CREATED")
    @JsonProperty("sourceType")
    private SportSourceType sourceType = SportSourceType.USER_CREATED;
    
    public void logRequestData() {
        log.info("SPORT_REQUEST_RECEIVED | name={} | category={} | sourceType={}", 
                name, category, sourceType);
        if (parameterTemplate != null) {
            log.debug("SPORT_REQUEST_PARAMETERS | templateSize={} | template={}", 
                    parameterTemplate.size(), parameterTemplate);
        }
    }
}