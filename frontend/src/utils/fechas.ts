const FORMATO_FECHA = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })

const FORMATO_HORA = new Intl.DateTimeFormat('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false })

/** Fecha ISO del backend como `12/03/2026`. */
export function formatearFecha(iso: string) {
  return FORMATO_FECHA.format(new Date(iso))
}

/** Fecha ISO del backend como `12/03/2026 10:32`. */
export function formatearFechaHora(iso: string) {
  const fecha = new Date(iso)
  return `${FORMATO_FECHA.format(fecha)} ${FORMATO_HORA.format(fecha)}`
}
