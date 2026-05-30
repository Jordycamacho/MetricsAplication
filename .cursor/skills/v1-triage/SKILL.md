---
name: v1-triage
description: Prioriza y planifica tareas de JNOBFIT v1 leyendo docs/BACKLOG.md. Usar cuando el usuario pida siguiente tarea, priorizar backlog, plan de v1, o qué arreglar primero.
---

# Triage v1 — JNOBFIT

## Instrucciones

1. Leer `docs/BACKLOG.md` en la raíz del proyecto
2. Identificar el **siguiente item 🔴 bloqueante** sin marcar ✅
3. Si no hay bloqueantes, tomar el primer 🟡 pendiente v1
4. Antes de codear, localizar archivos relevantes con búsqueda en `FrontEnd/` o `BackEnd/`
5. Proponer plan en 3–5 pasos; implementar cambio **mínimo**
6. Al cerrar: indicar cómo probar manualmente en Android o Docker

## Orden de prioridad

1. Crashes / `TODO()` en UI
2. Ejecución de rutinas (`feature/workout/presentation/execution/`)
3. Sync offline (`core/sync/`)
4. Suscripción / límites
5. Features 🔵 post-v1 — **no empezar** salvo petición explícita

## Formato de respuesta al usuario

```markdown
## Tarea: [nombre]
**Prioridad:** 🔴/🟡
**Archivos:** lista
**Plan:** 1…n
**Prueba manual:** pasos
```

## Reglas

- Español en comunicación
- No refactor masivo en fixes de v1
- Actualizar BACKLOG al completar (mover a ✅ Completadas)
