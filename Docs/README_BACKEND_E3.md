# USAL E3 Backend
Backend service para el Sistema de Tramitación de Equivalencias Electrónicas. Construido con Spring Boot 3.2, PostgreSQL, y Spring Security. Expone una REST API con autenticación OAuth2 + JWT, integración de auditoría, y gestión de permisos granulares.

**Estado:** Scaffolding completo — service stubs listos para implementación.

---

## Stack

| Layer | Technology |
|-------|------------|
| **Runtime** | Java 17 / Spring Boot 3.2 |
| **Database** | PostgreSQL 15 |
| **Auth** | Spring Security 6 + OAuth2 Google + JWT |
| **API docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build** | Maven |
| **Containerization** | Docker + Docker Compose |
| **DB Migrations** | Flyway |
| **Logging** | SLF4J + Logback |
| **Testing** | JUnit 5 + Mockito |

---

## Comenzar

El único prerequisito es Docker. No necesitas Java, Maven, ni PostgreSQL instalado localmente.

```bash
git clone https://github.com/USAL-Ingenieria/e3-equivalencias-backend.git
cd e3-equivalencias-backend
cp src/main/resources/application.yml.example src/main/resources/application.yml
make up-dev
```

El stack estará disponible en:

| Servicio | URL |
|----------|-----|
| **API REST** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health Check** | http://localhost:8080/actuator/health |
| **pgAdmin** | http://localhost:5050 |

---

## Comandos Make

```makefile
make up-dev        # Inicia stack completo (app + PostgreSQL + pgAdmin)
make down          # Detiene todos los contenedores
make up-infra      # Inicia solo PostgreSQL y pgAdmin (sin app)
make up-prod       # Inicia en modo producción (requiere env vars)

make test          # Ejecuta todos los tests
make test-coverage # Ejecuta tests y abre reporte JaCoCo
make lint          # Ejecuta Checkstyle y SpotBugs
make format        # Verifica formateo (Google Java Format)
make format-fix    # Auto-formatea archivos
make pr-checks     # Ejecuta todos los checks requeridos antes de PR

make build         # Build del JAR
make clean         # Limpia artefactos de build
make logs          # Sigue logs del contenedor backend
make db-migrate    # Ejecuta migraciones Flyway manualmente
make help          # Lista todos los comandos disponibles
```

Todos los comandos `make` que invocan Maven corren dentro de Docker — no necesitas JDK local.

---

## Gitflow

```
feature/* ──► develop ──► beta/* ──► main
                              │
                         hotfix/* ──► main
```

| Branch | Propósito |
|--------|-----------|
| **main** | Releases estables en producción |
| **develop** | Rama de integración — aquí mergean todos los features |
| **beta/*** | Release candidates (ej: beta/1.0) |
| **hotfix/*** | Fixes emergentes que van directo a main |
| **feature/*** | Features nuevos |
| **fix/*** | Bug fixes |
| **refactor/*** | Refactors sin cambio de comportamiento |
| **enhancement/*** | Mejoras a features existentes |

El CI valida nombres de branch. PRs desde ramas que no coincidan con estos prefijos fallarán automáticamente.

---

## Abrir una PR

1. Haz branch desde `develop` usando uno de los prefijos arriba.
2. Ejecuta `make pr-checks` localmente antes de pushear — ejecuta los mismos checks que CI.
3. Abre el PR apuntando a `develop` (nunca directamente a master).
4. Mantén PRs enfocados. Una preocupación por PR.

### Requisitos de PR (Enforced por CI)

| Check | Tool | Requisito |
|-------|------|-----------|
| **Formateo** | Spotless (Google Java Format) | Debe pasar — ejecuta `make format-fix` para auto-fix |
| **Análisis estático** | Checkstyle + SpotBugs | Cero violaciones |
| **Tests** | JUnit 5 + Mockito | Todos los tests deben pasar |
| **Coverage** | JaCoCo | Mínimo 80% cobertura de instrucciones |
| **Arquitectura** | ArchUnit | Services no deben depender de controllers; repositories no deben depender de services o controllers |

Los PRs en Draft son skipeados por CI hasta que se marquen como listos para review.

---

## Estructura del Proyecto

```
src/main/java/com/usal/e3/
├── domain/              # Entities — clases Java anotadas con @Entity
├── repository/          # Spring Data JPA + queries personalizadas
├── service/             # Lógica de negocio — @Service
├── api/                 # Controllers, DTOs (records), handlers
│   ├── auth/
│   ├── solicitudes/
│   ├── usuarios/
│   ├── carreras/
│   ├── chat/
│   └── admin/
├── security/            # JWT, OAuth2, Security Utils
├── exception/           # Excepciones personalizadas
├── mapper/              # MapStruct para Entity ↔ DTO
└── config/              # Configuración Spring (Security, JPA, OpenAPI)

src/main/resources/
├── application.yml                  # Config default
├── application-dev.yml              # Overrides para desarrollo
├── application-prod.yml             # Overrides para producción
├── application-docker.yml           # Config Docker (auto)
├── db/migration/                    # Scripts Flyway
│   ├── V1__initial.sql
│   ├── V2__add_roles.sql
│   └── V3__add_permissions.sql
└── logback-spring.xml              # Config logging

src/test/java/com/usal/e3/
├── service/             # Tests unitarios de servicios
├── repository/          # Tests de acceso a datos
└── integration/         # Tests de integración
```

Las dependencias fluyen en una dirección: `api → service → repository → domain`. Los tests de ArchUnit refuerzan esto en tiempo de build.

---

## Reglas de Arquitectura

- Controllers llaman services solamente — nunca repositories directamente.
- Services llaman repositories solamente — nunca controllers.
- Domain classes son Java puro — solo anotaciones `@Entity` e `@Id`.
- DTOs son Java Records y viven al lado del controller en `api/<dominio>/`.
- Todos los métodos de service no implementados lanzan `UnsupportedOperationException` — reemplazalo con lógica real cuando implementes.
- La validación de permisos (`revoked_at IS NULL`) vive en `PermisosService` — **NUNCA** en controllers.

---

## Persistencia de Datos

PostgreSQL y Flyway persisten en `data/` en la raíz del proyecto (bind-mounted en Docker). Este directorio está en `.gitignore`. Bórralo para resetear estado local.

---

## Datos Demo

Para poblar una BD fresca con un conjunto de datos demo completo e interconectado — usuarios en cada rol, catálogo de carreras, solicitudes en cada estado, y auditoría — setea `SEED_DEMO=true` antes del primer boot:

```bash
SEED_DEMO=true make up-dev
```

Se ejecuta una sola vez al iniciar e es idempotente: si los datos ya existen hace nada, así que es seguro entre restarts (desactivado por defecto). Para re-seedear, resetea la BD primero (`make down` y borra `data/`, o dropea la BD Postgres) y bootea de nuevo.

Credenciales de login para cuentas seededeadas están en `docs/demo-credentials.md` (contraseña compartida: `Demo1234!`).

---

## Configuración de Ambiente

Copia `src/main/resources/application.yml.example` a `application.yml` y ajusta según sea necesario. Este archivo está en `.gitignore` — **nunca** commitees secrets.

Cuando corres vía Docker Compose, las connection strings se setean automáticamente vía el Spring profile `docker` (`application-docker.yml`). No necesitas configuración manual para `make up-dev`.

### Variables de Ambiente Requeridas (Producción)

```bash
# Database
DB_URL=jdbc:postgresql://HOST:PORT/DATABASE
DB_USERNAME=usuario
DB_PASSWORD=contraseña

# JWT
JWT_SECRET=tu-llave-super-larga-y-secreta-aqui

# Google OAuth
GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret

# Mail
MAIL_HOST=smtp.tuproveedor.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@usal.edu.ar
MAIL_PASSWORD=tu-contraseña

# Seed Demo
SEED_DEMO=false  # Desactivado en producción
```
