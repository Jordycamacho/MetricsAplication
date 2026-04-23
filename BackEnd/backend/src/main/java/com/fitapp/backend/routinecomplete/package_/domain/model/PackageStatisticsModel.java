package com.fitapp.backend.routinecomplete.package_.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageStatisticsModel {
    private Long packageId;
    private Integer totalDownloads;
    private Integer totalPurchases;
    private Double averageRating;
    private Integer totalRatings;
    private Integer itemCount;
    private Integer downloadsLast30Days;
    private BigDecimal revenueGenerated;
    private LocalDateTime lastUpdated;
}