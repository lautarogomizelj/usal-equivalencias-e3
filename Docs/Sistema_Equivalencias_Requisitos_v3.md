# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales - Versión 3.0

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
- Cada rol (Aspirante, Académico, Secretaría Administrativa) tiene un panel personalizado
- El proceso avanza mediante transiciones de estado explícitas
- Los cambios de estado generan notificaciones automáticas por mail
- Hay trazabilidad completa de cada paso
- El almacenamiento es centralizado en la base de datos de la facultad (por año y DNI)

---

## 2. OBJETIVO DEL SISTEMA

Proporcionar un flujo de tramitación de equivalencias **trazable, automatizado y descentralizado**, donde:
1. El aspirante inicia el trámite completando un formulario inicial y cargando documentación
2. Cada rol ve un panel con acciones determinadas según su responsabilidad
3. El proceso avanza mediante transiciones explícitas sin depender de terceros recordando enviar mails
4. Hay registro auditable de cada acción, cambio de estado y timestamp
5. Las decisiones críticas requieren confirmación manual con doble verificación

---

## 3. ACTORES DEL SISTEMA

### 3.1 Aspirante
- **Perfil:** Estudiante de otra universidad solicitando equivalencias para ingresar a carrera de Ingeniería
- **Método de acceso:** Crear cuenta en el sistema (email personal + código de validación)
- **Acciones principales:** Completar formulario inicial, cargar documentación, ver responsable actual, enviar mensajes directos, ver estado, recargar documentos si se solicita, efectuar pago, visualizar informe de equivalencias
- **Visibilidad documental:** Solo sus documentos enviados, matriz tentativa y horario propuesto

### 3.2 Académico
- **Perfil:** Personal de área académica (profesor coordinador de carrera)
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Ejemplo:** Christian LOPEZ PASARON (ch.lopezpasaran) — Ingeniería en Informática
- **Acciones principales:** Recibir solicitudes asignadas por carrera, realizar análisis preliminar, revisar documentación legal, tomar decisiones de aprobación/rechazo, informar resultados, enviar y recibir mensajes directos del aspirante
- **Visibilidad documental:** Expediente completo de sus solicitudes asignadas + visualización (sin permisos de modificación) de expedientes de colegas académicos

### 3.3 Secretaría Administrativa
- **Perfil:** Personal de administración/secretaría académica
- **Método de acceso:** OAuth 2.0 con email @usal.edu.ar (Google)
- **Ejemplo:** Jimena Genoves
- **Acciones principales:** Gestionar cupones de pago, verificar pagos, confeccionar disposiciones decanales, archivar y exportar expedientes, acceso a expedientes completos
- **Visibilidad documental:** Expediente completo de todas las solicitudes

---

## 4. REQUISITOS FUNCIONALES

### 4.1 Módulo de Autenticación y Control de Acceso

#### RF-AUTH-001: Registro de Aspirantes
- El aspirante puede crear una nueva cuenta indicando:
  - Email personal
  - Contraseña (mínimo 8 caracteres, mayúscula, número, carácter especial)
  - Nombre completo
  - Teléfono de contacto
  - Número de documento (DNI)
- El sistema envía un mail de confirmación con código de validación (no link)
- Aspirante ingresa código en formulario para validar cuenta
- No puede iniciar trámite hasta que valide el email

#### RF-AUTH-002: Login de Aspirantes
- Acceso mediante email + contraseña
- Recupero de contraseña mediante email + código de validación
- Sesión con expiración (30 días con "recuérdame")
- Cada login se registra con timestamp e IP (auditoría)

#### RF-AUTH-003: Login de Académicos y Secretaría Administrativa
- OAuth 2.0 con Google, restringido a dominio @usal.edu.ar
- Si el email no pertenece al dominio, el login es rechazado
- Sesión automática (vinculada a sesión de Google)
- Los roles se asignan automáticamente en la BD según grupo USAL

#### RF-AUTH-004: Asignación Automática de Solicitudes por Carrera
- Cuando un aspirante selecciona una carrera de destino, el sistema asigna automáticamente la solicitud a:
  - Académico responsable de esa carrera (ej: Informática → Christian LOPEZ PASARON, Industriales → Sebastián)
  - Configuración editable por administrador (puede cambiar asignación después)
- **Importante:** Cualquier académico puede VISUALIZAR expedientes de colegas, pero:
  - Solo el académico asignado puede MODIFICAR/TOMAR DECISIONES
  - Los cambios registran quién los hizo

#### RF-AUTH-005: Control de Acceso basado en Roles (RBAC)
- **Aspirante:** Acceso solo a su solicitud (por ID único)
- **Académico:** Acceso a:
  - Solicitudes asignadas (todas las acciones: analizar, decidir, enviar mensajes)
  - Solicitudes de colegas (solo visualización, sin permisos de modificación)
  - Expedientes completos de sus asignadas
- **Secretaría:** Acceso a todas las solicitudes (completo)
- Cada acción es registrada con usuario, timestamp, IP, y acción realizada (auditoría)

#### RF-AUTH-006: Doble Verificación para Acciones Críticas
- Acciones que requieren doble verificación:
  - Rechazar una solicitud (requiere confirmar + escribir motivo + enviar)
  - Aprobar una solicitud (requiere confirmar + revisar datos + enviar)
  - Cambiar asignación de solicitud (requiere confirmar)
  - Anular/borrar un registro (requiere confirmación + 2 clicks)
- El sistema muestra modal de confirmación con resumen de la acción antes de ejecutar

---

### 4.2 Módulo de Recopilación de Datos Iniciales

#### RF-DATOS-001: Formulario Inicial del Aspirante
- Cuando el aspirante crea una solicitud NEW, debe completar formulario con:
  - **Datos personales (pre-rellenados de registro):**
    - Nombre completo
    - Email
    - Teléfono
    - Número de documento (DNI)
  - **Datos académicos de origen:**
    - Facultad/Universidad de origen
    - Carrera/Programa de origen
    - Año de ingreso
    - Condición (activo, egresado, suspendido, otro)
  - **Datos académicos de destino:**
    - Carrera de destino (dropdown con carreras de Ingeniería USAL)
  - **Datos adicionales:**
    - ¿Tienes analítico legalizado? (Sí/No)
    - Observaciones/comentarios (textarea opcional)

- Este formulario se completa ANTES de cargar documentación
- Los datos se guardan en el sistema y quedan visibles para Académico y Secretaría
- **Christian LOPEZ PASARON** proveerá email modelo con estructura de datos esperada

#### RF-DATOS-002: Validación de Datos Iniciales
- El sistema valida:
  - Que el DNI sea válido (formato, no duplicado en sistema)
  - Que el email sea válido y pertenezca al aspirante
  - Que carrera de destino sea válida
- Si hay validaciones fallidas, muestra errores claros y no permite continuar

---

### 4.3 Módulo de Identificación de Responsable

#### RF-RESP-001: Visualización del Responsable Actual
- El panel del Aspirante muestra prominentemente (sección "Tu trámite"):
  - **Responsable actual:** Nombre completo + rol (ej: "Lic. Christian LOPEZ PASARON - Académico")
  - **Estado actual:** [Estado vigente]
  - **Última actualización:** Fecha y hora
  - **Email de contacto:** ch.lopezpasaran@usal.edu.ar (clickeable para abrir cliente mail)
- Esta información se actualiza dinámicamente si hay reasignación

#### RF-RESP-002: Cambio de Responsable
- Si hay cambio de asignación (ej: otro académico toma la solicitud):
  - Aspirante recibe notificación por mail informando nuevo responsable
  - Panel se actualiza automáticamente
  - Histórico de responsables queda registrado en auditoría

---

### 4.4 Módulo de Mensajería Directa (No Automatizada)

#### RF-MSG-001: Canal de Mensajería Aspirante ↔ Académico
- Aspirante tiene acceso a un área de "Consultas/Mensajes" en su panel
- Puede escribir mensaje directo al Académico responsable
- **Importante:** Los mensajes NO son automáticos:
  - Se almacenan en la BD
  - Se envía notificación por correo electrónico al Académico responsable
  - El Académico responde directamente por correo (o si quiere, entra al sistema para ver el hilo)
  - Las respuestas pueden copiarse en el sistema o quedar en mail (criterio del Académico)

#### RF-MSG-002: Características de Mensajería
- Aspirante puede escribir:
  - **Máximo 500 caracteres** por mensaje
  - Tipo de consulta (dropdown): Documentación, Estado, Cálculos de equivalencias, Fechas de reunión, Otro
  - Archivo adjunto (opcional): máximo 5 MB
- Cada mensaje queda registrado con:
  - Timestamp
  - Contenido
  - Autor (aspirante)
  - Respuestas (si las hay)
- Académico ve los mensajes en su panel con notificación roja "Nuevo mensaje"

#### RF-MSG-003: Notificación al Académico
- Cuando Aspirante envía mensaje:
  - Email inmediato al Académico responsable (ch.lopezpasaran@usal.edu.ar)
  - Asunto: "E3-2024-001: Nuevo mensaje de Juan Pérez"
  - Cuerpo: Extracto del mensaje + link al panel
  - El Académico responde por mail (su preferencia explícita)

#### RF-MSG-004: Acceso a Mensajería en Secretaría
- Secretaría Administrativa tiene acceso de lectura a todos los mensajes
- Puede ver pero no puede responder (rol de auditoría)

---

### 4.5 Módulo de Gestión de Solicitudes (Aspirante)

#### RF-SOL-001: Crear Nueva Solicitud de Equivalencias
- Después de completar formulario inicial (RF-DATOS-001), aspirante clickea "Crear solicitud"
- Se genera ID único (ej: "E3-2024-001")
- Estado cambia a **"Solicitud iniciada"**
- Sistema envía mail automático al Académico asignado indicando: "Nueva solicitud de equivalencias. ID: E3-2024-001. Aspirante: Juan Pérez (carrera Informática)"
- El sistema asigna automáticamente el académico según la carrera elegida

#### RF-SOL-002: Cargar Documentación Inicial
- Dentro de la solicitud, aspirante carga:
  - Analítico parcial (PDF)
  - Plan de estudios con carga horaria (PDF)
  - Programas de materias aprobadas (PDFs, múltiples archivos)
  - Documentos adicionales (opcional)
- Sistema valida:
  - Que sea PDF o JPG
  - Tamaño máximo 10 MB por archivo
  - Tamaño total no supere 50 MB
- Al cargar documentación, aspirante puede cambiar estado a **"Documentación cargada"**
- Se envía mail automático al Académico: "Documentación lista para analizar. ID: E3-2024-001"

#### RF-SOL-003: Ver Estado de Solicitud
- Panel del Aspirante muestra:
  - Estado actual con descripción legible
  - Responsable actual (RF-RESP-001)
  - Timeline de cambios de estado (quién, cuándo, qué cambió)
  - Documentación cargada (con fecha de carga, descargable)
  - Mensajes/comentarios del Académico
  - Botones de acción disponibles según estado actual

#### RF-SOL-004: Recargar Documentación
- Si Académico rechaza documentación, estado es **"Documentación rechazada"**
- Panel muestra:
  - Motivo del rechazo (comentario del Académico)
  - Área para recargar documentos
- Al recargar, aspirante puede cambiar estado nuevamente a **"Documentación cargada"**
- Se notifica al Académico

#### RF-SOL-005: Ver Informe de Materias Equivalentes
- Después de que Académico aprueba, aspirante ve panel con:
  - Resumen de decisión (aprobado, cantidad de materias reconocidas)
  - Tabla: Materias reconocidas (nombre, carga horaria, correlativas)
  - Cantidad de materias a adeudar
  - Observaciones del Académico
  - Monto total del arancel/cupón a pagar
- Panel es **solo lectura** para aspirante

#### RF-SOL-006: Efectuar Pago
- Una vez aprobado, aspirante puede proceder al pago
- Panel muestra:
  - Monto exacto del arancel
  - Cupón descargable (generado por Secretaría)
  - Instrucciones de pago (banco, cuenta, referencia)
  - Área para cargar comprobante de pago (imagen JPG/PDF)
- Después de cargar, estado es **"Esperando verificación de pago"**
- Se notifica a Secretaría

#### RF-SOL-007: Descarga de Expediente Completo
- Al finalizar el trámite, aspirante puede descargar su expediente completo en ZIP:
  - Contiene: solicitud inicial, documentación cargada, matriz, horario, disposición
  - Nombrado: `E3-2024-001_Expediente_[DNI].zip`
  - Disponible después de estado **"Expediente finalizado"**

---

### 4.6 Módulo de Panel Académico

#### RF-ACAD-001: Ver Solicitudes Pendientes
- Listado con filtros:
  - Por estado
  - Por fecha de asignación
  - Búsqueda por ID o nombre aspirante
- Para cada solicitud:
  - ID y nombre aspirante
  - Carrera
  - Estado actual
  - Fecha última actualización
  - Botones de acción

#### RF-ACAD-002: Análisis Preliminar
- **Acciona:** Desde estado **"Documentación cargada"**
- Académico accede a vista detallada con:
  - Documentación del aspirante (descargable)
  - Comparación: plan aspirante vs. plan carrera USAL
  - Genera matriz tentativa de equivalencias
  - Arma horario propuesto de cursada
- Puede cargar archivo adjunto (matriz en PDF/Excel)
- **Transición:** Estado → **"Análisis preliminar completado"**
- Mail al Aspirante: "Tu solicitud está en análisis. Pronto tendrás respuesta."

#### RF-ACAD-003: Coordinación de Reunión (Meet)
- **Acciona:** Desde estado **"Análisis preliminar completado"**
- Académico abre **calendario integrado** en el panel
- Propone hasta **7 horarios disponibles** en próximos 14-30 días
- Duración estándar: 1 hora (ej: 14:00-15:00)
- Franjas recomendadas: 10:00-18:00
- **Guardar propuesta** → Sistema notifica al Aspirante
- Estado → **"Esperando selección de horario"**
- Mail al Aspirante: "Elige un horario para tu reunión. Opciones: [...]"

#### RF-ACAD-004: Confirmación de Horario (post-selección Aspirante)
- Aspirante eligió horario → Académico recibe notificación
- Académico ve opción de:
  - **Confirmar:** Horario se bloquea, ambos lo ven agendado
  - **No puedo:** Propone nuevos horarios (si es ronda 1, se abre ronda 2)
  - **Rechazar:** Si aspirante no pudo elegir ninguno después de 2 rondas
- Si confirma → Estado **"Reunion confirmada"**
- Mail a Aspirante: "Tu reunión está confirmada para [fecha/hora]"

#### RF-ACAD-005: Post-Meet (Notas y Decisión)
- Después de realizar el Meet:
  - Académico sube **notas de la reunión** (textarea)
  - Indica si se realizó o no (si aspirante no vino → "no show")
  - Link de Meet (opcional)
- Toma decisión:
  - **Permite inscripción:** Estado → **"Revisión legal completada"** (continúa flujo)
  - **Requiere cambios en matriz:** Estado → **"Documentación rechazada"**
  - **Rechaza solicitud:** Estado → **"Rechazado"** (final)
- Cada decisión requiere doble verificación (RF-AUTH-006)

#### RF-ACAD-006: Revisión Legal
- **Acciona:** Desde estado **"Revisión legal completada"**
- Académico verifica:
  - Validez de documentos (firmas electrónicas)
  - Concordancia entre materias y análisis preliminar
- Puede cargar documento de observaciones
- **Transición:**
  - Si OK → **"Aprobado"**
  - Si hay problemas → **"Documentación rechazada"**
- Requiere doble verificación

#### RF-ACAD-007: Decisión Final (Aprobación/Rechazo)
- **Acciona:** Desde estado **"Aprobado"** o después de revisión legal
- Académico confirma:
  - Cantidad de materias reconocidas
  - Cantidad a adeudar
  - Observaciones finales
- **Requiere doble verificación (RF-AUTH-006)**
- Decide:
  - **Aprobar:** Estado → **"Aprobado"**
  - **Rechazar:** Estado → **"Rechazado"**
  - Mail al Aspirante con decisión

#### RF-ACAD-008: Envío a Secretaría Administrativa
- **Acciona:** Cuando estado es **"Aprobado"**
- Académico clickea "Enviar a secretaría"
- Estado → **"Enviado a secretaría"**
- Mail a Secretaría: "Solicitud aprobada lista para gestión de pago. ID: E3-2024-001"

#### RF-ACAD-009: Cierre de Expediente
- **Acciona:** Cuando Secretaría ha generado disposición
- Académico revisa disposición
- Clickea "Cierre y finalización"
- **Requiere doble verificación**
- Estado → **"Expediente finalizado"**
- Mail al Aspirante: "Tu trámite de equivalencias ha sido completado."

---

### 4.7 Módulo de Panel Secretaría Administrativa

#### RF-SEC-001: Ver Solicitudes en Gestión
- Listado de solicitudes pendientes:
  - Estado **"Enviado a secretaría"** (listas para cupón)
  - Estado **"Esperando verificación de pago"** (pago cargado)
  - Estado **"Disposición generada"** (lista para entrega)

#### RF-SEC-002: Generar Cupón de Pago
- **Acciona:** Desde **"Enviado a secretaría"**
- Secretaría genera cupón con:
  - Monto (definido por política facultad)
  - ID solicitud
  - Datos aspirante
  - Referencia: "E3-2024-001"
  - Banco/cuenta
- Estado → **"Cupón generado"**
- Mail al Aspirante: "Tu cupón de pago está listo. Descargalo aquí: [link]. Monto: $[...]"

#### RF-SEC-003: Verificar Pago
- **Acciona:** Desde **"Esperando verificación de pago"** (aspirante cargó comprobante)
- Secretaría ve comprobante
- Valida monto
- **Requiere doble verificación (RF-AUTH-006)**
- Decisión:
  - **Correcto:** Estado → **"Pago verificado"** (mail: "Tu pago ha sido verificado")
  - **Incorrecto:** Estado → **"Pago rechazado"** (mail: "Hay problema con tu comprobante")

#### RF-SEC-004: Confeccionar Disposición Decanal
- **Acciona:** Desde **"Pago verificado"**
- Secretaría confecciona disposición:
  - Usa plantilla del sistema O sube PDF generado externamente
  - Incluye: nombre aspirante, carrera, materias reconocidas, observaciones
- Carga documento
- Estado → **"Disposición generada"**
- Mail a Académico: "Disposición generada. Revisar y confirmar cierre. ID: E3-2024-001"

#### RF-SEC-005: Exportación de Expedientes
- **Opción 1:** Exportar expediente individual en ZIP
  - Contiene: solicitud, documentación, matriz, disposición
  - Nombrado: `E3-2024-001_Expediente_[DNI].zip`
  - Descargable desde panel de solicitud
- **Opción 2:** Exportación masiva
  - Secretaría puede exportar múltiples expedientes finalizados
  - Genera ZIP consolidado: `Expedientes_2024_[fecha].zip`
- **Auditoría:** Cada exportación queda registrada con usuario, timestamp

#### RF-SEC-006: Archivar Solicitud
- **Acciona:** Cuando Académico ha cerrado (estado **"Expediente finalizado"**)
- Secretaría asigna número de expediente final (ej: "EXP-2024-001-E3")
- Archiva copia de disposición en carpeta del servidor
- Estado permanece **"Expediente finalizado"** (es final)

---

### 4.8 Módulo de Gestión de Estados y Flujo

#### RF-ESTADO-001: Transiciones de Estado Completas
El sistema mantiene 14 estados:

1. **Solicitud iniciada** → "Documentación cargada"
2. **Documentación cargada** → "Análisis preliminar completado" o "Documentación rechazada"
3. **Análisis preliminar completado** → "Esperando selección de horario"
4. **Esperando selección de horario** → "Reunion confirmada" o "Documentación rechazada" (2 rondas max)
5. **Reunion confirmada** → "Reunion realizada"
6. **Reunion realizada** → "Revisión legal completada" o "Documentación rechazada" o "Rechazado"
7. **Revisión legal completada** → "Aprobado" o "Rechazado"
8. **Aprobado** → "Enviado a secretaría"
9. **Enviado a secretaría** → "Cupón generado"
10. **Cupón generado** → "Esperando verificación de pago"
11. **Esperando verificación de pago** → "Pago verificado" o "Pago rechazado"
12. **Pago rechazado** → "Esperando verificación de pago" (reintentos)
13. **Pago verificado** → "Disposición generada"
14. **Disposición generada** → "Expediente finalizado" (final)

**También:**
- **Documentación rechazada** → "Documentación cargada" (aspirante recarga)
- **Rechazado** → Final (sin retorno)
- **Expediente finalizado** → Final (sin retorno)

#### RF-ESTADO-002: Notificaciones por Cambio de Estado
| De Estado | A Estado | Mail a | Contenido |
|---|---|---|---|
| Solicitud iniciada | Documentación cargada | Académico | "Nueva solicitud lista para analizar. ID: E3-2024-001" |
| Documentación cargada | Análisis preliminar completado | Aspirante | "Tu solicitud está siendo analizada." |
| Análisis preliminar completado | Esperando selección de horario | Aspirante | "Elige un horario para tu reunión. Opciones: [...]" |
| Esperando selección de horario | Reunion confirmada | Ambos | "Reunión confirmada para [fecha/hora]" |
| Reunion realizada | Revisión legal completada | Aspirante | "Continuamos procesando tu solicitud." |
| Aprobado | Enviado a secretaría | Secretaría | "Solicitud aprobada lista para gestión de pago. ID: E3-2024-001" |
| Enviado a secretaría | Cupón generado | Aspirante | "Tu cupón de pago está listo. Monto: $[...]" |
| Esperando verificación de pago | Pago verificado | Aspirante | "Tu pago ha sido verificado correctamente." |
| Pago verificado | Disposición generada | Académico | "Disposición generada. Revisar y confirmar cierre." |
| Disposición generada | Expediente finalizado | Aspirante | "¡Tu trámite ha sido completado! Descarga tu disposición." |
| Documentación rechazada | Documentación cargada | Académico | "Aspirante ha reacargado documentación. ID: E3-2024-001" |
| Rechazado | (final) | Aspirante | "Lamentablemente tu solicitud fue rechazada. Motivo: [...]" |

#### RF-ESTADO-003: Historial Auditable
- El sistema registra para cada cambio:
  - Timestamp exacto (fecha, hora, zona horaria)
  - Usuario que realizó el cambio (email/ID)
  - Estado anterior y nuevo
  - Comentarios asociados
  - IP de origen
- Historial es **inmutable** (no se puede editar, solo agregar)
- **Visible para:**
  - Académico y Secretaría: vista completa con detalles técnicos
  - Aspirante: versión legible sin detalles técnicos

---

### 4.9 Módulo de Limitación de Rondas de Coordinación

#### RF-RONDAS-001: Límite de Dos Rondas Automáticas
- **Flujo:**
  1. Académico propone horarios (Ronda 1)
  2. Aspirante elige o rechaza
  3. Si rechaza → Académico puede proponer nuevos horarios (Ronda 2)
  4. Aspirante elige o rechaza nuevamente
  5. Si rechaza nuevamente → **Se abre canal manual de coordinación**
     - Aspirante puede enviar mensaje directo (RF-MSG-001)
     - Académico y Aspirante coordinan por mail/teléfono

#### RF-RONDAS-002: Transición a Canal Manual
- Después de 2 rondas fallidas, estado permanece **"Esperando selección de horario"** pero:
  - Se deshabilita calendario
  - Se muestra botón prominente: "Contactar académico"
  - Se redirige a sección de mensajes (RF-MSG-001)
  - Aspirante puede escribir mensaje explicando qué horarios le quedan bien
- Académico recibe notificación: "Aspirante necesita coordinación manual para reunión"

---

### 4.10 Módulo de Comunicaciones

#### RF-COM-001: Envío Automático de Mails
- Sistema envía mails automáticos para cada transición (RF-ESTADO-002)
- Template incluye:
  - Asunto claro (ej: "E3-2024-001: Tu solicitud ha sido aprobada")
  - Cuerpo con información relevante
  - Datos de contacto
  - Link al panel (si aplica)
- Email desde: noreply@usal.edu.ar

#### RF-COM-002: Mensajería Directa
- Aspirante puede enviar mensajes al Académico (RF-MSG-001)
- Mensajes NO son automáticos (requieren respuesta manual)
- Notificación por correo al Académico
- El Académico responde por correo (su preferencia)
- Los mensajes se archivan en BD con historial

---

### 4.11 Módulo de Almacenamiento y Gestión de Documentos

#### RF-ARCH-001: Almacenamiento en Base de Datos
- Todos los documentos se guardan en BD con estructura:
  ```
  expedientes/
  ├── YYYY/ [Año de solicitud]
  │   ├── [DNI_ASPIRANTE]/
  │   │   ├── E3-YYYY-XXX/ [ID único de solicitud]
  │   │   │   ├── solicitud_inicial.json
  │   │   │   ├── datos_personales.json
  │   │   │   ├── documentos_aspirante/
  │   │   │   │   ├── analitico.pdf
  │   │   │   │   ├── plan_estudios.pdf
  │   │   │   │   └── programas/ [múltiples]
  │   │   │   ├── analisis_preliminar/
  │   │   │   │   ├── matriz_tentativa.pdf
  │   │   │   │   └── horario_propuesto.pdf
  │   │   │   ├── coordinacion_meet/
  │   │   │   │   ├── propuesta_ronda_1.json
  │   │   │   │   ├── propuesta_ronda_2.json
  │   │   │   │   └── horario_confirmado.json
  │   │   │   ├── revision_legal/
  │   │   │   │   └── observaciones.pdf
  │   │   │   ├── pago/
  │   │   │   │   ├── cupon.pdf
  │   │   │   │   └── comprobante_pago.pdf
  │   │   │   └── disposicion/
  │   │   │       ├── disposicion_decanal.pdf
  │   │   │       └── numero_expediente.txt
  ```

#### RF-ARCH-002: Restricción de Visibilidad Documental
- **Aspirante:** Puede ver/descargar solo:
  - Sus documentos cargados (solicitud, anexos)
  - Matriz tentativa y horario (cuando están disponibles)
  - Disposición final
  - **No puede ver:** Comentarios internos, notas de académico, comprobante de pago

- **Académico (asignado):** Acceso completo a expediente
- **Académico (colegas):** Acceso de lectura (sin permisos de modificación)
- **Secretaría:** Acceso completo a expediente

#### RF-ARCH-003: Exportación de Expedientes
- Aspirante puede descargar su expediente en ZIP al finalizar (RF-SOL-007)
- Secretaría puede exportar:
  - Expediente individual: `E3-2024-001_Expediente_[DNI].zip`
  - Múltiples: `Expedientes_2024_[fecha].zip`
- Cada exportación se registra en auditoría (usuario, timestamp, expedientes descargados)

#### RF-ARCH-004: Respaldo a Largo Plazo
- Todos los expedientes se respaldan en servidor de USAL con:
  - Copia diaria automatizada
  - Retención mínima: **7 años** (para futuras verificaciones de títulos)
  - Disponible para consulta por Secretaría/Directivos
  - Acceso restringido (solo lectura)

#### RF-ARCH-005: Integridad y Auditoría de Documentos
- Cada documento cargado tiene:
  - Hash SHA-256 (para verificar integridad)
  - Fecha de carga exacta
  - Usuario que cargó
  - Nombre original + nombre en sistema
- Registro de eventos inmutable: quién accedió, cuándo, qué hizo

---

## 5. DESCRIPCIÓN DE PANELES POR ESTADO (Resumen)

### Panel ASPIRANTE

**Secciones principales:**
1. **Tu trámite:**
   - Responsable actual (nombre + email)
   - Estado actual
   - Última actualización
   - Link directo al email del académico

2. **Documentación:**
   - Formulario inicial (editable si está en "Solicitud iniciada")
   - Upload de documentos (si estado permite)
   - Recargar documentación (si fue rechazada)

3. **Matriz y horarios:**
   - Matriz tentativa (cuando Académico la carga)
   - Horario propuesto
   - Selección de horario para reunión

4. **Pago:**
   - Información del cupón
   - Upload de comprobante
   - Verificación de pago

5. **Informe final:**
   - Detalle de materias reconocidas y adeudadas
   - Disposición decanal descargable
   - Opción de descargar expediente completo en ZIP

6. **Consultas:**
   - Sección de mensajes directos al académico
   - Historial de conversaciones

7. **Historial:**
   - Timeline de todos los cambios de estado
   - Quién, cuándo, qué cambió

---

### Panel ACADÉMICO

**Secciones principales:**
1. **Solicitudes asignadas:**
   - Listado filtrable por estado
   - Búsqueda por ID/aspirante
   - Botones de acción según estado

2. **Detalles de solicitud:**
   - Información del aspirante
   - Documentación descargable
   - Análisis preliminar (matriz + horario)
   - Calendario para proponer reunión

3. **Coordinación de reunión:**
   - Ver horarios elegidos
   - Confirmar o rechazar
   - Post-meet: notas y decisión

4. **Decisiones:**
   - Botones de Aprobar/Rechazar (con doble verificación)
   - Enviar a Secretaría

5. **Mensajes:**
   - Ver mensajes del aspirante
   - Notificaciones de nuevos mensajes

6. **Expedientes de colegas:**
   - Acceso de lectura a solicitudes de otros académicos
   - Vista completa (sin permisos de edición)

---

### Panel SECRETARÍA ADMINISTRATIVA

**Secciones principales:**
1. **Solicitudes en gestión:**
   - Filtros por estado de pago
   - Búsqueda por ID/aspirante

2. **Gestión de pagos:**
   - Generar cupones
   - Verificar pagos cargados
   - Rechazar o confirmar pagos

3. **Disposiciones:**
   - Confeccionar disposición
   - Ver historial de disposiciones

4. **Exportación:**
   - Descargar expedientes individuales en ZIP
   - Exportación masiva de finalizados
   - Registro de exportaciones

5. **Auditoría:**
   - Ver histórico completo
   - Acceso a logs de sistema

---

## 6. REQUISITOS NO FUNCIONALES

### 6.1 Seguridad

#### RNF-SEG-001: Autenticación
- OAuth 2.0 para @usal.edu.ar
- Contraseña de aspirantes: hashing bcrypt (salt ≥ 10)
- JWT o sesiones seguras (HTTPS obligatorio)
- Validación de email con código (no link desechable)

#### RNF-SEG-002: Autorización RBAC
- Control de acceso por rol implementado en backend
- Registro auditable de cada acción
- Doble verificación para acciones críticas

#### RNF-SEG-003: Cifrado
- Datos personales en BD: cifrados en reposo
- Comunicaciones: HTTPS
- Comprobantes de pago: almacenamiento seguro

#### RNF-SEG-004: Validación
- Inputs validados en backend
- Prevención de inyección SQL, XSS, CSRF
- Validación de file upload: tipo, tamaño, virus scan (opcional)

#### RNF-SEG-005: Auditoría
- Todos los cambios registrados inmutablemente
- Logs del sistema (errores, accesos, cambios)
- Retención: mínimo 7 años
- Cumplimiento de regulaciones de datos

### 6.2 Disponibilidad y Rendimiento

#### RNF-REND-001: Tiempo de respuesta
- Carga de página: < 2 segundos
- Operaciones CRUD: < 500 ms
- Upload: feedback progresivo

#### RNF-REND-002: Disponibilidad
- Uptime objetivo: 99%
- Mantenimiento: máximo 4 horas mensuales
- Notificación anticipada de paros

#### RNF-REND-003: Escalabilidad
- Soportar 10-1000 usuarios simultáneos
- BD indexada para queries frecuentes

#### RNF-REND-004: Almacenamiento
- MVP: 10 GB (expandible)
- Backup automático diario
- Retención: 7 años mínimo

### 6.3 Usabilidad

#### RNF-USA-001: Interfaz
- Responsive (mobile-friendly)
- Terminología clara del dominio
- Iconografía consistente

#### RNF-USA-002: Accesibilidad
- WCAG 2.1 Level A
- Screen reader compatible
- Contraste de colores adecuado

#### RNF-USA-003: Documentación
- Help inline (tooltips)
- FAQ con pasos comunes
- Manual para Académico y Secretaría

### 6.4 Confiabilidad

#### RNF-CONF-001: Integridad
- Transacciones atómicas
- Validación de constraints en BD
- Backups regulares (diario, retenidos 7 años)

#### RNF-CONF-002: Recuperación
- Reinicio automático de servicios
- Logs detallados
- Plan de rollback

#### RNF-CONF-003: Manejo de errores
- Mensajes informativos (sin stack traces)
- Página de error amigable
- Notificación a admin en error crítico

### 6.5 Mantenibilidad

#### RNF-MANT-001: Código
- Estándares consistentes
- Comentarios en código complejo
- Git con commits descriptivos

#### RNF-MANT-002: Documentación técnica
- Diagrama de arquitectura
- Endpoints API documentados
- Instrucciones de despliegue

#### RNF-MANT-003: Monitoreo
- Logs estructurados (JSON)
- Métricas: requests/sec, errores, latencia promedio

---

## 7. PARTICIPANTES Y ROLES

### Implementación del Sistema
- **Equipo de Desarrollo:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]
- **Profesor Supervisor:** [Nombre]
- **Cliente/Requisitante:** Christian LOPEZ PASARON (Académico)

### Usuarios del Sistema
- **Académicos:** Christian LOPEZ PASARON (Informática), Sebastián (Industriales), [otros]
- **Secretaría Administrativa:** Jimena Genoves
- **Aspirantes:** Estudiantes ingresantes que soliciten equivalencias

---

## 8. NOTAS IMPORTANTES

1. **Los mails de cambio de estado son SOLO notificaciones**, no canal de comunicación principal
2. **Los mensajes de aspirante → académico NO son automáticos**, requieren respuesta manual
3. **Doble verificación requerida** en aprobar, rechazar, cambios de asignación
4. **Almacenamiento por año/DNI** en BD, no en Drive
5. **Máximo 2 rondas automáticas** de propuesta de horarios, luego coordinación manual
6. **Auditoría completa** de todo cambio, acceso, exportación
7. **Respaldo a 7 años** mínimo para verificaciones de títulos futuras
8. **Aspirante ve responsable actual** del trámite en todo momento

---

**Versión:** 3.0  
**Fecha:** Septiembre 2024  
**Autores:** Lautaro Gomizelj, [Compañero 2], [Compañero 3]  
**Revisión:** Con profesor y stakeholders antes de iniciar desarrollo  
**Última actualización:** Post-reunión con Christian LOPEZ PASARON y equipo
