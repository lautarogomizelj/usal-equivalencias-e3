# Sistema de Tramitación de Equivalencias Electrónicas (E3)
## Especificación de Requisitos Funcionales y No Funcionales

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
- El almacenamiento es local en los servidores de la facultad

---

## 2. OBJETIVO DEL SISTEMA

Proporcionar un flujo de tramitación de equivalencias **trazable, automatizado y descentralizado**, donde:
1. El aspirante inicia el trámite cargando documentación sin necesidad de contacto previo
2. Cada rol ve un panel con acciones concretas según su responsabilidad
3. El proceso avanza mediante transiciones explícitas sin depender de terceros recordando enviar mails
4. Hay registro auditable de cada acción, cambio de estado y timestamp

---

## 3. ACTORES DEL SISTEMA

### 3.1 Aspirante
- **Perfil:** Estudiante de otra universidad solicitando equivalencias para ingresar a carrera de Ingeniería
- **Método de acceso:** Crear cuenta en el sistema (email + contraseña)
- **Acciones principales:** Cargar documentación, ver estado, recargar documentos si se solicita, efectuar pago, visualizar informe de equivalencias

### 3.2 Académico
- **Perfil:** Personal de área académica (usualmente profesor coordinador)
- **Método de acceso:** OAuth con email @usal.edu.ar (Google)
- **Acciones principales:** Realizar análisis preliminar, revisar documentación legal, tomar decisiones de aprobación/rechazo, informar resultados, enviar a secretaría

### 3.3 Secretaría Administrativa
- **Perfil:** Personal de administración/secretaría académica
- **Método de acceso:** OAuth con email @usal.edu.ar (Google)
- **Acciones principales:** Gestionar cupones de pago, verificar pagos, confeccionar disposiciones decanales, archivar expedientes

---

## 4. REQUISITOS FUNCIONALES

### 4.1 Módulo de Autenticación

#### RF-AUTH-001: Registro de Aspirantes
- El aspirante puede crear una nueva cuenta indicando:
  - Email personal
  - Contraseña (mínimo 8 caracteres, mayúscula, número, carácter especial)
  - Nombre completo
  - Teléfono de contacto
  - Número de documento
- El sistema envía un mail de confirmación con enlace de validación
- No puede iniciar trámite hasta que valide el email

#### RF-AUTH-002: Login de Aspirantes
- Acceso mediante email + contraseña
- Recupero de contraseña mediante email
- Sesión con expiración (30 días con "recuérdame")

#### RF-AUTH-003: Login de Académicos y Secretaría Administrativa
- OAuth 2.0 con Google, restringido a dominios @usal.edu.ar
- Si el email no pertenece al dominio, el login es rechazado
- Sesión automática (vinculada a sesión de Google)
- Los roles se asignan automáticamente en la BD según grupo USAL

### 4.2 Módulo de Gestión de Solicitudes (Aspirante)

#### RF-SOL-001: Crear Nueva Solicitud de Equivalencias
- El aspirante accede a "Iniciar Trámite" y completa un formulario con:
  - Carrera solicitada (dropdown con carreras disponibles)
  - Universidad de origen
  - Datos personales (validados/pre-rellenados desde el perfil)
  - Descripción breve de la solicitud
- Al guardar, el estado pasa a **"Solicitud iniciada"** y se genera un ID único
- Se envía mail automático al Académico indicando: "Nueva solicitud de equivalencias. ID: [...]"

#### RF-SOL-002: Cargar Documentación Inicial
- Dentro de la solicitud, el aspirante puede cargar:
  - Analítico parcial (PDF)
  - Plan de estudios con carga horaria (PDF)
  - Programas de materias aprobadas (PDF, múltiples archivos)
  - Cualquier documento adicional relevante
- Sistema valida:
  - Que sea PDF o JPG
  - Tamaño máximo 10 MB por archivo
  - Tamaño total no supere 50 MB
- Al cargar documentación, aspirante puede cambiar estado a **"Documentación cargada"**
- Se envía mail automático al Académico: "Documentación lista para analizar. ID: [...]"

#### RF-SOL-003: Ver Estado de Solicitud
- Panel del Aspirante muestra:
  - Estado actual con descripción legible
  - Timeline de cambios de estado (quién, cuándo, qué cambió)
  - Documentación cargada (con fecha de carga, posibilidad de descargar)
  - Mensajes/comentarios del Académico si aplica
  - Botones de acción disponibles según estado actual

#### RF-SOL-004: Recargar Documentación
- Si el Académico rechaza la documentación, el estado es **"Documentación rechazada"**
- El panel del Aspirante muestra:
  - Motivo del rechazo (comentario del Académico)
  - Opción de recargar documentos
- Al recargar, el aspirante puede cambiar el estado nuevamente a **"Documentación cargada"**
- Se notifica al Académico para revisión

#### RF-SOL-005: Ver Informe de Materias Equivalentes
- Después de que el Académico aprueba la solicitud, el aspirante ve un panel con:
  - Resumen de decisión (aprobado, cantidad de materias reconocidas)
  - Tabla/listado de materias equivalentes reconocidas (nombre, carga horaria)
  - Cantidad de materias a adeudar
  - Observaciones del Académico
  - Monto total del arancel/cupón a pagar
- Este panel es de **solo lectura** para el aspirante

#### RF-SOL-006: Efectuar Pago
- Una vez visto el informe, el aspirante puede proceder al pago
- El panel del Aspirante muestra:
  - Monto del arancel/cupón a pagar
  - Opción de descargar cupón generado por Secretaría
  - Área para cargar comprobante de pago (imagen)
- Después de cargar comprobante, estado es **"Esperando verificación de pago"**
- Se notifica a Secretaría Administrativa

### 4.3 Módulo de Panel Académico

#### RF-ACAD-001: Ver Solicitudes Pendientes
- El Académico ve un listado con filtros posibles:
  - Todas las solicitudes
  - Solo solicitudes por estado (ej: "En documentación cargada", "En análisis preliminar", etc.)
  - Búsqueda por ID o nombre de aspirante
- Para cada solicitud muestra:
  - ID y nombre del aspirante
  - Estado actual
  - Fecha de última actualización
  - Documentación disponible (indicador)
  - Botones de acción según estado

#### RF-ACAD-002: Análisis Preliminar
- **Acciona:** Desde estado **"Documentación cargada"**
- El Académico accede a vista detallada:
  - Puede descargar toda la documentación del aspirante
  - Realiza análisis comparativo: plan del aspirante vs. plan de carrera
  - Genera una **matriz tentativa de equivalencias** (qué materias se reconocen, cuáles se adeudan)
  - Arma un **horario propuesto** de cursada
- El sistema permite cargar archivo adjunto (matriz en PDF/Excel) o dejar comentarios
- **Transición de estado:** Cuando completa, cambia a **"Análisis preliminar completado"**
- Se envía mail al Aspirante: "Tu solicitud está en análisis. Pronto tendrás respuesta."

#### RF-ACAD-003: Revisión Legal
- **Acciona:** Desde estado **"Análisis preliminar completado"**
- El Académico verifica:
  - Validez del documento (firmas electrónicas legales)
  - Concordancia entre materias y programas vs. análisis preliminar
- Puede cargar un documento adjunto con observaciones legales
- Decide si hay conformidad o hay observaciones
- **Transición de estado:**
  - Si OK → **"Revisión legal completada"**
  - Si hay observaciones → **"Documentación rechazada"** (vuelve al aspirante)
- Si va a rechazo, se envía mail al Aspirante explicando

#### RF-ACAD-004: Decisión Final (Aprobación/Rechazo)
- **Acciona:** Desde estado **"Revisión legal completada"**
- El Académico toma decisión final:
  - **Aprobar:** Genera resumen de equivalencias (cantidad de materias reconocidas, a adeudar, observaciones)
    - Estado → **"Aprobado"**
    - Se envía mail al Aspirante con resumen
  - **Rechazar:** Incluye motivo en comentario
    - Estado → **"Rechazado"**
    - Se envía mail explicando

#### RF-ACAD-005: Informar Cantidad de Materias
- **Acciona:** Cuando estado es **"Aprobado"**
- El Académico registra en el sistema:
  - Cantidad exacta de materias equivalentes reconocidas
  - Cantidad de materias a adeudar
  - Observaciones relevantes (ej: necesita nivelación en X)
  - Archivo con la matriz de equivalencias final
- Este resumen quedará visible en el panel del Aspirante

#### RF-ACAD-006: Envío a Secretaría Administrativa
- **Acciona:** Cuando estado es **"Aprobado"** con información de materias completa
- El Académico puede cambiar estado a **"Enviado a secretaría"**
- Se envía mail a Secretaría Administrativa indicando: "Solicitud aprobada lista para gestión de pago. ID: [...]"
- La solicitud ahora aparece en el listado de Secretaría

#### RF-ACAD-007: Cierre de Expediente
- **Acciona:** Cuando estado es **"Disposición generada"**
- El Académico recibe la disposición decanal de Secretaría
- Verifica que esté correcta y puede cambiar estado a **"Expediente finalizado"**
- Se envía mail al Aspirante: "Tu trámite de equivalencias ha sido completado."

### 4.4 Módulo de Panel Secretaría Administrativa

#### RF-SEC-001: Ver Solicitudes en Espera de Gestión
- La Secretaría ve un listado con:
  - Solicitudes en estado **"Enviado a secretaría"** (listas para gestionar pago)
  - Solicitudes en estado **"Esperando verificación de pago"** (con comprobante cargado)
  - Solicitudes en estado **"Disposición generada"** (listas para entrega)
- Para cada solicitud: ID, aspirante, monto a pagar, fecha de aprobación

#### RF-SEC-002: Generar Cupón de Pago
- **Acciona:** Desde estado **"Enviado a secretaría"**
- La Secretaría genera un cupón de pago con:
  - Monto específico (definido según política de la facultad)
  - ID de solicitud
  - Datos del aspirante
  - Referencia clara (ej: "Arancel equivalencias - E3-2024-001")
  - Banco/cuenta donde pagar
- El cupón se guarda en la BD y se puede descargar/imprimir
- Estado cambia a **"Cupón generado"**
- Se envía mail al Aspirante con instrucciones: cupón adjunto, cómo pagar, referencia

#### RF-SEC-003: Verificar y Registrar Pago
- **Acciona:** Desde estado **"Esperando verificación de pago"** (aspirante cargó comprobante)
- La Secretaría accede a la vista de pago:
  - Ve el comprobante de pago cargado por el aspirante
  - Verifica que el monto sea correcto
  - Puede dejar un comentario si hay observación (ej: "Monto no coincide, falta $X")
- **Transición:**
  - Si correcto → **"Pago verificado"**
  - Si hay problema → **"Pago rechazado"** (el aspirante debe reenviar)
- En ambos casos se notifica al Aspirante

#### RF-SEC-004: Confeccionar Disposición Decanal
- **Acciona:** Desde estado **"Pago verificado"**
- La Secretaría confecciona la Disposición Decanal:
  - Puede usar una plantilla del sistema o cargar un documento PDF generado externamente
  - La disposición formaliza las equivalencias otorgadas
  - Debe incluir: nombre aspirante, carrera, materias reconocidas, observaciones
- Carga el documento en el sistema
- Estado cambia a **"Disposición generada"**
- Se envía mail al Académico: "Disposición generada. Revisar y confirmar cierre."

#### RF-SEC-005: Archivar y Finalizar Expediente
- **Acciona:** Cuando Académico ha revisado y estado es **"Expediente finalizado"**
- La Secretaría realiza cierre administrativo:
  - Asigna número de expediente final (ej: "EXP-2024-001-E3")
  - Archiva copia de la disposición en el sistema
  - Puede generar un comprobante/resumen para enviar al aspirante
- El trámite está completamente cerrado

### 4.5 Módulo de Gestión de Estados y Flujo

#### RF-ESTADO-001: Transiciones de Estado Completas
El sistema mantiene el siguiente conjunto de estados y transiciones posibles:

**Estados principales:**

1. **Solicitud iniciada**
   - Aspirante acaba de crear la solicitud, no ha cargado documentación
   - Transición → "Documentación cargada" (cuando Aspirante carga documentos)

2. **Documentación cargada**
   - Toda la documentación inicial está en el sistema
   - Transición → "Análisis preliminar completado" (cuando Académico finaliza análisis)
   - Transición → "Documentación rechazada" (si Académico requiere correcciones)

3. **Análisis preliminar completado**
   - El Académico armó la matriz tentativa y horario
   - Transición → "Revisión legal completada" (cuando Académico completa revisión legal)
   - Transición → "Documentación rechazada" (si hay observaciones legales)

4. **Revisión legal completada**
   - Documentación legal verificada sin observaciones
   - Transición → "Aprobado" (cuando Académico aprueba)
   - Transición → "Rechazado" (cuando Académico rechaza)

5. **Aprobado**
   - Solicitud aprobada, esperando envío a secretaría
   - Transición → "Enviado a secretaría" (cuando Académico gestiona envío)

6. **Enviado a secretaría**
   - Secretaría administrará el pago
   - Transición → "Cupón generado" (cuando Secretaría genera cupón)

7. **Cupón generado**
   - Cupón de pago disponible, esperando pago de aspirante
   - Transición → "Esperando verificación de pago" (cuando Aspirante carga comprobante)

8. **Esperando verificación de pago**
   - Comprobante cargado, Secretaría verifica
   - Transición → "Pago verificado" (si pago es correcto)
   - Transición → "Pago rechazado" (si hay problema)

9. **Pago verificado**
   - Pago confirmado, listo para disposición
   - Transición → "Disposición generada" (cuando Secretaría crea disposición)

10. **Pago rechazado**
    - Pago tiene problemas (monto incorrecto, etc.)
    - Transición → "Esperando verificación de pago" (cuando Aspirante reenvía comprobante)

11. **Disposición generada**
    - Disposición decanal creada, enviada a Académico para revisión
    - Transición → "Expediente finalizado" (cuando Académico verifica y cierra)

12. **Expediente finalizado**
    - Trámite completamente cerrado
    - **Estado terminal**

13. **Documentación rechazada**
    - Hay observaciones (documentación falta o error legal)
    - Transición → "Documentación cargada" (cuando Aspirante recarga documentos)

14. **Rechazado**
    - Solicitud rechazada, no hay equivalencias
    - **Estado terminal**

**Diagrama de transiciones:**
```
Solicitud iniciada
    ↓
Documentación cargada ←→ Documentación rechazada (Aspirante recarga)
    ↓
Análisis preliminar completado
    ↓
Revisión legal completada
    ↓
├→ Aprobado
│    ↓
│  Enviado a secretaría
│    ↓
│  Cupón generado
│    ↓
│  Esperando verificación de pago
│    ├→ Pago verificado
│    │    ↓
│    │  Disposición generada
│    │    ↓
│    │  Expediente finalizado ✓
│    │
│    └→ Pago rechazado ↻ (vuelve a "Esperando verificación")
│
└→ Rechazado ✓
```

#### RF-ESTADO-002: Notificaciones por Cambio de Estado
Cuando el estado cambia, el sistema envía mail automático al actor siguiente relevante:

| De Estado | A Estado | Mail a | Asunto/Contenido |
|---|---|---|---|
| Solicitud iniciada | Documentación cargada | Académico | "Nueva solicitud de equivalencias lista para analizar. ID: [...]" |
| Documentación cargada | Análisis preliminar completado | Aspirante | "Tu solicitud está siendo analizada por nuestro equipo académico." |
| Análisis preliminar completado | Revisión legal completada | (interno) | - |
| Revisión legal completada | Aprobado | Aspirante | "Tu solicitud ha sido aprobada. Aquí está el detalle de equivalencias: [matriz]" |
| Revisión legal completada | Rechazado | Aspirante | "Lamentablemente tu solicitud no pudo ser aprobada. Motivo: [...]" |
| Análisis preliminar completado | Documentación rechazada | Aspirante | "Observaciones en tu documentación. Motivo: [comentario del Académico]" |
| Documentación rechazada | Documentación cargada | Académico | "Aspirante ha reacargado documentación. ID: [...]" |
| Aprobado | Enviado a secretaría | Secretaría | "Solicitud aprobada lista para gestión de pago. ID: [...]" |
| Enviado a secretaría | Cupón generado | Aspirante | "Tu cupón de pago está listo. Descargalo aquí: [link]. Monto: $[...]" |
| Cupón generado | Esperando verificación de pago | (interno) | - |
| Esperando verificación de pago | Pago verificado | Aspirante | "Tu pago ha sido verificado correctamente. Gracias." |
| Esperando verificación de pago | Pago rechazado | Aspirante | "Hay un problema con tu comprobante. Detalle: [comentario de Secretaría]. Por favor reenvía." |
| Pago verificado | Disposición generada | Académico | "Disposición decanal generada. Revisar y confirmar cierre. ID: [...]" |
| Disposición generada | Expediente finalizado | Aspirante | "¡Tu trámite de equivalencias ha sido completado! Accede a tu cuenta para descargar la disposición." |

#### RF-ESTADO-003: Historial Auditable
- El sistema registra para cada cambio de estado:
  - Timestamp (fecha y hora exacta, zona horaria)
  - Usuario que realizó el cambio (email/ID)
  - Estado anterior y nuevo
  - Comentarios/notas asociadas
  - IP de origen (para seguridad)
- Este historial es:
  - **Visible para Académico y Secretaría:** vista completa con detalles técnicos
  - **Visible para Aspirante:** versión legible sin detalles técnicos (ej: "El 2024-09-15 a las 14:30 tu solicitud pasó a Análisis preliminar completado")
- El historial es **inmutable** (no se puede editar, solo agregar nuevas entradas)

### 4.6 Módulo de Comunicaciones

#### RF-COM-001: Envío Automático de Mails
- Sistema envía mails automáticos para cada transición de estado especificada en RF-ESTADO-002
- Template de mail incluye:
  - Asunto claro y descriptivo (ej: "E3-2024-001: Tu solicitud ha sido aprobada")
  - Cuerpo con información relevante del nuevo estado
  - Datos de contacto de la facultad (teléfono, email)
  - Link directo al panel si es relevante (para Académico y Secretaría)
- Los mails son enviados desde dirección de la facultad (noreply@usal.edu.ar)

#### RF-COM-002: Comentarios/Mensajes (Estados específicos)
- **Permitido SOLO en estado "Documentación rechazada"**
- Cuando el Académico cambia a este estado, debe incluir un comentario explicando:
  - Qué falta o está mal en la documentación
  - Qué debe hacer el Aspirante para corregir
  - Plazo para reenvío (si aplica)
- El Aspirante ve el comentario prominentemente en su panel
- El Aspirante puede responder con un comentario antes de recargar la documentación
- Los comentarios quedan en el historial auditable
- **En otros estados NO hay sección de comentarios** para evitar dispersión

### 4.7 Módulo de Análisis Preliminar (Subproceso - RF-ACAD-002)

#### RF-ANALI-001: Componentes del Análisis Preliminar
El Análico Preliminar (ejecutado por Académico, estado "Documentación cargada") incluye:

1. **Armado de Matriz Tentativa de Equivalencias**
   - Académico compara plan de estudios del aspirante vs. plan de la carrera USAL
   - Identifica qué materias pueden equivalerse (por contenidos similares)
   - Identifica qué materias el aspirante debe adeudar (no tiene equivalente)
   - Matriz tentativa se carga como archivo (PDF/Excel) en el sistema

2. **Armado de Horario Propuesto**
   - Basado en la matriz tentativa, Académico arma una propuesta de horario de cursada
   - Considera: materias equivalentes (no cursa), materias a adeudar (sí cursa), orden de prerrequisitos
   - Horario se carga como documento adjunto

3. **Presentación en Reunión Virtual (Meet)**
   - Se realiza una reunión entre Académico y Aspirante (vía Google Meet, Zoom, etc.)
   - **Nota:** Esta reunión se coordina fuera del sistema (por mail, WhatsApp, etc.)
   - En la reunión se presenta y explica la matriz y horario propuesto
   - Se recojen observaciones/preguntas del aspirante

4. **Almacenamiento**
   - Toda la documentación de análisis preliminar se guarda en:
     `/equivalencias/YYYY/EXP-ID/analisis_preliminar/`
   - Estructura: `matriz_tentativa.pdf`, `horario_propuesto.pdf`, `notas_reunion.txt`

#### RF-ANALI-002: Decisión Post-Análisis
- Después del análisis preliminar y reunión, el Académico decide:
  - **Continuar con revisión legal:** Estado → "Análisis preliminar completado"
  - **Pedir correcciones:** Si documentación está incompleta o hay dudas
    - Estado → "Documentación rechazada"
    - Comentario explicando qué necesita

### 4.8 Módulo de Revisión Legal (Subproceso - RF-ACAD-003)

#### RF-LEGAL-001: Componentes de Revisión Legal
La Revisión Legal (ejecutada por Académico, estado "Análisis preliminar completado") incluye:

1. **Verificar Validez del Documento**
   - Comprobar que el documento (analítico) tenga firmas electrónicas válidas
   - Verificar que sea documento oficial de la universidad de origen
   - Validar fecha de emisión (no muy antiguo, generalmente últimos 2 años)

2. **Verificar Materias y Programas vs. Análisis Preliminar**
   - Comparar las materias presentadas en documentación vs. la matriz tentativa armada
   - Validar que los programas de materias sean consistentes
   - Asegurar que no haya discrepancias entre lo analizado y lo documentado

3. **Generar Observaciones (si aplica)**
   - Si hay problemas (documento inválido, inconsistencias, etc.), genera un documento con observaciones
   - Puede pedir correcciones específicas

#### RF-LEGAL-002: Resultado de Revisión Legal
- **Si OK (sin observaciones):**
  - Estado → "Revisión legal completada"
  - Pasa a decisión final de Académico (aprobar o rechazar)

- **Si hay problemas:**
  - Estado → "Documentación rechazada"
  - Comentario explicando qué falta o qué está mal
  - El Aspirante debe corregir y recargar documentación

### 4.9 Módulo de Gestión de Archivos

#### RF-ARCH-001: Almacenamiento de Documentos
- Todos los PDFs/archivos cargados se guardan en el sistema de archivos local del servidor
- Estructura de carpetas:
  ```
  /equivalencias/
    ├── YYYY/
    │   ├── EXP-ID/
    │   │   ├── solicitud_original.json
    │   │   ├── datos_aspirante.json
    │   │   ├── documentos_aspirante/
    │   │   │   ├── analitico.pdf
    │   │   │   ├── plan_estudios.pdf
    │   │   │   └── programas/
    │   │   ├── analisis_preliminar/
    │   │   │   ├── matriz_tentativa.pdf
    │   │   │   └── horario_propuesto.pdf
    │   │   ├── revision_legal/
    │   │   │   └── observaciones.pdf
    │   │   ├── pago/
    │   │   │   ├── cupon.pdf
    │   │   │   └── comprobante_pago.pdf
    │   │   └── disposicion/
    │   │       └── disposicion_decanal.pdf
  ```
- Cada archivo tiene metadata en BD: nombre original, fecha de carga, tamaño, hash SHA-256

#### RF-ARCH-002: Descargar Documentación
- **Académico y Secretaría:** pueden descargar cualquier documento asociado a una solicitud
- **Aspirante:** puede descargar
  - Sus propios documentos cargados inicialmente
  - Matriz de equivalencias (cuando Académico la genera)
  - Disposición final (cuando está lista)
  - Comprobante de pago (después de ser verificado)

---

## 5. DESCRIPCIÓN DE PANELES POR ESTADO

### Panel ASPIRANTE según Estado Actual

#### Estado: "Solicitud iniciada"
- **Qué ve:**
  - Mensaje: "Tu solicitud ha sido creada. Por favor carga la documentación requerida."
  - Formulario para cargar: Analítico, Plan de estudios, Programas
  - Botón: "Documentación lista para enviar"
- **Qué puede hacer:**
  - Cargar archivos
  - Ver historial de cambios (cuando se creó)
  - Contactar facultad (email, teléfono)

#### Estado: "Documentación cargada"
- **Qué ve:**
  - Mensaje: "Tu documentación fue recibida. El equipo académico la está analizando."
  - Documentos cargados (lista con fecha)
  - Barra de progreso indicativa del proceso
- **Qué puede hacer:**
  - Descargar sus propios documentos
  - Ver historial
  - Esperar respuesta

#### Estado: "Análisis preliminar completado"
- **Qué ve:**
  - Mensaje: "Tu solicitud está en análisis legal. Pronto tendrás respuesta."
  - Matriz tentativa cargada por Académico (descargable)
  - Horario propuesto (descargable)
- **Qué puede hacer:**
  - Descargar documentos de análisis
  - Ver historial
  - Prepararse para próximo paso

#### Estado: "Documentación rechazada"
- **Qué ve:**
  - **MENSAJE DESTACADO EN ROJO:** "Necesitamos que corrijas algunos documentos"
  - Comentario específico del Académico explicando qué falta
  - Zona de carga de documentos con botón "Recargar documentos"
  - Opción de dejar comentario/pregunta
- **Qué puede hacer:**
  - Leer comentario detallado
  - Hacer preguntas (comentario)
  - Recargar los documentos corregidos
  - Ver historial

#### Estado: "Aprobado"
- **Qué ve:**
  - **MENSAJE DESTACADO EN VERDE:** "¡Felicitaciones! Tu solicitud ha sido APROBADA"
  - Informe detallado de equivalencias:
    - Tabla con materias reconocidas (nombre, carga horaria)
    - Cantidad de materias a adeudar
    - Observaciones del Académico
  - Monto total del arancel a pagar
  - Botón: "Proceder al pago"
- **Qué puede hacer:**
  - Ver informe (solo lectura)
  - Descargar informe en PDF
  - Descargar matriz de equivalencias
  - Proceder a pagar

#### Estado: "Cupón generado"
- **Qué ve:**
  - Mensaje: "Tu cupón de pago está listo"
  - Cupón descargable con:
    - Monto exacto a pagar
    - Referencia (ej: "E3-2024-001")
    - Banco, cuenta, instrucciones de pago
  - Botón: "Descargar cupón"
  - Área para subir comprobante de pago
  - Instrucciones claras
- **Qué puede hacer:**
  - Descargar cupón
  - Ver instrucciones de pago
  - Cargar comprobante de pago

#### Estado: "Esperando verificación de pago"
- **Qué ve:**
  - Mensaje: "Tu comprobante de pago fue recibido. Estamos verificando..."
  - Comprobante cargado (visible, con fecha)
  - Barra de progreso
  - Información del pago (monto, comprobante)
- **Qué puede hacer:**
  - Descargar comprobante cargado
  - Esperar verificación
  - Si Secretaría rechaza, ver comentario y recargar

#### Estado: "Pago rechazado"
- **Qué ve:**
  - **MENSAJE EN NARANJA:** "Hay un problema con tu comprobante"
  - Comentario de Secretaría (ej: "El monto no coincide. Debe ser $X")
  - Opción de recargar comprobante
- **Qué puede hacer:**
  - Leer problema específico
  - Recargar comprobante correcto
  - Contactar con Secretaría si hay dudas

#### Estado: "Pago verificado"
- **Qué ve:**
  - **MENSAJE EN VERDE:** "Tu pago ha sido verificado correctamente"
  - Resumen del pago verificado
  - Barra de progreso (próximo paso: disposición)
  - Mensaje: "Se está generando tu disposición decanal. Pronto recibirás confirmación."
- **Qué puede hacer:**
  - Ver resumen
  - Esperar disposición

#### Estado: "Disposición generada"
- **Qué ve:**
  - **MENSAJE EN VERDE:** "Tu trámite de equivalencias ha sido completado"
  - Disposición decanal descargable (PDF)
  - Resumen final: materias reconocidas, a adeudar, datos importantes
  - Número de expediente final (ej: "EXP-2024-001-E3")
- **Qué puede hacer:**
  - Descargar disposición decanal
  - Descargar resumen completo del trámite
  - Ver historial completo
  - Imprimir documentos

#### Estado: "Rechazado"
- **Qué ver:**
  - **MENSAJE EN ROJO:** "Lamentablemente tu solicitud fue RECHAZADA"
  - Motivo específico del rechazo
  - Datos de contacto para aclarar dudas
- **Qué puede hacer:**
  - Contactar académico para preguntas
  - Ver historial completo del trámite

---

### Panel ACADÉMICO según Estado Actual

#### Listado General de Solicitudes
- Tabla con todas las solicitudes filtradas por estado
- Columnas: ID, Aspirante, Universidad de origen, Estado actual, Última actualización
- Búsqueda por ID o nombre
- Filtros por estado

#### Estado: "Solicitud iniciada" (En panel)
- **Qué ve:**
  - ID de solicitud, nombre aspirante, universidad de origen
  - Estado: "Solicitud iniciada - Esperando documentación"
  - Fecha de creación
  - Botón: "Ver detalles"
- **Qué puede hacer:**
  - Ver detalles de solicitud
  - Esperar a que aspirante cargue documentación

#### Estado: "Documentación cargada" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Documentación cargada - Listo para análisis"
  - Fecha de carga de documentos
  - Botón: "Realizar análisis preliminar"
- **Qué puede hacer:**
  - Abrir vista detallada
  - Descargar todos los documentos del aspirante
  - Iniciar análisis preliminar

#### Al hacer "Realizar Análisis Preliminar"
- **Vista ampliada muestra:**
  - Todos los documentos descargables (analítico, plan, programas)
  - Área para cargar matriz tentativa (PDF/Excel)
  - Área para cargar horario propuesto
  - Editor de comentarios
  - Botón: "Análisis preliminar completado"
- **Qué puede hacer:**
  - Descargar y revisar documentos
  - Armar matriz tentativa en su herramienta (Excel, Word)
  - Armar horario propuesto
  - Cargar archivos en el sistema
  - Dejar comentarios
  - Cambiar estado a "Análisis preliminar completado"
  - O rechazar documentación si falta algo

#### Estado: "Análisis preliminar completado" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Análisis preliminar completado - Listo para revisión legal"
  - Matriz y horario cargados (descargables)
  - Botón: "Realizar revisión legal"
- **Qué puede hacer:**
  - Ver matriz y horario
  - Abrir vista de revisión legal

#### Al hacer "Realizar Revisión Legal"
- **Vista muestra:**
  - Documentación del aspirante (para verificar validez)
  - Matriz tentativa (para comparar)
  - Editor para cargar observaciones legales (si aplica)
  - Botón: "Revisión legal completada"
- **Qué puede hacer:**
  - Verificar validez de firmas/documentos
  - Comparar materias vs. matriz
  - Si hay problemas: rechazar documentación (con comentario)
  - Si está OK: pasar a "Revisión legal completada"

#### Estado: "Revisión legal completada" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Revisión legal OK - Listo para decisión"
  - Matriz y horario (descargables)
  - Botón: "Aprobar solicitud"
  - Botón: "Rechazar solicitud"
- **Qué puede hacer:**
  - Tomar decisión final
  - Cambiar a "Aprobado" o "Rechazado"

#### Estado: "Aprobado" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Aprobado - Esperando envío a secretaría"
  - Informe de equivalencias (descargable)
  - Botón: "Generar informe de materias"
  - Botón: "Enviar a secretaría"
- **Qué puede hacer:**
  - Revisar informe de equivalencias
  - Generar/confirmar informe de materias (cantidad reconocidas, a adeudar)
  - Enviar a secretaría

#### Al hacer "Enviar a secretaría"
- **Acción:** Estado cambia a "Enviado a secretaría"
- **Resultado:** Se envía mail a Secretaría
- **Panel actualiza:** Botón cambia a "Ver detalles" (ya no se puede modificar)

#### Estado: "Disposición generada" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Disposición generada - Pendiente cierre"
  - Disposición cargada por Secretaría (descargable)
  - Botón: "Revisar y cerrar"
- **Qué puede hacer:**
  - Descargar disposición
  - Revisar que esté correcta
  - Cerrar expediente

#### Estado: "Expediente finalizado" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Expediente finalizado ✓"
  - Número de expediente final asignado
  - Todos los documentos archivados (descargables)
- **Qué puede hacer:**
  - Ver historial completo
  - Descargar cualquier documento del trámite
  - Imprimir para archivo físico

---

### Panel SECRETARÍA ADMINISTRATIVA según Estado Actual

#### Listado General de Solicitudes
- Tabla con solicitudes en espera de gestión administrativa
- Columnas: ID, Aspirante, Estado, Monto a pagar, Última actualización
- Filtros por estado

#### Estado: "Enviado a secretaría" (En panel)
- **Qué ve:**
  - ID, aspirante, universidad
  - Estado: "Enviado a secretaría - Listo para cupón"
  - Monto aprobado por Académico
  - Documentación de aprobación (descargable)
  - Botón: "Generar cupón de pago"
- **Qué puede hacer:**
  - Ver detalles de aprobación
  - Generar cupón de pago

#### Al hacer "Generar Cupón"
- **Vista muestra:**
  - Formulario con datos del cupón:
    - Monto a pagar (pre-rellenado)
    - Referencia (auto-generada o editable: "E3-2024-001")
    - Banco/cuenta donde pagar
    - Fecha de vencimiento (si aplica)
  - Botón: "Generar y enviar"
- **Qué puede hacer:**
  - Validar datos
  - Generar cupón (PDF)
  - El sistema automáticamente:
    - Cambia estado a "Cupón generado"
    - Envía mail al Aspirante con cupón

#### Estado: "Cupón generado" (En panel)
- **Qué ve:**
  - ID, aspirante
  - Estado: "Cupón generado - Esperando pago"
  - Cupón cargado (descargable)
  - Monto a pagar
  - Fecha de generación
- **Qué puede hacer:**
  - Ver/descargar cupón
  - Esperar a que Aspirante cargue comprobante

#### Estado: "Esperando verificación de pago" (En panel)
- **Qué ve:**
  - ID, aspirante
  - Estado: "Esperando verificación de pago"
  - Comprobante de pago cargado por Aspirante (descargable, imagen)
  - Monto reportado en comprobante
  - Botón: "Verificar pago"
- **Qué puede hacer:**
  - Descargar comprobante
  - Revisar visualmente que monto coincida
  - Verificar o rechazar

#### Al hacer "Verificar pago"
- **Vista muestra:**
  - Comprobante visible
  - Checkbox: "El monto es correcto"
  - Área de comentarios (si hay problema)
  - Botón: "Pago correcto - Pasar a disposición"
  - O Botón: "Pago incorrecto - Rechazar"
- **Si correcto:**
  - Estado → "Pago verificado"
  - Se envía mail al Aspirante confirmando
- **Si incorrecto:**
  - Estado → "Pago rechazado"
  - Incluye comentario (ej: "Monto debe ser $2500, recibimos $2000")
  - Se envía mail al Aspirante pidiendo reenvío

#### Estado: "Pago verificado" (En panel)
- **Qué ve:**
  - ID, aspirante
  - Estado: "Pago verificado - Listo para disposición"
  - Datos del pago verificado
  - Botón: "Generar disposición decanal"
- **Qué puede hacer:**
  - Generar disposición

#### Al hacer "Generar Disposición"
- **Vista muestra:**
  - Información del aspirante
  - Carrera y materias reconocidas (pre-rellenado desde análisis del Académico)
  - Opción: cargar disposición generada externamente (PDF) O usar plantilla del sistema
  - Botón: "Generar y enviar"
- **Qué puede hacer:**
  - Subir PDF de disposición (generado en Word/formulario oficial)
  - O usar plantilla del sistema
  - Cambiar estado a "Disposición generada"
  - Sistema envía mail al Académico

#### Estado: "Disposición generada" (En panel)
- **Qué ve:**
  - ID, aspirante
  - Estado: "Disposición generada - Esperando cierre de Académico"
  - Disposición cargada (descargable)
  - Información de expediente
- **Qué puede hacer:**
  - Ver/descargar disposición
  - Esperar a que Académico cierre

#### Estado: "Expediente finalizado" (En panel)
- **Qué ve:**
  - ID, aspirante
  - Estado: "Expediente finalizado ✓"
  - Número de expediente asignado (ej: "EXP-2024-001-E3")
  - Todos los documentos archivados
  - Botón: "Ver archivo completo"
- **Qué puede hacer:**
  - Descargar cualquier documento
  - Imprimir expediente
  - Archivar expediente en sistema de archivos físicos de la facultad

---

## 6. REQUISITOS NO FUNCIONALES

### 6.1 Seguridad

#### RNF-SEG-001: Autenticación
- OAuth 2.0 para @usal.edu.ar (no exposición de credenciales)
- Contraseña de aspirantes: hashing con bcrypt (salt rounds ≥ 10)
- JWT o sesiones seguras (HTTPS obligatorio)

#### RNF-SEG-002: Autorización basada en roles
- Control de acceso (RBAC) implementado en backend
- Un Aspirante no puede ver solicitudes de otro
- Un Académico ve solo solicitudes de su área
- La Secretaría ve todas las solicitudes

#### RNF-SEG-003: Cifrado de datos sensibles
- Datos personales (DNI, teléfono) se cifran en BD
- Comprobantes de pago se almacenan de forma segura
- Comunicaciones en HTTPS

#### RNF-SEG-004: Validación de entrada
- Todos los inputs validados en backend
- Prevención de inyección SQL, XSS, CSRF
- File upload validado: tipo de archivo, tamaño, escaneo de virus

#### RNF-SEG-005: Auditoría y logs
- Todos los cambios de estado registrados inmutablemente
- Logs del sistema (errores, accesos fallidos, cambios)
- Retención de logs: mínimo 1 año

### 6.2 Disponibilidad y Rendimiento

#### RNF-REND-001: Tiempo de respuesta
- Carga de página: < 2 segundos en conexión normal
- Operaciones CRUD: < 500 ms
- Upload de archivos: feedback progresivo (barra de carga)

#### RNF-REND-002: Disponibilidad
- Sistema operativo 24/7 (uptime objetivo: 99%)
- Mantenimiento programado: máximo 4 horas mensuales, con notificación anticipada

#### RNF-REND-003: Escalabilidad
- Arquitectura debe permitir crecer de 10 a 1000 usuarios simultáneos
- Base de datos indexada para queries frecuentes
- Caché para sesiones si es necesario

#### RNF-REND-004: Almacenamiento
- Soporta hasta 10 GB de documentos inicialmente (expandible)
- Backup automático diario

### 6.3 Usabilidad

#### RNF-USA-001: Interfaz intuitiva
- Diseño responsive (mobile-friendly)
- Terminología consistente con el dominio
- Iconografía clara (checkmark=completado, reloj=en progreso)

#### RNF-USA-002: Accesibilidad
- Cumplimiento básico de WCAG 2.1 (Level A)
- Soporte para screen readers
- Contraste de colores adecuado

#### RNF-USA-003: Documentación
- Help inline en formularios (tooltips, placeholders claros)
- FAQ página con pasos comunes
- Manual de usuario para Académico y Secretaría

### 6.4 Confiabilidad

#### RNF-CONF-001: Integridad de datos
- Transacciones atómicas para cambios de estado
- Validación de constraints en BD
- Backups regulares (mínimo diario, retenidos 30 días)

#### RNF-CONF-002: Recuperación ante fallos
- Reinicio automático de servicios en caso de caída
- Logs de errores detallados para debugging
- Plan de rollback para despliegues

#### RNF-CONF-003: Manejo de errores
- Mensajes de error informativos (sin stack traces)
- Redirección a página de error amigable
- Notificación a administrador en caso de error crítico

### 6.5 Mantenibilidad

#### RNF-MANT-001: Código limpio
- Estándares de código consistentes
- Comentarios en código complejo
- Versionado Git con commits descriptivos

#### RNF-MANT-002: Documentación técnica
- Diagrama de arquitectura
- Descripción de endpoints API
- Instrucciones de despliegue

#### RNF-MANT-003: Logs y monitoreo
- Logs estructurados (JSON o similar)
- Métricas básicas: requests por segundo, errores, tiempo promedio

---

**Versión:** 2.0  
**Fecha:** Septiembre 2024  
**Autores:** [Nombres equipo práctica supervisada]  
**Revisión esperada:** Con profesor y stakeholders antes de iniciar desarrollo
