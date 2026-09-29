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

**Frontend** (implementado): React 19 sobre Vite 8, TypeScript 6, Tailwind CSS v4 como motor de estilos, React Router 8 para navegación y `lucide-react` como única librería de íconos. `oxlint` como linter. El estado global está previsto vía Context API de React, sin librería externa. Los imports usan el alias `@` para apuntar a `src`, así que no hay rutas relativas largas.

**Backend** (previsto, no implementado): Java 17, Spring Boot 3.2, PostgreSQL 15, Spring Security 6 con OAuth2 de Google y JWT, Flyway para migraciones, Maven como build, y JUnit 5 con Mockito para las pruebas.

**Infraestructura** (prevista, no implementada): Docker y Docker Compose para levantar aplicación, base de datos y pgAdmin.

Una aclaración importante para el desarrollo local: en la máquina actual no hay JDK ni Maven instalados. Todo lo que involucre Java tendrá que correr dentro de Docker.

---

## Organización del proyecto

El repositorio es un monorepo con dos aplicaciones y un directorio de documentación.

**`backend/`** — Servicio Spring Boot. Dentro de `src/main/java/com/usal/e3/` están las capas ya separadas: `api` con los controladores REST agrupados por dominio, `service` con la lógica de negocio, `repository` con el acceso a datos, `domain` con las entidades JPA, `mapper` para la conversión entre entidad y DTO, más `security`, `exception` y `config`. En `src/main/resources/db/migration/` van los scripts de Flyway. En `src/test/` están los tres niveles de prueba previstos: unitarios de servicio, de repositorio y de integración.

**`frontend/`** — Aplicación React. La carpeta `src/` está organizada por responsabilidad: `assets` para los archivos estáticos, `components/ui` para los componentes base compartidos, `constants` para rutas, roles, límites de archivo y variables de entorno, `hooks` y `store` para la lógica y el estado reutilizables, `layouts` para los layouts compartidos por sección, `pages` con una carpeta por área o rol, `router` para la definición de rutas, `services` para toda la comunicación con el backend, `styles` para el CSS global, `tokens` para el Design System, `types` para los tipos y DTOs compartidos, `utils` para funciones puras y `validations` para las reglas y mensajes de error.

**`Docs/`** — Documentación del proyecto: especificación de requisitos, especificación de interfaces de frontend, notas de arquitectura, los README de backend y de frontend, y la carpeta `Docs/Diagramas/` con los diagramas de actividades y de los flujos principales del proceso.

---

## Cómo levantar el frontend

Hace falta Node 24 y npm.

Desde la carpeta `frontend`: primero se crea el archivo `.env` a partir de `.env.example`, que es donde se define la URL del backend. Después se instalan las dependencias y se levanta el servidor de desarrollo, que queda disponible en `http://localhost:5173`.

Las variables de entorno de Vite solo se leen al arrancar el servidor. Si modificás el `.env`, tenés que reiniciar el dev server para que el cambio tome efecto.

Desde la raíz del proyecto, los mismos pasos están encapsulados en el Makefile.

---

## Makefile

En la raíz hay un Makefile que envuelve los pasos de trabajo. Solo hay tareas de frontend, porque el backend todavía no existe.

- `make help` lista las tareas disponibles. Es la tarea por defecto, así que correr `make` a secas muestra la ayuda.
- `make install` instala las dependencias. Usa `npm ci`, que respeta el lockfile y por lo tanto es reproducible.
- `make dev` levanta el servidor de desarrollo con recarga en caliente.
- `make build` compila TypeScript y genera el build de producción.
- `make lint` corre oxlint.
- `make preview` sirve localmente el build de producción.
- `make clean` borra el directorio de build. Es el único target que no envuelve un script de npm.
- `make down` detiene el stack de contenedores. Hoy no hace nada porque todavía no hay `docker-compose.yml`: avisa por pantalla en lugar de fallar, y pasa a hacer el teardown real de Docker Compose en cuanto ese archivo exista.

---

## Convenciones de git

El proyecto trabaja sobre `main` como rama de integración. Es la rama en la que vive todo el trabajo.

Los mensajes de commit llevan un prefijo que indica el tipo de cambio:

- `ADDED:` — funcionalidad nueva.
- `FIX:` — corrección de bug.
- `HOTFIX:` — corrección urgente aplicada directo sobre producción.
- `REFACTOR:` — reorganización que no cambia el comportamiento: mover, renombrar o limpiar.

`backend/` y `frontend/` son independientes entre sí, así que conviene que cada commit toque una sola de las dos, y que los cambios de documentación vengan en commits separados de los de código.

---

## Documentación

- `Docs/Sistema_Equivalencias_Requisitos_v6.md` — Especificación funcional y no funcional, versión 6. Es la fuente de verdad del negocio: actores, módulos, estados del expediente, esquema de tablas y reglas de permisos. Cuando haya una duda sobre qué tiene que hacer el sistema, la respuesta está acá.
- `Docs/frontend-interfaces.md` — Especificación de interfaces: cada pantalla, campo, estado de carga y acción por rol, más los estándares obligatorios de Frontend (tokens, accesibilidad, responsive, validaciones). Es el contrato entre el negocio y la capa de presentación, y es el documento que manda cuando hay que decidir cómo mostrar algo.
- `Docs/ARQUITECTURA_E3.md` — Estructura del monorepo, puertos, flujo de comunicación entre Frontend y Backend y las tres capas.
- `Docs/README_FRONTEND_E3.md` — Guía de trabajo del equipo Frontend: stack, estructura de carpetas, reglas rápidas de estilos y servicios, y la lista de pendientes del lado Frontend.
- `Docs/README_BACKEND_E3.md` — Guía de trabajo del equipo Backend.
- `Docs/Diagramas/` — Diagramas de actividades y de los flujos de análisis preliminar y revisión legal.

---

## Deuda técnica conocida

Todo lo que sigue está detectado, pero no corregido. Conviene atacarlo en commits separados, de a un tema.

### Documentación

- `Docs/README_BACKEND_E3.md` es el problema más grave. Describe herramientas de build, comandos de Docker, seed de datos demo, cobertura mínima y reglas de arquitectura que **no existen en el repositorio**, y además manda a clonar un repositorio distinto. Quien llegue nuevo y lo lea va a perder tiempo buscando cosas que no están.
- `Docs/ARQUITECTURA_E3.md` describe la carpeta de documentación como `docs/` en minúscula y se referencia a sí mismo con otro nombre. En Linux esa diferencia de mayúsculas rompe los enlaces. Además, el mismo archivo referencia tres documentos que no existen: el esquema de base de datos, el catálogo de endpoints y las credenciales de demo.
- `Docs/ARQUITECTURA_E3.md` también describe un stack desactualizado: React 18, Axios y `App.jsx`. Lo que hay en el código es React 19, React Router 8, `main.tsx` y `fetch` nativo sin Axios.
- `Docs/frontend-interfaces.md` declara estar basado en los requisitos versión 5, cuando el documento vigente es la versión 6.
- La lista de notas finales de la especificación de requisitos numera el ítem 8 dos veces. Es cosmético, pero el documento se rotula como versión final.
- La sección 6 de `AGENTS.md` pide verificar `~/CSS/styles.css` y exige ilustraciones en `Assets/new-icons`. Esas rutas no existen acá: son de otro proyecto, y contradicen la regla del propio repositorio de usar lucide como única librería de íconos.

### Esquema de base de datos

- El SQL de la especificación de requisitos está escrito en dialecto MySQL (autoincremento, backticks, claves `UNIQUE KEY`) y el stack es PostgreSQL 15. Hay que traducirlo antes de escribir la primera migración de Flyway.
- La definición de la tabla `usuarios` no incluye la columna de contraseña hasheada, pero el requisito de login del administrador general por usuario y contraseña la necesita. Es un hueco a resolver antes de implementar la autenticación.

### Frontend

- El cliente HTTP autentica por cookie, pero la documentación de arquitectura describe tokens JWT en el almacenamiento local y cabecera `Authorization: Bearer`. Hay que decidir cuál de los dos modelos se implementa y dejar el código y el documento de acuerdo.
- `.env.example` apunta al puerto 3000, y el backend cuando exista va a escuchar en el 8080.

### Git

- La rama `develop` del remoto quedó atrás de `main` y nunca se usó. El flujo de ramas que declara el README del backend no es el que se está siguiendo.
- El `.gitignore` de la raíz cubre Node pero no los artefactos de Java ni el volumen de PostgreSQL. Cuando exista el backend hay que agregar `target`, `*.class` y el directorio de datos, o se van a terminar commiteando binarios.
