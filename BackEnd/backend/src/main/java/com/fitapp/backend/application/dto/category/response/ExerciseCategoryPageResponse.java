package com.fitapp.backend.application.dto.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada de categorías")
public class ExerciseCategoryPageResponse {
    
    @Schema(description = "Lista de categorías")
    @JsonProperty("content")
    private List<ExerciseCategoryResponse> content;
    
    @Schema(description = "Número de página actual")
    @JsonProperty("pageNumber")
    private int pageNumber;
    
    @Schema(description = "Tamaño de página")
    @JsonProperty("pageSize")
    private int pageSize;
    
    @Schema(description = "Total de elementos")
    @JsonProperty("totalElements")
    private long totalElements;
    
    @Schema(description = "Total de páginas")
    @JsonProperty("totalPages")
    private int totalPages;
    
    @Schema(description = "Es la primera página")
    @JsonProperty("first")
    private boolean first;
    
    @Schema(description = "Es la última página")
    @JsonProperty("last")
    private boolean last;
}