/** Categorías de error manejadas de forma centralizada (frontend-interfaces.md §0.9.2). */
export type ApiErrorKind =
  | 'bad_request'
  | 'unauthorized'
  | 'forbidden'
  | 'not_found'
  | 'server'
  | 'network'
  | 'unknown'

/** Forma esperada del cuerpo de error del backend. Ajustar cuando se defina el contrato. */
export interface ApiErrorBody {
  message?: string
  errors?: Record<string, string[]>
}
