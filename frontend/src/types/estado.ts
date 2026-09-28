/**
 * Estado del catálogo `estados` (RF-ESTADO-BD-001).
 * Los estados vienen del backend: nunca comparar por `nombre`, solo por `codigo`.
 */
export interface Estado {
  id: number
  codigo: string
  nombre: string
  descripcion: string
  es_terminal: boolean
  activo: boolean
}
