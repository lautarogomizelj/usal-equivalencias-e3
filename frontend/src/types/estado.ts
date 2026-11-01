/**
 * Estado del catálogo `estados` (RF-ESTADO-003).
 * Los estados vienen del backend: nunca comparar por `nombre`, solo por `codigo`.
 */
export interface Estado {
  codigo: string
  nombre: string
  descripcion: string
  esTerminal: boolean
  activo: boolean
}
