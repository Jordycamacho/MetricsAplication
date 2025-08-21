package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("FREE")
public class FreeSubscriptionEntity extends SubscriptionEntity {

    @Override
    public SubscriptionType getType() {
        return SubscriptionType.FREE;
    }

    @Column(name = "max_routines")
    private Integer maxRoutines = 1;
}