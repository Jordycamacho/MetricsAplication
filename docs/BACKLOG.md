# JNOBFIT — Backlog v1

> **Leyenda:** ✅ completada · 🔴 bloqueante v1 · 🟡 pendiente v1 · 🔵 post-v1 / feature grande  
> **Prioridad v1:** correcciones de ejecución de rutinas y workout → sync → suscripción → resto.

Última actualización: 2026-05-29

---

## Resumen rápido

| Estado | Cantidad |
|--------|----------|
| ✅ Completadas | 16 |
| 🟡 Pendientes v1 | ~13 |
| 🔵 Post-v1 / grandes | ~8 |

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
| 10 | Verificación de email | Flujo registro/confirmación por correo |
| 11 | Eliminación en cascada (general) | Revisar otras entidades además de RoutineExercise |
| 12 | Paquetes (marketplace) | Completar flujo de paquetes |
| 13 | Import / export | Pantalla `ImportExportFragment` |
| 14 | Conversor libras ↔ kilos | Utilidad de conversión en UI de parámetros |
| 15 | Ocultar menú inferior | Durante ejecución de rutina u otras pantallas inmersivas |
| 16 | Funciones del menú superior | Completar acciones del toolbar |
| 17 | Completar Home | Pantalla principal incompleta |
| 18 | Migrar diálogos a Bottom Sheets | Reemplazar `AlertDialog`/`MaterialAlertDialog` por sheets estilo `bottom_sheet_parameter_detail` en toda la app |

### Sync offline (infraestructura — detectado en revisión)

| # | Tarea | Notas |
|---|-------|-------|
| 18 | Arrancar sync al iniciar app | `SyncWorker.schedulePeriodic()` + `NetworkMonitor.start()` no wired |
| 19 | Sync de `ROUTINE_EXERCISE` | Stub actual borra operación sin sincronizar |
| 20 | Sync de `SET_TEMPLATE` | Tipo definido en cola pero no procesado |

---

## 🔵 Post-v1 (features grandes)

| # | Tarea | Notas |
|---|-------|-------|
| 21 | Métricas | Backend tiene entidades; falta API + `MetricsFragment` |
| 22 | Verificador de rutina | Validación/coherencia de rutinas |
| 23 | Objetivos | Metas de usuario / progreso |

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
