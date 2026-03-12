# JNOBFIT — Plataforma de Seguimiento Fitness

Aplicación full-stack de fitness con cliente Android nativo y backend Spring Boot, que incluye gestión de entrenamientos, métricas personalizadas, planificación de rutinas y seguimiento de sesiones en tiempo real.

---

## Tabla de Contenidos

- [Descripción general](#descripción-general)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Primeros pasos](#primeros-pasos)
  - [Requisitos previos](#requisitos-previos)
  - [Backend con Docker](#backend-con-docker)
  - [Backend en local (desarrollo)](#backend-en-local-desarrollo)
  - [App Android](#app-android)
- [Configuración](#configuración)
  - [Variables de entorno](#variables-de-entorno)
  - [Perfiles de Spring](#perfiles-de-spring)
- [Documentación de la API](#documentación-de-la-api)
- [Monitorización](#monitorización)
- [Seguridad](#seguridad)
- [Base de datos](#base-de-datos)

---

## Descripción general

FitApp permite a los usuarios crear rutinas personalizadas, registrar sesiones de entrenamiento, definir ejercicios con parámetros propios y hacer seguimiento de su progreso. El backend expone una API REST que consume la app Android nativa.

**Funcionalidades principales:**
- Autenticación JWT + OAuth2 (Google)
- Parámetros de ejercicio personalizados y plantillas de series
- Ejecución y historial de sesiones de entrenamiento
- Planes de suscripción con límites por funcionalidad
- App Android con modo offline y sincronización en segundo plano
- Métricas y observabilidad con Prometheus y Grafana

---

## Stack tecnológico

### Backend
| Capa | Tecnología |
|------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Base de datos | PostgreSQL 17 |
| Migraciones | Flyway |
| Caché | Redis 7 + Caffeine |
| Seguridad | Spring Security, JWT (Auth0), OAuth2 |
| Build | Maven |
| Observabilidad | Micrometer, Prometheus, Grafana |
| Documentación | SpringDoc OpenAPI / Swagger UI |

### Android (Frontend)
| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin |
| Arquitectura | MVVM + patrón Repository |
| BD local | Room (SQLite) |
| Red | Retrofit + OkHttp |
| Navegación | Navigation Component |
| Tareas en fondo | WorkManager |

---

## Arquitectura

El backend sigue **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```
┌──────────────────────────────────────────────┐
│                 Controladores                │  ← Adaptadores HTTP (infraestructura)
├──────────────────────────────────────────────┤
│             Servicios de Aplicación          │  ← Casos de uso / lógica de negocio
│         (ports/input  ←→  ports/output)      │
├──────────────────────────────────────────────┤
│               Modelos de Dominio             │  ← Entidades de negocio puras
├──────────────────────────────────────────────┤
│        Adaptadores de Persistencia/Seguridad │  ← JPA, Redis, OAuth2
└──────────────────────────────────────────────┘
```

La app Android sigue **MVVM con patrón Repository**, sincronizando los datos con el servidor mediante un `SyncWorker` en segundo plano cuando hay conectividad disponible.

---

## Estructura del proyecto

```
MetricsApplication/
├── BackEnd/backend/                   # Aplicación Spring Boot
│   └── src/main/java/com/fitapp/backend/
│       ├── application/               # DTOs, puertos de casos de uso, servicios
│       ├── domain/                    # Modelos de dominio y excepciones
│       └── infrastructure/            # Controladores, persistencia, seguridad, config
└── FrontEnd/app/src/main/java/com/fitapp/appfit/
    ├── auth/                          # Login, Registro, AuthViewModel
    ├── database/                      # DAOs y entidades Room
    ├── network/                       # Cliente Retrofit y servicios API
    ├── repository/                    # Repositorios (API + BD local)
    ├── sync/                          # Infraestructura de sincronización WorkManager
    ├── ui/                            # Fragments y adaptadores (por funcionalidad)
    └── utils/                         # Helpers, interceptores, gestión de sesión
```

---

## Primeros pasos

### Requisitos previos

- **Docker y Docker Compose** (recomendado para el backend)
- **Java 21** (para desarrollo local del backend)
- **Maven 3.9+** (para desarrollo local del backend)
- **Android Studio Hedgehog o superior** (para la app Android)
- **Android SDK 26+**

---

### Backend con Docker

Esta es la forma recomendada de ejecutar el stack completo (app + PostgreSQL + Redis + Prometheus + Grafana).

**1. Clona el repositorio y navega al backend:**
```bash
cd BackEnd/backend
```

**2. Crea tu archivo `.env`** (ver [Variables de entorno](#variables-de-entorno)):
```bash
cp .env.example .env
# Edita .env con tus valores
```

**3. Levanta todos los servicios:**
```bash
docker compose up -d
```

**4. Verifica que la app está corriendo:**
```bash
curl http://localhost:8082/actuator/health
```

| Servicio | URL |
|---------|-----|
| API Backend | http://localhost:8082 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

---

### Backend en local (desarrollo)

```bash
cd BackEnd/backend

# Levanta solo la infraestructura necesaria
docker compose up -d db redis

# Ejecuta la app con el perfil de desarrollo
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

---

### App Android

1. Abre la carpeta `FrontEnd/` en **Android Studio**.
2. En `local.properties`, configura la URL base del backend:
   ```properties
   BASE_URL=http://10.0.2.2:8082/
   ```
   > `10.0.2.2` apunta a `localhost` desde el emulador de Android.
3. Ejecuta la app en un emulador o dispositivo físico (API 26+).

---

## Configuración

### Variables de entorno

Crea un archivo `.env` en `BackEnd/backend/`:

```env
# PostgreSQL
DB_URL=jdbc:postgresql://db:5432/fitapp_prod
DB_USERNAME=tu_usuario_db
DB_PASSWORD=tu_contraseña_db

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=tu_contraseña_redis

# JWT
JWT_SECRET=tu_secreto_jwt_minimo_256_bits

# Google OAuth2
GOOGLE_CLIENT_ID=tu_google_client_id
GOOGLE_CLIENT_SECRET=tu_google_client_secret
APP_BASE_URL=https://tu-dominio.com

# CORS
CORS_ALLOWED_ORIGINS=https://tu-dominio.com

# Correo electrónico (Gmail SMTP)
MAIL_USERNAME=tu@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicacion

# Grafana
GRAFANA_USER=admin
GRAFANA_PASSWORD=tu_contraseña_grafana
```

### Perfiles de Spring

| Perfil | Uso | Swagger | Logs SQL | DDL |
|--------|-----|---------|----------|-----|
| `dev` | Desarrollo local | ✅ Activo | ✅ Activo | `update` |
| `prod` | Producción | ❌ Desactivado | ❌ Desactivado | `validate` |

Activar mediante variable de entorno:
```bash
SPRING_PROFILES_ACTIVE=prod
```

En Docker ya está configurado en el `Dockerfile`:
```dockerfile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

---

## Documentación de la API

Disponible únicamente con el **perfil `dev`**:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`

Módulos principales de la API:

| Módulo | Ruta base |
|--------|-----------|
| Autenticación | `/api/auth` |
| Usuarios | `/api/users` |
| Ejercicios | `/api/exercises` |
| Categorías de ejercicios | `/api/categories` |
| Rutinas | `/api/routines` |
| Ejercicios de rutina | `/api/routine-exercises` |
| Plantillas de series | `/api/set-templates` |
| Parámetros personalizados | `/api/parameters` |
| Deportes | `/api/sports` |
| Suscripciones | `/api/subscriptions` |

---

## Monitorización

El stack incluye Prometheus y Grafana preconfigurados en Docker Compose.

**Prometheus** recoge métricas del endpoint de Spring Boot Actuator:
- Métricas: `http://app:8080/actuator/prometheus`
- Fichero de configuración: `monitoring/prometheus.yml`
- Retención de datos: 15 días

**Grafana** disponible en `http://localhost:3000` (credenciales definidas en `.env`).

Endpoints de Actuator expuestos en producción: `health`, `prometheus`.

---

## Seguridad

- **Autenticación JWT** con par de claves RSA (`src/main/resources/certs/`)
- **Lista negra de tokens** en Redis (para logout e invalidación)
- **Login social con Google OAuth2** y manejador de éxito personalizado
- **Filtro JWT** que valida y rechaza tokens en lista negra en cada petición
- En producción, el contenedor Docker se ejecuta con un usuario sin privilegios (`fitapp`)

---

## Base de datos

- **PostgreSQL 17** gestionado con migraciones **Flyway**
- Scripts de migración: `src/main/resources/db/migration/`
- El esquema nunca se modifica automáticamente en producción (`ddl-auto=validate`)
- Los datos iniciales se cargan al arrancar mediante beans `DataLoader` (ejercicios, categorías, deportes, límites de suscripción, rutinas por defecto)

Para crear una nueva migración:
```
src/main/resources/db/migration/V{N}__{descripcion}.sql
```