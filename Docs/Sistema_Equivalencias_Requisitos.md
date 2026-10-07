# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales - Versión 8.0

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
- El proceso avanza mediante transiciones de estado explícitas (16 estados)
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
- **Acciones principales:** Completar formulario inicial, cargar documentación, ver responsable(s) actual, enviar mensajes en chat, ver estado, recargar los documentos indicados por el Académico, elegir horario de reunión, efectuar pago, visualizar informe
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
    reunion_realizada (BOOLEAN, default FALSE)
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
  - **El SISTEMA automáticamente cambia estado a "Documentación cargada"** (código: `DOC-CAR`)
  - **Aspirante NO puede cambiar estado manualmente**
  - Mail al responsable (si lo hay): "Documentación cargada. ID: EXP-2024-001"
  - Si no hay responsable aún, mail a todos con acceso: "Documentación lista para EXP-2024-001"

#### RF-ASP-003: Ver Estado de Solicitud y Responsables
- Panel muestra:
  - Estado actual (con código, ej: "DOC-CAR - Documentación cargada")
  - **Responsables** (destacados):
    - **Responsable Académico:** [Si aplica] Nombre + email clickeable + fecha asignación
    - **Responsable Administrativo:** [Si aplica] Nombre + email clickeable + fecha asignación
    - Si hay ambos, se muestran los dos
    - Si no hay responsable aún: "Esperando que alguien tome el proceso"
  - Timeline de cambios de estado (visual, ordenado DESC por fecha)
  - Documentación cargada (listado con fechas y la marca de revisión de cada documento: ✅ correcto / ❌ debe recargar / ⏳ pendiente de revisión)
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
  - Sistema cambia estado a **"Documentación cargada"** (`DOC-CAR`) automáticamente
  - Cada documento recargado se guarda como una **nueva versión** (`version` + 1, `reemplaza_a_id` apunta a la anterior) con estado de revisión `PENDIENTE`
  - La versión anterior NO se borra: queda archivada y disponible para el historial y el expediente
  - Los documentos ✅ conservan su marca
  - Mail al Académico (RF-ESTADO-002)

#### RF-ASP-005: Elegir Horario de Reunión
- **Acciona:** Desde estado **"Esperando selección de horario"** (`RES-HORA`)
- Aspirante recibe mail y ve en su panel los horarios propuestos por el Académico (hasta 7, duración 1 hora)
- Puede:
  - **Elegir 1 horario** → queda pendiente de confirmación del Académico (RF-ACAD-003)
  - **Indicar "Ninguno me queda"** → habilita nueva ronda de propuesta (máximo 2 rondas)
- El aspirante NO cambia el estado: la elección solo registra su preferencia
- Si tras 2 rondas no hay acuerdo: se abre coordinación manual por chat (RF-ACAD-003)

#### RF-ASP-006: Ver Informe de Materias
- **Acciona:** Desde estado **"Aprobado"** (`APROBADO`) en adelante
- Aspirante ve resumen de equivalencias (solo lectura)
- Tabla: materias reconocidas, carga horaria, correlativas
- Cantidad a adeudar, observaciones, monto a pagar

#### RF-ASP-007: Efectuar Pago
- **Acciona:** Desde estado **"Cupón generado"** (`CUPON`) o **"Pago rechazado"** (`PAG-RECH`)
- Aspirante descarga el cupón de pago generado por Secretaría
- Carga comprobante de pago (imagen JPG/PDF)
- Validación de tamaño (máx 10 MB)
- Al enviar: Sistema cambia estado a **"Esperando verificación de pago"** (`PAG-ESP`)
- Si el pago fue rechazado, ve el comentario de Secretaría y puede reenviar el comprobante (vuelve a `PAG-ESP`)

#### RF-ASP-008: Descargar Expediente
- **Acciona:** Al finalizar (estado **"Expediente finalizado"**, `FINAL`)
- Aspirante puede descargar ZIP con:
  - Solicitud inicial (JSON/PDF)
  - Documentos cargados
  - Matriz de equivalencias
  - Horario propuesto
  - Disposición decanal
  - Comprobante de pago
- Archivo: `EXP-2024-001_Expediente_[DNI].zip`

---

### 4.3 Módulo de Estados y Notificaciones

#### RF-ESTADO-001: Estados de la Solicitud y Transiciones

**Reglas generales:**
- El estado de una solicitud solo cambia por una transición definida en este requisito
- El cambio lo ejecuta el SISTEMA como consecuencia de una acción de un actor (el Aspirante nunca cambia el estado manualmente)
- Las decisiones críticas exigen doble verificación (RF-AUTH-006)
- Todo cambio de estado se registra en `estados_solicitud` y en auditoría (RF-ESTADO-003, RF-ESTADO-004)

**Catálogo de estados (16):**

| # | Código | Estado | Descripción | Tipo |
|---|---|---|---|---|
| 1 | `SOL-INI` | Solicitud iniciada | Aspirante acaba de crear la solicitud, no ha cargado documentación | Inicial |
| 2 | `DOC-CAR` | Documentación cargada | Toda la documentación inicial está en el sistema | Intermedio |
| 3 | `DOC-RECH` | Documentación rechazada | Hay observaciones (documentación falta, error o cambios requeridos en la matriz); el sistema guarda cuáles documentos debe recargar el aspirante (RF-ACAD-009) | Intermedio |
| 4 | `ANAL-COMPL` | Análisis preliminar completado | El Académico armó la matriz tentativa y el horario | Intermedio |
| 5 | `RES-HORA` | Esperando selección de horario | El Académico propuso horarios de reunión; el aspirante debe elegir y el Académico confirmar | Intermedio |
| 6 | `RES-CONF` | Reunión confirmada | Fecha y hora de reunión (Meet) confirmadas; falta registrar el resultado | Intermedio |
| 7 | `REV-LEG` | Revisión legal completada | Documentación legal verificada sin observaciones | Intermedio |
| 8 | `APROBADO` | Aprobado | Solicitud aprobada, esperando envío a secretaría | Intermedio |
| 9 | `ENV-SECREC` | Enviado a secretaría | Secretaría administrará el pago | Intermedio |
| 10 | `CUPON` | Cupón generado | Cupón de pago disponible, esperando pago del aspirante | Intermedio |
| 11 | `PAG-ESP` | Esperando verificación de pago | Comprobante cargado, Secretaría verifica | Intermedio |
| 12 | `PAG-VER` | Pago verificado | Pago confirmado, listo para disposición | Intermedio |
| 13 | `PAG-RECH` | Pago rechazado | El pago tiene problemas (monto incorrecto, etc.) | Intermedio |
| 14 | `DISP-GEN` | Disposición generada | Disposición decanal creada, enviada a Académico para revisión | Intermedio |
| 15 | `FINAL` | Expediente finalizado | Trámite completamente cerrado | **Terminal** |
| 16 | `RECHAZO` | Rechazado | Solicitud rechazada, no hay equivalencias | **Terminal** |

**Tabla de transiciones:**

| # | De | A | Cuándo / quién | Requisito |
|---|---|---|---|---|
| 1 | (inicio) | `SOL-INI` | Aspirante crea la solicitud | RF-ASP-001 |
| 2 | `SOL-INI` | `DOC-CAR` | Aspirante envía la documentación (cambio automático del sistema) | RF-ASP-002 |
| 3 | `DOC-CAR` | `ANAL-COMPL` | Académico completa el análisis preliminar | RF-ACAD-001 |
| 4 | `DOC-CAR` | `DOC-RECH` | Académico requiere correcciones (marca los documentos a recargar) | RF-ACAD-001 |
| 5 | `DOC-RECH` | `DOC-CAR` | Aspirante recarga los documentos marcados con ❌ | RF-ASP-004 |
| 6 | `ANAL-COMPL` | `RES-HORA` | Académico guarda la propuesta de horarios | RF-ACAD-002 |
| 7 | `RES-HORA` | `RES-CONF` | Académico confirma el horario elegido (o acordado por chat) | RF-ACAD-003 |
| 8 | `RES-CONF` | `ANAL-COMPL` | Post-reunión: "Permite inscripción" (se marca reunión realizada y se habilita la revisión legal) | RF-ACAD-004 |
| 9 | `RES-CONF` | `DOC-RECH` | Post-reunión: "Requiere cambios en matriz" (marca los documentos a recargar) | RF-ACAD-004 |
| 10 | `RES-CONF` | `RECHAZO` | Post-reunión: "Rechaza solicitud" | RF-ACAD-004 |
| 11 | `ANAL-COMPL` | `REV-LEG` | Académico completa la revisión legal sin observaciones (requiere reunión realizada) | RF-ACAD-005 |
| 12 | `ANAL-COMPL` | `DOC-RECH` | Académico registra observaciones legales (marca los documentos a recargar) | RF-ACAD-005 |
| 13 | `REV-LEG` | `APROBADO` | Académico aprueba | RF-ACAD-006 |
| 14 | `REV-LEG` | `RECHAZO` | Académico rechaza | RF-ACAD-006 |
| 15 | `APROBADO` | `ENV-SECREC` | Académico gestiona el envío a secretaría | RF-ACAD-007 |
| 16 | `ENV-SECREC` | `CUPON` | Secretaría genera el cupón | RF-SEC-001 |
| 17 | `CUPON` | `PAG-ESP` | Aspirante carga el comprobante | RF-ASP-007 |
| 18 | `PAG-ESP` | `PAG-VER` | Secretaría verifica: pago correcto | RF-SEC-002 |
| 19 | `PAG-ESP` | `PAG-RECH` | Secretaría verifica: pago con problemas | RF-SEC-002 |
| 20 | `PAG-RECH` | `PAG-ESP` | Aspirante reenvía el comprobante | RF-ASP-007 |
| 21 | `PAG-VER` | `DISP-GEN` | Secretaría crea la disposición decanal | RF-SEC-003 |
| 22 | `DISP-GEN` | `FINAL` | Académico verifica y cierra | RF-ACAD-008 |
| 23 | `FINAL` | (estado elegido) | Admin reabre el expediente (excepcional) | RF-ADMIN-006 |

**Diagrama de transiciones:**

```
Solicitud iniciada (SOL-INI)
    ↓ (Aspirante envía documentación)
Documentación cargada (DOC-CAR) ←──────────────┐
    │                                          │ (Aspirante recarga)
    ├→ Documentación rechazada (DOC-RECH) ─────┘
    ↓ (Académico completa análisis)
Análisis preliminar completado (ANAL-COMPL) ←─────────────┐
    │                                                     │
    │  [A] Reunión                                        │ ("Permite inscripción":
    ├→ Esperando selección de horario (RES-HORA)          │  reunión realizada)
    │        ↓                                            │
    │   Reunión confirmada (RES-CONF) ────────────────────┘
    │        ├→ Documentación rechazada (DOC-RECH)   ("Requiere cambios")
    │        └→ Rechazado (RECHAZO) ✓                ("Rechaza solicitud")
    │
    │  [B] Revisión legal (requiere reunión realizada)
    ├→ Documentación rechazada (DOC-RECH)            (observaciones legales)
    ↓
Revisión legal completada (REV-LEG)
    ↓
├→ Aprobado (APROBADO)
│    ↓
│  Enviado a secretaría (ENV-SECREC)
│    ↓
│  Cupón generado (CUPON)
│    ↓
│  Esperando verificación de pago (PAG-ESP) ←──┐
│    ├→ Pago verificado (PAG-VER)              │
│    │    ↓                                    │
│    │  Disposición generada (DISP-GEN)        │
│    │    ↓                                    │
│    │  Expediente finalizado (FINAL) ✓        │
│    │                                         │
│    └→ Pago rechazado (PAG-RECH) ─────────────┘ (Aspirante reenvía)
│
└→ Rechazado (RECHAZO) ✓
```

**Nota sobre el desvío de reunión desde `ANAL-COMPL`:** desde `ANAL-COMPL` el Académico dispone de dos acciones: (A) coordinar la reunión (RF-ACAD-002) o (B) realizar la revisión legal (RF-ACAD-005). La revisión legal solo se habilita una vez registrada la reunión como realizada con resultado "Permite inscripción" (flag `reunion_realizada = true`). Ese flag impide volver a proponer horarios una vez cumplida la reunión.

#### RF-ESTADO-002: Notificaciones por Cambio de Estado

Cuando el estado cambia, el sistema envía mail automático al actor siguiente relevante:

| De Estado | A Estado | Mail a | Asunto/Contenido |
|---|---|---|---|
| Solicitud iniciada | Documentación cargada | Académico | "Nueva solicitud de equivalencias lista para analizar. ID: [...]" |
| Documentación cargada | Análisis preliminar completado | Aspirante | "Tu solicitud está siendo analizada por nuestro equipo académico." |
| Documentación cargada | Documentación rechazada | Aspirante | "Observaciones en tu documentación. Motivo: [comentario del Académico]. Documentos a recargar: [lista de documentos marcados con ❌]" |
| Análisis preliminar completado | Esperando selección de horario | Aspirante | "Elegí un horario para la reunión. Horarios propuestos: [...]" |
| Esperando selección de horario | Reunión confirmada | Aspirante | "Tu reunión fue confirmada para [fecha/hora]. Link: [Meet]" |
| Reunión confirmada | Análisis preliminar completado | Aspirante | "La reunión se realizó. Tu solicitud continúa con la revisión legal." |
| Reunión confirmada | Documentación rechazada | Aspirante | "Tras la reunión se requieren cambios. Motivo: [comentario del Académico]. Documentos a recargar: [lista de documentos marcados con ❌]" |
| Reunión confirmada | Rechazado | Aspirante | "Lamentablemente tu solicitud no pudo ser aprobada. Motivo: [...]" |
| Análisis preliminar completado | Revisión legal completada | (interno) | - |
| Análisis preliminar completado | Documentación rechazada | Aspirante | "Observaciones en tu documentación. Motivo: [comentario del Académico]. Documentos a recargar: [lista de documentos marcados con ❌]" |
| Revisión legal completada | Aprobado | Aspirante | "Tu solicitud ha sido aprobada. Aquí está el detalle de equivalencias: [matriz]" |
| Revisión legal completada | Rechazado | Aspirante | "Lamentablemente tu solicitud no pudo ser aprobada. Motivo: [...]" |
| Documentación rechazada | Documentación cargada | Académico | "Aspirante ha recargado documentación. ID: [...]" |
| Aprobado | Enviado a secretaría | Secretaría | "Solicitud aprobada lista para gestión de pago. ID: [...]" |
| Enviado a secretaría | Cupón generado | Aspirante | "Tu cupón de pago está listo. Descargalo aquí: [link]. Monto: $[...]" |
| Cupón generado | Esperando verificación de pago | (interno) | - |
| Esperando verificación de pago | Pago verificado | Aspirante | "Tu pago ha sido verificado correctamente. Gracias." |
| Esperando verificación de pago | Pago rechazado | Aspirante | "Hay un problema con tu comprobante. Detalle: [comentario de Secretaría]. Por favor reenvía." |
| Pago rechazado | Esperando verificación de pago | Secretaría | "El aspirante reenvió el comprobante. ID: [...]" |
| Pago verificado | Disposición generada | Académico | "Disposición decanal generada. Revisar y confirmar cierre. ID: [...]" |
| Disposición generada | Expediente finalizado | Aspirante | "¡Tu trámite de equivalencias ha sido completado! Accede a tu cuenta para descargar la disposición." |

- Los mails son SOLO notificaciones; no son el medio de comunicación principal (para eso está el chat, módulo 4.5)
- Las filas de reunión, "Documentación cargada → Documentación rechazada" y "Pago rechazado → Esperando verificación de pago" se incorporan en la v7 para cubrir todas las transiciones de RF-ESTADO-001

#### RF-ESTADO-003: Estructura de BD de Estados
- Tabla `estados` (catálogo):
  - `id` (auto-incremental, PK)
  - `codigo` (VARCHAR, UNIQUE) = "SOL-INI", "DOC-CAR", "ANAL-COMPL", etc.
  - `nombre` (VARCHAR) = "Solicitud iniciada", "Documentación cargada", etc.
  - `descripcion` (TEXT)
  - `es_terminal` (BOOLEAN) — si es estado final (`FINAL`, `RECHAZO`)
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
  - Por estado (dropdown con los 16 estados)
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
     - Matriz tentativa (descargable si está cargada)
     - Horario propuesto (descargable)
     - Botón: "Realizar análisis preliminar" (si estado permite)
  4. **Reunión:**
     - Calendario si está en etapa de propuesta
     - Horarios propuestos (editable si es académico asignado)
     - Horario elegido por aspirante
     - Botón: "Confirmar/Rechazar horario"
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

##### RF-ACAD-001: Realizar Análisis Preliminar
- **Acciona:** Desde estado **"Documentación cargada"** (`DOC-CAR`)
- Académico accede a solicitud
- Ve documentación del aspirante (descargable)
- Genera matriz tentativa (puede cargar archivo o escribir notas)
- Genera horario propuesto (puede cargar archivo o escribir notas)
- Marca cada documento con ✅ o ❌ (RF-ACAD-009)
- Decisión:
  - **"Completar análisis"**
    - Doble verificación (1er click muestra modal, 2do click ejecuta)
    - Sistema cambia estado a **"Análisis preliminar completado"** (`ANAL-COMPL`)
    - Registra en auditoría
    - Mail al aspirante: "Tu solicitud está siendo analizada..."
  - **"Requerir correcciones"** (comentario obligatorio y al menos un documento marcado con ❌, RF-ACAD-009)
    - Doble verificación
    - Sistema cambia estado a **"Documentación rechazada"** (`DOC-RECH`)
    - Mail al aspirante con el motivo y la lista de documentos a recargar

##### RF-ACAD-002: Proponer Horarios para Reunión
- **Acciona:** Desde estado **"Análisis preliminar completado"** (`ANAL-COMPL`), mientras la reunión no haya sido realizada
- Académico abre **calendario integrado**
- Propone hasta **7 horarios disponibles** (próximos 14-30 días)
- Duración: 1 hora (10:00-18:00, franjas académicas)
- Botón: "Guardar propuesta"
  - Sistema manda mail al aspirante: "Elige un horario para reunión"
  - Estado → **"Esperando selección de horario"** (`RES-HORA`)

##### RF-ACAD-003: Confirmar Horario de Reunión
- **Acciona:** Desde estado `RES-HORA`, cuando el aspirante elige horario (RF-ASP-005)
- Académico ve modal: "Aspirante eligió [fecha/hora]. ¿Confirmas?"
- Opciones:
  - **Confirmar:** Doble verificación
    - Estado → **"Reunión confirmada"** (`RES-CONF`)
    - Mail a aspirante
  - **Rechazar/Proponer nuevos:** nueva ronda de horarios (**máximo 2 rondas**); el estado permanece en `RES-HORA`
- Si el aspirante responde "Ninguno me queda" en una ronda, también se habilita la ronda siguiente
- **Si tras 2 rondas no hay acuerdo:** se abre canal de coordinación manual por chat (aspirante envía mensaje directo); una vez acordado, el Académico registra manualmente el horario y confirma → `RES-CONF`

##### RF-ACAD-004: Post-Reunión (Meet) - Notas y Decisión
- **Acciona:** Desde estado **"Reunión confirmada"** (`RES-CONF`), después de realizar el Meet
- Académico entra a solicitud
- Sección "Resultado de reunión":
  - Checkbox: "Reunión realizada" / "Aspirante no se presentó"
  - Notas de reunión (textarea)
  - Link de Meet (opcional)
  - Decisión (radio buttons):
    - ☐ Permite inscripción
    - ☐ Requiere cambios en matriz
    - ☐ Rechaza solicitud
  - Botón: "Guardar resultado" (doble verificación)
  - Sistema:
    - Si "Permite": marca `reunion_realizada = true` y Estado → **"Análisis preliminar completado"** (`ANAL-COMPL`), habilitando la revisión legal (RF-ACAD-005)
    - Si "Cambios": Estado → **"Documentación rechazada"** (`DOC-RECH`); requiere marcar al menos un documento con ❌ (RF-ACAD-009)
    - Si "Rechaza": Estado → **"Rechazado"** (`RECHAZO`)
    - Mail al aspirante con resultado
- Si el aspirante no se presentó, el Académico puede volver a coordinar la reunión (manual)

##### RF-ACAD-005: Revisión Legal de Documentación
- **Acciona:** Desde estado **"Análisis preliminar completado"** (`ANAL-COMPL`) con `reunion_realizada = true`
- Académico verifica:
  - Validez de firmas electrónicas
  - Concordancia entre documentos y análisis
- Puede cargar archivo de observaciones
- Decisión (radio buttons):
  - ☐ Sin observaciones
  - ☐ Con observaciones (requiere cambios)
- Botón: "Guardar revisión" (doble verificación)
- Sistema:
  - Si sin observaciones: Estado → **"Revisión legal completada"** (`REV-LEG`)
  - Si con observaciones: Estado → **"Documentación rechazada"** (`DOC-RECH`), con el motivo al aspirante; requiere marcar al menos un documento con ❌ (RF-ACAD-009)
- La aprobación o el rechazo de la solicitud NO se decide acá: se decide en RF-ACAD-006

##### RF-ACAD-006: Decisión Final (Aprobar / Rechazar)
- **Acciona:** Desde estado **"Revisión legal completada"** (`REV-LEG`)
- Decisión (radio buttons):
  - ☐ Aprobar
  - ☐ Rechazar (comentario obligatorio con el motivo)
- Botón: "Guardar decisión" (doble verificación)
- Sistema:
  - Si aprobar: Estado → **"Aprobado"** (`APROBADO`); el aspirante accede al informe de materias (RF-ASP-006)
  - Si rechazar: Estado → **"Rechazado"** (`RECHAZO`)
  - Mail al aspirante

##### RF-ACAD-007: Envío a Secretaría Administrativa
- **Acciona:** Desde estado **"Aprobado"** (`APROBADO`)
- Botón: "Enviar a secretaría"
  - Doble verificación
  - Estado → **"Enviado a secretaría"** (`ENV-SECREC`)
  - Mail a secretaría: "Solicitud aprobada lista para gestión de pago"
  - Registra en auditoría

##### RF-ACAD-008: Cierre de Expediente
- **Acciona:** Desde estado **"Disposición generada"** (`DISP-GEN`), cuando secretaría ha generado la disposición
- Académico revisa disposición
- Botón: "Cierre y finalización"
  - Doble verificación
  - Estado → **"Expediente finalizado"** (`FINAL`)
  - Mail al aspirante: "¡Tu trámite ha sido completado!"
  - Registra en auditoría

##### RF-ACAD-009: Revisión de Documentos (tilde / cruz)
- **Acciona:** Cada vez que el Académico revisa documentación: desde `DOC-CAR` (análisis preliminar, RF-ACAD-001), desde `RES-CONF` (post-reunión, RF-ACAD-004) y desde `ANAL-COMPL` (revisión legal, RF-ACAD-005)
- En la pestaña "Documentación" el Académico marca cada uno de los n documentos del expediente:
  - ✅ **Tilde (`OK`):** documento correcto, el aspirante no debe recargarlo
  - ❌ **Cruz (`RECARGAR`):** el aspirante debe recargarlo; el Académico puede escribir un comentario por documento (qué está mal)
  - ⏳ **Sin marcar (`PENDIENTE`):** documento recién cargado o todavía no revisado
- Puede agregar un **documento requerido faltante** (ítem sin archivo, tipo + descripción, ej: "Programa de Análisis Matemático II"), que queda marcado con ❌
- **Reglas:**
  - Marcar es opcional en las acciones que avanzan el proceso (completar análisis, revisión sin observaciones)
  - Para pasar a **"Documentación rechazada"** (`DOC-RECH`) es **obligatorio**: (a) al menos un documento con ❌ y (b) ningún documento en `PENDIENTE` (todos quedan con ✅ o ❌)
  - Las marcas se guardan al confirmar la decisión (doble verificación) o con el botón "Guardar marcas", que no cambia el estado
  - Solo pueden marcar los PARTICIPANTES de la solicitud (RF-AUTH-005); los observadores solo ven las marcas
  - Cuando la solicitud vuelve a `DOC-CAR` tras una recarga (RF-ASP-004), el Académico ve resaltados los documentos recargados (`PENDIENTE`) y los que ya estaban con ✅; puede cambiar cualquier marca si detecta un problema
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
  comentario_revision (TEXT, NULL)
  revisado_por_id (FK a usuarios, NULL)
  fecha_revision (TIMESTAMP, NULL)
  es_requerido_faltante (BOOLEAN, default FALSE)
  ```
- El aspirante ve estas marcas en su panel y actúa sobre ellas en RF-ASP-004

#### 4.4.3 Etapa Administrativa (Secretaría)

##### RF-SEC-001: Generar Cupón de Pago
- **Acciona:** Desde estado **"Enviado a secretaría"** (`ENV-SECREC`)
- Secretaría abre solicitud
- Sección "Gestión de Pago":
  - Información: Monto (pre-rellenado según política), ID solicitud, datos aspirante
  - Botón: "Generar cupón"
    - Doble verificación (1er click muestra preview, 2do click ejecuta)
    - Sistema genera PDF con cupón
    - Guarda cupón en BD
    - Estado → **"Cupón generado"** (`CUPON`)
    - Mail al aspirante: "Tu cupón está listo. Descárgalo aquí: [link]"

##### RF-SEC-002: Verificar Pago
- **Acciona:** Desde estado **"Esperando verificación de pago"** (`PAG-ESP`)
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
  - Si correcto: Estado → **"Pago verificado"** (`PAG-VER`)
    - Mail al aspirante: "Tu pago fue verificado"
  - Si incorrecto: Estado → **"Pago rechazado"** (`PAG-RECH`)
    - Mail al aspirante: "Hay problema con tu comprobante. [Comentario de secretaría]"
- Cuando el aspirante reenvía el comprobante (RF-ASP-007), la solicitud vuelve a `PAG-ESP` y se repite este requisito

##### RF-SEC-003: Confeccionar Disposición Decanal
- **Acciona:** Desde estado **"Pago verificado"** (`PAG-VER`)
- Secretaría abre sección "Disposición Decanal"
- Opciones:
  - Cargar PDF generado externamente
  - Usar plantilla del sistema (genera documento)
- Botón: "Generar disposición"
  - Doble verificación
  - Sistema guarda disposición en BD
  - Estado → **"Disposición generada"** (`DISP-GEN`)
  - Mail a académico: "Disposición generada. Revisar y confirmar cierre."

##### RF-SEC-004: Archivo Administrativo (posterior al cierre)
- **Acciona:** Cuando el Académico ya cerró el expediente (estado `FINAL`)
- Es una acción administrativa posterior al cierre; **NO genera un cambio de estado** (`FINAL` es terminal)
- Secretaría realiza:
  - Asigna número de expediente final: "EXP-2024-001-E3"
  - Archiva copia de disposición en servidor
- Se registra en auditoría

---

### 4.5 Módulo de Comunicación (Chat Grupal)

#### RF-MSG-001: Canal de Chat Grupal
- **Ubicación:** Pestaña "Consultas/Chat" en cada solicitud
- **Participantes:** Aspirante + Académico responsable + Administrativo responsable (si hay)
- **Nota importante:** Si no hay responsable aún, aspirante NO puede abrir chat (botón deshabilitado)

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
- Se audita: cambios de estado, tomar/baja/asignar responsable, crear cuenta, cambiar avatar, cambiar contraseña, revocar permisos, marcas de revisión de documentos, descargas de expedientes, logins, etc.

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

Las tablas `solicitudes` (RF-ASP-001), `estados` y `estados_solicitud` (RF-ESTADO-003), `carreras` (RF-ADMIN-002), `documentos_solicitud` (RF-ACAD-009) y `auditoria_sistema` (RF-AUDIT-001) están definidas en sus requisitos.

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
27. ✅ **v7: 16 estados** (los 14 base + `RES-HORA` y `RES-CONF`), con tabla de transiciones y notificaciones completas en 4.3
28. ✅ **v7: Revisión legal desde `ANAL-COMPL`**; la decisión de aprobar/rechazar se toma desde `REV-LEG` (RF-ACAD-005 y RF-ACAD-006)
29. ✅ **v7: 7 módulos**, sin requisitos duplicados (ver Apéndice A)
30. ✅ **v8: Revisión por documento (tilde/cruz):** el Académico marca cada documento y el sistema guarda cuáles debe recargar el aspirante (RF-ACAD-009); el aspirante solo recarga los marcados con ❌ (RF-ASP-004)
31. ✅ **v8:** el estado "Documentación rechazada" (`DOC-RECH`) no cambia de nombre ni de transiciones; las versiones anteriores de un documento recargado no se borran

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

## APÉNDICE A. EQUIVALENCIAS DE REQUISITOS v6 → v8

Los IDs de la columna izquierda corresponden a la numeración de la v6; los de la columna derecha, a la numeración vigente (v7/v8). Ojo: el RF-ACAD-009 de la v6 (exportación) no es el RF-ACAD-009 de la v8 (revisión de documentos).

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
| (nuevo) | RF-ESTADO-001, RF-ESTADO-002 | Catálogo de 16 estados, transiciones, diagrama y notificaciones |
| RF-DATOS-001, RF-SOL-001, RF-ASIGN-001 | RF-ASP-001 | Unificados (crear solicitud) |
| RF-SOL-002 | RF-ASP-002 | |
| RF-SOL-003 y RF-ASIGN-003 | RF-ASP-003 | Unificados (estado + responsables) |
| RF-SOL-004 | RF-ASP-004 | Ampliado en v8: recarga selectiva por documento (tilde/cruz) |
| (nuevo) | RF-ASP-005 | Elegir horario de reunión (faltaba el requisito del lado del aspirante) |
| RF-SOL-005, 006, 007 | RF-ASP-006, 007, 008 | |
| RF-ACAD-001, RF-SEC-001 | RF-PROC-001 | Unificados (bandeja) |
| RF-ACAD-002, RF-SEC-002 | RF-PROC-002 | Unificados (vista detallada) |
| RF-ASIGN-002 | RF-PROC-003 | Tomar proceso |
| RF-ASIGN-004 | RF-PROC-004 | Darse de baja |
| RF-ACAD-009, RF-SEC-006 | RF-PROC-005 | Unificados (exportación) |
| RF-ACAD-003 | RF-ACAD-001 | Análisis preliminar (ahora incluye "requerir correcciones") |
| RF-ACAD-004 | RF-ACAD-002 | Proponer horarios |
| RF-ACAD-005 | RF-ACAD-003 | Confirmar horario |
| RF-ACAD-006 | RF-ACAD-004 | Post-reunión ("Permite" ahora vuelve a `ANAL-COMPL`) |
| RF-ACAD-007 | RF-ACAD-005 y RF-ACAD-006 | **Corregido:** la revisión legal sale de `ANAL-COMPL`; aprobar/rechazar se separa y sale de `REV-LEG` |
| RF-ACAD-008 | RF-ACAD-007 | Envío a secretaría |
| RF-ACAD-010 | RF-ACAD-008 | Cierre de expediente |
| (nuevo v8) | RF-ACAD-009 | Revisión de documentos (tilde/cruz) y tabla `documentos_solicitud` |
| RF-SEC-003, 004, 005 | RF-SEC-001, 002, 003 | Cupón, verificación de pago, disposición |
| RF-SEC-007 | RF-SEC-004 | Ahora explícitamente posterior al cierre y sin cambio de estado |
| RF-MSG-001 a 004 | RF-MSG-001 a 004 | Sin cambios de fondo |
| RF-ASIGN-005 y RF-ADMIN-004 | RF-ADMIN-005 | Unificados |
| RF-ASIGN-006 y RF-ADMIN-003 | RF-ADMIN-004 | Unificados con RF-AUTH-007 |
| RF-ADMIN-005, 006 | RF-ADMIN-006, 007 | Reabrir expediente, dashboard de alertas |
| RF-ADMIN-007 | RF-AUDIT-003 | Movido a Auditoría |
| RF-AUDIT-001, 002 | RF-AUDIT-001, 002 | Sin cambios |
| RF-REVISOR-001 | RF-REVISOR-001 | Sin cambios |

---

**Versión:** 8.0
**Fecha:** Septiembre 2026
**Autores:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]
**Estado:** LISTO PARA ARQUITECTURA DE BD Y HITOS DE DESARROLLO
