/**
 * Mocks del contrato de la API. Se activan con `VITE_USE_MOCKS=true` y responden
 * desde el cliente HTTP, así los servicios y las pantallas no cambian al pasar a la API real.
 * Cada sprint suma acá las rutas de sus pantallas.
 */
import { ApiError } from '@/services/api/ApiError'
import { CARRERAS, ESTADOS } from './datos'

const DEMORA_MS = 300
const CODIGO_VALIDO = '483920'
const INTENTOS_CODIGO = 3

let intentosRestantes = INTENTOS_CODIGO

function comprobarCodigo(body: unknown) {
  if ((body as { codigo: string }).codigo === CODIGO_VALIDO) {
    intentosRestantes = INTENTOS_CODIGO
    return
  }
  intentosRestantes -= 1
  if (intentosRestantes === 0) {
    throw ApiError.fromStatus(423, { status: 423, code: 'CUENTA_BLOQUEADA' })
  }
  throw ApiError.fromStatus(400, { status: 400, code: 'CODIGO_INVALIDO', intentosRestantes })
}

function reiniciarIntentos() {
  intentosRestantes = INTENTOS_CODIGO
}

const RUTAS: Record<string, (body: unknown) => unknown> = {
  'GET /carreras': () => CARRERAS,
  'GET /estados': () => ESTADOS,
  'POST /auth/registro': (body) => {
    reiniciarIntentos()
    return { aspiranteId: 1, email: (body as { email: string }).email }
  },
  'POST /auth/registro/validar': comprobarCodigo,
  'POST /auth/registro/reenviar-codigo': reiniciarIntentos,
  'POST /auth/password/recuperar': reiniciarIntentos,
  'POST /auth/password/restablecer': comprobarCodigo,
}

export async function resolverMock<T>(metodo: string, ruta: string, body?: unknown): Promise<T> {
  await new Promise((resolver) => setTimeout(resolver, DEMORA_MS))

  const responder = RUTAS[`${metodo} ${ruta.split('?')[0]}`]
  if (!responder) {
    throw ApiError.fromStatus(404, {
      status: 404,
      code: 'NO_ENCONTRADO',
      message: `Sin mock para ${metodo} ${ruta}`,
      path: ruta,
    })
  }
  return responder(body) as T
}
