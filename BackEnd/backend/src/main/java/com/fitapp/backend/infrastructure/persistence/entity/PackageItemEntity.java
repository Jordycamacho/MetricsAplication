package com.fitapp.backend.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageItemType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Item de contenido dentro de un pack.
 * Solo uno de los FKs de contenido estará poblado, determinado por itemType.
 *
 * Por qué tabla única en vez de herencia:
 * - Los packs MIXED necesitan mezclar tipos en una misma lista ordenada.
 * - Las queries de "dame todo el contenido de este pack" son simples.
 * - El número de tipos es pequeño y estable.
 */
@Entity
@Table(name = "package_items",
    indexes = {
        @Index(name = "idx_pkgitem_pack",      columnList = "package_id"),
        @Index(name = "idx_pkgitem_type",      columnList = "item_type"),
        @Index(name = "idx_pkgitem_sport",     columnList = "sport_id"),
        @Index(name = "idx_pkgitem_parameter", columnList = "parameter_id"),
        @Index(name = "idx_pkgitem_routine",   columnList = "routine_id"),
        @Index(name = "idx_pkgitem_exercise",  columnList = "exercise_id"),
        @Index(name = "idx_pkgitem_category",  columnList = "category_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity pack;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private PackageItemType itemType;

    // ── FK de contenido (solo uno poblado según itemType) ────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private SportEntity sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id")
    private CustomParameterEntity parameter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private RoutineEntity routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ExerciseCategoryEntity category;

    // ── Presentación ─────────────────────────────────────────────────────────

    @Column(name = "display_order")
    private Integer displayOrder;

    /** Nota del creador del pack sobre este item. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Validación en persistencia ───────────────────────────────────────────

    @PrePersist
    @PreUpdate
    private void validate() {
        int populated = 0;
        if (sport != null)     populated++;
        if (parameter != null) populated++;
        if (routine != null)   populated++;
        if (exercise != null)  populated++;
        if (category != null)  populated++;

        if (populated != 1) {
            throw new IllegalStateException(
                "PackageItemEntity must have exactly one content FK populated, found: " + populated);
        }
    }
}