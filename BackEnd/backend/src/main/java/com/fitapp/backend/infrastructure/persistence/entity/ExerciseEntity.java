package com.fitapp.backend.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "exercises", indexes = {
        @Index(name = "idx_exercise_name", columnList = "name"),
        @Index(name = "idx_exercise_sport", columnList = "sport_id"),
        @Index(name = "idx_exercise_user", columnList = "user_id")
})
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

    @ManyToMany
    @JoinTable(name = "exercise_custom_parameters", joinColumns = @JoinColumn(name = "exercise_id"), inverseJoinColumns = @JoinColumn(name = "parameter_id"))
    private Set<CustomParameterEntity> supportedParameters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "exercise_parameter_templates", joinColumns = @JoinColumn(name = "exercise_id"))
    @MapKeyColumn(name = "parameter_name")
    @Column(name = "parameter_type")
    private Map<String, String> parameterTemplates = new HashMap<>();
}
