package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.HashMap;
import java.util.Map;
import com.fitapp.backend.infrastructure.config.HashMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sports")
@Data
public class SportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy; 

    @Convert(converter = HashMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> parameterTemplate = new HashMap<>();

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "category")
    private String category; 
}