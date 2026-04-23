package com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity;

import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageType;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;
import com.fitapp.backend.user.infrastructure.persistence.entity.UserEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Paquete distribuible de contenido (deportes, parámetros, rutinas,
 * ejercicios).
 *
 * Flujo de vida:
 * DRAFT → PUBLISHED → DEPRECATED (cuando hay versión nueva)
 * → SUSPENDED (por moderación)
 *
 * Packs oficiales (createdBy = NULL) son creados por el equipo de FitApp.
 * Packs de usuario requieren suscripción PREMIUM y están sujetos a moderación.
 */
@Entity
@Table(name = "packages", indexes = {
        @Index(name = "idx_pkg_status", columnList = "status"),
        @Index(name = "idx_pkg_type_status", columnList = "package_type, status"),
        @Index(name = "idx_pkg_free_status", columnList = "is_free, status"),
        @Index(name = "idx_pkg_created_by", columnList = "created_by"),
        @Index(name = "idx_pkg_requires_sub", columnList = "requires_subscription")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_pkg_slug", columnNames = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Identificador URL-friendly único. Ej: "powerlifting-starter-v2".
     * Generado automáticamente desde name + version, editable por admin.
     */
    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 30)
    private PackageType packageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PackageStatus status = PackageStatus.DRAFT;

    // ── Precio ───────────────────────────────────────────────────────────────

    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private boolean isFree = true;

    /** NULL si isFree = true. */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    // ── Versión ──────────────────────────────────────────────────────────────

    /** Semver: 1.0.0. Incrementar en cada publicación de cambios. */
    @Column(name = "version", nullable = false, length = 20)
    @Builder.Default
    private String version = "1.0.0";

    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;

    // ── Acceso ───────────────────────────────────────────────────────────────

    /**
     * Nivel mínimo de suscripción para instalar este pack.
     * FREE = cualquiera puede instalarlo (si isFree=true).
     * STANDARD / PREMIUM = requiere ese tier o superior.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "requires_subscription", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionType requiresSubscription = SubscriptionType.FREE;

    // ── Stats ────────────────────────────────────────────────────────────────

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    // ── Metadata ─────────────────────────────────────────────────────────────

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    /**
     * Tags para búsqueda. Almacenado como JSON array string.
     * Ej: ["powerlifting","fuerza","principiante"]
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    // ── Relaciones ───────────────────────────────────────────────────────────

    /** NULL = pack oficial de JnobFit. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<PackageItemEntity> items = new ArrayList<>();

    // ── Auditoría ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public void incrementDownloads() {
        this.downloadCount++;
    }

    public boolean isPublished() {
        return status == PackageStatus.PUBLISHED;
    }

    public boolean isAccessibleByTier(SubscriptionType userTier) {
        if (requiresSubscription == SubscriptionType.FREE)
            return true;
        if (requiresSubscription == SubscriptionType.STANDARD) {
            return userTier == SubscriptionType.STANDARD || userTier == SubscriptionType.PREMIUM;
        }
        return userTier == SubscriptionType.PREMIUM;
    }
}