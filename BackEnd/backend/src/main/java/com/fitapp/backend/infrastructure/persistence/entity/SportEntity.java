package com.fitapp.backend.infrastructure.persistence.entity;

import java.util.HashMap;
import java.util.Map;
import com.fitapp.backend.infrastructure.config.HashMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sports")
public class SportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Convert(converter = HashMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> parameterTemplate = new HashMap<>();
}