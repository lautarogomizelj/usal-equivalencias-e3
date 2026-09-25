# Sistema E3 - Análisis del Flujo de Coordinación de Meet
## "Presentación y explicación de la matriz de equivalencias"

---

## Problema Identificado

En el proceso actual (BPMN Diagrama 3), la actividad **"Meet para presentar y explicar la matriz"** se trata como una tarea simple. Sin embargo, requiere **coordinación entre dos personas (Académico y Aspirante) en tiempo real** para acordar horarios.

**Pregunta abierta:** ¿Cómo se gestiona esta coordinación en el sistema sin depender de mail/WhatsApp?

---

## Propuesta General del Flujo

### Fase 1: Académico propone horarios
1. Académico carga matriz tentativa y horario propuesto
2. Académico abre calendario y propone 5-7 slots horarios disponibles
3. Sistema notifica al Aspirante: "Elige un horario para tu reunión"

### Fase 2: Aspirante elige (o no puede)
1. Aspirante ve opciones en su panel
2. Aspirante elige UN horario O dice "Ninguno me queda"
3. Si eligió → va a Académico para confirmación
4. Si no puede → Académico puede proponer nuevos horarios (2da ronda, máximo)

### Fase 3: Académico confirma
1. Académico ve horario elegido
2. Académico confirma o rechaza
3. Si confirma → ambos ven reunión agendada
4. Si rechaza → propone nuevos horarios

### Fase 4: Después del Meet
1. Académico sube notas y resultado de la reunión
2. Académico decide: permitir inscripción / rehacer matriz / rechazar
3. Transición a siguiente estado según decisión

---

## Alternativas y Decisiones

### Decisión 1: ¿Cuántas rondas de propuesta?

#### Opción A: UNA SOLA (Rigurosa)
- Académico propone horarios **una única vez**
- Si Aspirante no elige en 5 días → solicitud rechazada automáticamente
- Ventaja: Evita conversaciones largas
- Desventaja: Poco realista, mucho rechazo por imprevistos

#### Opción B: DOS RONDAS (Flexible) ⭐ RECOMENDADO
- Académico propone horarios (1ra ronda)
- Si Aspirante dice "Ninguno me queda" → Académico propone nuevamente (2da ronda)
- Después de 2da ronda, si no coincide → rechazar
- Ventaja: Balance entre flexibilidad y eficiencia
- Desventaja: Un poco más de complejidad

#### Opción C: NEGOCIACIÓN ABIERTA
- Rondas ilimitadas hasta que alguien rechace
- Ventaja: Máxima flexibilidad
- Desventaja: Puede quedar en limbo, sin trazabilidad clara

**Mi recomendación: Opción B (dos rondas)**

---

### Decisión 2: ¿Qué pasa si Aspirante no puede ningún horario?

#### Flujo propuesto:

```
Aspirante ve horarios propuestos
    │
    ├→ [CASO 1] Elige uno 
    │   → Clickea botón "Confirmar esta fecha"
    │   → Va a Académico para confirmación
    │
    └→ [CASO 2] No le queda ninguno
        → Clickea botón "Ningún horario me queda"
        → Se abre campo de comentario (opcional): "¿Por qué no puedes?"
        → Sistema notifica al Académico: 
           "Aspirante no pudo coincidir. Comentario: [...]"
        → Transición a estado "Horarios no coinciden - Propuesta 2"
        
        Académico ve notificación y puede:
        a) Proponer nuevos horarios (2da ronda)
        b) Rechazar solicitud
        c) Dejar mensaje en comentarios
```

**Plazo recomendado para Aspirante:** 5 días calendario para elegir

---

### Decisión 3: ¿Horarios disponibles por defecto?

#### Opción A: Académico propone manualmente
- Académico abre calendario en su panel
- Cliquea en fechas/horas específicas
- Máximo 7 slots
- Ventaja: Control total
- Desventaja: Más clicks

#### Opción B: Académico indica "disponibilidad"
- Ejemplo: "Lunes y martes 14:00-18:00, Jueves 10:00-13:00"
- Sistema genera automáticamente 5-7 slots en esas franjas
- Ventaja: Rápido
- Desventaja: Menos granular, puede no coincidir con aspirante

**Mi recomendación: Opción A (manual)** — más claro y auditable

---

### Decisión 4: ¿Duración estándar de la reunión?

**Propuesta:** 1 hora (14:00-15:00, 10:00-11:00, etc.)

Razones:
- Suficiente para presentar matriz + aclaraciones
- No es invasivo (no pide 2+ horas)
- Horario académico típico (no madrugadas)

Franjas recomendadas: **14:00-18:00** (pre-siesta, accesible)

---

## Flujo Completo con Estados

### Estados actuales vs. Estados propuestos

**Actual:**
```
Documentación cargada
    ↓
Análisis preliminar completado
    ↓
Revisión legal completada
```

**Propuesto (con Meet):**
```
Documentación cargada
    ↓
Análisis preliminar completado
    ├── [Académico carga matriz + propone horarios]
    │
    ↓
**Esperando selección de horario** [NEW]
    ├── [Aspirante ve opciones]
    │   ├→ Elige → Confirmación pendiente de Académico
    │   └→ No puede → Notificación a Académico
    │
    ↓
**Reunion confirmada** [NEW]
    ├── [Ambos tienen reunión agendada]
    │   [Realización del Meet - fuera del sistema]
    │
    ↓
**Reunion realizada** [NEW]
    ├── [Académico carga notas + toma decisión]
    │   ├→ Permite inscripción → Aprobado
    │   ├→ Rechaza → Rechazado
    │   └→ Requiere cambios → Documentación rechazada
    │
    ↓
Revisión legal completada (si pasó a Aprobado)
```

---

## Interface del Usuario

### Panel del ACADÉMICO - Proponer horarios

**Cuando hace clic en "Realizar análisis preliminar" y termina:**

```
┌─────────────────────────────────────────┐
│ ANÁLISIS PRELIMINAR COMPLETADO          │
│                                         │
│ ✓ Matriz tentativa cargada              │
│ ✓ Horario propuesto cargado             │
│                                         │
│ SIGUIENTE: Proponer horarios para Meet  │
│                                         │
│ [Calendario] [← Semana anterior]       │
│                                         │
│ Selecciona hasta 7 horarios disponibles:│
│                                         │
│ Lunes 20/09   ☐ 10:00-11:00             │
│               ☐ 14:00-15:00             │
│               ☐ 16:00-17:00             │
│                                         │
│ Martes 21/09  ☐ 11:00-12:00             │
│               ☐ 15:00-16:00             │
│               ☑ 17:00-18:00             │
│                                         │
│ ... más fechas ...                      │
│                                         │
│ [← Anterior] [Siguiente →]              │
│                                         │
│ [Guardar propuesta y notificar]         │
│                                         │
│ ⚠ Puedes cambiar estas fechas hasta    │
│   que el aspirante elija una.           │
└─────────────────────────────────────────┘
```

**Estado del sistema:** "Esperando selección de horario"

---

### Panel del ASPIRANTE - Elegir horario

**Cuando Académico propone:**

```
┌─────────────────────────────────────────┐
│ ⏰ SELECCIONA HORARIO PARA TU REUNIÓN   │
│                                         │
│ El Lic. García propone estos horarios   │
│ para explicarte tu matriz de            │
│ equivalencias:                          │
│                                         │
│ ☐ Lunes 20/09 - 10:00-11:00             │
│ ☑ Lunes 20/09 - 14:00-15:00 (ELEGIDO)  │
│ ☐ Lunes 20/09 - 16:00-17:00             │
│                                         │
│ ☐ Martes 21/09 - 11:00-12:00            │
│ ☐ Martes 21/09 - 15:00-16:00            │
│ ☐ Martes 21/09 - 17:00-18:00            │
│                                         │
│ ... más opciones ...                    │
│                                         │
│ [Confirmar este horario]                │
│                                         │
│ O si ninguno te queda:                  │
│                                         │
│ [Ninguno me queda]                      │
│                                         │
│ Comentario (opcional):                  │
│ ┌─────────────────────────────────────┐ │
│ │ Tengo que viajar esa semana...      │ │
│ └─────────────────────────────────────┘ │
│ [Enviar]                                │
│                                         │
│ ⏳ Tienes hasta el 25/09 para elegir    │
└─────────────────────────────────────────┘
```

**Estado del sistema:** "Esperando selección de horario"

---

### Panel del ACADÉMICO - Confirmar horario elegido

**Cuando Aspirante elige:**

```
┌─────────────────────────────────────────┐
│ CONFIRMAR HORARIO CON ASPIRANTE         │
│                                         │
│ Aspirante: Juan Pérez                   │
│ Horario propuesto: Lunes 20/09, 14:00   │
│                                         │
│ ¿Confirmas este horario?                │
│                                         │
│ [Sí, confirmo] [No, proponer nuevos]   │
│                                         │
│ Si no puedo ese día, comentar:          │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

**Estado del sistema:** "Reunion confirmada" (si elige "Sí, confirmo")

---

### Panel de AMBOS - Reunión confirmada

**Académico ve:**
```
✓ Reunión agendada: Lunes 20/09 a las 14:00
  Aspirante: Juan Pérez
  Carrera: Ingeniería Informática
  
  [Ver matriz] [Ver horario propuesto]
  
  Cuando termines la reunión:
  [Cargar notas y resultado de Meet]
```

**Aspirante ve:**
```
✓ Reunión confirmada: Lunes 20/09 a las 14:00
  Con: Lic. García (Académico)
  Tema: Presentación de tu matriz de equivalencias
  
  [Ver matriz]
  
  Cuando se termine:
  - Recibiras un mail con los resultados
```

---

### Panel del ACADÉMICO - Después del Meet

**Cuando vuelve después de la reunión:**

```
┌─────────────────────────────────────────┐
│ RESULTADO DE LA REUNIÓN                 │
│                                         │
│ Aspirante: Juan Pérez                   │
│ Fecha: Lunes 20/09, 14:00               │
│                                         │
│ NOTAS DE LA REUNIÓN:                    │
│ ┌─────────────────────────────────────┐ │
│ │ - Presenté matriz de equivalencias  │ │
│ │ - Cuestión sobre Cálculo III        │ │
│ │ - Aclaramos diferencia de contenidos│ │
│ │ - Aspirante entiende y acepta       │ │
│ │ - Se realizó Meet Google Meet.      │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ¿Resultado?                             │
│                                         │
│ ◯ Permite inscripción → Continuar       │
│ ◯ Rechaza solicitud                     │
│ ◯ Requiere cambios en matriz            │
│                                         │
│ [Guardar y continuar]                   │
│                                         │
└─────────────────────────────────────────┘
```

**Estados resultantes:**
- Si "Permite": → "Revisión legal completada"
- Si "Rechaza": → "Rechazado" (fin del trámite)
- Si "Requiere cambios": → "Documentación rechazada" (Aspirante recarga)

---

## Notificaciones por Mail

### Mail 1: Propuesta de horarios (Académico → Aspirante)

```
Asunto: E3-2024-001: Elige horario para tu reunión

Hola [Aspirante],

El equipo académico ha completado el análisis preliminar 
de tu solicitud de equivalencias.

Ahora necesitamos hacer una reunión para presentarte y 
explicarte la matriz de equivalencias (qué materias se te 
reconocen, cuáles debes adeudar, etc.).

POR FAVOR ELIGE UN HORARIO en tu panel:
→ https://tuapp.usal.edu.ar/panel/solicitud/E3-2024-001

Opciones disponibles:
  • Lunes 20 de septiembre, 14:00-15:00
  • Lunes 20 de septiembre, 16:00-17:00
  • Martes 21 de septiembre, 11:00-12:00
  • ... más opciones en el panel

⏳ Por favor elige antes del 25 de septiembre.

Si ningún horario te queda, también puedes indicarlo 
en el panel.

Contacto: academica@usal.edu.ar
Teléfono: +54 11 XXXX-XXXX
```

---

### Mail 2: Aspirante eligió - Académico confirma (Sistema → Académico)

```
Asunto: E3-2024-001: Aspirante seleccionó horario

Hola [Académico],

El aspirante Juan Pérez ha seleccionado un horario para 
la reunión:

📅 Lunes 20 de septiembre, 14:00-15:00

¿Confirmas este horario? → Ve a tu panel y confirma:
https://tuapp.usal.edu.ar/panel/solicitud/E3-2024-001

Si no puedes a esa hora, puedes proponer nuevos 
horarios desde el panel (2da ronda).
```

---

### Mail 3: Ambos confirmados (Sistema → Ambos)

```
Asunto: E3-2024-001: Reunión confirmada ✓

¡Hola [Aspirante]!

Tu reunión ha sido confirmada:

📅 Lunes 20 de septiembre
🕐 14:00-15:00 (horario de Buenos Aires)
👤 Con: Lic. García (Académico)
📋 Tema: Presentación de tu matriz de equivalencias

Prepárate para:
- Conocer qué materias se te reconocen
- Entender qué materias debes adeudar
- Aclarar dudas sobre el reconocimiento

El Lic. García te enviará el link de Google Meet 
24 horas antes de la reunión.

¿Preguntas? Contacta: academica@usal.edu.ar

---

[Mail similar para Académico con datos del Aspirante]
```

---

### Mail 4: Después del Meet (Académico → Aspirante)

**Caso A: Aprobado**
```
Asunto: E3-2024-001: ¡Gracias por la reunión! ✓

Hola Juan,

Gracias por asistir a la reunión de presentación de 
tu matriz de equivalencias.

RESULTADO: ✓ PERMITIDA TU INSCRIPCIÓN

Se reconocen 25 materias de tu plan anterior.
Debes adeudar 8 materias.

Ahora necesitarás completar la revisión legal de 
tus documentos. Pronto tendrás más información 
en tu panel.

→ Ver detalle en: https://tuapp.usal.edu.ar/panel/...
```

**Caso B: Requiere cambios**
```
Asunto: E3-2024-001: Revisión de tu matriz

Hola Juan,

Gracias por asistir a la reunión. Tras revisión, 
necesitamos rehacer algunos puntos de la matriz 
de equivalencias.

Motivo: Se encontraron inconsistencias en el 
reconocimiento de Cálculo III (programa diferente 
a lo declarado).

Por favor recarga los documentos de Cálculo III 
en tu panel para revisión.

→ Tu panel: https://tuapp.usal.edu.ar/panel/...
```

**Caso C: Rechazado**
```
Asunto: E3-2024-001: Resultado de revisión

Hola Juan,

Tras la reunión de presentación, lamentablemente 
tu solicitud ha sido rechazada.

Motivo: Diferencias significativas entre el 
plan que declaraste y el analítico presentado.

Puedes contactar a académica@usal.edu.ar para 
aclaraciones.
```

---

## Puntos de Decisión Abiertos (Preguntar al Profesor)

1. **¿Máximo de rondas de propuesta?**
   - Sugerencia: 2 rondas (Académico propone 2 veces máximo)

2. **¿Plazo para que Aspirante elija?**
   - Sugerencia: 5 días calendario

3. **¿Si Aspirante no se presenta al Meet?**
   - Opción A: Rechazar automáticamente
   - Opción B: Permitir re-agendar 1 vez

4. **¿Dónde se realiza el Meet?**
   - ¿Google Meet (automático dentro del sistema)?
   - ¿Otro (Zoom, Teams, etc.)?
   - ¿Solo coordinar fecha, el link se envía por mail?

5. **¿Se archiva el link del Meet en el sistema?**
   - Sugerencia: Sí (para auditoría)

6. **¿Duración estándar?**
   - Sugerencia: 1 hora (14:00-15:00)

7. **¿Franjas horarias permitidas?**
   - Sugerencia: 10:00-18:00 (horario académico)

8. **¿El Aspirante puede ver la matriz ANTES de la reunión?**
   - Opción A: Solo después de confirmar horario
   - Opción B: Inmediatamente cuando Académico la carga
   - Mi recomendación: Opción A (genera interés en la reunión)

---

## Notas sobre Almacenamiento/Auditoría

Para cada solicitud, guardar en `/equivalencias/YYYY/EXP-ID/meet/`:

```
meet_data.json
├── propuesta_1: {
│     slots: ["2024-09-20 14:00", "2024-09-20 16:00", ...],
│     fecha_propuesta: "2024-09-15 10:30",
│     estado: "enviada_a_aspirante"
│   }
├── propuesta_2: {
│     slots: [...],
│     fecha_propuesta: "2024-09-18 15:45",
│     estado: "enviada_a_aspirante"
│   }
├── horario_elegido: {
│     slot: "2024-09-20 14:00",
│     aspirante_eligio: "2024-09-17 11:20",
│     academico_confirmo: "2024-09-17 11:35",
│     estado: "confirmada"
│   }
├── reunión_realizada: {
│     link_meet: "https://meet.google.com/xyz",
│     notas: "Presenté matriz. Aspirante cuestionó Cálculo III...",
│     resultado: "permite_inscripcion",
│     fecha_cierre: "2024-09-20 15:15",
│     académico: "garcia@usal.edu.ar"
│   }
```

---

## Flujo Alternativo: Si se quiere SIMPLIFICAR

Si después de hablar con el profesor, quiere hacerlo más simple, aquí está:

**Flujo Ultra-Simplificado (sin calendario):**

```
Académico carga: "Puedo hacer Meet el 20/09 a las 14:00"
    ↓
Sistema notifica: "¿Confirmas este horario?"
    ↓
Aspirante: "Sí, confirmo" o "No puedo"
    ↓
Si "Sí" → Se agenda, ambos lo ven, se realiza
Si "No" → Rechazo automático O Académico propone 1 vez más
```

**Esto elimina:**
- Calendario complejo
- Múltiples slots
- Interface de selección

**Ventaja:** Mucho más simple (4-5 pantallas vs. 10-12)
**Desventaja:** Menos flexible, puede generar muchos rechazos

---

**Conclusión: Cuando tengas claridad con el profesor, me avisas y adaptamos el documento v2 con los estados y flujos finales.**

