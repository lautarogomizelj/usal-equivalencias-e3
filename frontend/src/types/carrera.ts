/** Carrera del catálogo (contrato de la API §5). */
export interface Carrera {
  id: number
  codigo: string
  nombre: string
  facultad: string
  descripcion: string
  activo: boolean
}
