package com.fitapp.backend.application.ports.input;

import com.fitapp.backend.application.dto.package_.request.*;
import com.fitapp.backend.application.dto.package_.response.*;

public interface PackageCommandUseCase {

    /**
     * Crea un nuevo paquete en estado DRAFT.
     * Solo usuarios PREMIUM pueden crearlos (excepto admin para packs oficiales).
     */
    PackageDetailResponse createPackage(
            CreatePackageRequest request,
            Long creatorUserId);

    /**
     * Actualiza un paquete existente.
     * Solo el creador puede actualizar (o admin para oficiales).
     */
    PackageDetailResponse updatePackage(
            Long packageId,
            UpdatePackageRequest request,
            Long currentUserId);

    /**
     * Publica un paquete (DRAFT → PUBLISHED).
     * El paquete debe pasar validaciones.
     */
    PackageStatusChangeResponse publishPackage(
            Long packageId,
            Long currentUserId);

    /**
     * Depreca un paquete (PUBLISHED → DEPRECATED).
     * Se usa cuando hay versión nueva.
     */
    PackageStatusChangeResponse deprecatePackage(
            Long packageId,
            String reason,
            Long currentUserId);

    /**
     * Suspende un paquete (por moderación).
     */
    PackageStatusChangeResponse suspendPackage(
            Long packageId,
            String reason,
            Long moderatorUserId);

    /**
     * Reinstancia un paquete suspendido.
     */
    PackageStatusChangeResponse unsuspendPackage(
            Long packageId,
            Long moderatorUserId);

    /**
     * Elimina un paquete (solo DRAFT o por admin).
     */
    void deletePackage(Long packageId, Long currentUserId);

    /**
     * Agrega un item a un paquete.
     */
    PackageItemResponse addItemToPackage(
            Long packageId,
            AddPackageItemRequest request,
            Long currentUserId);

    /**
     * Remueve un item de un paquete.
     */
    void removeItemFromPackage(
            Long packageId,
            Long itemId,
            Long currentUserId);

    /**
     * Reordena los items de un paquete.
     */
    PackageDetailResponse reorderPackageItems(
            Long packageId,
            ReorderPackageItemsRequest request,
            Long currentUserId);

    /**
     * Registra una descarga de un paquete.
     * Para paquetes gratuitos o después de compra.
     */
    void recordPackageDownload(Long packageId, Long userId);

    /**
     * Registra una valoración de un paquete.
     */
    void ratePackage(Long packageId, Double rating, Long userId);

    /**
     * Compra un paquete (paid packages).
     * Crea registro de transacción e impide que el vendedor lo elimine.
     */
    void purchasePackage(Long packageId, Long buyerUserId);
}
