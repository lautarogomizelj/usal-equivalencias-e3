# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales - Versión 6.0 FINAL

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
- El proceso avanza mediante transiciones de estado explícitas
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
- **Acciones principales:** Completar formulario inicial, cargar documentación, ver responsable(s) actual, enviar mensajes en chat, ver estado, recargar documentos, efectuar pago, visualizar informe
- **Visibilidad:** Solo su solicitud, documentos, matriz, horario; puede ver auditoría del proceso (visual)

### 3.2 Académico
- **Perfil:** Personal de área académica (profesor coordinador de carrera)
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Permisos:**
  - **Acceso Básico (default):** Ver TODAS las solicitudes de TODAS las carreras (lectura, botones deshabilitados)
  - **Acceso de Proceso Permanente:** Participar en equivalencias de carrera(s) específica(s) asignadas por Admin
  - **Acceso de Proceso Temporal:** Participar SOLO en solicitud específica (sin tener acceso permanente a carrera)
- **Ejemplo:** Christian LOPEZ PASARON (ch.lopezpasaran) — Ingeniería en Informática
- **Acciones:** Ver solicitudes, "Tomar proceso" (si hay acceso), realizar análisis, revisar, tomar decisiones, participar en chat grupal, descargar expedientes
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

### 4.1 Módulo de Autenticación y Control de Acceso

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

#### RF-AUTH-003: Login de Académicos y Secretaría Administrativa (OAuth @usal.edu.ar)
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

#### RF-AUTH-004: Login de Admin General
- **Usuario + Contraseña (hasheada, a la vieja escuela)**
- NO OAuth
- Sesión con expiración (1 día)
- Cada login registrado en auditoría (timestamp, IP)
- **Cambio de credenciales desde panel:**
  - Puede cambiar email (con validación de dominio @usal.edu.ar)
  - Puede cambiar contraseña (con doble verificación)
  - Cambios quedan en auditoría

#### RF-AUTH-005: Login de Revisor Logs
- OAuth 2.0 con Google, dominio @usal.edu.ar
- Mismo flujo que Académicos (RF-AUTH-003)
- Rol asignado por Admin
- Sesión normal

#### RF-AUTH-006: Gestión de Permisos basada en Roles (RBAC) - SIMPLIFICADO
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

  **NOTA CRÍTICA:** `revoked_at IS NULL` significa permiso ACTIVO. Si `revoked_at` tiene una fecha → permiso REVOCADO, usuario pierde acceso inmediatamente.

- **Admin puede:**
  - Registrar nueva cuenta @usal (crea fila en `usuarios`, sin permisos)
  - Asignar permisos permanentes por carrera (crea fila en `user_career_permissions`)
  - Asignar permisos temporales para solicitud específica (crea fila en `user_solicitud_permissions`)
  - Revocar ambos tipos de permisos

#### RF-AUTH-007: Restricción de Revocación de Permisos Permanentes
- Si un académico/administrativo **tiene proceso activo** en una carrera:
  - Admin **NO puede revocar** permisos permanentes a esa carrera directamente
  - Si admin intenta, sistema muestra: "Proceso activo. Para revocar, asigne responsable a otro usuario primero."
  - **Excepción:** Admin puede forzar revocación con doble verificación:
    - Primer click: pregunta
    - Segundo click: "Estoy seguro, revocar igual"
    - **Sistema marca `revoked_at = NOW()`** en tabla `user_career_permissions`
    - Usuario pierde acceso **INMEDIATAMENTE** (queries verifican `revoked_at IS NULL`)
    - Automáticamente se reasigna el proceso a otros usuarios con acceso a esa carrera
    - Si nadie tiene acceso → **alerta en dashboard del admin**
    - Se manda mail a todos con acceso a esa carrera
  - Acción se registra en auditoría con comentario
  - **IMPORTANTE:** `revoked_at` != NULL significa permiso INACTIVO, usuario sin acceso aunque tenga otra fila en BD

#### RF-AUTH-008: Doble Verificación en Todas las Acciones Críticas
- Acciones que requieren 2 clicks + confirmación:
  - Aprobar solicitud
  - Rechazar solicitud
  - Cambiar responsable (admin asigna)
  - Revocar permisos (permanentes o temporales)
  - Reabrir expediente cerrado
  - Cambiar contraseña/email (admin)
  - Darse de baja como responsable
  - Desbloquear cuenta bloqueada
- **Flujo:** 1er click muestra modal de confirmación → 2do click ejecuta

---

### 4.2 Módulo de Gestión de Carreras

#### RF-CARR-001: Carreras Precargadas en BD
- Tabla `carreras`:
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

#### RF-CARR-002: Asignación de Académicos/Administrativos por Carrera
- Admin registra usuario @usal en tabla `usuarios` (sin permisos)
- Admin luego asigna permisos permanentes:
  - Crea fila en `user_career_permissions` para cada carrera
  - Un usuario puede tener acceso a múltiples carreras

---

### 4.3 Módulo de Estados (Estructura de BD Mejorada)

#### RF-ESTADO-BD-001: Tabla de Estados con Código Único
- Tabla `estados`:
  - `id` (auto-incremental, PK)
  - `codigo` (VARCHAR, UNIQUE) = "SOL-INI", "DOC-CAR", "ANAL-PREL", etc.
  - `nombre` (VARCHAR) = "Solicitud iniciada", "Documentación cargada", etc.
  - `descripcion` (TEXT)
  - `es_terminal` (BOOLEAN) — si es estado final
  - `activo` (BOOLEAN)

#### RF-ESTADO-BD-002: Relación Estados-Solicitud
- Tabla `estados_solicitud`:
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

#### RF-ESTADO-BD-003: Referencias en Auditoría
- Cuando auditoría registra cambio de estado:
  - Se guarda siempre `estado_anterior_codigo` y `estado_nuevo_codigo`
  - **NUNCA se usa nombre de estado**, solo código
  - Esto permite que si en futuro se renombran estados, auditoría sigue siendo válida

---

### 4.4 Módulo de Auditoría Detallada

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

---

### 4.5 Módulo de Recopilación de Datos Iniciales

#### RF-DATOS-001: Formulario Inicial
- Aspirante completa datos académicos:
  - Datos personales (pre-rellenados de registro)
  - Facultad/Universidad de origen
  - Carrera de origen
  - **Carrera destino:** Dropdown de carreras precargadas en BD (por `codigo`)
  - Condición académica
  - Observaciones
- Validación de datos
- Se guarda en tabla `solicitudes` con:
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

---

### 4.6 Módulo de Asignación Dinámica de Responsables

#### RF-ASIGN-001: Crear Solicitud (SIN asignación automática)
- Después de completar formulario inicial, aspirante clickea "Crear solicitud"
- Se genera ID: `EXP-2024-001`
- Estado → **"Solicitud iniciada"** (código: "SOL-INI")
- **SIN asignación de responsable aún**
- Sistema manda mail AUTOMÁTICO a TODOS los usuarios con acceso de proceso a esa carrera:
  - Asunto: "E3-2024-001: Nueva solicitud en Ingeniería Informática"
  - Cuerpo: "Nueva solicitud del aspirante Juan Pérez. [Ver detalles] [Tomar proceso]"
  - Link directo al proceso

#### RF-ASIGN-002: "Tomar Proceso" - Asignación Dinámica
- Cuando académico/administrativo hace click en "Tomar proceso":
  - Se abre modal de confirmación
  - Muestra: "Eres responsable de este proceso a partir de ahora"
  - Botón: "Confirmar"
  - Al confirmar:
    - Sistema asigna: `responsable_academico` o `responsable_administrativo` (según rol)
    - Guarda: `fecha_asignacion_academico` o `fecha_asignacion_administrativo` (timestamp)
    - Estado permanece **"Solicitud iniciada"** (sin cambiar)
    - Se envía mail a TODOS los demás con acceso a esa carrera:
      - Asunto: "E3-2024-001: Christian ha tomado este proceso"
      - Contenido: "Christian LOPEZ PASARON es ahora responsable. Ya no puedes tomar este proceso."
    - En el panel, el botón "Tomar proceso" desaparece para otros
    - Se registra en auditoría (acción: "tomar_proceso")

#### RF-ASIGN-003: Ver Responsable Actual
- Panel del Aspirante muestra prominentemente:
  - **Responsable Académico:** [Si aplica] Nombre + email clickeable + fecha asignación
  - **Responsable Administrativo:** [Si aplica] Nombre + email clickeable + fecha asignación
  - Si hay ambos, se muestran los dos
  - Si no hay responsable aún, muestra: "Esperando que alguien tome el proceso"

#### RF-ASIGN-004: Darse de Baja como Responsable
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

#### RF-ASIGN-005: Admin Asigna Responsable (Reasignación) - MEJORADO
- Admin General puede asignar/reasignar responsable manualmente:
  - Accede a solicitud
  - Botón: "Asignar responsable"
  - Abre modal con listado de usuarios
  - **Importante:** Puede seleccionar usuarios con o sin acceso permanente a carrera
    - Si selecciona usuario SIN acceso permanente:
      - Sistema crea automáticamente fila en `user_solicitud_permissions` (permiso temporal)
      - Usuario inmediatamente puede ver y participar en esa solicitud
  - Elige académico Y/O administrativo
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

#### RF-ASIGN-006: Reasignación Forzada por Revocación de Permisos - MEJORADO
- Si Admin revoca permiso a usuario que es responsable activo (con fuerza):
  - Sistema automáticamente busca OTROS usuarios con acceso a esa carrera
  - Si hay:
    - Los notifica por mail (como "proceso disponible")
    - No asigna automáticamente (deben "tomar proceso")
  - Si NO hay:
    - **Alerta en Dashboard del Admin:** Ícono/banner rojo
    - "⚠️ Proceso sin responsable académico"
    - Link directo a solicitud: "E3-2024-001 (Juan Pérez - Informática)"
    - Click lleva a solicitud → Modal "Asignar responsable"
    - Sistema manda mail a Admin: "Proceso E3-2024-001 sin responsable académico"
    - El admin debe asignar a alguien (puede usar permiso temporal)

---

### 4.7 Módulo de Chat Grupal

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
- Si hay cambio de responsable (RF-ASIGN-005):
  - Chat NO se borra
  - Nuevo responsable ve historial completo
  - Se registra en auditoría: quién fue el responsable anterior
- El chat está vinculado a la solicitud (no al usuario)

---

### 4.8 Módulo de Gestión de Solicitudes (Aspirante)

#### RF-SOL-001: Crear Nueva Solicitud (SIN asignación automática)
- Después de completar formulario inicial, aspirante clickea "Crear solicitud"
- Se genera ID: `EXP-2024-001`
- Estado → **"Solicitud iniciada"** (código: "SOL-INI")
- **NO se asigna responsable automáticamente**
- Sistema manda mails a todos con acceso a esa carrera
- Aspirante ve: "Tu solicitud ha sido creada. Esperando que alguien tome el proceso."

#### RF-SOL-002: Cargar Documentación Inicial
- Aspirante carga documentos (Analítico, Plan, Programas)
- Validación: PDF/JPG, máx 10 MB individual, 50 MB total
- Al hacer click en "Enviar documentación":
  - **El SISTEMA automáticamente cambia estado a "Documentación cargada"** (código: "DOC-CAR")
  - **Aspirante NO puede cambiar estado manualmente**
  - Mail al responsable (si lo hay): "Documentación cargada. ID: EXP-2024-001"
  - Si no hay responsable aún, mail a todos con acceso: "Documentación lista para EXP-2024-001"

#### RF-SOL-003: Ver Estado de Solicitud
- Panel muestra:
  - Estado actual (con código, ej: "DOC-CAR - Documentación cargada")
  - Responsables (académico y/o administrativo) + fecha asignación
  - Timeline de cambios de estado (visual linda, ordenado DESC por fecha)
  - Documentación cargada (listado con fechas)
  - Botón para abrir chat
  - Pestaña "Historial" con timeline visual

#### RF-SOL-004: Recargar Documentación
- Si estado es **"Documentación rechazada"** (código: "DOC-RECH")
- Aspirante recarga documentos
- Al enviar: Sistema cambia estado a **"Documentación cargada"** (código: "DOC-CAR") automáticamente

#### RF-SOL-005: Ver Informe de Materias
- Después de aprobación (estado: "APROBADO")
- Aspirante ve resumen de equivalencias (solo lectura)
- Tabla: materias reconocidas, carga horaria, correlativas
- Cantidad a adeudar, observaciones, monto a pagar

#### RF-SOL-006: Efectuar Pago
- Cargar comprobante de pago (imagen JPG/PDF)
- Validación de tamaño (máx 10 MB)
- Al enviar: Sistema cambia estado a **"Esperando verificación de pago"** (código: "PAG-ESP")

#### RF-SOL-007: Descargar Expediente
- Al finalizar (estado: "EXPEDIENTE FINALIZADO")
- Aspirante puede descargar ZIP con:
  - Solicitud inicial (JSON/PDF)
  - Documentos cargados
  - Matriz de equivalencias
  - Horario propuesto
  - Disposición decanal
  - Comprobante de pago
- Archivo: `EXP-2024-001_Expediente_[DNI].zip`

---

### 4.9 Módulo de Gestión del Proceso - ACADÉMICO (DETALLADO)

#### RF-ACAD-001: Ver Solicitudes Pendientes (ORDENAMIENTO MEJORADO)
- **Listado con filtros:**
  - Por estado (dropdown con todos los estados)
  - Por fecha (última actualización, rango)
  - Búsqueda por ID solicitud, nombre aspirante, carrera
  - Mis asignadas vs. Todas

- **Ordenamiento:**
  - **Si usuario TIENE carreras asignadas (filas en user_career_permissions):**
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

#### RF-ACAD-002: Vista Detallada de Solicitud
- **Panel con pestañas:**
  1. **Información General:**
     - Datos del aspirante (nombre, email, teléfono, DNI, universidad origen)
     - Carrera de destino
     - Responsables actuales (académico + administrativo si hay)
     - Estado actual
     - Fechas clave (creación, última mod, último cambio estado)
  2. **Documentación:**
     - Lista de documentos cargados (con fecha, tamaño, descarga)
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

#### RF-ACAD-003: Realizar Análisis Preliminar
- **Acciona:** Desde estado **"Documentación cargada"** (DOC-CAR)
- Académico accede a solicitud
- Ve documentación del aspirante (descargable)
- Genera matriz tentativa (puede cargar archivo o escribir notas)
- Genera horario propuesto (puede cargar archivo o escribir notas)
- Botón: "Completar análisis"
  - Doble verificación (1er click muestra modal, 2do click ejecuta)
  - Sistema cambia estado a **"Análisis preliminar completado"** (ANAL-COMPL)
  - Registra en auditoría
  - Mail al aspirante: "Tu solicitud está siendo analizada..."

#### RF-ACAD-004: Proponer Horarios para Reunión
- **Acciona:** Desde estado **"Análisis preliminar completado"** (ANAL-COMPL)
- Académico abre **calendario integrado**
- Propone hasta **7 horarios disponibles** (próximos 14-30 días)
- Duración: 1 hora (10:00-18:00, franjas académicas)
- Botón: "Guardar propuesta"
  - Sistema manda mail al aspirante: "Elige un horario para reunión"
  - Estado → **"Esperando selección de horario"** (RES-HORA)

#### RF-ACAD-005: Confirmar Horario Elegido por Aspirante
- **Acciona:** Cuando aspirante elige horario
- Académico ve modal: "Aspirante eligió [fecha/hora]. ¿Confirmas?"
- Opciones:
  - **Confirmar:** Doble verificación
    - Estado → **"Reunion confirmada"** (RES-CONF)
    - Mail a aspirante
  - **Rechazar/Proponer nuevos:** Ronda 2 (máximo 2)

#### RF-ACAD-006: Post-Reunión (Meet) - Notas y Decisión
- **Acciona:** Después de realizar el Meet
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
    - Si "Permite": Estado → **"Revisión legal completada"** (REV-LEG)
    - Si "Cambios": Estado → **"Documentación rechazada"** (DOC-RECH)
    - Si "Rechaza": Estado → **"Rechazado"** (RECHAZO)
    - Mail al aspirante con resultado

#### RF-ACAD-007: Revisión Legal de Documentación
- **Acciona:** Desde estado **"Revisión legal completada"** (REV-LEG)
- Académico verifica:
  - Validez de firmas electrónicas
  - Concordancia entre documentos y análisis
- Puede cargar archivo de observaciones
- Decisión (radio buttons):
  - ☐ Aprobado
  - ☐ Rechazado
  - ☐ Requiere cambios
- Botón: "Guardar decisión" (doble verificación)
- Sistema:
  - Si aprobado: Estado → **"Aprobado"** (APROBADO)
  - Si rechazado: Estado → **"Rechazado"** (RECHAZO)
  - Si cambios: Estado → **"Documentación rechazada"** (DOC-RECH)
  - Mail al aspirante

#### RF-ACAD-008: Envío a Secretaría Administrativa
- **Acciona:** Desde estado **"Aprobado"** (APROBADO)
- Botón: "Enviar a secretaría"
  - Doble verificación
  - Estado → **"Enviado a secretaría"** (ENV-SECREC)
  - Mail a secretaría: "Solicitud aprobada lista para gestión de pago"
  - Registra en auditoría

#### RF-ACAD-009: Exportación de Expedientes
- Botón: "Descargar expediente"
- Opciones:
  - Descargar expediente individual en ZIP
  - Descargar múltiples (filtro por estado, fecha, etc.)
- Archivo descargado registra en auditoría

#### RF-ACAD-010: Cierre de Expediente
- **Acciona:** Cuando secretaría ha generado disposición
- Académico revisa disposición
- Botón: "Cierre y finalización"
  - Doble verificación
  - Estado → **"Expediente finalizado"** (FINAL)
  - Mail al aspirante: "¡Tu trámite ha sido completado!"
  - Registra en auditoría

---

### 4.10 Módulo de Gestión del Proceso - SECRETARÍA ADMINISTRATIVA (DETALLADO)

#### RF-SEC-001: Ver Solicitudes Pendientes (ORDENAMIENTO MEJORADO)
- **Igual que Académico (RF-ACAD-001):**
  - Listado con filtros por estado, fecha, búsqueda
  - **Ordenamiento idéntico:**
    - Si tiene carreras asignadas: mostrar primero las de sus carreras
    - Si no tiene: mostrar todas alfabéticamente por carrera
  - Columnas: ID, Aspirante, Carrera, Estado, Responsable, Última actualización
  - Botones: "Ver detalles", "Tomar proceso" (si aplica)

#### RF-SEC-002: Vista Detallada de Solicitud
- **Igual que Académico (RF-ACAD-002):**
  - Pestañas: Información, Documentación, Análisis, Reunión, Chat, Historial, Auditoría

#### RF-SEC-003: Generar Cupón de Pago
- **Acciona:** Desde estado **"Enviado a secretaría"** (ENV-SECREC)
- Secretaría abre solicitud
- Sección "Gestión de Pago":
  - Información: Monto (pre-rellenado según política), ID solicitud, datos aspirante
  - Botón: "Generar cupón"
    - Doble verificación (1er click muestra preview, 2do click ejecuta)
    - Sistema genera PDF con cupón
    - Guarda cupón en BD
    - Estado → **"Cupón generado"** (CUPON)
    - Mail al aspirante: "Tu cupón está listo. Descárgalo aquí: [link]"

#### RF-SEC-004: Verificar Pago
- **Acciona:** Desde estado **"Esperando verificación de pago"** (PAG-ESP)
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
  - Si correcto: Estado → **"Pago verificado"** (PAG-VER)
    - Mail al aspirante: "Tu pago fue verificado"
  - Si incorrecto: Estado → **"Pago rechazado"** (PAG-RECH)
    - Mail al aspirante: "Hay problema con tu comprobante. [Comentario de secretaría]"

#### RF-SEC-005: Confeccionar Disposición Decanal
- **Acciona:** Desde estado **"Pago verificado"** (PAG-VER)
- Secretaría abre sección "Disposición Decanal"
- Opciones:
  - Cargar PDF generado externamente
  - Usar plantilla del sistema (genera documento)
- Botón: "Generar disposición"
  - Doble verificación
  - Sistema guarda disposición en BD
  - Estado → **"Disposición generada"** (DISP-GEN)
  - Mail a académico: "Disposición generada. Revisar y confirmar cierre."

#### RF-SEC-006: Exportación de Expedientes
- Botón: "Descargar expediente"
- Opciones:
  - Descargar expediente individual en ZIP
  - Descargar múltiples (filtro por estado, fecha)
  - Exportación consolidada: `Expedientes_2024_[fecha].zip`
- Cada descarga registra en auditoría (usuario, timestamp, qué descargó)

#### RF-SEC-007: Archivar y Finalizar
- **Acciona:** Cuando académico ha cerrado (estado FINAL)
- Secretaría realiza cierre administrativo:
  - Asigna número de expediente final: "EXP-2024-001-E3"
  - Archiva copia de disposición en servidor
  - El proceso permanece en estado FINAL (terminal)

---

### 4.11 Módulo de Admin

#### RF-ADMIN-001: Registrar Nueva Cuenta @usal.edu.ar
- Panel "Gestión de usuarios"
- Registrar nueva cuenta:
  - Email @usal.edu.ar
  - Rol: Académico, Administrativo, Revisor Logs
  - Cuenta se crea con **acceso básico** (NO se asignan carreras)
  - Sistema NO envía mail de confirmación (usuario entra directamente con Google)

#### RF-ADMIN-002: Asignación de Permisos Permanentes
- Seleccionar usuario registrado
- Seleccionar carrera(s)
- Botón: "Asignar permiso"
  - Crea fila en `user_career_permissions`
  - Usuario inmediatamente puede "Tomar proceso" en esa carrera
  - Cambio se registra en auditoría
  - Mail al usuario: "Se te ha asignado acceso a [carrera]"

#### RF-ADMIN-003: Revocar Permisos Permanentes (con validaciones)
- Igual RF-AUTH-007: Si hay proceso activo, no permite directamente
- Puede forzar (doble verificación) → reasignación automática

#### RF-ADMIN-004: Asignación Manual de Responsable (con Permiso Temporal)
- Modal con listado de usuarios (todos, sin filtrar por carrera)
- Al seleccionar usuario SIN acceso permanente a carrera:
  - Checkbox: "Este usuario no tiene acceso a esta carrera. ¿Crear permiso temporal?"
  - Si marca: Sistema crea fila en `user_solicitud_permissions` para esa solicitud
  - Usuario inmediatamente puede participar
- Doble verificación
- Mails a involucrados
- Auditoría registra creación de permiso temporal

#### RF-ADMIN-005: Reabrir Expediente Cerrado
- Seleccionar expediente en estado FINAL
- Elegir a qué estado volver
- Doble verificación
- Mails a responsables
- Auditoría registra

#### RF-ADMIN-006: Dashboard de Alertas
- Alertas visuales:
  - 🔴 **Procesos sin responsable académico:** "E3-2024-001 (Juan Pérez - Informática) [Ver]"
  - 🔴 **Procesos sin responsable administrativo:** "E3-2024-002 ..."
  - 🟡 **Cuentas bloqueadas:** "Juan Silva bloqueado hasta 2024-09-27 14:30 [Desbloquear]"
- Clicks redirigen a solicitud o acción correspondiente
- Botón para desbloquear cuentas sin doble verificación

#### RF-ADMIN-007: Ver Auditoría General
- Panel "Auditoría del sistema"
- Tabla `auditoria_sistema` con filtros:
  - Por usuario
  - Por tipo de acción
  - Por fecha
  - Por entidad
- Columnas: Timestamp, Usuario, Acción, Entidad, Detalles, IP
- Referencia a estados siempre por **código**, no nombre

#### RF-ADMIN-008: Perfil de Admin
- Sección "Mi perfil":
  - Avatar (personalizable por género)
  - Email (cambiar con doble verificación, validar dominio @usal.edu.ar)
  - Contraseña (cambiar con doble verificación)
  - Historial de logins (últimos 10 con IP, fecha)
  - Información personal (teléfono, etc.)

---

### 4.12 Módulo de Revisor Logs

#### RF-REVISOR-001: Acceso de Lectura
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

### 4.13 Módulo de Perfil de Usuario

#### RF-PROF-001: Perfil para Todos los Roles
- Sección "Mi perfil":
  - **Avatar:**
    - Galerías por género
    - Cambiar selección
  - **Información:**
    - Nombre (si no es OAuth, editable)
    - Email (no editable para OAuth, para admin con doble verificación)
    - Teléfono (editable)
  - **Seguridad:**
    - Cambiar contraseña (no para OAuth, admin sí con doble verificación)
    - Historial de accesos (últimos 10 logins con IP, fecha, duración)
    - Sesiones activas (desconectar si hay múltiples)
  - Todos los cambios registrados en auditoría

---

## 5. ESTRUCTURA DE TABLAS CLAVE (Módulo Autenticación)

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

### **¿Qué carreras tiene un usuario? (Para el ordenamiento RF-ACAD-001)**

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
8. ✅ Carreras precargadas en BD
9. ✅ Asignación dinámica (no automática): "Tomar proceso"
10. ✅ Máximo 2 rondas de calendario
11. ✅ Chat grupal (aspirante + académico + administrativo)
12. ✅ Doble verificación en todas las acciones críticas
13. ✅ Auditoría dual: General (admin) + Proceso (todos)
14. ✅ Avatar personalizable por género
15. ✅ Historial visual para todos (ordenado DESC por fecha)
16. ✅ Admin puede revocar con fuerza (con confirmación)
17. ✅ Sistema cambia estado (no aspirante)
18. ✅ **Estados con tabla catálogo (código UNIQUE)**, referencias por código
19. ✅ **Auditoría referencia estados por código**, no nombre
20. ✅ Búsqueda de historial: `SELECT ... WHERE solicitud_id = ? ORDER BY fecha_asignacion DESC`
21. ✅ Procesos sin responsable: alerta visual en dashboard admin
22. ✅ Admin puede asignar sin acceso permanente (permiso temporal)
23. ✅ Paneles detallados para Académico y Secretaría
24. ✅ **Ordenamiento en listado de solicitudes:**
    - Si usuario tiene carreras asignadas: mostrar primero esas, luego resto alfabético
    - Si NO tiene: mostrar todas alfabético por carrera
25. ✅ **Permisos sin permission_level:** Fila existe → participante, no existe → observador

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
  → Usuario pierden acceso INMEDIATAMENTE (próxima query devuelve 0 filas)
```

---

**Versión:** 6.0 FINAL  
**Fecha:** Septiembre 2024  
**Autores:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]  
**Estado:** LISTO PARA ARQUITECTURA DE BD Y HITOS DE DESARROLLO
