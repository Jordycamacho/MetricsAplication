package com.fitapp.backend.domain.model;

import java.time.LocalDate;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class SubscriptionModel {
    private Long id;
    private Long userId; 
    private SubscriptionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    
    public abstract Integer getMaxRoutines();
}