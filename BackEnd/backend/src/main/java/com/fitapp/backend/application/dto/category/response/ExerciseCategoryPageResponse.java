package com.fitapp.backend.application.dto.category.response;

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
    private List<ExerciseCategoryResponse> content;
    
    @Schema(description = "Número de página actual")
    private int pageNumber;
    
    @Schema(description = "Tamaño de página")
    private int pageSize;
    
    @Schema(description = "Total de elementos")
    private long totalElements;
    
    @Schema(description = "Total de páginas")
    private int totalPages;
    
    @Schema(description = "Es la primera página")
    private boolean first;
    
    @Schema(description = "Es la última página")
    private boolean last;
}