# Auditoría del repositorio · Lautaro

2026-11-19 · lautarogomizelj · Estado: vigente. Las decisiones están tomadas y ninguna está implementada todavía. Se revisó la rama `feature/review-auditoria` en el commit `1296283`.

Es la revisión del repositorio después del Sprint 3. Cada punto tiene la decisión, lo que se encontró en el código o en la documentación y lo que hay que tocar para cumplirla. El estado del proyecto está en el [README](../../../README.md#estado-actual).

## Resumen

| # | Tema | Decisión | Estado |
|---|------|----------|--------|
| 1 | Acceso al login de Admin (tema 1 de `TEMAS_PROFESOR.md`) | Se queda en `/login-admin`. Se saca el enlace "¿Sos administrador? Ingresá acá" de `/login-personal` | Resuelto, falta sacar el enlace |
| 2 | Redirección a `/login` después de verificar el correo | `/login` no debería existir, porque el login del aspirante está en `/` | A implementar |
| 3 | Escala de la aplicación | Se ve demasiado grande: hay que poner el navegador al 75% para verla bien | A implementar |
| 4 | Orden de los sprints | La parte del panel de Admin que da acceso a facultades y carreras va antes de la bandeja del Sprint 4 | A planificar |
| 5 | Login del personal mientras no haya Google | Académico y Secretaría entran en `/login-personal` con usuario y contraseña. Se saca el bloque de cuentas demo | A implementar |
| 6 | CC-001 a CC-005 (Sprint 1) | Se aprueban y se desarrollan. El Admin carga facultades y carreras y, al dar de alta al personal, asigna un nivel de acceso por carrera | Aprobado |
| 7 | Credenciales de Google (Sprint 2) | No bloquean | Sin cambios |
| 8 | Ilustraciones de avatar (Sprint 2) | No bloquean. Se piden 10 avatares a Claude Design, en otra conversación | Pendiente externo |
| 9 | CC-007: sesión con JWT | Se aprueba | Aprobado |
| 10 | CC-008: agregados del Sprint 2 | Se aprueban `NO_AUTENTICADO` y `fecha_ultimo_intento_fallido`. Se rechaza el login simulado. Se saca el puerto 5273 de CORS. CSRF se activa ahora | Aprobado en parte |
| 11 | Ramas y flujo de git | Borrar las ramas que no sean `main` ni `develop`, poner `develop` al día y sacar de ahí las ramas nuevas, como dice el README | Ramas viejas borradas. Falta poner `develop` al día |

## 1. Acceso al login de Admin

Es la respuesta al tema 1 de [`TEMAS_PROFESOR.md`](../../TEMAS_PROFESOR.md).

**Respuesta de lautarogomizelj, 2026-11-19. Tema resuelto.** El Admin entra desde `/login-admin` y no se cambia nada más. Se saca el enlace "¿Sos administrador? Ingresá acá" de `/login-personal`, porque la pantalla de Admin no necesita un botón para llegar: el Admin escribe la dirección.

**Dónde está.**

- El enlace está en `frontend/src/pages/public/LoginPersonalPage.tsx:37-42`.
- `frontend-interfaces.md` §3.6 (línea 999) lo describe como parte de la pantalla.
- CC-011 lo registra como decisión provisoria.

**Al implementarlo.** Hay que sacar el enlace, sacar la línea de §3.6 de `frontend-interfaces.md` (con su fila de versión) y actualizar la descripción de CC-011 en `GESTION_CAMBIOS.md`.

## 2. Redirección a `/login` después de verificar el correo

**Problema.** Después de crear la cuenta de aspirante y verificar el correo, la aplicación lleva a `/login`. Esa pantalla no debería existir, porque el login del aspirante está integrado en el inicio (`/`).

**Dónde está.** El diseño integra el login en el inicio, y así se decidió en el Anexo B, punto 14, de `frontend-interfaces.md`. Aun así quedó la ruta separada:

| Uso de `ROUTES.LOGIN` | Archivo |
|-----------------------|---------|
| Definición de la ruta `/login` | `frontend/src/constants/routes.ts:4`, `frontend/src/router/index.tsx:27` |
| Redirección después de verificar el correo | `frontend/src/pages/public/VerificarCorreoPage.tsx:20` |
| Redirección después de recuperar la contraseña | `frontend/src/hooks/useRecuperarContrasenia.ts:57` |
| Enlace "ya tenés cuenta" del registro | `frontend/src/pages/public/RegistroPage.tsx:145` |
| Pantalla `LoginPage` | `frontend/src/pages/public/LoginPage.tsx` |

**Qué hay que tener en cuenta.** El backend vuelve a `/login?error=...` cuando falla el login con Google (`backend/src/main/java/com/usal/e3/security/GoogleLoginHandler.java:53` y `:61`), y `LoginPage.tsx:27` reenvía ese error a `/login-personal`. Si se borra `/login`, ese retorno tiene que apuntar directo a `/login-personal`, ya sea cambiando el backend o dejando en `/login` solo la redirección. Eso cambia el contrato de la API, §4.1.

## 3. Escala de la aplicación

**Problema.** La aplicación se ve demasiado grande. Para verla bien hay que poner el navegador al 75%.

**Causa probable.** En pantallas de 1024 px o más, el tamaño base de letra del `html` crece con el ancho y el alto de la ventana. Como todo el sistema está en `rem`, la pantalla entera crece con él:

| Token | Valor | Dónde se aplica |
|-------|-------|-----------------|
| `--escala-auth` | `clamp(0.6875rem, min(1.0417vw, 1.5625vh), 1.25rem)` | Pantallas de acceso (`frontend/src/tokens/sizing.css:31`) |
| `--escala-inicio` | `min(1.0417vw, 1.5625vh)`, sin tope | Inicio (`sizing.css:43`) |
| `--escala-privada` | `clamp(0.6875rem, min(0.8333vw, 2.2vh), 1.25rem)` | Área privada (`sizing.css:94`) |

Las reglas están en `frontend/src/styles/global.css:22-37`. En un monitor de 1920 × 1080 la base queda en unos 16-17 px, pero en uno más grande llega a 20 px, un 125%. En el inicio no tiene tope. Además, varios textos suben de tamaño desde `lg:`, por ejemplo `lg:text-xl` en `LoginPersonalPage.tsx`.

**Al implementarlo.** Hay que bajar o eliminar el escalado dinámico y dejar el diseño en 100% de zoom como referencia. Se verifica en 1366 × 768, 1920 × 1080 y 2560 × 1440.

## 4. Orden de los sprints: permisos del Admin antes de la bandeja

**Problema.** Según el Sprint Planning, el panel de Admin (usuarios, carreras y permisos) es del Sprint 8 (1 al 12 de febrero). El Sprint 4 (30 de noviembre al 11 de diciembre) construye la bandeja y el detalle del Académico y de Secretaría, y "Tomar proceso" depende de los permisos por carrera (`user_career_permissions`). Sin una forma de dar esos permisos, la bandeja solo se puede probar con datos sembrados a mano.

**Decisión.** Antes de la bandeja se hace la parte del perfil de Admin que da acceso a una facultad o carrera. Lo mínimo:

- el catálogo de facultades y carreras (ver el punto 6);
- el alta del personal con sus permisos por carrera.

El resto del panel de Admin (alertas, reasignación, auditoría) sigue en el Sprint 8.

**Al implementarlo.** Hay que mover RF-ADMIN-001 y RF-ADMIN-003 (y RF-ADMIN-002 si incluye el catálogo de carreras) del Sprint 8 al Sprint 4, o a un sprint intermedio, en el Sprint Planning.

## 5. Login del personal con usuario y contraseña hasta tener Google

**Decisión.** Mientras no esté el login con Google, Académico y Secretaría entran en `/login-personal` con un login real, de usuario y contraseña, sobre usuarios reales:

- La pantalla avisa que el ingreso con Google todavía no está disponible.
- Se saca de esa pantalla el bloque "Solo en desarrollo: ingresar con una cuenta demo".
- Funciona así hasta que el sistema se publique en un servidor. Antes no tiene sentido configurar Google.

**Dónde está.**

- El bloque de cuentas demo está en `frontend/src/pages/public/LoginPersonalPage.tsx:23-34`, sobre `POST /auth/dev/login` (`DevAuthController.java:28`).
- `frontend-interfaces.md` §3.6 (línea 997) y `Docs/demo-credentials.md` lo documentan.
- `BLOQUEOS.md`, punto 2, lo da como la forma de trabajar mientras tanto.

**Qué contradice.** RF-AUTH-003 dice que Académico, Secretaría y Revisor entran solo con Google. En la base, `usuarios.password_hash` figura como "solo Admin" (`V1__initial.sql:25`). Hace falta un cambio de requisito nuevo en `GESTION_CAMBIOS.md` que deje el ingreso con contraseña como provisorio.

## 6. Sprint 1: CC-001 a CC-005

**Decisión.** Se aprueban CC-001 a CC-005 y se desarrollan tal como están planteados.

**CC-001 en detalle.**

- El panel del Admin tiene una sección para cargar todas las facultades de la universidad y las carreras de cada una.
- Al dar de alta a una persona del personal con su cuenta `@usal.edu.ar`, el Admin indica a qué facultades puede entrar.
- Dentro de cada facultad, indica para cada carrera qué nivel de acceso tiene.

**Nombres de los niveles.** Lautaro los llamó "nulo, observador o editor", sin que sean los nombres oficiales. En la documentación actual se llaman así:

| Nivel según Lautaro | Nombre en la documentación | Qué permite |
|---------------------|----------------------------|-------------|
| Nulo | No existe hoy. El acceso básico deja ver todas las solicitudes (RF-AUTH-005). Este nivel aparece con CC-003 | No ve los expedientes de esa carrera |
| Observador | **Observador** (§3.6 de los requisitos), u "observar" en CC-001 | Ve los expedientes sin participar (botones deshabilitados) |
| Editor | **Participante** (RF-AUTH-005), o "modificar" en CC-001 | Participa: toma el proceso y actúa sobre la solicitud |

Hay que elegir un solo juego de nombres al implementar CC-001 y usarlo en todos los documentos.

**Qué cambia.**

- Hoy no existe una tabla de facultades: `facultad` es un texto dentro de `carreras` (`V1__initial.sql:50`, `V2__catalogos.sql:32-35`).
- Las tablas de permisos no tienen `permission_level` (requisitos, notas 7 y 26), así que el nivel por carrera cambia el esquema y la lógica de validación de RF-AUTH-005.
- CC-012 ya pide que el alta del personal pida nombre y apellido por separado.

## 7. Sprint 2

### Credenciales de Google

No bloquean. Como se decidió en el punto 5, el personal entra con usuario y contraseña hasta que se publique el sistema. El punto 2 de [`BLOQUEOS.md`](../../BLOQUEOS.md) sigue pendiente. Al implementar el punto 5 cambia su columna "Cómo se trabaja mientras tanto".

### Ilustraciones de avatar

No bloquean. Se piden a Claude Design en una conversación aparte, no en Claude Code: 10 avatares distintos, para todos los usuarios y con el contexto de la universidad.

**Contradicción a resolver.** RF-AUTH-007 pide "galerías por género", y el backend valida claves con género (`hombre-02`, `mujer-03`, según `BLOQUEOS.md`, punto 3). Una galería única de 10 avatares para todos cambia ese requisito y esa validación.

Pedido para Claude Design:

```text
Diseñá 10 avatares ilustrados, distintos entre sí, para los usuarios de un sistema web
de equivalencias académicas de la Universidad del Salvador (USAL, Buenos Aires).
Los usan aspirantes, profesores y personal administrativo. Estilo plano y sobrio,
coherente entre los 10, con fondo de color liso, legibles en círculo a 32 px y a 128 px.
Variedad de edades, rasgos y peinados, sin logos ni texto. Entregá cada uno como SVG
cuadrado, con un nombre de archivo numerado (avatar-01.svg … avatar-10.svg).
```

### CC-007: sesión con JWT

Se aprueba la sesión como un JWT firmado en una cookie `HttpOnly`. CC-007 pasa a `Aprobado`.

### CC-008: agregados del Sprint 2

| Agregado | Decisión | Detalle |
|----------|----------|---------|
| Códigos `NO_AUTENTICADO` y `ACCESO_DENEGADO` | Aprobado | Sin cambios |
| Columna `fecha_ultimo_intento_fallido` | Aprobado | Sin cambios |
| Login simulado `POST /auth/dev/login` | Rechazado | Se reemplaza por el login real con usuario y contraseña del punto 5. Se sacan los botones de cuentas demo |
| CORS para el puerto 5273 | Se saca | Ver la explicación debajo |
| CSRF sin activar | Se activa ahora | Hoy está desactivado (`SecurityConfig.java:31`). `client.ts` tiene que enviar el header |

**Por qué se saca el puerto 5273.** El backend autoriza `http://localhost:5273` como origen en tres lugares:

- `backend/src/main/resources/application.yml.example:55`
- `backend/src/test/java/com/usal/e3/integration/LoginIntegrationTest.java:275`
- `Docs/Contratos API REST.md:29`

En el repositorio nada levanta un frontend en ese puerto. Vite usa el 5173, ni el `Makefile` ni `docker-compose.yml` mencionan el 5273, y el README documenta solo el 5173. Sacarlo deja en la lista de CORS solo los orígenes que el proyecto usa. Quien lo necesite en su máquina puede agregarlo con `CORS_ALLOWED_ORIGINS` en su entorno, sin dejarlo fijo para todos.

## 8. Ramas y flujo de git

**Problema.** El [README](../../../README.md#convenciones-de-git) define que `feature/*` y `fix/*` se mergean en `develop`, y que `develop` pasa a `main` en cada release. En la práctica, todo se trabaja contra `main`:

- Los cuatro pull requests del repositorio (#1 a #4) apuntan a `main`.
- `origin/develop` sigue en el commit inicial (`cb64e69`, 2026-09-25) y está 61 commits detrás de `main`. Nunca se usó.
- Los seis commits del Sprint 3 y de CC-012 (`7cd2d9d` a `1296283`) se subieron directo a `main`, sin pull request.
- `feature/review-auditoria`, la rama de esta revisión, también salió de `main`.

**Ramas remotas al revisar** (después de `git fetch --prune`):

| Rama | Último commit | ¿Está en `main`? | Qué se hizo |
|------|---------------|------------------|-------------|
| `main` | 2026-11-17 | — | Se queda |
| `develop` | 2026-09-25 (commit inicial) | Sí, es ancestro de `main` | Se queda. Falta ponerla al día con `main` |
| `feature/sprint-1` | 2026-11-02 | Sí (PR #3 y #4) | **Borrada el 2026-11-19** |
| `frontend/setup-inicial` | 2026-09-28 | Sí (PR #1) | **Borrada el 2026-11-19** |
| `feature/contrato-api` | 2026-10-20 | No: tenía 1 commit sin mergear | **Borrada el 2026-11-19** |
| `feature/review-auditoria` | 2026-11-17 | Sí (no tiene commits propios) | Se borra cuando se mergee esta revisión |

`feature/contrato-api` tenía el commit `2590c04`, "sprint planning en `Docs/SPRINT_PLANNING.md` y su enlace en el README": 130 líneas que nunca llegaron a `main`. **Decisión de lautarogomizelj, 2026-11-19:** se descarta, porque es documentación vieja que reemplaza el PDF del Sprint Planning que ya está en `main`. Si alguna vez hiciera falta, el commit se puede buscar por su hash mientras GitHub lo conserve.

Desde el 2026-11-19 las ramas remotas son `main`, `develop` y `feature/review-auditoria`.

**Lo que falta de la limpieza.**

1. Poner `develop` al día con `main`. Es un avance directo: `develop` es ancestro de `main`, así que no se pierde ni se reescribe nada.

   ```bash
   git fetch origin
   git push origin origin/main:develop
   ```

2. Mergear esta revisión y borrar su rama:

   ```bash
   git push origin --delete feature/review-auditoria
   git branch -d feature/review-auditoria
   ```

3. Cada integrante corre `git fetch --prune`, para que las ramas borradas desaparezcan también de su copia local.

Esta revisión se mergea en `main` y no en `develop`, porque la rama salió de `main` antes de que se decidiera el cambio de flujo. Es la última que sigue el flujo viejo.

**De acá en adelante.**

- Toda rama nueva sale de `develop` (`git switch -c feature/<tema> origin/develop`) y su pull request apunta a `develop`.
- `main` solo recibe merges de `develop` en cada release, y `fix/*` urgentes como `HOTFIX:`, según el README.
- Opcional en GitHub: poner `develop` como rama por defecto, para que los pull requests apunten ahí sin elegirla a mano, y proteger `main` para que solo acepte pull requests. Esto último evita que se repita la subida directa del Sprint 3.

## Preguntas abiertas

1. **Revisor en el punto 5.** La decisión nombra a Académico y Secretaría, pero el Revisor también entra con Google (RF-AUTH-003). ¿También entra con usuario y contraseña?
2. **Contraseñas del personal mientras no esté el alta del Admin.** ¿Se siembran con `SEED_DEMO`, o el alta del punto 4 ya pide una contraseña inicial?
3. **Login de Admin y del personal.** Si los dos usan usuario y contraseña, ¿siguen siendo dos endpoints separados (`/auth/login` y `/auth/admin/login`) o el personal reutiliza alguno?
4. **Avatares.** ¿Galería única para todos, o se mantiene la separación por género de RF-AUTH-007?
5. **Nombres de los niveles de acceso** del punto 6.

## Documentos que hay que actualizar al implementar

| Documento | Qué cambia | Puntos |
|-----------|------------|--------|
| `GESTION_CAMBIOS.md` | CC-001 a CC-005 y CC-007 pasan a `Aprobado`. CC-008 se divide en lo aprobado y lo rechazado. Se actualiza la descripción de CC-011. Se agrega un cambio nuevo para el login con contraseña del personal | 1, 5, 6, 9, 10 |
| `Sistema_Equivalencias_Requisitos.md` | RF-AUTH-003 (provisorio), RF-AUTH-005 (niveles por carrera), tablas de permisos y de facultades, RF-AUTH-007 si cambia la galería | 5, 6, 8 |
| `frontend-interfaces.md` | §3.6 sin el enlace a Admin, sin el bloque demo y con el formulario de usuario y contraseña. Login integrado en el inicio, sin `/login` | 1, 2, 5 |
| `Contratos API REST.md` | Línea 29 sin el 5273. §4.1 con el retorno del error de Google. CSRF. Endpoint de login del personal | 2, 5, 10 |
| `BLOQUEOS.md` | El punto 2 se trabaja con el login con contraseña. El punto 3 se pide a Claude Design | 7, 8 |
| `demo-credentials.md` | Contraseñas del personal en lugar de `POST /auth/dev/login` | 5 |
| Sprint Planning | El alta de personal y los permisos del Admin pasan antes de la bandeja | 4 |
| `README.md` | Estado actual y guía de backend (`/auth/dev/login`) | 5, 10 |
