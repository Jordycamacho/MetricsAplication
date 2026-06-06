# JNOBFIT — Visión y propósito

> Documento de referencia para desarrolladores y asistentes de IA.  
> Prioridades tácticas: [`docs/BACKLOG.md`](BACKLOG.md)

Última actualización: 2026-06-06

---

## Visión

JNOBFIT es una **aplicación deportiva general** que permite a cualquier persona planificar entrenamientos, ejecutarlos y medir su progreso con **métricas personalizables**, independientemente del deporte que practique.

La visión a largo plazo es una plataforma donde gimnasio, natación, running, deportes de contacto, flexibilidad y otros deportes convivan sobre la misma base técnica y de producto, sin reescribir la app por cada disciplina.

---

## Propósito

Ofrecer una herramienta útil y escalable para:

1. **Definir ejercicios** con parámetros propios (peso, repeticiones, tiempo, distancia, etc.).
2. **Organizar rutinas** con plantillas de series y reglas de medición coherentes.
3. **Ejecutar sesiones** en tiempo real, con historial y seguimiento del progreso.
4. **Medir en métricas** cuando el deporte y los parámetros lo permitan, siguiendo reglas claras y extensibles.

El producto no nace como “app de gym”: nace como **motor deportivo adaptable**. El gimnasio es el primer caso de uso porque es el más común y el más sencillo de medir con precisión.

---

## Estado actual

**JNOBFIT es hoy el único producto en desarrollo** en este repositorio. No hay otras apps activas en paralelo.

| Área | Estado |
|------|--------|
| Stack | Android (Kotlin, MVVM, Room) + backend Spring Boot (hexagonal, PostgreSQL, Redis) |
| Modo offline | Sí, con cola de sincronización (`SyncWorker`, `NetworkMonitor`) |
| Rutinas y ejecución | Funcional; gym es el flujo más maduro |
| Parámetros personalizados | Soportados (NUMBER, etc.); unidades configurables |
| Historial de workouts | Parcial; mejoras pendientes en coherencia de datos |
| Sync | Infraestructura presente; varios tipos aún incompletos o con stubs |
| Suscripción | Modelo definido; límites por plan pendientes de aplicar en cliente |
| Marketplace / paquetes | Planificado o parcialmente implementado |
| Métricas avanzadas | Entidades en backend; API y UI aún no completas |
| Deportes de contacto / parámetros cualitativos | Requieren trabajo adicional |

---

## Enfoque v1

El objetivo de **v1** no es “terminar el gym y cerrar el proyecto”, sino entregar la **primera versión útil y escalable** que cumpla el propósito general del producto.

**Prioridades v1** (en orden):

1. **Estabilizar ejecución de rutinas** — timers, sonido, coherencia de sets y parámetros en pantalla.
2. **Coherencia de datos** — alinear caché local (Room) con el servidor; evitar entrenar con datos obsoletos.
3. **Completar sync offline** — arranque automático, tipos pendientes (`ROUTINE_EXERCISE`, `SET_TEMPLATE`, etc.).
4. **Workout e historial** — distinguir días, nombres y sesiones de forma fiable.
5. **Suscripción** — aplicar límites según plan en backend y Android.
6. **Resto de pendientes v1** — ver [`docs/BACKLOG.md`](BACKLOG.md).

**Post-v1** (no bloquean v1): métricas avanzadas, verificador de rutinas, objetivos, marketplace completo.

---

## Principios de diseño

Estos principios guían decisiones de arquitectura y producto:

1. **Deporte-agnóstico por defecto** — ejercicios, parámetros y rutinas no asumen un solo deporte; las customizaciones son capas, no reescrituras.
2. **Métricas cuando sea posible** — si un parámetro tiene unidad y regla de medición, debe poder agregarse y compararse en el tiempo.
3. **Offline-first** — el usuario puede entrenar sin red; la sincronización reconcilia con el servidor cuando hay conectividad.
4. **Escalabilidad gradual** — añadir un deporte nuevo no exige refactor masivo; se extienden modelos, parámetros o flujos de UI según necesidad.
5. **v1 con visión, no atajo** — las decisiones de gym deben reutilizarse para natación, running, etc.; evitar atajos que conviertan el código en “solo gym”.
6. **Cambios mínimos y focalizados** — especialmente en v1, priorizar fixes de estabilidad sobre features nuevas amplias.

---

## Deporte por deporte

| Deporte | Rol en el roadmap | Notas |
|---------|-------------------|-------|
| **Gimnasio** | **Foco actual (v1)** | Series, peso, repeticiones, descanso; flujo más maduro y medible |
| Natación | Futuro cercano | Distancia, tiempo, ritmo; parámetros ya encajan en el modelo genérico |
| Running | Futuro | Distancia, tiempo, ritmo cardíaco; extensión natural de parámetros |
| Deportes de contacto | Futuro | Métricas mixtas (cuantitativas + cualitativas); requiere diseño de parámetros no numéricos |
| Flexibilidad / movilidad | Futuro | Tiempo, repeticiones, rangos; posible enfoque más cualitativo |

Las customizaciones por deporte se incorporan **de forma incremental**: primero un flujo sólido y genérico, luego adaptaciones de UI, parámetros o reglas específicas.

---

## Futuro (ecosistema)

A largo plazo, el ecosistema **JNOB** podría incluir productos complementarios. **JNOB Cook** (nutrición y cocina) es una **idea futura**, no un producto en desarrollo ni parte del alcance actual de este repositorio.

Hoy todo el esfuerzo va a **JNOBFIT**.

---

## Qué NO es el alcance actual

Para evitar desviaciones de foco:

- **No** es una app exclusiva de gimnasio (aunque v1 se centre en perfeccionar ese flujo).
- **No** incluye JNOB Cook ni funcionalidades de nutrición/recetas.
- **No** prioriza marketplace, métricas avanzadas u objetivos por encima de estabilidad de ejecución y sync.
- **No** implica soportar todos los deportes en v1; implica **no cerrar la puerta** a ellos en diseño y arquitectura.
- **No** es un refactor arquitectónico masivo; v1 es estabilizar lo existente con cambios acotados.

---

## Referencias rápidas

| Recurso | Uso |
|---------|-----|
| [`docs/BACKLOG.md`](BACKLOG.md) | Tareas priorizadas, bugs y estado de v1 |
| [`README.MD`](../README.MD) | Stack, setup, arquitectura técnica |
| `.cursor/rules/project-overview.mdc` | Contexto para asistentes en el IDE |
