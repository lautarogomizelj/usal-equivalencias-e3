# USAL E3 - Contrato de la API REST

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-10-14 | jumaluc | Versión inicial: convenciones, entidades, endpoints por módulo y acciones del trámite, basada en requisitos v12.0 |
| 1.1 | 2026-10-31 | jumaluc | Sprint 1: `ApiErrorKind` con `conflict` y `locked`, JSON en camelCase y `frontend-interfaces.md` en 20 estados; se quitan de los puntos abiertos |
| 1.2 | 2026-11-02 | jumaluc | Sprint 2: sesión con JWT en cookie, códigos `NO_AUTENTICADO` y `ACCESO_DENEGADO`, login simulado de desarrollo, flujo real de `/auth/google` y errores de perfil (CC-007, CC-008) |
| 1.3 | 2026-11-15 | jumaluc | Sprint 3: lectura de catálogos, creación y lectura de solicitudes, documentos en borrador, `ENVIAR_DOCUMENTACION` desde `SOL-INI`, reglas del motor de transiciones y `fechaCreacion` en `SolicitudResumen` (CC-009) |
| 1.4 | 2026-11-15 | jumaluc | Cancelación del aspirante: estado terminal `SOL-CANC` y acción `CANCELAR_SOLICITUD` (`POST /solicitudes/{id}/cancelar`) desde `SOL-INI`, que elimina los borradores (requisitos v13.0, CC-010) |
| 1.5 | 2026-11-17 | jumaluc | `apellido` separado de `nombre` en `usuarios`, `aspirantes`, registro, `Sesion`, `Perfil` y `PATCH /perfil`; las referencias a personas dentro de una solicitud siguen con el nombre completo (CC-012) |
| 1.6 | 2026-11-20 | jumaluc | Correcciones de la auditoría del 2026-11-19 y login del personal: CSRF activado y CORS sin el puerto 5273 (§1), el error de Google vuelve a `/login-personal` y `POST /auth/personal/login` reemplaza al login simulado `POST /auth/dev/login` (§4.1 y §10.1) (CC-014, CC-015, CC-016) |
| 1.7 | 2026-11-20 | jumaluc | Sprint 4: bandeja del personal con el filtro `sinResponsable`, `TOMAR_PROCESO`, `DARSE_DE_BAJA`, `GUARDAR_MARCAS`, `AGREGAR_FALTANTE`, aceptar y rechazar la documentación, recarga selectiva con `reemplazaAId`, `CAMBIO_RESPONSABLE` en el historial y mails por cambio de estado (CC-013) |
| 1.8 | 2026-11-20 | jumaluc | Aclaraciones tras la revisión del Sprint 4: `desde` y `hasta` de la bandeja son días de Argentina (§6.2), y en la recarga el borrador anterior se descarta recién cuando el archivo nuevo fue aceptado (§6.5) |
| 1.9 | 2026-11-20 | jumaluc | De la auditoría del 2026-11-19, lo que depende del Sprint 4: código `NO_RESPONSABLE` y orden de validación con el responsable (§1 y §7), y el Admin ejecuta las acciones del Académico y de Secretaría (§7 y §10.1) (CC-017) |
| 1.10 | 2026-11-20 | jumaluc | Sprint 5: `PROPONER_FECHAS`, `SOLICITAR_OTRAS_FECHAS`, `PROPONER_SEGUNDA_RONDA`, `ELEGIR_HORARIO`, `RECHAZAR_SEGUNDA_RONDA` y `CARGAR_ENLACE_MEET` implementadas; lectura de `Analisis`, que suma `subidoPor`, y de `ReunionInfo`; las fechas se proponen en punto, desde mañana y hasta dentro de 30 días (§2.10 a §2.12, §6.6 y §7; CC-018, CC-019) |
| 1.12 | 2026-11-22 | jumaluc | Sprint 7: legalización, informe de materias, cupón y comprobantes implementados (§2.13 a §2.15, §6.5, §6.6 y §7). El informe pasa a ser `materiasReconocidas`, `observaciones` y `montoAPagar`, y se carga con `ACEPTAR_LEGALIZADOS` (se quita `CARGAR_INFORME`); `ENVIAR_CUPON` recibe `monto`; `Pago` suma datos del archivo de cada comprobante; se quita `documentosLegalizados` (CC-022, CC-023) |
| 1.13 | 2026-11-22 | jumaluc | Sprint 8: notificaciones (§2.21 y §8.2, sin `tipo`), auditoría (§8.3, con filtro `usuario` por email y `GET /auditoria/opciones`), administración (§9: contraseña en el alta y en la edición, `PROCESO_ACTIVO` con los expedientes en el mensaje, la asignación de carreras devuelve la cuenta), carreras del Admin (§5) y exportación de expedientes (§7.2) implementadas. `GET /roles` no se implementa (CC-024, CC-025) |
| 1.11 | 2026-11-22 | jumaluc | Sprint 6: chat de la solicitud implementado, con `lecturas_chat`, `puedeEscribirChat`, `autor.rol` y adjuntos PDF, JPG o PNG (§2.16, §6.4 y §8.1); `FIJAR_FECHA` con cualquier fecha futura, `MARCAR_NO_ASISTIO`, `FINALIZAR_REUNION` con notas solo para el personal, `ACTUALIZAR_ANALISIS`, `ACEPTAR_INSCRIPCION` y `RECHAZAR_INSCRIPCION` (§2.12, §6.6, §7.2 y §7.3; CC-020, CC-021) |

> El stack, los puertos y las convenciones de git están en el [README de la raíz](../README.md). El backend implementa por ahora la autenticación y el perfil (§4), la lectura de catálogos (§5), la creación y lectura de solicitudes con sus documentos y la bandeja del personal (§6, con el análisis académico, las fechas de reunión, el informe de materias y el pago) y todas las acciones del trámite (§7), de la creación de la solicitud a `E3-INI`. También el chat de la solicitud (§8.1). El resto del documento es el contrato a acordar.

Este documento fija el contrato entre el frontend (React, `services/api/client.ts`) y el backend (Spring Boot, a implementar) del sistema E3. Se basa en los Requisitos v13.0 (21 estados, 27 transiciones). Donde los requisitos no definen algo, la decisión figura como **Propuesta** y hay que validarla con backend.

## 1. Convenciones generales

| Tema | Definición |
| --- | --- |
| Base URL | Context path `/api` del backend (valor de `VITE_API_URL`; puertos en el [README](../README.md)). Todas las rutas de este documento son relativas a esa base. |
| Formato | JSON UTF-8. Archivos por `multipart/form-data`. Descargas binarias con `Content-Disposition: attachment`. |
| Nombres de campos | camelCase en JSON (default de Jackson). |
| Fechas | ISO 8601 con zona: `2026-10-14T15:30:00-03:00`. Solo fecha: `2026-10-14`. |
| Identificadores | `id` numérico en las rutas. `codigoExp` (`EXP-2026-001`) solo para mostrar y buscar. |
| Estados | Siempre por `codigo` (`DOC-REV`), nunca por nombre (RF-ESTADO-004). |
| Sesión | Cookie `E3_SESION`, `HttpOnly`, `SameSite=Lax` y `Secure` en producción, emitida por el backend; el cliente ya envía `credentials: 'include'`. Lleva un JWT firmado con el id de la fila de `sesiones`, que se valida en cada pedido (CC-007). |
| CORS | Origen `http://localhost:5173` con `Access-Control-Allow-Credentials: true`; en producción, `CORS_ALLOWED_ORIGINS`. |
| CSRF | Cookie `XSRF-TOKEN` (legible desde el frontend, con `Path=/`) + header `X-XSRF-TOKEN` con el mismo valor en POST, PUT, PATCH y DELETE; `client.ts` lo agrega solo. El backend deja la cookie en cualquier respuesta, también en un 401. Sin el header, o con un valor distinto al de la cookie, responde 403 `ACCESO_DENEGADO`. El frontend tiene que poder leer la cookie: en desarrollo los dos corren en `localhost`, y en producción tienen que compartir dominio. |
| Paginación | `?page=0&size=20&sort=campo,desc`. Respuesta con la forma `Page<T>` (abajo). |
| Doble verificación | Es solo de UI (RF-AUTH-006): el backend recibe una única llamada, al confirmar. |
| Acciones disponibles | El detalle de una solicitud trae `accionesDisponibles`, calculadas por el backend según rol, permiso (participante u observador) y estado. El frontend solo muestra los botones de esa lista (frontend-interfaces §0.10 y §0.16). |
| Concurrencia | Toda acción que cambia el estado envía `estadoEsperado`. Si la solicitud ya cambió, el backend responde 409 `TRANSICION_INVALIDA` (frontend-interfaces §0.16.4). |

### Respuesta paginada

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 134,
  "totalPages": 7
}
```

### Formato de error

Compatible con `ApiErrorBody` (`message`, `errors`) de `types/api.ts`; suma `status`, `code`, `timestamp` y `path`.

```json
{
  "status": 409,
  "code": "TRANSICION_INVALIDA",
  "message": "Esta acción ya no está disponible porque el estado de la solicitud cambió.",
  "errors": { "comentario": ["Es obligatorio"] },
  "timestamp": "2026-10-14T15:30:00-03:00",
  "path": "/api/solicitudes/42/documentacion/rechazar"
}
```

### Códigos HTTP

| Código | Cuándo | `ApiErrorKind` en el front |
| --- | --- | --- |
| 200 | Lectura o acción que devuelve cuerpo | — |
| 201 | Recurso creado (con header `Location`) | — |
| 204 | Acción sin cuerpo de respuesta | — |
| 400 | Validación de campos (`errors` por campo) | `bad_request` |
| 401 | Sin sesión o sesión vencida | `unauthorized` |
| 403 | Rol sin permiso, observador o participante no responsable intentando actuar, o pedido sin el token CSRF | `forbidden` |
| 404 | Recurso inexistente o no visible para el usuario | `not_found` |
| 409 | Transición inválida, proceso ya tomado, email o DNI duplicado | `conflict` |
| 413 | Archivo de más de 10 MB o total de más de 50 MB | `bad_request` |
| 423 | Cuenta bloqueada (el cuerpo trae `bloqueadoHasta`) | `locked` |
| 500 | Error del servidor | `server` |

### Códigos de negocio (`code`)

| `code` | HTTP | Significado |
| --- | --- | --- |
| `VALIDACION` | 400 | Uno o más campos inválidos; detalle en `errors` |
| `NO_AUTENTICADO` | 401 | Sin sesión o sesión vencida |
| `ACCESO_DENEGADO` | 403 | El rol no puede usar esa ruta |
| `DUPLICADO` | 409 | Email, DNI o código de carrera ya existente |
| `CREDENCIALES_INVALIDAS` | 401 | Email o contraseña incorrectos; el cuerpo trae `intentosRestantes` |
| `CUENTA_BLOQUEADA` | 423 | 3 intentos fallidos: bloqueo de 6 h (login) o 15 min (código) |
| `EMAIL_NO_VALIDADO` | 403 | El aspirante no validó el código de 6 dígitos |
| `CODIGO_INVALIDO` / `CODIGO_EXPIRADO` | 400 | Código de validación o de recupero incorrecto o vencido (10 min) |
| `USUARIO_NO_REGISTRADO` | 403 | Login Google con email fuera de `usal.edu.ar`, inexistente o inactivo |
| `TRANSICION_INVALIDA` | 409 | La acción no corresponde al estado actual |
| `NO_PARTICIPANTE` | 403 | El usuario es observador en esa solicitud (RF-AUTH-005) |
| `NO_RESPONSABLE` | 403 | El usuario es participante, pero no es el responsable de su rol en esa solicitud o todavía no hay responsable (RF-PROC-003) |
| `PROCESO_YA_TOMADO` | 409 | Otro usuario tomó el proceso antes |
| `UNICO_RESPONSABLE` | 409 | No puede darse de baja: es el único con acceso (RF-PROC-004) |
| `PROCESO_ACTIVO` | 409 | Revocación de permiso con proceso activo sin `forzar` (RF-ADMIN-004) |
| `SIN_PERMISO_CARRERA` | 409 | Asignación manual a un usuario sin permiso y sin pedir permiso temporal |
| `CHAT_DESHABILITADO` | 403 | La solicitud todavía no tiene responsable (RF-MSG-001) |
| `ARCHIVO_INVALIDO` | 400 | Formato distinto de PDF o JPG |
| `LIMITE_TAMANO` | 413 | Supera 10 MB por archivo o 50 MB en total |
| `DOCUMENTO_BLOQUEADO` | 409 | Se intenta reemplazar un documento marcado ✅ |
| `MARCAS_INCOMPLETAS` | 409 | Rechazo sin al menos un ❌ o con documentos en `PENDIENTE` (RF-ACAD-009) |

## 2. Entidades

Son 22 entidades: 13 salen de los Requisitos v12 y 9 son **Propuesta** para cubrir datos que el frontend necesita y que los requisitos todavía no modelan. Los campos van en camelCase, como viajan en el JSON; en la base se guardan en snake\_case.

Hay dos tablas de personas: `usuarios` (personal @usal y Admin) y `aspirantes`. Por eso las referencias a "quién hizo algo" en chat, notificaciones y sesiones usan el par `tipo` + `id` (`USUARIO` o `ASPIRANTE`).

### 2.1 Rol (`roles`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| codigo | string | `ASPIRANTE`, `ACAD`, `SECRE`, `ADMIN`, `REVISOR`. El front lo mapea a `constants/roles.ts` |
| nombre | string |  |
| descripcion | string |  |
| activo | boolean |  |

Observador no es un rol: es un Académico o Secretaría sin permiso de proceso sobre esa solicitud.

### 2.2 Usuario (`usuarios`)

Personal @usal (Académico, Secretaría, Revisor) y Admin General.

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| email | string | Único. Dominio `usal.edu.ar` |
| nombre | string |  |
| apellido | string | Nombre y apellido van siempre en campos separados (CC-012) |
| rolId | int | FK a `roles` |
| activo | boolean | Default `true` |
| intentosLoginFallidos | int | Solo aplica al Admin (login con contraseña) |
| bloqueadoHasta | datetime? |  |
| fechaUltimoIntentoFallido | datetime? | **Propuesta**: el contador se reinicia a las 6 h (RF-AUTH-002). No se expone |
| fechaCreacion | datetime |  |
| fechaUltimoLogin | datetime? |  |
| telefono | string? | **Propuesta** (RF-AUTH-007) |
| avatar | string? | **Propuesta**: clave del avatar elegido (galería por género) |
| googleSub | string? | **Propuesta**: `sub` de Google, se completa en el primer login |
| passwordHash | string? | **Propuesta**: solo Admin. Nunca se expone |

### 2.3 Aspirante (`aspirantes`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| email | string | Único |
| passwordHash | string | Nunca se expone |
| nombre | string |  |
| apellido | string | Nombre y apellido van siempre en campos separados (CC-012) |
| telefono | string |  |
| dni | string | Único |
| emailValidado | boolean | Sin validar no puede crear solicitudes |
| intentosLoginFallidos | int | 3 fallidos → bloqueo 6 h |
| bloqueadoHasta | datetime? |  |
| fechaUltimoIntentoFallido | datetime? | **Propuesta**: el contador se reinicia a las 6 h (RF-AUTH-002). No se expone |
| activo | boolean |  |
| fechaCreacion | datetime |  |
| fechaUltimoLogin | datetime? |  |
| avatar | string? | **Propuesta** |

Los campos `codigoValidacion`, `fechaValidacionEnviada` y `expiracionCodigo` de los requisitos se mueven a `CodigoVerificacion` (2.19) para cubrir también el recupero de contraseña.

### 2.4 Carrera (`carreras`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| codigo | string | Único, ej. `ING-INF-001`. El formulario inicial envía este código |
| nombre | string |  |
| facultad | string |  |
| descripcion | string |  |
| activo | boolean | Baja lógica |
| fechaCreacion | datetime |  |

### 2.5 Estado (`estados`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| codigo | string | Único: los 21 códigos de RF-ESTADO-001 |
| nombre | string | Solo para mostrar |
| descripcion | string |  |
| esTerminal | boolean | Hoy `INSC-RECH` y `SOL-CANC` |
| activo | boolean |  |

### 2.6 Solicitud (`solicitudes`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| codigoExp | string | Único, `EXP-2026-001`: año de creación y un número de secuencia que no se reinicia con el año (CC-009) |
| aspiranteId | int | FK |
| carreraDestinoId | int | FK |
| estadoCodigo | string | **Propuesta**: estado actual desnormalizado, para filtrar la bandeja sin recorrer el historial |
| responsableAcademicoId | int? | FK a `usuarios` |
| responsableAdministrativoId | int? | FK a `usuarios` |
| fechaCreacion | datetime |  |
| fechaUltimaMod | datetime |  |
| fechaUltimoCambioEstado | datetime |  |
| fechaAsignacionAcademico | datetime? |  |
| fechaAsignacionAdministrativo | datetime? |  |
| universidadOrigen | string | **Propuesta** (Anexo A #1 de interfaces) |
| facultadOrigen | string | **Propuesta** |
| carreraOrigen | string | **Propuesta** |
| condicionAcademica | string | **Propuesta**; valores a definir (Anexo A #2). Hasta entonces, texto libre de hasta 50 caracteres |
| observaciones | string? | **Propuesta** |

### 2.7 Historial de estado (`estados_solicitud`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK |
| estadoCodigo | string | FK a `estados.codigo` |
| estadoAnteriorCodigo | string? | Nulo en la creación |
| fechaAsignacion | datetime | Orden DESC en el timeline |
| fechaUltimaModificacion | datetime |  |
| usuarioUltimoCambioId | int? | Nulo si el cambio lo disparó el aspirante |
| comentarios | string? | Motivo de rechazo, etc. |

### 2.8 Documento de solicitud (`documentos_solicitud`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK |
| tipo | string | `ANALITICO`, `PLAN`, `PROGRAMA`, `OTRO` |
| descripcion | string? | **Propuesta**: ej. "Programa de Análisis Matemático II" |
| nombreArchivo | string? | Nulo si es un requerido faltante |
| rutaArchivo | string? | Por año y DNI. Nunca se expone; se descarga por endpoint |
| tamanoBytes | int? |  |
| fechaCarga | datetime? |  |
| version | int | Default 1 |
| reemplazaAId | int? | Versión anterior (no se borra) |
| estadoRevision | enum | `PENDIENTE`, `OK`, `RECARGAR` |
| etapaRevision | enum | `DOCUMENTACION`, `LEGALIZACION` |
| comentarioRevision | string? | Visible para el aspirante |
| revisadoPorId | int? |  |
| fechaRevision | datetime? |  |
| esRequeridoFaltante | boolean | Agregado por el Académico, sin archivo |

Campo `borrador` (boolean): `true` mientras el aspirante subió el archivo pero todavía no ejecutó `ENVIAR_DOCUMENTACION` (Anexo A #4 de interfaces). Los borradores solo los ve el aspirante (CC-009).

### 2.9 Análisis académico (`analisis_academico`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK |
| tipo | enum | `MATRIZ_EQUIVALENCIAS`, `HORARIO_CURSADA` |
| version | int | La API expone solo la vigente (mayor versión) |
| reemplazaAId | int? |  |
| nombreArchivo | string? | Archivo, notas o ambos |
| rutaArchivo | string? |  |
| notas | string? |  |
| fechaCarga | datetime |  |
| subidoPorId | int |  |

### 2.10 Ronda de reunión (`rondas_reunion`) — Propuesta

Los requisitos definen las rondas como estados (`REU-FEC-ASP-1` y `-2`) pero no dónde se guardan las fechas propuestas. Esta tabla y las de §2.11 y §2.12 se implementaron así en el Sprint 5 (migración `V7`, CC-019).

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK |
| numero | int | 1 o 2 |
| propuestaPorId | int | Académico |
| fechaPropuesta | datetime |  |
| motivoOtrasFechas | string? | Lo que escribe el aspirante al pedir otras fechas (solo ronda 1) |
| disponibilidadAspirante | string? | Cuándo puede reunirse |
| rechazada | boolean | Ronda 2 rechazada → `REU-CHAT` |

### 2.11 Horario propuesto (`horarios_propuestos`) — Propuesta

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| rondaId | int | FK. Hasta 7 por ronda |
| inicio | datetime | En punto, entre las 10:00 y las 17:00 de Argentina; desde mañana y hasta dentro de 30 días (CC-018) |
| fin | datetime | inicio + 1 h |
| elegido | boolean | Uno solo por solicitud |

### 2.12 Reunión (`reuniones`) — Propuesta

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK. Una vigente por solicitud; una nueva si el aspirante no se presentó |
| inicio | datetime |  |
| fin | datetime |  |
| origen | enum | `ELEGIDA_ASPIRANTE`, `FIJADA_CHAT` |
| horarioPropuestoId | int? | Si la eligió el aspirante |
| enlaceMeet | string? | Lo carga el Académico en `REU-PEND` |
| notas | string? | Notas de reunión (RF-ACAD-004). Solo las recibe el personal |
| resultado | enum | `PENDIENTE`, `REALIZADA`, `NO_ASISTIO` |
| fechaResultado | datetime? |  |

### 2.13 Informe de materias (`informes_materias`)

Versión simple (CC-022): lo carga el Académico al aceptar los documentos legalizados. No hay tabla por materia: el detalle está en la matriz de equivalencias. El monto a pagar no se guarda acá: sale del cupón.

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK, único |
| materiasReconocidas | int | De 0 a 200 |
| observaciones | string? | Máx. 2000 |
| cargadoPorId | int | Académico |
| fechaCarga | datetime |  |

### 2.14 Cupón de pago (`cupones_pago`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK, único |
| monto | decimal | Lo ingresa Secretaría: mayor que 0, hasta 2 decimales (CC-022) |
| rutaArchivo | string | PDF generado por el sistema |
| fechaEmision | datetime |  |
| emitidoPorId | int | Secretaría |

### 2.15 Comprobante de pago (`comprobantes_pago`)

Además de los campos de abajo guarda `estadoVerificacion` (`PENDIENTE`, `ACEPTADO` o `RECHAZADO`), `comentarioSecretaria`, `verificadoPorId` y `fechaVerificacion`.

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK |
| cuponId | int | FK |
| version | int | Cada reenvío suma 1; los anteriores se conservan |
| nombreArchivo | string | PDF o JPG, máx. 10 MB |
| rutaArchivo | string |  |
| tamanoBytes | int |  |
| fechaCarga | datetime |  |
| estadoVerificacion | enum | `PENDIENTE`, `ACEPTADO`, `RECHAZADO` |
| comentarioSecretaria | string? | Obligatorio si se rechaza |
| verificadoPorId | int? |  |
| fechaVerificacion | datetime? |  |

### 2.16 Mensaje de chat (`mensajes_chat`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| solicitudId | int | FK. El chat es de la solicitud, no del usuario |
| autorTipo | enum | `USUARIO`, `ASPIRANTE` |
| autorId | int |  |
| contenido | string | Máx. 500 caracteres. Vacío si el mensaje es solo un adjunto |
| tipoConsulta | enum | `DOCUMENTACION`, `ESTADO`, `CALCULOS`, `FECHAS`, `OTRO` |
| adjuntoNombre | string? | PDF, JPG o PNG de hasta 5 MB |
| adjuntoRuta | string? |  |
| fechaEnvio | datetime |  |

El indicador "visto" y los mensajes sin leer salen de `lecturas_chat` (`solicitudId`, `lectorTipo`, `lectorId`, `hastaMensajeId`, `fechaLectura`): una fila por lector con el último mensaje que leyó. La propuesta original era `lecturas_mensaje`, con una fila por mensaje y lector; se implementó así porque `POST /chat/leidos` ya recibe "hasta qué mensaje" (migración `V8`, CC-021).

### 2.17 Permiso por carrera (`user_career_permissions`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| userId | int | FK a `usuarios` |
| careerId | int | FK a `carreras`. Único con `userId` |
| assignedAt | datetime |  |
| revokedAt | datetime? | Nulo = activo. Toda validación exige `revokedAt IS NULL` |

### 2.18 Permiso por solicitud (`user_solicitud_permissions`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| userId | int | FK |
| solicitudId | int | FK. Único con `userId` |
| assignedAt | datetime |  |
| assignedBy | int | Admin que lo creó |
| expiresAt | datetime? | Nulo = sin vencimiento |
| revokedAt | datetime? | Nulo = activo |

### 2.19 Código de verificación (`codigos_verificacion`) — Propuesta

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| aspiranteId | int | FK |
| proposito | enum | `VALIDACION_EMAIL`, `RECUPERO_PASSWORD` |
| codigoHash | string | 6 dígitos, guardado con hash |
| expiraEn | datetime | Creación + 10 min |
| intentos | int | 3 fallidos → bloqueo 15 min |
| usadoEn | datetime? |  |

### 2.20 Sesión (`sesiones`) — Propuesta

Alimenta "Historial de accesos" y "Sesiones activas" de Mi perfil (RF-AUTH-007).

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | string | UUID; viaja firmado dentro del JWT de la cookie |
| titularTipo | enum | `USUARIO`, `ASPIRANTE` |
| titularId | int |  |
| ip | string |  |
| userAgent | string |  |
| inicio | datetime |  |
| ultimoUso | datetime |  |
| expiraEn | datetime | 30 días con "recuérdame"; 1 día en el resto de los casos |
| cerradaEn | datetime? |  |

### 2.21 Notificación (`notificaciones`)

La campana de la barra superior replica los mails (interfaces §4.9).

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| destinatarioTipo | enum | `USUARIO`, `ASPIRANTE` |
| destinatarioId | int |  |
| solicitudId | int? | Destino del link |
| titulo | string |  |
| mensaje | string |  |
| leida | boolean |  |
| fechaCreacion | datetime |  |

### 2.22 Auditoría (`auditoria_sistema`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| id | int | PK |
| timestamp | datetime | Con zona |
| usuarioId | int? |  |
| usuarioEmail | string |  |
| accion | string | `cambio_estado`, `tomar_proceso`, `baja_responsable`, `marca_documento`, `login`, `descarga_expediente`… |
| entidad | string | `solicitud`, `usuario`, `carrera`, `permiso`… |
| entidadId | int |  |
| estadoCodigoAnterior | string? | Siempre código, nunca nombre |
| estadoCodigoNuevo | string? |  |
| detalles | JSON |  |
| comentarios | string? |  |
| ipOrigen | string |  |

## 3. Relaciones entre entidades

```mermaid
erDiagram
    ASPIRANTE ||--o{ SOLICITUD : inicia
    CARRERA ||--o{ SOLICITUD : "carrera destino"
    USUARIO |o--o{ SOLICITUD : responsables
    ROL ||--o{ USUARIO : tiene
    USUARIO ||--o{ PERMISO_CARRERA : recibe
    CARRERA ||--o{ PERMISO_CARRERA : habilita
    USUARIO ||--o{ PERMISO_SOLICITUD : recibe
    SOLICITUD ||--o{ PERMISO_SOLICITUD : habilita
    SOLICITUD ||--o{ HISTORIAL_ESTADO : registra
    ESTADO ||--o{ HISTORIAL_ESTADO : referencia
    SOLICITUD ||--o{ DOCUMENTO : tiene
    SOLICITUD ||--o{ ANALISIS_ACADEMICO : tiene
    SOLICITUD ||--o{ RONDA_REUNION : "tiene (Propuesta)"
    RONDA_REUNION ||--o{ HORARIO_PROPUESTO : "propone (Propuesta)"
    SOLICITUD ||--o{ REUNION : "tiene (Propuesta)"
    SOLICITUD ||--o| INFORME_MATERIAS : "tiene (Propuesta)"
    SOLICITUD ||--o{ CUPON_PAGO : "tiene (Propuesta)"
    CUPON_PAGO ||--o{ COMPROBANTE_PAGO : "recibe (Propuesta)"
    SOLICITUD ||--o{ MENSAJE_CHAT : tiene
```

Fuera del diagrama: Sesión, Código de verificación, Notificación y Auditoría.

La solicitud es el centro: el aspirante la inicia, la carrera destino define quién puede participar y todas las tablas del trámite (de la columna derecha) tienen `solicitudId`. El personal llega a una solicitud por permiso de carrera o por permiso temporal.

## 4. Autenticación y perfil

Hay tres formas de entrar: aspirante con email y contraseña, personal @usal con Google y Admin con usuario y contraseña. Las tres terminan igual: el backend setea la cookie de sesión y el front llama a `GET /auth/me`.

### 4.1 Endpoints de autenticación

| Método | Ruta | Quién | Qué hace | Respuesta | Requisito |
| --- | --- | --- | --- | --- | --- |
| POST | `/auth/registro` | Público | Crea el aspirante y envía el código de 6 dígitos | 201 `{ aspiranteId, email }` | RF-AUTH-001 |
| POST | `/auth/registro/validar` | Público | Valida el código e inicia sesión | 200 `Sesion` + cookie | RF-AUTH-001 |
| POST | `/auth/registro/reenviar-codigo` | Público | Genera un código nuevo (invalida el anterior) | 204 | RF-AUTH-001 |
| POST | `/auth/login` | Aspirante | Login con email y contraseña | 200 `Sesion` + cookie | RF-AUTH-002 |
| GET | `/auth/google` | Personal @usal | Redirige a Google, pasando por `/oauth2/authorization/google` | 302 | RF-AUTH-003 |
| GET | `/auth/google/callback` | Google | Valida dominio y alta; crea sesión | 302 a `/dashboard`, a `/login-personal?error=USUARIO_NO_REGISTRADO` o, si falla Google, a `/login-personal?error=GOOGLE_ERROR` | RF-AUTH-003 |
| POST | `/auth/personal/login` | Académico, Secretaría y Revisor | Login con usuario (el email) y contraseña: `{ "usuario", "password" }`. **Provisorio**, hasta publicar el sistema con Google (CC-016). Sesión de 1 día y los mismos errores que `/auth/admin/login`. El Admin no entra por acá, ni el personal por `/auth/admin/login`: los dos reciben `CREDENCIALES_INVALIDAS` | 200 `Sesion` + cookie | RF-AUTH-003 |
| POST | `/auth/admin/login` | Admin | Login con usuario y contraseña | 200 `Sesion` + cookie | RF-AUTH-004 |
| POST | `/auth/password/recuperar` | Aspirante | Envía código de recupero | 204 siempre (no revela si el email existe) | RF-AUTH-002 |
| POST | `/auth/password/restablecer` | Aspirante | Cambia la contraseña con el código | 204 | RF-AUTH-002 |
| POST | `/auth/logout` | Todos | Cierra la sesión actual | 204 | — |
| GET | `/auth/me` | Todos | Datos de la sesión | 200 `Sesion` | — |

**`POST /auth/registro`** — request

```json
{
  "email": "juan.perez@gmail.com",
  "password": "Clave#2026",
  "nombre": "Juan",
  "apellido": "Pérez",
  "telefono": "+54 11 5555-0000",
  "dni": "40123456"
}
```

Validaciones: contraseña de 8 o más caracteres con mayúscula, número y carácter especial. Email o DNI repetido → 409 `DUPLICADO` con `errors.email` o `errors.dni`.

**`POST /auth/registro/validar`** — request `{ "email": "juan.perez@gmail.com", "codigo": "483920" }`. Errores: 400 `CODIGO_INVALIDO` (con `intentosRestantes`), 400 `CODIGO_EXPIRADO`, 423 `CUENTA_BLOQUEADA` (15 min).

**`POST /auth/login`** — request

```json
{ "email": "juan.perez@gmail.com", "password": "Clave#2026", "recordarme": true }
```

Errores: 401 `CREDENCIALES_INVALIDAS` `{ "intentosRestantes": 2 }`, 423 `CUENTA_BLOQUEADA` `{ "bloqueadoHasta": "2026-10-14T21:30:00-03:00" }`, 403 `EMAIL_NO_VALIDADO`.

**`POST /auth/admin/login`** — request `{ "usuario": "admin@usal.edu.ar", "password": "…" }`. Sesión de 1 día. Mismos errores que `/auth/login`, sin `EMAIL_NO_VALIDADO`.

**`POST /auth/password/restablecer`** — request `{ "email", "codigo", "passwordNueva" }`.

**`Sesion`** — respuesta de login, validar y `GET /auth/me`

```json
{
  "tipo": "USUARIO",
  "id": 7,
  "nombre": "María",
  "apellido": "Gómez",
  "email": "m.gomez@usal.edu.ar",
  "rol": "ACAD",
  "avatar": "hombre-02",
  "emailValidado": true,
  "esOAuth": true,
  "carrerasConPermiso": [
    { "id": 1, "codigo": "ING-INF-001", "nombre": "Ingeniería en Informática" }
  ],
  "expiraEn": "2026-11-13T15:30:00-03:00"
}
```

`tipo` es `ASPIRANTE` o `USUARIO`. `nombre` y `apellido` van siempre separados en los datos de la cuenta (registro, `Sesion` y `Perfil`); las referencias a una persona dentro de una solicitud (`aspirante`, responsables, historial) traen el nombre completo en `nombre`. `carrerasConPermiso` solo viene para Académico y Secretaría (permisos activos, `revokedAt` nulo); el front la usa para ordenar la bandeja y no para habilitar botones.

### 4.2 Endpoints de perfil (RF-AUTH-007)

| Método | Ruta | Quién | Qué hace | Respuesta |
| --- | --- | --- | --- | --- |
| GET | `/perfil` | Todos | Datos del perfil | 200 `Perfil` |
| PATCH | `/perfil` | Todos menos Revisor | Edita `nombre` y `apellido` (no OAuth), `telefono`, `avatar` | 200 `Perfil` |
| PUT | `/perfil/password` | Aspirante, Admin | `{ passwordActual, passwordNueva }` | 204 |
| PUT | `/perfil/email` | Admin | `{ emailNuevo, passwordActual }`; valida dominio `usal.edu.ar` | 204 |
| GET | `/perfil/accesos` | Todos | Últimos 10 logins: `[{ fecha, ip, duracionMinutos }]` | 200 |
| GET | `/perfil/sesiones` | Todos | Sesiones activas: `[{ id, ip, userAgent, inicio, ultimoUso, actual }]` | 200 |
| DELETE | `/perfil/sesiones/{sesionId}` | Todos | Cierra otra sesión | 204 |

**`Perfil`**

```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@gmail.com",
  "telefono": "+54 11 5555-0000",
  "dni": "40123456",
  "avatar": "hombre-02",
  "rol": "ASPIRANTE",
  "esOAuth": false,
  "camposEditables": ["nombre", "apellido", "telefono", "avatar"]
}
```

El catálogo de avatares vive en `frontend/src/assets/`; el backend solo guarda y valida la clave (`hombre-02`). Todos los cambios de perfil quedan en auditoría.

Contraseña actual incorrecta en `PUT /perfil/password` o `PUT /perfil/email`: 400 `VALIDACION` con `errors.passwordActual`. Un usuario con Google que envía `nombre` o `apellido` en `PATCH /perfil`: 400 con el error en ese campo. Para el personal @usal, `telefono` puede venir nulo y `dni` siempre es nulo. Al cambiar la contraseña se cierran las demás sesiones.

## 5. Catálogos: carreras, estados y roles

Los catálogos los carga el backend; el frontend nunca los hardcodea (interfaces §0.12).

| Método | Ruta | Quién | Qué hace | Respuesta | Requisito |
| --- | --- | --- | --- | --- | --- |
| GET | `/carreras` | Autenticado | Lista carreras. `?activo=true` por defecto; el Admin puede pedir `?activo=todas` | 200 `Carrera[]` | RF-ASP-001 |
| GET | `/carreras/{id}` | Autenticado | Detalle | 200 `Carrera` | — |
| POST | `/carreras` | Admin | Crea carrera | 201 `Carrera` | RF-ADMIN-002 |
| PUT | `/carreras/{id}` | Admin | Modifica datos | 200 `Carrera` | RF-ADMIN-002 |
| PATCH | `/carreras/{id}/activo` | Admin | `{ "activo": false }` (baja lógica) | 200 `Carrera` | RF-ADMIN-002 |
| GET | `/estados` | Autenticado | Catálogo de los 21 estados | 200 `Estado[]` | RF-ESTADO-003 |
| GET | `/roles` | Admin | **No se implementa:** los tres roles asignables (`ACAD`, `SECRE`, `REVISOR`) son fijos y el frontend los toma de `constants/roles.ts` | — | RF-ADMIN-001 |

**`Carrera`**

```json
{
  "id": 1,
  "codigo": "ING-INF-001",
  "nombre": "Ingeniería en Informática",
  "facultad": "Facultad de Ingeniería",
  "descripcion": "…",
  "activo": true
}
```

Request de `POST` y `PUT`: los mismos campos sin `id` ni `activo`. `codigo` repetido → 409 `DUPLICADO`.

**`Estado`**

```json
{
  "codigo": "DOC-REV",
  "nombre": "Documentación pendiente de revisión",
  "descripcion": "El aspirante envió la documentación y espera la revisión del Académico",
  "esTerminal": false,
  "activo": true
}
```

El front puede cachear `/estados` durante la sesión: cambia solo con una nueva versión de requisitos.

## 6. Solicitudes: lectura, creación y documentos

Un mismo recurso `/solicitudes` sirve a todos los roles: el backend filtra según quién pregunta. El aspirante solo ve las suyas; el personal ve todas (acceso básico) y actúa solo donde es participante.

### 6.1 Endpoints

| Método | Ruta | Quién | Qué hace | Respuesta | Requisito |
| --- | --- | --- | --- | --- | --- |
| GET | `/solicitudes` | Todos | Listado o bandeja con filtros | 200 `Page<SolicitudResumen>` | RF-PROC-001 |
| POST | `/solicitudes` | Aspirante validado | Crea la solicitud en `SOL-INI` | 201 `SolicitudDetalle` | RF-ASP-001 |
| GET | `/solicitudes/{id}` | Dueño o personal | Detalle con `accionesDisponibles` | 200 `SolicitudDetalle` | RF-ASP-003, RF-PROC-002 |
| GET | `/solicitudes/{id}/historial` | Dueño o personal | Timeline de estados y responsables, DESC | 200 `EventoHistorial[]` | RF-AUDIT-002 |
| GET | `/solicitudes/{id}/documentos` | Dueño o personal | Documentos vigentes; `?versiones=true` suma las anteriores | 200 `Documento[]` | RF-ACAD-009 |
| POST | `/solicitudes/{id}/documentos` | Aspirante | Sube un archivo (borrador hasta enviar) | 201 `Documento` | RF-ASP-002, 004, 009 |
| DELETE | `/solicitudes/{id}/documentos/{docId}` | Aspirante | Quita un borrador no enviado | 204 | RF-ASP-002 |
| GET | `/solicitudes/{id}/documentos/{docId}/archivo` | Dueño o personal | Descarga el archivo | 200 binario | RF-PROC-002 |
| GET | `/solicitudes/{id}/analisis` | Dueño o personal | Matriz y horario vigentes (nunca versiones viejas) | 200 `Analisis[]` | RF-ACAD-002 |
| GET | `/solicitudes/{id}/analisis/{analisisId}/archivo` | Dueño o personal | Descarga | 200 binario | RF-ACAD-002 |
| GET | `/solicitudes/{id}/reunion` | Dueño o personal | Rondas, horarios propuestos y reunión vigente | 200 `ReunionInfo` | RF-ASP-005 |
| GET | `/solicitudes/{id}/informe` | Dueño o personal | Informe de materias, desde `CUPON-SOL` | 200 `Informe` | RF-ASP-006 |
| GET | `/solicitudes/{id}/pago` | Dueño o personal | Cupón y comprobantes | 200 `Pago` | RF-ASP-007, RF-SEC-002 |
| GET | `/solicitudes/{id}/pago/cupon/archivo` | Dueño o personal | PDF del cupón | 200 binario | RF-ASP-007 |
| GET | `/solicitudes/{id}/pago/comprobantes/{compId}/archivo` | Dueño o personal | Comprobante | 200 binario | RF-SEC-002 |

### 6.2 Listado y bandeja

Filtros de `GET /solicitudes`: `estado` (código, repetible), `carreraId`, `desde` y `hasta` (última actualización, en días de Argentina), `q` (busca en `codigoExp`, nombre del aspirante y carrera), `asignadas=true` ("Mis asignadas"), `sinResponsable=true` y `finalizadas=true|false`.

El orden por defecto lo resuelve el backend: primero las carreras con permiso del usuario, agrupadas, y después el resto alfabético por carrera (RF-PROC-001). Para el aspirante: primero las que requieren su acción, después por última actualización.

El aspirante solo usa `estado` y `finalizadas`; el personal (Académico, Secretaría, Revisor y Admin) tiene todos los filtros. Dentro de cada carrera, la última actualización va primero.

- `sinResponsable=true` deja las solicitudes que quien pregunta puede tomar: no finalizadas, sin responsable de su rol y donde es participante (CC-013).
- `requiereMiAccion`: para el aspirante, que el estado espere su acción; para el personal, que sea el responsable de su rol y el estado sea de tipo A para ese rol.
- `esDeMisCarreras`: la carrera destino es una de las que tiene con permiso permanente. En el aspirante siempre es `false`.

**`SolicitudResumen`**

```json
{
  "id": 42,
  "codigoExp": "EXP-2026-001",
  "aspirante": { "id": 15, "nombre": "Juan Pérez" },
  "universidadOrigen": "Universidad Nacional de La Plata",
  "carreraDestino": { "id": 1, "codigo": "ING-INF-001", "nombre": "Ingeniería en Informática" },
  "estado": { "codigo": "DOC-REV", "nombre": "Documentación pendiente de revisión" },
  "responsableAcademico": null,
  "responsableAdministrativo": null,
  "fechaCreacion": "2026-10-08T11:00:00-03:00",
  "fechaUltimaMod": "2026-10-14T15:30:00-03:00",
  "requiereMiAccion": true,
  "esDeMisCarreras": true
}
```

### 6.3 Crear solicitud

**`POST /solicitudes`** — request

```json
{
  "universidadOrigen": "Universidad Nacional de La Plata",
  "facultadOrigen": "Facultad de Informática",
  "carreraOrigen": "Licenciatura en Sistemas",
  "carreraDestinoCodigo": "ING-INF-001",
  "condicionAcademica": "REGULAR",
  "observaciones": ""
}
```

Los datos personales no se envían: salen de la cuenta. El backend genera `codigoExp`, deja la solicitud en `SOL-INI` sin responsable y notifica a quienes tienen permiso sobre la carrera. Errores: 403 `EMAIL_NO_VALIDADO`, 400 `VALIDACION` (carrera inexistente o inactiva, en `errors.carreraDestinoCodigo`). Responde 201 con `Location`.

Al crearla se avisa por mail al Académico y a Secretaría con permiso permanente sobre la carrera (RF-ESTADO-002). Los mails son avisos: si uno falla se registra en el log y la acción sigue adelante.

### 6.4 Detalle

**`SolicitudDetalle`**

```json
{
  "id": 42,
  "codigoExp": "EXP-2026-001",
  "estado": { "codigo": "DOC-RECH", "nombre": "Documentación rechazada", "esTerminal": false },
  "aspirante": { "id": 15, "nombre": "Juan Pérez", "email": "juan.perez@gmail.com", "telefono": "+54 11 5555-0000", "dni": "40123456" },
  "datosOrigen": {
    "universidad": "Universidad Nacional de La Plata",
    "facultad": "Facultad de Informática",
    "carrera": "Licenciatura en Sistemas",
    "condicionAcademica": "REGULAR",
    "observaciones": ""
  },
  "carreraDestino": { "id": 1, "codigo": "ING-INF-001", "nombre": "Ingeniería en Informática" },
  "responsableAcademico": { "id": 7, "nombre": "María Gómez", "email": "m.gomez@usal.edu.ar", "fechaAsignacion": "2026-10-10T09:00:00-03:00" },
  "responsableAdministrativo": null,
  "fechaCreacion": "2026-10-08T11:00:00-03:00",
  "fechaUltimaMod": "2026-10-14T15:30:00-03:00",
  "fechaUltimoCambioEstado": "2026-10-14T15:30:00-03:00",
  "miRelacion": "ASPIRANTE",
  "accionesDisponibles": ["ENVIAR_DOCUMENTACION"],
  "ultimaObservacion": { "comentario": "El analítico no está firmado", "usuario": "María Gómez", "fecha": "2026-10-14T15:30:00-03:00" },
  "resumenDocumentos": { "ok": 2, "recargar": 1, "pendiente": 0 },
  "chatHabilitado": true,
  "puedeEscribirChat": true,
  "mensajesNoLeidos": 3
}
```

`miRelacion` vale `ASPIRANTE`, `PARTICIPANTE`, `OBSERVADOR`, `ADMIN` o `REVISOR`, y resuelve el modo de las interfaces (acción o solo lectura). `accionesDisponibles` usa los códigos de las secciones 7.1 y 7.2 y solo incluye las acciones ya implementadas. `DARSE_DE_BAJA` aparece solo si hay otra persona de su rol con acceso a la carrera: si el responsable no la recibe, es el único.

`ultimaObservacion` es el último comentario que dejó el personal en un cambio de estado, o `null`. `resumenDocumentos` cuenta los documentos vigentes que ve quien pregunta. `chatHabilitado` es `true` cuando hay algún responsable. `puedeEscribirChat` es `true` para el aspirante dueño y para los responsables, mientras haya algún responsable y la solicitud no esté finalizada (CC-021). `mensajesNoLeidos` cuenta los mensajes de otros que quien pregunta todavía no marcó como leídos; para quien no participa del chat vale 0.

**`EventoHistorial`**

```json
{
  "fecha": "2026-10-14T15:30:00-03:00",
  "tipo": "CAMBIO_ESTADO",
  "estadoAnterior": "DOC-REV",
  "estadoNuevo": "DOC-RECH",
  "usuario": "María Gómez",
  "comentario": "El analítico no está firmado"
}
```

`tipo`: `CAMBIO_ESTADO`, `CAMBIO_RESPONSABLE` o `DOCUMENTO_CARGADO`. En `DOCUMENTO_CARGADO`, `comentario` trae el nombre del archivo y los borradores no aparecen. En `CAMBIO_RESPONSABLE`, `usuario` es quien tomó el proceso o se dio de baja y `comentario` lo describe ("Tomó el proceso como responsable académico.").

### 6.5 Documentos

**`POST /solicitudes/{id}/documentos`** — `multipart/form-data`

| Parte | Tipo | Obligatorio | Notas |
| --- | --- | --- | --- |
| `archivo` | file | Sí | PDF o JPG, máx. 10 MB; 50 MB entre todos los del envío |
| `tipo` | string | Sí, salvo en recargas | `ANALITICO`, `PLAN`, `PROGRAMA`, `OTRO` |
| `descripcion` | string | No | ej. "Programa de Física I" |
| `reemplazaAId` | int | En recargas | Documento marcado `RECARGAR` que se reemplaza |

Se acepta en `SOL-INI` (documentos nuevos) y en `DOC-RECH` y `LEG-RECH` (solo reemplazos de documentos `RECARGAR`). Cada archivo se sube al elegirlo y queda como borrador hasta `ENVIAR_DOCUMENTACION`. Errores: 400 `ARCHIVO_INVALIDO`, 413 `LIMITE_TAMANO`, 409 `DOCUMENTO_BLOQUEADO`, 409 `TRANSICION_INVALIDA`.

Implementado hoy (CC-009 y CC-013):

- En `SOL-INI` (documentos nuevos) y en `DOC-RECH` y `LEG-RECH` (recarga).
- En `DOC-RECH`, `reemplazaAId` es obligatorio (400 `VALIDACION` con `errors.reemplazaAId`) y tiene que ser un documento vigente marcado `RECARGAR`: si no existe responde 404 y si está en `OK` o `PENDIENTE`, 409 `DOCUMENTO_BLOQUEADO`. El archivo nuevo hereda el tipo y la descripción del que reemplaza, y `version` sube en uno salvo que reemplace a un requerido faltante, que no tenía archivo.
- Subir otro archivo para el mismo documento descarta el borrador anterior, recién cuando el nuevo fue aceptado: si se rechaza (por ejemplo, por superar los 50 MB), el anterior queda como estaba.
- Un borrador no archiva al documento que reemplaza hasta que se envía: mientras tanto el aspirante recibe los dos (el observado y su reemplazo, unidos por `reemplazaAId`) y el personal, solo el observado.
- El formato se reconoce por el contenido del archivo, no por su nombre ni por el `Content-Type`.
- Los 50 MB se cuentan entre los borradores todavía no enviados.
- `tipo` fuera del catálogo: 400 `VALIDACION` con `errors.tipo`.
- `DELETE` de un documento ya enviado: 409 `TRANSICION_INVALIDA`.
- `CANCELAR_SOLICITUD` elimina los borradores con sus archivos; después la solicitud ya no acepta documentos.
- Los archivos se guardan en disco por año, DNI y expediente; el nombre original solo se muestra.

`ENVIAR_DOCUMENTACION` desde `SOL-INI` exige Analítico, Plan de estudios y al menos un Programa entre los borradores (Anexo A #3 de interfaces). Desde `DOC-RECH` exige un archivo nuevo para cada documento marcado `RECARGAR`. Si falta alguno responde 400 `VALIDACION` con `errors.documentos`.

**`Documento`**

```json
{
  "id": 301,
  "tipo": "ANALITICO",
  "descripcion": null,
  "nombreArchivo": "analitico.pdf",
  "tamanoBytes": 1048576,
  "fechaCarga": "2026-10-12T10:00:00-03:00",
  "version": 2,
  "reemplazaAId": 120,
  "borrador": false,
  "estadoRevision": "PENDIENTE",
  "etapaRevision": "DOCUMENTACION",
  "comentarioRevision": null,
  "revisadoPor": null,
  "fechaRevision": null,
  "esRequeridoFaltante": false,
  "urlDescarga": "/solicitudes/42/documentos/301/archivo"
}
```

Un requerido faltante (`esRequeridoFaltante`) todavía no tiene archivo: `nombreArchivo`, `tamanoBytes`, `fechaCarga` y `urlDescarga` llegan en `null`.

### 6.6 Otras respuestas de lectura

**`Analisis`** — una entrada por tipo, solo la vigente:

```json
{ "id": 88, "tipo": "MATRIZ_EQUIVALENCIAS", "version": 2, "nombreArchivo": "matriz.pdf", "notas": "Se reconocen 6 materias", "fechaCarga": "2026-10-20T12:00:00-03:00", "subidoPor": "María Gómez", "urlDescarga": "/solicitudes/42/analisis/88/archivo" }
```

`subidoPor` es el nombre de quien lo cargó. `nombreArchivo` y `urlDescarga` llegan en `null` cuando el documento se cargó solo con notas, y `notas` en `null` cuando se cargó solo con archivo.

**`ReunionInfo`**

```json
{
  "rondaActual": 1,
  "rondas": [
    {
      "numero": 1,
      "horarios": [
        { "id": 501, "inicio": "2026-10-28T10:00:00-03:00", "fin": "2026-10-28T11:00:00-03:00", "elegido": false }
      ],
      "motivoOtrasFechas": null,
      "disponibilidadAspirante": null
    }
  ],
  "reunion": null
}
```

`reunion`, cuando existe: `{ inicio, fin, origen, enlaceMeet, resultado, notas }`. Es la última de la solicitud: después de una inasistencia sigue siendo la que quedó en `NO_ASISTIO`, hasta que se pacta otra. `notas` son las que escribió el Académico al indicar que la reunión finalizó y llegan solo para el personal: al aspirante le llegan en `null` (CC-021). `rondaActual` es `null` hasta que el Académico propone la primera ronda, y los horarios de cada ronda llegan del más cercano al más lejano.

**`Informe`**

```json
{ "materiasReconocidas": 6, "observaciones": "Quedan Física II y Química para cursar.", "montoAPagar": 150000.00 }
```

Responde 404 antes de `CUPON-SOL`. `montoAPagar` es `null` hasta que Secretaría envía el cupón.

**`Pago`**

```json
{
  "cupon": { "monto": 150000.00, "fechaEmision": "2026-11-20T10:00:00-03:00", "urlDescarga": "/solicitudes/42/pago/cupon/archivo" },
  "comprobantes": [
    { "id": 9, "version": 1, "nombreArchivo": "pago.pdf", "tamanoBytes": 240000, "fechaCarga": "2026-11-21T18:00:00-03:00", "estadoVerificacion": "RECHAZADO", "comentarioSecretaria": "El monto no coincide", "urlDescarga": "/solicitudes/42/pago/comprobantes/9/archivo" }
  ]
}
```

`cupon` es `null` hasta que se envía. Los comprobantes llegan del primero al último; el vigente es el de mayor `version`.

## 7. Acciones del trámite

Cada transición de RF-ESTADO-001 es un `POST` propio bajo `/solicitudes/{id}`. Todas reciben `estadoEsperado` y devuelven 200 `SolicitudDetalle` con el estado nuevo. El backend valida rol, permiso de participante, responsable y estado; si algo no cumple responde 403 `NO_PARTICIPANTE`, 403 `NO_RESPONSABLE` o 409 `TRANSICION_INVALIDA`.

El motor valida en este orden (CC-009 y CC-017): el rol (403 `ACCESO_DENEGADO`), la participación (403 `NO_PARTICIPANTE`; para el aspirante, que la solicitud sea suya), el responsable (403 `NO_RESPONSABLE`, solo para el personal) y el estado (409 `TRANSICION_INVALIDA`, también si `estadoEsperado` no coincide). La solicitud se bloquea durante la acción, así que de dos pedidos simultáneos solo uno parte del estado esperado. Cada cambio deja una fila en `estados_solicitud` y otra en auditoría, incluida la creación (`estadoAnterior` nulo). Hoy están implementadas `ENVIAR_DOCUMENTACION` (desde `SOL-INI` y `DOC-RECH`), `CANCELAR_SOLICITUD`, `ACEPTAR_DOCUMENTACION`, `RECHAZAR_DOCUMENTACION`, `PROPONER_FECHAS`, `SOLICITAR_OTRAS_FECHAS`, `PROPONER_SEGUNDA_RONDA`, `ELEGIR_HORARIO`, `RECHAZAR_SEGUNDA_RONDA`, `FIJAR_FECHA`, `MARCAR_NO_ASISTIO`, `FINALIZAR_REUNION`, `ACEPTAR_INSCRIPCION` y `RECHAZAR_INSCRIPCION`: las transiciones 1 a 15 y la 27.

Para el personal, participar no alcanza: tiene que ser además el responsable de su rol en esa solicitud. Un participante que no es el responsable recibe 403 `NO_RESPONSABLE` y, si todavía no hay responsable, su única acción es `TOMAR_PROCESO` (CC-013 y CC-017). Un responsable al que le revocaron el permiso recibe 403 `NO_PARTICIPANTE`, porque la participación se valida antes.

El Admin ejecuta las transiciones del Académico y de Secretaría, y también `GUARDAR_MARCAS` y `AGREGAR_FALTANTE`, sin ser participante ni responsable (frontend-interfaces §7.0, CC-017). El estado y `estadoEsperado` se validan igual, el cambio queda registrado a su nombre y el responsable de la solicitud no cambia. Las transiciones del aspirante no entran: recibe 403 `ACCESO_DENEGADO`.

### 7.1 Transiciones (27)

| Código de acción | Endpoint (`POST /solicitudes/{id}…`) | Actor | De → A | Body (además de `estadoEsperado`) |
| --- | --- | --- | --- | --- |
| `CREAR_SOLICITUD` | `POST /solicitudes` | Aspirante | (inicio) → `SOL-INI` | Ver 6.3 |
| `ENVIAR_DOCUMENTACION` | `/documentacion/enviar` | Aspirante | `SOL-INI` → `DOC-REV`; `DOC-RECH` → `DOC-REV`; `LEG-RECH` → `LEG-REV` | — |
| `CANCELAR_SOLICITUD` | `/cancelar` | Aspirante | `SOL-INI` → `SOL-CANC` | — |
| `ACEPTAR_DOCUMENTACION` | `/documentacion/aceptar` | Académico | `DOC-REV` → `ANAL-PEND` | `marcas?` |
| `RECHAZAR_DOCUMENTACION` | `/documentacion/rechazar` | Académico | `DOC-REV` → `DOC-RECH` | `comentario`, `marcas` |
| `PROPONER_FECHAS` | `/analisis/propuesta` (multipart) | Académico | `ANAL-PEND` → `REU-FEC-ASP-1` | Matriz, horario de cursada y `horarios` |
| `SOLICITAR_OTRAS_FECHAS` | `/reunion/solicitar-otras-fechas` | Aspirante | `REU-FEC-ASP-1` → `REU-FEC-ACAD` | `motivo`, `disponibilidad` |
| `PROPONER_SEGUNDA_RONDA` | `/reunion/segunda-ronda` | Académico | `REU-FEC-ACAD` → `REU-FEC-ASP-2` | `horarios` |
| `ELEGIR_HORARIO` | `/reunion/elegir-horario` | Aspirante | `REU-FEC-ASP-1` o `-2` → `REU-PEND` | `horarioId` |
| `RECHAZAR_SEGUNDA_RONDA` | `/reunion/rechazar-ronda` | Aspirante | `REU-FEC-ASP-2` → `REU-CHAT` | `motivo?` |
| `FIJAR_FECHA` | `/reunion/fijar-fecha` | Académico | `REU-CHAT` → `REU-PEND` | `inicio` |
| `MARCAR_NO_ASISTIO` | `/reunion/no-asistio` | Académico | `REU-PEND` → `REU-FEC-ACAD` | `comentario?` |
| `FINALIZAR_REUNION` | `/reunion/finalizar` | Académico | `REU-PEND` → `ANAL-POST` | `notas?` |
| `ACEPTAR_INSCRIPCION` | `/inscripcion/aceptar` | Académico | `ANAL-POST` → `INSC-ACEP` | — |
| `RECHAZAR_INSCRIPCION` | `/inscripcion/rechazar` | Académico | `ANAL-POST` → `INSC-RECH` | `comentario` |
| `INDICAR_LEGALIZACION` | `/legalizacion/indicar` | Aspirante | `INSC-ACEP` → `LEG-REV` (sí) o `LEG-PEND` (no) | `legalizados: boolean` |
| `AVISAR_LEGALIZADO` | `/legalizacion/listo` | Aspirante | `LEG-PEND` → `LEG-REV` | — |
| `ACEPTAR_LEGALIZADOS` | `/legalizacion/aceptar` | Académico | `LEG-REV` → `CUPON-SOL` | `marcas?` (ninguna en `RECARGAR`), `materiasReconocidas`, `observaciones?`: guarda el informe de materias |
| `RECHAZAR_LEGALIZADOS` | `/legalizacion/rechazar` | Académico | `LEG-REV` → `LEG-RECH` | `comentario`, `marcas` |
| `ENVIAR_CUPON` | `/pago/cupon` | Secretaría | `CUPON-SOL` → `CUPON-PAG` | `monto` (el sistema genera el PDF con ese monto) |
| `ENVIAR_COMPROBANTE` | `/pago/comprobante` (multipart) | Aspirante | `CUPON-PAG` o `PAG-REENV` → `PAG-CTRL` | `archivo` |
| `ACEPTAR_PAGO` | `/pago/aceptar` | Secretaría | `PAG-CTRL` → `E3-INI` | `comentario?` |
| `RECHAZAR_PAGO` | `/pago/rechazar` | Secretaría | `PAG-CTRL` → `PAG-REENV` | `comentario` |

Las 27 transiciones quedan en 23 acciones: el reenvío de documentación (4) y de legalizados (20) usan `ENVIAR_DOCUMENTACION`, el reenvío de comprobante (25) usa `ENVIAR_COMPROBANTE`, e `INDICAR_LEGALIZACION` cubre la 16 y la 17.

### 7.2 Acciones sin cambio de estado

| Código de acción | Método y ruta | Actor | Cuándo | Body | Requisito |
| --- | --- | --- | --- | --- | --- |
| `TOMAR_PROCESO` | `POST /solicitudes/{id}/responsables/tomar` | Académico o Secretaría participante | Sin responsable de su rol | — | RF-PROC-003 |
| `DARSE_DE_BAJA` | `POST /solicitudes/{id}/responsables/baja` | Responsable actual | Siempre que no sea el único | — | RF-PROC-004 |
| `GUARDAR_MARCAS` | `PUT /solicitudes/{id}/documentos/marcas` | Académico participante | `DOC-REV`, `LEG-REV` | `marcas` | RF-ACAD-009 |
| `AGREGAR_FALTANTE` | `POST /solicitudes/{id}/documentos/faltantes` | Académico participante | `DOC-REV`, `LEG-REV` | `{ tipo, descripcion }` | RF-ACAD-009 |
| `CARGAR_ENLACE_MEET` | `PUT /solicitudes/{id}/reunion/enlace` | Académico | `REU-PEND` | `{ enlaceMeet }` | RF-ACAD-003 |
| `ACTUALIZAR_ANALISIS` | `POST /solicitudes/{id}/analisis` (multipart) | Académico | `ANAL-POST` | `tipo`, `archivo?`, `notas?` | RF-ACAD-004 |
| `DESCARGAR_EXPEDIENTE` | `GET /solicitudes/{id}/expediente` | Académico, Secretaría, Admin y Revisor (aspirante a definir) | Siempre | — → `Expediente_EXP-2026-001.zip` | RF-PROC-005 |
| `EXPORTAR_EXPEDIENTES` | `GET /solicitudes/expedientes?estado=…&desde=…` | Académico, Secretaría, Admin y Revisor | Siempre. Mismos filtros que la bandeja, sin paginar; sin resultados responde 404 | — → `Expedientes_2026_[fecha].zip`, con una carpeta por expediente | RF-PROC-005 |

Las dos exportaciones no figuran en `accionesDisponibles` y quedan en auditoría (`descarga_expediente` y `descarga_expedientes`). Cada expediente trae `resumen.pdf`, `documentos/` (los vigentes), `analisis/`, y `pago/` con el cupón y los comprobantes.

`TOMAR_PROCESO` asigna `responsableAcademico` o `responsableAdministrativo` según el rol de quien llama y devuelve el detalle. Errores: 409 `PROCESO_YA_TOMADO` (otro lo tomó primero), 409 `UNICO_RESPONSABLE` (baja).

Implementado hoy (CC-013): `TOMAR_PROCESO`, `DARSE_DE_BAJA`, `GUARDAR_MARCAS` y `AGREGAR_FALTANTE`, las dos últimas solo en `DOC-REV`. Del Sprint 5 (CC-019), `CARGAR_ENLACE_MEET`. Del Sprint 6 (CC-021), `ACTUALIZAR_ANALISIS`.

- `TOMAR_PROCESO` y `DARSE_DE_BAJA` no llevan body y devuelven 200 `SolicitudDetalle`. Una solicitud finalizada no se toma (409 `TRANSICION_INVALIDA`); quien no es participante recibe 403 `NO_PARTICIPANTE`, y quien se da de baja sin ser el responsable, 403 `ACCESO_DENEGADO`.
- La baja se permite cuando hay otra persona activa del mismo rol con permiso permanente sobre la carrera.
- Las dos avisan por mail a los demás del mismo rol con acceso a la carrera (RF-PROC-003 y 004) y dejan una fila de auditoría (`tomar_proceso`, `baja_responsable`).
- `GUARDAR_MARCAS` devuelve 200 `Documento[]` con los vigentes. Cada cambio de marca deja una fila de auditoría (`marca_documento`, con la marca anterior y la nueva).
- `AGREGAR_FALTANTE` exige `tipo` del catálogo y `descripcion`, y devuelve 201 `Documento` marcado `RECARGAR` y sin archivo.
- `CARGAR_ENLACE_MEET` la ejecuta el Académico responsable, o el Admin, solo en `REU-PEND`: en otro estado responde 409 `TRANSICION_INVALIDA`. El enlace tiene que empezar con `https://meet.google.com/` y no llevar espacios (400 `VALIDACION` con `errors.enlaceMeet`). Devuelve 200 `ReunionInfo`, avisa al aspirante por mail, deja una fila de auditoría (`enlace_meet`) y se puede repetir para corregirlo.
- `ACTUALIZAR_ANALISIS` la ejecuta el Académico responsable, o el Admin, solo en `ANAL-POST`: en otro estado responde 409 `TRANSICION_INVALIDA`. No lleva `estadoEsperado`. `tipo` es `MATRIZ_EQUIVALENCIAS` o `HORARIO_CURSADA` (400 `VALIDACION` con `errors.tipo`) y hace falta el archivo, las notas o las dos cosas (400 con `errors.notas`), con las mismas reglas que en `PROPONER_FECHAS`. Crea una versión nueva, que reemplaza entera a la anterior: si la nueva no trae archivo, el anterior deja de exponerse. Devuelve 200 `Analisis[]` con los dos documentos vigentes, avisa al aspirante por mail y deja una fila de auditoría (`actualizar_analisis`, con el tipo y la versión).

### 7.3 Bodies

**Marcas** (en `GUARDAR_MARCAS`, aceptar y rechazar documentación o legalizados)

```json
{
  "estadoEsperado": "DOC-REV",
  "comentario": "El analítico no está firmado",
  "marcas": [
    { "documentoId": 301, "estadoRevision": "RECARGAR", "comentario": "Falta la firma" },
    { "documentoId": 302, "estadoRevision": "OK", "comentario": null }
  ]
}
```

Para rechazar: al menos un `RECARGAR` y ningún documento en `PENDIENTE`, si no 409 `MARCAS_INCOMPLETAS`. `comentario` general obligatorio en todo rechazo (400 `VALIDACION` con `errors.comentario`). Si el rechazo no pasa, las marcas que traía tampoco se guardan.

Cada `documentoId` tiene que ser un documento vigente de la solicitud y cada comentario admite hasta 500 caracteres; si no, 400 `VALIDACION` con `errors.marcas`. Un requerido faltante solo admite `RECARGAR`. En aceptar, `marcas` es opcional.

**`PROPONER_FECHAS`** — `multipart/form-data`

| Parte | Tipo | Notas |
| --- | --- | --- |
| `matrizArchivo` | file | Archivo o notas: al menos uno |
| `matrizNotas` | string |  |
| `horarioArchivo` | file | Archivo o notas: al menos uno |
| `horarioNotas` | string |  |
| `datos` | JSON | `{ "estadoEsperado": "ANAL-PEND", "horarios": [{ "inicio": "2026-10-28T10:00:00-03:00" }] }` |

Reglas de `horarios` (también en `PROPONER_SEGUNDA_RONDA`): de 1 a 7, sin repetidos, duración fija de 1 hora, en punto y entre las 10:00 y las 18:00 de Argentina (el último empieza a las 17:00), desde mañana y hasta dentro de 30 días (CC-018). Si no cumplen, 400 `VALIDACION` con `errors.horarios`.

La parte `datos` viaja con `Content-Type: application/json`. Cada archivo es PDF o JPG de hasta 10 MB (400 `ARCHIVO_INVALIDO`, 413 `LIMITE_TAMANO`) y las notas admiten hasta 2000 caracteres. Si a la matriz o al horario de cursada les faltan el archivo y las notas, 400 `VALIDACION` con `errors.matriz` o `errors.horario`. No hay borradores: si algo falla no se guarda nada (CC-019). El mail al aspirante lista los horarios y avisa que los dos documentos están en su solicitud, sin adjuntarlos.

**Reunión**

```json
{ "estadoEsperado": "REU-FEC-ASP-1", "horarioId": 501 }
```

```json
{ "estadoEsperado": "REU-FEC-ASP-1", "motivo": "Trabajo en ese horario", "disponibilidad": "Martes y jueves después de las 16" }
```

```json
{ "estadoEsperado": "REU-CHAT", "inicio": "2026-11-04T15:00:00-03:00" }
```

En `SOLICITAR_OTRAS_FECHAS`, `motivo` y `disponibilidad` son obligatorios y admiten hasta 500 caracteres (400 `VALIDACION` con `errors.motivo` o `errors.disponibilidad`); quedan en la ronda y en el historial, sin contar como `ultimaObservacion`. En `RECHAZAR_SEGUNDA_RONDA`, `motivo` es opcional. En `ELEGIR_HORARIO`, `horarioId` tiene que ser un horario de la ronda vigente que todavía no pasó (400 `VALIDACION` con `errors.horarioId`).

En `FIJAR_FECHA`, `inicio` es obligatorio y tiene que ser a futuro (400 `VALIDACION` con `errors.inicio`). No sigue las reglas de `horarios`: la fecha la pautaron por chat, así que vale cualquier día y hora, y la reunión dura 1 hora (CC-020). No manda mail: el aspirante se entera de la fecha cuando el Académico carga el enlace de Meet.

**Resultado de la reunión e inscripción**

```json
{ "estadoEsperado": "REU-PEND", "notas": "Se acordó reconocer 6 materias" }
```

```json
{ "estadoEsperado": "REU-PEND", "comentario": "Esperé 20 minutos" }
```

```json
{ "estadoEsperado": "ANAL-POST", "comentario": "El plan de origen no es compatible" }
```

En `FINALIZAR_REUNION`, `notas` es opcional y admite hasta 2000 caracteres (400 `VALIDACION` con `errors.notas`): queda en la reunión y no va al historial. En `MARCAR_NO_ASISTIO`, `comentario` es opcional, admite hasta 500 caracteres y queda en el historial como observación para el aspirante; después el Académico vuelve a proponer fechas con `PROPONER_SEGUNDA_RONDA`, también si la 2ª ronda ya se había usado (interfaces, Anexo A #7). Ninguna de las dos exige que la hora de la reunión ya haya pasado. En `RECHAZAR_INSCRIPCION`, `comentario` es obligatorio y admite hasta 500 caracteres (400 con `errors.comentario`); `ACEPTAR_INSCRIPCION` lleva solo `estadoEsperado` (CC-021).

**Legalización**

```json
{ "estadoEsperado": "INSC-ACEP", "legalizados": false }
```

**Pago** — `ENVIAR_COMPROBANTE` es multipart con `archivo` (PDF o JPG, máx. 10 MB) y `estadoEsperado`. `RECHAZAR_PAGO`:

```json
{ "estadoEsperado": "PAG-CTRL", "comentario": "El monto no coincide con el cupón" }
```

## 8. Chat, notificaciones y auditoría

### 8.1 Chat de la solicitud (RF-MSG-001 a 004)

| Método | Ruta | Quién | Qué hace | Respuesta |
| --- | --- | --- | --- | --- |
| GET | `/solicitudes/{id}/chat/mensajes` | Dueño y personal (lectura) | Historial; `?despuesDe={mensajeId}` trae solo los nuevos | 200 `Page<Mensaje>` |
| POST | `/solicitudes/{id}/chat/mensajes` | Aspirante y responsables | Envía mensaje (multipart) | 201 `Mensaje` |
| POST | `/solicitudes/{id}/chat/leidos` | Aspirante y responsables | `{ "hastaMensajeId": 812 }` marca como visto | 204 |
| GET | `/solicitudes/{id}/chat/mensajes/{mensajeId}/adjunto` | Dueño y personal | Descarga el adjunto | 200 binario |

Partes del `POST`: `contenido` (máx. 500 caracteres), `tipoConsulta` (`DOCUMENTACION`, `ESTADO`, `CALCULOS`, `FECHAS`, `OTRO`) y `adjunto` opcional (máx. 5 MB). Sin responsable asignado el aspirante recibe 403 `CHAT_DESHABILITADO`; los observadores, 403 `NO_PARTICIPANTE`.

**`Mensaje`**

```json
{
  "id": 812,
  "autor": { "tipo": "USUARIO", "id": 7, "nombre": "María Gómez", "rol": "ACAD", "avatar": "mujer-03" },
  "contenido": "¿Podés subir el programa de Física I firmado?",
  "tipoConsulta": "DOCUMENTACION",
  "adjunto": null,
  "fechaEnvio": "2026-10-14T16:02:00-03:00",
  "visto": true,
  "esMio": false
}
```

Implementado hoy (CC-021):

- `GET` devuelve los mensajes del más viejo al más nuevo, con `page`, `size` (20 por defecto y 100 como máximo) y `despuesDe`. El frontend pide tandas de 100 con `despuesDe` hasta que una llega incompleta.
- `autor.nombre` es el nombre completo y `autor.rol`, el código del rol de quien escribió (`ASPIRANTE`, `ACAD` o `SECRE`): sirve para rotularlo aunque ya no sea el responsable.
- El mensaje lleva `contenido`, `adjunto` o las dos cosas (400 `VALIDACION` con `errors.contenido` si faltan los dos); `tipoConsulta` es obligatorio (400 con `errors.tipoConsulta`). El adjunto es PDF, JPG o PNG, reconocido por su contenido (400 `ARCHIVO_INVALIDO`), de hasta 5 MB (413 `LIMITE_TAMANO`).
- Escriben el aspirante dueño y los responsables académico y administrativo. Un participante que no es el responsable recibe 403 `NO_RESPONSABLE`; el Admin y el Revisor, 403 `ACCESO_DENEGADO`. Con la solicitud finalizada nadie escribe: 409 `TRANSICION_INVALIDA`.
- `visto` es `true` cuando lo leyó alguien que no es quien lo escribió. Solo cuentan las lecturas de quienes participan del chat: `POST /chat/leidos` responde 403 a los demás, 400 `VALIDACION` con `errors.hastaMensajeId` si el mensaje no es de esa solicitud, y nunca retrocede.
- No se manda mail por cada mensaje: el aviso es el contador `mensajesNoLeidos` del detalle.

**Actualización en vivo:** el frontend pide `?despuesDe=` cada 15 s mientras el chat está abierto. El refresco de `GET /solicitudes/{id}` al volver a la pestaña para detectar cambios de estado (interfaces §0.16.4) y WebSocket quedan para más adelante.

### 8.2 Notificaciones (campana)

| Método | Ruta | Quién | Qué hace | Respuesta |
| --- | --- | --- | --- | --- |
| GET | `/notificaciones` | Todos | `?leida=false` filtra las nuevas | 200 `Page<Notificacion>` |
| GET | `/notificaciones/no-leidas/cantidad` | Todos | Contador de la campana | 200 `{ "cantidad": 4 }` |
| PATCH | `/notificaciones/{id}` | Todos | `{ "leida": true }` | 204 |
| POST | `/notificaciones/marcar-todas-leidas` | Todos |  | 204 |

```json
{
  "id": 1203,
  "titulo": "Observaciones en tu documentación",
  "mensaje": "Documentos a recargar: Analítico",
  "solicitudId": 42,
  "leida": false,
  "fechaCreacion": "2026-10-14T15:30:00-03:00"
}
```

Los mails los manda el backend en cada transición (RF-ESTADO-002); el front no los dispara. Cada aviso que sale por mail queda además como notificación de quien lo recibe, con el asunto como `titulo` y el texto como `mensaje`; `solicitudId` es nulo en los que no son sobre un trámite (permiso asignado). `GET /notificaciones` trae las más nuevas primero, con `page` y `size` (10 por defecto). Cada uno lee y marca solo las suyas: marcar una ajena responde 404. El frontend pide el contador cada minuto.

### 8.3 Auditoría (RF-AUDIT-001 a 003, RF-REVISOR-001)

| Método | Ruta | Quién | Qué hace | Respuesta |
| --- | --- | --- | --- | --- |
| GET | `/auditoria` | Admin, Revisor | Filtros `usuario` (parte del email), `accion`, `entidad`, `entidadId`, `desde`, `hasta` (días de Argentina). Lo más nuevo primero | 200 `Page<EventoAuditoria>` |
| GET | `/auditoria/opciones` | Admin, Revisor | Acciones y entidades que ya tienen eventos, para armar los filtros | 200 `{ acciones: string[], entidades: string[] }` |
| GET | `/auditoria/{id}` | Admin, Revisor | Detalle completo | 200 `EventoAuditoria` |
| GET | `/solicitudes/{id}/auditoria` | Admin, Revisor | Pestaña Auditoría del detalle | 200 `Page<EventoAuditoria>` |

```json
{
  "id": 9921,
  "timestamp": "2026-10-14T15:30:00-03:00",
  "usuario": { "id": 7, "email": "m.gomez@usal.edu.ar" },
  "accion": "cambio_estado",
  "entidad": "solicitud",
  "entidadId": 42,
  "estadoCodigoAnterior": "DOC-REV",
  "estadoCodigoNuevo": "DOC-RECH",
  "detalles": { "documentosRecargar": [301] },
  "comentarios": "El analítico no está firmado",
  "ipOrigen": "10.0.0.15"
}
```

## 9. Administración

Todo lo de esta sección es solo para Admin General y vive bajo `/admin`.

| Método | Ruta | Qué hace | Respuesta | Requisito |
| --- | --- | --- | --- | --- |
| GET | `/admin/usuarios` | Lista personal @usal; filtros `rol`, `activo`, `q` | 200 `Page<UsuarioAdmin>` | RF-ADMIN-001 |
| POST | `/admin/usuarios` | Registra cuenta @usal con acceso básico y su contraseña inicial | 201 `UsuarioAdmin` | RF-ADMIN-001 |
| GET | `/admin/usuarios/{id}` | Detalle con permisos | 200 `UsuarioAdmin` | — |
| PATCH | `/admin/usuarios/{id}` | `{ nombre?, apellido?, rolCodigo?, activo?, password? }`. Con procesos activos, cambiar el rol o desactivar responde 409 `PROCESO_ACTIVO` | 200 `UsuarioAdmin` | — |
| POST | `/admin/usuarios/{id}/permisos/carreras` | `{ "carreraIds": [1, 3] }`. Saltea las que ya tiene, avisa al usuario por cada carrera nueva y devuelve la cuenta con todos sus permisos activos. Solo para Académico y Secretaría | 201 `UsuarioAdmin` | RF-ADMIN-003 |
| POST | `/admin/permisos/carreras/{permisoId}/revocar` | `{ forzar, comentario }` | 204 | RF-ADMIN-004 |
| POST | `/admin/permisos/solicitudes/{permisoId}/revocar` | `{ comentario }` | 204 | RF-ADMIN-004 |
| POST | `/admin/solicitudes/{id}/responsables` | Asigna o reasigna responsables | 200 `SolicitudDetalle` | RF-ADMIN-005 |
| GET | `/admin/alertas` | Procesos sin responsable y cuentas bloqueadas | 200 `Alertas` | RF-ADMIN-007 |
| POST | `/admin/cuentas/{tipo}/{id}/desbloquear` | `tipo` = `aspirante` o `usuario`; resetea intentos a 0 | 204 | RF-AUTH-002 |
| POST | `/admin/solicitudes/{id}/reabrir` | **A implementar**: depende de estados posteriores a `E3-INI` | — | RF-ADMIN-006 |

**`POST /admin/usuarios`** — request `{ "email": "l.diaz@usal.edu.ar", "nombre": "Laura", "apellido": "Díaz", "rolCodigo": "SECRE", "password": "…" }`. Dominio distinto, rol que no sea `ACAD`, `SECRE` o `REVISOR`, o contraseña que no cumple las reglas del registro → 400 `VALIDACION` con `errors.email`, `errors.rolCodigo` o `errors.password`; email existente → 409 `DUPLICADO`. No se envía mail. La contraseña es provisoria, hasta que el personal entre con Google (CC-016 y CC-024). El listado no incluye al Admin.

**`UsuarioAdmin`**

```json
{
  "id": 12,
  "email": "l.diaz@usal.edu.ar",
  "nombre": "Laura",
  "apellido": "Díaz",
  "rol": "SECRE",
  "activo": true,
  "bloqueadoHasta": null,
  "fechaUltimoLogin": "2026-10-13T09:12:00-03:00",
  "permisosCarrera": [
    { "id": 30, "carrera": { "id": 1, "nombre": "Ingeniería en Informática" }, "assignedAt": "2026-10-01T10:00:00-03:00" }
  ],
  "permisosSolicitud": [
    { "id": 4, "solicitud": { "id": 42, "codigoExp": "EXP-2026-001" }, "assignedAt": "2026-10-12T10:00:00-03:00", "expiresAt": null }
  ]
}
```

Solo se listan permisos activos (`revokedAt` nulo y, en los temporales, sin vencer).

**Revocar permiso de carrera** — con procesos activos y `forzar: false` responde 409 `PROCESO_ACTIVO`, y el `message` nombra los expedientes ("Proceso activo en EXP-2026-001. Para revocar…"). El front lo muestra, pide la segunda confirmación y reenvía con `forzar: true`: el usuario pierde el acceso, esos procesos quedan sin responsable y se avisa a los demás del rol con acceso a la carrera o, si no hay nadie, al Admin. Un proceso sobre el que el usuario conserva un permiso temporal no se toca. Volver a asignar una carrera revocada reactiva el mismo permiso. La revocación de un permiso temporal no pide confirmación: si el usuario era el responsable y no tiene permiso sobre la carrera, el proceso se libera igual.

**`POST /admin/solicitudes/{id}/responsables`**

```json
{
  "academicoId": 7,
  "administrativoId": null,
  "crearPermisoTemporal": true,
  "expiresAt": null,
  "comentario": "Reasignación por licencia"
}
```

`comentario` es obligatorio (400 con `errors.comentario`) y hace falta al menos un id (`errors.academicoId`). Un id nulo deja ese responsable como está. Cada elegido tiene que ser una cuenta activa del rol que corresponde (`errors.academicoId` o `errors.administrativoId`). Si no tiene permiso sobre la carrera y `crearPermisoTemporal` es `false`, responde 409 `SIN_PERMISO_CARRERA`. Con la solicitud finalizada responde 409 `TRANSICION_INVALIDA`. Avisa al nuevo responsable, al anterior y al aspirante, y el cambio aparece en el historial de la solicitud.

**`Alertas`**

```json
{
  "sinResponsableAcademico": [ { "id": 42, "codigoExp": "EXP-2026-001", "aspirante": "Juan Pérez", "carrera": "Ingeniería en Informática" } ],
  "sinResponsableAdministrativo": [],
  "cuentasBloqueadas": [ { "tipo": "aspirante", "id": 15, "nombre": "Juan Pérez", "bloqueadoHasta": "2026-10-14T21:30:00-03:00" } ]
}
```

La "Actividad reciente" del dashboard sale de `GET /auditoria?size=10`.

## 10. Resumen por rol y puntos abiertos

### 10.1 Qué puede hacer cada rol

| Grupo de endpoints | Aspirante | Académico | Secretaría | Admin | Revisor |
| --- | --- | --- | --- | --- | --- |
| Autenticación (§4.1) | Email y contraseña | Usuario y contraseña (provisorio); Google al publicar | Usuario y contraseña (provisorio); Google al publicar | Usuario y contraseña | Usuario y contraseña (provisorio); Google al publicar |
| Perfil (§4.2) | Edita | Edita (sin nombre ni email) | Edita (sin nombre ni email) | Edita, incluido email | Solo lee |
| Carreras y estados (§5) | Lee | Lee | Lee | Lee y escribe | Lee |
| Solicitudes, lectura (§6) | Solo las suyas | Todas | Todas | Todas | Todas |
| Crear solicitud y subir documentos (§6) | Sí | — | — | — | — |
| Transiciones del aspirante (§7.1) | Sí | — | — | — | — |
| Transiciones académicas y marcas (§7) | — | Si es participante y responsable | — | Sí, sin ser participante ni responsable (interfaces §7.0) | — |
| Cupón y verificación de pago (§7.1) | — | — | Si es participante y responsable | Sí, sin ser participante ni responsable (interfaces §7.0) | — |
| Tomar proceso y darse de baja (§7.2) | — | Si es participante | Si es participante | — | — |
| Chat (§8.1) | Escribe si hay responsable | Escribe si es responsable; si no, lee | Escribe si es responsable; si no, lee | Lee | Lee |
| Notificaciones (§8.2) | Sí | Sí | Sí | Sí | Sí |
| Auditoría (§8.3) | — | — | — | Lee | Lee |
| Administración (§9) | — | — | — | Sí | — |

"Participante" = permiso temporal activo sobre la solicitud, o permiso de carrera activo sobre su carrera destino (RF-AUTH-005). Sin eso, el usuario es observador.

### 10.2 Servicios del frontend

Un archivo por grupo dentro de `frontend/src/services/`, todos sobre `api` de `services/api/client.ts`.

| Archivo | Cubre |
| --- | --- |
| `auth.ts` | §4.1 |
| `perfil.ts` | §4.2 |
| `catalogos.ts` | §5 |
| `solicitudes.ts` | §6 |
| `acciones.ts` | §7 |
| `chat.ts` | §8.1 |
| `notificaciones.ts` | §8.2 |
| `auditoria.ts` | §8.3 |
| `admin.ts` | §9 |

Los DTOs de este documento van en `frontend/src/types/` con los mismos nombres (`SolicitudDetalle`, `Documento`, `Sesion`…).

### 10.3 Puntos abiertos para cerrar con backend

- [x] Sesión por cookie `HttpOnly` o por header `Authorization: Bearer`: cookie `HttpOnly` con JWT (CC-007).
- [x] Protección CSRF: cookie `XSRF-TOKEN` y header `X-XSRF-TOKEN`, que `client.ts` ya envía (§1, CC-015).
- [x] Dónde se guardan los datos de origen de la solicitud: en `solicitudes` (CC-009).
- [ ] Qué valores tiene "Condición académica" (Anexo A #2 de interfaces); hoy es texto libre.
- [x] Si el Admin puede ejecutar transiciones por "bypass" (sección 2 de requisitos) o solo asignar responsables: ejecuta las del Académico y de Secretaría, no las del aspirante (§7, CC-017).
- [ ] Descarga del expediente por el aspirante y reapertura: dependen de los estados posteriores a `E3-INI` (RF-ASP-008, RF-ADMIN-006).
- [ ] Cambios CC-001 a CC-005 (permisos por facultad), aprobados el 2026-11-19: al implementarlos cambian `/admin/usuarios/{id}/permisos/…` y `Sesion.carrerasConPermiso`.
