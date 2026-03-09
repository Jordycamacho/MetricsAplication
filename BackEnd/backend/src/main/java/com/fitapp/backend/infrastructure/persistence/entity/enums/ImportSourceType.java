package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum ImportSourceType {
    FILE,           // JSON/ZIP subido por el usuario
    SHARE_LINK,     // enlace de share por export_key
    PACK,           // instalado desde un PackageEntity
    MARKETPLACE     // comprado en marketplace
}
