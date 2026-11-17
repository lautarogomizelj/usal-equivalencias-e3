/**
 * Mocks del contrato de la API. Se activan con `VITE_USE_MOCKS=true` y responden
 * desde el cliente HTTP, así los servicios y las pantallas no cambian al pasar a la API real.
 * Cada sprint suma acá las rutas de sus pantallas.
 */
import { ApiError } from '@/services/api/ApiError'
import type { Sesion } from '@/types/sesion'
import { CARRERAS, ESTADOS, PERFIL, SESION_ASPIRANTE, SESIONES_USAL, SOLICITUDES } from './datos'
import { RUTAS_SOLICITUDES } from './solicitudes'

const DEMORA_MS = 300
const CODIGO_VALIDO = '483920'
const INTENTOS_CODIGO = 3
const PASSWORD_VALIDA = 'Demo1234!'
const CLAVE_SESION = 'e3-mock-sesion'

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

function abrirSesion(sesion: Sesion) {
  sessionStorage.setItem(CLAVE_SESION, sesion.email)
  return sesion
}

function credencialesInvalidas() {
  return ApiError.fromStatus(401, { status: 401, code: 'CREDENCIALES_INVALIDAS', intentosRestantes: 2 })
}

function iniciarSesion(body: unknown) {
  if ((body as { password: string }).password !== PASSWORD_VALIDA) throw credencialesInvalidas()
  return abrirSesion(SESION_ASPIRANTE)
}

function iniciarSesionAdmin(body: unknown) {
  const { usuario, password } = body as { usuario: string; password: string }
  const sesion = SESIONES_USAL.find(({ email, rol }) => rol === 'ADMIN' && email === usuario)
  if (!sesion || password !== PASSWORD_VALIDA) throw credencialesInvalidas()
  return abrirSesion(sesion)
}

function iniciarSesionDev(body: unknown) {
  const sesion = SESIONES_USAL.find(({ email, rol }) => rol !== 'ADMIN' && email === (body as { email: string }).email)
  if (!sesion) throw ApiError.fromStatus(403, { status: 403, code: 'USUARIO_NO_REGISTRADO' })
  return abrirSesion(sesion)
}

function sesionActual() {
  const email = sessionStorage.getItem(CLAVE_SESION)
  const sesion = [SESION_ASPIRANTE, ...SESIONES_USAL].find((candidata) => candidata.email === email)
  if (!sesion) throw ApiError.fromStatus(401, { status: 401, code: 'NO_AUTENTICADO' })
  return sesion
}

/** Los números de la ruta se escriben `:id` y llegan en `ids`, en orden. */
const RUTAS: Record<string, (body: unknown, ids: number[]) => unknown> = {
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
  'POST /auth/login': iniciarSesion,
  'POST /auth/admin/login': iniciarSesionAdmin,
  'POST /auth/dev/login': iniciarSesionDev,
  'POST /auth/logout': () => sessionStorage.removeItem(CLAVE_SESION),
  'GET /auth/me': sesionActual,
  'GET /perfil': () => PERFIL,
  'GET /solicitudes': () => ({
    content: SOLICITUDES,
    page: 0,
    size: 20,
    totalElements: SOLICITUDES.length,
    totalPages: 1,
  }),
  ...RUTAS_SOLICITUDES,
}

export async function resolverMock<T>(metodo: string, ruta: string, body?: unknown): Promise<T> {
  await new Promise((resolver) => setTimeout(resolver, DEMORA_MS))

  const camino = ruta.split('?')[0]
  const responder = RUTAS[`${metodo} ${camino.replace(/\d+/g, ':id')}`]
  if (!responder) {
    throw ApiError.fromStatus(404, {
      status: 404,
      code: 'NO_ENCONTRADO',
      message: `Sin mock para ${metodo} ${ruta}`,
      path: ruta,
    })
  }
  return responder(body, (camino.match(/\d+/g) ?? []).map(Number)) as T
}
