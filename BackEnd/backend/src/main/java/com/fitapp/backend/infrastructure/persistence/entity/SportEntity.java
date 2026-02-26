package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.config.StringMapConverter;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "sports")
@Data
@Slf4j
public class SportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SportSourceType sourceType = SportSourceType.OFFICIAL;

    @Convert(converter = StringMapConverter.class)
    @Column(columnDefinition = "TEXT", name = "parameter_template")
    private Map<String, String> parameterTemplate = new HashMap<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @PrePersist
    @PreUpdate
    public void logDataFormat() {
        log.debug("SPORT_ENTITY_DATA | id={} | name={} | sourceType={} | isPredefined={}",
                id, name, sourceType, isPredefined);
        log.debug("SPORT_ENTITY_PARAMETERS | templateKeys={}",
                parameterTemplate != null ? parameterTemplate.keySet().size() : 0);

        if (name != null && !name.matches("^[a-zA-Z0-9\\s]+$")) {
            log.warn("SPORT_NAME_FORMAT_WARNING | name contains special characters");
        }
    }
}