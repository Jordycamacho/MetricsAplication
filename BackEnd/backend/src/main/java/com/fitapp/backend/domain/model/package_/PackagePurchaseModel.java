package com.fitapp.backend.domain.model.package_;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PackagePurchaseModel {
    private Long id;
    private Long packageId;
    private Long buyerId;
    private BigDecimal amount;
    private LocalDateTime purchasedAt;
    private String transactionId;
    private boolean downloaded;
    private LocalDateTime downloadedAt;
}