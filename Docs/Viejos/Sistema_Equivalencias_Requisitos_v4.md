# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales - Versión 4.0

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
  - Acceso básico (default): Ver TODAS las solicitudes de TODAS las carreras (lectura)
  - Acceso de proceso: Participar en proceso de equivalencia para carrera(s) específica(s) asignadas por Admin
- **Ejemplo:** Christian LOPEZ PASARON (ch.lopezpasaran) — Ingeniería en Informática
- **Acciones:** Ver solicitudes, "Tomar proceso" (si hay acceso a carrera), realizar análisis, revisar, tomar decisiones, participar en chat grupal, descargar expedientes
- **Visibilidad documental:** Expediente completo de sus solicitudes activas + visualización (lectura) de colegas

### 3.3 Secretaría Administrativa
- **Perfil:** Personal de administración/secretaría académica
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Permisos:** Similar a Académico (acceso básico + acceso de proceso por carrera)
- **Ejemplo:** Jimena Genoves
- **Acciones:** Iguales que Académico (ver solicitudes, participar en chat, tomar proceso, gestionar pago, descargar expedientes)
- **Visibilidad:** Expediente completo de todas las solicitudes (rol administrativo)

### 3.4 Admin General
- **Perfil:** Administrador del sistema (IT/Administración)
- **Método de acceso:** Usuario + Contraseña (hasheada, NO OAuth)
- **Permisos:** TODOS (puede cambiar: email, contraseña desde panel)
- **Acciones principales:**
  - Crear/modificar cuentas @usal (académico o administrativo)
  - Asignar/revocar permisos de proceso por carrera
  - Asignar manualmente responsable a solicitud
  - Reasignar procesos activos (con confirmación doble)
  - Ver auditoría general (todos los cambios del sistema)
  - Reabrir expedientes cerrados
  - Ver perfil, cambiar avatar, etc.
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

#### RF-AUTH-002: Login de Aspirantes
- Email + contraseña
- Recupero de contraseña: email + código (expiración 10 minutos)
- Sesión con expiración (30 días con "recuérdame")
- Cada login registrado en auditoría (timestamp, IP)

#### RF-AUTH-003: Login de Académicos y Secretaría Administrativa
- OAuth 2.0 con Google, dominio @usal.edu.ar
- Sesión automática
- Roles asignados por Admin General

#### RF-AUTH-004: Login de Admin General
- **Usuario + Contraseña (hasheada, a la vieja escuela)**
- NO OAuth
- Sesión con expiración (1 día)
- Cada login registrado en auditoría (timestamp, IP)
- **Cambio de credenciales desde panel:**
  - Puede cambiar email (con validación)
  - Puede cambiar contraseña (con doble verificación)
  - Cambios quedan en auditoría

#### RF-AUTH-005: Login de Revisor Logs
- OAuth 2.0 con Google, dominio @usal.edu.ar
- Rol asignado por Admin
- Sesión normal

#### RF-AUTH-006: Gestión de Permisos basada en Roles (RBAC)
- **Sistema de permisos por tipo:**
  - **Acceso Básico (default):** Ver todas las solicitudes de todas las carreras (lectura)
  - **Acceso de Proceso:** Participar en equivalencias de carrera(s) específica(s)
- **Implementación técnica:**
  - BD almacena: `user_id`, `role`, `permissions` (como bitmap/array)
  - Permisos número (estilo Linux):
    - `1` = Ver procesos
    - `2` = Participar en proceso Informática
    - `4` = Participar en proceso Industriales
    - `8` = Participar en proceso Ingeniería Civil
    - etc. (uno por carrera)
  - Ejemplo: Académico con permisos `3` (1+2) = Ve procesos + Participa en Informática
- **Admin puede:**
  - Crear nueva cuenta @usal (académico o administrativo)
  - Cuenta se crea con solo acceso básico (1)
  - Admin asigna después otros permisos según carrera(s)

#### RF-AUTH-007: Restricción de Revocación de Permisos
- Si un académico/administrativo **tiene proceso activo** en una carrera:
  - Admin **NO puede revocar** acceso a esa carrera directamente
  - Si admin intenta, sistema muestra: "Proceso activo. Para revocar, asigne responsable a otro usuario primero."
  - **Excepción:** Admin puede forzar revocación con doble verificación:
    - Primer click: pregunta
    - Segundo click: "Estoy seguro, revocar igual"
    - Automáticamente se reasigna el proceso a otros usuarios con acceso a esa carrera
    - Se manda mail a todos con acceso a esa carrera
  - Acción se registra en auditoría con comentario

#### RF-AUTH-008: Doble Verificación en Todas las Acciones Críticas
- Acciones que requieren 2 clicks + confirmación:
  - Aprobar solicitud
  - Rechazar solicitud
  - Cambiar responsable (admin asigna)
  - Revocar permisos
  - Reabrir expediente cerrado
  - Cambiar contraseña/email (admin)
  - Darse de baja como responsable
- **Flujo:** 1er click muestra modal de confirmación → 2do click ejecuta

---

### 4.2 Módulo de Gestión de Carreras

#### RF-CARR-001: Carreras Precargadas en BD
- Sistema contiene tabla `carreras` con:
  - `id` (auto-incremental)
  - `codigo` (único, ej: "ING-INF-001")
  - `nombre` (ej: "Ingeniería en Informática")
  - `facultad` (ej: "Facultad de Ingeniería")
  - `descripcion`
  - `activo` (boolean)
- Carreras precargadas inicialmente para Ingeniería USAL:
  - Ingeniería en Informática
  - Ingeniería Industrial
  - Ingeniería Civil
  - etc. (según oferta USAL)
- **Admin puede:**
  - Agregar nuevas carreras
  - Modificar datos de carrera
  - Desactivar carrera (soft delete)

#### RF-CARR-002: Asignación de Académicos/Administrativos por Carrera
- Admin asigna a cada usuario qué carrera(s) puede procesar
- Tabla `user_career_permissions`:
  - `user_id`
  - `career_id`
  - `permission_level` (1=observador, 2=participante)
  - `assigned_at`
- Un usuario puede tener permisos en múltiples carreras
- Un usuario puede tener diferentes permisos en diferentes carreras

---

### 4.3 Módulo de Recopilación de Datos Iniciales

#### RF-DATOS-001: Formulario Inicial (igual v3, pero con carrera precargada)
- Aspirante completa formulario con:
  - Datos personales (pre-rellenados)
  - Facultad/Universidad origen
  - Carrera origen
  - **Carrera destino:** Dropdown pre-cargado de carreras en BD
  - Condición académica
  - Observaciones
- Validación de datos
- Se guardan en tabla `solicitudes_inicial`

---

### 4.4 Módulo de Asignación Dinámica de Responsables

#### RF-ASIGN-001: Crear Solicitud (SIN asignación automática)
- Cuando aspirante completa formulario inicial:
  - Se genera ID único: `EXP-2024-001` (formato: EXP-YYYY-XXX)
  - Estado → **"Solicitud iniciada"**
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
    - Guarda: `fecha_asignacion` (timestamp)
    - Estado permanece **"Solicitud iniciada"** (sin cambiar)
    - **Importante:** Se envía mail a TODOS los demás con acceso a esa carrera:
      - Asunto: "E3-2024-001: Christian ha tomado este proceso"
      - Contenido: "Christian LOPEZ PASARON es ahora responsable. Ya no puedes tomar este proceso."
    - En el panel, el botón "Tomar proceso" desaparece para otros
    - Se registra en auditoría

#### RF-ASIGN-003: Ver Responsable Actual
- Panel del Aspirante muestra prominentemente:
  - **Responsable Académico:** [Si aplica] Nombre + email clickeable
  - **Responsable Administrativo:** [Si aplica] Nombre + email clickeable
  - Si hay ambos, se muestran los dos
  - Fecha en que se asignó
  - Si no hay responsable aún, muestra: "Esperando que un académico tome el proceso"

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
    - Confirmar → Sistema manda mail a TODOS los demás con acceso:
      - Asunto: "E3-2024-001: Proceso disponible nuevamente"
      - Contenido: "Christian se ha dado de baja. Puedes tomar este proceso."
    - `responsable_academico` o `responsable_administrativo` pasa a NULL
    - Se registra en auditoría

#### RF-ASIGN-005: Admin Asigna Responsable (Reasignación)
- Admin General puede asignar/reasignar responsable manualmente:
  - Accede a solicitud
  - Botón: "Asignar responsable"
  - Abre modal con listado de usuarios con acceso a esa carrera
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

#### RF-ASIGN-006: Reasignación Forzada por Revocación de Permisos
- Si Admin revoca permiso a usuario que es responsable activo (con fuerza):
  - Sistema automáticamente busca OTROS usuarios con acceso a esa carrera
  - Si hay:
    - Los notifica por mail (como "proceso disponible")
    - No asigna automáticamente (deben "tomar proceso")
  - Si NO hay:
    - Genera alerta para Admin: "¡No hay otro usuario para esta carrera!"
    - El proceso queda sin responsable
    - Sistema manda mail a Admin: "Proceso E3-2024-001 sin responsable académico"

---

### 4.5 Módulo de Recopilación de Datos Iniciales

#### RF-DATOS-001: Formulario Inicial (igual v3)
- Aspirante completa datos académicos
- Carrera destino: dropdown de carreras precargadas en BD
- Se guarda tabla `solicitudes` con:
  ```
  id (PK)
  codigo_exp (VARCHAR, UNIQUE) = "EXP-2024-001"
  aspirante_id (FK)
  carrera_destino_id (FK a tabla carreras)
  estado_id (FK)
  responsable_academico_id (FK, nullable)
  responsable_administrativo_id (FK, nullable)
  fecha_creacion (TIMESTAMP)
  fecha_ultima_mod (TIMESTAMP)
  fecha_ultimo_cambio_estado (TIMESTAMP)
  fecha_asignacion_academico (TIMESTAMP, nullable)
  fecha_asignacion_administrativo (TIMESTAMP, nullable)
  ... otros campos
  ```

---

### 4.6 Módulo de Gestión de Solicitudes (Aspirante)

#### RF-SOL-001: Crear Nueva Solicitud (SIN asignación automática)
- Después de completar formulario inicial, aspirante clickea "Crear solicitud"
- Se genera ID: `EXP-2024-001`
- Estado → **"Solicitud iniciada"**
- **NO se asigna responsable automáticamente**
- Sistema manda mails a todos con acceso a esa carrera (RF-ASIGN-001)
- Aspirante ve: "Tu solicitud ha sido creada. Esperando que alguien tome el proceso."

#### RF-SOL-002: Cargar Documentación Inicial
- Aspirante carga documentos (Analítico, Plan, Programas)
- Al hacer click en "Enviar documentación":
  - Sistema valida documentos
  - **El SISTEMA automáticamente cambia estado a "Documentación cargada"**
  - **Aspirante NO puede cambiar estado manualmente**
  - Mail al responsable (si lo hay): "Documentación cargada. ID: EXP-2024-001"
  - Si no hay responsable aún, mail a todos con acceso: "Documentación lista para EXP-2024-001"

#### RF-SOL-003: Ver Estado de Solicitud
- Panel muestra:
  - Estado actual
  - Responsables (académico y/o administrativo)
  - Timeline de cambios de estado (visual linda)
  - Documentación cargada
  - Botón para participar en chat (RF-MSG)
  - Auditoría del proceso (visual, ver punto 18 de mejoras)

#### RF-SOL-004: Recargar Documentación
- Si estado es **"Documentación rechazada"**
- Aspirante recarga documentos
- Al enviar: Sistema cambia estado a **"Documentación cargada"** automáticamente

#### RF-SOL-005: Ver Informe de Materias
- Después de aprobación, aspirante ve resumen de equivalencias
- Solo lectura

#### RF-SOL-006: Efectuar Pago
- Cargar comprobante de pago
- Sistema cambia estado a **"Esperando verificación de pago"**

#### RF-SOL-007: Descargar Expediente
- Al finalizar, aspirante puede descargar ZIP con toda la documentación

---

### 4.7 Módulo de Chat Grupal

#### RF-MSG-001: Canal de Chat Grupal (NO automatizado)
- **Ubicación:** Pestaña "Consultas/Chat" en cada solicitud
- **Participantes:** Aspirante + Académico responsable + Administrativo responsable (si hay)
- **Nota importante:** Si no hay responsable aún, aspirante NO puede abrir chat (botón deshabilitado)

#### RF-MSG-002: Estructura de Mensajes
- Cada mensaje tiene:
  - **Avatar:** Icono personalizable del usuario
    - Hombre: opciones de avatares masculinos
    - Mujer: opciones de avatares femeninos
    - Avatar editable desde perfil
  - **Nombre:** Nombre del usuario (solamente)
  - **Contenido:** Texto + archivo opcional (máx 5 MB)
  - **Timestamp:** Fecha y hora exacta
  - **Visto:** Indicador si fue leído
- Máximo 500 caracteres por mensaje
- Tipo de consulta (dropdown): Documentación, Estado, Cálculos, Fechas, Otro

#### RF-MSG-003: Participación en Chat
- **Aspirante:**
  - Puede escribir mensajes
  - Ve mensajes de académico y administrativo
  - Notificación si hay respuesta (en panel + email)
- **Académico responsable:**
  - Puede escribir mensajes
  - Ve mensajes de aspirante y administrativo
  - Puede responder directamente en chat (no obligado por email)
  - Notificación de nuevos mensajes
- **Administrativo responsable:**
  - Igual que académico
  - Participa en mismo chat (grupal)
- **Otros usuarios sin acceso:**
  - NO pueden escribir (botón deshabilitado)
  - Pueden VER el chat (auditoría/observación)

#### RF-MSG-004: Historial de Chat
- Todos los mensajes quedan archivados en BD
- Si hay cambio de responsable (RF-ASIGN-005):
  - Chat NO se borra
  - Nuevo responsable ve historial completo
  - Se registra en auditoría: quién fue el responsable anterior
- El chat está vinculado a la solicitud (no al usuario)

#### RF-MSG-005: Notificaciones de Chat
- Cuando alguien escribe mensaje:
  - Mail a otros participantes: "Nuevo mensaje en E3-2024-001"
  - Badge rojo en panel
  - Notificación in-app

---

### 4.8 Módulo de Gestión de Estados

#### RF-ESTADO-001: Estados de Solicitud
- **14 estados** (igual v3, pero con matices):
  1. Solicitud iniciada (sin responsable)
  2. Documentación cargada
  3. Análisis preliminar completado
  4. Esperando selección de horario
  5. Reunion confirmada
  6. Reunion realizada
  7. Revisión legal completada
  8. Aprobado
  9. Enviado a secretaría
  10. Cupón generado
  11. Esperando verificación de pago
  12. Pago verificado
  13. Disposición generada
  14. Expediente finalizado (terminal)

**También:**
- Documentación rechazada
- Pago rechazado
- Rechazado (terminal)

#### RF-ESTADO-002: Estructura de Estado
- Tabla `estados_solicitud`:
  ```
  id (PK)
  solicitud_id (FK)
  codigo_exp (UNIQUE) = "EXP-2024-001"
  estado (VARCHAR) = "Documentación cargada"
  fecha_creacion (TIMESTAMP)
  fecha_ultima_modificacion (TIMESTAMP)
  ultimo_cambio_estado (TIMESTAMP)
  usuario_ultimo_cambio (FK a users)
  estado_anterior (VARCHAR, para auditoría)
  comentarios (TEXT, nullable)
  ```

#### RF-ESTADO-003: Notificaciones por Cambio de Estado
- Similar v3, pero considerando asignación dinámica
- Si no hay responsable aún: mail a todos con acceso a carrera

#### RF-ESTADO-004: Historial Auditable - DUAL
- **Dos tipos de auditoría:**

  **A) Auditoría General (solo Admin ve):**
  - Tabla `auditoria_sistema`
  - Registra: TODOS los cambios en el sistema
  - Campos:
    ```
    id (PK)
    timestamp (TIMESTAMP with TZ)
    usuario_id (FK)
    usuario_email (VARCHAR)
    accion (VARCHAR) = "cambio_estado", "crear_cuenta", "cambiar_avatar", etc.
    entidad (VARCHAR) = "solicitud", "usuario", "carrera", etc.
    entidad_id
    detalles (JSON)
    cambio_anterior (JSON) = estado/dato anterior
    cambio_nuevo (JSON) = estado/dato nuevo
    comentarios (TEXT)
    ip_origen (VARCHAR)
    ```
  - Solo Admin y Revisor Logs pueden ver esta tabla
  - Almacena TODOS los cambios:
    - Cambio de estado
    - Cambio de avatar
    - Cambio de contraseña
    - Crear cuenta
    - Asignar responsable
    - Mensaje enviado
    - Etc.

  **B) Historial del Proceso (todos pueden ver, visual linda):**
  - Vista/pestaña "Historial" en cada solicitud
  - Muestra en timeline:
    - Cambios de estado (fecha, hora, quién)
    - Responsables asignados/cambios
    - Mensajes enviados (resumen)
    - Documentación cargada
    - Decisiones tomadas
  - Visible para: Aspirante, Académico, Administrativo, Admin, Revisor Logs
  - **Visual clara:** Icons + cronología legible
  - No muestra detalles técnicos (IP, etc.)

---

### 4.9 Módulo de Académico y Administrativo (igual v3, adaptado)

#### RF-ACAD-001 / RF-SEC-001: Ver Solicitudes Pendientes (IGUAL para ambos)
- Listado de solicitudes filtrable:
  - Por estado
  - Por fecha
  - Búsqueda
  - Carreras a las que tiene acceso
- **Columnas:**
  - ID solicitud (EXP-2024-001)
  - Aspirante
  - Carrera
  - Estado
  - Responsable actual
  - Última actualización
  - Botones: Ver detalles, Tomar proceso (si no hay responsable)

#### RF-ACAD-002 a RF-ACAD-009: Gestión del Proceso (igual v3)
- Análisis preliminar
- Reunión (calendario)
- Revisión legal
- Decisión final
- Envío a secretaría
- Cierre
- Todos requieren doble verificación en acciones críticas

#### RF-SEC-002 a RF-SEC-005: Gestión de Pago y Disposición (igual v3)
- Generación de cupón
- Verificación de pago
- Confección de disposición
- **RF-SEC-005 (NEW): Exportación de Expedientes**
  - Secretaría puede descargar:
    - Expediente individual en ZIP
    - Múltiples expedientes (filtro por estado/fecha)
    - Archivo consolidado

#### RF-ACAD-010 (NEW): Exportación de Expedientes para Académico
- Académico también puede descargar:
  - Expedientes individuales de sus solicitudes
  - Múltiples expedientes (filtro)
  - En ZIP

---

### 4.10 Módulo de Admin

#### RF-ADMIN-001: Gestión de Cuentas @usal
- Admin puede:
  - Crear nueva cuenta (email @usal.edu.ar)
  - Seleccionar rol: Académico o Administrativo
  - Cuenta se crea con **acceso básico** (1) — solo ver procesos
  - Enviar mail de activación al usuario (con instrucciones)

#### RF-ADMIN-002: Asignación de Permisos
- Admin puede:
  - Seleccionar usuario (académico/administrativo)
  - Seleccionar carrera(s)
  - Asignar acceso de proceso para esa(s) carrera(s)
  - Usuario inmediatamente ve esa carrera en sus solicitudes
  - Cambio se registra en auditoría

#### RF-ADMIN-003: Revocar Permisos
- Admin puede:
  - Revocar acceso de proceso a usuario para carrera(s)
  - **Validación:** Si usuario es responsable activo
    - No permite revocación directa
    - Botón deshabilitado
    - Tooltip: "Usuario es responsable. Primero reasigna el proceso."
  - **Excepción:** Admin puede forzar (doble verificación)
    - Sistema automáticamente reasigna procesos
    - Manda mails correspondientes

#### RF-ADMIN-004: Asignación Manual de Responsable
- Admin puede asignar manualmente responsable a una solicitud
- Modal con listado de usuarios con acceso a carrera
- Doble verificación
- Mails a todos los involucrados
- Se registra en auditoría

#### RF-ADMIN-005: Reabrir Expediente Cerrado
- Admin puede reabrir expediente en estado **"Expediente finalizado"**
- Cambia estado a un estado anterior (pregunta cuál)
- Doble verificación
- Envía mail a responsables
- Se registra en auditoría

#### RF-ADMIN-006: Ver Auditoría General
- Panel específico para ver tabla `auditoria_sistema`
- Filtros:
  - Por usuario
  - Por tipo de acción
  - Por fecha
  - Por entidad (solicitud, usuario, etc.)
- Vista de logs completa (timestamp, usuario, IP, acción, detalles)

#### RF-ADMIN-007: Perfil de Admin
- Sección de perfil donde puede:
  - Ver avatar actual
  - Cambiar avatar
  - Ver email (no editable, es su usuario)
  - Cambiar contraseña (doble verificación)
  - Cambiar email (doble verificación, valida acceso)
  - Ver datos de login (último acceso, IP, etc.)

---

### 4.11 Módulo de Revisor Logs
- Puede:
  - Ver todas las solicitudes (lectura)
  - Ver auditoría general
  - Ver historial de procesos
  - Ver chat/mensajes
  - **NO puede:**
    - Hacer cambios en solicitudes
    - Modificar datos
    - Tomar procesos
    - Generar reportes (puede verlos, no generarlos)

---

### 4.12 Módulo de Perfil de Usuario

#### RF-PROF-001: Perfil para todos los roles
- Sección "Mi perfil" donde cada usuario puede:
  - **Avatar:**
    - Galerías segregadas por género
    - Cambiar selección
  - **Información:**
    - Nombre (si no es OAuth, editable)
    - Email (si no es OAuth, cambiar con doble verificación)
    - Teléfono (editable)
  - **Seguridad:**
    - Cambiar contraseña (doble verificación)
    - Ver historial de accesos (últimos 10 logins con IP, fecha)
    - Sesiones activas (desconectar si hay múltiples)
  - **Cambios:**
    - Todos registrados en auditoría

---

### 4.13 Módulo de Auditoría Detallada

#### RF-AUDIT-001: Sistema de Auditoría (Punto 15 de mejoras)
- Tabla `auditoria_sistema` que registra:
  - **Timestamp exacto** (fecha, hora, zona horaria)
  - **Usuario que realizó cambio** (email/ID)
  - **Tipo de acción** (cambio_estado, crear_cuenta, asignar_responsable, cambiar_avatar, etc.)
  - **Entidad afectada** (solicitud, usuario, carrera, etc.)
  - **ID de entidad**
  - **Cambio anterior** (JSON)
  - **Cambio nuevo** (JSON)
  - **Comentarios** (si aplica)
  - **IP de origen**

#### RF-AUDIT-002: Qué se audita
- Cambios de estado
- Asignación/reasignación de responsables
- Creación/modificación de cuentas
- Cambio de permisos
- Cambio de avatar/perfil
- Cambio de contraseña
- Creación de carrera
- Darse de baja como responsable
- Revocar permisos (especialmente con fuerza)
- Reabrir expediente
- Descargas de expedientes
- Mensajes enviados
- **Cualquier acción que modifique estado**

#### RF-AUDIT-003: Acceso a Auditoría
- **Admin General:** Ve tabla completa (auditoría_sistema)
- **Revisor Logs:** Ve tabla completa (auditoría_sistema)
- **Otros roles:** No ven auditoría general
- **Panel del Proceso:** Todos ven historial visual del proceso (punto 4 de mejoras)

---

## 5. NOTAS IMPORTANTES

1. ✅ Código de validación para aspirantes: expiración 10 minutos
2. ✅ Rol Admin con usuario/contraseña (no OAuth)
3. ✅ Carreras precargadas en BD
4. ✅ Asignación dinámica (no automática): "Tomar proceso"
5. ✅ Máximo 2 rondas de calendario
6. ✅ Chat grupal (aspirante + académico + administrativo)
7. ✅ Doble verificación en todas las acciones críticas
8. ✅ Auditoría dual: General (admin) + Proceso (todos)
9. ✅ Avatar personalizable por género
10. ✅ Historial visual para todos
11. ✅ Admin puede revocar con fuerza (con confirmación)
12. ✅ Sistema cambia estado (no aspirante)
13. ✅ Darse de baja solo si hay otro responsable
14. ✅ Sin responsable = mail a todos con acceso
15. ✅ Expediente cerrado puede reabrirse (admin)
16. ✅ Estados con: id, codigo_exp, fecha_asignacion, fecha_ultima_mod, etc.

---

**Versión:** 4.0  
**Fecha:** Septiembre 2024  
**Autores:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]  
**Ultima actualización:** Con mejoras de reunión de follow-up
