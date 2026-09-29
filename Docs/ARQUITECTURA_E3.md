# USAL E3 - Arquitectura

## Estructura del Monorepo

```
usal-equivalencias-e3/
├── backend/                    # Java + Spring Boot 3.2
│   ├── src/main/java/com/usal/e3/
│   │   ├── api/                # Controllers REST
│   │   ├── service/            # Lógica de negocio
│   │   ├── repository/         # Acceso a datos (JPA)
│   │   ├── domain/             # Entidades JPA (modelos de dominio)
│   │   ├── mapper/             # MapStruct: Entity ↔ DTO
│   │   ├── security/           # JWT, OAuth2, permisos
│   │   ├── exception/          # Excepciones personalizadas
│   │   └── config/             # Configuración Spring
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/       # Scripts Flyway
│   ├── src/test/java/com/usal/e3/   # Tests (unitarios, repos, integración)
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                   # React + Vite
│   ├── src/
│   │   ├── assets/             # Imágenes y archivos estáticos
│   │   ├── components/
│   │   │   └── ui/             # Componentes base compartidos (Button, Input, Modal, Table...)
│   │   ├── constants/          # Rutas, roles, límites de archivos, reglas de auth, env
│   │   ├── hooks/              # Hooks reutilizables
│   │   ├── layouts/            # Layouts compartidos (público, dashboard, detalle...)
│   │   ├── pages/              # Una carpeta por área/rol (public, aspirante, academico...)
│   │   ├── router/             # Definición de rutas
│   │   ├── services/           # Comunicación con backend (api/client.ts es el único que usa fetch)
│   │   ├── store/              # Estado global (Context API)
│   │   ├── styles/             # Estilos globales
│   │   ├── tokens/             # Design Tokens (colores, espaciado, radios, sombras, tipografía...)
│   │   ├── types/              # Tipos y DTOs compartidos
│   │   ├── utils/              # Utilidades puras
│   │   ├── validations/        # Validaciones y mensajes de error compartidos
│   │   └── App.jsx
│   ├── public/
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
├── db/                         # Esquema BD (documentación)
│   └── schema.md               # Diagrama de tablas
│
├── docs/
│   ├── ARQUITECTURA.md         # Este archivo
│   ├── API.md                  # Endpoints
│   └── demo-credentials.md
│
├── Makefile                    # Orquestación centralizada
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| **Frontend (React)** | 5173 | http://localhost:5173 |
| **Backend (Spring)** | 8080 | http://localhost:8080 |
| **PostgreSQL** | 5432 | localhost:5432 |
| **pgAdmin** | 5050 | http://localhost:5050 |

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
make backend-dev
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
│  React App (JavaScript)                 │
│  ├─ Componentes                         │
│  ├─ Router (páginas)                    │
│  ├─ Context (estado global)             │
│  └─ Axios (cliente HTTP)                │
└─────────────────────────────────────────┘
```

---

## Interacción Frontend ↔ Backend

### Ejemplo: Usuario hace Login con Google

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
- React 18
- Vite
- Axios (cliente HTTP)
- Context API (estado global)
- Tailwind CSS

#### Estructura de Carpetas

| Carpeta | Contenido | Ejemplos |
|---------|-----------|----------|
| **assets/** | Imágenes, SVGs, fuentes estáticas | logos, iconos, fotos |
| **components/ui/** | Componentes base reutilizables | Button, Input, Modal, Table, Card |
| **constants/** | Constantes de aplicación | rutas, roles (ACAD, ADMIN), límites de archivo, reglas auth |
| **hooks/** | Hooks React reutilizables | useAuth, usePermiso, useFormData, useFetch |
| **layouts/** | Layouts compartidos por sección | LayoutPublico, LayoutDashboard, LayoutDetalle |
| **pages/** | Páginas por área/rol | pages/public/, pages/aspirante/, pages/academico/, pages/admin/ |
| **router/** | Definición de rutas | rutas públicas, protegidas por rol |
| **services/** | Cliente HTTP | api/client.ts (ÚNICO que hace fetch), servicios por dominio |
| **store/** | Estado global con Context | AuthContext, SolicitudesContext, PermsContext |
| **styles/** | CSS/SCSS globales | reset, tipografía global, breakpoints |
| **tokens/** | Design System | colores, espaciado, radios, sombras, tamaños |
| **types/** | TypeScript types | DTOs, interfaces compartidas |
| **utils/** | Funciones puras | formatters, parsers, helpers |
| **validations/** | Esquemas de validación | Zod schemas, mensajes de error |

#### Cómo habla con Backend

- **Único punto de comunicación:** `services/api/client.ts` (centraliza Axios/fetch)
- **Interceptores:** Agregan JWT automáticamente en headers Authorization
- **Servicios por dominio:** `services/solicitudes.ts`, `services/usuarios.ts`, etc.
- **Sin fetch directo:** Nunca hacer Axios/fetch desde componentes o utils
- **Manejo de errores centralizado:** Interceptores responden a 401 (token expirado), 403 (sin permisos), etc.