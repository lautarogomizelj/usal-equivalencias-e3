# Frontend Interfaces Specification
## Sistema de Tramitación de Equivalencias Electrónicas (E3)

Versión: 3.0
Documento objetivo: Equipo Frontend
Basado en: Especificación de Requisitos Funcionales y No Funcionales v12.0 (20 estados, 26 transiciones)

A partir del punto 4 las interfaces están definidas **por estado de la solicitud**. Para cada estado en que un rol tiene que hacer algo (o esperar algo) existe una interfaz propia, con su código (`ASP-…`, `ACAD-…`, `SEC-…`). La fuente de verdad de estados, transiciones y notificaciones es `Docs/Sistema_Equivalencias_Requisitos.md`.

Los diseños visuales están en la carpeta compartida de Google Drive **Interfaces**. Cada interfaz indica el archivo de referencia cuando existe. Los puntos que requieren decisión de negocio están en el Anexo A y las diferencias entre el diseño y los requisitos, en el Anexo B.

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-09-28 | lautarogomizelj | Versión inicial: estándares frontend, componentes globales e interfaces por rol, basada en requisitos v5.0 |
| 2.0 | 2026-09-29 | lautarogomizelj | Interfaces reorganizadas por estado de la solicitud (16 estados) con códigos `ASP-…`/`ACAD-…`/`SEC-…`, basadas en requisitos v8.0 |
| 2.1 | 2026-10-13 | lautarogomizelj | Nota de obsolescencia: el documento sigue basado en requisitos v8.0 (16 estados); queda pendiente su re-derivación al modelo vigente (v12.0, 20 estados) |
| 3.0 | 2026-10-31 | jumaluc | Interfaces re-derivadas a los 20 estados y 26 transiciones de requisitos v12.0; referencia a los diseños de Drive y Anexo B con las diferencias entre diseño y requisitos |
| 3.1 | 2026-10-31 | jumaluc | Registro con Nombre y Apellido separados, como el diseño (§3.2 y Anexo B, punto 1) |

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

El sistema E3 es una aplicación **guiada por estado**: una misma solicitud se ve y se comporta distinto en cada uno de sus 20 estados, y distinto para cada rol.

### 0.16.1 Regla general

La interfaz que se muestra dentro de una solicitud se decide por la combinación:

```text
(rol del usuario, ¿participante u observador?, estado_codigo, flags auxiliares)
```

Flags auxiliares definidos hasta ahora: existencia de responsable académico / administrativo. Las rondas de reunión ya no son un flag: cada ronda es un estado (`REU-FEC-ASP-1` y `REU-FEC-ASP-2`).

### 0.16.2 Registro de interfaces por estado

Debe existir un único registro (archivo de configuración) que asocie cada combinación con un componente. Ejemplo conceptual:

```text
stateViews = {
  ASPIRANTE: {
    'SOL-INI':  CargaDocumentalInicial,
    'DOC-REV':  DocumentacionEnRevision,
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
| **T – Terminal** | El trámite terminó (`INSC-RECH`, único estado terminal vigente) | No (solo consulta) |

### 0.16.4 Cambio de estado en vivo y conflictos

- Si el estado cambia mientras el usuario tiene la pantalla abierta (por acción de otro actor, reasignación o reapertura), la interfaz debe refrescarse y avisar: "El estado de la solicitud cambió".
- Si el usuario ejecuta una acción que ya no está permitida (el backend responde conflicto/403), se muestra "Esta acción ya no está disponible porque el estado de la solicitud cambió" y se recarga el panel. Los archivos seleccionados pero no enviados no se pierden en el aviso, pero tampoco se envían.

### 0.16.5 Códigos de interfaz

Cada interfaz tiene un código estable para referenciarla en tickets, QA y backend:

- `ASP-<ESTADO>`: aspirante (ej. `ASP-SOL-INI`)
- `ACAD-<ESTADO>`: académico (ej. `ACAD-DOC-REV`)
- `SEC-<ESTADO>`: secretaría (ej. `SEC-CUPON-SOL`)
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
- **Carga** (aspirante en `SOL-INI`, `DOC-RECH` y `LEG-RECH`)
- **Revisión** (académico participante: puede cambiar marcas y comentarios)

Debe mostrar versiones anteriores de un documento recargado (la versión anterior nunca se borra).

---

## 2.9 Stepper de Etapas

Indicador visual del avance del trámite en macro-etapas: Documentación, Análisis, Reunión, Inscripción, Legalización, Pago, Inicio de E3.

- La etapa activa se deriva del estado mediante una tabla de configuración (ver 4.5).
- Es informativo: no navega ni permite acciones.
- `INSC-RECH` se muestra como cierre negativo.

---

## 2.10 Badge de Estado

Muestra el nombre del estado, con el código disponible como título (`DOC-REV`). Se usa en listados, tarjetas y encabezados. La paleta se define en tokens por tipo de estado (acción, espera, éxito, atención, neutro) y cada código se asocia a un tipo en `constants/estados.ts`.

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

- Nombre
- Apellido
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

Dentro de una solicitud, el contenido principal que ve el aspirante **no es una pantalla fija**: es un **Panel de Acción** que se reemplaza según el estado. Ejemplo: en `SOL-INI` el aspirante ve la carga de documentos; en `REU-FEC-ASP-1` elige una fecha de reunión; en `CUPON-PAG` descarga el cupón y carga el comprobante.

Reglas (aplican a todas las interfaces de 4.6):

1. Cada uno de los 20 estados tiene su interfaz `ASP-<ESTADO>`.
2. Si el aspirante debe hacer algo, la interfaz es de tipo **A (Acción)**. Si otro actor tiene la pelota, es de tipo **E (Espera)**. Si el trámite terminó, es **T (Terminal)** (ver 0.16.3).
3. Una interfaz de espera **nunca** tiene botones de trámite; siempre informa qué pasó, qué sigue y quién está a cargo.
4. El aspirante **nunca cambia el estado manualmente**. Botones como "Enviar documentación" ejecutan una acción; la transición la hace el sistema.
5. Los modales de confirmación del aspirante son confirmaciones simples (1 modal, 1 botón "Confirmar"). La "doble verificación" de RF-AUTH-006 aplica a acciones de Académico, Secretaría y Admin.
6. El frontend resuelve la interfaz con el registro de 0.16.2, usando el código de estado y `accionesDisponibles`.

### 4.0.1 Matriz general de interfaces del aspirante

| Estado | Tipo | Interfaz | Qué ve / qué hace el aspirante | Transición | Req. |
|---|---|---|---|---|---|
| — (antes de existir) | A | `ASP-NUEVA` | Completa el formulario inicial | 1: crea la solicitud → `SOL-INI` | RF-ASP-001 |
| `SOL-INI` | A | `ASP-SOL-INI` | Carga los documentos y los envía | 2: → `DOC-REV` | RF-ASP-002 |
| `DOC-REV` | E | `ASP-DOC-REV` | Documentación en revisión | — | RF-ASP-003 |
| `DOC-RECH` | A | `ASP-DOC-RECH` | Recarga solo los documentos marcados ❌ | 4: → `DOC-REV` | RF-ASP-004 |
| `ANAL-PEND` | E | `ASP-ANAL-PEND` | Documentación aceptada, análisis en curso | — | RF-ASP-003 |
| `REU-FEC-ASP-1` | A | `ASP-REU-FEC-ASP-1` | Elige una fecha o solicita otras | 9: → `REU-PEND` · 7: → `REU-FEC-ACAD` | RF-ASP-005 |
| `REU-FEC-ASP-2` | A | `ASP-REU-FEC-ASP-2` | Elige una fecha o rechaza la ronda | 9: → `REU-PEND` · 10: → `REU-CHAT` | RF-ASP-005 |
| `REU-FEC-ACAD` | E | `ASP-REU-FEC-ACAD` | Espera nuevas fechas del Académico | — | RF-ASP-003 |
| `REU-CHAT` | A | `ASP-REU-CHAT` | Pauta la fecha por chat | Ninguna: la fija el Académico | RF-MSG-001 |
| `REU-PEND` | E | `ASP-REU-PEND` | Reunión pactada, con enlace de Meet | — | RF-ASP-003 |
| `ANAL-POST` | E | `ASP-ANAL-POST` | Análisis posterior a la reunión | — | RF-ASP-003 |
| `INSC-ACEP` | A | `ASP-INSC-ACEP` | Responde si sus documentos están legalizados | 16: → `LEG-REV` · 17: → `LEG-PEND` | RF-ASP-009 |
| `LEG-PEND` | A | `ASP-LEG-PEND` | Avisa que ya legalizó | 18: → `LEG-REV` | RF-ASP-009 |
| `INSC-RECH` | T | `ASP-INSC-RECH` | Inscripción rechazada, con motivo | — | RF-ASP-003 |
| `LEG-REV` | E | `ASP-LEG-REV` | Documentos legalizados en revisión | — | RF-ASP-003 |
| `LEG-RECH` | A | `ASP-LEG-RECH` | Recarga los legalizados marcados ❌ | 20: → `LEG-REV` | RF-ASP-009 |
| `CUPON-SOL` | E | `ASP-CUPON-SOL` | Informe de materias; Secretaría prepara el cupón | — | RF-ASP-006 |
| `CUPON-PAG` | A | `ASP-CUPON-PAG` | Descarga el cupón y carga el comprobante | 23: → `PAG-CTRL` | RF-ASP-007 |
| `PAG-CTRL` | E | `ASP-PAG-CTRL` | Pago en verificación | — | RF-ASP-003 |
| `PAG-REENV` | A | `ASP-PAG-REENV` | Ve el motivo y reenvía el comprobante | 25: → `PAG-CTRL` | RF-ASP-007 |
| `E3-INI` | E | `ASP-E3-INI` | Pago aceptado, inicio de E3 | — | RF-ASP-003 |

`E3-INI` no es terminal: los estados posteriores todavía no están definidos (RF-ESTADO-001).

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

1. **Requieren tu acción**: solicitudes en estados de tipo A (`SOL-INI`, `DOC-RECH`, `REU-FEC-ASP-1`, `REU-FEC-ASP-2`, `REU-CHAT`, `INSC-ACEP`, `LEG-PEND`, `LEG-RECH`, `CUPON-PAG`, `PAG-REENV`).
2. **En curso**: solicitudes en estados de tipo E.
3. **Finalizadas**: solicitudes en `INSC-RECH`.

Diseño: `Interfaces/Aspirantes/Inicio/Dashboard Aspirante 4.1.2.jpg`.

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
| `REU-FEC-ASP-1`, `REU-FEC-ASP-2` | Elegir fecha | Interfaz del estado |
| `REU-CHAT` | Abrir chat | `ASP-REU-CHAT` |
| `INSC-ACEP` | Indicar legalización | `ASP-INSC-ACEP` |
| `LEG-PEND` | Avisar que legalicé | `ASP-LEG-PEND` |
| `LEG-RECH` | Recargar legalizados | `ASP-LEG-RECH` |
| `CUPON-PAG` | Pagar | `ASP-CUPON-PAG` |
| `PAG-REENV` | Reenviar comprobante | `ASP-PAG-REENV` |
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
| **Informe de materias** | Desde `CUPON-SOL` en adelante | Informe de equivalencias (4.7.4) |

### 4.5.2 Etapa del stepper según el estado

Tabla de configuración (no hardcodear en componentes):

| Etapa | Estados que la activan |
|---|---|
| Documentación | `SOL-INI`, `DOC-REV`, `DOC-RECH` |
| Análisis | `ANAL-PEND` |
| Reunión | `REU-FEC-ASP-1`, `REU-FEC-ASP-2`, `REU-FEC-ACAD`, `REU-CHAT`, `REU-PEND` |
| Inscripción | `ANAL-POST`, `INSC-ACEP` |
| Legalización | `LEG-PEND`, `LEG-REV`, `LEG-RECH` |
| Pago | `CUPON-SOL`, `CUPON-PAG`, `PAG-CTRL`, `PAG-REENV` |
| Inicio de E3 | `E3-INI` |

`INSC-RECH`: el stepper queda cortado en Inscripción y muestra "Rechazada".

Nota: la no asistencia a la reunión vuelve de `REU-PEND` a `REU-FEC-ACAD`; el stepper se mantiene en Reunión.

### 4.5.3 Reglas del contenedor

- Al abrir la solicitud (por listado, dashboard o link de mail) siempre se abre "Mi trámite".
- El Panel de Acción se resuelve por código de estado y flags (0.16). Nunca por texto.
- Toda interfaz de tipo E o T es de solo lectura.
- Si el estado cambia con la pantalla abierta, aplica 0.16.4.

---

## 4.6 Interfaces del aspirante por estado

Cada subsección describe lo que aparece en la pestaña **Mi trámite** cuando la solicitud está en ese estado. Se mantiene siempre el contenedor de 4.5.

---

### 4.6.1 `ASP-SOL-INI` – Carga documental inicial

**Estado:** `SOL-INI` · **Tipo:** A · **Requisito:** RF-ASP-002 · **Diseño:** `Mis Solicitudes/Carga de Documentacion 4.6.png`

**Banner:** "Cargá tu documentación para iniciar el análisis." Si no hay responsable: "Todavía nadie tomó tu proceso. Podés cargar tu documentación igual."

**Indicadores:** archivos cargados, tamaño utilizado de 50 MB y límites (PDF o JPG, máximo 10 MB por archivo).

**Lista de documentos:** una fila por documento obligatorio, con uploader (2.11), estado de la fila (sin cargar, subiendo, cargado, error) y acciones ver, reemplazar y quitar. Obligatorios: Analítico, Plan de estudios y al menos un Programa con su materia. Botón **"+ Agregar otro programa"** (una fila por materia).

**Acciones:** **Enviar documentación**, habilitado cuando están los obligatorios y no hay errores. Si está deshabilitado, un texto lista lo que falta.

**Validaciones por fila:** formato, tamaño individual y tamaño total (mensajes de `validations/messages.ts`).

**Modal `ASP-SOL-INI-M1`:** resumen de documentos y aviso "Una vez enviada, no vas a poder modificar la documentación salvo que el equipo académico te pida recargarla." Botones "Volver" / "Confirmar envío".

**Resultado:** el sistema pasa a `DOC-REV`; cada documento queda con revisión ⏳.

---

### 4.6.2 `ASP-DOC-REV` – Documentación en revisión

**Estado:** `DOC-REV` · **Tipo:** E · **Requisito:** RF-ASP-003 · **Diseño:** `Mis Solicitudes/Estado Pendiente de Revision/`

**Banner:** "Tu documentación está en revisión. Te avisamos por mail si necesitamos algo más."

**Contenido:** tabla de documentos en solo lectura (2.8). Si viene de una recarga, los ✅ conservan la marca y los recargados se muestran ⏳ con la etiqueta "Recargado (v2)". También el bloque Documentación académica, vacío hasta que el Académico suba matriz y horario.

**Acciones:** ninguna de trámite.

---

### 4.6.3 `ASP-DOC-RECH` – Recarga selectiva de documentos

**Estado:** `DOC-RECH` · **Tipo:** A · **Requisito:** RF-ASP-004 · **Diseño:** `Mis Solicitudes/Estado Documentación rechazada/`

**Banner (variante atención):** "Necesitamos que recargues algunos documentos."

**Contenido:** motivo general del Académico y tabla con TODOS los documentos (2.8, modo carga):

- **✅ Correcto:** fila bloqueada, solo permite descargar.
- **❌ Debe recargar:** comentario del Académico y botón "Cargar nuevo archivo".
- **❌ Documento faltante:** tipo y descripción pedidos, y botón "Cargar archivo".

**Acciones:** **Enviar documentación**, habilitado cuando **todos** los ❌ tienen archivo nuevo.

**Modal `ASP-DOC-RECH-M1`:** lista de documentos a enviar y aviso "Los documentos correctos no se modifican."

**Resultado:** estado → `DOC-REV`. Cada archivo recargado se guarda como nueva versión con revisión ⏳; la anterior queda archivada.

---

### 4.6.4 `ASP-ANAL-PEND` – Análisis preliminar pendiente

**Estado:** `ANAL-PEND` · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Tu documentación fue aceptada. Tu solicitud está siendo analizada por nuestro equipo académico."

**Contenido:** documentos en solo lectura. **Acciones:** ninguna de trámite.

---

### 4.6.5 `ASP-REU-FEC-ASP-1` – Elegir fecha de reunión (1ª ronda)

**Estado:** `REU-FEC-ASP-1` · **Tipo:** A · **Requisito:** RF-ASP-005 · **Diseño:** `Mis Solicitudes/Estado Pendiente de Fecha de Reunion/` (detalle y selección de fecha)

**Banner:** "Es necesario pactar una fecha de reunión."

**Contenido:** matriz de equivalencias y posible horario de cursada (descargables) y botón **"Seleccionar fecha de reunión"**, que abre el modal `ASP-REU-FEC-M1`.

**Modal `ASP-REU-FEC-M1`:** responsable académico, hasta 7 fechas de 1 hora como tarjetas de **selección única** y la opción **"No puedo en ninguno de estos horarios"** con motivo y detalle de cuándo puede reunirse (obligatorios).

**Resultado:** al elegir fecha → `REU-PEND`, sin confirmación posterior del Académico. Al solicitar otras → `REU-FEC-ACAD`.

---

### 4.6.6 `ASP-REU-FEC-ASP-2` – Elegir fecha de reunión (2ª ronda)

**Estado:** `REU-FEC-ASP-2` · **Tipo:** A · **Requisito:** RF-ASP-005

Igual que 4.6.5 con el indicador "Ronda 2 de 2". En vez de solicitar otras fechas, ofrece **"Rechazar 2ª ronda de fechas"**, que lleva a `REU-CHAT`. No existe una 3ª ronda.

---

### 4.6.7 `ASP-REU-FEC-ACAD` – Esperando nuevas fechas

**Estado:** `REU-FEC-ACAD` · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Estamos esperando nuevas fechas del Académico." Si viene de una inasistencia: "No registramos tu asistencia a la reunión. El Académico va a proponer nuevas fechas."

**Acciones:** ninguna de trámite.

---

### 4.6.8 `ASP-REU-CHAT` – Coordinar fecha por chat

**Estado:** `REU-CHAT` · **Tipo:** A · **Requisito:** RF-MSG-001 · **Diseño:** `Seleccion de fecha de reunion (Por chat).png`

**Banner:** "Es necesario coordinar la fecha de la reunión por chat."

**Contenido:** el detalle se abre en la pestaña **Chat**, por defecto. La fecha la fija el Académico; el aspirante no cambia el estado.

---

### 4.6.9 `ASP-REU-PEND` – Reunión pactada

**Estado:** `REU-PEND` · **Tipo:** E · **Requisito:** RF-ASP-003 · **Diseño:** `Reunion confirmada.png`

**Banner:** "Reunión pactada, pendiente de realizarse."

**Contenido:** fecha, horario, modalidad, responsable académico y botón **"Unirme a la reunión"** cuando el Académico cargó el enlace de Meet. **Acciones:** ninguna de trámite.

---

### 4.6.10 `ASP-ANAL-POST` – Análisis posterior a la reunión

**Estado:** `ANAL-POST` · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "La reunión se realizó. Tu solicitud está siendo analizada para definir tu inscripción."

**Contenido:** versión vigente de la matriz y del horario de cursada. **Acciones:** ninguna de trámite.

---

### 4.6.11 `ASP-INSC-ACEP` – Indicar legalización

**Estado:** `INSC-ACEP` · **Tipo:** A · **Requisito:** RF-ASP-009

**Banner (variante éxito):** "Tu inscripción fue aceptada. Indicá si tus documentos están legalizados."

**Contenido:** pregunta con dos opciones, sin carga de archivos: **"Sí, están legalizados"** (→ `LEG-REV`) y **"No, todavía no"** (→ `LEG-PEND`). Modal de confirmación simple.

---

### 4.6.12 `ASP-LEG-PEND` – Legalización pendiente

**Estado:** `LEG-PEND` · **Tipo:** A · **Requisito:** RF-ASP-009 · **Diseño:** `Estado Documentacion Legalizada/Documentacion legalizada (Envio de docs).png` (ver Anexo B #9)

**Banner:** "Tu documentación aún no está legalizada. Seguí las indicaciones y avisanos cuando esté lista."

**Acciones:** **"Ya legalicé mis documentos"** → `LEG-REV`.

---

### 4.6.13 `ASP-INSC-RECH` – Inscripción rechazada

**Estado:** `INSC-RECH` · **Tipo:** T · **Requisito:** RF-ASP-003

**Banner (variante cierre negativo):** "Lamentablemente tu solicitud no pudo ser aprobada."

**Contenido:** motivo, fecha, quién resolvió e historial. **Acciones:** ninguna. Chat en solo lectura.

---

### 4.6.14 `ASP-LEG-REV` – Documentos legalizados en revisión

**Estado:** `LEG-REV` · **Tipo:** E · **Requisito:** RF-ASP-003 · **Diseño:** `Documentacion legalizada (Pendiente en doc).png`

**Banner:** "Tus documentos legalizados están en revisión."

**Contenido:** documentos en solo lectura, con sus marcas. Secciones plegables Documentación inicial y Documentación académica. **Acciones:** ninguna de trámite.

---

### 4.6.15 `ASP-LEG-RECH` – Recarga de documentos legalizados

**Estado:** `LEG-RECH` · **Tipo:** A · **Requisito:** RF-ASP-009 · **Diseño:** `Documentacion legalizada (Error en doc).png`

Mismo comportamiento que `ASP-DOC-RECH` (4.6.3) para la etapa `LEGALIZACION`. **Resultado:** estado → `LEG-REV`.

---

### 4.6.16 `ASP-CUPON-SOL` – Informe y cupón en preparación

**Estado:** `CUPON-SOL` · **Tipo:** E · **Requisito:** RF-ASP-006

**Banner:** "Tus documentos fueron aceptados. Secretaría está preparando tu cupón de pago."

**Contenido:** Informe de materias (4.7.4), monto y responsable administrativo. **Acciones:** ninguna de trámite.

---

### 4.6.17 `ASP-CUPON-PAG` – Efectuar pago

**Estado:** `CUPON-PAG` · **Tipo:** A · **Requisito:** RF-ASP-007 · **Diseño:** `Estado Pendiente de Comprobante/Detalle de Solicitud Comprobante.png`

**Banner:** "Envío de comprobante de pago requerido."

**Contenido:** tres pasos: descargar el cupón, pagar y cargar el comprobante (PDF o JPG, máximo 10 MB). **Acciones:** **Enviar comprobante**, deshabilitado hasta tener un archivo válido. Modal `ASP-CUPON-PAG-M1` de confirmación simple.

**Resultado:** estado → `PAG-CTRL`.

---

### 4.6.18 `ASP-PAG-CTRL` – Pago en verificación

**Estado:** `PAG-CTRL` · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner:** "Comprobante de pago pendiente de revisión."

**Contenido:** comprobante enviado, cupón con su monto y responsable administrativo. **Acciones:** ninguna.

---

### 4.6.19 `ASP-PAG-REENV` – Reenviar comprobante

**Estado:** `PAG-REENV` · **Tipo:** A · **Requisito:** RF-ASP-007

**Banner (variante atención):** "Comprobante de pago rechazado."

**Contenido:** comentario de Secretaría, comprobante anterior, cupón y uploader. **Acciones:** **Enviar nuevo comprobante**, con modal de confirmación simple.

**Resultado:** estado → `PAG-CTRL`.

---

### 4.6.20 `ASP-E3-INI` – Inicio de E3

**Estado:** `E3-INI` · **Tipo:** E · **Requisito:** RF-ASP-003

**Banner (variante éxito):** "Tu pago ha sido verificado correctamente. Gracias."

**Acciones:** ninguna. Lo posterior se define junto con los estados que siguen a `E3-INI`, incluida la descarga del expediente (RF-ASP-008).

---

## 4.7 Pestañas transversales del detalle (según estado)

Las pestañas que no son "Mi trámite" se mantienen en todos los estados, pero su comportamiento cambia. **La carga y recarga de archivos se hace únicamente desde "Mi trámite"**, para que cada acción viva en un solo lugar.

### 4.7.1 Pestaña Documentación

Tabla de todos los documentos de la solicitud (2.8, solo lectura) y acceso a versiones anteriores.

| Estado | Comportamiento |
|---|---|
| `SOL-INI` | Lista los archivos ya cargados. Enlace "Ir a cargar documentación" |
| `DOC-RECH`, `LEG-RECH` | Muestra marcas y comentarios. Enlace "Ir a recargar" |
| Resto | Solo lectura |

Vacío: "Todavía no cargaste documentos."

### 4.7.2 Pestaña Chat

Componente 2.5. Participantes: aspirante, Académico responsable y Administrativo responsable (RF-MSG-001).

| Situación | Comportamiento |
|---|---|
| Sin responsable | Chat **deshabilitado**: "El chat se habilita cuando alguien tome tu proceso." |
| Con responsable | Chat completo |
| `REU-CHAT` | Chat completo y abierto por defecto |
| `INSC-RECH` | Solo lectura |

Reglas del mensaje (RF-MSG-002): avatar, nombre, texto de hasta 500 caracteres, adjunto opcional de hasta 5 MB, tipo de consulta, fecha y hora, indicador de visto. Si cambia el responsable, el historial se conserva.

### 4.7.3 Pestaña Historial

Componente Timeline (2.3), orden descendente por fecha. Es la única pestaña con contenido idéntico en todos los estados.

### 4.7.4 Pestaña Informe de materias

**Visible desde `CUPON-SOL` en adelante** (RF-ASP-006). No existe en `INSC-RECH`.

Contenido (solo lectura): materias reconocidas, carga horaria, correlativas, cantidad a adeudar, observaciones y monto a pagar.

---

## 4.8 Datos mínimos que el backend debe exponer para renderizar cada estado

Los define el contrato de la API: `SolicitudDetalle` (§6.4), `Documento` (§6.5), `Analisis`, `ReunionInfo`, `Informe` y `Pago` (§6.6). La interfaz se resuelve con `estado.codigo`, `miRelacion` y `accionesDisponibles`.

---

## 4.9 Notificaciones por mail y destino al abrir el link

Los mails son solo avisos (RF-ESTADO-002). Cada link lleva a la solicitud, en "Mi trámite", con la interfaz del estado vigente. La campana de la barra superior replica estos avisos.

---

## 4.10 Regresiones y cambios de responsable

- **Vueltas a `DOC-RECH` y `LEG-RECH`:** tras recargar, la solicitud vuelve a `DOC-REV` o a `LEG-REV`.
- **No asistencia a la reunión:** `REU-PEND` vuelve a `REU-FEC-ACAD` y se reprograma por el flujo normal de rondas.
- **Baja o revocación del responsable:** el bloque de responsables vuelve a "Esperando que alguien tome el proceso" y el chat se deshabilita. El Panel de Acción **no cambia**, porque depende del estado.

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

| Estado | Tipo | Interfaz | Qué hace el Académico | Transición | Req. |
|---|---|---|---|---|---|
| `SOL-INI` | E | `ACAD-SOL-INI` | Espera la documentación. Puede tomar el proceso | — | RF-PROC-003 |
| `DOC-REV` | A | `ACAD-DOC-REV` | Marca documentos; acepta o rechaza | 5: → `ANAL-PEND` · 3: → `DOC-RECH` | RF-ACAD-001, 009 |
| `DOC-RECH` | E | `ACAD-DOC-RECH` | Espera la recarga del aspirante | — | — |
| `ANAL-PEND` | A | `ACAD-ANAL-PEND` | Sube matriz y horario, y propone fechas | 6: → `REU-FEC-ASP-1` | RF-ACAD-002 |
| `REU-FEC-ASP-1`, `REU-FEC-ASP-2` | E | `ACAD-REU-FEC-ASP` | Espera la elección del aspirante | — | — |
| `REU-FEC-ACAD` | A | `ACAD-REU-FEC-ACAD` | Propone la 2ª ronda de fechas | 8: → `REU-FEC-ASP-2` | RF-ACAD-003 |
| `REU-CHAT` | A | `ACAD-REU-CHAT` | Pauta por chat y fija la fecha | 11: → `REU-PEND` | RF-ACAD-003 |
| `REU-PEND` | A | `ACAD-REU-PEND` | Carga el Meet; indica fin o inasistencia | 13: → `ANAL-POST` · 12: → `REU-FEC-ACAD` | RF-ACAD-003, 004 |
| `ANAL-POST` | A | `ACAD-ANAL-POST` | Actualiza análisis; acepta o rechaza la inscripción | 14: → `INSC-ACEP` · 15: → `INSC-RECH` | RF-ACAD-004, 006 |
| `INSC-ACEP`, `LEG-PEND` | E | `ACAD-LEG-ESPERA` | Espera al aspirante | — | — |
| `INSC-RECH` | T | `ACAD-INSC-RECH` | Consulta | — | — |
| `LEG-REV` | A | `ACAD-LEG-REV` | Marca legalizados; acepta o rechaza | 21: → `CUPON-SOL` · 19: → `LEG-RECH` | RF-ACAD-005, 009 |
| `LEG-RECH` | E | `ACAD-LEG-RECH` | Espera la recarga del aspirante | — | — |
| `CUPON-SOL`, `CUPON-PAG`, `PAG-CTRL`, `PAG-REENV`, `E3-INI` | E | `ACAD-SEGUIMIENTO` | Seguimiento de la etapa de pago | — | — |

---

## 5.1 Dashboard Académico

Widgets:

- **Mis solicitudes**, agrupadas por "Requieren mi acción" (estados A del Académico: `DOC-REV`, `ANAL-PEND`, `REU-FEC-ACAD`, `REU-CHAT`, `REU-PEND`, `ANAL-POST`, `LEG-REV`) y "En espera de otros"
- **Sin responsable** (con botón "Tomar proceso")
- **Pendientes**
- **Reuniones próximas** (solicitudes en `REU-PEND` con fecha futura)

Filtros rápidos. Diseño: `Interfaces/Academicos/Inicio/Dashboard Inicio.png`.

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

Filtros: estado (20 estados), fecha, búsqueda por ID / aspirante / carrera, "Mis asignadas" / "Todas".

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

Banner: "El aspirante todavía no envió su documentación." Datos de la solicitud y "Tomar proceso" si corresponde.

---

### 5.6.2 `ACAD-DOC-REV` – Revisión de documentación (A)

**Requisitos:** RF-ACAD-001, RF-ACAD-009. **Diseño:** `Analisis Preliminar/Documentacion Inicial/`

**Banner:** "Documentación lista para revisar." Si viene de una recarga: "El aspirante recargó documentos" y se resaltan los recargados.

**Contenido:** revisión de documentos (5.7).

**Acciones**

- **Guardar marcas** (no cambia el estado).
- **Aceptar documentación** → modal `ACAD-DOC-REV-M1` (doble verificación) → `ANAL-PEND`. Marcar es opcional.
- **Rechazar documentación** → modal `ACAD-DOC-REV-M2` (doble verificación) → `DOC-RECH`. Comentario general obligatorio, al menos un ❌ y ningún ⏳.

---

### 5.6.3 `ACAD-DOC-RECH` – Esperando recarga (E)

Banner: "Esperando que el aspirante recargue los documentos marcados." Tabla en solo lectura y motivo general.

---

### 5.6.4 `ACAD-ANAL-PEND` – Análisis preliminar y fechas (A)

**Requisito:** RF-ACAD-002. **Diseño:** `Analisis Preliminar/Documentos Tentativos y Horarios/`

**Contenido:**

- **Matriz de equivalencias:** archivo o notas.
- **Horario de cursada:** archivo o notas.
- **Fechas disponibles:** hasta 7 horarios de 1 hora, entre 10:00 y 18:00, dentro de los próximos 14 a 30 días.

**Acción:** **Guardar propuesta** → modal `ACAD-ANAL-PEND-M1` (doble verificación) → `REU-FEC-ASP-1`. Se envía mail al aspirante.

---

### 5.6.5 `ACAD-REU-FEC-ASP` – Esperando elección de fecha (E)

Banner: "Ronda X de 2. Esperando que el aspirante elija una fecha." Fechas propuestas en solo lectura.

---

### 5.6.6 `ACAD-REU-FEC-ACAD` – Proponer nuevas fechas (A)

**Requisito:** RF-ACAD-003.

Motivo y disponibilidad del aspirante, o aviso de inasistencia. Calendario de propuesta con las mismas reglas que 5.6.4.

**Acción:** **Sugerir 2ª ronda de fechas** → `REU-FEC-ASP-2`. Si la 2ª ronda ya se usó y el aspirante no se presentó, el comportamiento queda pendiente (Anexo A #7).

---

### 5.6.7 `ACAD-REU-CHAT` – Fijar fecha por chat (A)

**Requisito:** RF-ACAD-003.

Se abre el chat por defecto. **Fijar fecha** abre un modal con la fecha pautada → `REU-PEND`.

---

### 5.6.8 `ACAD-REU-PEND` – Realización de la reunión (A)

**Requisitos:** RF-ACAD-003, RF-ACAD-004. **Diseño:** `Analisis Preliminar/Reunion Pendiente de confirmacion/`

- **Enlace de Meet:** al guardarlo se avisa al aspirante; el estado no cambia.
- **Notas de la reunión:** opcional.
- **Indicar que la reunión finalizó** (doble verificación) → `ANAL-POST`.
- **Indicar que el aspirante no se presentó** (doble verificación) → `REU-FEC-ACAD`.

---

### 5.6.9 `ACAD-ANAL-POST` – Decisión de inscripción (A)

**Requisitos:** RF-ACAD-004, RF-ACAD-006.

- **Actualizar matriz de equivalencias** y **Actualizar horario de cursada**: crean una versión nueva; solo la vigente es visible. Se avisa al aspirante y el estado no cambia.
- Decisión (radio): **Aceptar** / **Rechazar** (comentario obligatorio).
- **Guardar decisión** → modal `ACAD-ANAL-POST-M1` (doble verificación) → `INSC-ACEP` o `INSC-RECH`.

---

### 5.6.10 `ACAD-LEG-ESPERA` – Esperando al aspirante (E)

**Estados:** `INSC-ACEP`, `LEG-PEND`. Banner: "Esperando que el aspirante indique la legalización de sus documentos."

---

### 5.6.11 `ACAD-LEG-REV` – Análisis de documentos legalizados (A)

**Requisitos:** RF-ACAD-005, RF-ACAD-009. **Diseño:** `Analisis Preliminar/Documentacion Legalizada/`

Revisión de documentos (5.7) en etapa `LEGALIZACION`, con carga opcional de un archivo de observaciones.

**Acción:** **Guardar revisión** → modal `ACAD-LEG-REV-M1` (doble verificación):

- Aceptar → `CUPON-SOL`. No puede haber ❌.
- Rechazar → `LEG-RECH`. Comentario obligatorio, al menos un ❌ y ningún ⏳.

---

### 5.6.12 `ACAD-LEG-RECH` – Esperando recarga (E)

Banner: "Esperando que el aspirante recargue los documentos legalizados."

---

### 5.6.13 `ACAD-SEGUIMIENTO` – Etapa de pago (E)

| Estado | Banner |
|---|---|
| `CUPON-SOL` | "Secretaría está generando el cupón de pago." |
| `CUPON-PAG` | "Cupón generado. Esperando el pago del aspirante." |
| `PAG-CTRL` | "El aspirante envió su comprobante. Secretaría lo está verificando." |
| `PAG-REENV` | "Pago rechazado. Esperando que el aspirante reenvíe el comprobante." |
| `E3-INI` | "Pago aceptado. Inicio de E3." |

---

### 5.6.14 `ACAD-INSC-RECH` – Terminal (T)

Banner de cierre con el motivo. Solo lectura de todas las pestañas.

---

## 5.7 Componente de revisión de documentos (marcas ✅ / ❌)

**Requisito:** RF-ACAD-009. Es el modo "Revisión" de 2.8, usado en `ACAD-DOC-REV` y `ACAD-LEG-REV`.

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

- Para rechazar (`DOC-RECH` o `LEG-RECH`): se exige al menos un ❌ y ninguno ⏳. Si no se cumple, el botón queda deshabilitado con explicación.
- Marcar es opcional en las acciones que avanzan el proceso, pero para aceptar legalizados no puede haber ❌.
- Cada cambio de marca se audita (lo hace el backend).
- Los observadores ven las marcas pero no pueden cambiarlas.
- Cuando el aspirante recarga (`DOC-RECH` → `DOC-REV` o `LEG-RECH` → `LEG-REV`), se resaltan los recargados (⏳) y se mantienen los ✅; el Académico puede cambiar cualquier marca.

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
| `SOL-INI` a `LEG-RECH` (sin `INSC-RECH`) | E | `SEC-SEGUIMIENTO` | Seguimiento; puede tomar el proceso administrativo | — | RF-PROC-003 |
| `CUPON-SOL` | A | `SEC-CUPON-SOL` | Genera y envía el cupón | 22: → `CUPON-PAG` | RF-SEC-001 |
| `CUPON-PAG` | E | `SEC-CUPON-PAG` | Espera el pago del aspirante | — | — |
| `PAG-CTRL` | A | `SEC-PAG-CTRL` | Verifica el comprobante | 26: → `E3-INI` · 24: → `PAG-REENV` | RF-SEC-002 |
| `PAG-REENV` | E | `SEC-PAG-REENV` | Espera el reenvío del aspirante | — | — |
| `E3-INI` | E | `SEC-E3-INI` | Consulta | — | — |
| `INSC-RECH` | T | `SEC-INSC-RECH` | Consulta | — | — |

---

## 6.1 Dashboard Secretaría

Widgets:

- **Pendientes de generar cupón** (`CUPON-SOL`)
- **Pagos pendientes de verificar** (`PAG-CTRL`)
- **Mis solicitudes**

---

## 6.2 Listado de Solicitudes

Misma estructura que Académico (5.2).

---

## 6.3 Detalle de Solicitud (contenedor común)

Misma estructura que Académico (5.4): encabezado, responsables, banner de estado, pestañas ("Acción actual" + Información General, Documentación, Análisis, Reunión, Chat, Historial, Auditoría).

---

## 6.4 Interfaces de Secretaría por estado

No hay diseños de Secretaría en Drive todavía (Anexo B #13).

### 6.4.1 `SEC-SEGUIMIENTO` – Seguimiento previo a la gestión de pago (E)

Banner con el estado actual y "En etapa académica". Datos de la solicitud y botón "Tomar proceso" como responsable administrativo, si corresponde.

---

### 6.4.2 `SEC-CUPON-SOL` – Generar y enviar cupón (A)

**Requisito:** RF-SEC-001.

Banner: "Solicitud lista para gestión de pago." ID de la solicitud, datos del aspirante, monto prellenado e Informe de materias en solo lectura.

**Acción:** **Generar y enviar cupón** → vista previa del PDF (`SEC-CUPON-SOL-M1`) → confirmar (doble verificación) → `CUPON-PAG`.

---

### 6.4.3 `SEC-CUPON-PAG` – Esperando pago (E)

Banner: "Cupón generado. Esperando el comprobante del aspirante."

---

### 6.4.4 `SEC-PAG-CTRL` – Verificación de pago (A)

**Requisito:** RF-SEC-002.

Banner: "El aspirante cargó su comprobante." Si viene de un reenvío: "El aspirante reenvió el comprobante."

Visor del comprobante, monto del cupón, comentarios (obligatorios si el pago es incorrecto) y decisión (radio): **Pago correcto** / **Pago incorrecto / monto no coincide**.

**Acción:** **Guardar verificación** → modal `SEC-PAG-CTRL-M1` (doble verificación) → `E3-INI` o `PAG-REENV`.

---

### 6.4.5 `SEC-PAG-REENV` – Esperando reenvío (E)

Banner: "Pago rechazado. Esperando que el aspirante reenvíe el comprobante."

---

### 6.4.6 `SEC-E3-INI` y `SEC-INSC-RECH` – Consulta

Solo lectura. La disposición decanal y el archivo administrativo (RF-SEC-003 y RF-SEC-004) quedan pendientes hasta que se definan los estados posteriores a `E3-INI`.

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

Pendiente de definición: depende de los estados posteriores a `E3-INI` (RF-ADMIN-006).

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
- Aceptar o rechazar documentación
- Guardar propuesta de análisis y fechas
- Indicar que la reunión finalizó o que el aspirante no se presentó
- Guardar decisión de inscripción
- Guardar revisión de legalizados
- Generar y enviar cupón
- Guardar verificación de pago

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
- **Un mismo estado puede recorrerse más de una vez (`DOC-RECH`, `LEG-RECH`, `PAG-CTRL`).**

---

# ANEXO A. PUNTOS ABIERTOS Y SUPUESTOS

Cosas que los requisitos v12 no terminan de definir y que el equipo de frontend necesita para cerrar las interfaces. Lo que está en este documento es el **supuesto adoptado** hasta que negocio confirme.

| # | Tema | Supuesto adoptado en este documento |
|---|---|---|
| 1 | **Datos del formulario inicial:** la tabla `solicitudes` de RF-ASP-001 no tiene columnas para universidad, facultad y carrera de origen, condición académica ni observaciones | Se piden en `ASP-NUEVA`. Backend debe agregar dónde guardarlos |
| 2 | **Valores de "Condición académica"** | Selector con valores a definir |
| 3 | **Documentos obligatorios para enviar en `SOL-INI`:** RF-ASP-002 lista Analítico, Plan y Programas | Obligatorios: Analítico, Plan de estudios y al menos un Programa |
| 4 | **Archivos como borrador:** si el aspirante sube archivos y sale sin enviar, ¿se conservan? | Se asume que sí (cada archivo se sube al elegirlo). Confirmar con backend |
| 5 | **Límite de 50 MB en la recarga** (`DOC-RECH`) | Se aplica a los archivos que se envían en esa recarga |
| 6 | **Cambiar la fecha elegida:** al seleccionar fecha el estado pasa directo a `REU-PEND` | No se puede cambiar desde la interfaz; se resuelve por chat |
| 7 | **Reprogramación tras la 2ª ronda:** si el aspirante no se presenta y ya se usó la 2ª ronda, `REU-FEC-ACAD` solo tiene la transición hacia `REU-FEC-ASP-2` | Se reutiliza la 2ª ronda hasta que negocio defina otra cosa |
| 8 | **Estructura del informe de materias:** RF-ACAD-001 dice que la matriz es un archivo o notas libres, pero `ASP-CUPON-SOL` muestra una tabla (materia, carga horaria, correlativas, monto) | Se asume que el backend expone estos datos estructurados. Falta definir cómo los carga el Académico |
| 9 | **Monto reportado por el aspirante:** RF-SEC-002 menciona "Monto reportado", pero RF-ASP-007 no pide que el aspirante lo ingrese | El aspirante solo sube el comprobante |
| 10 | **`INSC-RECH`:** ¿el aspirante puede iniciar una nueva solicitud? ¿el chat queda en solo lectura? | Chat en solo lectura. "Nueva solicitud" sigue disponible en el menú |
| 11 | **Varias solicitudes por aspirante:** ¿puede tener más de una activa? | Sí (el dashboard agrupa varias) |
| 12 | **Editar el formulario inicial** después de crear la solicitud | No editable. Los cambios se resuelven por chat |
| 13 | **Desbloqueo de cuenta:** RF-AUTH-006 lo lista como acción con doble verificación, pero RF-AUTH-002 y RF-ADMIN-007 dicen que se hace sin doble verificación | Se implementa **sin** doble verificación hasta que negocio decida |
| 14 | **Monto del cupón:** ¿Secretaría puede modificar el monto prellenado? | Solo lectura por defecto |
| 15 | **Comprobantes rechazados:** ¿se archivan como versión anterior, como los documentos? | Se muestra el anterior en `ASP-PAG-REENV`. Confirmar conservación con backend |
| 16 | **Adjuntos de chat:** formatos permitidos (solo se define el límite de 5 MB) | Mismos formatos que documentación más imágenes |

---

# ANEXO B. DIFERENCIAS ENTRE EL DISEÑO DE DRIVE Y LOS REQUISITOS

Mientras no se decidan (CC-006 en `GESTION_CAMBIOS.md`), manda el documento indicado en "Criterio vigente": los requisitos para las reglas de negocio y el diseño para la apariencia.

| # | Tema | Diseño de Drive | Requisitos o contrato | Criterio vigente |
|---|---|---|---|---|
| 1 | Registro | Nombre y Apellido separados | Nombre completo | Diseño para la pantalla; al enviar, `nombre` del contrato une ambos campos |
| 2 | Contraseña | Mayúscula, minúscula y número | Mayúscula, número y carácter especial | Requisitos |
| 3 | Expiración del código | Contador desde 04:59 | 10 minutos | Requisitos |
| 4 | Formatos de archivo | PDF, JPG y PNG | PDF y JPG | Requisitos |
| 5 | Documentos obligatorios | Analítico, Programa, Plan y DNI | Analítico, Plan y Programas por materia | Requisitos |
| 6 | Código de expediente | `EQ-2024-001` | `EXP-2026-001` | Requisitos |
| 7 | Formulario inicial | Dirección y listas desplegables de origen | Sin dirección; origen en texto libre | Requisitos |
| 8 | Elección de fecha | Selección múltiple | Una sola fecha | Requisitos |
| 9 | Legalización | Carga de Plan de estudios y Cronograma de materias | RF-ASP-009 solo pide responder y avisar; la transición 18 habla de recargar | Requisitos, con la contradicción pendiente |
| 10 | Menú y perfil del Académico | Mis tareas, Calendario, disponibilidad en Mi perfil, Ayuda y LU del estudiante | No están definidos | Se implementan cuando se aprueben |
| 11 | Revisión del Académico | Estado por documento, "Guardar borrador" y "Enviar evaluación" | "Guardar marcas", aceptar y rechazar | Requisitos para las acciones; diseño para la tabla |
| 12 | Nombres de estados | "Con observaciones", "En evaluación", "Error en documentación" | Nombres del catálogo de 20 estados | Requisitos |
| 13 | Pantallas sin diseño | No hay Secretaría, Admin, Revisor, Login @usal ni Login Admin | Definidas en este documento | Se aplican los componentes y tokens existentes |
| 14 | Pantalla de inicio | El login está integrado en el inicio | Inicio y login son pantallas separadas (3.1 y 3.4) | Diseño, porque no cambia reglas de negocio |

---

Fin del documento.
