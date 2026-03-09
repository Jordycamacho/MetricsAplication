package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.ImportSourceType;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

/**
 * Trazabilidad de cada importación de rutina.
 * Append-only — no se modifica.
 *
 * Útil para:
 * - Saber de dónde viene una rutina importada
 * - Detectar rutinas problemáticas que se importan mucho con warnings
 * - Soporte al usuario ("¿cuándo importaste esta rutina?")
 */
@Entity
@Table(name = "routine_import_log",
    indexes = {
        @Index(name = "idx_ril_routine",      columnList = "imported_routine_id"),
        @Index(name = "idx_ril_user",         columnList = "imported_by"),
        @Index(name = "idx_ril_imported_at",  columnList = "imported_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineImportLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_routine_id", nullable = false)
    private RoutineEntity importedRoutine;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ImportSourceType sourceType;

    /**
     * Referencia a la fuente según sourceType:
     * - FILE        → nombre del archivo
     * - SHARE_LINK  → el export_key UUID
     * - PACK        → "pack:{packageId}"
     * - MARKETPLACE → "marketplace:{originalRoutineId}"
     */
    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by", nullable = false)
    private UserEntity importedBy;

    @CreationTimestamp
    @Column(name = "imported_at", nullable = false, updatable = false)
    private LocalDateTime importedAt;

    /**
     * JSON array de advertencias generadas durante la importación.
     * Ej: ["exercise 'Sentadilla Búlgara' not found, skipped",
     *      "parameter 'RPE' not found, using default"]
     */
    @Column(name = "import_warnings", columnDefinition = "TEXT")
    private String importWarnings;
}