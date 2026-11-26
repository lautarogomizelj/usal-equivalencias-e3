# Auditoría de los servicios del backend y del motor de transiciones

2026-11-19 · lautarogomizelj · Estado: vigente. Se revisó la rama `feature/review-auditoria` en el commit `1296283`. Acompaña a la [auditoría del repositorio](auditoria_lautaro.md).

Revisa por lectura de código los servicios de `backend/src/main/java/com/usal/e3/service/`, las reglas de `SecurityConfig` que los protegen y los tests que los cubren. También corre la suite completa (`make backend-test`). Los hallazgos marcados "por lectura" no se reprodujeron con un test. El estado del proyecto está en el [README](../../../README.md#estado-actual).

## Resultado de los tests

Se corrió `make backend-test` el 2026-11-19, dentro de Docker y contra un PostgreSQL descartable: **102 tests, 0 fallos, 0 errores, 0 omitidos (`BUILD SUCCESS`).**

| Clase | Tipo | Tests | Qué cubre |
|-------|------|-------|-----------|
| `TransicionServiceTest` | Unitario | 10 | Reglas del motor: orden de validación, `estadoEsperado`, acciones disponibles y registro de la creación |
| `PermisosServiceTest` | Unitario | 6 | Participante y observador, permisos temporales y por carrera, y roles que no trabajan por carrera |
| `BloqueoCuentaTest` | Unitario | 6 | Contador de fallos y bloqueo de 6 horas |
| `JwtServiceTest` | Unitario | 3 | Firma y lectura del token |
| `GlobalExceptionHandlerTest` | Unitario | 3 | Formato de error del contrato |
| `DocumentosIntegrationTest` | Integración | 14 | Subida, formatos, límites, borradores, envío, cancelación, `estadoEsperado` viejo y visibilidad del personal |
| `SolicitudIntegrationTest` | Integración | 10 | Creación, visibilidad, orden del listado, participante u observador y catálogos |
| `LoginIntegrationTest` | Integración | 21 | Los cuatro logins, bloqueo, sesión, RBAC, Google y CORS |
| `PerfilIntegrationTest` | Integración | 17 | Mi perfil, contraseña, email, sesiones y accesos |
| `RegistroIntegrationTest` | Integración | 12 | Registro, códigos, reenvío y recuperación de contraseña |

El único `ERROR` del log ("Error no controlado en /roto") sale de `GlobalExceptionHandlerTest`, que prueba a propósito el camino de un error inesperado.

## Motor de transiciones

### Cómo funciona hoy

`TransicionService` es una tabla de transiciones en memoria (`TABLA`), con dos operaciones:

- `validar` revisa, en el orden que fija el contrato (§7, CC-009), tres cosas: el rol (403 `ACCESO_DENEGADO`), la participación (403 `NO_PARTICIPANTE`) y el estado (409 `TRANSICION_INVALIDA`). El estado tiene que ser el origen de la transición y además coincidir con `estadoEsperado`.
- `aplicar` cambia el estado y registra el cambio en `estados_solicitud` y en `auditoria`.

`registrarCreacion` deja la creación como primer cambio, con `estadoAnterior` nulo. Las acciones disponibles del detalle salen de la misma tabla, así que la pantalla nunca ofrece una acción que el motor rechace. `SolicitudService` busca la solicitud con `buscarParaActualizar`, que hace un `SELECT ... FOR UPDATE`. Así, de dos pedidos simultáneos solo uno parte del estado esperado.

### Avance contra RF-ESTADO-001

| | Implementado | Total del contrato (§7.1) |
|-|--------------|---------------------------|
| Transiciones | 3: creación, 2 (`SOL-INI` → `DOC-REV`) y 27 (`SOL-INI` → `SOL-CANC`) | 27 |
| Acciones | 3: `CREAR_SOLICITUD`, `ENVIAR_DOCUMENTACION` (solo desde `SOL-INI`) y `CANCELAR_SOLICITUD` | 23 |
| Estados del catálogo | 21, cargados en `V2` y `V5` | 21 |

Se usan 3 estados (`SOL-INI`, `DOC-REV` y `SOL-CANC`). Los otros 18 existen en el catálogo, pero ninguna transición llega a ellos.

### Lo que está bien

- **Una sola puerta para cambiar el estado.** `Solicitud.cambiarEstado` solo lo llama `TransicionService.aplicar`, y el estado se guarda siempre por código (RF-ESTADO-004).
- **Concurrencia resuelta con bloqueo de fila**, sin depender de `estadoEsperado` solamente.
- **Rechazo antes de consultar permisos** cuando el rol no tiene la acción (`sinAccionesDelRolNiSeConsultanLosPermisos`).
- **Trazabilidad completa:** cada cambio deja una fila en `estados_solicitud` y otra en `auditoria`, con IP.
- **Tests:** las reglas del motor tienen 10 tests unitarios (`TransicionServiceTest`) y los recorridos de punta a punta tienen tests de integración (`DocumentosIntegrationTest`), incluido el `estadoEsperado` viejo.

### Hallazgos

| # | Severidad | Hallazgo | Dónde | Qué hacer |
|---|-----------|----------|-------|-----------|
| M1 | Media | **El motor no puede guardar comentarios.** `registrar` siempre pasa `null` como comentario, tanto en `estados_solicitud.comentarios` como en la auditoría. En cambio, `SolicitudService.detalle` busca la "última observación" en ese campo. Las transiciones del Sprint 4 (`RECHAZAR_DOCUMENTACION`, `RECHAZAR_INSCRIPCION`, `RECHAZAR_PAGO`) exigen comentario | `TransicionService.java:66-69` y `:88-97` | Que `aplicar` reciba el comentario y lo pase a `HistorialEstado` y a `registrarCambioEstado` |
| M2 | Media | **Falta elegir el destino según el body.** `validar` toma la primera fila que coincide en acción, rol y origen. `INDICAR_LEGALIZACION` sale de `INSC-ACEP` hacia `LEG-REV` o `LEG-PEND` según `legalizados`, y con la tabla actual siempre ganaría la primera fila | `TransicionService.java:60-63` | Que el destino se elija por el dato del body, o separar esa acción en dos filas con distinto código interno |
| M3 | Media | **No hay notificaciones (RF-ESTADO-002).** Es del Sprint 4. El riesgo de diseño es que `MailService` envía de forma sincrónica dentro de la transacción. Si los avisos de cambio de estado se envían igual, un error de SMTP deshace la transición, y un rollback posterior deja enviado un mail de algo que no pasó. Eso contradice "el trámite no depende de los mails" (`MailService.java:8`) | `MailService.java` | Enviar los avisos después del commit (`@TransactionalEventListener(phase = AFTER_COMMIT)`) y sin propagar el error |
| M4 | Baja | **Falta definir qué puede hacer el Admin.** `puedeActuar` trata al Admin como personal y `esParticipante` le devuelve `false`, así que el Admin nunca puede actuar sobre una solicitud. Hoy ninguna transición es del Admin, pero RF-ADMIN-006 (reabrir) y la reasignación lo van a necesitar | `TransicionService.java:82-86`, `PermisosService.java:42-45` | Ver [Solución de M4](#solución-de-m4) |
| M5 | Media | **El motor no exige ser el responsable.** Alcanza con ser participante. Con dos académicos con permiso sobre la carrera, el que no tomó el proceso igual podría aceptar o rechazar. Los requisitos y las interfaces piden que actúe solo el responsable | `TransicionService.java:56-58` | Ver [Solución de M5](#solución-de-m5) |
| M6 | Baja | **Sin test de concurrencia.** El bloqueo de fila está, pero ningún test dispara dos acciones a la vez para comprobar que solo una pasa | `SolicitudRepository.java:15-18` | Un test de integración con dos hilos sobre la misma solicitud |

### Solución de M5

Decisión de lautarogomizelj, 2026-11-19: para actuar sobre una solicitud, el personal tiene que ser participante y además el responsable del rol de esa etapa. Si es participante pero no es el responsable, el backend responde un código nuevo, `NO_RESPONSABLE`.

#### La regla y de dónde sale

| Regla | Fuente |
|-------|--------|
| Hay un solo responsable académico y uno solo administrativo por solicitud | RF-PROC-003, que asigna "`responsable_academico` o `responsable_administrativo` (según rol)". El esquema lo garantiza con dos columnas sueltas (`V4__solicitudes.sql:13-14`) |
| Actúa el responsable: el académico en las etapas académicas y el administrativo en las de Secretaría | RF-PROC-003 (línea 632), RF-ACAD-009 (línea 593, "el Académico responsable"), RF-PROC-002 (línea 602, "académico asignado"), `frontend-interfaces.md` §5.0 (línea 1602) y §6.0 (línea 1916) |
| Si no hay responsable, la única acción es "Tomar proceso" | `frontend-interfaces.md` §5.0 (línea 1602) |
| Cualquier participante del rol libre puede tomar el proceso, en cualquier estado. Tomarlo no cambia el estado | RF-PROC-003 (sin condición de estado), RF-ESTADO-002 (línea 489, el aviso de creación ya trae "[Tomar proceso]"), contrato §7.2 ("Sin responsable de su rol"), `frontend-interfaces.md` §6.4.1 (Secretaría lo puede tomar "En etapa académica") |
| Si el responsable se da de baja o le revocan el permiso, el campo vuelve a `NULL` y el proceso queda libre para su rol, sin asignarse solo | RF-PROC-004, RF-ADMIN-004, `frontend-interfaces.md` §4.10 |
| El único con acceso a la carrera no puede darse de baja (`UNICO_RESPONSABLE`) | RF-PROC-004 |

Solo una frase de los requisitos contradice la regla: el final de RF-PROC-002 (línea 615), que habilita los botones "solo si el usuario es PARTICIPANTE (RF-AUTH-005) y el estado actual lo permite", sin mencionar al responsable. El contrato §10.1 dice lo mismo ("Si es participante"). Los dos se corrigen al implementar esta solución.

#### Cambios de código

1. **`ErrorCode`.** Agregar `NO_RESPONSABLE(HttpStatus.FORBIDDEN, "Tenés que ser el responsable de este proceso para actuar.")`. Cubre los dos casos: que no haya responsable y que el responsable sea otro.
2. **`Solicitud`.** Agregar `esResponsable(Cuenta cuenta)`, junto a `esPropiaDe`. Devuelve `true` si la cuenta es el `responsableAcademico` y su rol es `ACAD`, o si es el `responsableAdministrativo` y su rol es `SECRE`.
3. **`TransicionService.validar`.** Agregar un paso entre la participación y el estado:

   ```java
   if (!puedeActuar(actor, solicitud)) {
       throw new ApiException(ErrorCode.NO_PARTICIPANTE);
   }
   if (actor.tipo() == TitularTipo.USUARIO && !solicitud.esResponsable(actor)) {
       throw new ApiException(ErrorCode.NO_RESPONSABLE);
   }
   ```

   La tabla ya filtra por `rolActor`, así que el rol de quien actúa es el rol de la etapa. Un académico responsable que intenta una transición de Secretaría sigue recibiendo `ACCESO_DENEGADO`.
4. **`TransicionService.accionesDisponibles`.** Aplicar la misma condición, para que el detalle no ofrezca acciones a quien no es responsable. "Tomar proceso" no sale de esta tabla, porque no es una transición (contrato §7.2).
5. **El aspirante no cambia.** Sigue actuando sobre sus propias solicitudes, sin responsable.

El orden de validación pasa a ser:

1. Rol: 403 `ACCESO_DENEGADO`.
2. Participación: 403 `NO_PARTICIPANTE`.
3. Responsable: 403 `NO_RESPONSABLE`.
4. Estado: 409 `TRANSICION_INVALIDA`.

#### Tests

Se agregan a `TransicionServiceTest` cuando exista la primera transición del personal (`ACEPTAR_DOCUMENTACION`, Sprint 4). Hoy la tabla solo tiene transiciones del aspirante.

| Caso | Resultado esperado |
|------|--------------------|
| Académico participante y responsable | La transición se aplica |
| Académico participante, con otro académico como responsable | `NO_RESPONSABLE` y `accionesDisponibles` vacía |
| Académico participante, sin responsable asignado | `NO_RESPONSABLE` y `accionesDisponibles` vacía |
| Responsable cuyo permiso se revocó | `NO_PARTICIPANTE`, porque la participación se valida antes |
| Responsable con un `estadoEsperado` viejo | `TRANSICION_INVALIDA` |
| Académico responsable que intenta una transición de Secretaría | `ACCESO_DENEGADO` |
| Transiciones del aspirante | Igual que hoy |

#### Cuándo implementarla

En el Sprint 4, junto con "Tomar proceso" (RF-PROC-003) y "Darse de baja" (RF-PROC-004). Hoy no hay ningún mecanismo que asigne un responsable: `Solicitud` no tiene cómo escribir esas columnas y ningún endpoint las toca. Si la regla entrara antes, ningún académico podría actuar.

"Tomar proceso" tiene que bloquear la fila con `buscarParaActualizar` y responder `PROCESO_YA_TOMADO` si el rol ya tiene responsable. Así se cumple el cierre del Hito 2: si dos académicos lo toman a la vez, solo uno queda asignado. `PROCESO_YA_TOMADO` y `UNICO_RESPONSABLE` ya están en `ErrorCode.java:17-18`, sin uso.

#### Documentos a actualizar al implementarla

| Documento | Cambio |
|-----------|--------|
| `Sistema_Equivalencias_Requisitos.md` | RF-PROC-002, línea 615: "si el usuario es PARTICIPANTE, es el responsable del rol de la etapa y el estado actual lo permite" |
| `Contratos API REST.md` | Tabla de errores: `NO_RESPONSABLE` (403). §7: el orden de validación con el paso nuevo. §10.1: "Si es participante y responsable" en las transiciones académicas y de pago |
| `GESTION_CAMBIOS.md` | Un cambio nuevo para la regla del responsable y `NO_RESPONSABLE` |

Cada documento suma su fila de control de versiones.

#### Lo que queda abierto

- **Reasignación entre el Sprint 4 y el 8.** La reasignación del Admin (RF-ADMIN-005) es del Sprint 8. Si el único responsable posible no puede darse de baja (`UNICO_RESPONSABLE`), el proceso queda trabado hasta entonces. Esto refuerza el punto 4 de la [auditoría del repositorio](auditoria_lautaro.md#4-orden-de-los-sprints-permisos-del-admin-antes-de-la-bandeja).
- **Alerta de "sin responsable administrativo" (RF-ADMIN-007).** Como Secretaría puede tomar el proceso en cualquier momento, la alerta se dispararía para cada solicitud nueva durante toda la etapa académica. Hay que definir desde qué estado corresponde. La propuesta es desde `CUPON-SOL`.

### Solución de M4

Decisión de lautarogomizelj, 2026-11-19: el Admin puede ejecutar las transiciones del Académico y de Secretaría en cualquier solicitud, sin ser participante ni responsable. Es el "bypass de permisos" de `frontend-interfaces.md` §7.0 (línea 2019) y coincide con §3.4 de los requisitos ("Permisos: TODOS"). Las transiciones del aspirante no entran: el Admin no actúa en nombre del aspirante.

#### Cambios de código

1. **`TransicionService`.** Hoy la tabla filtra por `rolActor` igual al rol de quien actúa, así que el Admin no encuentra ninguna fila y recibe `ACCESO_DENEGADO`. El filtro pasa a ser:

   ```java
   private static boolean puedeEjecutar(Transicion transicion, Cuenta actor) {
       return transicion.rolActor().equals(actor.rolCodigo())
               || (Rol.ADMIN.equals(actor.rolCodigo()) && !Rol.ASPIRANTE.equals(transicion.rolActor()));
   }
   ```

   Lo usan `validar` y `accionesDisponibles`.
2. **`validar`.** Para el Admin se saltean la participación y el responsable (M5). El estado se sigue validando igual, con `estadoEsperado`.
3. **Sin cambios en la auditoría.** El cambio queda registrado con el Admin como actor, en `estados_solicitud` y en `auditoria`. Ejecutar una acción no lo convierte en responsable. Para eso está la asignación de RF-ADMIN-005.
4. **`relacion`.** Ya devuelve `ADMIN` (`SolicitudService.java:172`), así que el frontend sabe que tiene que mostrarle el Panel de Acción.

#### Tests

| Caso | Resultado esperado |
|------|--------------------|
| Admin sin permiso de carrera ejecuta una transición académica | La transición se aplica y queda registrada con el Admin como actor |
| Admin ejecuta una transición de Secretaría, con otro usuario como responsable | La transición se aplica y el responsable no cambia |
| Admin intenta una transición del aspirante | `ACCESO_DENEGADO` |
| Admin con un `estadoEsperado` viejo | `TRANSICION_INVALIDA` |
| `accionesDisponibles` para el Admin | Las del Académico y de Secretaría del estado actual |

#### Cuándo implementarla y qué documentos cambian

Junto con M5, en el Sprint 4. Además de los documentos de M5, en el contrato §10.1 las celdas "A definir" del Admin pasan a "Sí, sin ser participante ni responsable (interfaces §7.0)".

Esto alivia en parte la traba entre el Sprint 4 y el 8. Si el responsable no está, el Admin puede hacer avanzar el trámite aunque todavía no pueda reasignarlo.

## Servicios

### Resumen

| Servicio | Responsabilidad | Requisitos | Tests | Estado |
|----------|-----------------|------------|-------|--------|
| `TransicionService` | Motor de transiciones | RF-ESTADO-001, 003 | Unitarios e integración | 2 de 27 transiciones. Ver el motor |
| `SolicitudService` | Crear, listar, detalle, historial, enviar y cancelar | RF-ASP-001, 002, 003, 010 | Integración | Completo para el Sprint 3 |
| `DocumentoService` | Subir, quitar, listar y descargar documentos | RF-ASP-002, RF-ACAD-009 | Integración | Completo para `SOL-INI`. Hallazgos D1 a D3 |
| `ArchivoService` | Archivos en disco | RF-ACAD-009 | Indirectos | Sin hallazgos |
| `PermisosService` | Participante u observador | RF-AUTH-005 | Unitarios | Completo. Lo cambian CC-001 a CC-005 |
| `AuthService` | Registro y los cuatro logins | RF-AUTH-001 a 004 | Integración | Completo. Hallazgos A1 y A2 |
| `BloqueoCuenta` | Bloqueo de 6 horas por 3 fallos | RF-AUTH-002 | Unitarios e integración | Hallazgo A1 |
| `CodigoService` | Códigos de 6 dígitos | RF-AUTH-001 | Integración | Hallazgo A1 |
| `SesionService` | Sesión JWT con fila en `sesiones` | RF-AUTH-007, CC-007 | Integración | Sin hallazgos |
| `PerfilService` | Mi perfil | RF-AUTH-007 | Integración | Hallazgo P1 |
| `CuentaService` | Resolver la cuenta y desbloquear | RF-AUTH-002 | Integración | Sin hallazgos |
| `AuditoriaService` | Traza de eventos | RF-AUDIT-001 | Indirectos | Ver M1 |
| `CatalogoService` | Carreras y estados | Contrato §5 | Integración | Hallazgo C1 |
| `MailService` | Mails de códigos | RF-AUTH-001 | Indirectos (Mailpit) | Ver M3 |
| `SeedDemo` | Cuentas demo | README, "Datos Demo" | — | Cambia con el login con contraseña del personal ([auditoría del repositorio](auditoria_lautaro.md), punto 5) |

Se respeta la regla de arquitectura del README: los controllers llaman solo a services, y la validación de `revoked_at IS NULL` vive solo en `PermisosService` y sus repositorios.

### `SolicitudService`

Crea la solicitud en `SOL-INI`, sin responsable y con el código `EXP-AAAA-NNN` sacado de una secuencia. Antes exige que el email esté validado y que la carrera esté activa. Visibilidad: el aspirante ve solo las suyas y el personal ve todas (acceso básico). `relacion` resuelve el modo de la pantalla.

- **S1 (baja, por lectura).** `delAspirante` trae todas las solicitudes del aspirante y pagina en memoria (`SolicitudService.java:87-99`). Para el aspirante alcanza, porque tiene pocas. La bandeja del Sprint 4 no puede copiar este patrón y tiene que paginar en la base.
- **S2 (baja, por lectura).** Cada detalle llama a `catalogo.estadosPorCodigo()`, que lee los 21 estados de la base cada vez (`SolicitudService.java:161`). No es un problema con el volumen actual.

### `DocumentoService` y `ArchivoService`

Reconoce el formato por el contenido (`%PDF` y `FFD8FF`), no por el nombre. Limita a 10 MB por archivo y 50 MB entre todos, como `spring.servlet.multipart`. Los borradores los ve solo el aspirante, y lo enviado ya no se quita. La ruta en disco se arma con año, DNI (validado como 7 u 8 dígitos) y código de expediente, más un UUID, así que el nombre que manda el usuario no entra en la ruta.

| # | Severidad | Hallazgo | Dónde |
|---|-----------|----------|-------|
| D1 | Media | **Archivos borrados antes del commit.** `quitar` y `descartarBorradores` borran el archivo del disco dentro de la transacción. Si la transacción se deshace después, por ejemplo si falla `transiciones.aplicar` al cancelar, la fila del documento vuelve pero el archivo ya no existe, y la descarga falla | `DocumentoService.java:107-108`, `:168-171` |
| D2 | Baja | **Archivos huérfanos.** `subir` escribe el archivo antes de guardar la fila. Si el `save` falla, queda un archivo sin documento | `DocumentoService.java:85-95` |
| D3 | Baja | **Subir en un estado que no corresponde devuelve 409 `TRANSICION_INVALIDA`.** Subir no es una transición, así que el código puede confundir al frontend. Pasa lo mismo con quitar un documento ya enviado | `DocumentoService.java:69-71`, `:104-106` |

D1 y D2 se resuelven igual: borrar o confirmar el archivo después del commit (`TransactionSynchronization.afterCommit`).

### `AuthService`, `BloqueoCuenta` y `CodigoService`

Registro con código por mail, login de aspirante, login de Admin, login con Google y login simulado. Los errores de login usan `noRollbackFor`, para que el intento fallido quede guardado. La fila de la cuenta se bloquea durante el login, así que dos intentos simultáneos no pierden un fallo. Los emails se normalizan a minúscula en los controllers. Recuperar la contraseña y reenviar el código no revelan si el email existe. Restablecer la contraseña cierra todas las sesiones.

| # | Severidad | Hallazgo | Dónde |
|---|-----------|----------|-------|
| A1 | Media (por lectura) | **Dos bloqueos comparten el mismo campo `bloqueado_hasta`.** Tres códigos malos bloquean 15 minutos (`CodigoService.java:69-71`). Si el aspirante intenta entrar en ese lapso, `BloqueoCuenta.verificar` responde "Tu cuenta está bloqueada por 6 horas", con un `bloqueadoHasta` 15 minutos adelante (`BloqueoCuenta.java:21-23`). Pasa sobre todo en la recuperación de contraseña de una cuenta ya validada. Además, una vez vencido ese bloqueo, el login lo limpia junto con el contador de fallos de login | `BloqueoCuenta.java:19-28`, `CodigoService.java:65-75` |
| A2 | Baja | **El login simulado no normaliza el email.** `loginDesarrollo` busca el email tal como llega, mientras que `loginGoogle` lo pasa a minúscula. Queda sin efecto si se elimina el login simulado, como se decidió en la auditoría del repositorio | `AuthService.java:131-137` |

### `SesionService` y `CuentaService`

La sesión es un JWT con el id de una fila de `sesiones`, validada en cada pedido. Cerrar sesión, una sesión vencida o una cuenta desactivada cortan el acceso al instante. `ultimoUso` se actualiza como mucho una vez por minuto, para no escribir en cada pedido. Cambiar la contraseña cierra las otras sesiones. No hay hallazgos.

**Nota para cuando se publique el sistema.** Las IP de sesiones y auditoría salen de `request.getRemoteAddr()`. Detrás de un proxy inverso todas van a ser la IP del proxy, salvo que se configure `server.forward-headers-strategy`.

### `PerfilService`

Los campos que se pueden editar dependen del rol: el Revisor no edita nada, el personal con Google no cambia nombre ni apellido, y solo el Admin cambia el email. `SecurityConfig` refuerza lo mismo por ruta. El avatar se valida con el patrón `(hombre|mujer)-NN`.

- **P1 (baja, por lectura).** `cambiarPassword` y `cambiarEmail` no cuentan los fallos de `passwordActual` (`PerfilService.java:104-108`). Con una sesión robada se puede probar contraseñas sin límite. Lo mitigan que hace falta la sesión y que BCrypt es lento.
- **Cruce con la auditoría del repositorio.** Si el personal pasa a entrar con contraseña (punto 5), `esOAuth()` ya no puede seguir significando "no es Admin" (`Usuario.java:60-62`). De ese método dependen `camposEditables`, `editar`, `loginGoogle` y `loginDesarrollo`. Además, `SecurityConfig` solo deja cambiar la contraseña al aspirante y al Admin (`SecurityConfig.java:62-63`).

### `PermisosService`

Implementa la lógica de RF-AUTH-005: primero el permiso temporal vigente sobre la solicitud, después el permanente sobre la carrera, y si no hay ninguno, observador. `revoked_at IS NULL` está siempre en las consultas. Solo Académico y Secretaría trabajan por carrera. No hay hallazgos, pero es el servicio que más cambia con CC-001 a CC-005 (facultades y niveles de acceso por carrera).

### `CatalogoService`

- **C1 (baja, por lectura).** `Estado` tiene un campo `activo`, pero `CatalogoService.estados()` devuelve todos sin filtrar. Hoy no importa, porque `V2` y `V5` no dan de baja ningún estado.

## Seguridad transversal

| Tema | Estado |
|------|--------|
| RBAC por ruta | Correcto para lo implementado. `GET /solicitudes` es solo del aspirante hasta el Sprint 4 |
| CSRF | Desactivado (`SecurityConfig.java:31`). En la auditoría del repositorio se decidió activarlo ahora |
| Login simulado | `DevAuthController` existe solo con el perfil `dev` (`@Profile("dev")`), aunque `/auth/dev/**` figura como `permitAll` en todos los perfiles. Se elimina según la auditoría del repositorio |
| Errores | `GlobalExceptionHandler` devuelve siempre el formato del contrato. Lo inesperado se loguea y responde `ERROR_INTERNO` sin detalles |
| Duplicados por concurrencia | Los frena el `UNIQUE` y se responden como `DUPLICADO` |

## Prioridades

| Prioridad | Hallazgos | Por qué |
|-----------|-----------|---------|
| Antes del Sprint 4 | M1, M3, M4, M5 | El Sprint 4 agrega los rechazos con comentario, los mails de cada cambio de estado, la bandeja con varios académicos por carrera y las primeras transiciones del personal |
| Cuando aparezca la acción | M2 | Bloquea `INDICAR_LEGALIZACION` (Sprint 7) |
| Cuando se pueda | D1, D2, A1, M6 | Inconsistencias poco probables, pero difíciles de diagnosticar si pasan |
| Menores | S1, S2, D3, P1, C1, A2 | Sin impacto con el volumen y el alcance actuales |
