package com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fitapp.backend.auth.infrastructure.persistence.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de un pack instalado por un usuario.
 *
 * Controla el acceso: si el pack es de pago, este registro representa
 * la "licencia" del usuario sobre el pack. Aunque el usuario haga downgrade,
 * el pack sigue accesible (ya pagó).
 *
 * Para packs gratuitos, se crea igualmente para poder rastrear qué tiene
 * instalado el usuario y mostrarle actualizaciones disponibles.
 */
@Entity
@Table(name = "user_installed_packages",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_package", columnNames = {"user_id", "package_id"})
    },
    indexes = {
        @Index(name = "idx_uip_user_active",   columnList = "user_id, is_active"),
        @Index(name = "idx_uip_package",       columnList = "package_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInstalledPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity pack;

    @CreationTimestamp
    @Column(name = "installed_at", nullable = false, updatable = false)
    private LocalDateTime installedAt;

    /** Versión del pack en el momento de la instalación. */
    @Column(name = "installed_version", length = 20)
    private String installedVersion;

    /**
     * False si el usuario desinstalé el pack (se queda como historial
     * pero el contenido ya no es accesible). Para packs de pago,
     * el usuario puede reinstalar sin coste adicional.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // ── Pago ─────────────────────────────────────────────────────────────────

    /** NULL si el pack era gratuito en el momento de la instalación. */
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /** ID de transacción del proveedor de pago. NULL si gratuito. */
    @Column(name = "transaction_id", length = 200)
    private String transactionId;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isPurchased() {
        return purchasePrice != null;
    }

    public boolean hasUpdate(String currentPackVersion) {
        return installedVersion != null && !installedVersion.equals(currentPackVersion);
    }
}