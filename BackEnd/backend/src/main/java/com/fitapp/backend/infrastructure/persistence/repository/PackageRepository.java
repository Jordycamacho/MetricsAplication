package com.fitapp.backend.infrastructure.persistence.repository;

import com.fitapp.backend.infrastructure.persistence.entity.PackageEntity;
import com.fitapp.backend.infrastructure.persistence.entity.PackageItemEntity;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageStatus;
import com.fitapp.backend.infrastructure.persistence.entity.enums.PackageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Long>, JpaSpecificationExecutor<PackageEntity> {

        /**
         * Obtiene un paquete con todos sus items eager-loaded.
         */
        @EntityGraph(attributePaths = { "items", "createdBy" })
        Optional<PackageEntity> findById(Long id);

        /**
         * Obtiene un paquete por slug.
         */
        Optional<PackageEntity> findBySlug(String slug);

        /**
         * Comprueba si existe un slug (para validar duplicados).
         */
        boolean existsBySlug(String slug);

        /**
         * Comprueba si existe un slug diferente a un ID dado
         * (usado en actualizaciones).
         */
        @Query("SELECT COUNT(p) > 0 FROM PackageEntity p WHERE p.slug = :slug AND p.id != :packageId")
        boolean existsSlugForOtherId(@Param("slug") String slug, @Param("packageId") Long packageId);

        // ── Búsqueda & Filtrado ────────────────────────────────────────────────

        /**
         * Obtiene paquetes publicados.
         */
        Page<PackageEntity> findByStatus(PackageStatus status, Pageable pageable);

        /**
         * Obtiene paquetes de un tipo específico.
         */
        Page<PackageEntity> findByPackageType(PackageType packageType, Pageable pageable);

        /**
         * Obtiene paquetes gratuitos.
         */
        Page<PackageEntity> findByIsFree(Boolean isFree, Pageable pageable);

        /**
         * Obtiene paquetes creados por un usuario.
         */
        @EntityGraph(attributePaths = { "items" })
        Page<PackageEntity> findByCreatedByIdOrderByCreatedAtDesc(Long createdById, Pageable pageable);

        /**
         * Obtiene paquetes oficiales (createdBy = NULL).
         */
        @EntityGraph(attributePaths = { "items" })
        Page<PackageEntity> findByCreatedByIsNullAndStatus(PackageStatus status, Pageable pageable);

        /**
         * Búsqueda combinada en nombre y descripción.
         */
        @EntityGraph(attributePaths = { "items" })
        @Query("SELECT p FROM PackageEntity p WHERE p.status = 'PUBLISHED' AND " +
                        "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(p.tags) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<PackageEntity> searchPublished(@Param("search") String search, Pageable pageable);

        /**
         * Obtiene paquetes trending (más descargas en últimos 30 días).
         */
        @Query("SELECT p FROM PackageEntity p WHERE p.status = 'PUBLISHED' " +
                        "ORDER BY p.downloadCount DESC, p.rating DESC")
        List<PackageEntity> findTrending(Pageable pageable);

        /**
         * Obtiene paquetes mejor valorados.
         */
        @Query("SELECT p FROM PackageEntity p WHERE p.status = 'PUBLISHED' AND p.rating IS NOT NULL " +
                        "ORDER BY p.rating DESC, p.ratingCount DESC")
        List<PackageEntity> findTopRated(Pageable pageable);

        /**
         * Búsqueda avanzada con múltiples filtros.
         */
        @EntityGraph(attributePaths = { "items" })
        @Query("SELECT p FROM PackageEntity p WHERE " +
                        "p.status = 'PUBLISHED' AND " +
                        "(:packageType IS NULL OR p.packageType = :packageType) AND " +
                        "(:isFree IS NULL OR p.isFree = :isFree) AND " +
                        "(:minRating IS NULL OR p.rating >= :minRating) AND " +
                        "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<PackageEntity> findWithFilters(
                        @Param("packageType") PackageType packageType,
                        @Param("isFree") Boolean isFree,
                        @Param("minRating") Double minRating,
                        @Param("search") String search,
                        Pageable pageable);

        // ── Items ──────────────────────────────────────────────────────────────

        /**
         * Obtiene los items de un paquete en orden.
         */
        @Query("SELECT i FROM PackageItemEntity i WHERE i.pack.id = :packageId ORDER BY i.displayOrder ASC")
        List<PackageItemEntity> findItemsByPackageId(@Param("packageId") Long packageId);

        // ── Estadísticas ───────────────────────────────────────────────────────

        /**
         * Obtiene el contador de descargas de un paquete.
         */
        @Query("SELECT p.downloadCount FROM PackageEntity p WHERE p.id = :packageId")
        Integer getDownloadCount(@Param("packageId") Long packageId);

        /**
         * Incrementa el contador de descargas.
         */
        @Modifying
        @Query("UPDATE PackageEntity p SET p.downloadCount = p.downloadCount + 1 WHERE p.id = :packageId")
        void incrementDownloadCount(@Param("packageId") Long packageId);

        /**
         * Obtiene rating y rating count de un paquete.
         */
        @Query("SELECT p.rating FROM PackageEntity p WHERE p.id = :packageId")
        Double getRating(@Param("packageId") Long packageId);

        /**
         * Actualiza rating (media ponderada).
         * Se invoca desde el servicio.
         */
        @Modifying
        @Query("UPDATE PackageEntity p SET p.rating = :rating, p.ratingCount = p.ratingCount + 1 WHERE p.id = :packageId")
        void addRating(@Param("packageId") Long packageId, @Param("rating") Double rating);

        // ── Cambios de estado ──────────────────────────────────────────────────

        /**
         * Actualiza el estado de un paquete.
         */
        @Modifying
        @Query("UPDATE PackageEntity p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :packageId")
        int updateStatus(@Param("packageId") Long packageId, @Param("status") PackageStatus status);

        /**
         * Actualiza el slug de un paquete.
         */
        @Modifying
        @Query("UPDATE PackageEntity p SET p.slug = :slug, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :packageId")
        int updateSlug(@Param("packageId") Long packageId, @Param("slug") String slug);

        /**
         * Actualiza la versión (para nuevo release).
         */
        @Modifying
        @Query("UPDATE PackageEntity p SET p.version = :version, p.changelog = :changelog, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :packageId")
        int updateVersion(@Param("packageId") Long packageId, @Param("version") String version,
                        @Param("changelog") String changelog);

        // ── Listados especiales ────────────────────────────────────────────────

        /**
         * Obtiene paquetes creados recientemente.
         */
        @Query("SELECT p FROM PackageEntity p WHERE p.status = 'PUBLISHED' ORDER BY p.createdAt DESC")
        List<PackageEntity> findRecentPublished(Pageable pageable);

        /**
         * Obtiene paquetes deprecados (probablemente hay versión nueva).
         */
        @Query("SELECT p FROM PackageEntity p WHERE p.status = 'DEPRECATED' AND p.createdBy.id = :userId")
        List<PackageEntity> findDeprecatedByCreator(@Param("userId") Long userId);

        /**
         * Comprueba si un paquete puede ser deprecado
         * (existe un published con createdBy = null y packageType = X).
         */
        @Query("SELECT COUNT(p) > 0 FROM PackageEntity p " +
                        "WHERE p.createdBy IS NULL AND p.packageType = :packageType AND p.status = 'PUBLISHED'")
        boolean existsOfficialVersionOfType(@Param("packageType") PackageType packageType);

        /**
         * Obtiene paquetes según rango de fechas.
         */
        @Query("SELECT p FROM PackageEntity p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
        List<PackageEntity> findByCreatedAtRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Cuenta paquetes por estado.
         */
        long countByStatus(PackageStatus status);

        /**
         * Cuenta paquetes por usuario.
         */
        long countByCreatedById(Long userId);
}
