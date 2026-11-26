# E3 · Equivalencias USAL

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-09-28 | lautarogomizelj | README canónico: estado actual, stack y cómo correr el proyecto |
| 1.1 | 2026-09-29 | lautarogomizelj | Stack en tablas, tabla de puertos y convenciones de git unificadas |
| 1.2 | 2026-10-07 | lautarogomizelj | Corrección de rutas de documentos y alta de Gestión de Cambios |
| 1.3 | 2026-10-07 | lautarogomizelj | Unificación de las guías de Frontend y Backend en este documento |
| 1.4 | 2026-10-07 | lautarogomizelj | Sección de control de versiones y gestión de cambios; regla de versionado en AGENTS.md |
| 1.5 | 2026-10-14 | jumaluc | Enlace al contrato de la API propuesto en `Docs/API.md` |
| 1.6 | 2026-10-31 | jumaluc | Sprint 1 de frontend: mocks y tokens del diseño; corrige el nombre de la universidad y el enlace al contrato |
| 1.7 | 2026-10-31 | jumaluc | Estado actual: pantallas públicas de acceso construidas contra mocks |
| 1.8 | 2026-11-02 | jumaluc | Backend de los sprints 1 y 2: Docker, Flyway, login por rol, sesión, RBAC y perfil; guía de backend con lo que ya existe (CC-007, CC-008) |
| 1.9 | 2026-11-15 | jumaluc | Backend del Sprint 3: motor de transiciones, solicitudes, documentos, permisos temporales y catálogos; carpeta de archivos y `ARCHIVOS_DIR` (CC-009) |
| 1.10 | 2026-11-15 | jumaluc | Frontend: sesión, rutas protegidas, layout privado con menú por rol y pantalla Mis solicitudes del aspirante |
| 1.11 | 2026-11-15 | jumaluc | Frontend del Sprint 3: panel de inicio, Nueva solicitud y detalle de la solicitud con carga de documentos, responsables e historial |
| 1.12 | 2026-11-15 | jumaluc | El aspirante puede cancelar su solicitud antes de enviar la documentación: estado `SOL-CANC`, backend y frontend (RF-ASP-010, CC-010) |
| 1.13 | 2026-11-17 | jumaluc | Frontend: login del personal y login de Admin (CC-011); alta de `Docs/TEMAS_PROFESOR.md` |
| 1.14 | 2026-11-17 | jumaluc | Frontend: Mi perfil y "Recuérdame"; nombre y apellido separados en backend y frontend (migración `V6`, CC-012); alta de `Docs/BLOQUEOS.md` |
| 1.15 | 2026-11-17 | jumaluc | Frontend: stepper de etapas en el detalle de la solicitud |
| 1.16 | 2026-11-20 | jumaluc | Correcciones de la auditoría del 2026-11-19 (CSRF, archivos y bloqueos, sin ruta `/login`, sin enlace al login de Admin y CORS solo para el puerto 5173), login del personal con usuario y contraseña con cuentas demo `1234`, y títulos, cerrar sesión y franjas del área privada (CC-014, CC-015, CC-016) |
| 1.17 | 2026-11-20 | jumaluc | Sprint 4 en backend y frontend: bandeja del personal, tomar proceso, darse de baja, revisión por documento, recarga selectiva y mails por cambio de estado (CC-013); de la auditoría del 2026-11-19, lo que depende de este sprint: código `NO_RESPONSABLE`, acciones del Admin sobre una solicitud y avisos por mail después del commit (CC-017) |
| 1.18 | 2026-11-20 | jumaluc | Sprint 5 en backend y frontend: análisis preliminar con matriz de equivalencias, posible horario de cursada y fechas de reunión, las dos rondas de fechas, elección del aspirante y enlace de Meet; migración `V7` (CC-018, CC-019) |
| 1.19 | 2026-11-22 | jumaluc | Sprint 6 en backend y frontend: chat de la solicitud, fecha pautada por chat, resultado de la reunión, actualización del análisis y decisión de inscripción; migración `V8`. El informe de materias del aspirante pasa al Sprint 7 (CC-020, CC-021) |
| 1.20 | 2026-11-22 | jumaluc | Sprint 7 en backend y frontend: legalización de documentos, informe de materias, cupón de pago en PDF, comprobante y verificación del pago; migración `V9` y dependencia OpenPDF. Una solicitud recorre las 27 transiciones (CC-022, CC-023) |
| 1.21 | 2026-11-22 | jumaluc | Sprint 8 en backend y frontend: panel de Admin (cuentas del personal, permisos por carrera, carreras, asignación de responsables y alertas), auditoría general, campana de notificaciones, exportación de expedientes en ZIP y pantallas del Revisor; migración `V10` (CC-024, CC-025) |
| 1.22 | 2026-11-26 | jumaluc | Guía de pruebas (`Docs/GUIA_PRUEBAS.md`): cómo levantar el sistema completo y recorrerlo con las cuentas demo. |

Sistema web de tramitación de equivalencias académicas. Digitaliza y automatiza el proceso de reconocimiento de materias para ingresantes de la Facultad de Ingeniería de la Universidad del Salvador (USAL).

---

## Qué problema resuelve

Hoy el proceso de reconocimiento de equivalencias depende por completo del correo electrónico y de Google Drive. Nadie sabe en qué etapa está cada solicitud, el avance depende de que una sola persona recuerde cuándo enviar cada mail, y tanto los aspirantes como el personal administrativo no tienen forma de consultar el estado de su trámite. Tampoco existe un registro claro de quién hizo qué y cuándo.

Este sistema reemplaza ese circuito por una plataforma web donde cada rol tiene su propio panel, el trámite avanza mediante transiciones de estado explícitas, cada cambio de estado genera una notificación automática, y queda una traza auditable de cada paso con usuario, fecha e IP de origen. Los responsables se asignan de forma dinámica: nadie queda asignado por defecto, cada uno toma el proceso que puede tomar.

---

## Estado actual

**El backend y el frontend completaron el Hito 2 (solicitud y revisión documental, sprints 3 y 4), el Hito 3 (análisis, reunión e inscripción, sprints 5 y 6) y el Hito 4 (legalización y pago en el Sprint 7; administración, auditoría, notificaciones y exportación en el Sprint 8). No hay nada desplegado.**

El backend tiene hecha su parte de los sprints 1 y 2: Docker Compose (backend, PostgreSQL, pgAdmin y mail de prueba), migraciones Flyway, formato de error común, Swagger UI, registro de aspirante con código por mail, login con bloqueo por intentos, login con Google, login del personal con usuario y contraseña, login de Admin, sesión con JWT en cookie, protección CSRF, RBAC por rol y perfil (RF-AUTH-001 a 005 y 007). El login con Google está implementado, pero no se probó contra Google porque faltan las credenciales de la USAL; mientras tanto el personal entra con usuario y contraseña (`POST /auth/personal/login`, CC-016).

Del Sprint 3, el backend tiene el motor de transiciones (rol, permiso de participante, responsable y `estadoEsperado`, con registro en `estados_solicitud` y auditoría), los permisos temporales por solicitud, los catálogos de carreras y estados, y RF-ASP-001, 002 y 010: el aspirante crea su solicitud, sube documentos en borrador y envía la documentación (`SOL-INI` → `DOC-REV`) o la cancela antes de enviarla (`SOL-INI` → `SOL-CANC`). También expone el detalle con responsables y acciones disponibles, el historial y el listado del aspirante.

Del Sprint 4, el backend tiene la bandeja del personal con sus filtros y su orden por carrera (RF-PROC-001), tomar el proceso y darse de baja (RF-PROC-003 y 004), la revisión de la documentación con marcas por documento, documentos faltantes y las decisiones de aceptar (`DOC-REV` → `ANAL-PEND`) o rechazar (`DOC-REV` → `DOC-RECH`) (RF-ACAD-001 y 009), la recarga selectiva del aspirante (`DOC-RECH` → `DOC-REV`, RF-ASP-004) y los mails de cada cambio de estado implementado (RF-ESTADO-002). Con esto una solicitud recorre `SOL-INI` → `DOC-REV` → `DOC-RECH` → `DOC-REV` → `ANAL-PEND`. Para actuar, el personal tiene que ser participante y además el responsable de su rol; el Admin ejecuta las acciones del Académico y de Secretaría sin serlo, por ahora solo desde la API. Los mails de aviso salen recién cuando la acción queda confirmada y, si fallan, no la frenan.

Del Sprint 5, el backend tiene el análisis preliminar (RF-ACAD-002): el Académico responsable carga la matriz de equivalencias y el posible horario de cursada, cada uno como archivo, como notas o las dos cosas, y en la misma acción propone hasta 7 horarios de reunión de 1 hora (`ANAL-PEND` → `REU-FEC-ASP-1`). También las rondas de fechas (RF-ASP-005 y RF-ACAD-003): el aspirante elige un horario (→ `REU-PEND`), pide otras fechas en la 1ª ronda (→ `REU-FEC-ACAD`, donde el Académico propone la 2ª) o rechaza la 2ª (→ `REU-CHAT`); con la reunión pactada, el Académico carga el enlace de Meet y el sistema se lo avisa al aspirante. Cada cambio manda su mail de RF-ESTADO-002.

Del Sprint 6, el backend tiene el chat de la solicitud (RF-MSG-001 a 004): escriben el aspirante y los responsables, con texto, adjunto o las dos cosas, y el resto del personal lo lee; lleva la cuenta de los mensajes sin leer y de los vistos, y queda en solo lectura cuando la solicitud termina. También el cierre de la etapa de reunión (RF-ACAD-003 y 004): el Académico fija la fecha que pautaron por chat (`REU-CHAT` → `REU-PEND`), indica que la reunión finalizó, con notas que ve solo el personal (→ `ANAL-POST`), o que el aspirante no se presentó (→ `REU-FEC-ACAD`, donde vuelve a proponer fechas). En `ANAL-POST` puede actualizar la matriz de equivalencias y el horario de cursada, con una versión nueva que reemplaza a la anterior, y decide la inscripción (RF-ACAD-006): la acepta (→ `INSC-ACEP`) o la rechaza con su motivo (→ `INSC-RECH`, que termina el trámite). Con esto una solicitud recorre las transiciones 1 a 15.

Del Sprint 7, el backend tiene la legalización (RF-ASP-009 y RF-ACAD-005): con la inscripción aceptada el aspirante responde si sus documentos están legalizados (→ `LEG-REV`, o → `LEG-PEND` hasta que avisa que los legalizó), sin cargar archivos; el Académico los revisa otra vez documento por documento y los rechaza (→ `LEG-RECH`, y el aspirante recarga los marcados) o los acepta informando la cantidad de materias reconocidas (→ `CUPON-SOL`). Ese dato, las observaciones y el monto del cupón forman el informe de materias del aspirante (RF-ASP-006). En la etapa de pago (RF-SEC-001, RF-ASP-007 y RF-SEC-002) la Secretaría responsable indica el monto y el sistema genera el cupón en PDF (→ `CUPON-PAG`); el aspirante carga el comprobante (→ `PAG-CTRL`) y Secretaría acepta el pago (→ `E3-INI`) o lo rechaza con su comentario (→ `PAG-REENV`, y el aspirante carga otro; los anteriores se conservan). Con esto una solicitud recorre las 27 transiciones. Quedan sin hacer los requisitos que dependen de estados posteriores a `E3-INI`.

El frontend tiene los Design Tokens del diseño de interfaces, el cliente HTTP centralizado con manejo tipado de errores, una capa de mocks del contrato, el router, los layouts público y privado, la página de error 404 y los componentes base (botón, campos de formulario, modales, tabla, paginador y badge de estado). Las pantallas públicas de inicio (con el login del aspirante integrado, sin una ruta `/login` aparte), registro, verificación del correo y recuperación de contraseña siguen el diseño de Drive; registro, verificación y recuperación funcionan contra mocks. El login del personal (`/login-personal`, con usuario y contraseña hasta que esté el ingreso con Google) y el login de Admin (`/login-admin`, sin enlaces que lleven a él: el Admin escribe la dirección) reutilizan el diseño del login de aspirante. El login del aspirante inicia sesión y lleva al área privada, que tiene la sesión compartida (`AuthProvider`), las rutas protegidas por rol y el menú lateral según el rol. El aspirante tiene el panel de inicio, Mis solicitudes, Nueva solicitud y el detalle de la solicitud: la carga de documentos con envío o cancelación en `SOL-INI` y, en los demás estados, el banner del estado con las pestañas Documentación, Detalles (con los responsables) e Historial. Mi perfil está disponible para todos los roles: datos personales con nombre y apellido separados, cambio de contraseña, cambio de email del Admin, sesiones activas e historial de accesos. Ayuda muestra un aviso de "Próximamente". El login y las pantallas del aspirante se probaron también contra el backend real. El andamiaje de rutas, roles y tipos ya contempla los seis roles del sistema —los cinco del catálogo (`ASPIRANTE`, `ACADEMICO`, `SECRETARIA`, `REVISOR`, `ADMIN`) más `OBSERVADOR`, que no es un rol del catálogo sino el estado de un usuario sin permisos en una carrera—, y todos tienen sus pantallas. El Académico y Secretaría tienen su panel de inicio, la bandeja de solicitudes con buscador y filtros, y el detalle con "Tomar proceso" y "Darse de baja"; el Académico responsable revisa la documentación documento por documento y la acepta o la rechaza, y el aspirante recarga lo observado desde su solicitud. En la etapa de análisis y reunión, el Académico responsable carga la matriz de equivalencias y el posible horario de cursada y propone las fechas; el aspirante ve los dos documentos en su solicitud y elige una fecha, pide otras o rechaza la 2ª ronda; y con la reunión pactada el Académico carga el enlace de Meet y el aspirante se une desde su solicitud. La pestaña Chat del detalle es el chat de la solicitud, con el contador de mensajes sin leer; cuando la fecha se coordina por chat es la pestaña que se abre al entrar, y el Académico fija la fecha desde el banner. Después de la reunión, el Académico indica si finalizó o si el aspirante no se presentó, actualiza el análisis si hace falta y acepta o rechaza la inscripción; el aspirante ve el resultado en su solicitud, con el motivo si fue rechazada. Quien no tiene permiso sobre la carrera ve todo en modo lectura. El Admin y el Revisor tienen sus pantallas desde el Sprint 8, que se detallan más abajo.

Del Sprint 8, el backend tiene el módulo de Administración (RF-ADMIN-001 a 005 y 007, bajo `/admin`): alta y edición de cuentas del personal, con la contraseña inicial que escribe el Admin mientras el ingreso sea con usuario y contraseña; permisos permanentes por carrera, con revocación que pide forzarla si hay procesos activos; asignación manual de responsables con permiso temporal sobre la solicitud; alertas de procesos sin responsable y de cuentas bloqueadas, y alta, edición y baja lógica de carreras. También la auditoría general con filtros para Admin y Revisor (RF-AUDIT-003), la campana, que guarda en `notificaciones` cada aviso que sale por mail (migración `V10`), y la exportación de expedientes en ZIP, individual o de todos los que muestra la bandeja con sus filtros (RF-PROC-005). El frontend tiene las pantallas de todo eso: inicio del Admin con alertas, gestión de usuarios y de carreras, "Asignar responsable" en el detalle, auditoría, inicio del Revisor, campana en la barra superior y los botones "Descargar expediente" y "Exportar". Quedan sin hacer la reapertura de expedientes (RF-ADMIN-006, pendiente de definición) y los permisos por facultad (CC-001 a CC-005).

En resumen: el frontend completó su parte del Sprint 1. Del Sprint 2 solo quedan dos cosas que no dependen del código: probar el login con Google con credenciales reales y la galería de avatares del perfil, que espera las ilustraciones (ver [`Docs/BLOQUEOS.md`](Docs/BLOQUEOS.md)). De los sprints 3 a 8 tiene todas sus pantallas: del Sprint 7, la pregunta de legalidad, el aviso de legalizados, su recarga y su revisión, el informe de materias, la gestión de pago de Secretaría (cupón con monto y verificación del comprobante) y la carga del comprobante del aspirante. Por defecto el frontend trabaja contra mocks (`VITE_USE_MOCKS=true`); registro, verificación, recuperación, los tres logins, Mi perfil y el recorrido completo del Hito 2 se probaron también contra la API real; del Sprint 5, el envío del análisis con sus fechas, la elección del horario y el enlace de Meet; y del Sprint 6, el chat con adjunto, la fecha pautada por chat, el resultado de la reunión, la actualización del análisis y la aceptación de la inscripción; y del Sprint 7, el recorrido completo desde la inscripción aceptada hasta `E3-INI`, con el rechazo y la recarga de legalizados, el cupón en PDF y el rechazo y reenvío del comprobante; y del Sprint 8, el alta de una cuenta, los permisos por carrera, la asignación de un responsable con permiso temporal, las carreras, la auditoría, la campana y la exportación de expedientes.

---

## Stack

### Frontend

| Capa | Tecnología |
|-------|------------|
| **Runtime** | React 19 + React DOM 19 |
| **Lenguaje** | TypeScript 6 |
| **Build / Dev server** | Vite 8 |
| **Estilos** | Tailwind CSS v4 + Design Tokens del proyecto |
| **Tipografías** | DM Serif Display (títulos) e Inter (texto), servidas con `@fontsource` |
| **Navegación** | React Router 8 |
| **Íconos** | `lucide-react` (única librería de íconos) |
| **Estado global** | Context API de React, sin librería externa |
| **Linter** | `oxlint` |
| **Cliente HTTP** | `fetch` nativo, centralizado en `src/services/api/client.ts` |
| **Entorno** | Node 24 + npm · alias `@` para apuntar a `src` |

### Backend

| Capa | Tecnología |
|-------|------------|
| **Runtime** | Java 17 / Spring Boot 3.2 |
| **Base de datos** | PostgreSQL 15 |
| **Autenticación** | Spring Security 6 + OAuth2 Google + JWT en cookie `HttpOnly` |
| **Mail** | Spring Mail (Mailpit en desarrollo) |
| **Documentación de la API** | SpringDoc OpenAPI (Swagger UI) |
| **Migraciones** | Flyway |
| **Build** | Maven |
| **Logging** | SLF4J + Logback |
| **Testing** | JUnit 5 + Mockito |

### Infraestructura

| Capa | Tecnología |
|-------|------------|
| **Contenedores** | Docker |
| **Orquestación** | Docker Compose |
| **Servicios del stack** | Aplicación backend, PostgreSQL, pgAdmin y Mailpit |

### Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| **Frontend (React)** | 5173 | http://localhost:5173 |
| **Backend (Spring)** | 8080 | http://localhost:8080/api |
| **PostgreSQL** | 5432 | localhost:5432 |
| **pgAdmin** | 5050 | http://localhost:5050 |
| **Mailpit** | 8025 | http://localhost:8025 |

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

## Control de versiones y gestión de cambios

La documentación de este repo es fuente de verdad: se versiona en lugar de duplicarse.

- **Control de versiones.** Cada documento fuente tiene una tabla *Control de versiones* justo debajo del título: [`Docs/Sistema_Equivalencias_Requisitos.md`](Docs/Sistema_Equivalencias_Requisitos.md), [`Docs/frontend-interfaces.md`](Docs/frontend-interfaces.md), [`Docs/ARQUITECTURA_E3.md`](Docs/ARQUITECTURA_E3.md) y este README. Los cambios se hacen **editando el archivo en el lugar** y agregando una fila (Versión · Fecha · Autor · Descripción) al final de la tabla, en orden cronológico. No se crea un archivo nuevo por cada cambio: los históricos quedan en `Docs/Desactualizados/`.
- **Control de cambios.** Lo que cambia durante el desarrollo y no estaba previsto en la especificación se registra en [`Docs/GESTION_CAMBIOS.md`](Docs/GESTION_CAMBIOS.md) con un ID (`CC-001`, `CC-002`, …) y un estado: `Propuesto` → `Aprobado` → `Implementado` / `Rechazado`.
- **Flujo.** Registrar el cambio en Gestión de Cambios → evaluar impacto → aprobar → implementar en el documento fuente y sumar la fila en su tabla de control de versiones → commit con el prefijo correspondiente y el ID del cambio.

---

## Cómo correr el proyecto

Hace falta Node 24 y npm para el frontend, y Docker para el backend. El punto de entrada es el Makefile de la raíz, que envuelve los scripts de npm. Las tareas del backend llevan el prefijo `backend-` para no chocar con las del frontend y están en la [Guía de Backend](#guía-de-backend).

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
| `make down` | Detiene el stack de contenedores. |

Para levantar el entorno de desarrollo:

```bash
cd frontend
cp .env.example .env
cd ..
make install
make dev          # http://localhost:5173
```

El `.env` se crea una sola vez y es donde se define la URL del backend. Con `VITE_USE_MOCKS=true` el frontend responde con los mocks del contrato y no necesita backend; con los mocks se entra como aspirante con cualquier email y la contraseña `1234`, y como personal o Admin con las cuentas de [`Docs/demo-credentials.md`](Docs/demo-credentials.md). Las variables de entorno de Vite solo se leen al arrancar el servidor, así que si modificás el `.env` tenés que reiniciar el dev server para que el cambio tome efecto.

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

- El contrato de la API (rutas, DTOs y formato de errores) está propuesto en [`Docs/Contratos API REST.md`](Docs/Contratos%20API%20REST.md); falta cerrar los puntos abiertos de su §10.3.
- Las diferencias entre el diseño de Drive y los requisitos están en el Anexo B de `frontend-interfaces.md`.

---

## Guía de Backend

Las secciones marcadas "(a implementar)" describen el diseño previsto, no el estado actual.

### Cómo arrancar el servicio

El único prerequisito es Docker. No hace falta tener Java, Maven ni PostgreSQL instalados localmente.

```bash
make backend-up-dev
```

La primera vez crea `application.yml` a partir del ejemplo. El stack queda disponible en:

| Servicio | URL |
|----------|-----|
| **API REST** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health Check** | http://localhost:8080/actuator/health |
| **pgAdmin** | http://localhost:5050 |
| **Mails de prueba** | http://localhost:8025 |

Los códigos de 6 dígitos del registro y del recupero de contraseña se leen en Mailpit. No hacen falta credenciales de Google: el personal entra con usuario y contraseña (`POST /auth/personal/login`).

### Comandos Make

Los targets del backend llevan el prefijo `backend-` para no chocar con los del frontend (`make dev`, `make build`, `make lint`, `make clean` y `make down`).

```makefile
make backend-up-dev      # Inicia stack completo (app + PostgreSQL + pgAdmin + Mailpit)
make backend-up-infra    # Inicia solo PostgreSQL, pgAdmin y Mailpit (sin app)
make down                # Detiene todos los contenedores (compartido con el frontend)

make backend-test        # Ejecuta todos los tests contra un PostgreSQL descartable
make backend-build       # Construye la imagen del backend
make backend-logs        # Sigue logs del contenedor backend
```

Todos los comandos corren dentro de Docker: no necesitás JDK local.

### Comandos Make (a implementar)

```makefile
make backend-up-prod     # En modo producción (requiere env vars)
make backend-test-coverage # Ejecuta tests y abre reporte JaCoCo
make backend-lint          # Ejecuta Checkstyle y SpotBugs
make backend-format        # Verifica formateo (Google Java Format)
make backend-format-fix    # Auto-formatea archivos
make backend-pr-checks     # Ejecuta todos los checks requeridos antes de PR

make backend-clean      # Limpia artefactos de build
make backend-db-migrate # Ejecuta migraciones Flyway manualmente
```

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

### Reglas de Arquitectura

- Controllers llaman services solamente — nunca repositories directamente.
- Services llaman repositories solamente — nunca controllers.
- Domain classes son Java puro — solo anotaciones `@Entity` e `@Id`.
- DTOs son Java Records y viven al lado del controller en `api/<dominio>/`.
- La validación de permisos (`revoked_at IS NULL`) vive en `PermisosService` — **NUNCA** en controllers.

### Persistencia de Datos

PostgreSQL persiste en `data/` en la raíz del monorepo (bind-mounted en Docker e ignorado por Git). Se borra para resetear el estado local.

Los documentos que suben los aspirantes van a `data/archivos/`, ordenados por año, DNI y expediente. Fuera de Docker, la carpeta la define `ARCHIVOS_DIR` (por defecto, `./archivos`).

### Datos Demo

Para poblar la base con una cuenta por rol y un segundo Académico de la misma carrera, se setea `SEED_DEMO=true` al arrancar. Las solicitudes en cada estado se suman cuando exista el trámite (a implementar).

```bash
SEED_DEMO=true make backend-up-dev
```

Es idempotente: si los datos ya existen no hace nada, así que es seguro entre restarts (desactivado por defecto y siempre apagado con el perfil `prod`). Para re-seedear, se resetea la BD primero (`make down` y borrar `data/`) y se bootea de nuevo.

Las credenciales de las cuentas demo están en [`Docs/demo-credentials.md`](Docs/demo-credentials.md).

### Configuración de Ambiente

`make backend-up-dev` copia `backend/src/main/resources/application.yml.example` a `application.yml` si no existe. Ese archivo está en `.gitignore` — **nunca** commitear secrets. Si el ejemplo suma claves nuevas, hay que volver a copiarlo.

Cuando se corre vía Docker Compose, las connection strings se setean automáticamente. No hace falta configuración manual para `make backend-up-dev`.

El backend acepta pedidos solo desde `http://localhost:5173`. Para levantar el frontend en otro puerto hay que sumarlo en `e3.cors.allowed-origins` del `application.yml` local. El frontend y la API se abren con el mismo nombre de host (`localhost` en los dos), porque el frontend lee la cookie del token CSRF.

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
MAIL_FROM=no-responder@usal.edu.ar

# Frontend (destino del login con Google y origen permitido por CORS)
FRONTEND_URL=https://tu-frontend
CORS_ALLOWED_ORIGINS=https://tu-frontend

# Seed Demo
SEED_DEMO=false  # Desactivado en producción

# Documentos del trámite (carpeta con backup)
ARCHIVOS_DIR=/ruta/a/archivos
```

---

## Documentación

El repositorio es un monorepo con dos aplicaciones y un directorio de documentación.

Este README es la fuente canónica del estado actual, el stack, los puertos y las convenciones de git. Los demás documentos se vinculan a esta página en lugar de repetir esa información, para que no haya dos versiones que puedan divergir.

**`backend/`** — Servicio Spring Boot. MapStruct, ArchUnit y los checks de PR todavía no están (a implementar):

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
├── application.yml.example          # Config default (se copia a application.yml)
├── application-dev.yml              # Overrides para desarrollo
├── application-prod.yml             # Overrides para producción
├── application-docker.yml           # Config Docker (auto)
├── db/migration/                    # Scripts Flyway
│   ├── V1__initial.sql
│   ├── V2__catalogos.sql
│   ├── V3__autenticacion.sql
│   ├── V4__solicitudes.sql
│   ├── V5__cancelacion.sql
│   ├── V6__apellido.sql
│   ├── V7__analisis_y_reunion.sql
│   ├── V8__chat.sql
│   └── V9__legalizacion_y_pago.sql
└── logback-spring.xml              # Config logging

src/test/java/com/usal/e3/
├── service/             # Tests unitarios de servicios
├── repository/          # Tests de acceso a datos
└── integration/         # Tests de integración, contra PostgreSQL real
```

Las dependencias fluyen en una dirección: `api → service → repository → domain`. Los tests de ArchUnit refuerzan esto en tiempo de build.

**`frontend/`** — Aplicación React:

```text
src/
├── assets/        imágenes y archivos estáticos
├── components/
│   ├── privado/   piezas de las pantallas del área privada (encabezado con título y foto)
│   ├── solicitudes/  tabla, carga de documentos y detalle de una solicitud; `vistas.ts` asocia cada estado con su interfaz
│   └── ui/        componentes base compartidos (Button, Input, Modal, Table...)
├── constants/     rutas, roles, límites de archivos, reglas de auth, env
├── hooks/         hooks reutilizables
├── layouts/       layouts compartidos (público, dashboard, detalle...)
├── pages/         una carpeta por área/rol (public, aspirante, academico...)
├── router/        definición de rutas
├── services/      comunicación con el backend (api/client.ts es el único que usa fetch; mocks/ responde sin backend)
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
| `Docs/demo-credentials.md` | Cuentas demo por rol para probar el sistema en desarrollo. |
| `Docs/GUIA_PRUEBAS.md` | Guía para probar el sistema: puesta en marcha contra el backend real, recorrido completo de una solicitud, pruebas por rol y qué revisar si algo falla. |
| `Docs/BLOQUEOS.md` | Lo que falta conseguir o decidir para poder seguir: credenciales, cuentas y materiales que no dependen del código. |
| `Docs/TEMAS_PROFESOR.md` | Dudas del equipo para charlar con el profesor, con lo que se hizo mientras tanto. |
| `Docs/Diagramas/` (3 archivos `.jpeg`) | Diagramas de actividades y de los flujos de análisis preliminar y revisión legal. |
| `AGENTS.md` | Pautas de trabajo para las herramientas de asistencia: cómo encarar un cambio, reglas de UI y prefijos de commit. |
