# E3 · Equivalencias USAL

Sistema web de tramitación de equivalencias académicas. Digitaliza y automatiza el proceso de reconocimiento de materias para ingresantes de la Facultad de Ingeniería de la Universidad Nacional de San Luis.

---

## Qué problema resuelve

Hoy el proceso de reconocimiento de equivalencias depende por completo del correo electrónico y de Google Drive. Nadie sabe en qué etapa está cada solicitud, el avance depende de que una sola persona recuerde cuándo enviar cada mail, y tanto los aspirantes como el personal administrativo no tienen forma de consultar el estado de su trámite. Tampoco existe un registro claro de quién hizo qué y cuándo.

Este sistema reemplaza ese circuito por una plataforma web donde cada rol tiene su propio panel, el trámite avanza mediante transiciones de estado explícitas, cada cambio de estado genera una notificación automática, y queda una traza auditable de cada paso con usuario, fecha e IP de origen. Los responsables se asignan de forma dinámica: nadie queda asignado por defecto, cada uno toma el proceso que puede tomar.

---

## Estado actual

**El proyecto está en etapa de scaffolding. No hay nada desplegado ni en ejecución.**

El backend es un esqueleto vacío. Existen las carpetas del package de Java con la separación por capas prevista, pero todas contienen únicamente un archivo `.gitkeep`. No hay `pom.xml`, ni Dockerfile, ni migraciones de Flyway, ni una sola clase de Java. Tampoco existe la orquestación con Docker Compose que la documentación describe.

El frontend sí es una base funcional: tiene el sistema de Design Tokens completo, el cliente HTTP centralizado con manejo tipado de errores, el router, el layout público, la página de inicio, la página de error 404 y un componente base de botón. El andamiaje de rutas, roles y tipos ya contempla los seis roles del sistema, pero las pantallas de negocio todavía no existen.

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
| **Migraciones** | Flyway |
| **Build** | Maven |
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
| **Backend (Spring)** | 8080 | http://localhost:8080 |
| **PostgreSQL** | 5432 | localhost:5432 |
| **pgAdmin** | 5050 | http://localhost:5050 |

Una aclaración importante para el desarrollo local: en la máquina actual no hay JDK ni Maven instalados. Todo lo que involucre Java tendrá que correr dentro de Docker.

---

## Convenciones de git

```
feature/* ──► develop ──► beta/* ──► main
                              │
                         hotfix/* ──► main
```

| Rama | Propósito |
|------|-----------|
| **main** | Releases estables en producción |
| **develop** | Rama de integración — aquí mergean todos los features |
| **beta/*** | Release candidates (ej: beta/1.0) |
| **hotfix/*** | Fixes emergentes que van directo a main |
| **feature/*** | Features nuevos |
| **fix/*** | Bug fixes |
| **enhancement/*** | Mejoras a features existentes |

Los mensajes de commit llevan un prefijo que indica el tipo de cambio:

| Prefijo | Uso |
|---------|-----|
| **ADDED:** | Nueva funcionalidad |
| **FIX:** | Corrección de bug |
| **HOTFIX:** | Corrección urgente aplicada directo en producción |
| **REFACTOR:** | Reorganización sin cambio de comportamiento (mover, renombrar, limpiar) |

`backend/` y `frontend/` son independientes entre sí, así que conviene que cada commit toque una sola de las dos, y que los cambios de documentación vengan en commits separados de los de código.

---

## Organización del proyecto

El repositorio es un monorepo con dos aplicaciones y un directorio de documentación.

**`backend/`** — Servicio Spring Boot. Dentro de `src/main/java/com/usal/e3/` están las capas ya separadas: `api` con los controladores REST agrupados por dominio, `service` con la lógica de negocio, `repository` con el acceso a datos, `domain` con las entidades JPA, `mapper` para la conversión entre entidad y DTO, más `security`, `exception` y `config`. En `src/main/resources/db/migration/` van los scripts de Flyway. En `src/test/` están los tres niveles de prueba previstos: unitarios de servicio, de repositorio y de integración.

**`frontend/`** — Aplicación React. La carpeta `src/` está organizada por responsabilidad: `assets` para los archivos estáticos, `components/ui` para los componentes base compartidos, `constants` para rutas, roles, límites de archivo y variables de entorno, `hooks` y `store` para la lógica y el estado reutilizables, `layouts` para los layouts compartidos por sección, `pages` con una carpeta por área o rol, `router` para la definición de rutas, `services` para toda la comunicación con el backend, `styles` para el CSS global, `tokens` para el Design System, `types` para los tipos y DTOs compartidos, `utils` para funciones puras y `validations` para las reglas y mensajes de error.

**`Docs/`** — Documentación del proyecto: especificación de requisitos, especificación de interfaces de frontend, notas de arquitectura, los README de backend y de frontend, y la carpeta `Docs/Diagramas/` con los diagramas de actividades y de los flujos principales del proceso.

---

## Cómo correr el proyecto

Hace falta Node 24 y npm. El punto de entrada es el Makefile de la raíz, que envuelve los scripts de npm. Solo hay tareas de frontend: el backend todavía no existe.

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

## Documentación

- `Docs/Sistema_Equivalencias_Requisitos_v6.md` — Especificación funcional y no funcional, versión 6. Es la fuente de verdad del negocio: actores, módulos, estados del expediente, esquema de tablas y reglas de permisos. Cuando haya una duda sobre qué tiene que hacer el sistema, la respuesta está acá.
- `Docs/frontend-interfaces.md` — Especificación de interfaces: cada pantalla, campo, estado de carga y acción por rol, más los estándares obligatorios de Frontend (tokens, accesibilidad, responsive, validaciones). Es el contrato entre el negocio y la capa de presentación, y es el documento que manda cuando hay que decidir cómo mostrar algo.
- `Docs/ARQUITECTURA_E3.md` — Estructura del monorepo, puertos, flujo de comunicación entre Frontend y Backend y las tres capas.
- `Docs/README_FRONTEND_E3.md` — Guía de trabajo del equipo Frontend: stack, estructura de carpetas, reglas rápidas de estilos y servicios, y la lista de pendientes del lado Frontend.
- `Docs/README_BACKEND_E3.md` — Guía de trabajo del equipo Backend.
- `Docs/Diagramas/` — Diagramas de actividades y de los flujos de análisis preliminar y revisión legal.
