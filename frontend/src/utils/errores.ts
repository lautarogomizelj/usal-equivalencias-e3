import { ApiError } from '@/services/api/ApiError'

/** Primer mensaje de cada campo que rechazó el backend (`errors` del contrato de la API §1). */
export function erroresPorCampo(error: unknown): Record<string, string> {
  const errores = error instanceof ApiError ? error.body?.errors : undefined
  return Object.fromEntries(Object.entries(errores ?? {}).map(([campo, mensajes]) => [campo, mensajes[0]]))
}

export function mensajeDeError(error: unknown) {
  return error instanceof Error ? error.message : null
}
