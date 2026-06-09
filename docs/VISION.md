# JNOBFIT — Visión y propósito

> Documento de referencia para desarrolladores y asistentes de IA.  
> Prioridades tácticas: [`docs/BACKLOG.md`](BACKLOG.md)

Última actualización: 2026-06-07

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

## Ecosistema JNOB (visión a largo plazo)

Más allá de JNOBFIT, la visión es un **ecosistema de productos** que comparten la misma estructura flexible: la comunidad puede crear contenido **gratis o de pago**, y cada app reutiliza el mismo patrón de producto sin reescribir la plataforma.

### Patrón de producto compartido

Todas las apps del ecosistema siguen el mismo ciclo:

1. **Crear entidades** con parámetros configurables (ejercicios, ingredientes, materiales, hábitos…).
2. **Organizar en plantillas o paquetes** (rutinas, recetas, protocolos, planes de estudio…).
3. **Ejecutar con guía** (timers, pasos, avisos de temperatura, descansos, etc.).
4. **Medir y guardar historial** cuando las reglas del dominio lo permitan.
5. **Compartir o vender** en un marketplace comunitario.

### Productos previstos

| Producto | Estado | Descripción |
|----------|--------|-------------|
| **JNOBFIT** | **En desarrollo** | Deporte general; v1 centrada en gimnasio, escalable a otros deportes |
| **JNOB Cook** | Idea futura | Cocina y nutrición: alimentos con calorías/macros, recetas, ejecución con timers y temperatura, porciones para una o varias personas; marketplace de paquetes de recetas; integración con Fit (alimentación ↔ entrenamiento) |
| **JNOB Body** | Idea futura | Composición corporal y medidas (peso, circunferencias, fotos de progreso, objetivos); complementa las métricas de entreno |
| **JNOB Rehab / Mobility** | Idea futura | Protocolos de recuperación, estiramientos y fisioterapia guiada; mismo motor de ejecución que Fit con otro perfil (tiempo, amplitud, escala de dolor, etc.) |
| **JNOB Mind** | Idea futura | Bienestar mental: respiración, meditación, sueño, hábitos con sesiones cronometradas e historial |
| **JNOB Outdoor** | Idea futura | Actividades al aire libre (rutas, ascensos, aguas abiertas); distancia, tiempo, GPS; paquetes de rutas de la comunidad |
| **JNOB Learn** | Idea futura | Aprendizaje de habilidades: drills, planes de estudio, ejecución con Pomodoro/timers; paquetes de creadores |
| **JNOB Craft / DIY** | Idea futura | Proyectos manuales: materiales, pasos, tiempos; paralelo directo con Cook (ingredientes → materiales, receta → proyecto) |

Hidratación, hábitos generales u otros nichos (p. ej. entrenamiento canino) pueden empezar como **módulos** dentro de apps existentes antes de justificar una app independiente.

### Webs de plataforma (no son apps de consumo)

| Web | Rol |
|-----|-----|
| **JNOB Hub** | Descubrimiento: perfiles, reseñas, rankings, seguir creadores, explorar paquetes gratis y de pago |
| **Portal de creadores** | Subir paquetes, fijar precios, ver analytics de ventas; taller para entrenadores, nutricionistas y otros creadores |
| **Dashboard unificado** | Una cuenta JNOB: progreso global, objetivos cruzados entre Fit, Cook y futuras apps |
| **Admin / moderación** | Calidad de contenido, reportes, políticas del marketplace |

El marketplace y la participación comunitaria dependen tanto de estas webs como de las apps; sin Hub + creadores, el ecosistema queda limitado al consumo pasivo.

### Núcleo compartido vs extensión por app

Para que el ecosistema escale sin fragmentarse, conviene separar:

| Núcleo compartido (plataforma) | Extensión por app / dominio |
|-------------------------------|-----------------------------|
| Cuenta y autenticación | Tipos de parámetro específicos |
| Suscripción y límites por plan | Reglas de agregación a métricas |
| Modelo de paquetes y marketplace | UI de ejecución por deporte/receta/etc. |
| Ejecución con pasos, timers e historial | Integraciones entre apps (p. ej. calorías ↔ gasto de entreno) |
| Sync offline y cola de operaciones | |

Nuevas apps deben ser **sabores del mismo motor**, no reescrituras desde cero.

### Orden estratégico sugerido

1. **JNOBFIT v1** estable (gimnasio funcional, arquitectura deporte-agnóstica).
2. **Plataforma compartida** en backend (cuenta, suscripción, paquetes, sync) — aunque al principio solo la use Fit.
3. **JNOB Cook** como segunda app que valida que el modelo no es exclusivo del deporte.
4. **Webs Hub + Creadores** para habilitar contenido comunitario gratis y de pago.
5. **Tercera vertical** según tracción: Rehab (cercana a Fit) o Body (métricas simples, alto valor para el usuario).

Añadir muchas apps antes de tener marketplace, creadores y cuenta unificada multiplica coste y fragmenta la comunidad.

**Hoy todo el esfuerzo de este repositorio va a JNOBFIT.** El resto del ecosistema es visión y planificación, no alcance de implementación actual.

---

## Qué NO es el alcance actual

Para evitar desviaciones de foco:

- **No** es una app exclusiva de gimnasio (aunque v1 se centre en perfeccionar ese flujo).
- **No** incluye JNOB Cook, otras apps del ecosistema JNOB ni sus webs (Hub, creadores, etc.).
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
