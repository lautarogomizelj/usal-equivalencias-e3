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
}
