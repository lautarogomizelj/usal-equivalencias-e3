import type { ApiErrorBody, ApiErrorKind } from '@/types/api'

const MENSAJES_POR_TIPO: Record<ApiErrorKind, string> = {
  bad_request: 'Revisá los datos ingresados.',
  unauthorized: 'Tu sesión expiró. Volvé a iniciar sesión.',
  forbidden: 'No tenés permiso para realizar esta acción.',
  not_found: 'El recurso solicitado no existe.',
  server: 'Ocurrió un error en el servidor. Intentá nuevamente más tarde.',
  network: 'No se pudo conectar con el servidor. Revisá tu conexión.',
  unknown: 'Ocurrió un error inesperado.',
}

function tipoDesdeStatus(status: number): ApiErrorKind {
  if (status === 400 || status === 422) return 'bad_request'
  if (status === 401) return 'unauthorized'
  if (status === 403) return 'forbidden'
  if (status === 404) return 'not_found'
  if (status >= 500) return 'server'
  return 'unknown'
}

/** Error único que lanza la capa de servicios (frontend-interfaces.md §0.9.2). */
export class ApiError extends Error {
  readonly kind: ApiErrorKind
  readonly status: number | null
  readonly body: ApiErrorBody | null

  constructor(kind: ApiErrorKind, status: number | null, body: ApiErrorBody | null = null) {
    super(body?.message ?? MENSAJES_POR_TIPO[kind])
    this.name = 'ApiError'
    this.kind = kind
    this.status = status
    this.body = body
  }

  static fromStatus(status: number, body: ApiErrorBody | null): ApiError {
    return new ApiError(tipoDesdeStatus(status), status, body)
  }

  static network(): ApiError {
    return new ApiError('network', null)
  }
}
