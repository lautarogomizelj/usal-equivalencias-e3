/** Reglas de las fechas de reunión que propone el Académico (RF-ACAD-002, contrato de la API §7.3). */
export const REUNION = {
  RONDAS: 2,
  MAX_HORARIOS: 7,
  /** Franjas de 1 hora entre las 10:00 y las 18:00: la última empieza a las 17. */
  PRIMERA_HORA: 10,
  ULTIMA_HORA: 17,
  /** Se propone desde mañana y hasta dentro de esta cantidad de días. */
  DIAS_HACIA_ADELANTE: 30,
  /** Argentina no cambia la hora: todo el año es UTC−3. */
  DESFASE_HORARIO: '-03:00',
  MAX_TEXTO: 500,
  MAX_NOTAS: 2000,
  PREFIJO_MEET: 'https://meet.google.com/',
} as const

const HORA_MS = 60 * 60 * 1000

/** Una reunión dura siempre 1 hora. */
export function finDeReunion(inicio: string) {
  return new Date(Date.parse(inicio) + HORA_MS).toISOString()
}
