/**
 * Cliente HTTP central. Todas las llamadas al backend pasan por acá
 * (frontend-interfaces.md §0.9.1); los componentes nunca usan `fetch` directo.
 */
import { ENV } from '@/constants/env'
import type { ApiErrorBody } from '@/types/api'
import { ApiError } from './ApiError'

type Metodo = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

interface Opciones {
  body?: unknown
  signal?: AbortSignal
}

async function request<T>(metodo: Metodo, ruta: string, { body, signal }: Opciones = {}): Promise<T> {
  if (ENV.USAR_MOCKS) {
    const { resolverMock } = await import('@/services/mocks')
    return resolverMock<T>(metodo, ruta, body)
  }

  const esFormData = body instanceof FormData
  const headers: Record<string, string> = {}
  if (!esFormData && body !== undefined) headers['Content-Type'] = 'application/json'
  /* CSRF (contrato de la API §1): el backend deja el token en una cookie y lo espera de vuelta en el header. */
  const tokenCsrf = metodo === 'GET' ? undefined : /(?:^|; )XSRF-TOKEN=([^;]+)/.exec(document.cookie)?.[1]
  if (tokenCsrf) headers['X-XSRF-TOKEN'] = decodeURIComponent(tokenCsrf)
  let respuesta: Response

  try {
    respuesta = await fetch(`${ENV.API_URL}${ruta}`, {
      method: metodo,
      credentials: 'include',
      signal,
      headers,
      body: esFormData ? body : body === undefined ? undefined : JSON.stringify(body),
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw ApiError.network()
  }

  if (!respuesta.ok) {
    const cuerpo = (await respuesta.json().catch(() => null)) as ApiErrorBody | null
    throw ApiError.fromStatus(respuesta.status, cuerpo)
  }

  if (respuesta.status === 204) return undefined as T
  return (await respuesta.json()) as T
}

export const api = {
  get: <T>(ruta: string, opciones?: Omit<Opciones, 'body'>) => request<T>('GET', ruta, opciones),
  post: <T>(ruta: string, body?: unknown, opciones?: Omit<Opciones, 'body'>) =>
    request<T>('POST', ruta, { ...opciones, body }),
  put: <T>(ruta: string, body?: unknown, opciones?: Omit<Opciones, 'body'>) =>
    request<T>('PUT', ruta, { ...opciones, body }),
  patch: <T>(ruta: string, body?: unknown, opciones?: Omit<Opciones, 'body'>) =>
    request<T>('PATCH', ruta, { ...opciones, body }),
  delete: <T>(ruta: string, opciones?: Omit<Opciones, 'body'>) => request<T>('DELETE', ruta, opciones),
}
