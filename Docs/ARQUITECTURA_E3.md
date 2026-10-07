# USAL E3 - Arquitectura

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.1 | 2026-09-29 | lautarogomizelj | Alineación con el código real: `fetch` nativo, React 19 y Design Tokens |
| 1.0 | 2026-09-28 | lautarogomizelj | Versión inicial: estructura del monorepo, flujo de comunicación y capas |

> El stack, los puertos y las convenciones de git están en el [README de la raíz](../README.md). Este documento se limita a la estructura del monorepo, el flujo de comunicación y las capas.

## Estructura del Monorepo

Este es el árbol real del repositorio hoy. Lo que todavía no existe está en [Estructura prevista](#estructura-prevista), más abajo.

```
usal-equivalencias-e3/
├── AGENTS.md                       # Pautas de trabajo para herramientas de asistencia
├── Makefile                        # Orquestación centralizada
├── README.md                       # Documento canónico: estado, stack, puertos, git
├── .gitignore
│
├── backend/                        # Java + Spring Boot 3.2 (a implementar)
│   ├── src/main/java/com/usal/e3/
│   │   ├── api/                    # Controllers REST
│   │   │   ├── auth/
│   │   │   ├── solicitudes/
│   │   │   ├── usuarios/
│   │   │   ├── carreras/
│   │   │   ├── chat/
│   │   │   └── admin/
│   │   ├── service/                # Lógica de negocio
│   │   ├── repository/             # Acceso a datos (JPA)
│   │   ├── domain/                 # Entidades JPA (modelos de dominio)
│   │   ├── mapper/                 # MapStruct: Entity ↔ DTO
│   │   ├── security/               # JWT, OAuth2, permisos
│   │   ├── exception/              # Excepciones personalizadas
│   │   └── config/                 # Configuración Spring
│   ├── src/main/resources/
│   │   └── db/migration/           # Scripts Flyway
│   └── src/test/java/com/usal/e3/  # Tests (unitarios, repos, integración)
│
├── frontend/                       # React + Vite
│   ├── public/
│   ├── src/
│   │   ├── assets/                 # Imágenes y archivos estáticos
│   │   ├── components/
│   │   │   └── ui/                 # Componentes base compartidos (Button, Input, Modal, Table...)
│   │   ├── constants/              # Rutas, roles, límites de archivos, reglas de auth, env
│   │   ├── hooks/                  # Hooks reutilizables
│   │   ├── layouts/                # Layouts compartidos (público, dashboard, detalle...)
│   │   ├── pages/                  # Una carpeta por área/rol (public, aspirante, academico...)
│   │   ├── router/                 # Definición de rutas
│   │   ├── services/               # Comunicación con backend (api/client.ts es el único que usa fetch)
│   │   ├── store/                  # Estado global (Context API)
│   │   ├── styles/                 # Estilos globales
│   │   ├── tokens/                 # Design Tokens (colores, espaciado, radios, sombras, tipografía...)
│   │   ├── types/                  # Tipos y DTOs compartidos
│   │   ├── utils/                  # Utilidades puras
│   │   ├── validations/            # Validaciones y mensajes de error compartidos
│   │   └── main.tsx                # Entry point
│   ├── .env.example                # URL del backend
│   ├── .oxlintrc.json
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
└── Docs/
    ├── ARQUITECTURA_E3.md          # Este archivo
    ├── README_BACKEND_E3.md        # Guía del equipo Backend (todo a implementar)
    ├── README_FRONTEND_E3.md       # Guía del equipo Frontend
    ├── frontend-interfaces.md      # Especificación de interfaces
    ├── Sistema_Equivalencias_Requisitos_v6.md
    └── Diagramas/                  # DiagramaActividades.jpeg, LogicaPrincipal_*.jpeg
```

### Estructura prevista

Estos archivos todavía no existen. Van a aparecer cuando se implemente el backend y el stack de contenedores:

| Ruta | Qué es |
|------|--------|
| `backend/pom.xml` | Build de Maven |
| `backend/Dockerfile` | Imagen del servicio Spring Boot |
| `backend/src/main/resources/application*.yml` | Configuración por ambiente y perfil `docker` |
| `backend/src/main/resources/logback-spring.xml` | Configuración de logging |
| `backend/src/main/resources/db/migration/V*.sql` | Migraciones Flyway |
| `docker-compose.yml` | Stack de contenedores (backend + PostgreSQL + pgAdmin) |
| `Docs/API.md` | Contrato de la API |
| `db/schema.md` | Diagrama de tablas |
| `Docs/demo-credentials.md` | Credenciales de las cuentas demo |

---

## Puertos

Ver la tabla de puertos en el [README de la raíz](../README.md), sección **Puertos**. El backend expone su API bajo el context path `/api`.

---

## Flujo de Comunicación

### 1️⃣ Frontend Inicia en Desarrollo

```
Frontend Developer
        ↓
make dev
        ↓
┌────────────────────────────────┐
│  Vite Dev Server               │
│  http://localhost:5173         │
│                                │
│  React App cargado             │
│  (HMR: hot module reload)      │
└────────────────────────────────┘
        ↓
Cliente (navegador)
http://localhost:5173
```

### 2️⃣ Backend Inicia en Desarrollo (a implementar)

```
Backend Developer
        ↓
make backend-up-dev
        ↓
┌────────────────────────────────┐
│  Spring Boot Application       │
│  http://localhost:8080         │
│  /api (context path)           │
│                                │
│  - OAuth Google setup          │
│  - JWT ready                   │
│  - PostgreSQL conectado        │
│  - Swagger UI en /swagger-ui   │
└────────────────────────────────┘
        ↓
REST API ready
http://localhost:8080/api
```

### 3️⃣ Usuario Abre App en Navegador

```
Usuario
  ↓
Abre http://localhost:5173
  ↓
┌─────────────────────────────────────────┐
│  NAVEGADOR (Cliente)                    │
│                                         │
│  React App                              │
│  ├─ Componentes                         │
│  ├─ Router (páginas)                    │
│  ├─ Context (estado global)             │
│  └─ fetch (services/api/client.ts)      │
└─────────────────────────────────────────┘
```

---

## Interacción Frontend ↔ Backend

### Ejemplo: Usuario hace Login con Google

> **Diseño tentativa.** La autenticación no está implementada y todavía no está decidida. El diagrama de abajo es uno de los flujos posibles —OAuth 2.0 con Google y JWT—, pero el cliente HTTP que existe hoy (`frontend/src/services/api/client.ts`) va con `credentials: 'include'`, es decir cookie de sesión, y no guarda el token en `localStorage`. Además, según la especificación funcional, no todos los roles usan OAuth: Aspirante y Admin General entram con email y contraseña. Cuando se implemente la auth hay que volver a este diagrama con el flujo real.

```
NAVEGADOR (Frontend)                          SERVIDOR (Backend)
    │                                             │
    │ 1. Click "Ingresar con Google"            │
    ├──────────────────────────────────────────>│ (Redirección a Google)
    │                                             │
    │ 2. Usuario se autentica en Google         │
    │ 3. Google retorna código                  │
    │                                             │
    │ 4. POST /api/auth/login                   │
    │    { code: "..." }                         │
    ├──────────────────────────────────────────>│
    │                                             │
    │                                    ┌──────┴──────┐
    │                                    │ Spring      │
    │                                    │ valida      │
    │                                    │ código con  │
    │                                    │ Google      │
    │                                    │ verifica    │
    │                                    │ email en BD │
    │                                    │ genera JWT  │
    │                                    └──────┬──────┘
    │                                             │
    │ 5. HTTP 200 + JWT                        │
    │    { token, usuario }                     │
    │<──────────────────────────────────────────┤
    │                                             │
    │ 6. LocalStorage.setItem("token", JWT)    │
    │                                             │
    │ 7. GET /api/solicitudes                   │
    │    Header: Authorization: Bearer {JWT}    │
    ├──────────────────────────────────────────>│
    │                                             │
    │                                    ┌──────┴──────┐
    │                                    │ Spring      │
    │                                    │ valida JWT  │
    │                                    │ verifica    │
    │                                    │ permisos    │
    │                                    │ consulta BD │
    │                                    └──────┬──────┘
    │                                             │
    │ 8. HTTP 200 + JSON                       │
    │    [ {solicitud}, {solicitud}, ... ]     │
    │<──────────────────────────────────────────┤
    │                                             │
    │ 9. React renderiza lista                  │
    │ 10. Usuario ve solicitudes                │
    │                                             │
```

---

## Capas (3-Tier Architecture)

### Capa 1: FRONTEND (Presentación)

**Ubicación:** `frontend/src/`

**Responsabilidades:**
- Renderizar interfaz (React)
- Capturar entrada del usuario
- Validar datos (antes de enviar)
- Hacer requests HTTP al backend
- Mostrar errores y mensajes

**NO hace:**
- Lógica de negocio
- Acceso directo a BD
- Validaciones complejas

**Stack:**
- React 19
- Vite 8
- TypeScript 6
- `fetch` nativo (cliente HTTP)
- Context API (estado global)
- Tailwind CSS v4

#### Estructura de Carpetas

| Carpeta | Contenido | Ejemplos |
|---------|-----------|----------|
| **assets/** | Imágenes, SVGs, fuentes estáticas | logos, iconos, fotos |
| **components/ui/** | Componentes base reutilizables | Button, Input, Modal, Table, Card |
| **constants/** | Constantes de aplicación | rutas, roles (ACADEMICO, ADMIN, ...), límites de archivo, reglas auth, env |
| **hooks/** | Hooks React reutilizables | useAuth, usePermiso, useFormData |
| **layouts/** | Layouts compartidos por sección | PublicLayout, LayoutDashboard, LayoutDetalle |
| **pages/** | Páginas por área/rol | pages/public/, pages/aspirante/, pages/academico/, pages/admin/ |
| **router/** | Definición de rutas | rutas públicas, protegidas por rol |
| **services/** | Cliente HTTP | api/client.ts (ÚNICO que hace fetch), ApiError.ts, servicios por dominio |
| **store/** | Estado global con Context | AuthContext, SolicitudesContext, PermsContext |
| **styles/** | CSS global | reset, tipografía global |
| **tokens/** | Design System | colores, espaciado, radios, sombras, tamaños, breakpoints, z-index |
| **types/** | TypeScript types | DTOs, interfaces compartidas |
| **utils/** | Funciones puras | formatters, parsers, helpers |
| **validations/** | Validadores y mensajes de error | requerido(), email(), dni(), archivo() |

#### Cómo habla con Backend

- **Único punto de comunicación:** `services/api/client.ts`, que expone `api.get/post/put/patch/delete` sobre `fetch` nativo.
- **Errores tipados:** las respuestas que no son `ok` se convierten en `ApiError` con un `kind` (`unauthorized`, `forbidden`, `network`...).
- **Servicios por dominio:** `services/solicitudes.ts`, `services/usuarios.ts`, etc.
- **Sin fetch directo:** nunca llamar a `fetch` desde componentes o utils.
- **Pendiente de definir:** cómo viaja la sesión (cookie `HttpOnly` o header `Authorization: Bearer`). El cliente actual usa `credentials: 'include'`.