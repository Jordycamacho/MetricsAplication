package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.domain.model.package_.PackageItemModel;
import com.fitapp.backend.domain.model.package_.PackageModel;
import com.fitapp.backend.domain.model.package_.PackageStatisticsModel;
import com.fitapp.backend.application.dto.package_.request.*;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PackagePersistencePort {

    // ── CRUD Básico ────────────────────────────────────────────────────────

    /**
     * Persiste un nuevo paquete.
     */
    PackageModel save(PackageModel packageModel);

    /**
     * Actualiza un paquete existente.
     */
    PackageModel update(PackageModel packageModel);

    /**
     * Obtiene un paquete por ID.
     */
    PackageModel findById(Long id);

    /**
     * Obtiene un paquete con todos sus items eager-loaded.
     */
    PackageModel findWithItemsById(Long id);

    /**
     * Obtiene un paquete por slug.
     */
    PackageModel findBySlug(String slug);

    /**
     * Elimina un paquete (lógico o físico según lógica de negocio).
     */
    void delete(Long id);

    /**
     * Comprueba si existe un paquete con ese slug.
     */
    boolean existsBySlug(String slug);

    // ── Búsqueda & Filtrado ────────────────────────────────────────────────

    /**
     * Busca paquetes publicados con filtros.
     */
    PageResponse<PackageModel> searchPublished(
            PackageFilterRequest filters,
            Pageable pageable);

    /**
     * Obtiene paquetes de un usuario.
     */
    PageResponse<PackageModel> findByCreatorId(
            Long userId,
            Pageable pageable);

    /**
     * Obtiene paquetes oficiales (createdBy IS NULL).
     */
    PageResponse<PackageModel> findOfficialPackages(Pageable pageable);

    /**
     * Obtiene trending packages (por downloads/rating).
     */
    List<PackageModel> findTrending(int limit);

    /**
     * Obtiene best-rated packages.
     */
    List<PackageModel> findTopRated(int limit);

    // ── Items ──────────────────────────────────────────────────────────────

    /**
     * Agrega un item a un paquete.
     */
    PackageItemModel saveItem(PackageItemModel item);

    /**
     * Elimina un item de un paquete.
     */
    void deleteItem(Long itemId);

    /**
     * Obtiene todos los items de un paquete.
     */
    List<PackageItemModel> findItemsByPackageId(Long packageId);

    /**
     * Actualiza el orden de los items.
     */
    void updateItemsOrder(List<PackageItemModel> items);

    // ── Stats ──────────────────────────────────────────────────────────────

    /**
     * Incrementa contador de descargas.
     */
    void incrementDownloadCount(Long packageId);

    /**
     * Obtiene estadísticas de un paquete.
     */
    PackageStatisticsModel getStatistics(Long packageId);

    /**
     * Registra una valoración.
     */
    void addRating(Long packageId, Double rating);
}

/**
 * Port para operaciones de compra/descarga.
 * Interactúa con el sistema de pagos y transacciones.
 */
