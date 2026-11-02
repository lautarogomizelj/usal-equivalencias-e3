# USAL E3 - Arquitectura

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-09-28 | lautarogomizelj | Versión inicial: estructura del monorepo, flujo de comunicación y capas |
| 1.1 | 2026-09-29 | lautarogomizelj | Alineación con el código real: `fetch` nativo, React 19 y Design Tokens |
| 1.2 | 2026-10-14 | jumaluc | Alta de `Docs/API.md` (contrato de la API) en el árbol del monorepo |
| 1.3 | 2026-10-31 | jumaluc | Corrige el nombre del contrato de la API |
| 1.4 | 2026-11-02 | jumaluc | Backend de los sprints 1 y 2: árbol real, flujo de autenticación y sesión (CC-007) |

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
├── docker-compose.yml              # Backend, PostgreSQL, pgAdmin, Mailpit y tests
├── backend/                        # Java + Spring Boot 3.2
│   ├── pom.xml
│   ├── Dockerfile
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
│   │   ├── security/               # JWT, cookie de sesión, OAuth2
│   │   ├── exception/              # Excepciones personalizadas
│   │   └── config/                 # Configuración Spring
│   ├── src/main/resources/
│   │   ├── application*.yml        # Configuración por ambiente y perfil `docker`
│   │   ├── logback-spring.xml      # Configuración de logging
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
    ├── Contratos API REST.md       # Contrato de la API REST (a acordar con backend)
    ├── ARQUITECTURA_E3.md          # Este archivo
    ├── GESTION_CAMBIOS.md          # Registro de cambios de requisitos y diseño
    ├── frontend-interfaces.md      # Especificación de interfaces
    ├── demo-credentials.md         # Cuentas demo por rol
    ├── Sistema_Equivalencias_Requisitos.md
    └── Diagramas/                  # DiagramaActividades.jpeg, LogicaPrincipal_*.jpeg
```

### Estructura prevista

Estos archivos todavía no existen:

| Ruta | Qué es |
|------|--------|
| `backend/src/main/java/com/usal/e3/mapper/` | MapStruct: Entity ↔ DTO. Hoy los DTOs se arman a mano en `api/` |
| `db/schema.md` | Diagrama de tablas |

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

### 2️⃣ Backend Inicia en Desarrollo

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
│  - OAuth Google configurado    │
│  - Sesión JWT en cookie        │
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

Aspirante y Admin General entran con email y contraseña (`POST /auth/login` y `POST /auth/admin/login`); los tres caminos terminan igual, con la cookie de sesión. El token nunca queda al alcance de JavaScript.

```
NAVEGADOR (Frontend)                          SERVIDOR (Backend)
    │                                             │
    │ 1. Click "Ingresar con Google"              │
    │    GET /api/auth/google                     │
    ├────────────────────────────────────────────>│ (Redirección a Google)
    │                                             │
    │ 2. Usuario se autentica en Google           │
    │ 3. Google vuelve con un código              │
    │    GET /api/auth/google/callback            │
    ├────────────────────────────────────────────>│
    │                                      ┌──────┴──────┐
    │                                      │ Spring      │
    │                                      │ valida el   │
    │                                      │ código con  │
    │                                      │ Google,     │
    │                                      │ dominio y   │
    │                                      │ alta en BD; │
    │                                      │ crea fila   │
    │                                      │ en sesiones │
    │                                      └──────┬──────┘
    │ 4. 302 a /dashboard                         │
    │    Set-Cookie: E3_SESION=<JWT>; HttpOnly    │
    │<────────────────────────────────────────────┤
    │                                             │
    │ 5. GET /api/auth/me (viaja la cookie)       │
    ├────────────────────────────────────────────>│
    │                                      ┌──────┴──────┐
    │                                      │ Spring      │
    │                                      │ valida JWT, │
    │                                      │ sesión      │
    │                                      │ abierta y   │
    │                                      │ cuenta      │
    │                                      │ activa      │
    │                                      └──────┬──────┘
    │ 6. HTTP 200 + Sesion                        │
    │<────────────────────────────────────────────┤
    │                                             │
```

El JWT solo lleva el id de la sesión. Como cada pedido consulta `sesiones` y la cuenta, cerrar sesión, desconectar un dispositivo o desactivar un usuario corta el acceso al instante.

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
- **Sesión:** viaja en una cookie `HttpOnly`, por eso el cliente usa `credentials: 'include'` y no maneja tokens.