package com.fitapp.backend.application.dto.sport;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class SportRequest {
    
    @NotBlank(message = "El nombre del deporte es obligatorio")
    private String name;

    private Map<String, String> parameterTemplate;
    
    private String category;
}