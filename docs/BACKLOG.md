# JNOBFIT — Backlog v1

> **Leyenda:** ✅ completada · 🔴 bloqueante v1 · 🟡 pendiente v1 · 🔵 post-v1 / feature grande  
> **Prioridad v1:** correcciones de ejecución de rutinas y workout → sync → suscripción → resto.  
> **Visión y propósito del producto:** [`docs/VISION.md`](VISION.md)

Última actualización: 2026-06-13

---

## Resumen rápido

| Estado | Cantidad |
|--------|----------|
| ✅ Completadas | 18 |
| 🟡 Pendientes v1 | ~13 |
| 🔵 Post-v1 / grandes | ~15 |

---

## 🔴 Bloqueantes v1 (corregir primero)

### Ejecución de rutinas (`WorkoutFragment` / execution)

| # | Tarea | Notas |
|---|-------|-------|
| 1 | Timers no son automáticos | Los timers de descanso no arrancan solos al completar set |
| 2 | Sonido con pantalla apagada | Al terminar set/ejercicio no suena con móvil apagado; solo al encender |
| 5 | Eliminar `RoutineExercise` en cascada | Borrar ejercicio de rutina debe eliminar también sus sets asociados |
| 6 | Mostrar unidad real del parámetro en ejecución | ✅ Corregido: etiqueta superior muestra unidad (kg, m…), no nombre |
| 7 | Logo en ejecución | Completar/implementar logo en pantalla de ejecución |

### Workout / historial

| # | Tarea | Notas |
|---|-------|-------|
| 8 | Distinción de día por ejercicio | En workout no se distingue qué ejercicios corresponden a cada día |

---

## 🟡 Pendientes v1 (importantes, no bloquean todo)

### Suscripción

| # | Tarea | Notas |
|---|-------|-------|
| 9 | Limitaciones por tipo de suscripción | Backend + Android: aplicar límites según plan |

### Implementaciones generales

| # | Tarea | Notas |
|---|-------|-------|
| 10 | Verificación de email | ✅ Flujo registro/confirmación por correo, hard gate login, módulo notification |
| 11 | Eliminación en cascada (general) | Revisar otras entidades además de RoutineExercise |
| 12 | Paquetes (marketplace) | Completar flujo de paquetes |
| 13 | Import / export | Pantalla `ImportExportFragment` |
| 14 | Conversor libras ↔ kilos | Utilidad de conversión en UI de parámetros |
| 15 | Ocultar menú inferior | Durante ejecución de rutina u otras pantallas inmersivas |
| 16 | Funciones del menú superior | Completar acciones del toolbar |
| 17 | Completar Home | Pantalla principal incompleta |
| 18 | Migrar diálogos a Bottom Sheets | Reemplazar `AlertDialog`/`MaterialAlertDialog` por sheets estilo `bottom_sheet_parameter_detail` en toda la app |
| 25 | Reportes y sugerencias (feedback) | ✅ App + backend + emails; ver Completadas |

### Sync offline (infraestructura — detectado en revisión)

| # | Tarea | Notas |
|---|-------|-------|
| 18 | Arrancar sync al iniciar app | `SyncWorker.schedulePeriodic()` + `NetworkMonitor.start()` no wired |
| 19 | Sync de `ROUTINE_EXERCISE` | Stub actual borra operación sin sincronizar |
| 20 | Sync de `SET_TEMPLATE` | Tipo definido en cola pero no procesado |

### Room / caché local Android (interferencias detectadas en uso real)

| # | Tarea | Notas |
|---|-------|-------|
| 24 | **Auditar y reducir interferencias de SQLite (Room) local** | La BD local del móvil provoca inconsistencias en varios flujos. **Síntomas:** rutina regenerada en servidor sigue viéndose antigua; listados/ejecución no coinciden con API; datos mezclados tras actualizar rutina. **Dónde impacta:** (1) `RoutineRepository` cachea listado y rutina completa en Room (`routines`, `routine_exercises`, `set_templates`, `set_parameters`); (2) **ejecución de rutina** (`WorkoutFragment` / `getRoutineForTraining`) usa fallback offline a Room si falla red — puede entrenar con ejercicios/sets viejos sin llamada al backend; (3) `LastSetExecutionEntity` aplica valores históricos locales sobre la plantilla en memoria; (4) `ActiveWorkoutCache` (SharedPreferences) restaura sesión con IDs de sets antiguos; (5) `getRoutines` persiste summaries con `userId=""` y no purga entradas huérfanas al sincronizar. **Objetivo v1:** definir estrategia clara servidor↔local (invalidar caché al regenerar/borrar rutina, TTL o versión de rutina, no mezclar plantilla API + histórico local sin validar `routineId`/versión). Archivos: `RoutineRepository.kt`, `WorkoutExecutionViewModel.kt`, `LocalLastExecutionValuesHelper.kt`, `ActiveWorkoutCache.kt`, DAOs en `feature/routine/database/`. **Workaround actual:** borrar datos de app o limpiar caché tras regenerar rutina en backend |

---

## 🔵 Post-v1 (features grandes)

| # | Tarea | Notas |
|---|-------|-------|
| 21 | Métricas | Backend tiene entidades; falta API + `MetricsFragment` |
| 22 | Verificador de rutina | Validación/coherencia de rutinas |
| 23 | Objetivos | Metas de usuario / progreso |
| 26 | Sección "Preguntas de ayuda" | Separada de reportes/sugerencias en la app |
| 27 | Reporte de contenido marketplace | Moderación de paquetes publicados |
| 28 | Valoración de la app | In-app review / enlace Play Store |
| 29 | Historial "Mis envíos" en app | Estados visibles del feedback (roadmap) |
| 30 | Sugerencias públicas + votos | Usar campo `is_public` en `user_feedback` |
| 31 | Panel admin web (feedback) | UI que consuma `/api/admin/feedback` |
| 32 | Firebase Crashlytics | Captura automática de crashes |
| 33 | Política de privacidad (feedback) | Consentimiento explícito antes de multi-idioma |

---

## ✅ Completadas

### Ejecución de rutinas
- [x] Guardar set: redirección y auto-incremento de set
- [x] Tiempo total desincronizado — corregido
- [x] Checkbox no se propagaban entre sets
- [x] Contador de sets hechos no aumentaba
- [x] Último valor ejecutado: solo local, sin llamar backend
- [x] Checkbox visual mejorado
- [x] Agregar parámetros individuales por set
- [x] Fix generador de rutinas

### Workout
- [x] No visualizaba nombre de ejercicios

### Implementaciones
- [x] Tiempo total de entrenamiento
- [x] Sonido y timers en segundo plano (parcial — ver pendiente pantalla apagada)
- [x] Filtros por día al añadir ejercicios a rutina
- [x] Ajuste de volumen y sonidos distintos
- [x] Parámetros NUMBER: botones +/- siempre +1 (decimales → redondeo al entero más cercano)
- [x] Edición manual vía Bottom Sheet (long press), estilo `bottom_sheet_parameter_detail`
- [x] Etiqueta superior en ejecución muestra unidad real (kg), no nombre del parámetro
- [x] Reportes y sugerencias (feedback): `FeedbackFragment`, `POST /api/feedback`, emails usuario + admin, API admin `/api/admin/feedback`

---

## Cómo usar este archivo

1. Al empezar un chat: *"Toma el siguiente item bloqueante del BACKLOG"*
2. Al terminar una tarea: marcar ✅ y mover a Completadas
3. Añadir síntoma + archivo + pasos para reproducir en items nuevos

### Plantilla para nuevos bugs

```markdown
| # | Tarea | Notas |
|---|-------|-------|
| N | Descripción corta | Archivo: `ruta/Archivo.kt` · Repro: 1) … 2) … · Esperado: … |
```
