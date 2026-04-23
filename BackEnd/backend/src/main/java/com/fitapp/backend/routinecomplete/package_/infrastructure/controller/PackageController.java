package com.fitapp.backend.routinecomplete.package_.infrastructure.controller;

import com.fitapp.backend.application.dto.page.PageResponse;
import com.fitapp.backend.routinecomplete.package_.aplication.dto.request.*;
import com.fitapp.backend.routinecomplete.package_.aplication.dto.response.*;
import com.fitapp.backend.routinecomplete.package_.aplication.port.input.PackageCommandUseCase;
import com.fitapp.backend.routinecomplete.package_.aplication.port.input.PackageQueryUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Packages", description = "Package marketplace and management endpoints")
public class PackageController {

    private final PackageQueryUseCase packageQueryUseCase;
    private final PackageCommandUseCase packageCommandUseCase;

    @Operation(summary = "Search marketplace packages", description = "Search published packages with filters")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<PackageSummaryResponse>> searchMarketplace(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String packageType,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);

        PackageFilterRequest filters = PackageFilterRequest.builder()
                .search(search)
                .packageType(packageType)
                .isFree(isFree)
                .minRating(minRating)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<PackageSummaryResponse> result = packageQueryUseCase.searchMarketplace(filters, pageable, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get package by ID", description = "Retrieve full package details with all items")
    @GetMapping("/{packageId}")
    @ApiResponse(responseCode = "200", description = "Package found", content = @Content(schema = @Schema(implementation = PackageDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageDetailResponse> getPackageById(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        PackageDetailResponse response = packageQueryUseCase.getPackageById(packageId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get package by slug", description = "Retrieve package using URL-friendly slug")
    @GetMapping("/by-slug/{slug}")
    @ApiResponse(responseCode = "200", description = "Package found")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageDetailResponse> getPackageBySlug(
            @Parameter(description = "URL-friendly slug") @PathVariable String slug,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        PackageDetailResponse response = packageQueryUseCase.getPackageBySlug(slug, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get official packages", description = "List FitApp official packages")
    @GetMapping("/official")
    @ApiResponse(responseCode = "200", description = "Official packages retrieved")
    public ResponseEntity<PageResponse<PackageSummaryResponse>> getOfficialPackages(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PackageSummaryResponse> result = packageQueryUseCase.getOfficialPackages(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get user packages", description = "List packages created by specific user")
    @GetMapping("/creator/{userId}")
    @ApiResponse(responseCode = "200", description = "User packages retrieved")
    public ResponseEntity<PageResponse<PackageSummaryResponse>> getUserPackages(
            @Parameter(description = "Creator user ID") @PathVariable Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long currentUserId = extractUserId(jwt);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PackageSummaryResponse> result = packageQueryUseCase.getUserPackages(userId, pageable,
                currentUserId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get purchased packages", description = "List packages downloaded/purchased by current user")
    @GetMapping("/my-purchases")
    @ApiResponse(responseCode = "200", description = "Purchased packages retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<PageResponse<PackageSummaryResponse>> getMyPurchasedPackages(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PackageSummaryResponse> result = packageQueryUseCase.getUserPurchasedPackages(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get trending packages", description = "List trending packages (most downloaded recently)")
    @GetMapping("/trending")
    @ApiResponse(responseCode = "200", description = "Trending packages retrieved")
    public ResponseEntity<List<PackageSummaryResponse>> getTrendingPackages(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {

        List<PackageSummaryResponse> result = packageQueryUseCase.getTrendingPackages(limit);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get top-rated packages", description = "List packages with highest ratings")
    @GetMapping("/top-rated")
    @ApiResponse(responseCode = "200", description = "Top-rated packages retrieved")
    public ResponseEntity<List<PackageSummaryResponse>> getTopRatedPackages(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {

        List<PackageSummaryResponse> result = packageQueryUseCase.getTopRatedPackages(limit);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get recommended packages", description = "Get personalized package recommendations")
    @GetMapping("/recommendations")
    @ApiResponse(responseCode = "200", description = "Recommendations retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<List<PackageSummaryResponse>> getRecommendations(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        List<PackageSummaryResponse> result = packageQueryUseCase.getRecommendedPackages(userId, limit);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get package statistics", description = "Retrieve stats for a specific package")
    @GetMapping("/{packageId}/statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageStatisticsResponse> getPackageStatistics(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        PackageStatisticsResponse result = packageQueryUseCase.getPackageStatistics(packageId, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Create new package", description = "Create a new package (DRAFT status)")
    @PostMapping
    @ApiResponse(responseCode = "201", description = "Package created", content = @Content(schema = @Schema(implementation = PackageDetailResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Insufficient subscription tier")
    public ResponseEntity<PackageDetailResponse> createPackage(
            @Valid @RequestBody CreatePackageRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("CREATE_PACKAGE_REQUEST | userId={} | name={}", userId, request.getName());

        PackageDetailResponse response = packageCommandUseCase.createPackage(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update package", description = "Update an existing package (only creator)")
    @PutMapping("/{packageId}")
    @ApiResponse(responseCode = "200", description = "Package updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageDetailResponse> updatePackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @Valid @RequestBody UpdatePackageRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("UPDATE_PACKAGE_REQUEST | packageId={} | userId={}", packageId, userId);

        PackageDetailResponse response = packageCommandUseCase.updatePackage(packageId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Publish package", description = "Change package status from DRAFT to PUBLISHED")
    @PostMapping("/{packageId}/publish")
    @ApiResponse(responseCode = "200", description = "Package published", content = @Content(schema = @Schema(implementation = PackageStatusChangeResponse.class)))
    @ApiResponse(responseCode = "400", description = "Package cannot be published")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageStatusChangeResponse> publishPackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("PUBLISH_PACKAGE_REQUEST | packageId={} | userId={}", packageId, userId);

        PackageStatusChangeResponse response = packageCommandUseCase.publishPackage(packageId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deprecate package", description = "Mark package as deprecated (new version available)")
    @PostMapping("/{packageId}/deprecate")
    @ApiResponse(responseCode = "200", description = "Package deprecated")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageStatusChangeResponse> deprecatePackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        PackageStatusChangeResponse response = packageCommandUseCase.deprecatePackage(packageId, reason, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete package", description = "Delete a DRAFT package")
    @DeleteMapping("/{packageId}")
    @ApiResponse(responseCode = "204", description = "Package deleted")
    @ApiResponse(responseCode = "400", description = "Cannot delete published package")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<Void> deletePackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("DELETE_PACKAGE_REQUEST | packageId={} | userId={}", packageId, userId);

        packageCommandUseCase.deletePackage(packageId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add item to package", description = "Add content (sport, parameter, routine, exercise) to package")
    @PostMapping("/{packageId}/items")
    @ApiResponse(responseCode = "201", description = "Item added", content = @Content(schema = @Schema(implementation = PackageItemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid item")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageItemResponse> addItemToPackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @Valid @RequestBody AddPackageItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("ADD_ITEM_REQUEST | packageId={} | itemType={}", packageId, request.getItemType());

        PackageItemResponse response = packageCommandUseCase.addItemToPackage(packageId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove item from package", description = "Delete an item from package")
    @DeleteMapping("/{packageId}/items/{itemId}")
    @ApiResponse(responseCode = "204", description = "Item removed")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package or item not found")
    public ResponseEntity<Void> removeItemFromPackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        packageCommandUseCase.removeItemFromPackage(packageId, itemId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reorder package items", description = "Change display order of items")
    @PutMapping("/{packageId}/reorder")
    @ApiResponse(responseCode = "200", description = "Items reordered", content = @Content(schema = @Schema(implementation = PackageDetailResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid item order")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<PackageDetailResponse> reorderItems(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @Valid @RequestBody ReorderPackageItemsRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        PackageDetailResponse response = packageCommandUseCase.reorderPackageItems(packageId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download/install package", description = "Register package download")
    @PostMapping("/{packageId}/download")
    @ApiResponse(responseCode = "200", description = "Download recorded")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Access denied (subscription requirement)")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<Void> downloadPackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("DOWNLOAD_PACKAGE_REQUEST | packageId={} | userId={}", packageId, userId);

        packageCommandUseCase.recordPackageDownload(packageId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rate package", description = "Add or update package rating")
    @PostMapping("/{packageId}/rate")
    @ApiResponse(responseCode = "200", description = "Rating recorded")
    @ApiResponse(responseCode = "400", description = "Invalid rating (must be 1.0-5.0)")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    public ResponseEntity<Void> ratePackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @Parameter(description = "Rating value (1.0-5.0)") @RequestParam Double rating,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("RATE_PACKAGE_REQUEST | packageId={} | rating={}", packageId, rating);

        packageCommandUseCase.ratePackage(packageId, rating, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Purchase package", description = "Purchase a paid package")
    @PostMapping("/{packageId}/purchase")
    @ApiResponse(responseCode = "200", description = "Purchase successful")
    @ApiResponse(responseCode = "400", description = "Package is free or already purchased")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Package not found")
    @ApiResponse(responseCode = "409", description = "Payment failed")
    public ResponseEntity<Void> purchasePackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        log.info("PURCHASE_PACKAGE_REQUEST | packageId={} | userId={}", packageId, userId);

        packageCommandUseCase.purchasePackage(packageId, userId);
        return ResponseEntity.ok().build();
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt == null) {
            throw new RuntimeException("Authentication required");
        }
        
        Number userIdClaim = jwt.getClaim("userId");
        if (userIdClaim == null) {
            throw new RuntimeException("userId claim not found in JWT token");
        }

        return userIdClaim.longValue();
    }
}