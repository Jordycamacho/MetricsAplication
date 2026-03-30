package com.fitapp.backend.application.ports.output;

import com.fitapp.backend.domain.model.package_.PackagePurchaseModel;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;

public interface PackageTransactionPort {

    /**
     * Registra una compra de paquete.
     * Retorna ID de transacción.
     */
    String recordPurchase(Long packageId, Long buyerUserId, BigDecimal amount);

    /**
     * Verifica si un usuario ha comprado un paquete.
     */
    boolean hasPurchased(Long packageId, Long userId);

    /**
     * Obtiene el historial de descargas/compras de un usuario.
     */
    List<PackagePurchaseModel> getUserPurchaseHistory(Long userId, Pageable pageable);
}