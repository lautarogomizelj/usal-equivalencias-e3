# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales - Versión 10.0

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-09-25 | lautarogomizelj | Carga inicial de requisitos funcionales y no funcionales |
| 2.0 | 2026-09-25 | lautarogomizelj | Agrega la descripción de paneles por estado |
| 3.0 | 2026-09-25 | lautarogomizelj | Incorpora participantes y roles, notas importantes y el resumen de paneles por estado |
| 4.0 | 2026-09-27 | lautarogomizelj | Reestructuración y simplificación del documento |
| 5.0 | 2026-09-27 | lautarogomizelj | Agrega la estructura de tablas clave |
| 6.0 | 2026-09-28 | lautarogomizelj | Módulos 4.1 a 4.13, estructura de tablas de permisos, queries de validación y regla crítica de `revoked_at` |
| 7.0 | 2026-09-29 | lautarogomizelj | Reescritura de los requisitos funcionales con códigos (RF-AUTH, RF-ASP, RF-ESTADO, RF-PROC, RF-ACAD…), RBAC simplificado y 16 estados explícitos |
| 8.0 | 2026-09-29 | lautarogomizelj | RF-ASP-004 de recarga selectiva por documento y RF-ACAD-009 de revisión de documentos; apéndice de equivalencias v6 → v8 |
| 9.0 | 2026-10-13 | lautarogomizelj | Reemplaza el catálogo de estados, la tabla de transiciones y las notificaciones por la nueva tabla de estados (18 estados, 23 transiciones) y elimina el diagrama de transiciones; actualiza los requisitos que dependen de estados (RF-ASP, RF-ACAD, RF-SEC, RF-PROC, RF-MSG); agrega RF-ASP-009; quita `reunion_realizada` y agrega `etapa_revision` en `documentos_solicitud`; deja marcados como pendientes los requisitos de estados aún no definidos |
| 10.0 | 2026-10-13 | lautarogomizelj | Permite al Académico actualizar la matriz de equivalencias y el horario de cursada desde `ANAL-POST` (RF-ACAD-002, RF-ACAD-004, RF-PROC-002); agrega la tabla versionada `analisis_academico`, la notificación al aspirante (RF-ESTADO-002) y la entrada de auditoría |

---

## 1. DESCRIPCIÓN GENERAL DEL SISTEMA

El **Sistema de Tramitación de Equivalencias Electrónicas (E3)** es una aplicación web diseñada para digitalizar, automatizar y hacer trazable el proceso de reconocimiento de equivalencias académicas en la Facultad de Ingeniería de USAL.

### Problema actual:
- El proceso depende completamente de correo electrónico y Google Drive
- No hay visibilidad sobre en qué etapa está cada solicitud
- El flujo depende de que una única persona (Académico) recuerde cuándo enviar cada mail
- Aspirantes y administrativos desconocen el estado de su trámite
- No hay auditoría clara de quién hizo qué y cuándo

### Solución propuesta:
Plataforma web centralizada donde:
- Cada rol (Aspirante, Académico, Secretaría Administrativa, Admin, Revisor Logs) tiene un panel personalizado
- El proceso avanza mediante transiciones de estado explícitas (18 estados)
- Los cambios de estado generan notificaciones automáticas por mail
- Hay trazabilidad completa y auditoría detallada de cada paso
- El almacenamiento es centralizado en la base de datos de la facultad (por año y DNI)
- La asignación de responsables es dinámica y flexible

---

## 2. OBJETIVO DEL SISTEMA

Proporcionar un flujo de tramitación de equivalencias **trazable, automatizado, auditable y descentralizado**, donde:
1. El aspirante inicia el trámite completando un formulario inicial y cargando documentación
2. Cada rol ve un panel con acciones determinadas según su responsabilidad y permisos
3. El proceso avanza mediante transiciones explícitas sin depender de terceros
4. Los responsables se asignan dinámicamente (no automático, sino por "tomar el proceso")
5. Hay registro auditable de cada acción, cambio, timestamp e IP
6. Las decisiones críticas requieren confirmación manual doble
7. El sistema de permisos es granular y flexible, permitiendo al admin bypass en casos necesarios

---

## 3. ACTORES DEL SISTEMA

### 3.1 Aspirante
- **Perfil:** Estudiante de otra universidad solicitando equivalencias para ingresar a carrera de Ingeniería
- **Método de acceso:** Crear cuenta en el sistema (email personal + código de validación con expiración 10 minutos)
- **Acciones principales:** Completar formulario inicial, cargar documentación, ver responsable(s) actual, enviar mensajes en chat, ver estado, recargar los documentos indicados por el Académico, elegir horario de reunión, indicar si sus documentos están legalizados, recargar los documentos legalizados rechazados, efectuar pago, visualizar informe
- **Visibilidad:** Solo su solicitud, documentos, matriz, horario; puede ver auditoría del proceso (visual)

### 3.2 Académico
- **Perfil:** Personal de área académica (profesor coordinador de carrera)
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Permisos:**
  - **Acceso Básico (default):** Ver TODAS las solicitudes de TODAS las carreras (lectura, botones deshabilitados)
  - **Acceso de Proceso Permanente:** Participar en equivalencias de carrera(s) específica(s) asignadas por Admin
  - **Acceso de Proceso Temporal:** Participar SOLO en solicitud específica (sin tener acceso permanente a carrera)
- **Ejemplo:** Christian LOPEZ PASARON (ch.lopezpasaran) — Ingeniería en Informática
- **Acciones:** Ver solicitudes, "Tomar proceso" (si hay acceso), realizar análisis, marcar cada documento (tilde/cruz), coordinar reunión, revisar, tomar decisiones, participar en chat grupal, descargar expedientes
- **Visibilidad documental:** Expediente completo de sus solicitudes activas + visualización (lectura) de colegas

### 3.3 Secretaría Administrativa
- **Perfil:** Personal de administración/secretaría académica
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Permisos:** Igual que Académico (acceso básico + acceso de proceso permanente por carrera + temporal)
- **Ejemplo:** Jimena Genoves
- **Acciones:** Iguales que Académico (ver solicitudes, participar en chat, tomar proceso, gestionar pago, descargar expedientes)
- **Visibilidad:** Expediente completo de todas las solicitudes (rol administrativo)

### 3.4 Admin General
- **Perfil:** Administrador del sistema (IT/Administración)
- **Método de acceso:** Usuario + Contraseña (hasheada, NO OAuth)
- **Permisos:** TODOS (puede cambiar: email, contraseña desde panel)
- **Acciones principales:**
  - Registrar nuevas cuentas @usal (académico o administrativo)
  - Administrar el catálogo de carreras
  - Asignar/revocar permisos de proceso por carrera
  - Asignar manualmente responsable a solicitud (con o sin acceso permanente a carrera)
  - Asignar permisos temporales para solicitud específica
  - Reasignar procesos activos (con confirmación doble)
  - Ver auditoría general (todos los cambios del sistema)
  - Reabrir expedientes cerrados
  - Ver perfil, cambiar avatar, etc.
  - Recibir alertas de procesos sin responsable
  - Desbloquear cuentas bloqueadas
- **Visibilidad:** Acceso total a todo el sistema

### 3.5 Revisor Logs
- **Perfil:** Personal de auditoría/compliance
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Permisos:** Ver todo, no modificar nada
- **Acciones:** Ver todas las solicitudes, auditoría general, historial de cambios
- **Visibilidad:** Lectura completa de expedientes, auditoría, logs (sin permisos de edición)

### 3.6 Usuario sin Permisos (Observador)
- Cualquier académico/administrativo sin acceso específico a una carrera
- Puede entrar al sistema, ver procesos, ver mensajes/auditoría pero **NO puede participar**
- Útil para que colegas vean en qué andan otros sin interferir

---

## 4. REQUISITOS FUNCIONALES

**Módulos:**
1. 4.1 Autenticación y Acceso
2. 4.2 Aspirante
3. 4.3 Estados y Notificaciones
4. 4.4 Gestión del Proceso (Académico y Secretaría)
5. 4.5 Comunicación (Chat)
6. 4.6 Administración
7. 4.7 Auditoría y Revisión

---

### 4.1 Módulo de Autenticación y Acceso

#### RF-AUTH-001: Registro de Aspirantes con Validación por Código
- Aspirante crea cuenta con:
  - Email personal
  - Contraseña (mínimo 8 caracteres, mayúscula, número, carácter especial)
  - Nombre completo
  - Teléfono
  - Número de documento (DNI)
- **Validación por código (NO link):**
  - Sistema envía código de 6 dígitos al email
  - **Expiración: 10 minutos**
  - Aspirante ingresa código en formulario
  - Si expira, puede solicitar nuevo código
  - Máximo 3 intentos fallidos → bloquea cuenta temporalmente (15 minutos)
- No puede iniciar trámite hasta validar

#### RF-AUTH-002: Login de Aspirantes con Bloqueo por Intentos
- Email + contraseña
- **Sistema de bloqueo por intentos fallidos:**
  - Máximo 3 intentos fallidos consecutivos
  - Después del 3er intento: cuenta **bloqueada por 6 horas**
  - Contador de intentos se reinicia automáticamente cada 6 horas
  - **Admin General puede:**
    - Ver cuentas bloqueadas en panel
    - Desbloquear manualmente (sin doble verificación necesaria)
    - Desbloquear resetea contador de intentos a 0
  - Aspirante bloqueado ve mensaje: "Tu cuenta está bloqueada por 6 horas. Contacta al admin si necesitas acceso urgente."
- Recupero de contraseña: email + código (expiración 10 minutos)
- Sesión con expiración (30 días con "recuérdame")
- Cada login registrado en auditoría (timestamp, IP)

#### RF-AUTH-003: Login OAuth @usal.edu.ar (Académico, Secretaría Administrativa y Revisor Logs)
- Aplica a los tres roles; el rol lo asigna el Admin al registrar la cuenta (RF-ADMIN-001)
- **Flujo:**
  1. Usuario clickea "Ingresar con Google"
  2. Google lo redirige a pantalla de login
  3. Usuario ingresa credenciales de `[nombre]@usal.edu.ar`
  4. Google autentica y retorna `email`, `sub` (google_id), `name`
  5. Sistema valida:
     - `dominio = email.split('@')[1]` debe ser `usal.edu.ar`
     - `email` debe existir en tabla `usuarios` y estar `activo = true`
  6. Si valida correctamente:
     - Crea sesión
     - Usuario entra al sistema
     - Registra login en auditoría (timestamp, IP)
  7. Si NO valida:
     - Mensaje: "Usuario no registrado en el sistema. Contacta al admin."
     - NO se crea sesión
- Sesión normal

#### RF-AUTH-004: Login de Admin General
- **Usuario + Contraseña (hasheada, a la vieja escuela)**
- NO OAuth
- Sesión con expiración (1 día)
- Cada login registrado en auditoría (timestamp, IP)
- **Cambio de credenciales desde panel:**
  - Puede cambiar email (con validación de dominio @usal.edu.ar)
  - Puede cambiar contraseña (con doble verificación)
  - Cambios quedan en auditoría

#### RF-AUTH-005: Gestión de Permisos basada en Roles (RBAC) - SIMPLIFICADO
- **Sistema de permisos de 2 niveles:**
  - **Acceso Básico (default):** Ver TODAS las solicitudes (lectura, botones deshabilitados)
  - **Acceso de Proceso:** Si existe fila en `user_career_permissions` o `user_solicitud_permissions` → Participante

- **Tablas de permisos (SIN permission_level):**
  - `user_career_permissions`: Usuario tiene acceso permanente a carrera (indefinido)
  - `user_solicitud_permissions`: Usuario tiene acceso temporal a solicitud específica

- **Lógica de validación (IMPORTANTE: SIEMPRE verificar `revoked_at`):**
  ```
  Si usuario intenta ACTUAR en solicitud X:
    1. ¿Tiene fila en user_solicitud_permissions para X?
       ├─ Fila existe + (expires_at IS NULL OR expires_at > NOW()) + revoked_at IS NULL
       │  → SÍ: PARTICIPANTE ✅
       │  → NO: ir a paso 2

    2. ¿Tiene fila en user_career_permissions para carrera de X?
       ├─ Fila existe + revoked_at IS NULL
       │  → SÍ: PARTICIPANTE ✅
       │  → NO: ir a paso 3

    3. → OBSERVADOR (botones deshabilitados) ❌
  ```
- La asignación y revocación de estos permisos se gestiona desde el módulo de Administración (RF-ADMIN-003, RF-ADMIN-004, RF-ADMIN-005)

#### RF-AUTH-006: Doble Verificación en Todas las Acciones Críticas
- Acciones que requieren 2 clicks + confirmación:
  - Aprobar solicitud
  - Rechazar solicitud
  - Cambiar responsable (admin asigna)
  - Revocar permisos (permanentes o temporales)
  - Reabrir expediente cerrado
  - Cambiar contraseña/email (admin)
  - Darse de baja como responsable
  - Tomar proceso
  - Desbloquear cuenta bloqueada
- **Flujo:** 1er click muestra modal de confirmación → 2do click ejecuta

#### RF-AUTH-007: Perfil de Usuario (todos los roles)
- Sección "Mi perfil":
  - **Avatar:**
    - Galerías por género
    - Cambiar selección
  - **Información:**
    - Nombre (si no es OAuth, editable)
    - Email (no editable para OAuth; para Admin, editable con doble verificación y validando dominio @usal.edu.ar)
    - Teléfono (editable)
  - **Seguridad:**
    - Cambiar contraseña (no aplica a OAuth; Admin sí, con doble verificación)
    - Historial de accesos (últimos 10 logins con IP, fecha, duración)
    - Sesiones activas (desconectar si hay múltiples)
  - Todos los cambios registrados en auditoría

---

### 4.2 Módulo de Aspirante

#### RF-ASP-001: Crear Solicitud (formulario inicial y creación)
- El aspirante (con cuenta validada) completa el formulario inicial ANTES de cargar documentación:
  - Datos personales (pre-rellenados de registro)
  - Facultad/Universidad de origen
  - Carrera de origen
  - **Carrera destino:** Dropdown de carreras precargadas en BD (por `codigo`)
  - Condición académica
  - Observaciones
- Validación de datos
- Al clickear "Crear solicitud":
  - Se genera ID: `EXP-2024-001`
  - Estado → **"Solicitud iniciada"** (código: `SOL-INI`)
  - **SIN asignación automática de responsable**
  - Se guarda en tabla `solicitudes`:
    ```
    id (PK)
    codigo_exp (VARCHAR, UNIQUE) = "EXP-2024-001"
    aspirante_id (FK)
    carrera_destino_id (FK a carreras)
    responsable_academico_id (FK, nullable)
    responsable_administrativo_id (FK, nullable)
    fecha_creacion (TIMESTAMP)
    fecha_ultima_mod (TIMESTAMP)
    fecha_ultimo_cambio_estado (TIMESTAMP)
    fecha_asignacion_academico (TIMESTAMP, nullable)
    fecha_asignacion_administrativo (TIMESTAMP, nullable)
    ```
  - Sistema manda mail AUTOMÁTICO a TODOS los usuarios con acceso de proceso a esa carrera:
    - Asunto: "E3-2024-001: Nueva solicitud en Ingeniería Informática"
    - Cuerpo: "Nueva solicitud del aspirante Juan Pérez. [Ver detalles] [Tomar proceso]"
    - Link directo al proceso
  - Aspirante ve: "Tu solicitud ha sido creada. Esperando que alguien tome el proceso."

#### RF-ASP-002: Cargar Documentación Inicial
- **Acciona:** Desde estado `SOL-INI`
- Aspirante carga documentos (Analítico, Plan, Programas)
- Validación: PDF/JPG, máx 10 MB individual, 50 MB total
- Cada documento queda registrado individualmente con estado de revisión inicial `PENDIENTE` (RF-ACAD-009)
- Al hacer click en "Enviar documentación":
  - **El SISTEMA automáticamente cambia estado a "Documentación pendiente de revisión"** (código: `DOC-REV`)
  - **Aspirante NO puede cambiar estado manualmente**
  - Mail al responsable (si lo hay): "Documentación pendiente de revisión. ID: EXP-2024-001"
  - Si no hay responsable aún, mail a todos con acceso: "Documentación lista para revisar para EXP-2024-001"
- La matriz de equivalencias, el posible horario de cursada y las fechas de reunión NO los carga el aspirante: los sube el Académico (RF-ACAD-002)

#### RF-ASP-003: Ver Estado de Solicitud y Responsables
- Panel muestra:
  - Estado actual (con código, ej: "DOC-REV - Documentación pendiente de revisión")
  - **Responsables** (destacados):
    - **Responsable Académico:** [Si aplica] Nombre + email clickeable + fecha asignación
    - **Responsable Administrativo:** [Si aplica] Nombre + email clickeable + fecha asignación
    - Si hay ambos, se muestran los dos
    - Si no hay responsable aún: "Esperando que alguien tome el proceso"
  - Timeline de cambios de estado (visual, ordenado DESC por fecha)
  - Documentación cargada (listado con fechas y la marca de revisión de cada documento: ✅ correcto / ❌ debe recargar / ⏳ pendiente de revisión)
  - Matriz de equivalencias y horario de cursada, una vez que el Académico los subió (RF-ACAD-002)
  - Botón para abrir chat (módulo 4.5)
  - Pestaña "Historial" con timeline visual (RF-AUDIT-002)

#### RF-ASP-004: Recargar Documentación (selectiva por documento)
- **Acciona:** Desde estado **"Documentación rechazada"** (`DOC-RECH`). El estado NO cambia de nombre; lo que se agrega es el detalle de qué documentos debe rehacer el aspirante
- El Académico marcó cada documento con tilde o cruz (RF-ACAD-009) y el sistema guardó cuáles debe recargar el aspirante
- Aspirante ve el motivo general de la observación y la lista de TODOS sus documentos, cada uno con su marca:
  - ✅ **Correcto:** no hay que hacer nada; el documento queda bloqueado (no se puede reemplazar)
  - ❌ **Debe recargar:** se muestra el comentario del Académico para ese documento y el botón "Cargar nuevo archivo"
  - Si el Académico agregó un documento requerido que faltaba (RF-ACAD-009), aparece como ❌ sin archivo y el aspirante debe cargarlo
- Aspirante solo puede reemplazar/cargar los documentos marcados con ❌
- Validación de archivos: igual que RF-ASP-002 (PDF/JPG, máx 10 MB individual, 50 MB total)
- El botón "Enviar documentación" se habilita solo cuando todos los documentos marcados con ❌ tienen un archivo nuevo
- Al enviar:
  - Sistema cambia estado a **"Documentación pendiente de revisión"** (`DOC-REV`) automáticamente
  - Cada documento recargado se guarda como una **nueva versión** (`version` + 1, `reemplaza_a_id` apunta a la anterior) con estado de revisión `PENDIENTE`
  - La versión anterior NO se borra: queda archivada y disponible para el historial y el expediente
  - Los documentos ✅ conservan su marca
  - Mail al Académico (RF-ESTADO-002)
- La recarga de documentos legalizados rechazados (`LEG-RECH`) se resuelve en RF-ASP-009

#### RF-ASP-005: Elegir Horario de Reunión
- **Acciona:** Desde estado **"Reunión pendiente de fechas aspirante"** (`REU-FEC-ASP`)
- Aspirante recibe mail y ve en su panel las fechas disponibles propuestas por el Académico (hasta 7, duración 1 hora), junto con la matriz de equivalencias y el posible horario de cursada que el Académico subió (RF-ACAD-002)
- El sistema lleva un contador de rondas de fechas (**máximo 2**); `REU-FEC-ASP` es el mismo estado en ambas rondas
- **En la 1ª ronda** el aspirante puede:
  - **Seleccionar fecha** (una sola) → Estado **"Reunión pendiente de realización"** (`REU-PEND`)
  - **Solicitar otras fechas:** envía el motivo y el detalle de cuándo puede reunirse → Estado **"Reunión pendiente de fechas académico"** (`REU-FEC-ACAD`)
- **En la 2ª ronda** el aspirante puede:
  - **Seleccionar fecha** (una sola) → `REU-PEND`
  - **Rechazar la 2ª ronda de fechas** → Estado **"Resolución fecha por chat"** (`REU-CHAT`)
- Al elegir una fecha NO hay confirmación posterior del Académico: el Académico solo incluye el enlace de Meet (RF-ACAD-003)
- Si tras 2 rondas no hay acuerdo, se resuelve la fecha por chat (RF-ACAD-003)

#### RF-ASP-006: Ver Informe de Materias
- **Acciona:** Desde estado **"Solicitud de cupón de pago"** (`CUPON-SOL`) en adelante
- Aspirante ve resumen de equivalencias (solo lectura)
- Tabla: materias reconocidas, carga horaria, correlativas
- Cantidad a adeudar, observaciones, monto a pagar

#### RF-ASP-007: Efectuar Pago
- **Acciona:** Desde estado **"Pago de cupón por aspirante"** (`CUPON-PAG`) o **"Reenvío de comprobante"** (`PAG-REENV`)
- Aspirante descarga el cupón de pago enviado por Secretaría
- Carga comprobante de pago (imagen JPG/PDF)
- Validación de tamaño (máx 10 MB)
- Al enviar: Sistema cambia estado a **"Control de pago"** (`PAG-CTRL`)
- Si el pago fue rechazado, ve el comentario de Secretaría y puede reenviar el comprobante (vuelve a `PAG-CTRL`)

#### RF-ASP-008: Descargar Expediente
- ⚠ **PENDIENTE DE DEFINICIÓN:** este requisito dependía del estado `FINAL` (Expediente finalizado), que no existe en la tabla de estados vigente. Se completará cuando se definan los estados posteriores a `E3-INI`
- Contenido previsto (sin cambios respecto de la v8): el aspirante puede descargar ZIP con:
  - Solicitud inicial (JSON/PDF)
  - Documentos cargados
  - Matriz de equivalencias
  - Horario propuesto
  - Disposición decanal
  - Comprobante de pago
- Archivo: `EXP-2024-001_Expediente_[DNI].zip`

#### RF-ASP-009: Indicar Legalidad de Documentos y Reenviar Documentos Legalizados
- **Indicar legalidad**
  - **Acciona:** Desde estado **"Inscripción aceptada"** (`INSC-ACEP`)
  - El aspirante responde únicamente si su documentación está legalizada (no carga archivos en este paso)
  - Al confirmar, el SISTEMA cambia estado a **"Análisis de documentos legalizados"** (`LEG-REV`)
  - Mail al Académico (RF-ESTADO-002)
  - ⚠ **Pendiente de definir:** qué ocurre si el aspirante responde que su documentación NO está legalizada. La tabla de estados vigente no tiene transición para ese caso; la solicitud permanece en `INSC-ACEP`
- **Reenviar documentos legalizados**
  - **Acciona:** Desde estado **"Documentos legalizados rechazados"** (`LEG-RECH`)
  - El Académico marcó cada documento con tilde o cruz (RF-ACAD-009, etapa `LEGALIZACION`) y el sistema guardó cuáles debe recargar el aspirante
  - Aspirante ve el motivo general de la observación y la lista de TODOS sus documentos, cada uno con su marca (✅ correcto, bloqueado / ❌ debe recargar, con el comentario del Académico y el botón "Cargar nuevo archivo")
  - Aspirante solo puede reemplazar/cargar los documentos marcados con ❌
  - Validación de archivos: igual que RF-ASP-002 (PDF/JPG, máx 10 MB individual, 50 MB total)
  - El botón "Enviar documentación" se habilita solo cuando todos los documentos marcados con ❌ tienen un archivo nuevo
  - Al enviar:
    - Sistema cambia estado a **"Análisis de documentos legalizados"** (`LEG-REV`) automáticamente
    - Cada documento recargado se guarda como una **nueva versión** (`version` + 1, `reemplaza_a_id` apunta a la anterior) con estado de revisión `PENDIENTE`; la versión anterior NO se borra
    - Los documentos ✅ conservan su marca
    - Mail al Académico (RF-ESTADO-002)

---

### 4.3 Módulo de Estados y Notificaciones

#### RF-ESTADO-001: Estados de la Solicitud y Transiciones

**Reglas generales:**
- El estado de una solicitud solo cambia por una transición definida en este requisito
- El cambio lo ejecuta el SISTEMA como consecuencia de una acción de un actor (el Aspirante nunca cambia el estado manualmente)
- Las decisiones críticas exigen doble verificación (RF-AUTH-006)
- Todo cambio de estado se registra en `estados_solicitud` y en auditoría (RF-ESTADO-003, RF-ESTADO-004)
- Esta tabla es la fuente de verdad de lo que permite hacer el sistema: ninguna acción puede cambiar el estado si no figura como transición
- La tabla está incompleta: los estados posteriores a `E3-INI` se definirán en próximas versiones

**Catálogo de estados (18):**

| # | Código | Estado | Descripción | Tipo |
|---|---|---|---|---|
| 1 | `SOL-INI` | Solicitud iniciada | Aspirante acaba de crear la solicitud, no ha cargado documentación | Inicial |
| 2 | `DOC-REV` | Documentación pendiente de revisión | El aspirante envió la documentación y espera la revisión del Académico | Intermedio |
| 3 | `DOC-RECH` | Documentación rechazada | Hay observaciones sobre la documentación; el sistema guarda cuáles documentos debe recargar el aspirante (RF-ACAD-009) | Intermedio |
| 4 | `ANAL-PEND` | Análisis preliminar pendiente | Documentación aceptada; el Académico debe armar la matriz de equivalencias, el posible horario de cursada y las fechas disponibles para la reunión | Intermedio |
| 5 | `REU-FEC-ASP` | Reunión pendiente de fechas aspirante | El aspirante debe elegir una de las fechas propuestas por el Académico, solicitar otras o (en la 2ª ronda) rechazarlas | Intermedio |
| 6 | `REU-FEC-ACAD` | Reunión pendiente de fechas académico | El aspirante solicitó otras fechas (motivo y detalle de cuándo puede); el Académico debe sugerir una 2ª ronda | Intermedio |
| 7 | `REU-CHAT` | Resolución fecha por chat | El aspirante rechazó la 2ª ronda; aspirante y Académico pautan la fecha por chat (se abre por defecto) y el Académico la fija | Intermedio |
| 8 | `REU-PEND` | Reunión pendiente de realización | Fecha de reunión definida (el Académico incluye el enlace de Meet); falta realizar la reunión | Intermedio |
| 9 | `ANAL-POST` | Análisis pos reunión | La reunión finalizó; espacio previo a la aceptación/rechazo de la inscripción, donde el Académico puede actualizar la matriz de equivalencias y el horario de cursada si hace falta | Intermedio |
| 10 | `INSC-ACEP` | Inscripción aceptada | Análisis preliminar/equivalencias aceptado; el aspirante debe indicar si sus documentos están legalizados | Intermedio |
| 11 | `INSC-RECH` | Inscripción rechazada | Análisis preliminar/equivalencias rechazado; se termina el proceso | **Terminal** |
| 12 | `LEG-REV` | Análisis de documentos legalizados | El aspirante indicó la legalidad de sus documentos; el Académico los analiza | Intermedio |
| 13 | `LEG-RECH` | Documentos legalizados rechazados | Hay observaciones sobre los documentos legalizados; el sistema guarda cuáles debe recargar el aspirante (RF-ACAD-009) | Intermedio |
| 14 | `CUPON-SOL` | Solicitud de cupón de pago | Documentos legalizados aceptados; se informa al aspirante la cantidad de materias y Secretaría debe enviar el cupón de pago | Intermedio |
| 15 | `CUPON-PAG` | Pago de cupón por aspirante | Cupón enviado; esperando que el aspirante pague y cargue el comprobante | Intermedio |
| 16 | `PAG-CTRL` | Control de pago | Comprobante cargado; Secretaría controla el pago | Intermedio |
| 17 | `PAG-REENV` | Reenvío de comprobante | El pago fue rechazado; el aspirante debe reenviar el comprobante | Intermedio |
| 18 | `E3-INI` | Inicio de E3 | Pago aceptado; inicio de E3. Último estado cargado en la tabla vigente | Intermedio |

**Tabla de transiciones:**

| # | Actor | Acción | De | A | Comentario | Requisito |
|---|---|---|---|---|---|---|
| 1 | Aspirante | Crear solicitud | (inicio) | `SOL-INI` | | RF-ASP-001 |
| 2 | Aspirante | Enviar documentación (cambio automático del sistema) | `SOL-INI` | `DOC-REV` | | RF-ASP-002 |
| 3 | Académico | Rechaza documentación | `DOC-REV` | `DOC-RECH` | Marca los documentos a recargar (tilde/cruz) | RF-ACAD-001, RF-ACAD-009 |
| 4 | Aspirante | Reenviar documentación | `DOC-RECH` | `DOC-REV` | Recarga los documentos marcados con ❌ | RF-ASP-004 |
| 5 | Académico | Acepta | `DOC-REV` | `ANAL-PEND` | | RF-ACAD-001 |
| 6 | Académico | Sube documentación y sugiere fechas disponibles | `ANAL-PEND` | `REU-FEC-ASP` | El Académico sube la matriz de equivalencias, el horario de cursada y las fechas disponibles. Se le manda al aspirante los horarios disponibles del Académico para la reunión junto con los documentos | RF-ACAD-002 |
| 7 | Aspirante | Solicita otras fechas | `REU-FEC-ASP` | `REU-FEC-ACAD` | El aspirante envía motivo y detalle de cuándo puede reunirse | RF-ASP-005 |
| 8 | Académico | Sugiere 2ª ronda de fechas | `REU-FEC-ACAD` | `REU-FEC-ASP` | | RF-ACAD-003 |
| 9 | Aspirante | Seleccionar fechas | `REU-FEC-ASP` | `REU-PEND` | El Académico incluye un enlace de Meet para la reunión | RF-ASP-005, RF-ACAD-003 |
| 10 | Aspirante | Rechaza 2ª ronda de fechas | `REU-FEC-ASP` | `REU-CHAT` | Tanto al Académico como al aspirante se les pone por defecto el chat para que pauten una fecha | RF-ASP-005, RF-MSG-001 |
| 11 | Académico | Fijar fecha | `REU-CHAT` | `REU-PEND` | Al tocar el botón se abre un modal preguntando la fecha pautada para la reunión | RF-ACAD-003 |
| 12 | Académico | Indica que la reunión finalizó | `REU-PEND` | `ANAL-POST` | Espacio previo a la aceptación/rechazo de la inscripción, en donde, si hace falta, el Académico puede actualizar la matriz de equivalencias y el horario de cursada | RF-ACAD-004 |
| 13 | Académico | Aceptar análisis preliminar/equivalencias | `ANAL-POST` | `INSC-ACEP` | | RF-ACAD-006 |
| 14 | Académico | Rechazar análisis preliminar/equivalencias | `ANAL-POST` | `INSC-RECH` | Se termina el proceso | RF-ACAD-006 |
| 15 | Aspirante | Indica la legalidad de sus documentos | `INSC-ACEP` | `LEG-REV` | El aspirante tiene que responder si su documentación está legalizada | RF-ASP-009 |
| 16 | Académico | Rechaza documentos | `LEG-REV` | `LEG-RECH` | Marca los documentos a recargar (tilde/cruz) | RF-ACAD-005, RF-ACAD-009 |
| 17 | Aspirante | Reenvía documentos legalizados | `LEG-RECH` | `LEG-REV` | Recarga los documentos marcados con ❌ | RF-ASP-009 |
| 18 | Académico | Acepta documentos | `LEG-REV` | `CUPON-SOL` | Al aspirante se le informa la cantidad de materias | RF-ACAD-005 |
| 19 | Administrativo | Envío del cupón de pago | `CUPON-SOL` | `CUPON-PAG` | | RF-SEC-001 |
| 20 | Aspirante | Envía comprobante | `CUPON-PAG` | `PAG-CTRL` | Se realiza la transferencia y se adjunta el comprobante a modo de confirmación | RF-ASP-007 |
| 21 | Administrativo | Pago rechazado | `PAG-CTRL` | `PAG-REENV` | | RF-SEC-002 |
| 22 | Aspirante | Reenvía comprobante | `PAG-REENV` | `PAG-CTRL` | | RF-ASP-007 |
| 23 | Administrativo | Pago aceptado | `PAG-CTRL` | `E3-INI` | | RF-SEC-002 |

**Notas sobre las transiciones:**
- **Rondas de fechas (transiciones 6 a 10):** `REU-FEC-ASP` es el mismo estado en las dos rondas, por eso el sistema lleva un contador de rondas (máximo 2). En la 1ª ronda el aspirante puede seleccionar fecha (9) o solicitar otras (7); en la 2ª puede seleccionar fecha (9) o rechazar la ronda (10)
- **Selección de fecha:** al seleccionar fecha el aspirante pasa directamente a `REU-PEND`; no hay confirmación posterior del Académico
- **Rechazos con marca por documento (transiciones 3 y 16):** en ambos casos se aplica la revisión por documento de RF-ACAD-009 (al menos un ❌ y ningún documento en `PENDIENTE`)
- **Casos sin transición definida (pendientes):** que el aspirante responda que sus documentos NO están legalizados (`INSC-ACEP`); que el aspirante no se presente a la reunión (`REU-PEND`); reapertura de expedientes (RF-ADMIN-006); estados posteriores a `E3-INI`

#### RF-ESTADO-002: Notificaciones por Cambio de Estado

Cuando el estado cambia, el sistema envía mail automático al actor siguiente relevante:

| De Estado | A Estado | Mail a | Asunto/Contenido |
|---|---|---|---|
| (inicio) | Solicitud iniciada | Usuarios con acceso de proceso a la carrera | "Nueva solicitud del aspirante [nombre]. [Ver detalles] [Tomar proceso]" (RF-ASP-001) |
| Solicitud iniciada | Documentación pendiente de revisión | Académico | "Nueva solicitud de equivalencias con documentación pendiente de revisión. ID: [...]" |
| Documentación pendiente de revisión | Documentación rechazada | Aspirante | "Observaciones en tu documentación. Motivo: [comentario del Académico]. Documentos a recargar: [lista de documentos marcados con ❌]" |
| Documentación rechazada | Documentación pendiente de revisión | Académico | "Aspirante ha recargado documentación. ID: [...]" |
| Documentación pendiente de revisión | Análisis preliminar pendiente | Aspirante | "Tu documentación fue aceptada. Tu solicitud está siendo analizada por nuestro equipo académico." |
| Análisis preliminar pendiente | Reunión pendiente de fechas aspirante | Aspirante | "Elegí un horario para la reunión. Horarios propuestos: [...]. Se adjuntan la matriz de equivalencias y el posible horario de cursada." |
| Reunión pendiente de fechas aspirante | Reunión pendiente de fechas académico | Académico | 🆕 "El aspirante solicitó otras fechas para la reunión. Motivo y disponibilidad: [...]. ID: [...]" |
| Reunión pendiente de fechas académico | Reunión pendiente de fechas aspirante | Aspirante | 🆕 "El Académico propuso una segunda ronda de fechas para la reunión. Horarios propuestos: [...]" |
| Reunión pendiente de fechas aspirante | Reunión pendiente de realización | Académico | 🆕 "El aspirante eligió la fecha [fecha/hora] para la reunión. Incluí el enlace de Meet. ID: [...]" |
| Reunión pendiente de fechas aspirante | Resolución fecha por chat | Académico y Aspirante | 🆕 "La 2ª ronda de fechas fue rechazada. Pauten la fecha de la reunión por el chat de la solicitud. ID: [...]" |
| Resolución fecha por chat | Reunión pendiente de realización | (interno) | - |
| Reunión pendiente de realización | Análisis pos reunión | Aspirante | "La reunión se realizó. Tu solicitud está siendo analizada para definir tu inscripción." |
| Análisis pos reunión | Inscripción aceptada | Aspirante | "Tu inscripción fue aceptada. Ingresá a tu cuenta e indicá si tus documentos están legalizados." |
| Análisis pos reunión | Inscripción rechazada | Aspirante | "Lamentablemente tu solicitud no pudo ser aprobada. Motivo: [...]" |
| Análisis pos reunión | Análisis pos reunión (sin cambio de estado) | Aspirante | 🆕 "El Académico actualizó la matriz de equivalencias / el horario de cursada de tu solicitud. ID: [...]" |
| Inscripción aceptada | Análisis de documentos legalizados | Académico | 🆕 "El aspirante indicó que su documentación está legalizada. Revisá los documentos. ID: [...]" |
| Análisis de documentos legalizados | Documentos legalizados rechazados | Aspirante | 🆕 "Observaciones en tus documentos legalizados. Motivo: [comentario del Académico]. Documentos a recargar: [lista de documentos marcados con ❌]" |
| Documentos legalizados rechazados | Análisis de documentos legalizados | Académico | 🆕 "Aspirante ha reenviado documentos legalizados. ID: [...]" |
| Análisis de documentos legalizados | Solicitud de cupón de pago | Secretaría | "Solicitud lista para gestión de pago. ID: [...]" |
| Análisis de documentos legalizados | Solicitud de cupón de pago | Aspirante | "Tus documentos fueron aceptados. Cantidad de materias: [...]. Detalle de equivalencias: [matriz]" |
| Solicitud de cupón de pago | Pago de cupón por aspirante | Aspirante | "Tu cupón de pago está listo. Descargalo aquí: [link]. Monto: $[...]" |
| Pago de cupón por aspirante | Control de pago | (interno) | - |
| Control de pago | Reenvío de comprobante | Aspirante | "Hay un problema con tu comprobante. Detalle: [comentario de Secretaría]. Por favor reenvía." |
| Reenvío de comprobante | Control de pago | Secretaría | "El aspirante reenvió el comprobante. ID: [...]" |
| Control de pago | Inicio de E3 | Aspirante | "Tu pago ha sido verificado correctamente. Gracias." |

- Los mails son SOLO notificaciones; no son el medio de comunicación principal (para eso está el chat, módulo 4.5)
- 🆕 marca los mails redactados en la v9 que no tienen antecedente en la v8
- **Enlace de Meet:** cuando el Académico incluye el enlace en `REU-PEND` (no es una transición), el sistema envía al aspirante: "Tu reunión fue confirmada para [fecha/hora]. Link: [Meet]"
- **Actualización de matriz/horario en `ANAL-POST`:** cuando el Académico actualiza la matriz de equivalencias o el horario de cursada (RF-ACAD-004), **no hay cambio de estado**; el sistema envía el mail al aspirante de la fila "Análisis pos reunión → Análisis pos reunión"
- Las notificaciones de los estados posteriores a `E3-INI` (disposición generada, expediente finalizado) quedan pendientes hasta que se definan esos estados

#### RF-ESTADO-003: Estructura de BD de Estados
- Tabla `estados` (catálogo):
  - `id` (auto-incremental, PK)
  - `codigo` (VARCHAR, UNIQUE) = "SOL-INI", "DOC-REV", "ANAL-PEND", etc.
  - `nombre` (VARCHAR) = "Solicitud iniciada", "Documentación pendiente de revisión", etc.
  - `descripcion` (TEXT)
  - `es_terminal` (BOOLEAN) — si es estado final (`INSC-RECH`)
  - `activo` (BOOLEAN)
- Tabla `estados_solicitud` (historial de estados de cada solicitud):
  ```
  id (PK)
  solicitud_id (FK a solicitudes)
  estado_codigo (VARCHAR, FK a estados.codigo) ← REFERENCIA POR CÓDIGO, NO NOMBRE
  fecha_asignacion (TIMESTAMP)
  fecha_ultima_modificacion (TIMESTAMP)
  usuario_ultimo_cambio_id (FK a usuarios)
  estado_anterior_codigo (VARCHAR) — para auditoría
  comentarios (TEXT)
  ```

#### RF-ESTADO-004: Referencias en Auditoría
- Cuando auditoría registra cambio de estado:
  - Se guarda siempre `estado_anterior_codigo` y `estado_nuevo_codigo`
  - **NUNCA se usa nombre de estado**, solo código
  - Esto permite que si en futuro se renombran estados, auditoría sigue siendo válida

---

### 4.4 Módulo de Gestión del Proceso (Académico y Secretaría Administrativa)

Ambos roles comparten permisos, bandeja, vista detallada y mecanismo de asignación. Lo específico de cada uno se detalla en las etapas 4.4.2 (Académico) y 4.4.3 (Secretaría).

#### 4.4.1 Funcionalidades comunes

##### RF-PROC-001: Bandeja de Solicitudes (ordenamiento)
- **Listado con filtros:**
  - Por estado (dropdown con los 18 estados)
  - Por fecha (última actualización, rango)
  - Búsqueda por ID solicitud, nombre aspirante, carrera
  - Mis asignadas vs. Todas

- **Ordenamiento:**
  - **Si usuario TIENE carreras asignadas (filas activas en user_career_permissions):**
    - Mostrar PRIMERO solicitudes de esas carreras (agrupadas por carrera)
    - Luego resto de solicitudes (ordenadas alfabéticamente por carrera)
  - **Si usuario NO tiene carreras asignadas (acceso básico):**
    - Mostrar todas las solicitudes ordenadas alfabéticamente por carrera

- **Columnas del listado:**
  - ID solicitud (EXP-2024-001)
  - Aspirante (nombre)
  - Universidad origen
  - Carrera destino
  - Estado actual (con código)
  - Responsable actual (si hay)
  - Última actualización (fecha)
  - Botones: "Ver detalles", "Tomar proceso" (si aplica)

##### RF-PROC-002: Vista Detallada de Solicitud
- **Panel con pestañas:**
  1. **Información General:**
     - Datos del aspirante (nombre, email, teléfono, DNI, universidad origen)
     - Carrera de destino
     - Responsables actuales (académico + administrativo si hay)
     - Estado actual
     - Fechas clave (creación, última mod, último cambio estado)
  2. **Documentación:**
     - Lista de documentos cargados (con fecha, tamaño, descarga)
     - Marca de revisión de cada documento (✅ / ❌ / ⏳) y comentario por documento; el Académico responsable puede modificarla (RF-ACAD-009)
     - Botón para descargar todo en ZIP
  3. **Análisis Preliminar:**
     - Matriz de equivalencias (descargable: solo la versión vigente)
     - Horario de cursada (descargable: solo la versión vigente)
     - Botón: "Subir matriz, horario de cursada y fechas disponibles" (solo desde `ANAL-PEND`)
     - Botones: "Actualizar matriz de equivalencias" y "Actualizar horario de cursada" (desde `ANAL-POST`, RF-ACAD-004)
  4. **Reunión:**
     - Calendario si está en etapa de propuesta
     - Horarios propuestos (editable si es académico asignado)
     - Fecha elegida por el aspirante
     - Botones del Académico según el estado: "Sugerir 2ª ronda de fechas" (`REU-FEC-ACAD`), "Fijar fecha" (`REU-CHAT`), campo "Enlace de Meet" (`REU-PEND`), "Indicar que la reunión finalizó" (`REU-PEND`)
  5. **Chat:**
     - Historial de mensajes
     - Campo para enviar mensaje
  6. **Historial:**
     - Timeline visual de cambios de estado
     - Cambios de responsable
     - Documentación cargada
  7. **Auditoría:**
     - (Solo para académicos con permiso especial o admin)
     - Tabla con todos los cambios registrados
- Los botones de acción se habilitan solo si el usuario es PARTICIPANTE (RF-AUTH-005) y el estado actual lo permite; en caso contrario quedan deshabilitados (modo observador)

##### RF-PROC-003: "Tomar Proceso" - Asignación Dinámica de Responsables
- **No hay asignación automática:** al crear la solicitud queda sin responsable y se notifica a todos los usuarios con acceso a esa carrera (RF-ASP-001)
- Cuando académico/administrativo hace click en "Tomar proceso":
  - Se abre modal de confirmación
  - Muestra: "Eres responsable de este proceso a partir de ahora"
  - Botón: "Confirmar"
  - Al confirmar:
    - Sistema asigna: `responsable_academico` o `responsable_administrativo` (según rol)
    - Guarda: `fecha_asignacion_academico` o `fecha_asignacion_administrativo` (timestamp)
    - **El estado NO cambia** (tomar el proceso no es una transición)
    - Se envía mail a TODOS los demás con acceso a esa carrera:
      - Asunto: "E3-2024-001: Christian ha tomado este proceso"
      - Contenido: "Christian LOPEZ PASARON es ahora responsable. Ya no puedes tomar este proceso."
    - En el panel, el botón "Tomar proceso" desaparece para otros
    - Se registra en auditoría (acción: "tomar_proceso")
- El responsable académico actúa en las etapas académicas y el administrativo en las etapas de Secretaría (4.4.3)

##### RF-PROC-004: Darse de Baja como Responsable
- Académico/Administrativo actualmente responsable puede "Darse de baja"
- **Validación:**
  - Si es el ÚNICO con acceso a esa carrera en ese proceso:
    - Botón deshabilitado
    - Tooltip: "No puedes darte de baja. Eres el único responsable. Contacta al admin."
  - Si HAY otros con acceso a esa carrera:
    - Botón habilitado
    - Click → Modal de confirmación (doble verificación)
    - "¿Deseas darte de baja? El proceso vuelve a estar disponible."
    - Confirmar → Sistema:
      - Manda mail a TODOS los demás con acceso a carrera
      - Pone `responsable_academico` o `responsable_administrativo` a NULL
      - Registra en auditoría (acción: "baja_responsable")
- La reasignación por parte del Admin está en RF-ADMIN-005

##### RF-PROC-005: Exportación de Expedientes
- Botón: "Descargar expediente"
- Opciones:
  - Descargar expediente individual en ZIP
  - Descargar múltiples (filtro por estado, fecha, etc.)
  - Exportación consolidada: `Expedientes_2024_[fecha].zip`
- Cada descarga registra en auditoría (usuario, timestamp, qué descargó)

#### 4.4.2 Etapa Académica

##### RF-ACAD-001: Revisar Documentación Inicial
- **Acciona:** Desde estado **"Documentación pendiente de revisión"** (`DOC-REV`)
- Académico accede a solicitud
- Ve documentación del aspirante (descargable)
- Marca cada documento con ✅ o ❌ (RF-ACAD-009)
- Decisión:
  - **"Aceptar documentación"**
    - Doble verificación (1er click muestra modal, 2do click ejecuta)
    - Sistema cambia estado a **"Análisis preliminar pendiente"** (`ANAL-PEND`)
    - Registra en auditoría
    - Mail al aspirante: "Tu documentación fue aceptada..."
  - **"Rechazar documentación"** (comentario obligatorio y al menos un documento marcado con ❌, RF-ACAD-009)
    - Doble verificación
    - Sistema cambia estado a **"Documentación rechazada"** (`DOC-RECH`)
    - Mail al aspirante con el motivo y la lista de documentos a recargar

##### RF-ACAD-002: Análisis Preliminar y Propuesta de Fechas
- **Acciona:** Desde estado **"Análisis preliminar pendiente"** (`ANAL-PEND`)
- Es donde el Académico sube por primera vez la matriz de equivalencias, el horario de cursada y las fechas disponibles (la matriz y el horario también se pueden **actualizar desde `ANAL-POST`**, RF-ACAD-004). Las fechas disponibles solo se proponen desde `ANAL-PEND`/`REU-FEC-ACAD` (RF-ACAD-003)
- Académico prepara:
  - **Matriz de equivalencias** (puede cargar archivo o escribir notas)
  - **Posible horario de cursada** (puede cargar archivo o escribir notas)
  - **Fechas disponibles para la reunión:** abre **calendario integrado** y propone hasta **7 horarios disponibles** (próximos 14-30 días), duración 1 hora (10:00-18:00, franjas académicas)
- Botón: "Guardar propuesta"
  - Sistema envía al aspirante las fechas junto con los documentos (matriz y horario de cursada)
  - Mail al aspirante: "Elegí un horario para la reunión..."
  - Estado → **"Reunión pendiente de fechas aspirante"** (`REU-FEC-ASP`)
  - Se inicia el contador de rondas en 1 (RF-ACAD-003)
- **Almacenamiento:** la matriz de equivalencias y el horario de cursada se guardan en la tabla `analisis_academico` (versionada):
  ```
  analisis_academico
    id (PK)
    solicitud_id (FK a solicitudes)
    tipo (ENUM: 'MATRIZ_EQUIVALENCIAS','HORARIO_CURSADA')
    version (INT, default 1)
    reemplaza_a_id (FK a analisis_academico, NULL)   -- versión anterior
    nombre_archivo (VARCHAR, NULL)
    ruta_archivo (VARCHAR, NULL)
    notas (TEXT, NULL)                               -- el Académico puede escribir notas en vez de/de además del archivo
    fecha_carga (TIMESTAMP)
    subido_por_id (FK a usuarios)
  ```
  - La **versión vigente** es la de mayor `version` por (`solicitud_id`, `tipo`); la API y todos los paneles exponen **solo esa**
  - Las versiones anteriores se conservan en BD pero **no se exponen** por la API ni por ningún panel; solo el administrador de la base de datos puede consultarlas directamente
  - Cada actualización crea una versión nueva (`version` + 1, `reemplaza_a_id` apunta a la anterior) y se registra en auditoría

##### RF-ACAD-003: Rondas de Fechas, Fecha por Chat y Enlace de Meet
- **Acciona:** Desde los estados `REU-FEC-ACAD`, `REU-CHAT` y `REU-PEND`
- **2ª ronda de fechas** (desde **"Reunión pendiente de fechas académico"**, `REU-FEC-ACAD`):
  - El Académico ve el motivo y el detalle de cuándo puede reunirse el aspirante (RF-ASP-005)
  - Botón: "Sugerir 2ª ronda de fechas": propone hasta 7 horarios de 1 hora
  - Estado → **"Reunión pendiente de fechas aspirante"** (`REU-FEC-ASP`), con el contador de rondas en 2
  - **Máximo 2 rondas:** no se puede proponer una 3ª
- **Fecha por chat** (desde **"Resolución fecha por chat"**, `REU-CHAT`):
  - Se abre por defecto el chat para el Académico y el aspirante (RF-MSG-001)
  - Una vez pautada la fecha, el Académico toca "Fijar fecha": se abre un modal que pregunta la fecha pautada para la reunión
  - Estado → **"Reunión pendiente de realización"** (`REU-PEND`)
- **Enlace de Meet** (desde **"Reunión pendiente de realización"**, `REU-PEND`):
  - Tanto si el aspirante seleccionó la fecha (RF-ASP-005) como si el Académico la fijó por chat, el Académico incluye el enlace de Meet
  - Al guardarlo, el sistema envía el mail al aspirante (RF-ESTADO-002); el estado no cambia
- NO hay confirmación del Académico sobre la fecha elegida por el aspirante

##### RF-ACAD-004: Reunión Realizada
- **Acciona:** Desde estado **"Reunión pendiente de realización"** (`REU-PEND`), después de realizar el Meet
- Académico entra a solicitud
- Sección "Resultado de reunión":
  - Notas de reunión (textarea, opcional)
  - Botón: "Indicar que la reunión finalizó" (doble verificación)
  - Sistema: Estado → **"Análisis pos reunión"** (`ANAL-POST`)
  - Mail al aspirante
- En `ANAL-POST` el Académico puede **actualizar la matriz de equivalencias y el horario de cursada** si hace falta, antes de decidir la aceptación o el rechazo (RF-ACAD-006)
  - Cada actualización crea una versión nueva en `analisis_academico` (RF-ACAD-002); solo la versión vigente es visible para los usuarios
  - Se envía mail al aspirante (RF-ESTADO-002) y se registra en auditoría
- ⚠ **Pendiente de definir:** si el aspirante no se presentó, la tabla de estados vigente no tiene una transición para volver a coordinar la reunión

##### RF-ACAD-005: Análisis de Documentos Legalizados
- **Acciona:** Desde estado **"Análisis de documentos legalizados"** (`LEG-REV`)
- Académico verifica:
  - Validez de firmas electrónicas
  - Concordancia entre documentos y análisis
- Marca cada documento con ✅ o ❌ (RF-ACAD-009, etapa `LEGALIZACION`)
- Puede cargar archivo de observaciones
- Decisión (radio buttons):
  - ☐ Aceptar documentos (no puede haber documentos marcados con ❌)
  - ☐ Rechazar documentos (comentario obligatorio, al menos un documento con ❌ y ninguno en `PENDIENTE`)
- Botón: "Guardar revisión" (doble verificación)
- Sistema:
  - Si acepta: Estado → **"Solicitud de cupón de pago"** (`CUPON-SOL`)
    - Mail a Secretaría: "Solicitud lista para gestión de pago"
    - Mail al aspirante con la cantidad de materias y el detalle de equivalencias
  - Si rechaza: Estado → **"Documentos legalizados rechazados"** (`LEG-RECH`), con el motivo y la lista de documentos a recargar al aspirante (RF-ASP-009)

##### RF-ACAD-006: Decisión sobre la Inscripción (Aceptar / Rechazar)
- **Acciona:** Desde estado **"Análisis pos reunión"** (`ANAL-POST`)
- Decisión (radio buttons):
  - ☐ Aceptar análisis preliminar/equivalencias
  - ☐ Rechazar (comentario obligatorio con el motivo)
- Botón: "Guardar decisión" (doble verificación)
- Sistema:
  - Si acepta: Estado → **"Inscripción aceptada"** (`INSC-ACEP`); el aspirante debe indicar la legalidad de sus documentos (RF-ASP-009)
  - Si rechaza: Estado → **"Inscripción rechazada"** (`INSC-RECH`); se termina el proceso
  - Mail al aspirante

##### RF-ACAD-007: Envío a Secretaría Administrativa (sin uso en v9)
- ⚠ **SIN USO EN v9:** el paso "Enviar a secretaría" ya no existe en la tabla de estados vigente. La transición de `LEG-REV` a `CUPON-SOL` (RF-ACAD-005) notifica directamente a Secretaría
- Se conserva el ID para no alterar la numeración (ver Apéndice A)

##### RF-ACAD-008: Cierre de Expediente
- ⚠ **PENDIENTE DE DEFINICIÓN:** este requisito dependía de los estados `DISP-GEN` y `FINAL`, que no existen en la tabla de estados vigente. Se completará cuando se definan los estados posteriores a `E3-INI`
- Contenido previsto (sin cambios respecto de la v8):
  - Académico revisa disposición
  - Botón: "Cierre y finalización" (doble verificación)
  - Mail al aspirante: "¡Tu trámite ha sido completado!"
  - Registra en auditoría

##### RF-ACAD-009: Revisión de Documentos (tilde / cruz)
- **Acciona:** Cada vez que el Académico revisa documentación: desde `DOC-REV` (revisión inicial, RF-ACAD-001) y desde `LEG-REV` (documentos legalizados, RF-ACAD-005)
- En la pestaña "Documentación" el Académico marca cada uno de los n documentos del expediente:
  - ✅ **Tilde (`OK`):** documento correcto, el aspirante no debe recargarlo
  - ❌ **Cruz (`RECARGAR`):** el aspirante debe recargarlo; el Académico puede escribir un comentario por documento (qué está mal)
  - ⏳ **Sin marcar (`PENDIENTE`):** documento recién cargado o todavía no revisado
- Puede agregar un **documento requerido faltante** (ítem sin archivo, tipo + descripción, ej: "Programa de Análisis Matemático II"), que queda marcado con ❌
- **Etapas de revisión:** la marca vigente de cada documento pertenece a una etapa (`DOCUMENTACION` o `LEGALIZACION`). Al entrar a `LEG-REV` por primera vez (transición 15), el sistema reinicia a `PENDIENTE` las marcas de las versiones vigentes y pasa la etapa a `LEGALIZACION`; las marcas anteriores quedan en auditoría
- **Reglas:**
  - Marcar es opcional en las acciones que avanzan el proceso (aceptar documentación, aceptar documentos legalizados), pero para aceptar documentos legalizados no puede haber ❌
  - Para pasar a **"Documentación rechazada"** (`DOC-RECH`) o a **"Documentos legalizados rechazados"** (`LEG-RECH`) es **obligatorio**: (a) al menos un documento con ❌ y (b) ningún documento en `PENDIENTE` (todos quedan con ✅ o ❌)
  - Las marcas se guardan al confirmar la decisión (doble verificación) o con el botón "Guardar marcas", que no cambia el estado
  - Solo pueden marcar los PARTICIPANTES de la solicitud (RF-AUTH-005); los observadores solo ven las marcas
  - Cuando la solicitud vuelve a `DOC-REV` tras una recarga (RF-ASP-004) o a `LEG-REV` (RF-ASP-009), el Académico ve resaltados los documentos recargados (`PENDIENTE`) y los que ya estaban con ✅; puede cambiar cualquier marca si detecta un problema
  - Cada cambio de marca se registra en auditoría (documento, marca anterior, marca nueva, usuario, timestamp)
- Tabla `documentos_solicitud`:
  ```
  id (PK)
  solicitud_id (FK a solicitudes)
  tipo (VARCHAR) = "Analítico", "Plan", "Programa", etc.
  nombre_archivo (VARCHAR, NULL si es un documento requerido faltante)
  ruta_archivo (VARCHAR, NULL) — estructura por año y DNI
  tamano_bytes (INT, NULL)
  fecha_carga (TIMESTAMP, NULL)
  version (INT, default 1)
  reemplaza_a_id (FK a documentos_solicitud, NULL) — versión anterior
  estado_revision (ENUM: 'PENDIENTE','OK','RECARGAR', default 'PENDIENTE')
  etapa_revision (ENUM: 'DOCUMENTACION','LEGALIZACION', default 'DOCUMENTACION') — etapa a la que pertenece la marca vigente
  comentario_revision (TEXT, NULL)
  revisado_por_id (FK a usuarios, NULL)
  fecha_revision (TIMESTAMP, NULL)
  es_requerido_faltante (BOOLEAN, default FALSE)
  ```
- El aspirante ve estas marcas en su panel y actúa sobre ellas en RF-ASP-004 (`DOC-RECH`) y RF-ASP-009 (`LEG-RECH`)

#### 4.4.3 Etapa Administrativa (Secretaría)

##### RF-SEC-001: Generar y Enviar Cupón de Pago
- **Acciona:** Desde estado **"Solicitud de cupón de pago"** (`CUPON-SOL`)
- Secretaría abre solicitud
- Sección "Gestión de Pago":
  - Información: Monto (pre-rellenado según política), ID solicitud, datos aspirante
  - Botón: "Generar y enviar cupón"
    - Doble verificación (1er click muestra preview, 2do click ejecuta)
    - Sistema genera PDF con cupón
    - Guarda cupón en BD
    - Estado → **"Pago de cupón por aspirante"** (`CUPON-PAG`)
    - Mail al aspirante: "Tu cupón está listo. Descárgalo aquí: [link]"

##### RF-SEC-002: Verificar Pago
- **Acciona:** Desde estado **"Control de pago"** (`PAG-CTRL`)
- Aspirante cargó comprobante
- Secretaría ve:
  - Comprobante (imagen descargable)
  - Monto reportado
  - Campo para comentarios
- Decisión (radio buttons):
  - ☐ Pago correcto
  - ☐ Pago incorrecto / Monto no coincide
- Botón: "Guardar verificación" (doble verificación)
- Sistema:
  - Si correcto: Estado → **"Inicio de E3"** (`E3-INI`)
    - Mail al aspirante: "Tu pago fue verificado"
  - Si incorrecto: Estado → **"Reenvío de comprobante"** (`PAG-REENV`)
    - Mail al aspirante: "Hay problema con tu comprobante. [Comentario de secretaría]"
- Cuando el aspirante reenvía el comprobante (RF-ASP-007), la solicitud vuelve a `PAG-CTRL` y se repite este requisito

##### RF-SEC-003: Confeccionar Disposición Decanal
- ⚠ **PENDIENTE DE DEFINICIÓN:** este requisito dependía de los estados `PAG-VER` y `DISP-GEN`, que no existen en la tabla de estados vigente. Se completará cuando se definan los estados posteriores a `E3-INI`
- Contenido previsto (sin cambios respecto de la v8):
  - Secretaría abre sección "Disposición Decanal"
  - Opciones: cargar PDF generado externamente, o usar plantilla del sistema (genera documento)
  - Botón: "Generar disposición" (doble verificación); el sistema guarda la disposición en BD
  - Mail a Académico: "Disposición generada. Revisar y confirmar cierre."

##### RF-SEC-004: Archivo Administrativo (posterior al cierre)
- ⚠ **PENDIENTE DE DEFINICIÓN:** este requisito dependía del estado `FINAL`, que no existe en la tabla de estados vigente. Se completará cuando se definan los estados posteriores a `E3-INI`
- Contenido previsto (sin cambios respecto de la v8):
  - Es una acción administrativa posterior al cierre; **NO genera un cambio de estado**
  - Secretaría asigna número de expediente final ("EXP-2024-001-E3") y archiva copia de disposición en servidor
  - Se registra en auditoría

---

### 4.5 Módulo de Comunicación (Chat Grupal)

#### RF-MSG-001: Canal de Chat Grupal
- **Ubicación:** Pestaña "Consultas/Chat" en cada solicitud
- **Participantes:** Aspirante + Académico responsable + Administrativo responsable (si hay)
- **Nota importante:** Si no hay responsable aún, aspirante NO puede abrir chat (botón deshabilitado)
- **Chat por defecto en `REU-CHAT`:** cuando la solicitud está en "Resolución fecha por chat", el chat se abre por defecto para el aspirante y el Académico para que pauten la fecha de la reunión (RF-ACAD-003)

#### RF-MSG-002: Estructura de Mensajes
- Cada mensaje tiene:
  - **Avatar:** Icono personalizable del usuario (hombre/mujer)
  - **Nombre:** Solo nombre del usuario
  - **Contenido:** Texto + archivo opcional (máx 5 MB)
  - **Timestamp:** Fecha y hora exacta
  - **Visto:** Indicador si fue leído
- Máximo 500 caracteres por mensaje
- Tipo de consulta (dropdown): Documentación, Estado, Cálculos, Fechas, Otro

#### RF-MSG-003: Participación en Chat
- **Aspirante:** Puede escribir, ve mensajes de responsables
- **Académico responsable:** Puede escribir, ve mensajes de aspirante y administrativo
- **Administrativo responsable:** Igual que académico
- **Otros usuarios:** NO pueden escribir (botón deshabilitado), pueden VER (auditoría)

#### RF-MSG-004: Historial de Chat
- Todos los mensajes quedan archivados en BD
- Si hay cambio de responsable (RF-ADMIN-005):
  - Chat NO se borra
  - Nuevo responsable ve historial completo
  - Se registra en auditoría: quién fue el responsable anterior
- El chat está vinculado a la solicitud (no al usuario)

---

### 4.6 Módulo de Administración

#### RF-ADMIN-001: Registrar Nueva Cuenta @usal.edu.ar
- Panel "Gestión de usuarios"
- Registrar nueva cuenta:
  - Email @usal.edu.ar
  - Rol: Académico, Administrativo, Revisor Logs
  - Cuenta se crea con **acceso básico** (NO se asignan carreras)
  - Sistema NO envía mail de confirmación (usuario entra directamente con Google, RF-AUTH-003)

#### RF-ADMIN-002: Gestión de Carreras
- Tabla `carreras` (catálogo precargado en BD):
  - `id` (auto-incremental, PK)
  - `codigo` (VARCHAR, UNIQUE, ej: "ING-INF-001")
  - `nombre` (VARCHAR, ej: "Ingeniería en Informática")
  - `facultad` (VARCHAR, ej: "Facultad de Ingeniería")
  - `descripcion` (TEXT)
  - `activo` (BOOLEAN, default TRUE)
- Carreras precargadas inicialmente:
  - Ingeniería en Informática
  - Ingeniería Industrial
  - Ingeniería Civil
  - etc.
- **Admin puede:**
  - Agregar nuevas carreras
  - Modificar datos de carrera
  - Desactivar carrera (soft delete)
- El aspirante elige la carrera destino desde este catálogo (RF-ASP-001)

#### RF-ADMIN-003: Asignación de Permisos Permanentes por Carrera
- Admin registra usuario @usal en tabla `usuarios` (sin permisos, RF-ADMIN-001)
- Luego asigna permisos permanentes:
  - Seleccionar usuario registrado
  - Seleccionar carrera(s); un usuario puede tener acceso a múltiples carreras
  - Botón: "Asignar permiso"
    - Crea fila en `user_career_permissions` para cada carrera
    - Usuario inmediatamente puede "Tomar proceso" en esa carrera
    - Cambio se registra en auditoría
    - Mail al usuario: "Se te ha asignado acceso a [carrera]"

#### RF-ADMIN-004: Revocación de Permisos y Reasignación Forzada
- Si un académico/administrativo **tiene proceso activo** en una carrera:
  - Admin **NO puede revocar** permisos permanentes a esa carrera directamente
  - Si admin intenta, sistema muestra: "Proceso activo. Para revocar, asigne responsable a otro usuario primero."
  - **Excepción:** Admin puede forzar revocación con doble verificación:
    - Primer click: pregunta
    - Segundo click: "Estoy seguro, revocar igual"
    - **Sistema marca `revoked_at = NOW()`** en tabla `user_career_permissions`
    - Usuario pierde acceso **INMEDIATAMENTE** (queries verifican `revoked_at IS NULL`)
    - Sistema busca OTROS usuarios con acceso a esa carrera:
      - Si hay: los notifica por mail (como "proceso disponible"); **no asigna automáticamente** (deben "tomar proceso")
      - Si NO hay: **alerta en Dashboard del Admin** (RF-ADMIN-007) y mail al Admin: "Proceso E3-2024-001 sin responsable académico"; el Admin debe asignar a alguien (puede usar permiso temporal, RF-ADMIN-005)
  - Acción se registra en auditoría con comentario
  - **IMPORTANTE:** `revoked_at` != NULL significa permiso INACTIVO, usuario sin acceso aunque tenga otra fila en BD
- Aplica también a la revocación de permisos temporales (`user_solicitud_permissions`)

#### RF-ADMIN-005: Asignación / Reasignación Manual de Responsable (con Permiso Temporal)
- Admin General puede asignar/reasignar responsable manualmente:
  - Accede a solicitud
  - Botón: "Asignar responsable"
  - Abre modal con listado de todos los usuarios (sin filtrar por carrera)
  - Elige académico Y/O administrativo
  - **Importante:** Puede seleccionar usuarios con o sin acceso permanente a carrera
    - Si selecciona usuario SIN acceso permanente:
      - Checkbox: "Este usuario no tiene acceso a esta carrera. ¿Crear permiso temporal?"
      - Si marca: Sistema crea fila en `user_solicitud_permissions` para esa solicitud
      - Usuario inmediatamente puede ver y participar en esa solicitud
  - Doble verificación:
    - Primer click: muestra confirmación
    - Segundo click: ejecuta
  - Sistema:
    - Asigna nuevos responsables
    - Si había responsable anterior, lo reemplaza
    - Envía mails a:
      - Nuevos responsables: "Has sido asignado a E3-2024-001"
      - Responsable anterior (si aplica): "Has sido reemplazado en E3-2024-001"
      - El aspirante: "Tu proceso ha sido reasignado a [nombre]"
    - Se registra en auditoría con motivo (admin debe escribir comentario)
    - Si se asignó usuario sin acceso permanente, se registra creación de permiso temporal

#### RF-ADMIN-006: Reabrir Expediente Cerrado
- ⚠ **PENDIENTE DE DEFINICIÓN:** este requisito dependía del estado `FINAL`, que no existe en la tabla de estados vigente. Se completará cuando se definan los estados posteriores a `E3-INI`
- Contenido previsto (sin cambios respecto de la v8):
  - Seleccionar expediente en estado `FINAL`
  - Elegir a qué estado volver
  - Doble verificación
  - Mails a responsables
  - Auditoría registra

#### RF-ADMIN-007: Dashboard de Alertas
- Alertas visuales:
  - 🔴 **Procesos sin responsable académico:** "E3-2024-001 (Juan Pérez - Informática) [Ver]"
    - Click lleva a solicitud → Modal "Asignar responsable" (RF-ADMIN-005)
  - 🔴 **Procesos sin responsable administrativo:** "E3-2024-002 ..."
  - 🟡 **Cuentas bloqueadas:** "Juan Silva bloqueado hasta 2024-09-27 14:30 [Desbloquear]"
- Clicks redirigen a solicitud o acción correspondiente
- Botón para desbloquear cuentas sin doble verificación (resetea contador de intentos a 0)

---

### 4.7 Módulo de Auditoría y Revisión

#### RF-AUDIT-001: Tabla de Auditoría General
- Tabla `auditoria_sistema`:
  ```
  id (PK)
  timestamp (TIMESTAMP with TZ)
  usuario_id (FK)
  usuario_email (VARCHAR)
  accion (VARCHAR) = "cambio_estado", "crear_cuenta", etc.
  entidad (VARCHAR) = "solicitud", "usuario", "carrera", etc.
  entidad_id (INT)
  estado_codigo_anterior (VARCHAR) — si aplica
  estado_codigo_nuevo (VARCHAR) — si aplica
  detalles (JSON)
  comentarios (TEXT)
  ip_origen (VARCHAR)
  ```
- Se audita: cambios de estado, tomar/baja/asignar responsable, crear cuenta, cambiar avatar, cambiar contraseña, revocar permisos, marcas de revisión de documentos, actualización de matriz de equivalencias / horario de cursada, descargas de expedientes, logins, etc.

#### RF-AUDIT-002: Historial del Proceso (Visual para Todos)
- **Búsqueda:**
  - Tabla `estados_solicitud` filtrada por `solicitud_id`
  - Ordenada por `fecha_asignacion DESC` (más reciente primero)
- **Query de ejemplo:**
  ```sql
  SELECT
    es.id, es.estado_codigo, e.nombre, es.fecha_asignacion,
    u.nombre as usuario, es.comentarios
  FROM estados_solicitud es
  JOIN estados e ON es.estado_codigo = e.codigo
  LEFT JOIN usuarios u ON es.usuario_ultimo_cambio_id = u.id
  WHERE es.solicitud_id = ?
  ORDER BY es.fecha_asignacion DESC
  ```
- **Visual:** Timeline limpio mostrando:
  - Fecha/hora de cada cambio
  - Estado anterior → Estado nuevo
  - Quién lo hizo
  - Comentario si hay

#### RF-AUDIT-003: Ver Auditoría General (Admin)
- Panel "Auditoría del sistema"
- Tabla `auditoria_sistema` con filtros:
  - Por usuario
  - Por tipo de acción
  - Por fecha
  - Por entidad
- Columnas: Timestamp, Usuario, Acción, Entidad, Detalles, IP
- Referencia a estados siempre por **código**, no nombre

#### RF-REVISOR-001: Acceso de Lectura (Revisor Logs)
- Puede ver:
  - Todas las solicitudes (no puede tomar procesos)
  - Auditoría general (`auditoria_sistema`)
  - Historial de procesos
  - Chat/Mensajes
  - Perfil: avatar, información (sin editar)
- NO puede:
  - Hacer cambios en solicitudes
  - Modificar datos
  - Asignar responsables
  - Tomar procesos
  - Cambiar configuración

---

## 5. ESTRUCTURA DE TABLAS CLAVE (Módulo Autenticación)

Las tablas `solicitudes` (RF-ASP-001), `estados` y `estados_solicitud` (RF-ESTADO-003), `carreras` (RF-ADMIN-002), `documentos_solicitud` (RF-ACAD-009), `analisis_academico` (RF-ACAD-002) y `auditoria_sistema` (RF-AUDIT-001) están definidas en sus requisitos.

### **Tabla 1: `usuarios` (@usal.edu.ar)**
```sql
CREATE TABLE usuarios (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  rol_id INT NOT NULL,
  activo BOOLEAN DEFAULT TRUE,
  intentos_login_fallidos INT DEFAULT 0,
  bloqueado_hasta TIMESTAMP NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_ultimo_login TIMESTAMP NULL,

  FOREIGN KEY (rol_id) REFERENCES roles(id),
  INDEX idx_email (email),
  INDEX idx_rol (rol_id)
);
```

### **Tabla 2: `roles` (Catálogo)**
```sql
CREATE TABLE roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(50) UNIQUE NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  activo BOOLEAN DEFAULT TRUE
);

-- Datos: ACAD, SECRE, ADMIN, REVISOR, ASPIRANTE
```

### **Tabla 3: `user_career_permissions` (Permisos Permanentes - SIN permission_level)**
```sql
CREATE TABLE user_career_permissions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  career_id INT NOT NULL,
  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  revoked_at TIMESTAMP NULL,
    -- NULL = permiso ACTIVO
    -- TIMESTAMP = fecha en que fue revocado (permiso INACTIVO)

  FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  FOREIGN KEY (career_id) REFERENCES carreras(id),
  UNIQUE KEY unique_user_career (user_id, career_id),
  INDEX idx_user (user_id),
  INDEX idx_active (revoked_at)
);

-- LÓGICA: Si existe fila + revoked_at IS NULL → PARTICIPANTE
-- LÓGICA: Si existe fila + revoked_at != NULL → REVOCADO (sin acceso)
-- LÓGICA: Si no existe fila → sin acceso a esa carrera
```

### **Tabla 4: `user_solicitud_permissions` (Permisos Temporales - SIN permission_level)**
```sql
CREATE TABLE user_solicitud_permissions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  solicitud_id INT NOT NULL,
  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  assigned_by INT NOT NULL,
    -- Quién asignó este permiso (admin_id)

  expires_at TIMESTAMP NULL,
    -- NULL = sin expiración (indefinido)
    -- TIMESTAMP = fecha en que expira automáticamente

  revoked_at TIMESTAMP NULL,
    -- NULL = permiso ACTIVO
    -- TIMESTAMP = fecha en que fue revocado manualmente por admin

  FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  FOREIGN KEY (solicitud_id) REFERENCES solicitudes(id) ON DELETE CASCADE,
  FOREIGN KEY (assigned_by) REFERENCES usuarios(id),
  UNIQUE KEY unique_user_solicitud (user_id, solicitud_id),
  INDEX idx_user (user_id),
  INDEX idx_solicitud (solicitud_id),
  INDEX idx_active (expires_at, revoked_at)
);

-- LÓGICA: Si existe fila + (expires_at IS NULL OR expires_at > NOW()) + revoked_at IS NULL → PARTICIPANTE
-- LÓGICA: Si expires_at < NOW() → Expirado automáticamente
-- LÓGICA: Si revoked_at != NULL → Revocado manualmente por admin
-- LÓGICA: Si no existe fila → sin acceso a esa solicitud
```

### **Tabla 5: `carreras` (Catálogo)**
```sql
CREATE TABLE carreras (
  id INT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(50) UNIQUE NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  facultad VARCHAR(255),
  descripcion TEXT,
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Tabla 6: `aspirantes` (Cuentas externas)**
```sql
CREATE TABLE aspirantes (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  telefono VARCHAR(20),
  dni VARCHAR(20) UNIQUE NOT NULL,
  email_validado BOOLEAN DEFAULT FALSE,
  codigo_validacion VARCHAR(10),
  fecha_validacion_enviada TIMESTAMP,
  expiracion_codigo TIMESTAMP,
  intentos_login_fallidos INT DEFAULT 0,
  bloqueado_hasta TIMESTAMP NULL,
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_ultimo_login TIMESTAMP NULL,

  INDEX idx_email (email),
  INDEX idx_dni (dni)
);
```

---

## 6. QUERY DE VALIDACIÓN DE PERMISOS

### **¿Puede usuario PARTICIPAR en solicitud X?**

```sql
-- PASO 1: ¿Tiene permiso TEMPORAL activo?
SELECT 1 FROM user_solicitud_permissions
WHERE user_id = ?
  AND solicitud_id = ?
  AND (expires_at IS NULL OR expires_at > NOW())  -- No expirado
  AND revoked_at IS NULL  -- 🔴 CRÍTICO: revoked_at debe ser NULL
LIMIT 1;

-- Si encontró → PARTICIPANTE ✅
-- Si no encontró → CONTINUAR A PASO 2

-- PASO 2: ¿Tiene permiso CARRERA permanente activo?
SELECT 1 FROM user_career_permissions ucp
JOIN solicitudes s ON s.carrera_destino_id = ucp.career_id
WHERE ucp.user_id = ?
  AND s.id = ?
  AND ucp.revoked_at IS NULL  -- 🔴 CRÍTICO: revoked_at debe ser NULL
LIMIT 1;

-- Si encontró → PARTICIPANTE ✅
-- Si no encontró → OBSERVADOR (solo lectura) ❌

-- NOTAS CRÍTICAS:
-- - revoked_at IS NULL = permiso ACTIVO
-- - revoked_at != NULL = permiso REVOCADO (sin acceso inmediato)
-- - expires_at: si la fecha pasó → permiso expirado automáticamente
```

### **¿Qué carreras tiene un usuario? (Para el ordenamiento RF-PROC-001)**

```sql
-- Obtener TODAS las carreras con permiso permanente activo
SELECT c.id, c.codigo, c.nombre
FROM user_career_permissions ucp
JOIN carreras c ON ucp.career_id = c.id
WHERE ucp.user_id = ?
  AND ucp.revoked_at IS NULL  -- 🔴 Solo activos
ORDER BY c.nombre;

-- Si retorna 0 filas → Usuario SIN carreras asignadas
--   └─ Mostrar todas las solicitudes ordenadas ALFABÉTICAMENTE por carrera
--
-- Si retorna N filas → Usuario TIENE carreras asignadas
--   └─ Mostrar PRIMERO solicitudes de esas carreras (agrupadas)
--   └─ Luego resto de solicitudes (alfabéticamente por carrera)
```

### **¿Fue revocado un permiso?**

```sql
-- Para auditoría: verificar si admin revocó un permiso
SELECT revoked_at, assigned_at FROM user_career_permissions
WHERE user_id = ? AND career_id = ?;

-- Resultado:
-- - revoked_at = NULL → ACTIVO
-- - revoked_at = TIMESTAMP → REVOCADO el [fecha]
-- - assigned_at = TIMESTAMP → Asignado el [fecha]
```

---

## 7. NOTAS IMPORTANTES

1. ✅ Código de validación para aspirantes: expiración 10 minutos, máximo 3 intentos
2. ✅ Login de aspirantes: bloqueo por 6 horas después de 3 intentos fallidos (admin puede desbloquear)
3. ✅ Rol Admin con usuario/contraseña (no OAuth)
4. ✅ Login @usal.edu.ar: OAuth + validación de dominio + existencia en BD
5. ✅ Admin REGISTRA usuario (no crea), usuario entra con Google
6. ✅ Permisos simplificados: SI fila → participante, NO fila → observador
7. ✅ **Permisos SIN permission_level** en tablas
8. ✅ **🔴 CRÍTICO: `revoked_at IS NULL` en TODAS las queries de validación**
   - Cuando admin REVOCA un permiso: se marca `revoked_at = NOW()` (fecha de revocación)
   - Usuario pierde acceso INMEDIATAMENTE
   - Queries deben chequear: `revoked_at IS NULL` para determinar si está activo
   - **Nunca asumir que existe fila = acceso. SIEMPRE validar revoked_at**
9. ✅ Carreras precargadas en BD
10. ✅ Asignación dinámica (no automática): "Tomar proceso"
11. ✅ Máximo 2 rondas de calendario
12. ✅ Chat grupal (aspirante + académico + administrativo)
13. ✅ Doble verificación en todas las acciones críticas
14. ✅ Auditoría dual: General (admin) + Proceso (todos)
15. ✅ Avatar personalizable por género
16. ✅ Historial visual para todos (ordenado DESC por fecha)
17. ✅ Admin puede revocar con fuerza (con confirmación)
18. ✅ Sistema cambia estado (no aspirante)
19. ✅ **Estados con tabla catálogo (código UNIQUE)**, referencias por código
20. ✅ **Auditoría referencia estados por código**, no nombre
21. ✅ Búsqueda de historial: `SELECT ... WHERE solicitud_id = ? ORDER BY fecha_asignacion DESC`
22. ✅ Procesos sin responsable: alerta visual en dashboard admin
23. ✅ Admin puede asignar sin acceso permanente (permiso temporal)
24. ✅ Paneles detallados para Académico y Secretaría
25. ✅ **Ordenamiento en listado de solicitudes:**
    - Si usuario tiene carreras asignadas: mostrar primero esas, luego resto alfabético
    - Si NO tiene: mostrar todas alfabético por carrera
26. ✅ **Permisos sin permission_level:** Fila existe → participante, no existe → observador
27. ✅ **v9: 18 estados** y 23 transiciones tomados de la tabla de estados (E3), con catálogo, transiciones y notificaciones completas en 4.3; la tabla es la fuente de verdad de lo que permite hacer el sistema
28. ✅ **v9: aceptación de la inscripción desde `ANAL-POST`** (RF-ACAD-006); el análisis de documentos legalizados se hace después, desde `LEG-REV` (RF-ACAD-005)
29. ✅ **v7: 7 módulos**, sin requisitos duplicados (ver Apéndice A)
30. ✅ **v8: Revisión por documento (tilde/cruz):** el Académico marca cada documento y el sistema guarda cuáles debe recargar el aspirante (RF-ACAD-009); el aspirante solo recarga los marcados con ❌ (RF-ASP-004)
31. ✅ **v8:** las versiones anteriores de un documento recargado no se borran
32. ✅ **v9:** se elimina el diagrama de transiciones; la tabla de transiciones es suficiente
33. ✅ **v9:** la matriz de equivalencias, el horario de cursada y las fechas disponibles los sube el Académico, solo desde `ANAL-PEND` (RF-ACAD-002)
34. ✅ **v9:** al seleccionar fecha el aspirante pasa directo a `REU-PEND`, sin confirmación del Académico; contador de rondas (máximo 2) y chat por defecto en `REU-CHAT`
35. ✅ **v9:** revisión por documento (tilde/cruz) desde `DOC-REV` y desde `LEG-REV`, con el campo `etapa_revision` en `documentos_solicitud`
36. ✅ **v9:** se elimina el campo `reunion_realizada` de `solicitudes`
37. ✅ **v9:** RF-ASP-009 nuevo; RF-ACAD-007 sin uso; pendientes de estados no definidos: RF-ASP-008, RF-ACAD-008, RF-SEC-003, RF-SEC-004 y RF-ADMIN-006
38. ✅ **v10:** la matriz de equivalencias y el horario de cursada se pueden actualizar desde `ANAL-POST` (RF-ACAD-004), se guardan versionados en `analisis_academico` (solo la versión vigente es visible) y su actualización notifica al aspirante y queda en auditoría

---

## 8. 🔴 VALIDACIÓN CRÍTICA PARA DESARROLLO

### **REGLA ABSOLUTA: revoked_at en validación de permisos**

**CUANDO IMPLEMENTES:**
```
NUNCA hacer:
  SELECT 1 FROM user_career_permissions
  WHERE user_id = ? AND career_id = ?  ← INCORRECTO

SIEMPRE hacer:
  SELECT 1 FROM user_career_permissions
  WHERE user_id = ? AND career_id = ? AND revoked_at IS NULL  ← CORRECTO
```

**Por qué:**
- Si admin REVOCA permiso → `revoked_at = NOW()`
- Si NO checkeas `revoked_at` → usuario sigue viendo "tiene acceso" aunque fue revocado
- Causa SECURITY BUG: usuario mantiene acceso después de que admin lo revocó

**Aplica a:**
- `user_career_permissions`: SIEMPRE `revoked_at IS NULL`
- `user_solicitud_permissions`: SIEMPRE `revoked_at IS NULL` + validar `expires_at`
- Cualquier query que determines si usuario tiene permiso

### **Flujo de Revocación:**
```
Admin clickea "Revocar"
  → Modal de confirmación
  → Segundo click
  → Sistema: UPDATE user_career_permissions SET revoked_at = NOW() WHERE ...
  → Usuario pierde acceso INMEDIATAMENTE (próxima query devuelve 0 filas)
```

---

## APÉNDICE A. EQUIVALENCIAS DE REQUISITOS v6 → v9

Los IDs de la columna izquierda corresponden a la numeración de la v6; los de la columna derecha, a la numeración vigente (v7/v8/v9). Ojo: el RF-ACAD-009 de la v6 (exportación) no es el RF-ACAD-009 de la v8 (revisión de documentos).

| Requisito v6 | Requisito v7 / v8 | Observación |
|---|---|---|
| RF-AUTH-001, 002, 004 | RF-AUTH-001, 002, 004 | Sin cambios |
| RF-AUTH-003 y RF-AUTH-005 | RF-AUTH-003 | Unificados (mismo flujo OAuth) |
| RF-AUTH-006 | RF-AUTH-005 | RBAC |
| RF-AUTH-007 | RF-ADMIN-004 | Movido a Administración |
| RF-AUTH-008 | RF-AUTH-006 | Doble verificación |
| RF-PROF-001 y RF-ADMIN-008 | RF-AUTH-007 | Unificados (perfil) |
| RF-CARR-001 | RF-ADMIN-002 | Movido a Administración |
| RF-CARR-002 y RF-ADMIN-002 | RF-ADMIN-003 | Unificados |
| RF-ESTADO-BD-001, 002 | RF-ESTADO-003 | Unificados |
| RF-ESTADO-BD-003 | RF-ESTADO-004 | |
| (nuevo) | RF-ESTADO-001, RF-ESTADO-002 | Catálogo de estados, transiciones y notificaciones (v9: 18 estados y 23 transiciones, sin diagrama) |
| RF-DATOS-001, RF-SOL-001, RF-ASIGN-001 | RF-ASP-001 | Unificados (crear solicitud) |
| RF-SOL-002 | RF-ASP-002 | |
| RF-SOL-003 y RF-ASIGN-003 | RF-ASP-003 | Unificados (estado + responsables) |
| RF-SOL-004 | RF-ASP-004 | Ampliado en v8: recarga selectiva por documento (tilde/cruz) |
| (nuevo) | RF-ASP-005 | Elegir horario de reunión (faltaba el requisito del lado del aspirante) |
| RF-SOL-005, 006, 007 | RF-ASP-006, 007, 008 | RF-ASP-008 pendiente en v9 |
| RF-ACAD-001, RF-SEC-001 | RF-PROC-001 | Unificados (bandeja) |
| RF-ACAD-002, RF-SEC-002 | RF-PROC-002 | Unificados (vista detallada) |
| RF-ASIGN-002 | RF-PROC-003 | Tomar proceso |
| RF-ASIGN-004 | RF-PROC-004 | Darse de baja |
| RF-ACAD-009, RF-SEC-006 | RF-PROC-005 | Unificados (exportación) |
| RF-ACAD-003 | RF-ACAD-001 | Revisión de documentación (v9: acepta o rechaza desde `DOC-REV`; el análisis preliminar pasa a RF-ACAD-002) |
| RF-ACAD-004 | RF-ACAD-002 | Análisis preliminar y propuesta de fechas (v9: matriz, horario de cursada y fechas en una sola acción) |
| RF-ACAD-005 | RF-ACAD-003 | Rondas de fechas, fecha por chat y enlace de Meet (v9: ya no hay confirmación del Académico) |
| RF-ACAD-006 | RF-ACAD-004 | Reunión realizada (v9: "Indicar que la reunión finalizó" pasa a `ANAL-POST`) |
| RF-ACAD-007 | RF-ACAD-005 y RF-ACAD-006 | **v9:** RF-ACAD-005 = análisis de documentos legalizados (`LEG-REV`); RF-ACAD-006 = aceptar/rechazar la inscripción (`ANAL-POST`) |
| RF-ACAD-008 | RF-ACAD-007 | Envío a secretaría (sin uso en v9) |
| RF-ACAD-010 | RF-ACAD-008 | Cierre de expediente (pendiente en v9) |
| (nuevo v8) | RF-ACAD-009 | Revisión de documentos (tilde/cruz) y tabla `documentos_solicitud` (v9: también desde `LEG-REV`, con `etapa_revision`) |
| (nuevo v9) | RF-ASP-009 | Indicar legalidad de documentos y reenviar documentos legalizados |
| RF-SEC-003, 004, 005 | RF-SEC-001, 002, 003 | Cupón, verificación de pago, disposición (RF-SEC-003 pendiente en v9) |
| RF-SEC-007 | RF-SEC-004 | Ahora explícitamente posterior al cierre y sin cambio de estado (pendiente en v9) |
| RF-MSG-001 a 004 | RF-MSG-001 a 004 | Sin cambios de fondo |
| RF-ASIGN-005 y RF-ADMIN-004 | RF-ADMIN-005 | Unificados |
| RF-ASIGN-006 y RF-ADMIN-003 | RF-ADMIN-004 | Unificados con RF-AUTH-007 |
| RF-ADMIN-005, 006 | RF-ADMIN-006, 007 | Reabrir expediente (pendiente en v9), dashboard de alertas |
| RF-ADMIN-007 | RF-AUDIT-003 | Movido a Auditoría |
| RF-AUDIT-001, 002 | RF-AUDIT-001, 002 | Sin cambios |
| RF-REVISOR-001 | RF-REVISOR-001 | Sin cambios |

---

**Versión:** 10.0
**Fecha:** Octubre 2026
**Autores:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]
**Estado:** LISTO PARA ARQUITECTURA DE BD Y HITOS DE DESARROLLO (los estados posteriores a `E3-INI` están pendientes de definición)
