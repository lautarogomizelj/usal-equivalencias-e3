# Frontend Interfaces Specification
## Sistema de Tramitación de Equivalencias Electrónicas (E3)

Versión: 2.0  
Documento objetivo: Equipo Frontend  
Basado en: Especificación de Requisitos Funcionales y No Funcionales v8.0 (16 estados)  

**Cambio principal respecto de v1.0:** a partir del punto 4 las interfaces están definidas **por estado de la solicitud**. Para cada estado en que un rol tiene que hacer algo (o esperar algo) existe una interfaz propia, con su código (`ASP-…`, `ACAD-…`, `SEC-…`). El listado completo de puntos que requieren decisión de negocio está en el Anexo A.

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 2.0 | 2026-09-29 | lautarogomizelj | Interfaces reorganizadas por estado de la solicitud (16 estados) con códigos `ASP-…`/`ACAD-…`/`SEC-…`, basadas en requisitos v8.0 |
| 1.0 | 2026-09-28 | lautarogomizelj | Versión inicial: estándares frontend, componentes globales e interfaces por rol, basada en requisitos v5.0 |

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
│   └── ui/
├── constants/
├── hooks/
├── layouts/
├── pages/
├── router/
├── services/
├── store/
├── styles/
├── tokens/
├── types/
├── utils/
└── validations/
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
├── colors.css
├── spacing.css
├── radius.css
├── shadows.css
├── typography.css
├── breakpoints.css
├── sizing.css
└── z-index.css
```

Todos los tokens se importan desde `tokens/index.css`, que es el único punto de entrada. Todos los componentes deben consumir estos tokens.

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

La selección de interfaz por estado se resuelve con un registro configurable (ver 0.16).

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

## 0.16 Arquitectura de UI basada en estado (State-driven UI)

El sistema E3 es una aplicación **guiada por estado**: una misma solicitud se ve y se comporta distinto en cada uno de sus 16 estados, y distinto para cada rol.

### 0.16.1 Regla general

La interfaz que se muestra dentro de una solicitud se decide por la combinación:

```text
(rol del usuario, ¿participante u observador?, estado_codigo, flags auxiliares)
```

Flags auxiliares definidos hasta ahora: `reunion_realizada`, ronda de horarios (1 o 2), horario elegido por el aspirante, existencia de responsable académico / administrativo.

### 0.16.2 Registro de interfaces por estado

Debe existir un único registro (archivo de configuración) que asocie cada combinación con un componente. Ejemplo conceptual:

```text
stateViews = {
  ASPIRANTE: {
    'SOL-INI':  CargaDocumentalInicial,
    'DOC-CAR':  DocumentacionEnRevision,
    'DOC-RECH': RecargaSelectiva,
    ...
  },
  ACADEMICO: { ... },
  SECRETARIA: { ... }
}
```

Reglas:

- Se usa siempre el **código** de estado (`SOL-INI`), nunca el nombre (RF-ESTADO-004).
- Un estado sin entrada en el registro muestra una interfaz genérica de "solo lectura" (nunca pantalla en blanco).
- Agregar o renombrar un estado no debe requerir tocar los componentes, solo el registro.
- La interfaz **no decide** qué transiciones son válidas: eso lo hace el backend. La interfaz muestra los botones que el estado habilita y maneja el rechazo del backend (ver 0.16.4).

### 0.16.3 Tipos de interfaz por estado

| Tipo | Significado | Contiene botones de trámite |
|---|---|---|
| **A – Acción** | El rol tiene algo que hacer en este estado | Sí |
| **E – Espera** | Otro actor tiene la pelota; se informa qué pasó y qué sigue | No |
| **T – Terminal** | El trámite terminó (`FINAL`, `RECHAZO`) | No (solo descargas / consulta) |

### 0.16.4 Cambio de estado en vivo y conflictos

- Si el estado cambia mientras el usuario tiene la pantalla abierta (por acción de otro actor, reasignación o reapertura), la interfaz debe refrescarse y avisar: "El estado de la solicitud cambió".
- Si el usuario ejecuta una acción que ya no está permitida (el backend responde conflicto/403), se muestra "Esta acción ya no está disponible porque el estado de la solicitud cambió" y se recarga el panel. Los archivos seleccionados pero no enviados no se pierden en el aviso, pero tampoco se envían.

### 0.16.5 Códigos de interfaz

Cada interfaz tiene un código estable para referenciarla en tickets, QA y backend:

- `ASP-<ESTADO>`: aspirante (ej. `ASP-SOL-INI`)
- `ACAD-<ESTADO>`: académico (ej. `ACAD-DOC-CAR`)
- `SEC-<ESTADO>`: secretaría (ej. `SEC-ENV-SECREC`)
- Sufijos `-M1`, `-M2`: modales de esa interfaz

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

**Enfoque v2:** el documento no describe "pantallas sueltas" sino, para cada rol, las interfaces que aparecen en cada estado de la solicitud. Un mismo botón (por ejemplo "Enviar documentación") puede vivir en dos interfaces distintas (`ASP-SOL-INI` y `ASP-DOC-RECH`) porque el contexto y las reglas cambian.

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

## 2.7 Banner de Estado

Componente reutilizable, siempre presente en el detalle de una solicitud (todos los roles).

Muestra:

- Título corto del momento actual ("Cargá tu documentación", "Estamos verificando tu pago")
- Texto de una o dos líneas: qué pasó y qué sigue
- Quién tiene la pelota: "Te toca a vos" / "Estamos trabajando en esto" / "Trámite cerrado"
- Botón de acción principal (si el estado es de tipo A para el rol)

Variantes visuales: acción requerida, en espera, éxito, atención (rechazos), cierre negativo.

---

## 2.8 Tabla de Documentos con Marcas

Componente compartido entre Aspirante y Académico/Secretaría (RF-ACAD-009).

Columnas base: tipo, descripción, archivo, versión, fecha de carga, tamaño, marca de revisión, comentario.

Marcas de revisión:

- ⏳ Pendiente de revisión (`PENDIENTE`)
- ✅ Correcto (`OK`)
- ❌ Debe recargar (`RECARGAR`)

Modos:

- **Solo lectura** (aspirante en estados de espera, observadores, revisor)
- **Carga** (aspirante en `SOL-INI` y `DOC-RECH`)
- **Revisión** (académico participante: puede cambiar marcas y comentarios)

Debe mostrar versiones anteriores de un documento recargado (la versión anterior nunca se borra).

---

## 2.9 Stepper de Etapas

Indicador visual del avance del trámite en macro-etapas: Documentación, Análisis, Reunión, Revisión legal, Resolución, Pago, Disposición, Finalizado.

- La etapa activa se deriva del estado mediante una tabla de configuración (ver 4.5).
- Es informativo: no navega ni permite acciones.
- `RECHAZO` se muestra como cierre negativo.

---

## 2.10 Badge de Estado

Muestra código y nombre del estado (`DOC-CAR – Documentación cargada`). Se usa en listados, tarjetas y encabezados. La paleta se define en tokens por tipo de estado (acción, espera, éxito, atención, terminal).

---

## 2.11 Cargador de Archivos (Uploader)

Componente único para toda carga de archivos.

Debe:

- Aceptar arrastrar y soltar y selector clásico
- Validar formato y tamaño antes de subir (límites parametrizables por pantalla)
- Mostrar progreso, éxito y error por archivo, con reintento
- Permitir quitar un archivo seleccionado antes de enviar
- Mostrar el consumo de tamaño total cuando aplique

Límites vigentes:

| Uso | Formatos | Límite por archivo | Límite total |
|---|---|---|---|
| Documentación (RF-ASP-002 / 004) | PDF, JPG | 10 MB | 50 MB |
| Comprobante de pago (RF-ASP-007) | PDF, JPG | 10 MB | — |
| Adjunto de chat (RF-MSG-002) | A definir | 5 MB | — |

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
- Código inválido (se informa cuántos intentos quedan; máximo 3)
- Código expirado (10 minutos; habilita "Reenviar código")
- Cuenta bloqueada temporalmente (3 intentos fallidos → bloqueo de 15 minutos)

---

## 3.4 Login Aspirante

Campos:

- Email
- Contraseña
- Recuérdame

Acciones:

- Ingresar
- Recuperar contraseña

Mensajes de bloqueo: tras 3 intentos fallidos consecutivos la cuenta se bloquea 6 horas. Mensaje: "Tu cuenta está bloqueada por 6 horas. Contacta al admin si necesitas acceso urgente." 

---

## 3.5 Recuperar Contraseña

Paso 1:

- Email

Paso 2:

- Código (10 minutos de expiración)

Paso 3:

- Nueva contraseña (mínimo 8 caracteres, mayúscula, número y carácter especial)

---

## 3.6 Login Personal @usal.edu.ar (Académico, Secretaría, Revisor)

Botón único: "Ingresar con Google".

Estados:

- Error de dominio o usuario inexistente/inactivo: "Usuario no registrado en el sistema. Contacta al admin." (no se crea sesión)

---

## 3.7 Login Admin

Campos:

- Usuario
- Contraseña

Sesión de 1 día. Sin OAuth.

---

# 4. ROL ASPIRANTE

---

## 4.0 Principio de diseño: la interfaz depende del estado

Dentro de una solicitud, el contenido principal que ve el aspirante **no es una pantalla fija**: es un **Panel de Acción** que se reemplaza según el estado (`SOL-INI`, `DOC-RECH`, `CUPON`, etc.). Ejemplo: cuando la solicitud está en `SOL-INI`, el aspirante ve una tabla grande con los documentos que tiene que subir; en `RES-HORA` ve horarios para elegir; en `CUPON` ve el cupón y el cargador de comprobante.

Reglas (aplican a todas las interfaces de 4.6):

1. Cada uno de los 16 estados tiene su interfaz `ASP-<ESTADO>`.
2. Si el aspirante debe hacer algo, la interfaz es de tipo **A (Acción)**. Si otro actor tiene la pelota, es de tipo **E (Espera)**. Si el trámite terminó, es **T (Terminal)** (ver 0.16.3).
3. Una interfaz de espera **nunca** tiene botones de trámite; siempre informa qué pasó, qué sigue y quién está a cargo.
4. El aspirante **nunca cambia el estado manualmente**. Botones como "Enviar documentación" ejecutan una acción; la transición la hace el sistema.
5. Los modales de confirmación del aspirante son confirmaciones simples (1 modal, 1 botón "Confirmar"). La "doble verificación" de RF-AUTH-006 aplica a acciones de Académico, Secretaría y Admin.
6. El frontend resuelve la interfaz con el registro de 0.16.2, usando el código de estado.

### 4.0.1 Matriz general de interfaces del aspirante

| Estado | Tipo | Interfaz | Qué ve / qué hace el aspirante | Transición que dispara | Req. |
|---|---|---|---|---|---|
| — (antes de existir) | A | `ASP-NUEVA` | Completa el formulario inicial (datos personales precargados + datos académicos) | crea la solicitud → `SOL-INI` | RF-ASP-001 |
| `SOL-INI` | A | `ASP-SOL-INI` | Tabla grande de documentos a subir; envía la documentación | → `DOC-CAR` (automático) | RF-ASP-002 |
| `DOC-CAR` | E | `ASP-DOC-CAR` | Documentación en revisión (solo lectura) | — | RF-ASP-003 |
| `DOC-RECH` | A | `ASP-DOC-RECH` | Recarga selectiva: solo los documentos marcados ❌ | → `DOC-CAR` (automático) | RF-ASP-004 |
| `ANAL-COMPL` | E | `ASP-ANAL-COMPL` | Espera con 2 variantes: A) reunión pendiente · B) reunión realizada, en revisión legal | — | RF-ASP-003 |
| `RES-HORA` | A | `ASP-RES-HORA` | Elige un horario de reunión o indica "Ninguno me queda" | ninguna (el Académico confirma → `RES-CONF`) | RF-ASP-005 |
| `RES-CONF` | E | `ASP-RES-CONF` | Reunión confirmada: fecha, hora, link Meet | — | RF-ASP-003 |
| `REV-LEG` | E | `ASP-REV-LEG` | Revisión legal completada, esperando decisión | — | RF-ASP-003 |
| `APROBADO` | E | `ASP-APROBADO` | Informe de materias (solo lectura) | — | RF-ASP-006 |
| `ENV-SECREC` | E | `ASP-ENV-SECREC` | Secretaría prepara el cupón | — | RF-ASP-003 |
| `CUPON` | A | `ASP-CUPON` | Descarga el cupón y carga el comprobante de pago | → `PAG-ESP` (automático) | RF-ASP-007 |
| `PAG-ESP` | E | `ASP-PAG-ESP` | Pago en verificación | — | RF-ASP-003 |
| `PAG-RECH` | A | `ASP-PAG-RECH` | Ve el motivo del rechazo y reenvía el comprobante | → `PAG-ESP` (automático) | RF-ASP-007 |
| `PAG-VER` | E | `ASP-PAG-VER` | Pago verificado, se prepara la disposición | — | RF-ASP-003 |
| `DISP-GEN` | E | `ASP-DISP-GEN` | Disposición generada, pendiente de cierre | — | RF-ASP-003 |
| `FINAL` | T | `ASP-FINAL` | Expediente finalizado, descarga del ZIP | — | RF-ASP-008 |
| `RECHAZO` | T | `ASP-RECHAZO` | Solicitud rechazada, con motivo | — | RF-ASP-003 |

---

## 4.1 Navegación del aspirante

Menú lateral:

- Inicio (dashboard)
- Nueva solicitud
- Mis solicitudes
- Mi perfil

Barra superior (2.1): notificaciones, perfil, cerrar sesión.

Precondición: si la cuenta no validó el código de email (3.3), no puede acceder a "Nueva solicitud". Se lo redirige a la pantalla de validación.

---

## 4.2 Dashboard Aspirante (Inicio)

Pantalla principal. Organiza las solicitudes según lo que se espera del aspirante.

### Bloques

1. **Requieren tu acción**: solicitudes en estados de tipo A (`SOL-INI`, `DOC-RECH`, `RES-HORA`, `CUPON`, `PAG-RECH`).
2. **En curso**: solicitudes en estados de tipo E.
3. **Finalizadas**: solicitudes en `FINAL` o `RECHAZO`.

### Tarjeta de solicitud

- Código de expediente
- Carrera destino y universidad origen
- Badge de estado (2.10)
- Responsable académico y administrativo (o "Esperando que alguien tome el proceso")
- Última actualización
- Botón contextual según estado

### Botón contextual por estado

| Estado | Texto del botón | Destino |
|---|---|---|
| `SOL-INI` | Cargar documentación | `ASP-SOL-INI` |
| `DOC-RECH` | Recargar documentos | `ASP-DOC-RECH` |
| `RES-HORA` | Elegir horario | `ASP-RES-HORA` |
| `CUPON` | Pagar | `ASP-CUPON` |
| `PAG-RECH` | Reenviar comprobante | `ASP-PAG-RECH` |
| `FINAL` | Descargar expediente | `ASP-FINAL` |
| resto | Ver solicitud | interfaz del estado |

### Acciones globales

- Nueva solicitud

### Estados de UI

- Loading: skeleton de tarjetas
- Vacío (sin solicitudes): mensaje y botón "Nueva solicitud"
- Error de carga: mensaje con reintento

---

## 4.3 Nueva Solicitud (`ASP-NUEVA`)

**Requisito:** RF-ASP-001. **Acceso:** menú lateral → "Nueva solicitud".

Formulario en dos secciones. Aún no existe la solicitud ni el estado.

### Sección A – Datos personales (precargados, solo lectura)

- Nombre completo
- DNI
- Email
- Teléfono

Texto de ayuda: "¿Algún dato es incorrecto? Editalo en Mi perfil." (link a Mi perfil).

### Sección B – Formulario inicial (datos académicos)

| Campo | Tipo | Obligatorio |
|---|---|---|
| Universidad de origen | Texto | Sí |
| Facultad de origen | Texto | Sí |
| Carrera de origen | Texto | Sí |
| Carrera destino | Dropdown con las carreras activas del catálogo (se envía el código) | Sí |
| Condición académica | Selector (valores a definir, ver Anexo A) | Sí |
| Observaciones | Textarea | No |

### Acciones

- **Cancelar**: vuelve a Mis solicitudes (si hay datos cargados, pide confirmación).
- **Crear solicitud**: habilitado con el formulario válido. Abre el modal `ASP-NUEVA-M1`.

### Modal `ASP-NUEVA-M1` – Confirmar creación

- Resumen de todos los datos cargados
- Carrera destino destacada
- Botones: "Volver a editar" / "Confirmar creación"

### Resultado

- El sistema crea la solicitud con ID tipo `EXP-2024-001`, estado `SOL-INI`, sin responsable.
- Toast de éxito: "Tu solicitud fue creada. Esperando que alguien tome el proceso."
- Se redirige **directamente a la solicitud**, ya en la interfaz `ASP-SOL-INI` (el aspirante cae en la tabla de documentos, no en un listado).

### Estados de UI

- Loading del catálogo de carreras
- Catálogo vacío: mensaje "No hay carreras disponibles. Contactá a la facultad." y botón de crear deshabilitado
- Error de creación: mensaje y datos conservados en el formulario

---

## 4.4 Mis Solicitudes

**Acceso:** menú lateral → "Mis solicitudes".

### Listado (tabla en desktop, tarjetas en mobile)

Columnas:

- Código de expediente
- Carrera destino
- Universidad origen
- Estado (badge, 2.10)
- Responsables
- Última actualización
- Indicador "Requiere tu acción" (punto o etiqueta, cuando el estado es tipo A)

### Filtros

- Activas / Finalizadas / Todas
- Por estado

### Orden por defecto

1. Primero las que requieren acción del aspirante
2. Luego por última actualización descendente

### Acción

- Click en una fila → abre el detalle de la solicitud (4.5), en la pestaña "Mi trámite" con la interfaz que corresponde al estado.

### Estados de UI

- Loading (skeleton), vacío ("Todavía no iniciaste ninguna solicitud" + botón), error de carga

---

## 4.5 Detalle de Solicitud (contenedor común)

Es el marco que se repite en **todos** los estados. Lo único que cambia entre estados es el contenido de la pestaña "Mi trámite" (Panel de Acción, 4.6).

### 4.5.1 Estructura

De arriba hacia abajo:

1. **Encabezado**: código de expediente, badge de estado, carrera destino, universidad origen, fecha de creación, última actualización.
2. **Stepper de etapas** (2.9).
3. **Bloque de responsables** (RF-ASP-003):
   - Responsable Académico: nombre, email clickeable, fecha de asignación.
   - Responsable Administrativo: ídem (si aplica).
   - Si no hay ninguno: "Esperando que alguien tome el proceso".
   - Si hay ambos, se muestran los dos.
4. **Banner de estado** (2.7).
5. **Pestañas**:

| Pestaña | Visible en | Contenido |
|---|---|---|
| **Mi trámite** (por defecto) | Siempre | Panel de Acción del estado (4.6) |
| **Documentación** | Siempre | Listado de documentos con marcas (4.7.1) |
| **Chat** | Siempre | Chat de la solicitud (4.7.2). Con contador de no leídos |
| **Historial** | Siempre | Timeline de cambios (4.7.3) |
| **Informe de materias** | Desde `APROBADO` hasta `FINAL` | Informe de equivalencias (4.7.4) |

### 4.5.2 Etapa del stepper según el estado

Tabla de configuración (no hardcodear en componentes):

| Etapa | Estados que la activan |
|---|---|
| Documentación | `SOL-INI`, `DOC-CAR`, `DOC-RECH` |
| Análisis | `ANAL-COMPL` con `reunion_realizada = false` |
| Reunión | `RES-HORA`, `RES-CONF` |
| Revisión legal | `ANAL-COMPL` con `reunion_realizada = true`, `REV-LEG` |
| Resolución | `APROBADO` |
| Pago | `ENV-SECREC`, `CUPON`, `PAG-ESP`, `PAG-RECH`, `PAG-VER` |
| Disposición | `DISP-GEN` |
| Finalizado | `FINAL` |

`RECHAZO`: el stepper queda cortado en la etapa donde ocurrió y muestra "Rechazado".

Nota: como `DOC-RECH` puede alcanzarse desde varias etapas (análisis, reunión, revisión legal) y siempre vuelve a `DOC-CAR`, el stepper puede retroceder a "Documentación". Es esperado.

### 4.5.3 Reglas del contenedor

- Al abrir la solicitud (por listado, dashboard o link de mail) siempre se abre "Mi trámite".
- El Panel de Acción se resuelve por código de estado y flags (0.16). Nunca por texto.
- Toda interfaz de tipo E o T es de solo lectura.
- Si el estado cambia con la pantalla abierta, aplica 0.16.4.

---

## 4.6 Interfaces del aspirante por estado

Cada subsección describe lo que aparece en la pestaña **Mi trámite** cuando la solicitud está en ese estado. Se mantiene siempre el contenedor de 4.5 (encabezado, stepper, responsables, banner, pestañas).

---

### 4.6.1 `ASP-SOL-INI` – Carga documental inicial

**Estado:** `SOL-INI` – Solicitud iniciada · **Tipo:** A · **Requisito:** RF-ASP-002

**Cuándo se muestra:** la solicitud acaba de crearse y el aspirante todavía no envió documentación.

**Banner:** "Cargá tu documentación para iniciar el análisis."
Si no hay responsable: se agrega "Todavía nadie tomó tu proceso. Podés cargar tu documentación igual."

**Indicadores (arriba de la tabla):**

- Archivos cargados: X
- Tamaño utilizado: X MB de 50 MB (barra de progreso)
- Recordatorio de límites: PDF o JPG, máximo 10 MB por archivo

**Tabla grande de documentos a subir**

Filas iniciales (obligatorias): 1 Analítico, 1 Plan de estudios, 1 Programa.

| Columna | Contenido |
|---|---|
| Tipo | Analítico / Plan de estudios / Programa, con etiqueta "Obligatorio" |
| Descripción | Analítico y Plan: texto fijo. Programa: campo "Materia" (obligatorio) |
| Archivo | Uploader (2.11). Una vez cargado: nombre del archivo |
| Tamaño | MB del archivo |
| Estado de la fila | Sin cargar / Subiendo (progreso) / Cargado / Error (con motivo) |
| Acciones | Ver, Reemplazar, Quitar |

Controles de la tabla:

- Botón **"+ Agregar otro programa"** (una fila nueva de tipo Programa por materia; sin límite de filas salvo los 50 MB totales).
- Las filas de Programa agregadas pueden eliminarse; las obligatorias iniciales solo se vacían.

**Acciones**

- **Enviar documentación** (botón primario). Habilitado solo cuando están cargados Analítico, Plan de estudios y al menos un Programa (con su materia completa) y no hay errores. Si está deshabilitado, un texto de ayuda lista lo que falta (ej. "Falta: Plan de estudios").

**Validaciones (por fila, inline)**

- Formato: "Formato no permitido. Usá PDF o JPG."
- Tamaño individual: "El archivo supera los 10 MB."
- Tamaño total: "Superaste el límite total de 50 MB."

**Modal `ASP-SOL-INI-M1` – Confirmar envío**

- Resumen: tipo, materia (si aplica), nombre de archivo y tamaño de cada documento
- Aviso: "Una vez enviada, no vas a poder modificar la documentación salvo que el equipo académico te pida recargarla."
- Botones: "Volver" / "Confirmar envío"

**Resultado**

- El sistema pasa automáticamente a `DOC-CAR` (el aspirante no cambia el estado).
- Cada documento queda registrado con revisión ⏳ pendiente.
- Toast: "Documentación enviada." y el panel pasa a `ASP-DOC-CAR`.

**Estados de UI:** skeleton de la tabla al abrir; error de subida por fila con "Reintentar"; sesión expirada durante la carga (se informa y se pide volver a ingresar sin perder la selección si es posible).

---

### 4.6.2 `ASP-DOC-CAR` – Documentación en revisión

**Estado:** `DOC-CAR` – Documentación cargada · **Tipo:** E · **Requisito:** RF-ASP-003

**Cuándo se muestra:** tras enviar la documentación inicial, o tras una recarga (`DOC-RECH` → `DOC-CAR`).

**Banner:** "Tu documentación está en revisión. Te avisamos por mail si necesitamos algo más."
Sin responsable: "Esperando que alguien tome el proceso."

**Contenido:** tabla de documentos en **solo lectura** (2.8):

- Tipo / descripción, archivo, versión, fecha de carga, tamaño
- Marca de revisión: ⏳ pendiente o ✅ correcto
- Descargar

Cuando viene de una recarga:

- Los documentos ✅ conservan la marca.
- Los recargados se muestran ⏳ con etiqueta "Recargado (v2)".

**Acciones:** ninguna de trámite. Disponibles: descargar y abrir Chat (si hay responsable).

---

### 4.6.3 `ASP-DOC-RECH` – Recarga selectiva de documentos

**Estado:** `DOC-RECH` – Documentación rechazada · **Tipo:** A · **Requisito:** RF-ASP-004

**Cuándo se muestra:** el Académico marcó documentos con ❌. Es el mismo panel sin importar el origen (análisis preliminar, "requiere cambios" post-reunión u observaciones de la revisión legal). Lo que cambia es el motivo escrito por el Académico.

**Banner (variante atención):** "Necesitamos que recargues algunos documentos."

**Bloque de motivo:** motivo general escrito por el Académico, con fecha y nombre.

**Resumen:** "N documentos a recargar · M correctos".

**Tabla con TODOS los documentos** (2.8, modo carga):

| Columna | Contenido |
|---|---|
| Tipo / descripción | Documento |
| Archivo actual | Nombre, versión, fecha |
| Marca | ✅ o ❌ (no debe haber ⏳ en este estado) |
| Comentario del Académico | Solo en filas ❌ |
| Acción | Depende de la marca (ver abajo) |

Comportamiento por fila:

- **✅ Correcto:** fila bloqueada (ícono de candado, tooltip "Este documento está correcto y no se puede reemplazar"). Solo permite Descargar.
- **❌ Debe recargar:** fila destacada, comentario visible, botón **"Cargar nuevo archivo"** (uploader). Con archivo nuevo seleccionado: nombre y tamaño, "Quitar" para deshacer. Enlace **"Ver versión anterior"**.
- **❌ Documento faltante** (requerido agregado por el Académico, sin archivo): se muestran el tipo y la descripción pedidos, y el botón **"Cargar archivo"**.

**Indicadores:** "Recargados: X de N" y tamaño de los archivos a enviar (límites de 2.11).

**Acciones**

- **Enviar documentación**: habilitado solo cuando **todos** los documentos ❌ tienen archivo nuevo.

**Modal `ASP-DOC-RECH-M1` – Confirmar envío**

- Lista de documentos nuevos a enviar
- Aviso: "Los documentos correctos no se modifican."
- Botones: "Volver" / "Confirmar envío"

**Resultado**

- Estado → `DOC-CAR` (automático).
- Cada archivo recargado se guarda como nueva versión con revisión ⏳. La versión anterior queda archivada y visible en "Ver versión anterior".
- Panel → `ASP-DOC-CAR`.

**Estados de UI:** igual que `ASP-SOL-INI` (errores por fila, reintento, sesión expirada).

---

### 4.6.4 `ASP-ANAL-COMPL` – Análisis completado (espera, 2 variantes)

**Estado:** `ANAL-COMPL` – Análisis preliminar completado · **Tipo:** E · **Requisito:** RF-ASP-003

Este estado se usa dos veces en el proceso: antes de la reunión y después de una reunión con resultado "Permite inscripción". La interfaz se decide con el flag `reunion_realizada`.

**Variante A (`reunion_realizada = false`):**

- Banner: "Tu análisis preliminar está completo. El próximo paso es coordinar una reunión."
- Texto: "El equipo académico te va a proponer horarios. Te avisamos por mail."
- Stepper en "Análisis".

**Variante B (`reunion_realizada = true`):**

- Banner: "La reunión se realizó. Tu solicitud continúa con la revisión legal."
- Stepper en "Revisión legal".

**Contenido común:** resumen de documentos en solo lectura (con marcas) y acceso a Chat.

**Acciones:** ninguna de trámite.

---

### 4.6.5 `ASP-RES-HORA` – Selección de horario de reunión

**Estado:** `RES-HORA` – Esperando selección de horario · **Tipo:** A · **Requisito:** RF-ASP-005

**Cuándo se muestra:** el Académico propuso horarios (hasta 7, duración 1 hora). Hay hasta **2 rondas**.

**Banner:** "Elegí un horario para tu reunión." con indicador **"Ronda X de 2"**.

La interfaz tiene 4 variantes según el estado interno de la coordinación:

#### Variante a) Elegir horario

- Lista de horarios propuestos como tarjetas seleccionables (selección única): día y fecha, hora de inicio y fin, duración.
- Estado de cada horario: Disponible / Descartado (de una ronda anterior).
- Botón primario **"Confirmar elección"** (deshabilitado hasta seleccionar).
- Botón secundario **"Ninguno me queda"**.

**Modal `ASP-RES-HORA-M1` – Confirmar elección:** fecha y hora elegidas + "Tu elección queda pendiente de confirmación del Académico."

**Modal `ASP-RES-HORA-M2` – Ninguno me queda:**

- En ronda 1: "Se habilitará una nueva propuesta de horarios (ronda 2)."
- En ronda 2: "Vamos a coordinar la reunión por chat con tu Académico."

#### Variante b) Elección enviada, esperando confirmación

- Tarjeta del horario elegido destacada: "Elegiste [fecha/hora]. Esperando la confirmación del Académico."
- Los demás horarios en gris.
- Botón "Cambiar elección" (mientras el Académico no confirme; ver Anexo A).

#### Variante c) Esperando nueva propuesta (ronda 2)

- Se llega cuando el aspirante indicó "Ninguno me queda" en la ronda 1, o el Académico rechazó la elección y va a proponer nuevos horarios.
- Mensaje: "Estamos esperando una nueva propuesta del Académico."
- Cuando llegan los horarios de la ronda 2, se vuelve a la variante a) con "Ronda 2 de 2". Los horarios de la ronda 1 quedan en un acordeón "Horarios anteriores".

#### Variante d) Coordinación manual por chat

- Se llega cuando tras 2 rondas no hay acuerdo.
- Banner: "No logramos coincidir con los horarios. Coordiná la reunión por chat con tu Académico."
- Botón **"Abrir chat"**. No se muestran tarjetas de horarios.
- El Académico registra el horario acordado y confirma.

**Resultado:** las acciones del aspirante en este estado **no cambian el estado**. El estado pasa a `RES-CONF` cuando el Académico confirma (llega mail).

---

### 4.6.6 `ASP-RES-CONF` – Reunión confirmada

**Estado:** `RES-CONF` – Reunión confirmada · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Tu reunión está confirmada."

**Contenido:**

- Tarjeta destacada: fecha, hora, duración (1 hora), Académico responsable
- Botón **"Unirme a la reunión"** con el link de Meet (si está cargado)
- Mientras la fecha no pasó: "Te esperamos el [fecha] a las [hora]."
- Cuando la fecha ya pasó y el estado sigue igual: "La reunión ya se realizó. Estamos registrando el resultado."

**Acciones:** ninguna de trámite. Disponible: Chat.

---

### 4.6.7 `ASP-REV-LEG` – Revisión legal completada

**Estado:** `REV-LEG` – Revisión legal completada · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Revisamos tu documentación legal sin observaciones. Estamos definiendo la resolución final."

**Contenido:** resumen de documentos en solo lectura. Chat.

**Acciones:** ninguna.

---

### 4.6.8 `ASP-APROBADO` – Solicitud aprobada e informe de materias

**Estado:** `APROBADO` – Aprobado · **Tipo:** E (con contenido informativo) · **Requisito:** RF-ASP-006

**Banner (variante éxito):** "¡Tu solicitud fue aprobada!" + "Próximo paso: la enviamos a Secretaría para generar tu cupón de pago."

**Contenido:** el **Informe de materias** (mismo componente que la pestaña de 4.7.4), en solo lectura:

- Tabla: materia reconocida, carga horaria, correlativas
- Cantidad de materias a adeudar
- Observaciones
- Monto a pagar

**Acciones:** ninguna de trámite.

---

### 4.6.9 `ASP-ENV-SECREC` – Enviado a Secretaría

**Estado:** `ENV-SECREC` – Enviado a secretaría · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Secretaría está preparando tu cupón de pago."

**Contenido:**

- Resumen "Monto a pagar" (del informe)
- Responsable Administrativo (o "Esperando que Secretaría tome el proceso")
- Acceso a la pestaña Informe de materias

**Acciones:** ninguna.

---

### 4.6.10 `ASP-CUPON` – Efectuar pago

**Estado:** `CUPON` – Cupón generado · **Tipo:** A · **Requisito:** RF-ASP-007

**Banner:** "Tu cupón de pago está listo. Descargalo, pagá y subí el comprobante."

**Layout en 3 pasos:**

1. **Descargar cupón**: tarjeta con fecha de generación, monto y botón "Descargar cupón (PDF)".
2. **Pagar**: texto informativo "Realizá el pago con los datos del cupón." (sin acción en el sistema).
3. **Cargar comprobante**: uploader único (2.11), PDF o JPG, máximo 10 MB, con vista previa. Botón **"Enviar comprobante"**, deshabilitado hasta tener un archivo válido.

**Modal `ASP-CUPON-M1` – Confirmar envío:** nombre y tamaño del archivo + "Secretaría va a verificar tu pago." Botones "Volver" / "Confirmar envío".

**Resultado:** estado → `PAG-ESP` (automático). Panel → `ASP-PAG-ESP`.

**Estados de UI:** error al descargar el cupón (reintento), error de formato/tamaño, error de subida.

---

### 4.6.11 `ASP-PAG-ESP` – Pago en verificación

**Estado:** `PAG-ESP` – Esperando verificación de pago · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Estamos verificando tu pago."

**Contenido:**

- Comprobante enviado: nombre, fecha y tamaño, con "Ver" y "Descargar"
- Cupón (descargable) con su monto
- Responsable Administrativo

**Acciones:** ninguna (no se puede reenviar mientras se verifica).

---

### 4.6.12 `ASP-PAG-RECH` – Pago rechazado, reenviar comprobante

**Estado:** `PAG-RECH` – Pago rechazado · **Tipo:** A · **Requisito:** RF-ASP-007

**Banner (variante atención):** "Hay un problema con tu comprobante. Revisá el detalle y volvé a enviarlo."

**Contenido:**

- Tarjeta **"Comentario de Secretaría"**: texto, fecha y quién lo escribió
- Comprobante anterior en solo lectura (nombre, fecha, "Ver")
- Cupón (descargable) con su monto, para poder cotejar
- Uploader de nuevo comprobante (PDF o JPG, máximo 10 MB)
- Botón **"Reenviar comprobante"**, deshabilitado hasta tener un archivo válido

**Modal `ASP-PAG-RECH-M1` – Confirmar reenvío:** nombre y tamaño del archivo nuevo. Botones "Volver" / "Confirmar reenvío".

**Resultado:** estado → `PAG-ESP` (automático). Panel → `ASP-PAG-ESP`.

---

### 4.6.13 `ASP-PAG-VER` – Pago verificado

**Estado:** `PAG-VER` – Pago verificado · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner (variante éxito):** "Tu pago fue verificado. Gracias."
Texto: "Secretaría está preparando la disposición decanal."

**Acciones:** ninguna.

---

### 4.6.14 `ASP-DISP-GEN` – Disposición generada

**Estado:** `DISP-GEN` – Disposición generada · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "La disposición decanal fue generada. El Académico la va a revisar y cerrar tu expediente."

**Contenido:** el aspirante todavía no accede a la disposición (se incluye en el expediente descargable al finalizar).

**Acciones:** ninguna.

---

### 4.6.15 `ASP-FINAL` – Expediente finalizado

**Estado:** `FINAL` – Expediente finalizado · **Tipo:** T · **Requisito:** RF-ASP-008

**Banner (variante éxito, destacado):** "¡Tu trámite de equivalencias fue completado!"

**Contenido:**

- Estado final, fecha de finalización, código de expediente
- Lista de lo que contiene el expediente: solicitud inicial, documentos cargados, matriz de equivalencias, horario propuesto, disposición decanal, comprobante de pago
- Botón **"Descargar expediente (.zip)"**. Nombre del archivo: `EXP-2024-001_Expediente_[DNI].zip`

**Acciones:** descargar el ZIP. El Informe de materias, el Historial y el Chat (solo lectura) siguen accesibles.

---

### 4.6.16 `ASP-RECHAZO` – Solicitud rechazada

**Estado:** `RECHAZO` – Rechazado · **Tipo:** T · **Requisito:** RF-ASP-003

**Banner (variante cierre negativo):** "Lamentablemente tu solicitud no pudo ser aprobada."

**Contenido:**

- Motivo (comentario del Académico), fecha y quién resolvió
- Historial completo

**Acciones:** ninguna de trámite. Chat en solo lectura. No hay Informe de materias en este estado.

---

## 4.7 Pestañas transversales del detalle (según estado)

Las pestañas que no son "Mi trámite" se mantienen en todos los estados, pero su comportamiento cambia. **La carga y recarga de archivos se hace únicamente desde "Mi trámite"**, no desde otras pestañas (evita dos lugares para la misma acción).

### 4.7.1 Pestaña Documentación

Tabla de todos los documentos de la solicitud (2.8, solo lectura): tipo, archivo, versión, fecha, tamaño, marca de revisión, comentario, descarga, y acceso a versiones anteriores.

| Estado | Comportamiento |
|---|---|
| `SOL-INI` | Lista los archivos ya seleccionados/cargados. Enlace "Ir a cargar documentación" hacia `ASP-SOL-INI` |
| `DOC-RECH` | Muestra marcas ✅/❌ y comentarios. Enlace "Ir a recargar" hacia `ASP-DOC-RECH` |
| `DOC-CAR` a `DISP-GEN` | Solo lectura |
| `FINAL` | Solo lectura + botón "Descargar expediente (.zip)" |
| `RECHAZO` | Solo lectura |

Vacío: "Todavía no cargaste documentos."

### 4.7.2 Pestaña Chat

Componente 2.5. Participantes: aspirante, Académico responsable, Administrativo responsable (RF-MSG-001).

| Situación | Comportamiento |
|---|---|
| Sin responsable (`SOL-INI` sin nadie que tomó el proceso, o baja/revocación) | Chat **deshabilitado**: "El chat se habilita cuando alguien tome tu proceso." |
| Con responsable, estados `SOL-INI` a `DISP-GEN` | Chat completo |
| `RES-HORA` en coordinación manual (variante d) | Chat completo, destacado desde el banner |
| `FINAL` y `RECHAZO` | Solo lectura |

Reglas del mensaje (RF-MSG-002): avatar, nombre, texto de hasta 500 caracteres (con contador), adjunto opcional de hasta 5 MB, tipo de consulta (Documentación, Estado, Cálculos, Fechas, Otro), fecha y hora, indicador de visto.

Si cambia el responsable, el chat no se borra: el historial se conserva.

### 4.7.3 Pestaña Historial

Componente Timeline (2.3), orden descendente por fecha:

- Fecha y hora
- Estado anterior → estado nuevo (nombre y código)
- Usuario que lo produjo
- Comentario (si hay)
- También: cambios de responsable y documentación cargada (RF-PROC-002)

Es la única pestaña con contenido idéntico en todos los estados.

### 4.7.4 Pestaña Informe de materias

**Visible desde `APROBADO` hasta `FINAL`** (RF-ASP-006). No existe en `RECHAZO` ni en estados anteriores a la aprobación.

Contenido (solo lectura):

- Tabla: materia reconocida, carga horaria, correlativas
- Cantidad a adeudar
- Observaciones
- Monto a pagar

Es el mismo componente que se muestra dentro de `ASP-APROBADO`.

---

## 4.8 Datos mínimos que el backend debe exponer para renderizar cada estado

Para que el frontend pueda resolver las interfaces de 4.6:

| Dato | Se usa en |
|---|---|
| `estado_codigo` | Todas |
| `reunion_realizada` (boolean) | `ASP-ANAL-COMPL`, stepper |
| Responsable académico / administrativo (nombre, email, fecha de asignación, o nulo) | Bloque de responsables, chat |
| Lista de documentos con: tipo, descripción, archivo, versión, versión anterior, `estado_revision`, comentario, `es_requerido_faltante` | `ASP-SOL-INI`, `ASP-DOC-CAR`, `ASP-DOC-RECH`, pestaña Documentación |
| Motivo general de la observación (comentario del Académico) y quién/cuándo | `ASP-DOC-RECH`, `ASP-RECHAZO` |
| Ronda actual (1 o 2), horarios propuestos con su estado, horario elegido, indicador de coordinación manual | `ASP-RES-HORA` |
| Fecha, hora y link de la reunión confirmada | `ASP-RES-CONF` |
| Informe de materias (materia, carga horaria, correlativas, cantidad a adeudar, observaciones, monto) | `ASP-APROBADO`, pestaña Informe |
| Cupón (fecha, monto, archivo) | `ASP-ENV-SECREC` a `ASP-PAG-RECH` |
| Comprobante enviado y comentario de Secretaría | `ASP-PAG-ESP`, `ASP-PAG-RECH` |
| Fecha de finalización y URL del ZIP | `ASP-FINAL` |
| Motivo de rechazo | `ASP-RECHAZO` |
| Historial de estados y de responsables | Pestaña Historial |

---

## 4.9 Notificaciones por mail y destino al abrir el link

Los mails son solo avisos (RF-ESTADO-002). Cada link debe llevar a la solicitud, en "Mi trámite", con la interfaz del estado vigente.

| Aviso (transición) | Interfaz que ve el aspirante |
|---|---|
| `DOC-CAR` → `ANAL-COMPL`: "Tu solicitud está siendo analizada" | `ASP-ANAL-COMPL` (variante A) |
| → `DOC-RECH` (desde `DOC-CAR`, `RES-CONF` o `ANAL-COMPL`): "Observaciones en tu documentación" (con motivo y lista de documentos a recargar) | `ASP-DOC-RECH` |
| `ANAL-COMPL` → `RES-HORA`: "Elegí un horario para la reunión" | `ASP-RES-HORA` |
| `RES-HORA` → `RES-CONF`: "Tu reunión fue confirmada" | `ASP-RES-CONF` |
| `RES-CONF` → `ANAL-COMPL`: "La reunión se realizó" | `ASP-ANAL-COMPL` (variante B) |
| `RES-CONF` o `REV-LEG` → `RECHAZO` | `ASP-RECHAZO` |
| `REV-LEG` → `APROBADO` | `ASP-APROBADO` |
| `ENV-SECREC` → `CUPON`: "Tu cupón de pago está listo" | `ASP-CUPON` |
| `PAG-ESP` → `PAG-VER` | `ASP-PAG-VER` |
| `PAG-ESP` → `PAG-RECH`: "Hay un problema con tu comprobante" | `ASP-PAG-RECH` |
| `DISP-GEN` → `FINAL`: "Tu trámite fue completado" | `ASP-FINAL` |
| Reasignación de responsable (RF-ADMIN-005): "Tu proceso ha sido reasignado" | Interfaz del estado actual, con responsables actualizados |

La campana de la barra superior (2.1) replica estos avisos dentro del sistema.

---

## 4.10 Regresiones, reaperturas y cambios de responsable

- **Vueltas a `DOC-RECH`:** puede ocurrir hasta tres veces en una misma solicitud (análisis, post-reunión, revisión legal). Cada vez el aspirante ve `ASP-DOC-RECH` con las marcas y motivo vigentes. Tras recargar, siempre vuelve a `DOC-CAR`.
- **Reapertura por Admin** (`FINAL` → estado elegido, RF-ADMIN-006): el panel se resuelve con el nuevo estado. El banner suma la aclaración "Tu expediente fue reabierto", tomada de la última entrada del Historial.
- **Baja o revocación del responsable:** el bloque de responsables vuelve a "Esperando que alguien tome el proceso" y el chat se deshabilita. El Panel de Acción **no cambia**, porque lo que el aspirante debe hacer depende del estado, no del responsable.

---

# 5. ROL ACADÉMICO

---

## 5.0 Principio de diseño

Igual que en el rol Aspirante (4.0), el Panel de Acción del Académico se resuelve por estado: interfaces `ACAD-<ESTADO>`.

Reglas adicionales del rol:

- Un Académico es **participante** de una solicitud si tiene permiso de proceso (permanente por carrera o temporal por solicitud, RF-AUTH-005). Si no, es **observador**: ve las mismas interfaces con todos los botones de acción **deshabilitados** y tooltip "No tenés permiso de proceso sobre esta solicitud".
- Además de ser participante, debe ser el **responsable** de la solicitud para actuar: si no hay responsable, la única acción disponible es "Tomar proceso" (5.3). El estado no cambia al tomar el proceso.
- Las acciones que cambian el estado usan **doble verificación** (RF-AUTH-006): 1er click abre el modal, 2do click ejecuta.

### 5.0.1 Matriz general de interfaces del Académico

| Estado | Tipo | Interfaz | Qué hace el Académico | Transición que dispara | Req. |
|---|---|---|---|---|---|
| `SOL-INI` | E | `ACAD-SOL-INI` | Espera la documentación. Puede tomar el proceso | — | RF-PROC-003 |
| `DOC-CAR` | A | `ACAD-DOC-CAR` | Revisa documentos (✅/❌), arma matriz y horario; completa análisis o requiere correcciones | → `ANAL-COMPL` o `DOC-RECH` | RF-ACAD-001, 009 |
| `DOC-RECH` | E | `ACAD-DOC-RECH` | Espera la recarga del aspirante | — | — |
| `ANAL-COMPL` | A | `ACAD-ANAL-COMPL` | Hub con 2 acciones: (A) coordinar reunión, (B) revisión legal (según `reunion_realizada`) | → `RES-HORA`, `REV-LEG` o `DOC-RECH` | RF-ACAD-002, 005 |
| `RES-HORA` | A | `ACAD-RES-HORA` | Espera elección del aspirante; confirma horario o propone nueva ronda | → `RES-CONF` | RF-ACAD-003 |
| `RES-CONF` | A | `ACAD-RES-CONF` | Registra el resultado de la reunión | → `ANAL-COMPL`, `DOC-RECH` o `RECHAZO` | RF-ACAD-004 |
| `REV-LEG` | A | `ACAD-REV-LEG` | Decisión final: aprobar o rechazar | → `APROBADO` o `RECHAZO` | RF-ACAD-006 |
| `APROBADO` | A | `ACAD-APROBADO` | Envía a Secretaría | → `ENV-SECREC` | RF-ACAD-007 |
| `ENV-SECREC` | E | `ACAD-ENV-SECREC` | Seguimiento: Secretaría genera el cupón | — | — |
| `CUPON` | E | `ACAD-CUPON` | Seguimiento: espera el pago del aspirante | — | — |
| `PAG-ESP` | E | `ACAD-PAG-ESP` | Seguimiento: Secretaría verifica el pago | — | — |
| `PAG-RECH` | E | `ACAD-PAG-RECH` | Seguimiento: espera reenvío del aspirante | — | — |
| `PAG-VER` | E | `ACAD-PAG-VER` | Seguimiento: Secretaría confecciona la disposición | — | — |
| `DISP-GEN` | A | `ACAD-DISP-GEN` | Revisa la disposición y cierra el expediente | → `FINAL` | RF-ACAD-008 |
| `FINAL` | T | `ACAD-FINAL` | Consulta y exportación | — | RF-PROC-005 |
| `RECHAZO` | T | `ACAD-RECHAZO` | Consulta y exportación | — | RF-PROC-005 |

---

## 5.1 Dashboard Académico

Widgets:

- **Mis solicitudes**, agrupadas por "Requieren mi acción" (estados A del Académico: `DOC-CAR`, `ANAL-COMPL`, `RES-HORA`, `RES-CONF`, `REV-LEG`, `APROBADO`, `DISP-GEN`) y "En espera de otros"
- **Sin responsable** (con botón "Tomar proceso")
- **Pendientes**
- **Reuniones próximas** (solicitudes en `RES-CONF` con fecha futura)

Filtros rápidos.

---

## 5.2 Listado de Solicitudes

**Requisito:** RF-PROC-001.

Tabla. Columnas:

- ID
- Aspirante
- Universidad origen
- Carrera destino
- Estado (badge con código)
- Responsable actual
- Última actualización
- Botones: "Ver detalles", "Tomar proceso" (si aplica)

Filtros: estado (16 estados), fecha, búsqueda por ID / aspirante / carrera, "Mis asignadas" / "Todas".

Ordenamiento:

- Si el usuario tiene carreras asignadas (permisos permanentes activos): primero las solicitudes de esas carreras agrupadas por carrera, luego el resto alfabéticamente por carrera.
- Si no tiene carreras asignadas: todas alfabéticamente por carrera.

---

## 5.3 Modal Tomar Proceso

**Requisito:** RF-PROC-003. **Doble verificación.**

- Información: solicitud y aspirante
- Texto: "Sos responsable de este proceso a partir de ahora."
- Botón "Confirmar"

Resultado: el usuario queda asignado como responsable académico y el estado **no cambia**. Para los demás usuarios con acceso a esa carrera el botón "Tomar proceso" desaparece.

---

## 5.4 Detalle de Solicitud (contenedor común)

Se repite en todos los estados.

1. **Encabezado**: código, badge de estado, carrera destino, universidad origen.
2. **Bloque de responsables**: académico y administrativo, con fechas de asignación.
3. **Botones de responsabilidad**:
   - "Tomar proceso" (si no hay responsable y el usuario es participante)
   - "Darse de baja" (5.10)
4. **Banner de estado** (2.7).
5. **Pestañas**:

| Pestaña | Función |
|---|---|
| **Acción actual** (por defecto) | Panel de Acción del estado (5.6) |
| Información General | Datos del aspirante, académicos, responsables, estado, fechas clave |
| Documentación | Consulta de documentos con marcas (5.7) |
| Análisis | Consulta de matriz y horario cargados |
| Reunión | Consulta de horarios propuestos, elegido y resultado |
| Chat | Chat de la solicitud (2.5); modo lectura si es observador |
| Historial | Timeline (2.3) |
| Auditoría | Tabla de auditoría de la solicitud (solo con permiso especial o admin) |

Regla: las pestañas Documentación, Análisis y Reunión son de **consulta**. Si el estado permite una acción sobre lo que muestran, incluyen un botón "Ir a la acción" que lleva a "Acción actual". Así cada acción vive en un solo lugar.

---

## 5.5 Pestaña Información General

- Datos personales del aspirante (nombre, email, teléfono, DNI)
- Datos académicos (universidad, facultad y carrera de origen, carrera destino, condición académica, observaciones)
- Responsables
- Estado actual
- Fechas clave (creación, última modificación, último cambio de estado)

---

## 5.6 Interfaces del Académico por estado

### 5.6.1 `ACAD-SOL-INI` – Esperando documentación (E)

- Banner: "El aspirante todavía no envió su documentación."
- Contenido: datos de la solicitud y botón "Tomar proceso" (si corresponde).
- Sin acciones de trámite.

---

### 5.6.2 `ACAD-DOC-CAR` – Análisis preliminar y revisión de documentos (A)

**Requisitos:** RF-ACAD-001, RF-ACAD-009. **Estado:** `DOC-CAR`.

**Banner:** "Documentación lista para analizar." Si viene de una recarga: "El aspirante recargó documentos" y se resaltan los recargados.

**Bloque 1 – Revisión de documentos** (componente 2.8 en modo revisión, ver 5.7).

**Bloque 2 – Matriz tentativa:** cargar archivo o escribir notas.

**Bloque 3 – Horario propuesto:** cargar archivo o escribir notas.

**Acciones**

- **Guardar marcas** (no cambia el estado).
- **Completar análisis** → modal `ACAD-DOC-CAR-M1` (doble verificación) → estado `ANAL-COMPL`.
  - Marcar documentos es opcional en esta acción.
- **Requerir correcciones** → modal `ACAD-DOC-CAR-M2` (doble verificación) → estado `DOC-RECH`.
  - Campo comentario general obligatorio.
  - Requiere al menos un documento ❌ y ningún documento ⏳.
  - El modal resume la lista de documentos a recargar.

**Deshabilitado si:** es observador o no es responsable.

---

### 5.6.3 `ACAD-DOC-RECH` – Esperando recarga del aspirante (E)

- Banner: "Esperando que el aspirante recargue los documentos marcados."
- Contenido: tabla en solo lectura con las marcas y comentarios enviados, y el motivo general.
- Sin acciones de trámite.

---

### 5.6.4 `ACAD-ANAL-COMPL` – Hub: reunión o revisión legal (A)

**Requisitos:** RF-ACAD-002, RF-ACAD-005. **Estado:** `ANAL-COMPL`.

Muestra dos tarjetas de acción. Su habilitación depende de `reunion_realizada`:

| Tarjeta | `reunion_realizada = false` | `reunion_realizada = true` |
|---|---|---|
| **A) Coordinar reunión** | Habilitada → 5.6.4.1 | Oculta/deshabilitada ("La reunión ya se realizó") |
| **B) Revisión legal** | Deshabilitada, tooltip "Requiere reunión realizada con resultado 'Permite inscripción'" | Habilitada → 5.6.4.2 |

#### 5.6.4.1 `ACAD-ANAL-COMPL-PROPUESTA` – Propuesta de horarios

- Calendario integrado, próximos 14-30 días, franjas 10:00-18:00.
- Crear, editar y eliminar horarios. Máximo **7** propuestas, 1 hora cada una.
- Contador "X de 7".
- Botón **"Guardar propuesta"** → modal `ACAD-ANAL-COMPL-M1` (confirmar envío) → estado `RES-HORA`. Se envía mail al aspirante.

#### 5.6.4.2 `ACAD-ANAL-COMPL-LEGAL` – Revisión legal

- Documentación en solo lectura, con enlaces de descarga
- Verificaciones: validez de firmas electrónicas y concordancia entre documentos y análisis
- Carga de archivo de observaciones (opcional)
- Decisión (radio): **Sin observaciones** / **Con observaciones (requiere cambios)**
- Si "Con observaciones": comentario obligatorio y marcado de documentos (al menos un ❌, ninguno ⏳), con el componente de 5.7
- Botón **"Guardar revisión"** → modal `ACAD-ANAL-COMPL-M2` (doble verificación):
  - Sin observaciones → `REV-LEG`
  - Con observaciones → `DOC-RECH`
- Aclaración visible: aprobar o rechazar la solicitud **no se decide acá** sino en `REV-LEG`.

---

### 5.6.5 `ACAD-RES-HORA` – Coordinación de horario (A, con variantes)

**Requisito:** RF-ACAD-003. **Estado:** `RES-HORA`.

Banner: "Ronda X de 2".

| Variante | Qué muestra | Acciones |
|---|---|---|
| **Esperando elección del aspirante** | Horarios propuestos y estado de cada uno | Ninguna (o editar propuesta, a definir; ver Anexo A) |
| **Aspirante eligió un horario** | Modal/tarjeta: "El aspirante eligió [fecha/hora]. ¿Confirmás?" | **Confirmar** (doble verificación, `ACAD-RES-HORA-M1`) → `RES-CONF` · **Rechazar / Proponer nuevos** → nueva ronda; el estado permanece en `RES-HORA` |
| **Aspirante respondió "Ninguno me queda"** | Aviso y calendario de propuesta | Proponer nueva ronda (máx. 2) |
| **2 rondas sin acuerdo** | Aviso "Coordinación manual por chat" y botón al chat | **Registrar horario acordado y confirmar** (doble verificación) → `RES-CONF` |

---

### 5.6.6 `ACAD-RES-CONF` – Resultado de la reunión (A)

**Requisito:** RF-ACAD-004. **Estado:** `RES-CONF`.

Encabezado con fecha, hora y link de Meet de la reunión confirmada.

Formulario:

- Checkbox: **Reunión realizada** / **Aspirante no se presentó**
- Notas de la reunión (textarea)
- Link de Meet (opcional)
- Decisión (radio): **Permite inscripción** / **Requiere cambios en matriz** / **Rechaza solicitud**
- Si "Requiere cambios": comentario obligatorio y marcado de documentos (5.7)
- Si "Rechaza": comentario con el motivo (visible para el aspirante en el mail)

Botón **"Guardar resultado"** → modal `ACAD-RES-CONF-M1` (doble verificación):

| Decisión | Efecto |
|---|---|
| Permite inscripción | `reunion_realizada = true` → `ANAL-COMPL` |
| Requiere cambios | → `DOC-RECH` |
| Rechaza solicitud | → `RECHAZO` |

Si marca "No se presentó": ver Anexo A (no hay transición definida en la tabla de RF-ESTADO-001).

---

### 5.6.7 `ACAD-REV-LEG` – Decisión final (A)

**Requisito:** RF-ACAD-006. **Estado:** `REV-LEG`.

- Resumen de la solicitud: matriz, documentación, resultado de reunión y revisión legal
- Decisión (radio): **Aprobar** / **Rechazar** (comentario obligatorio con el motivo)
- Botón **"Guardar decisión"** → modal `ACAD-REV-LEG-M1` (doble verificación):
  - Aprobar → `APROBADO`
  - Rechazar → `RECHAZO`

---

### 5.6.8 `ACAD-APROBADO` – Envío a Secretaría (A)

**Requisito:** RF-ACAD-007. **Estado:** `APROBADO`.

- Resumen de equivalencias aprobadas
- Botón **"Enviar a secretaría"** → modal `ACAD-APROBADO-M1` (doble verificación) → `ENV-SECREC`

---

### 5.6.9 `ACAD-ENV-SECREC`, `ACAD-CUPON`, `ACAD-PAG-ESP`, `ACAD-PAG-RECH`, `ACAD-PAG-VER` – Seguimiento (E)

Cinco interfaces de espera. Mismo layout, cambia el banner:

| Interfaz | Banner |
|---|---|
| `ACAD-ENV-SECREC` | "Secretaría está generando el cupón de pago." |
| `ACAD-CUPON` | "Cupón generado. Esperando el pago del aspirante." |
| `ACAD-PAG-ESP` | "El aspirante envió su comprobante. Secretaría lo está verificando." |
| `ACAD-PAG-RECH` | "Pago rechazado por Secretaría. Esperando que el aspirante reenvíe el comprobante." |
| `ACAD-PAG-VER` | "Pago verificado. Secretaría está preparando la disposición decanal." |

Contenido común: responsable administrativo, datos de pago en solo lectura, chat. Sin acciones de trámite.

---

### 5.6.10 `ACAD-DISP-GEN` – Cierre de expediente (A)

**Requisito:** RF-ACAD-008. **Estado:** `DISP-GEN`.

- Visor de la disposición decanal generada por Secretaría
- Archivos finales del expediente
- Botón **"Cierre y finalización"** → modal `ACAD-DISP-GEN-M1` (doble verificación) → `FINAL`

---

### 5.6.11 `ACAD-FINAL` y `ACAD-RECHAZO` – Terminales (T)

- Banner de cierre (éxito o negativo, con motivo).
- Solo lectura de todas las pestañas.
- Exportación del expediente (5.9).
- En `FINAL`, el Admin puede reabrir (7.13).

---

## 5.7 Componente de revisión de documentos (marcas ✅ / ❌)

**Requisito:** RF-ACAD-009. Es el modo "Revisión" de 2.8, usado en `ACAD-DOC-CAR`, `ACAD-ANAL-COMPL-LEGAL` y `ACAD-RES-CONF`.

Por cada documento:

- Archivo (descarga, versión, fecha, tamaño) y versiones anteriores
- Marca: ⏳ pendiente / ✅ correcto / ❌ debe recargar
- Comentario por documento (visible al aspirante; se sugiere completarlo en los ❌)
- Etiqueta "Recargado" en los documentos que el aspirante reemplazó

Controles:

- **"Agregar documento requerido faltante"**: tipo y descripción (ej. "Programa de Análisis Matemático II"). Queda como ❌ sin archivo.
- **"Guardar marcas"**: guarda sin cambiar el estado.
- Contadores: ✅ X · ❌ Y · ⏳ Z.

Reglas visuales:

- Para **Requerir correcciones** / decisiones que pasan a `DOC-RECH`: se exige al menos un ❌ y ninguno ⏳. Si no se cumple, el botón queda deshabilitado con explicación.
- Marcar es opcional en las acciones que avanzan el proceso.
- Cada cambio de marca se audita (lo hace el backend).
- Los observadores ven las marcas pero no pueden cambiarlas.
- Cuando el aspirante recarga (`DOC-RECH` → `DOC-CAR`), se resaltan los recargados (⏳) y se mantienen los ✅; el Académico puede cambiar cualquier marca.

---

## 5.8 Pestaña Chat

Componente 2.5.

- Participantes con escritura: aspirante y responsables (académico y administrativo).
- Observadores: solo lectura.
- Si no hay responsable, el aspirante no puede iniciar el chat (RF-MSG-001).

---

## 5.9 Exportación de Expedientes

**Requisito:** RF-PROC-005.

Opciones:

- Descargar expediente individual (ZIP)
- Descargar múltiples (filtros por estado y fecha)
- Exportación consolidada: `Expedientes_2024_[fecha].zip`

Cada descarga queda registrada en auditoría.

---

## 5.10 Modal Darse de Baja

**Requisito:** RF-PROC-004. **Doble verificación.**

- Información: consecuencias y estado actual
- Texto: "¿Querés darte de baja? El proceso vuelve a estar disponible."
- Si sos el único usuario con acceso a esa carrera en el proceso: botón deshabilitado, tooltip "No podés darte de baja. Sos el único responsable. Contactá al admin."

---

# 6. ROL SECRETARÍA ADMINISTRATIVA

---

## 6.0 Principio de diseño

Secretaría comparte con el Académico los permisos, la bandeja, el detalle y el mecanismo de "Tomar proceso" (RF-PROC-001 a 005). Lo específico son las etapas administrativas (4.4.3). Las interfaces se resuelven por estado: `SEC-<ESTADO>`.

Reglas:

- Es **participante** solo con permiso de proceso (permanente o temporal) y actúa como **responsable administrativo** de la solicitud. Si no hay responsable administrativo, la acción disponible es "Tomar proceso" (mismo modal que 5.3).
- Los observadores ven las mismas interfaces con botones deshabilitados.
- Las acciones que cambian el estado usan doble verificación.

### 6.0.1 Matriz general de interfaces de Secretaría

| Estado | Tipo | Interfaz | Qué hace Secretaría | Transición | Req. |
|---|---|---|---|---|---|
| `SOL-INI` a `APROBADO` | E | `SEC-SEGUIMIENTO` | Seguimiento de la solicitud; puede tomar el proceso administrativo | — | RF-PROC-003 |
| `ENV-SECREC` | A | `SEC-ENV-SECREC` | Gestión de pago: genera el cupón | → `CUPON` | RF-SEC-001 |
| `CUPON` | E | `SEC-CUPON` | Espera el pago del aspirante | — | — |
| `PAG-ESP` | A | `SEC-PAG-ESP` | Verifica el comprobante | → `PAG-VER` o `PAG-RECH` | RF-SEC-002 |
| `PAG-RECH` | E | `SEC-PAG-RECH` | Espera el reenvío del aspirante | — | — |
| `PAG-VER` | A | `SEC-PAG-VER` | Confecciona la disposición decanal | → `DISP-GEN` | RF-SEC-003 |
| `DISP-GEN` | E | `SEC-DISP-GEN` | Espera el cierre del Académico | — | — |
| `FINAL` | A (sin cambio de estado) | `SEC-FINAL` | Archivo administrativo | ninguna | RF-SEC-004 |
| `RECHAZO` | T | `SEC-RECHAZO` | Consulta | — | — |

---

## 6.1 Dashboard Secretaría

Widgets:

- **Pendientes de generar cupón** (`ENV-SECREC`)
- **Pagos pendientes de verificar** (`PAG-ESP`)
- **Disposiciones pendientes** (`PAG-VER`)
- **Pendientes de archivo administrativo** (`FINAL` sin archivar)
- **Mis solicitudes**

---

## 6.2 Listado de Solicitudes

Misma estructura que Académico (5.2).

---

## 6.3 Detalle de Solicitud (contenedor común)

Misma estructura que Académico (5.4): encabezado, responsables, banner de estado, pestañas ("Acción actual" + Información General, Documentación, Análisis, Reunión, Chat, Historial, Auditoría).

---

## 6.4 Interfaces de Secretaría por estado

### 6.4.1 `SEC-SEGUIMIENTO` – Seguimiento previo a la gestión de pago (E)

**Estados:** `SOL-INI`, `DOC-CAR`, `DOC-RECH`, `ANAL-COMPL`, `RES-HORA`, `RES-CONF`, `REV-LEG`, `APROBADO`.

- Banner con el estado actual y a quién le toca ("En etapa académica").
- Datos de la solicitud y botón "Tomar proceso" (como responsable administrativo, si corresponde).
- Sin acciones de trámite.

---

### 6.4.2 `SEC-ENV-SECREC` – Gestión de pago: generar cupón (A)

**Requisito:** RF-SEC-001. **Estado:** `ENV-SECREC`.

Banner: "Solicitud aprobada lista para gestión de pago."

Sección "Gestión de Pago":

- ID de la solicitud y datos del aspirante
- Monto (prellenado según política; editable solo si negocio lo permite, ver Anexo A)
- Informe de materias en solo lectura

Acciones:

- **Generar cupón** → vista previa del PDF (`SEC-ENV-SECREC-M1`) → botón "Confirmar generación" (doble verificación: 1er click muestra preview, 2do ejecuta) → estado `CUPON`.

Resultado: se genera y guarda el PDF, se envía mail al aspirante.

---

### 6.4.3 `SEC-CUPON` – Esperando pago (E)

- Banner: "Cupón generado. Esperando el comprobante del aspirante."
- Contenido: cupón generado (descargable), monto.
- Sin acciones.

---

### 6.4.4 `SEC-PAG-ESP` – Verificación de pago (A)

**Requisito:** RF-SEC-002. **Estado:** `PAG-ESP`.

Banner: "El aspirante cargó su comprobante." Si viene de un reenvío: "El aspirante reenvió el comprobante."

Contenido:

- Visor del comprobante (imagen o PDF, descargable)
- Monto del cupón y monto reportado (si existe, ver Anexo A)
- Campo de comentarios (obligatorio si el pago es incorrecto)
- Decisión (radio): **Pago correcto** / **Pago incorrecto / monto no coincide**

Acciones:

- **Guardar verificación** → modal `SEC-PAG-ESP-M1` (doble verificación):
  - Correcto → `PAG-VER`
  - Incorrecto → `PAG-RECH` (el comentario llega al aspirante por mail)

---

### 6.4.5 `SEC-PAG-RECH` – Esperando reenvío (E)

- Banner: "Pago rechazado. Esperando que el aspirante reenvíe el comprobante."
- Contenido: comprobante rechazado y comentario enviado, en solo lectura.
- Sin acciones.

---

### 6.4.6 `SEC-PAG-VER` – Disposición decanal (A)

**Requisito:** RF-SEC-003. **Estado:** `PAG-VER`.

Banner: "Pago verificado. Falta la disposición decanal."

Opciones (excluyentes):

- **Subir PDF** generado externamente (uploader 2.11)
- **Generar desde plantilla del sistema**

Acción:

- **Generar disposición** → modal `SEC-PAG-VER-M1` (doble verificación) → estado `DISP-GEN`. Se envía mail al Académico.

---

### 6.4.7 `SEC-DISP-GEN` – Esperando cierre (E)

- Banner: "Disposición generada. El Académico la va a revisar y cerrar el expediente."
- Contenido: disposición en solo lectura.
- Sin acciones.

---

### 6.4.8 `SEC-FINAL` – Archivo administrativo (A, sin cambio de estado)

**Requisito:** RF-SEC-004. **Estado:** `FINAL`.

- Banner: "Expediente finalizado. Falta el archivo administrativo."
- Campos: número de expediente final (ej. `EXP-2024-001-E3`), archivo de la copia de la disposición.
- Estado archivado: pendiente / archivado.
- Acción: **Guardar archivo administrativo**. **No cambia el estado** (`FINAL` es terminal). Se registra en auditoría.

---

### 6.4.9 `SEC-RECHAZO` – Terminal (T)

Solo lectura y exportación.

---

## 6.5 Exportación de Expedientes

Mismas opciones que 5.9: individual, múltiple y consolidada.

---

# 7. ROL ADMIN GENERAL

---

## 7.0 Alcance

El Admin tiene todos los permisos. En el detalle de una solicitud puede ver y ejecutar las mismas interfaces de Académico (5.6) y Secretaría (6.4) según el estado (bypass de permisos), además de las funciones administrativas de este punto. Los cambios de responsable y las acciones críticas requieren doble verificación.

---

## 7.1 Dashboard Admin

Widgets:

- 🔴 Procesos sin responsable académico (con enlace directo a "Asignar responsable")
- 🔴 Procesos sin responsable administrativo (idem)
- 🟡 Cuentas bloqueadas (con botón "Desbloquear")
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

- Email @usal.edu.ar
- Nombre
- Rol (Académico, Administrativo, Revisor Logs)

Acción: Crear cuenta. La cuenta se crea con acceso básico (sin carreras). No se envía mail de confirmación: el usuario entra con Google.

---

## 7.4 Editar Usuario

Permite modificar datos, activar y desactivar.

---

## 7.5 Gestión de Permisos

Debe mostrar por usuario:

- Carreras asignadas (permisos permanentes activos)
- Permisos temporales por solicitud (con vencimiento, si tiene)
- Permisos revocados (historial)

Acciones: asignar, revocar.

---

## 7.6 Modal Asignación de Permisos

Campos:

- Usuario
- Carrera(s) (selección múltiple)

Nota: **ya no existe "nivel de permiso"** (eliminado en v8). Un permiso activo equivale a ser participante.

---

## 7.7 Modal Revocación

Debe mostrar:

- Procesos activos afectados
- Consecuencias

Si el usuario tiene procesos activos en la carrera: mensaje "Proceso activo. Para revocar, asigne responsable a otro usuario primero." y opción de **forzar la revocación** con doble verificación ("Estoy seguro, revocar igual"). Aplica también a permisos temporales.

---

## 7.8 Gestión de Carreras

Tabla:

- Código
- Nombre
- Facultad
- Estado (activa / inactiva)

Acciones: crear, editar, desactivar (baja lógica).

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
- Usuarios disponibles (sin filtrar por carrera)

Permite:

- Asignar académico
- Asignar administrativo

---

## 7.12 Modal Asignación Manual

Debe contemplar:

- Checkbox "Este usuario no tiene acceso a esta carrera. ¿Crear permiso temporal?" (cuando corresponde)
- Comentario obligatorio (motivo)

Doble verificación. Se avisa por mail a los nuevos responsables, al anterior y al aspirante.

---

## 7.13 Pantalla Reapertura de Expediente

Aplica a solicitudes en `FINAL`.

Debe mostrar:

- Estado actual
- Selector de estado destino (cualquier estado del catálogo, definido por negocio)

Campo: motivo.

---

## 7.14 Modal Reapertura

Doble verificación. Tras reabrir, la solicitud se muestra a cada rol con la interfaz del estado elegido (ver 4.10).

---

## 7.15 Auditoría General

Tabla:

- Timestamp
- Usuario
- Acción
- Entidad
- Detalles
- IP

Filtros: usuario, acción, fecha, entidad. Los estados se muestran siempre por **código**.

---

## 7.16 Detalle de Evento de Auditoría

Debe mostrar:

- Datos completos
- Estados involucrados (código anterior y nuevo)
- Comentarios

---

## 7.17 Perfil Admin

Secciones:

- Información
- Seguridad
- Historial de accesos
- Sesiones activas

---

## 7.18 Cambio de Email

Formulario dedicado. Valida dominio @usal.edu.ar. Doble verificación.

---

## 7.19 Cambio de Contraseña

Formulario dedicado. Doble verificación.

---

## 7.20 Cuentas Bloqueadas

Listado de cuentas bloqueadas con hora de fin del bloqueo y botón **Desbloquear**. Al desbloquear se resetea el contador de intentos. Ver Anexo A (contradicción sobre doble verificación).

---

# 8. ROL REVISOR DE LOGS

---

## 8.1 Dashboard Revisor

Widgets:

- Solicitudes recientes
- Cambios recientes
- Actividad del sistema

---

## 8.2 Listado de Solicitudes

Solo lectura. Sin "Tomar proceso".

---

## 8.3 Detalle de Solicitud

Solo lectura. Muestra, para el estado actual, la interfaz del Académico o de Secretaría (5.6 / 6.4) **con todos los botones de acción deshabilitados**.

Incluye: información, documentación, chat, historial y auditoría.

---

## 8.4 Auditoría General

Misma información que Admin. Sin acciones.

---

## 8.5 Detalle Evento Auditoría

Solo lectura.

---

## 8.6 Perfil Revisor

Solo lectura.

---

# 9. ROL OBSERVADOR

Es un Académico o de Secretaría **sin permiso de proceso** sobre esa solicitud (RF-AUTH-005). Un mismo usuario puede ser participante en una solicitud y observador en otra.

---

## 9.1 Dashboard Observador

Igual que el del rol correspondiente (Académico o Secretaría). Acceso únicamente de consulta sobre solicitudes donde no participa.

---

## 9.2 Listado de Solicitudes

Solo lectura en las solicitudes donde no participa. El botón "Tomar proceso" solo aparece si tiene permiso de carrera o temporal.

---

## 9.3 Detalle de Solicitud

Se muestra la interfaz del estado (5.6 / 6.4) en **modo observador**: todos los botones de acción deshabilitados con tooltip "No tenés permiso de proceso sobre esta solicitud".

Puede visualizar:

- Información
- Documentación y marcas
- Chat (lectura)
- Historial

No puede:

- Escribir en el chat
- Tomar proceso
- Marcar documentos
- Ejecutar ninguna acción de estado

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
- Sin horarios propuestos
- Sin cupón todavía

---

# 11. ESTADOS DE ERROR

Todas las pantallas deben contemplar:

- Error de carga
- Error de permisos
- Archivo inválido (formato)
- Archivo demasiado grande
- Sesión expirada
- Acción no permitida
- Conflicto de estado (la solicitud cambió, ver 0.16.4)

---

# 12. ACCIONES CON DOBLE VERIFICACIÓN

Frontend debe implementar el flujo visual de doble confirmación (1er click abre modal, 2do click ejecuta) para las acciones de RF-AUTH-006 y las etapas de trámite:

**Académico / Secretaría**

- Tomar proceso
- Darse de baja como responsable
- Completar análisis
- Requerir correcciones
- Confirmar horario de reunión
- Guardar resultado de reunión
- Guardar revisión legal
- Guardar decisión final (aprobar / rechazar)
- Enviar a secretaría
- Generar cupón
- Verificar pago
- Generar disposición
- Cierre y finalización

**Admin**

- Cambiar responsable
- Revocar permisos (permanentes o temporales)
- Reabrir expediente
- Cambiar email
- Cambiar contraseña
- Desbloquear cuenta (ver Anexo A)

**Aspirante:** no aplica doble verificación. Sus confirmaciones son modales simples (4.0).

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
- **La interfaz que se muestra depende del estado y de los flags auxiliares (0.16); el estado puede cambiar en vivo.**
- **Un mismo estado puede recorrerse más de una vez (`DOC-RECH`, `ANAL-COMPL`, `PAG-ESP`).**

---

# ANEXO A. PUNTOS ABIERTOS Y SUPUESTOS

Cosas que los requisitos v8 no terminan de definir y que el equipo de frontend necesita para cerrar las interfaces. Lo que está en este documento es el **supuesto adoptado** hasta que negocio confirme.

| # | Tema | Supuesto adoptado en este documento |
|---|---|---|
| 1 | **Datos del formulario inicial:** la tabla `solicitudes` de RF-ASP-001 no tiene columnas para universidad, facultad y carrera de origen, condición académica ni observaciones | Se piden en `ASP-NUEVA`. Backend debe agregar dónde guardarlos |
| 2 | **Valores de "Condición académica"** | Selector con valores a definir |
| 3 | **Documentos obligatorios para enviar en `SOL-INI`:** RF-ASP-002 lista Analítico, Plan y Programas | Obligatorios: Analítico, Plan de estudios y al menos un Programa |
| 4 | **Archivos como borrador:** si el aspirante sube archivos y sale sin enviar, ¿se conservan? | Se asume que sí (cada archivo se sube al elegirlo). Confirmar con backend |
| 5 | **Límite de 50 MB en la recarga** (`DOC-RECH`) | Se aplica a los archivos que se envían en esa recarga |
| 6 | **`RES-HORA`:** ¿el aspirante puede cambiar su elección antes de que el Académico confirme? ¿El Académico puede editar la propuesta antes de que el aspirante elija? | Aspirante: sí puede cambiarla. Académico: no edita una propuesta ya enviada |
| 7 | **"Aspirante no se presentó"** (RF-ACAD-004): dice que el Académico puede volver a coordinar, pero desde `RES-CONF` no hay transición hacia `RES-HORA` en RF-ESTADO-001 | Pendiente. Falta definir la transición |
| 8 | **Estructura del informe de materias:** RF-ACAD-001 dice que la matriz es un archivo o notas libres, pero `ASP-APROBADO` muestra una tabla (materia, carga horaria, correlativas, monto) | Se asume que el backend expone estos datos estructurados. Falta definir cómo los carga el Académico |
| 9 | **Monto reportado por el aspirante:** RF-SEC-002 menciona "Monto reportado", pero RF-ASP-007 no pide que el aspirante lo ingrese | El aspirante solo sube el comprobante |
| 10 | **`RECHAZO`:** ¿el aspirante puede iniciar una nueva solicitud? ¿el chat queda en solo lectura? | Chat en solo lectura. "Nueva solicitud" sigue disponible en el menú |
| 11 | **Varias solicitudes por aspirante:** ¿puede tener más de una activa? | Sí (el dashboard agrupa varias) |
| 12 | **Editar el formulario inicial** después de crear la solicitud | No editable. Los cambios se resuelven por chat |
| 13 | **Desbloqueo de cuenta:** RF-AUTH-006 lo lista como acción con doble verificación, pero RF-AUTH-002 y RF-ADMIN-007 dicen que se hace sin doble verificación | Se implementa **sin** doble verificación hasta que negocio decida |
| 14 | **Monto del cupón:** ¿Secretaría puede modificar el monto prellenado? | Solo lectura por defecto |
| 15 | **Comprobantes rechazados:** ¿se archivan como versión anterior, como los documentos? | Se muestra el anterior en `ASP-PAG-RECH`. Confirmar conservación con backend |
| 16 | **Adjuntos de chat:** formatos permitidos (solo se define el límite de 5 MB) | Mismos formatos que documentación más imágenes |

---

Fin del documento.
