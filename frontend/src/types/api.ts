/** Categorías de error manejadas de forma centralizada (frontend-interfaces.md §0.9.2). */
export type ApiErrorKind =
  | 'bad_request'
  | 'unauthorized'
  | 'forbidden'
  | 'not_found'
  | 'conflict'
  | 'locked'
  | 'server'
  | 'network'
  | 'unknown'

/** Formato de error del contrato de la API (§1). */
export interface ApiErrorBody {
  status?: number
  code?: string
  message?: string
  errors?: Record<string, string[]>
  intentosRestantes?: number
  timestamp?: string
  path?: string
}

/** Respuesta paginada del contrato de la API (§1). */
export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
