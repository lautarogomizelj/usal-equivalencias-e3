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
  'SOL-CANC': 'Cancelaste esta solicitud antes de enviar la documentación. Si querés retomar el trámite, creá una nueva.',
}

/** Texto del banner del personal en cada estado (frontend-interfaces.md §5.6 y §6.4). Cada sprint suma los suyos. */
export const BANNER_PERSONAL: Record<string, string> = {
  'SOL-INI': 'El aspirante todavía no envió su documentación.',
  'DOC-REV': 'Documentación lista para revisar.',
  'DOC-RECH': 'Esperando que el aspirante recargue los documentos marcados.',
  'ANAL-PEND':
    'La documentación fue aceptada. Sigue el análisis preliminar: la carga de la matriz de equivalencias, el horario de cursada y las fechas de reunión todavía no está disponible.',
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
