---
name: sync-debug
description: Depura sync offline de JNOBFIT — PendingSyncOperation, SyncWorker, NetworkMonitor, Room. Usar cuando falle sincronización, datos offline no suben, o rutinas/workouts desincronizados.
---

# Debug sync offline — JNOBFIT

## Archivos clave

- `FrontEnd/app/src/main/java/com/fitapp/appfit/core/sync/SyncWorker.kt`
- `FrontEnd/app/src/main/java/com/fitapp/appfit/core/sync/NetworkMonitor.kt`
- `FrontEnd/app/src/main/java/com/fitapp/appfit/core/database/entity/PendingSyncOperation.kt`
- `FrontEnd/app/src/main/java/com/fitapp/appfit/App.kt` — punto de arranque
- `FrontEnd/.../feature/workout/data/repository/WorkoutRepositoryImpl.kt`

## Checklist de diagnóstico

```
- [ ] ¿SyncWorker.schedulePeriodic() se llama en App.onCreate?
- [ ] ¿NetworkMonitor.start() registrado?
- [ ] ¿Operaciones en tabla pending_sync_operations con estado correcto?
- [ ] ¿Tipo ROUTINE vs ROUTINE_EXERCISE vs SET_TEMPLATE?
- [ ] ¿API responde 2xx? ¿Token JWT válido?
- [ ] ¿Tras éxito, entidad Room pasa a SYNCED?
```

## Tipos de cola

| entityType | Estado implementación |
|------------|----------------------|
| ROUTINE | ✅ processRoutineQueue |
| ROUTINE_EXERCISE | ❌ stub — elimina op sin sync |
| SET_TEMPLATE | ❌ no procesado |

## Al arreglar

1. Reproducir: crear/editar offline → activar red → verificar logs
2. Fix mínimo en SyncWorker o repositorio que encola
3. No duplicar entidades en servidor (idempotencia)
4. Documentar en BACKLOG si queda pendiente otro tipo

## Prueba manual

1. Modo avión ON → crear/editar rutina
2. Modo avión OFF → abrir app / forzar sync
3. Verificar en backend o segunda instalación que datos aparecen
