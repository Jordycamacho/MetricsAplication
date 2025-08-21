package com.fitapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "custom_parameters")
public class CustomParameterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;  // Ej: "peso", "repeticiones", "altura_muro"
    
    @Column(nullable = false)
    private String unit;  // Ej: "kg", "rep", "m"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id") 
    private UserEntity owner;  // Dueño del parámetro (puede ser nulo si es global)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;  // Deporte asociado (opcional)
}