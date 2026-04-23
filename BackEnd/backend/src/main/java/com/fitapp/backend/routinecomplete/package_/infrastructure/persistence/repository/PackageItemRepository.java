package com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.repository;

import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity.PackageEntity;
import com.fitapp.backend.routinecomplete.package_.infrastructure.persistence.entity.PackageItemEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackageItemRepository extends JpaRepository<PackageItemEntity, Long> {

    /**
     * Obtiene todos los items de un paquete en orden.
     */
    @Query("SELECT i FROM PackageItemEntity i WHERE i.pack.id = :packageId ORDER BY i.displayOrder ASC")
    List<PackageItemEntity> findByPackageIdOrdered(@Param("packageId") Long packageId);

    /**
     * Obtiene items de un paquete por tipo.
     */
    @Query("SELECT i FROM PackageItemEntity i WHERE i.pack.id = :packageId AND i.itemType = :itemType")
    List<PackageItemEntity> findByPackageIdAndType(@Param("packageId") Long packageId, @Param("itemType") String itemType);

    /**
     * Cuenta items de un paquete.
     */
    long countByPackId(PackageEntity pack);

    /**
     * Comprueba si un paquete tiene items.
     */
    boolean existsByPackId(PackageEntity pack);

    /**
     * Elimina todos los items de un paquete.
     */
    @Modifying
    @Query("DELETE FROM PackageItemEntity i WHERE i.pack.id = :packageId")
    void deleteByPackageId(@Param("packageId") Long packageId);

    /**
     * Actualiza el orden de un item.
     */
    @Modifying
    @Query("UPDATE PackageItemEntity i SET i.displayOrder = :displayOrder WHERE i.id = :itemId")
    void updateDisplayOrder(@Param("itemId") Long itemId, @Param("displayOrder") Integer displayOrder);
}
