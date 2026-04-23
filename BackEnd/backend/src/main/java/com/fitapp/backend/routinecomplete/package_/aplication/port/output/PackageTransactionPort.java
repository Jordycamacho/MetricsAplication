package com.fitapp.backend.routinecomplete.package_.aplication.port.output;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.fitapp.backend.routinecomplete.package_.domain.model.PackagePurchaseModel;

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