const BYTES_POR_MB = 1024 * 1024
const FORMATO_MB = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 1 })

/** Tamaño de un archivo como `1,5 MB`. */
export function formatearTamanio(bytes: number) {
  return `${FORMATO_MB.format(bytes / BYTES_POR_MB)} MB`
}
