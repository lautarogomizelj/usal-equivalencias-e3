/**
 * Mocks del contrato de la API. Se activan con `VITE_USE_MOCKS=true` y responden
 * desde el cliente HTTP, así los servicios y las pantallas no cambian al pasar a la API real.
 * Cada sprint suma acá las rutas de sus pantallas.
 */
import { ApiError } from '@/services/api/ApiError'
import type { Sesion } from '@/types/sesion'
import { rutasAdmin } from './admin'
import { rutasAuditoria } from './auditoria'
import { CARRERAS, ESTADOS, INACTIVAS, PASSWORD_VALIDA, SESION_ASPIRANTE, SESIONES_USAL } from './datos'
import { rutasNotificaciones } from './notificaciones'
import { rutasPerfil } from './perfil'
import { rutasSolicitudes } from './solicitudes'

const DEMORA_MS = 300
const CODIGO_VALIDO = '483920'
const INTENTOS_CODIGO = 3
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

/** Login con usuario y contraseña: el del Admin y el del personal son dos entradas separadas. */
function iniciarSesionUsuario(deAdmin: boolean) {
  return (body: unknown) => {
    const { usuario, password } = body as { usuario: string; password: string }
    const sesion = SESIONES_USAL.find(({ email, rol }) => (rol === 'ADMIN') === deAdmin && email === usuario)
    if (!sesion || INACTIVAS.has(sesion.id) || password !== PASSWORD_VALIDA) throw credencialesInvalidas()
    return abrirSesion(sesion)
  }
}

function sesionActual() {
  const email = sessionStorage.getItem(CLAVE_SESION)
  const sesion = [SESION_ASPIRANTE, ...SESIONES_USAL].find((candidata) => candidata.email === email)
  if (!sesion) throw ApiError.fromStatus(401, { status: 401, code: 'NO_AUTENTICADO' })
  return sesion
}

/** Los números de la ruta se escriben `:id` y llegan en `ids`, en orden; `consulta` trae lo que sigue al `?`. */
const RUTAS: Record<string, (body: unknown, ids: number[], consulta: URLSearchParams) => unknown> = {
  /** Las inactivas solo las ve el Admin, con `?activo=todas`. */
  'GET /carreras': (_, __, consulta) =>
    CARRERAS.filter(({ activo }) => activo || consulta.get('activo') === 'todas').map((carrera) => ({ ...carrera })),
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
  'POST /auth/admin/login': iniciarSesionUsuario(true),
  'POST /auth/personal/login': iniciarSesionUsuario(false),
  'POST /auth/logout': () => sessionStorage.removeItem(CLAVE_SESION),
  'GET /auth/me': () => ({ ...sesionActual() }),
  ...rutasPerfil(sesionActual, abrirSesion),
  ...rutasSolicitudes(sesionActual),
  ...rutasNotificaciones(sesionActual),
  ...rutasAuditoria(sesionActual),
  ...rutasAdmin(sesionActual),
}

export async function resolverMock<T>(metodo: string, ruta: string, body?: unknown): Promise<T> {
  await new Promise((resolver) => setTimeout(resolver, DEMORA_MS))

  const [camino, consulta] = ruta.split('?')
  const responder = RUTAS[`${metodo} ${camino.replace(/\d+/g, ':id')}`]
  if (!responder) {
    throw ApiError.fromStatus(404, {
      status: 404,
      code: 'NO_ENCONTRADO',
      message: `Sin mock para ${metodo} ${ruta}`,
      path: ruta,
    })
  }
  return responder(body, (camino.match(/\d+/g) ?? []).map(Number), new URLSearchParams(consulta)) as T
}
