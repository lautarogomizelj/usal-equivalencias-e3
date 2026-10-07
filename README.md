# E3 · Equivalencias USAL

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-09-28 | lautarogomizelj | README canónico: estado actual, stack y cómo correr el proyecto |
| 1.1 | 2026-09-29 | lautarogomizelj | Stack en tablas, tabla de puertos y convenciones de git unificadas |
| 1.2 | 2026-10-07 | lautarogomizelj | Corrección de rutas de documentos y alta de Gestión de Cambios |
| 1.3 | 2026-10-07 | lautarogomizelj | Unificación de las guías de Frontend y Backend en este documento |

Sistema web de tramitación de equivalencias académicas. Digitaliza y automatiza el proceso de reconocimiento de materias para ingresantes de la Facultad de Ingeniería de la Universidad Nacional de San Luis.

---

## Qué problema resuelve

Hoy el proceso de reconocimiento de equivalencias depende por completo del correo electrónico y de Google Drive. Nadie sabe en qué etapa está cada solicitud, el avance depende de que una sola persona recuerde cuándo enviar cada mail, y tanto los aspirantes como el personal administrativo no tienen forma de consultar el estado de su trámite. Tampoco existe un registro claro de quién hizo qué y cuándo.

Este sistema reemplaza ese circuito por una plataforma web donde cada rol tiene su propio panel, el trámite avanza mediante transiciones de estado explícitas, cada cambio de estado genera una notificación automática, y queda una traza auditable de cada paso con usuario, fecha e IP de origen. Los responsables se asignan de forma dinámica: nadie queda asignado por defecto, cada uno toma el proceso que puede tomar.

---

## Estado actual

**El proyecto está en etapa de scaffolding. No hay nada desplegado ni en ejecución.**

El backend es un esqueleto vacío. Existen las carpetas del package de Java con la separación por capas prevista, pero todas contienen únicamente un archivo `.gitkeep`. No hay `pom.xml`, ni Dockerfile, ni migraciones de Flyway, ni una sola clase de Java. Tampoco existe la orquestación con Docker Compose que la documentación describe.

El frontend sí es una base funcional: tiene el sistema de Design Tokens completo, el cliente HTTP centralizado con manejo tipado de errores, el router, el layout público, la página de inicio, la página de error 404 y un componente base de botón. El andamiaje de rutas, roles y tipos ya contempla los seis roles del sistema —los cinco del catálogo (`ASPIRANTE`, `ACADEMICO`, `SECRETARIA`, `REVISOR`, `ADMIN`) más `OBSERVADOR`, que no es un rol del catálogo sino el estado de un usuario sin permisos en una carrera—, pero las pantallas de negocio todavía no existen.

En resumen: la base técnica y la especificación están sólidas, la implementación de negocio está sin empezar.

---

## Stack

### Frontend

| Capa | Tecnología |
|-------|------------|
| **Runtime** | React 19 + React DOM 19 |
| **Lenguaje** | TypeScript 6 |
| **Build / Dev server** | Vite 8 |
| **Estilos** | Tailwind CSS v4 + Design Tokens del proyecto |
| **Navegación** | React Router 8 |
| **Íconos** | `lucide-react` (única librería de íconos) |
| **Estado global** | Context API de React, sin librería externa |
| **Linter** | `oxlint` |
| **Cliente HTTP** | `fetch` nativo, centralizado en `src/services/api/client.ts` |
| **Entorno** | Node 24 + npm · alias `@` para apuntar a `src` |

### Backend (a implementar)

| Capa | Tecnología |
|-------|------------|
| **Runtime** | Java 17 / Spring Boot 3.2 |
| **Base de datos** | PostgreSQL 15 |
| **Autenticación** | Spring Security 6 + OAuth2 Google + JWT |
| **Documentación de la API** | SpringDoc OpenAPI (Swagger UI) |
| **Migraciones** | Flyway |
| **Build** | Maven |
| **Logging** | SLF4J + Logback |
| **Testing** | JUnit 5 + Mockito |

### Infraestructura (a implementar)

| Capa | Tecnología |
|-------|------------|
| **Contenedores** | Docker |
| **Orquestación** | Docker Compose |
| **Servicios del stack** | Aplicación backend, PostgreSQL y pgAdmin |

### Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| **Frontend (React)** | 5173 | http://localhost:5173 |
| **Backend (Spring)** | 8080 | http://localhost:8080/api |
| **PostgreSQL** | 5432 | localhost:5432 |
| **pgAdmin** | 5050 | http://localhost:5050 |

El backend expone su API bajo el context path `/api`, que es lo que declara `VITE_API_URL` en el `.env` del frontend.

Requisito de entorno: en la máquina de desarrollo actual no hay JDK ni Maven instalados, así que todo lo que involucre Java tendrá que correr dentro de Docker.

---

## Convenciones de git

```
feature/* ──► develop ──► main
fix/*     ──► develop ──► main
                  ▲
                  └── fix/* urgente: merge directo a main
```

| Rama | Propósito |
|------|-----------|
| **main** | Releases estables en producción |
| **develop** | Rama de integración — aquí mergean todos los `feature/*` y `fix/*` |
| **feature/*** | Features nuevos |
| **fix/*** | Bug fixes |

Los mensajes de commit llevan un prefijo que indica el tipo de cambio:

| Prefijo | Uso |
|---------|-----|
| **ADDED:** | Nueva funcionalidad |
| **FIX:** | Corrección de bug |
| **HOTFIX:** | Corrección urgente aplicada directo en producción (un `fix/*` mergeado directo a `main`) |
| **REFACTOR:** | Reorganización sin cambio de comportamiento (mover, renombrar, limpiar) |

`backend/` y `frontend/` son independientes entre sí, así que conviene que cada commit toque una sola de las dos, y que los cambios de documentación vengan en commits separados de los de código.

---

## Cómo correr el proyecto

Hace falta Node 24 y npm. El punto de entrada es el Makefile de la raíz, que envuelve los scripts de npm. Solo hay tareas de frontend: el backend todavía no existe. Cuando se implemente, sus tareas van a llevar el prefijo `backend-` para no chocar con las del frontend.

### Comandos del Makefile

| Comando | Qué hace |
|---------|----------|
| `make help` | Lista las tareas disponibles. Es la tarea por defecto, así que correr `make` a secas muestra la ayuda. |
| `make install` | Instala las dependencias con `npm ci`, que respeta el lockfile y por lo tanto es reproducible. |
| `make dev` | Levanta el servidor de desarrollo con recarga en caliente. |
| `make build` | Compila TypeScript y genera el build de producción. |
| `make lint` | Corre oxlint. |
| `make preview` | Sirve localmente el build de producción. |
| `make clean` | Borra el directorio de build. Es el único target que no envuelve un script de npm. |
| `make down` | Detiene el stack de contenedores. Hoy no hace nada porque todavía no hay `docker-compose.yml`: avisa por pantalla en lugar de fallar, y pasa a hacer el teardown real de Docker Compose en cuanto ese archivo exista. |

Para levantar el entorno de desarrollo:

```bash
cd frontend
cp .env.example .env
cd ..
make install
make dev          # http://localhost:5173
```

El `.env` se crea una sola vez y es donde se define la URL del backend. Las variables de entorno de Vite solo se leen al arrancar el servidor, así que si modificás el `.env` tenés que reiniciar el dev server para que el cambio tome efecto.

### Correr solo el frontend

Para trabajar sin make, los mismos pasos desde la carpeta `frontend`:

```bash
cp .env.example .env   # URL del backend
npm install            # o npm ci, si respetás el lockfile
npm run dev            # http://localhost:5173
```

Otros scripts de `package.json`: `npm run build`, `npm run preview`, `npm run lint`.

---

## Guía de Frontend

Pantallas y reglas: [`Docs/frontend-interfaces.md`](Docs/frontend-interfaces.md) · Requisitos: [`Docs/Sistema_Equivalencias_Requisitos.md`](Docs/Sistema_Equivalencias_Requisitos.md). La estructura de carpetas está en [Documentación](#documentación).

### Reglas rápidas

- **Nada hardcodeado:** colores, espaciados, radios, sombras y tamaños salen de `src/tokens/`.
  Usar clases como `bg-primary`, `text-danger`, `rounded-md`, `h-(--size-input)`, `z-(--z-modal)`.
  Nada de valores arbitrarios (`p-[13px]`, `bg-[#ff0000]`).
- **Escala de espaciado:** solo `1, 2, 3, 4, 6, 8, 12, 16` (= 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64 px).
- **Sin estilos inline** (`style={{...}}`).
- **Sin `fetch` en componentes:** crear un servicio en `src/services/` que use `api` de `services/api/client.ts`.
  Los errores llegan como `ApiError` con `kind` (`unauthorized`, `forbidden`, `network`...).
- **Estados por código, nunca por nombre** (`estado.codigo === 'DOC-CAR'`); el catálogo viene del backend.
- **Validaciones** en `src/validations/`; el backend sigue siendo la fuente de verdad.
- **Acciones críticas** con doble confirmación (§12 de `frontend-interfaces.md`).
- Cada pantalla contempla estados de carga, vacío, error y éxito (§0.5).

### Pendiente

- Los colores de `tokens/colors.css` son **provisorios** hasta tener el Design System definitivo.
- Falta definir con backend el contrato de la API (rutas, DTOs y formato de errores en `types/api.ts`).

---

## Guía de Backend

Todas las secciones de esta guía están marcadas "(a implementar)": el servicio todavía no existe y describen el diseño previsto, no el estado actual.

### Cómo arrancar el servicio (a implementar)

El único prerequisito va a ser Docker. No vas a necesitar Java, Maven, ni PostgreSQL instalados localmente.

```bash
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

### Comandos Make (a implementar)

Los targets del backend llevan el prefijo `backend-` para no chocar con los del frontend (`make dev`, `make build`, `make lint`, `make clean` y `make down`).

```makefile
make backend-up-dev      # Inicia stack completo (app + PostgreSQL + pgAdmin)
make backend-up-infra    # Inicia solo PostgreSQL y pgAdmin (sin app)
make backend-up-prod     # En modo producción (requiere env vars)
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

### Abrir una PR (a implementar)

1. Creá la branch desde `develop` con prefijo `feature/` o `fix/`.
2. Ejecutá `make backend-pr-checks` localmente antes de pushear — ejecuta los mismos checks que CI.
3. Abrí el PR apuntando a `develop` (nunca directamente a `main`).
4. Mantené PRs enfocados. Una preocupación por PR.

#### Requisitos de PR (a implementar)

El backend todavía no tiene CI. Estos son los checks que se van a exigir cuando exista, y quedan subjects de definición:

| Check | Herramienta | Requisito |
|-------|-------------|-----------|
| **Formateo** | Spotless (Google Java Format) | Debe pasar — ejecuta `make backend-format-fix` para auto-fix |
| **Análisis estático** | Checkstyle + SpotBugs | Cero violaciones |
| **Tests** | JUnit 5 + Mockito | Todos los tests deben pasar |
| **Coverage** | JaCoCo | Mínimo 80% cobertura de instrucciones |
| **Arquitectura** | ArchUnit | Services no deben depender de controllers; repositories no deben depender de services o controllers |

Los PRs en Draft van a ser skipeados por CI hasta que se marquen como listos para review.

### Reglas de Arquitectura (a implementar)

- Controllers llaman services solamente — nunca repositories directamente.
- Services llaman repositories solamente — nunca controllers.
- Domain classes son Java puro — solo anotaciones `@Entity` e `@Id`.
- DTOs son Java Records y viven al lado del controller en `api/<dominio>/`.
- La validación de permisos (`revoked_at IS NULL`) vive en `PermisosService` — **NUNCA** en controllers.

### Persistencia de Datos (a implementar)

PostgreSQL y Flyway van a persistir en `data/` en la raíz del monorepo (bind-mounted en Docker). Ese directorio todavía no está en el `.gitignore`: se agrega junto con el `docker-compose.yml`. Se borra para resetear el estado local.

### Datos Demo (a implementar)

Para poblar una BD fresca con un conjunto de datos demo completo e interconectado — usuarios en cada rol, catálogo de carreras, solicitudes en cada estado, y auditoría — se va a setear `SEED_DEMO=true` antes del primer boot:

```bash
SEED_DEMO=true make backend-up-dev
```

Se ejecuta una sola vez al iniciar y es idempotente: si los datos ya existen hace nada, así que es seguro entre restarts (desactivado por defecto). Para re-seedear, se resetea la BD primero (`make down` y borrar `data/`, o dropear la BD Postgres) y bootear de nuevo.

Las credenciales de las cuentas demo van a estar en `Docs/demo-credentials.md` (contraseña compartida: `Demo1234!`). Ese archivo todavía no existe.

### Configuración de Ambiente (a implementar)

Se va a copiar `backend/src/main/resources/application.yml.example` a `application.yml` y ajustar según sea necesario. Ese archivo va a estar en `.gitignore` — **nunca** commitear secrets.

Cuando se corra vía Docker Compose, las connection strings se van a setear automáticamente vía el Spring profile `docker` (`application-docker.yml`). No debería hacer falta configuración manual para `make backend-up-dev`.

#### Variables de Ambiente Requeridas (Producción)

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

---

## Documentación

El repositorio es un monorepo con dos aplicaciones y un directorio de documentación.

Este README es la fuente canónica del estado actual, el stack, los puertos y las convenciones de git. Los demás documentos se vinculan a esta página en lugar de repetir esa información, para que no haya dos versiones que puedan divergir.

**`backend/`** — Servicio Spring Boot (a implementar):

```text
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

**`frontend/`** — Aplicación React:

```text
src/
├── assets/        imágenes y archivos estáticos
├── components/
│   └── ui/        componentes base compartidos (Button, Input, Modal, Table...)
├── constants/     rutas, roles, límites de archivos, reglas de auth, env
├── hooks/         hooks reutilizables
├── layouts/       layouts compartidos (público, dashboard, detalle...)
├── pages/         una carpeta por área/rol (public, aspirante, academico...)
├── router/        definición de rutas
├── services/      comunicación con el backend (api/client.ts es el único que usa fetch)
├── store/         estado global (Context API)
├── styles/        estilos globales
├── tokens/        Design Tokens (colores, espaciado, radios, sombras, tipografía...)
├── types/         tipos y DTOs compartidos
├── utils/         utilidades puras
└── validations/   validaciones y mensajes de error compartidos
```

Importar con el alias `@/` (ej: `import { Button } from '@/components/ui'`).

### Documentos del proyecto

| Documento | Qué contiene |
|-----------|--------------|
| `Docs/Sistema_Equivalencias_Requisitos.md` | Especificación funcional y no funcional. Es la fuente de verdad del negocio: actores, módulos, estados del expediente, esquema de tablas y reglas de permisos. Cuando haya una duda sobre qué tiene que hacer el sistema, la respuesta está acá. |
| `Docs/frontend-interfaces.md` | Especificación de interfaces: cada pantalla, campo, estado de carga y acción por rol, más los estándares obligatorios de Frontend (tokens, accesibilidad, responsive, validaciones). Es el contrato entre el negocio y la capa de presentación, y es el documento que manda cuando hay que decidir cómo mostrar algo. |
| `Docs/ARQUITECTURA_E3.md` | Estructura del monorepo, flujo de comunicación entre Frontend y Backend y las tres capas. Los puertos y el stack están en este README, no se repiten ahí. |
| `Docs/GESTION_CAMBIOS.md` | Registro de cambios de requisito y de diseño que surgen durante el desarrollo. Cada cambio aprobado se refleja en el documento fuente y en su tabla de control de versiones. |
| `Docs/Diagramas/` (3 archivos `.jpeg`) | Diagramas de actividades y de los flujos de análisis preliminar y revisión legal. |
| `AGENTS.md` | Pautas de trabajo para las herramientas de asistencia: cómo encarar un cambio, reglas de UI y prefijos de commit. |
