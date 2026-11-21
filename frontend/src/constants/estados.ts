/**
 * Variante visual de cada estado (RF-ESTADO-001). El nombre y el catálogo vienen del
 * backend: acá solo se asocia el código con su color (frontend-interfaces.md §2.10).
 */
export type VarianteEstado = 'accion' | 'espera' | 'exito' | 'atencion' | 'neutro'

export const VARIANTE_POR_ESTADO: Record<string, VarianteEstado> = {
  'SOL-INI': 'accion',
  'DOC-REV': 'espera',
  'DOC-RECH': 'atencion',
  'ANAL-PEND': 'espera',
  'REU-FEC-ASP-1': 'accion',
  'REU-FEC-ASP-2': 'accion',
  'REU-FEC-ACAD': 'espera',
  'REU-CHAT': 'accion',
  'REU-PEND': 'espera',
  'ANAL-POST': 'espera',
  'INSC-ACEP': 'exito',
  'LEG-PEND': 'accion',
  'INSC-RECH': 'atencion',
  'LEG-REV': 'espera',
  'LEG-RECH': 'atencion',
  'CUPON-SOL': 'espera',
  'CUPON-PAG': 'accion',
  'PAG-CTRL': 'espera',
  'PAG-REENV': 'atencion',
  'E3-INI': 'exito',
  'SOL-CANC': 'neutro',
}

/** Texto del banner del aspirante en cada estado (frontend-interfaces.md §4.6). Cada sprint suma los suyos. */
export const BANNER_ASPIRANTE: Record<string, string> = {
  'DOC-REV': 'Tu documentación está en revisión. Te avisamos por mail si necesitamos algo más.',
  'DOC-RECH': 'Necesitamos que recargues algunos documentos para continuar con el trámite.',
  'ANAL-PEND': 'Tu documentación fue aceptada. Estamos analizando tu solicitud.',
  'REU-FEC-ASP-1': 'Es necesario pactar una fecha de reunión con tu responsable académico. Elegí uno de los horarios propuestos.',
  'REU-FEC-ASP-2':
    'Ronda 2 de 2. Tu responsable académico propuso nuevas fechas para la reunión: elegí una. Si tampoco podés, la fecha se coordina por chat.',
  'REU-FEC-ACAD': 'Estamos esperando nuevas fechas del Académico.',
  'REU-CHAT':
    'Es necesario coordinar la fecha de la reunión por chat con tu responsable académico. El chat de la solicitud todavía no está disponible.',
  'REU-PEND': 'Reunión pactada, pendiente de realizarse.',
  'SOL-CANC': 'Cancelaste esta solicitud antes de enviar la documentación. Si querés retomar el trámite, creá una nueva.',
}

/** Texto del banner del personal en cada estado (frontend-interfaces.md §5.6 y §6.4). Cada sprint suma los suyos. */
export const BANNER_PERSONAL: Record<string, string> = {
  'SOL-INI': 'El aspirante todavía no envió su documentación.',
  'DOC-REV': 'Documentación lista para revisar.',
  'DOC-RECH': 'Esperando que el aspirante recargue los documentos marcados.',
  'ANAL-PEND':
    'La documentación fue aceptada. Falta cargar la matriz de equivalencias y el posible horario de cursada, y proponer las fechas de la reunión.',
  'REU-FEC-ASP-1': 'Ronda 1 de 2. Esperando que el aspirante elija una fecha.',
  'REU-FEC-ASP-2': 'Ronda 2 de 2. Esperando que el aspirante elija una fecha.',
  'REU-FEC-ACAD': 'El aspirante pidió otras fechas para la reunión. Falta proponer la 2ª ronda.',
  'REU-CHAT':
    'El aspirante rechazó la 2ª ronda de fechas. La fecha se pauta por el chat de la solicitud, que todavía no está disponible.',
  'REU-PEND': 'Reunión pactada, pendiente de realizarse.',
  'SOL-CANC': 'El aspirante canceló la solicitud antes de enviar la documentación.',
}

/** Estados en los que le toca actuar a cada rol del personal (frontend-interfaces.md §5.1 y §6.1). */
export const ESTADOS_DE_ACCION: Record<string, string[]> = {
  ACAD: ['DOC-REV', 'ANAL-PEND', 'REU-FEC-ACAD', 'REU-CHAT', 'REU-PEND', 'ANAL-POST', 'LEG-REV'],
  SECRE: ['CUPON-SOL', 'PAG-CTRL'],
}

/** Estados en los que el aspirante tiene que corregir algo que le observaron. */
export const ESTADOS_CON_OBSERVACIONES = ['DOC-RECH', 'LEG-RECH', 'PAG-REENV']

/** Macro-etapas del trámite y los estados que activan cada una (frontend-interfaces.md §2.9 y §4.5.2). */
export const ETAPAS: { nombre: string; estados: string[] }[] = [
  { nombre: 'Documentación', estados: ['SOL-INI', 'DOC-REV', 'DOC-RECH', 'SOL-CANC'] },
  { nombre: 'Análisis', estados: ['ANAL-PEND'] },
  { nombre: 'Reunión', estados: ['REU-FEC-ASP-1', 'REU-FEC-ASP-2', 'REU-FEC-ACAD', 'REU-CHAT', 'REU-PEND'] },
  { nombre: 'Inscripción', estados: ['ANAL-POST', 'INSC-ACEP', 'INSC-RECH'] },
  { nombre: 'Legalización', estados: ['LEG-PEND', 'LEG-REV', 'LEG-RECH'] },
  { nombre: 'Pago', estados: ['CUPON-SOL', 'CUPON-PAG', 'PAG-CTRL', 'PAG-REENV'] },
  { nombre: 'Inicio de E3', estados: ['E3-INI'] },
]

/** Estados que cortan el trámite en su etapa, con el texto que muestra el stepper. */
export const CIERRE_NEGATIVO: Record<string, string> = {
  'INSC-RECH': 'Rechazada',
  'SOL-CANC': 'Cancelada',
}
