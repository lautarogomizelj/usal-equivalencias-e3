const FORMATO_FECHA = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })

const FORMATO_HORA = new Intl.DateTimeFormat('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false })

/** Las reuniones se muestran siempre en hora de Argentina, esté donde esté quien mira. */
const ZONA_REUNION = 'America/Argentina/Buenos_Aires'

const FORMATO_DIA_LARGO = new Intl.DateTimeFormat('es-AR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: ZONA_REUNION,
})

const FORMATO_HORA_REUNION = new Intl.DateTimeFormat('es-AR', {
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
  timeZone: ZONA_REUNION,
})

/** Fecha ISO del backend como `12/03/2026`. */
export function formatearFecha(iso: string) {
  return FORMATO_FECHA.format(new Date(iso))
}

/** Fecha ISO del backend como `12/03/2026 10:32`. */
export function formatearFechaHora(iso: string) {
  const fecha = new Date(iso)
  return `${FORMATO_FECHA.format(fecha)} ${FORMATO_HORA.format(fecha)}`
}

/** Día de una reunión como `Lunes 30 de noviembre de 2026`. */
export function formatearDiaLargo(iso: string) {
  const texto = FORMATO_DIA_LARGO.format(new Date(iso)).replace(',', '')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

/** Franja de una reunión como `10:00 – 11:00 hs`. */
export function formatearFranja(inicio: string, fin: string) {
  return `${FORMATO_HORA_REUNION.format(new Date(inicio))} – ${FORMATO_HORA_REUNION.format(new Date(fin))} hs`
}

/** Hoy en Argentina, como `2026-11-20`. */
export function hoyEnArgentina() {
  return new Intl.DateTimeFormat('en-CA', { timeZone: ZONA_REUNION }).format(new Date())
}

/** Suma días a una fecha `AAAA-MM-DD`. */
export function sumarDias(dia: string, cantidad: number) {
  const fecha = new Date(`${dia}T00:00:00Z`)
  fecha.setUTCDate(fecha.getUTCDate() + cantidad)
  return fecha.toISOString().slice(0, 10)
}
