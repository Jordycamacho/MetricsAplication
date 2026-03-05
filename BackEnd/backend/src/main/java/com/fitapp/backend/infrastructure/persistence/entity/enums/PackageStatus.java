package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum PackageStatus {
    DRAFT,        // en preparación, solo visible por el creador
    PUBLISHED,    // disponible en marketplace
    DEPRECATED,   // sustituido por versión nueva, sigue descargable
    SUSPENDED     // retirado por violación de normas
}