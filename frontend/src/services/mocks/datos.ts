import type { Carrera } from '@/types/carrera'
import type { Estado } from '@/types/estado'

export const CARRERAS: Carrera[] = [
  { id: 1, codigo: 'ING-INF-001', nombre: 'Ingeniería en Informática', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
  { id: 2, codigo: 'ING-IND-001', nombre: 'Ingeniería Industrial', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
  { id: 3, codigo: 'ING-CIV-001', nombre: 'Ingeniería Civil', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
]

/** Código, nombre y si es terminal: mismos datos que la migración V2__catalogos.sql. */
const CATALOGO: [codigo: string, nombre: string, esTerminal?: boolean][] = [
  ['SOL-INI', 'Solicitud iniciada'],
  ['DOC-REV', 'Documentación pendiente de revisión'],
  ['DOC-RECH', 'Documentación rechazada'],
  ['ANAL-PEND', 'Análisis preliminar pendiente'],
  ['REU-FEC-ASP-1', 'Reunión pendiente de fechas aspirante (1ª ronda)'],
  ['REU-FEC-ASP-2', 'Reunión pendiente de fechas aspirante (2ª ronda)'],
  ['REU-FEC-ACAD', 'Reunión pendiente de fechas académico'],
  ['REU-CHAT', 'Resolución fecha por chat'],
  ['REU-PEND', 'Reunión pendiente de realización'],
  ['ANAL-POST', 'Análisis pos reunión'],
  ['INSC-ACEP', 'Inscripción aceptada'],
  ['LEG-PEND', 'Legalización pendiente'],
  ['INSC-RECH', 'Inscripción rechazada', true],
  ['LEG-REV', 'Análisis de documentos legalizados'],
  ['LEG-RECH', 'Documentos legalizados rechazados'],
  ['CUPON-SOL', 'Solicitud de cupón de pago'],
  ['CUPON-PAG', 'Pago de cupón por aspirante'],
  ['PAG-CTRL', 'Control de pago'],
  ['PAG-REENV', 'Reenvío de comprobante'],
  ['E3-INI', 'Inicio de E3'],
]

export const ESTADOS: Estado[] = CATALOGO.map(([codigo, nombre, esTerminal = false]) => ({
  codigo,
  nombre,
  descripcion: '',
  esTerminal,
  activo: true,
}))
