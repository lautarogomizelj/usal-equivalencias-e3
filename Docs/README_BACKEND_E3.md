# USAL E3 Backend
Backend service para el Sistema de Tramitación de Equivalencias Electrónicas. Construido con Spring Boot 3.2, PostgreSQL, y Spring Security. Expone una REST API con autenticación OAuth2 + JWT, integración de auditoría, y gestión de permisos granulares.

**Estado: a implementar.** El servicio todavía no existe. Lo único que hay en `backend/` es la estructura de carpetas del package `com.usal.e3` con la separación por capas prevista, y cada carpeta contiene únicamente un `.gitkeep`. Este documento describe el diseño previsto, no el estado actual.

---

## Stack (a implementar)

El stack tecnológico completo y las versiones canónicas están en el [README de la raíz](../README.md), sección **Stack**. No se repiten acá para evitar que las dos copias diverjan.

---

## Cómo arrancar el servicio (a implementar)

El único prerequisito va a ser Docker. No vas a necesitar Java, Maven, ni PostgreSQL instalados localmente.

```bash
git clone git@github.com:lautarogomizelj/usal-equivalencias-e3.git
cd usal-equivalencias-e3
cp backend/src/main/resources/application.yml.example backend/src/main/resources/application.yml
make backend-up-dev
```

El stack va a estar disponible en:

| Servicio | URL |
|----------|-----|
| **API REST** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health Check** | http://localhost:8080/actuator/health |
| **pgAdmin** | http://localhost:5050 |

---

## Comandos Make (a implementar)

Los targets del backend llevan el prefijo `backend-` para no chocar con los del frontend, que son `make dev`, `make build`, `make lint`, `make clean` y `make down`. Los que existen hoy están en el [README de la raíz](../README.md), sección **Cómo correr el proyecto**.

```makefile
make backend-up-dev      # Inicia stack completo (app + PostgreSQL + pgAdmin)
make backend-up-infra    # Inicia solo PostgreSQL y pgAdmin (sin app)
make backend-up-prod     # Inicia en modo producción (requiere env vars)
make down                # Detiene todos los contenedores (compartido con el frontend)

make backend-test          # Ejecuta todos los tests
make backend-test-coverage # Ejecuta tests y abre reporte JaCoCo
make backend-lint          # Ejecuta Checkstyle y SpotBugs
make backend-format        # Verifica formateo (Google Java Format)
make backend-format-fix    # Auto-formatea archivos
make backend-pr-checks     # Ejecuta todos los checks requeridos antes de PR

make backend-build      # Build del JAR
make backend-clean      # Limpia artefactos de build
make backend-logs       # Sigue logs del contenedor backend
make backend-db-migrate # Ejecuta migraciones Flyway manualmente
```

Todos los comandos `make` que invoquen Maven van a correr dentro de Docker — no necesitás JDK local.

---

## Convenciones de git

Las ramas, el flujo de trabajo y los prefijos de commit están definidos en el [README de la raíz](../README.md), sección **Convenciones de git**. Aplica igual para `backend/`: ramas `feature/*` y `fix/*` mergeadas a `develop`, releases a `main`.

---

## Abrir una PR (a implementar)

1. Creá la branch desde `develop` con prefijo `feature/` o `fix/`.
2. Ejecutá `make backend-pr-checks` localmente antes de pushear — ejecuta los mismos checks que CI.
3. Abrí el PR apuntando a `develop` (nunca directamente a `main`).
4. Mantené PRs enfocados. Una preocupación por PR.

### Requisitos de PR (a implementar)

El backend todavía no tiene CI. Estos son los checks que se van a exigir cuando exista, y quedan subjects de definición:

| Check | Herramienta | Requisito |
|-------|-------------|-----------|
| **Formateo** | Spotless (Google Java Format) | Debe pasar — ejecuta `make backend-format-fix` para auto-fix |
| **Análisis estático** | Checkstyle + SpotBugs | Cero violaciones |
| **Tests** | JUnit 5 + Mockito | Todos los tests deben pasar |
| **Coverage** | JaCoCo | Mínimo 80% cobertura de instrucciones |
| **Arquitectura** | ArchUnit | Services no deben depender de controllers; repositories no deben depender de services o controllers |

Los PRs en Draft van a ser skipeados por CI hasta que se marquen como listos para review.

---

## Estructura del Proyecto (a implementar)

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

Las carpetas `api`, `domain`, `repository`, `service`, `mapper`, `security`, `exception` y `config` ya están creadas en el repo. `resources/db/migration/` y los tres niveles de prueba también, todos vacíos.

Las dependencias fluyen en una dirección: `api → service → repository → domain`. Los tests de ArchUnit refuerzan esto en tiempo de build.

---

## Reglas de Arquitectura (a implementar)

- Controllers llaman services solamente — nunca repositories directamente.
- Services llaman repositories solamente — nunca controllers.
- Domain classes son Java puro — solo anotaciones `@Entity` e `@Id`.
- DTOs son Java Records y viven al lado del controller en `api/<dominio>/`.
- La validación de permisos (`revoked_at IS NULL`) vive en `PermisosService` — **NUNCA** en controllers.

---

## Persistencia de Datos (a implementar)

PostgreSQL y Flyway van a persistir en `data/` en la raíz del monorepo (bind-mounted en Docker). Ese directorio todavía no está en el `.gitignore`: se agrega junto con el `docker-compose.yml`. Se borra para resetear el estado local.

---

## Datos Demo (a implementar)

Para poblar una BD fresca con un conjunto de datos demo completo e interconectado — usuarios en cada rol, catálogo de carreras, solicitudes en cada estado, y auditoría — se va a setear `SEED_DEMO=true` antes del primer boot:

```bash
SEED_DEMO=true make backend-up-dev
```

Se ejecuta una sola vez al iniciar y es idempotente: si los datos ya existen hace nada, así que es seguro entre restarts (desactivado por defecto). Para re-seedear, se resetea la BD primero (`make down` y borrar `data/`, o dropear la BD Postgres) y bootear de nuevo.

Las credenciales de las cuentas demo van a estar en `Docs/demo-credentials.md` (contraseña compartida: `Demo1234!`). Ese archivo todavía no existe.

---

## Configuración de Ambiente (a implementar)

Se va a copiar `backend/src/main/resources/application.yml.example` a `application.yml` y ajustar según sea necesario. Ese archivo va a estar en `.gitignore` — **nunca** commitear secrets.

Cuando se corra vía Docker Compose, las connection strings se van a setear automáticamente vía el Spring profile `docker` (`application-docker.yml`). No debería hacer falta configuración manual para `make backend-up-dev`.

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
