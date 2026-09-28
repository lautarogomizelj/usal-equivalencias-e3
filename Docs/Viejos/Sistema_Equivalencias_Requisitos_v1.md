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
3. El proceso avanza automáticamente (cambios de estado) sin depender de terceros recordando enviar mails
4. Hay registro auditable de cada acción, cambio de estado y timestamp

---

## 3. ACTORES DEL SISTEMA

### 3.1 Aspirante
- **Perfil:** Estudiante de otra universidad solicitando equivalencias para ingresar a carrera de Ingeniería
- **Método de acceso:** Crear cuenta en el sistema (email + contraseña)
- **Acciones principales:** Cargar documentación, ver estado, recargar documentos si se solicita, efectuar pago

### 3.2 Académico
- **Perfil:** Personal de área académica (usualmente profesor coordinador)
- **Método de acceso:** OAuth con email @usal.edu.ar (Google)
- **Acciones principales:** Realizar análisis preliminar, revisar documentación legal, tomar decisiones de aprobación/rechazo, informar resultados

### 3.3 Secretaría Administrativa
- **Perfil:** Personal de administración/secretaría académica
- **Método de acceso:** OAuth con email @usal.edu.ar (Google)
- **Acciones principales:** Gestionar cupones de pago, verificar pagos, confeccionar disposiciones decanales

---

## 4. REQUISITOS FUNCIONALES

### 4.1 Módulo de Autenticación

#### RF-AUTH-001: Registro de Aspirantes
- El aspirante puede crear una nueva cuenta indicando:
  - Email personal
  - Contraseña (con validaciones: mínimo 8 caracteres, incluir mayúscula, número, carácter especial)
  - Nombre completo
  - Teléfono de contacto
- El sistema envía un mail de confirmación con enlace de validación
- No puede iniciar trámite hasta que valide el email

#### RF-AUTH-002: Login de Aspirantes
- Acceso mediante email + contraseña
- Recupero de contraseña mediante email
- Sesión con expiración (ej: 30 días con "recuérdame")

#### RF-AUTH-003: Login de Académicos y Secretaría Administrativa
- OAuth 2.0 con Google, restringido a dominios @usal.edu.ar
- Si el email no pertenece al dominio, el login es rechazado
- Sesión automática (vinculada a sesión de Google)
- Los roles se asignan automáticamente en la BD según grupo USAL (ej: "group_academicos@usal.edu.ar")

### 4.2 Módulo de Gestión de Solicitudes (Aspirante)

#### RF-SOL-001: Crear Nueva Solicitud de Equivalencias
- El aspirante accede a "Iniciar Trámite" y llena un formulario con:
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
- Al cargar toda la documentación requerida, estado cambia a **"Documentación cargada - En análisis preliminar"**
- Se envía mail automático al Académico: "Documentación lista para analizar. ID: [...]"

#### RF-SOL-003: Ver Estado de Solicitud
- Panel del Aspirante muestra:
  - Estado actual (ej: "En análisis preliminar")
  - Descripción legible del estado
  - Timeline de cambios de estado (quién, cuándo, qué cambió)
  - Documentación cargada (con fecha de carga)
  - Mensajes de retroalimentación del Académico si aplica
  - Botones de acción disponibles según estado actual

#### RF-SOL-004: Recargar Documentación
- Si el Académico rechaza la documentación, el estado es **"Documentación rechazada - Requiere corrección"**
- El panel del Aspirante muestra:
  - Motivo del rechazo (comentario del Académico)
  - Opción de recargar documentos
- Al recargar, estado vuelve a **"Documentación cargada - En análisis preliminar"**
- Se notifica nuevamente al Académico

#### RF-SOL-005: Efectuar Pago
- Cuando el Académico aprueba equivalencias, estado pasa a **"Aprobado - Pendiente pago"**
- El panel del Aspirante muestra:
  - Monto del arancel/cupón a pagar
  - Opción de generar cupón (si no está generado)
  - Comprobante de pago (puede cargar imagen)
- Después de cargar comprobante, estado es **"Esperando verificación de pago"**
- Se notifica a Secretaría Administrativa

### 4.3 Módulo de Panel Académico

#### RF-ACAD-001: Ver Solicitudes Pendientes
- El Académico ve un listado de todas las solicitudes asignadas con:
  - ID y nombre del aspirante
  - Estado actual
  - Fecha de última actualización
  - Documentación disponible (indicador de descarga)
  - Botones de acción según estado

#### RF-ACAD-002: Realizar Análisis Preliminar
- El Académico accede a una vista detallada de la solicitud
- Puede descargar toda la documentación cargada
- Realiza el análisis (matriz tentativa de equivalencias, horario propuesto)
  - Sistema permite cargar un archivo adjunto (ej: matriz en PDF o Excel)
  - O simplemente cambiar estado con un comentario
- Al completar, cambia estado a **"Análisis preliminar completado - Esperando aprobación"**
- Se envía mail al Aspirante: "Tu solicitud ha sido analizada. Estamos procesando..."

#### RF-ACAD-003: Aprobar o Rechazar Solicitud (post-análisis)
- Después del análisis preliminar, el Académico decide:
  - **Aprobar:** Estado → **"Aprobado - Pendiente pago"** + mail al Aspirante con resumen de equivalencias
  - **Rechazar:** Estado → **"Rechazado"** + mail al Aspirante explicando motivo
  - **Solicitar correcciones:** Estado → **"Documentación rechazada - Requiere corrección"** + comentarios específicos

#### RF-ACAD-004: Revisión Legal (Opcional en MVP)
- El Académico puede cargar un documento de "Revisión legal"
- Si hay observaciones, el estado puede pasar a **"En revisión legal"**
- Luego vuelve a "Aprobado" o "Rechazado" según resultado

#### RF-ACAD-005: Informar Cantidad de Materias
- Antes de pasar a Secretaría, el Académico genera un resumen con:
  - Cantidad de materias equivalentes reconocidas
  - Cantidad de materias a adeudar
  - Observaciones relevantes
- Este resumen se guarda en la BD y se incluye en comunicaciones posteriores

### 4.4 Módulo de Panel Secretaría Administrativa

#### RF-SEC-001: Ver Solicitudes en Espera de Gestión
- La Secretaría ve un listado de solicitudes en estado:
  - "Aprobado - Pendiente pago"
  - "Esperando verificación de pago"
  - "Listo para disposición"
- Con información de: ID, aspirante, monto a pagar, fecha de aprobación

#### RF-SEC-002: Generar Cupón de Pago
- La Secretaría puede generar un cupón de pago con:
  - Monto específico
  - ID de solicitud
  - Datos del aspirante
  - Referencia clara (ej: "Arancel equivalencias - E3-2024-001")
- El cupón se guarda en la BD y se puede descargar/imprimir
- Se envía mail al Aspirante con instrucciones de pago

#### RF-SEC-003: Verificar y Registrar Pago
- La Secretaría ve solicitudes con comprobante de pago cargado
- Puede:
  - **Verificar:** Confirmar que el monto es correcto
    - Estado → **"Pago verificado - Listo para disposición"**
  - **Rechazar:** Si el monto no coincide o hay irregularidad
    - Estado → **"Pago rechazado - Requiere reenvío"**
    - Se notifica al Aspirante para que reenvíe comprobante
  - Adjuntar comentarios/observaciones

#### RF-SEC-004: Confeccionar Disposición Decanal
- Una vez verificado el pago, la Secretaría confecciona la Disposición Decanal (resolución que formaliza las equivalencias)
- Puede:
  - Cargar un documento PDF generado externamente
  - O el sistema genera un documento base que edita
- Al cargar, estado → **"Disposición generada - Pendiente envío a Académico"**
- Se envía mail al Académico notificando

#### RF-SEC-005: Archivar y Cerrar
- Después de que el Académico cierra el trámite, la Secretaría puede marcar como **"Finalizado"**
- Se guarda copia de la disposición en el archivo
- Genera un número de expediente final (ej: "EXP-2024-001-E3")

### 4.5 Módulo de Gestión de Estados y Flujo

#### RF-ESTADO-001: Transiciones de Estado Controladas
El sistema mantiene un conjunto finito de estados posibles:
1. Solicitud iniciada
2. Documentación cargada - En análisis preliminar
3. Análisis preliminar completado
4. Aprobado - Pendiente pago
5. Documentación rechazada - Requiere corrección
6. Rechazado
7. Esperando verificación de pago
8. Pago verificado - Listo para disposición
9. Pago rechazado - Requiere reenvío
10. Disposición generada - Pendiente envío a Académico
11. Finalizado

- Cada transición es explícita (no hay cambios automáticos innecesarios)
- Solo ciertos roles pueden efectuar ciertas transiciones (control de permisos)
- Cada cambio de estado queda registrado con timestamp, usuario, y razón (comentario)

#### RF-ESTADO-002: Notificaciones por Cambio de Estado
- Cuando el estado cambia, se envía mail automático al actor siguiente relevante:
  - Si estado → "En análisis preliminar": mail al Académico
  - Si estado → "Aprobado": mail al Aspirante
  - Si estado → "Pendiente pago": mail al Aspirante y Secretaría
  - Si estado → "Pago verificado": mail al Académico
  - Si estado → "Disposición generada": mail al Académico
  - Si estado → "Rechazado" o "Requiere corrección": mail al Aspirante

#### RF-ESTADO-003: Historial Auditable
- El sistema registra para cada cambio de estado:
  - Timestamp (fecha y hora exacta)
  - Usuario que realizó el cambio (email/ID)
  - Estado anterior y nuevo
  - Comentarios/notas asociadas (si aplica)
  - IP de origen (opcional, para seguridad)
- Este historial es visible solo para Académico y Secretaría (rol de auditoría)
- Aspirante ve versión resumida y legible

### 4.6 Módulo de Gestión de Archivos

#### RF-ARCH-001: Almacenamiento de Documentos
- Todos los PDFs/archivos cargados se guardan en el sistema de archivos local del servidor
- Estructura de carpetas:
  ```
  /equivalencias/
    ├── 2024/
    │   ├── EXP-2024-001/
    │   │   ├── solicitud_original.json
    │   │   ├── documentos_aspirante/
    │   │   │   ├── analitico.pdf
    │   │   │   ├── plan_estudios.pdf
    │   │   │   └── programas/
    │   │   ├── analisis_academico/
    │   │   ├── comprobante_pago.pdf
    │   │   └── disposicion_decanal.pdf
  ```
- Cada archivo tiene metadata en BD: nombre original, fecha de carga, tamaño, hash SHA-256 (integridad)

#### RF-ARCH-002: Descargar Documentación
- Académico y Secretaría pueden descargar cualquier documento asociado a una solicitud
- Aspirante puede descargar solo sus propios documentos y la disposición final

#### RF-ARCH-003: Generar Reportes/Resúmenes
- Sistema puede generar un PDF resumen de cada solicitud (opcional para MVP2)
  - Incluye: datos aspirante, documentos cargados, matriz de equivalencias, disposición

### 4.7 Módulo de Comunicaciones

#### RF-COM-001: Envío Automático de Mails
- Sistema envía mails automáticos para cada transición de estado relevante
- Template de mail incluye:
  - Asunto claro indicando acción (ej: "E3-2024-001: Aprobada tu solicitud")
  - Cuerpo con información relevante del estado
  - Datos de contacto de la facultad
  - Link directo al panel (si es relevante)

#### RF-COM-002: Comentarios/Mensajes
- En cada solicitud, usuarios pueden dejar comentarios visibles solo para roles con acceso
- Ej: Académico comenta "Falta programa de Cálculo III", Aspirante ve y puede responder
- Los comentarios quedan en el historial

#### RF-COM-003: Notificaciones In-App (Opcional MVP2)
- Panel muestra badge con cantidad de solicitudes nuevas/urgentes
- Sistema puede mostrar notificación toast cuando llega un mail de cambio de estado

---

## 5. REQUISITOS NO FUNCIONALES

### 5.1 Seguridad

#### RNF-SEG-001: Autenticación
- OAuth 2.0 para @usal.edu.ar (no exposición de credenciales)
- Contraseña de aspirantes: hashing con bcrypt (salt rounds ≥ 10)
- JWT o sesiones seguidas (HTTPS obligatorio)

#### RNF-SEG-002: Autorización basada en roles
- Control de acceso (RBAC) implementado en backend
- Un Aspirante no puede ver solicitudes de otro
- Un Académico ve solo equivalencias de su carrera/facultad (si aplica)
- La Secretaría ve todas las equivalencias (para gestión de pagos)

#### RNF-SEG-003: Cifrado de datos sensibles
- Datos personales (DNI, teléfono) se cifran en BD
- Comprobantes de pago se almacenan de forma segura
- Comunicaciones en HTTPS

#### RNF-SEG-004: Validación de entrada
- Todos los inputs se validan en backend (no confiar en frontend)
- Prevención de inyección SQL, XSS, CSRF
- File upload validado: tipo de archivo, tamaño, escaneo de virus (opcional)

#### RNF-SEG-005: Auditoría y logs
- Todos los cambios de estado quedan registrados inmutablemente
- Logs del sistema (errores, accesos fallidos, cambios de permisos)
- Retención de logs: mínimo 1 año

### 5.2 Disponibilidad y Rendimiento

#### RNF-REND-001: Tiempo de respuesta
- Carga de página: < 2 segundos en conexión normal
- Operaciones CRUD: < 500 ms
- Upload de archivos: feedback progresivo (barra de carga)

#### RNF-REND-002: Disponibilidad
- Sistema operativo 24/7 (uptime objetivo: 99%)
- Mantenimiento programado: máximo 4 horas mensuales, notificado con anticipación

#### RNF-REND-003: Escalabilidad
- Arquitectura debe permitir crecer de 10 a 1000 usuarios simultáneos
- Base de datos indexada para queries frecuentes
- Caché (Redis optional) para sesiones

#### RNF-REND-004: Almacenamiento
- MVP soporta hasta 10 GB de documentos (expandible fácilmente)
- Backup automático diario

### 5.3 Usabilidad

#### RNF-USA-001: Interfaz intuitiva
- Diseño responsive (mobile-friendly)
- Terminología consistente con el dominio (ej: "equivalencias", "disposición", estados legibles)
- Iconografía clara (ej: checkmark para completado, reloj para en progreso)

#### RNF-USA-002: Accesibilidad
- Cumplimiento básico de WCAG 2.1 (Level A)
- Soporte para screen readers
- Contraste de colores adecuado

#### RNF-USA-003: Documentación
- Help inline en formularios (tooltips, placeholders claros)
- FAQ página con pasos comunes
- Manual de usuario para Académico y Secretaría

### 5.4 Confiabilidad

#### RNF-CONF-001: Integridad de datos
- Transacciones atómicas para cambios de estado
- Validación de constraints en BD
- Backups regulares (mínimo diario, retenidos 30 días)

#### RNF-CONF-002: Recuperación ante fallos
- Reinicio automático de servicios en caso de caída
- Logs de errores detallados para debugging
- Plan de rollback para despliegues

#### RNF-CONF-003: Manejo de errores
- Mensajes de error informativos (no exponer stack traces al usuario)
- Redirección a página de error amigable
- Notificación a administrador en caso de error crítico

### 5.5 Mantenibilidad

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
- Dashboard simple de monitoreo (opcional MVP2)

---

## 6. ALCANCE DEL MVP

### MVP Fase 1: Flujo básico end-to-end

**Funcionalidades incluidas:**
- ✅ Autenticación (aspirante manual + Google OAuth para staff)
- ✅ Crear solicitud de equivalencias
- ✅ Cargar documentación (aspirante)
- ✅ Panel de Académico: ver solicitudes, realizar análisis, aprobar/rechazar
- ✅ Panel de Secretaría: ver solicitudes aprobadas, generar cupón, verificar pago
- ✅ Panel de Aspirante: ver estado, cargar documentos, ver estado de pago
- ✅ Cambios de estado automatizados
- ✅ Notificaciones por mail en transiciones clave
- ✅ Historial auditable de cambios
- ✅ Almacenamiento local en servidor

**Funcionalidades excluidas (MVP2+):**
- ❌ Análisis preliminar detallado (Diagrama 3) — simplificado
- ❌ Revisión legal (Diagrama 2) — ejecutado manualmente sin subproceso
- ❌ Generación automática de disposición
- ❌ Integración con sistema de inscripción de USAL
- ❌ Reportes avanzados
- ❌ Notificaciones in-app (solo mail)
- ❌ Buscar/filtrar avanzado
- ❌ API REST pública

### Criterios de aceptación MVP:
- Un aspirante puede ir de "solicitar" a "pago verificado" sin salir del sistema
- Académico ve notificación clara cuando hay trabajo por hacer
- Secretaría recibe todas las solicitudes aprobadas listas para gestión de pago
- Cada cambio de estado queda auditado y visible

---

## 7. TECNOLOGÍA (Indicativo, no vinculante)

### Backend
- **Lenguaje:** Node.js (Express) o Python (Django/FastAPI)
- **BD:** PostgreSQL
- **ORM:** Sequelize (Node) o SQLAlchemy (Python)
- **Email:** Sendgrid, Mailgun, o SMTP nativo del servidor
- **Auth:** Passport.js (OAuth + Local)

### Frontend
- **Framework:** React o Vue.js
- **Styling:** Tailwind CSS o Bootstrap
- **State management:** Context API o Pinia

### Infraestructura
- **Hosting:** Servidor local USAL (Ubuntu/CentOS)
- **Reverse proxy:** Nginx
- **SSL:** Let's Encrypt (certificado gratuito)
- **Storage:** Sistema de archivos local (/var/equivalencias o similar)

---

## 8. CRONOGRAMA ESTIMADO (16 semanas, 3 personas)

### Semanas 1-2: Setup + Autenticación
- Configurar repositorio Git
- Diseñar BD básica
- Implementar login/registro

### Semanas 3-4: Módulos Aspirante + Académico
- Panel de solicitudes
- Upload de documentos
- Cambios de estado

### Semanas 5-6: Módulo Secretaría + Mails
- Panel de gestión de pagos
- Notificaciones automáticas
- Pruebas end-to-end

### Semanas 7-8: Refinamiento + Documentación
- Bugs, UX improvements
- Manual de usuario
- Preparación para presentación

### Semanas 9-16: Buffer + MVP2 (si tiempo permite)
- Reportes
- Análisis preliminar detallado
- Integración con sistemas adicionales

---

## 9. RIESGOS Y MITIGACIONES

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|--------|-----------|
| Cambios en requisitos mid-project | Alta | Medio | Validar con profesor frecuentemente, documentar decisiones |
| Servidor no disponible para testing | Media | Alto | Negociar acceso temprano, plan B local |
| Integración OAuth complicada | Baja | Medio | Proof of concept temprano |
| Volumen de archivos muy alto | Baja | Medio | Implementar compresión, validar límites |
| Falta de claridad en flujo legal | Media | Alto | Entrevista con Secretaría/Académico en semana 1 |

---

## 10. CRITERIOS DE ÉXITO

1. ✅ El proceso de equivalencias funciona end-to-end sin salir de la plataforma
2. ✅ Cero dependencia de Gmail para flujo (solo notificaciones)
3. ✅ Cada rol ve exactamente lo que necesita hacer, sin confusión
4. ✅ Auditoría completa: se puede saber qué pasó, quién lo hizo, y cuándo
5. ✅ Académico ahorra mínimo 50% del tiempo dedicado a recordar y enviar mails
6. ✅ Aspirante tiene visibilidad clara de su solicitud en todo momento
7. ✅ Sistema es mantenible por personal técnico de USAL post-proyecto

---

**Versión:** 1.0  
**Fecha:** Septiembre 2024  
**Autores:** [Nombres equipo práctica supervisada]  
**Revisión esperada:** Con profesor y stakeholders antes de iniciar desarrollo
