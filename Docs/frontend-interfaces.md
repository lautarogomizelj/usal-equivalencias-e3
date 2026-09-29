# Frontend Interfaces Specification
## Sistema de Tramitación de Equivalencias Electrónicas (E3)

Versión: 1.0  
Documento objetivo: Equipo Frontend  
Basado en: Especificación de Requisitos Funcionales v5.0

---

# 0. ESTÁNDARES Y LINEAMIENTOS DE DESARROLLO FRONTEND

## 0.1 Objetivo de esta sección

Esta sección define los estándares técnicos y organizativos obligatorios para el equipo Frontend durante el desarrollo del Sistema E3.

Su objetivo es garantizar:

- Consistencia visual
- Escalabilidad
- Mantenibilidad
- Reutilización de código
- Facilidad de evolución futura
- Experiencia de usuario uniforme
- Integración predecible con Backend

Estas reglas aplican a todas las pantallas, componentes, formularios, tablas, modales y flujos definidos posteriormente en este documento.

## 0.2 Arquitectura y organización del código

### 0.2.1 Separación de responsabilidades

Debe existir una separación clara entre:

- Presentación (UI)
- Lógica de negocio
- Estado de aplicación
- Comunicación con APIs
- Estilos
- Utilidades compartidas

No se debe mezclar lógica compleja dentro de componentes visuales.

Los componentes de interfaz deben enfocarse únicamente en renderizar información y emitir eventos.

### 0.2.2 Reutilización de código

La reutilización es obligatoria.

Antes de crear un nuevo componente se deberá verificar si existe uno reutilizable.

Debe priorizarse:

- Composición
- Parametrización
- Reutilización

por encima de copiar y pegar código.

Se considera deuda técnica duplicar implementaciones equivalentes.

### 0.2.3 Componentes compartidos

Todo elemento reutilizable debe abstraerse en componentes compartidos.

Ejemplos:

- Botones
- Inputs
- Selects
- Textareas
- Checkboxes
- Radio buttons
- Tablas
- Paginadores
- Modales
- Tooltips
- Alertas
- Toasts
- Badges
- Timeline
- Chat
- Visores de documentos
- Selectores de fecha
- Skeleton loaders

No deben existir múltiples implementaciones del mismo componente base.

### 0.2.4 Estructura de carpetas recomendada

Se recomienda una estructura similar a:

```text
src/
├── assets/
├── components/
├── layouts/
├── pages/
├── services/
├── hooks/
├── store/
├── styles/
├── tokens/
├── types/
├── utils/
└── constants/
```

Objetivos:

- Facilitar escalabilidad
- Reducir acoplamiento
- Simplificar mantenimiento

### 0.2.5 Componentes de página

Las páginas deben componerse mediante componentes reutilizables.

No deben contener:

- Lógica repetida
- Estilos propios excesivos
- Llamadas directas a APIs

### 0.2.6 Archivos de configuración

Toda configuración compartida debe estar centralizada.

Ejemplos:

- Rutas
- Constantes
- Estados
- Permisos
- Tipos de archivos permitidos
- Límites de carga

## 0.3 Design System y estilos

### 0.3.1 Sistema de tokens obligatorio

Debe existir un sistema centralizado de Design Tokens.

Como mínimo:

- Colores
- Espaciados
- Bordes
- Radios
- Sombras
- Tipografías
- Breakpoints
- Alturas
- Anchuras reutilizables
- Z-index
- Tamaños de iconos

Ejemplo:

```text
tokens/
├── colors/
├── spacing/
├── radius/
├── shadows/
├── typography/
├── breakpoints/
├── sizing/
└── zIndex/
```

Todos los componentes deben consumir estos tokens.

### 0.3.2 Prohibición de valores hardcodeados

No se deben hardcodear:

- Colores
- Radios
- Márgenes
- Paddings
- Sombras
- Tamaños

Ejemplo incorrecto:

```css
color: #ff0000;
border-radius: 13px;
```

Ejemplo correcto:

```css
color: var(--color-danger);
border-radius: var(--radius-md);
```

### 0.3.3 Prohibición de inline styles

No utilizar estilos inline.

Incorrecto:

```jsx
<div style={{ marginTop: 10 }}>
```

Todo estilo debe estar definido en:

- Archivos CSS
- CSS Modules
- Styled Components
- Sistema equivalente adoptado

### 0.3.4 Separación de estilos

Los estilos deben estar desacoplados del comportamiento.

No mezclar:

- Estilos
- Llamadas API
- Validaciones
- Lógica de negocio

dentro del mismo archivo cuando sea evitable.

### 0.3.5 Escala de espaciado

Debe existir una escala consistente.

Ejemplo:

```text
4
8
12
16
24
32
48
64
```

Todos los componentes deben utilizar dicha escala.

### 0.3.6 Radios y bordes

Deben existir valores reutilizables.

Ejemplo:

```text
xs
sm
md
lg
xl
```

### 0.3.7 Sistema de iconografía

Los iconos deben provenir de una única librería.

No mezclar múltiples bibliotecas sin justificación.

### 0.3.8 Layouts reutilizables

Se recomienda crear layouts compartidos para:

- Dashboard
- Formularios
- Tablas
- Detalle de solicitud
- Gestión administrativa

## 0.4 Validaciones

### 0.4.1 Validaciones frontend obligatorias

Todas las validaciones implementadas en backend deben poseer una validación equivalente en frontend.

Ejemplos:

- Email obligatorio
- DNI obligatorio
- Contraseña válida
- Teléfono válido
- Archivo obligatorio
- Tamaño máximo permitido
- Formato permitido

### 0.4.2 Backend como fuente de verdad

Las validaciones frontend son una mejora de experiencia.

Las validaciones backend son la autoridad final.

Frontend nunca debe asumir que un dato es válido.

### 0.4.3 Validaciones compartidas

Las reglas deben centralizarse.

Ejemplo:

```text
validations/
├── email/
├── password/
├── phone/
├── documents/
└── forms/
```

No deben existir validaciones duplicadas.

### 0.4.4 Mensajes de error consistentes

Los mensajes de error deben ser:

- Claros
- Descriptivos
- Reutilizables

Evitar mensajes ambiguos.

## 0.5 Manejo de estados de interfaz

Toda pantalla debe contemplar explícitamente los siguientes estados.

### 0.5.1 Loading state

Mientras se esperan datos.

Ejemplos:

- Solicitudes
- Documentos
- Historial
- Auditoría

### 0.5.2 Empty state

Ejemplos:

- Sin solicitudes
- Sin mensajes
- Sin responsables
- Sin documentos

### 0.5.3 Error state

Ejemplos:

- Error servidor
- Error red
- Acceso denegado
- Recurso inexistente

### 0.5.4 Success state

Toda acción importante debe generar feedback visual.

Ejemplos:

- Guardado exitoso
- Documento cargado
- Solicitud creada

### 0.5.5 Skeleton loaders

Las cargas deben utilizar skeletons o placeholders.

Evitar pantallas vacías durante la carga.

## 0.6 Accesibilidad

Las interfaces deben contemplar:

- Navegación por teclado
- Foco visible
- Labels asociados
- Contraste suficiente
- Mensajes de error accesibles
- Estructura semántica correcta

### 0.6.1 Formularios accesibles

Todo campo debe tener:

- Label
- Mensaje de ayuda (si aplica)
- Mensaje de error

### 0.6.2 Tablas accesibles

Las tablas deben permitir:

- Navegación
- Lectura clara
- Adaptación responsive

## 0.7 Responsive design

Todas las pantallas deben funcionar correctamente en:

- Desktop
- Tablet
- Mobile

### 0.7.1 Breakpoints centralizados

Todos los breakpoints deben provenir de tokens.

### 0.7.2 Componentes responsivos

Los componentes deben adaptarse sin duplicar vistas.

## 0.8 Consistencia de navegación

El usuario siempre debe entender:

- Dónde está
- Qué está viendo
- Qué puede hacer

### 0.8.1 Encabezados consistentes

Toda pantalla debe incluir:

- Título
- Contexto
- Acciones principales

### 0.8.2 Navegación predecible

La navegación debe ser uniforme en toda la aplicación.

## 0.9 Comunicación con backend

### 0.9.1 Servicios centralizados

Toda llamada a APIs debe pasar por servicios centralizados.

No realizar llamadas dispersas dentro de componentes.

### 0.9.2 Manejo centralizado de errores

Debe existir una capa común para:

- 400
- 401
- 403
- 404
- 500
- Errores de red

### 0.9.3 Tipado compartido

Siempre que sea posible se recomienda compartir contratos y DTOs con backend.

## 0.10 Gestión de permisos

La interfaz debe reaccionar dinámicamente a permisos recibidos desde backend.

### 0.10.1 Visibilidad condicional

Botones y acciones deben mostrarse únicamente cuando el usuario tenga permiso.

### 0.10.2 Roles soportados

La aplicación debe contemplar:

- Aspirante
- Académico
- Secretaría
- Admin
- Revisor
- Observador

### 0.10.3 Permisos temporales

La interfaz debe soportar permisos temporales por solicitud.

## 0.11 Auditoría de acciones

Las acciones críticas deben mostrar claramente:

- Qué acción se ejecutará
- Consecuencias
- Impacto

### 0.11.1 Doble verificación

Las acciones definidas por negocio deberán respetar el flujo de doble confirmación.

## 0.12 Escalabilidad

El frontend debe diseñarse considerando futuras expansiones.

### 0.12.1 Nuevos roles

La incorporación de nuevos roles no debe requerir reescribir pantallas existentes.

### 0.12.2 Nuevos estados

Los estados deben ser configurables.

Evitar lógica rígida basada en textos hardcodeados.

### 0.12.3 Nuevas carreras

La interfaz debe soportar crecimiento indefinido de carreras.

## 0.13 Mantenibilidad

El código debe ser:

- Legible
- Modular
- Documentado
- Consistente

### 0.13.1 Convenciones

Mantener convenciones uniformes para:

- Nombres de archivos
- Nombres de componentes
- Nombres de variables
- Nombres de servicios

### 0.13.2 Complejidad

Evitar componentes excesivamente grandes.

Promover componentes pequeños y reutilizables.

## 0.14 Objetivo de esta etapa

Este documento corresponde a la primera etapa de definición funcional de interfaces.

No representa el diseño visual final.

El equipo frontend podrá proponer mejoras de UX/UI siempre que:

- No alteren reglas funcionales
- No modifiquen permisos
- No eliminen información obligatoria
- No cambien flujos de negocio

## 0.15 Etapas futuras

Posteriormente podrán incorporarse:

- Wireframes
- Mockups
- Design System definitivo
- Biblioteca de componentes
- Manual UX
- Manual UI
- Guía de accesibilidad
- Guía responsive
- Guía de integración frontend-backend

Sin modificar los requerimientos funcionales definidos en este documento.

---

# 1. OBJETIVO DEL DOCUMENTO

Este documento define todas las interfaces visibles del sistema E3 desde la perspectiva del usuario final.

Su propósito es servir como guía para el equipo Frontend durante el diseño y construcción de la experiencia de usuario.

No define:

- Colores
- Tipografías
- Diseño visual
- Estilo gráfico
- Framework UI

Sí define:

- Pantallas
- Formularios
- Tablas
- Pestañas
- Modales
- Componentes
- Estados visuales
- Acciones disponibles
- Restricciones por rol
- Elementos obligatorios visibles

El objetivo es que ningún flujo funcional definido en requisitos quede sin representación visual.

---

# 2. COMPONENTES GLOBALES DEL SISTEMA

Estos componentes son reutilizados por múltiples roles.

---

## 2.1 Barra Superior

Debe existir para todos los usuarios autenticados.

Contenido:

- Nombre del sistema
- Acceso a inicio/dashboard
- Notificaciones
- Perfil de usuario
- Cerrar sesión

---

## 2.2 Menú Lateral

Contenido dinámico según rol.

Puede incluir:

- Dashboard
- Solicitudes
- Gestión de usuarios
- Carreras
- Auditoría
- Alertas
- Mi perfil

---

## 2.3 Timeline de Proceso

Componente reutilizable.

Debe mostrar:

- Fecha y hora
- Estado anterior
- Estado nuevo
- Usuario responsable
- Comentarios
- Orden descendente por fecha

Utilizado por:

- Aspirante
- Académico
- Secretaría
- Admin
- Revisor

---

## 2.4 Visor de Documentos

Permite:

- Descargar archivo
- Ver metadatos
- Ver fecha de carga
- Ver tamaño

Aplicable a:

- PDFs
- JPG
- PNG

---

## 2.5 Chat de Solicitud

Componente reutilizable.

Incluye:

- Historial
- Campo de mensaje
- Adjuntos
- Estado leído
- Timestamp
- Tipo de consulta

Modo lectura para observadores.

---

## 2.6 Modal de Confirmación

Utilizado para acciones críticas.

Debe mostrar:

- Acción solicitada
- Consecuencia
- Confirmación explícita

Compatible con doble verificación.

---

# 3. INTERFACES PÚBLICAS

---

## 3.1 Pantalla de Inicio

Visible sin autenticación.

Debe contener:

- Información del sistema
- Botón iniciar sesión
- Botón registrarse
- Recuperar contraseña

---

## 3.2 Registro de Aspirante

Formulario.

Campos:

- Nombre completo
- Email
- Contraseña
- Confirmación contraseña
- Teléfono
- DNI

Validaciones visuales.

Acciones:

- Crear cuenta

---

## 3.3 Validación de Código

Pantalla posterior al registro.

Debe contener:

- Campo código de 6 dígitos
- Temporizador de expiración
- Reenviar código

Estados:

- Código válido
- Código inválido
- Código expirado
- Cuenta bloqueada temporalmente

---

## 3.4 Login Aspirante

Campos:

- Email
- Contraseña
- Recuérdame

Acciones:

- Ingresar
- Recuperar contraseña

Mensajes de bloqueo.

---

## 3.5 Recuperar Contraseña

Paso 1:

- Email

Paso 2:

- Código

Paso 3:

- Nueva contraseña

---

# 4. ROL ASPIRANTE

---

## 4.1 Dashboard Aspirante

Pantalla principal.

Debe mostrar:

- Solicitudes activas
- Solicitudes finalizadas
- Estado actual
- Responsable académico
- Responsable administrativo
- Última actualización

Acciones:

- Crear solicitud
- Ver solicitud

---

## 4.2 Crear Solicitud

Formulario completo.

Secciones:

### Datos Personales

Precompletados.

### Datos Académicos

Campos:

- Universidad origen
- Facultad origen
- Carrera origen
- Carrera destino
- Condición académica
- Observaciones

Acción:

- Crear solicitud

---

## 4.3 Modal Confirmación Crear Solicitud

Muestra:

- Resumen de datos
- Carrera seleccionada

Acción:

- Confirmar creación

---

## 4.4 Detalle de Solicitud

Vista principal del expediente.

Debe incluir:

### Información General

- Código expediente
- Estado
- Carrera destino
- Universidad origen

### Responsables

- Académico
- Administrativo

### Pestañas

- Documentación
- Chat
- Historial
- Informe Final

---

## 4.5 Pestaña Documentación

Listado de documentos.

Información:

- Nombre
- Fecha
- Tamaño
- Tipo

Acciones:

- Descargar
- Cargar documento

---

## 4.6 Pantalla Carga Documental Inicial

Documentos requeridos:

- Analítico
- Plan de estudios
- Programas

Indicadores:

- Archivos cargados
- Tamaño utilizado

Acción:

- Enviar documentación

---

## 4.7 Modal Confirmación Envío Documentación

Resumen de archivos.

Acción:

- Confirmar

---

## 4.8 Pantalla Recarga Documental

Disponible en DOC-RECH.

Permite:

- Eliminar archivo anterior
- Cargar nueva versión

---

## 4.9 Chat del Proceso

Debe mostrar:

- Historial completo
- Adjuntos
- Tipo de consulta

Permite:

- Enviar mensaje
- Adjuntar archivo

---

## 4.10 Selección de Horario

Visible cuando exista propuesta.

Debe mostrar:

- Horarios disponibles
- Estado de cada horario

Acciones:

- Seleccionar horario

---

## 4.11 Modal Confirmación Horario

Muestra:

- Fecha
- Hora

Acción:

- Confirmar selección

---

## 4.12 Pantalla Resultado Aprobado

Muestra:

- Materias reconocidas
- Correlativas
- Carga horaria
- Observaciones

---

## 4.13 Pantalla Cupón de Pago

Muestra:

- Cupón generado
- Fecha
- Monto

Acciones:

- Descargar

---

## 4.14 Pantalla Carga de Comprobante

Campos:

- Archivo comprobante

Acción:

- Enviar comprobante

---

## 4.15 Modal Confirmación Pago

Acción:

- Confirmar envío

---

## 4.16 Pantalla Expediente Finalizado

Debe mostrar:

- Estado final
- Fecha finalización

Acciones:

- Descargar expediente ZIP

---

# 5. ROL ACADÉMICO

---

## 5.1 Dashboard Académico

Widgets:

- Mis solicitudes
- Sin responsable
- Pendientes
- Reuniones próximas

Filtros rápidos.

---

## 5.2 Listado de Solicitudes

Tabla.

Columnas:

- ID
- Aspirante
- Universidad origen
- Carrera
- Estado
- Responsable
- Última actualización

Filtros:

- Estado
- Fecha
- Carrera
- Asignadas
- Todas

---

## 5.3 Modal Tomar Proceso

Información:

- Solicitud
- Aspirante

Acción:

- Confirmar

---

## 5.4 Vista Detallada de Solicitud

Pestañas obligatorias:

- Información General
- Documentación
- Análisis
- Reunión
- Chat
- Historial
- Auditoría

---

## 5.5 Pestaña Información General

Debe mostrar:

- Datos personales
- Datos académicos
- Responsables
- Estado
- Fechas clave

---

## 5.6 Pestaña Documentación

Listado completo.

Acciones:

- Descargar archivo
- Descargar expediente ZIP

---

## 5.7 Pantalla Análisis Preliminar

Debe permitir:

- Cargar matriz tentativa
- Cargar horario
- Escribir observaciones

Acción:

- Completar análisis

---

## 5.8 Modal Completar Análisis

Doble verificación.

---

## 5.9 Pantalla Propuesta de Horarios

Calendario integrado.

Funciones:

- Crear horarios
- Editar horarios
- Eliminar horarios

Máximo:

- 7 propuestas

---

## 5.10 Modal Envío de Horarios

Acción:

- Confirmar envío

---

## 5.11 Modal Confirmación de Reunión

Opciones:

- Confirmar
- Rechazar
- Proponer nuevos horarios

---

## 5.12 Pantalla Resultado de Reunión

Campos:

- Reunión realizada
- No asistió
- Notas
- Link Meet

Decisiones:

- Permite inscripción
- Requiere cambios
- Rechaza solicitud

---

## 5.13 Modal Guardar Resultado

Doble verificación.

---

## 5.14 Pantalla Revisión Legal

Elementos:

- Documentación
- Observaciones
- Archivo adjunto

Decisiones:

- Aprobar
- Rechazar
- Solicitar cambios

---

## 5.15 Modal Decisión Legal

Doble verificación.

---

## 5.16 Modal Enviar a Secretaría

Doble verificación.

---

## 5.17 Exportación de Expedientes

Opciones:

- Individual
- Múltiple

Filtros:

- Estado
- Fecha

---

## 5.18 Modal Darse de Baja

Información:

- Consecuencias
- Estado actual

Doble verificación.

---

## 5.19 Pantalla Cierre de Expediente

Debe mostrar:

- Disposición decanal
- Archivos finales

Acción:

- Finalizar expediente

---

## 5.20 Modal Finalización

Doble verificación.

---

# 6. ROL SECRETARÍA ADMINISTRATIVA

---

## 6.1 Dashboard Secretaría

Widgets:

- Pendientes de pago
- Pagos pendientes verificar
- Disposiciones pendientes
- Mis solicitudes

---

## 6.2 Listado Solicitudes

Misma estructura que académico.

---

## 6.3 Vista Detallada Solicitud

Mismas pestañas que académico.

---

## 6.4 Pantalla Gestión de Pago

Datos:

- Monto
- Solicitud
- Aspirante

Acción:

- Generar cupón

---

## 6.5 Vista Previa Cupón

Debe mostrar PDF generado.

Acción:

- Confirmar generación

---

## 6.6 Modal Generar Cupón

Doble verificación.

---

## 6.7 Pantalla Verificación de Pago

Debe mostrar:

- Comprobante
- Monto
- Comentarios

Opciones:

- Correcto
- Incorrecto

---

## 6.8 Modal Verificación Pago

Doble verificación.

---

## 6.9 Pantalla Disposición Decanal

Opciones:

- Subir PDF
- Generar desde plantilla

---

## 6.10 Modal Generación Disposición

Doble verificación.

---

## 6.11 Exportación de Expedientes

Opciones:

- Individual
- Múltiple
- Consolidada

---

## 6.12 Pantalla Archivo Administrativo

Campos:

- Número expediente final
- Estado archivado

---

# 7. ROL ADMIN GENERAL

---

## 7.1 Dashboard Admin

Widgets:

- Procesos sin responsable académico
- Procesos sin responsable administrativo
- Cuentas bloqueadas
- Actividad reciente
- Alertas

---

## 7.2 Gestión de Usuarios

Tabla:

- Nombre
- Email
- Rol
- Estado
- Último acceso

Acciones:

- Crear
- Editar
- Desactivar
- Ver permisos

---

## 7.3 Crear Usuario

Campos:

- Nombre
- Email
- Rol

Acción:

- Crear cuenta

---

## 7.4 Editar Usuario

Permite:

- Modificar datos
- Activar
- Desactivar

---

## 7.5 Gestión de Permisos

Debe mostrar:

- Carreras asignadas
- Permisos permanentes
- Permisos temporales

Acciones:

- Asignar
- Revocar

---

## 7.6 Modal Asignación de Permisos

Campos:

- Usuario
- Carrera
- Nivel permiso

---

## 7.7 Modal Revocación

Debe mostrar:

- Procesos afectados
- Consecuencias

Doble verificación.

---

## 7.8 Gestión de Carreras

Tabla:

- Código
- Nombre
- Facultad
- Estado

Acciones:

- Crear
- Editar
- Desactivar

---

## 7.9 Crear Carrera

Campos:

- Código
- Nombre
- Facultad
- Descripción

---

## 7.10 Editar Carrera

Mismos campos.

---

## 7.11 Pantalla Asignar Responsable

Debe mostrar:

- Solicitud
- Responsables actuales
- Usuarios disponibles

Permite:

- Asignar académico
- Asignar administrativo

---

## 7.12 Modal Asignación Manual

Debe contemplar:

- Permiso temporal
- Comentario obligatorio

Doble verificación.

---

## 7.13 Pantalla Reapertura de Expediente

Debe mostrar:

- Estado actual
- Estado destino

Campo:

- Motivo

---

## 7.14 Modal Reapertura

Doble verificación.

---

## 7.15 Auditoría General

Tabla:

- Timestamp
- Usuario
- Acción
- Entidad
- Detalles
- IP

Filtros:

- Usuario
- Acción
- Fecha
- Entidad

---

## 7.16 Detalle de Evento de Auditoría

Debe mostrar:

- Datos completos
- Estados involucrados
- Comentarios

---

## 7.17 Perfil Admin

Secciones:

- Información
- Seguridad
- Historial accesos

---

## 7.18 Cambio de Email

Formulario dedicado.

Doble verificación.

---

## 7.19 Cambio de Contraseña

Formulario dedicado.

Doble verificación.

---

# 8. ROL REVISOR DE LOGS

---

## 8.1 Dashboard Revisor

Widgets:

- Solicitudes recientes
- Cambios recientes
- Actividad sistema

---

## 8.2 Listado Solicitudes

Solo lectura.

---

## 8.3 Detalle Solicitud

Solo lectura.

Incluye:

- Información
- Documentación
- Chat
- Historial
- Auditoría

---

## 8.4 Auditoría General

Misma información que Admin.

Sin acciones.

---

## 8.5 Detalle Evento Auditoría

Solo lectura.

---

## 8.6 Perfil Revisor

Solo lectura.

---

# 9. ROL OBSERVADOR

---

## 9.1 Dashboard Observador

Acceso únicamente de consulta.

---

## 9.2 Listado Solicitudes

Solo lectura.

---

## 9.3 Detalle Solicitud

Puede visualizar:

- Información
- Documentación
- Chat
- Historial

No puede:

- Escribir
- Tomar proceso
- Modificar

---

# 10. ESTADOS VACÍOS

Todas las pantallas deben contemplar estados vacíos.

Ejemplos:

- Sin solicitudes
- Sin documentos
- Sin mensajes
- Sin responsables
- Sin resultados
- Sin auditoría

---

# 11. ESTADOS DE ERROR

Todas las pantallas deben contemplar:

- Error de carga
- Error de permisos
- Archivo inválido
- Archivo demasiado grande
- Sesión expirada
- Acción no permitida

---

# 12. ACCIONES CON DOBLE VERIFICACIÓN

Frontend debe implementar obligatoriamente flujo visual de doble confirmación para:

- Aprobar solicitud
- Rechazar solicitud
- Cambiar responsable
- Revocar permisos
- Reabrir expediente
- Cambiar email admin
- Cambiar contraseña admin
- Darse de baja como responsable
- Completar análisis
- Confirmar reunión
- Generar cupón
- Verificar pago
- Generar disposición
- Finalizar expediente

---

# 13. CONSIDERACIONES DE IMPLEMENTACIÓN

El frontend debe asumir que:

- Los permisos pueden cambiar dinámicamente.
- Existen permisos temporales por solicitud.
- Existen responsables académicos y administrativos independientes.
- Un usuario puede visualizar una solicitud sin poder participar.
- La auditoría es visible en múltiples roles.
- El historial del proceso es un componente central de la aplicación.
- Los estados del expediente son controlados por backend.
- Ningún usuario cambia estados manualmente salvo mediante acciones funcionales autorizadas.

Fin del documento.