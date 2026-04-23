package com.fitapp.backend.routinecomplete.package_.aplication.port.input;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.routinecomplete.package_.aplication.dto.request.*;
import com.fitapp.backend.routinecomplete.package_.aplication.dto.response.*;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PackageQueryUseCase {

    /**
     * Obtiene un paquete publicado por ID (marketplace view).
     * Valida acceso según el tier del usuario.
     */
    PackageDetailResponse getPackageById(Long packageId, Long currentUserId);

    /**
     * Obtiene un paquete por su slug (URL-friendly).
     */
    PackageDetailResponse getPackageBySlug(String slug, Long currentUserId);

    /**
     * Lista paquetes publicados en marketplace con filtros.
     */
    PageResponse<PackageSummaryResponse> searchMarketplace(
            PackageFilterRequest filters,
            Pageable pageable,
            Long currentUserId);

    /**
     * Lista paquetes creados por un usuario específico.
     */
    PageResponse<PackageSummaryResponse> getUserPackages(
            Long userId,
            Pageable pageable,
            Long currentUserId);

    /**
     * Obtiene paquetes comprados/descargados por el usuario actual.
     */
    PageResponse<PackageSummaryResponse> getUserPurchasedPackages(
            Long currentUserId,
            Pageable pageable);

    /**
     * Obtiene paquetes oficiales (createdBy = NULL).
     */
    PageResponse<PackageSummaryResponse> getOfficialPackages(
            Pageable pageable);

    /**
     * Obtiene recomendaciones personalizadas para el usuario.
     */
    List<PackageSummaryResponse> getRecommendedPackages(
            Long currentUserId,
            int limit);

    /**
     * Obtiene estadísticas de un paquete.
     */
    PackageStatisticsResponse getPackageStatistics(Long packageId, Long currentUserId);

    /**
     * Obtiene trending packages.
     */
    List<PackageSummaryResponse> getTrendingPackages(int limit);

    /**
     * Obtiene paquetes mejor valorados.
     */
    List<PackageSummaryResponse> getTopRatedPackages(int limit);
}