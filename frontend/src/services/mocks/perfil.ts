/** Mocks de Mi perfil (contrato de la API §4.2). Los cambios viven en memoria y se pierden al recargar. */
import { AUTH } from '@/constants/auth'
import { ApiError } from '@/services/api/ApiError'
import type { Acceso, Perfil, SesionActiva } from '@/types/perfil'
import type { Sesion } from '@/types/sesion'
import { PASSWORD_VALIDA, PERFIL } from './datos'

const HORA_MS = 60 * 60 * 1000
const NAVEGADOR_CHROME = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0 Safari/537.36'
const NAVEGADOR_CELULAR = 'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/131.0 Mobile Safari/537.36'

const perfiles = new Map<string, Perfil>([[PERFIL.email, PERFIL]])

/** Los ids son UUID que solo cambian en el último tramo, que es el que llega en `ids`. */
let sesionesActivas: SesionActiva[] = [
  ['00000000-0000-4000-8000-000000000001', '190.210.14.7', NAVEGADOR_CHROME, 1, true] as const,
  ['00000000-0000-4000-8000-000000000002', '181.46.120.33', NAVEGADOR_CELULAR, 30, false] as const,
].map(([id, ip, userAgent, horas, actual]) => ({
  id,
  ip,
  userAgent,
  inicio: new Date(Date.now() - horas * HORA_MS).toISOString(),
  ultimoUso: new Date(Date.now() - (horas - 1) * HORA_MS).toISOString(),
  actual,
}))

const ACCESOS: Acceso[] = [1, 30, 54, 126].map((horas, indice) => ({
  fecha: new Date(Date.now() - horas * HORA_MS).toISOString(),
  ip: indice % 2 ? '181.46.120.33' : '190.210.14.7',
  duracionMinutos: [12, 47, 95, 8][indice],
}))

function enCampo(campo: string, mensaje: string) {
  return ApiError.fromStatus(400, { status: 400, code: 'VALIDACION', errors: { [campo]: [mensaje] } })
}

function camposEditables({ rol, esOAuth }: Sesion) {
  if (rol === 'REVISOR') return []
  return [...(esOAuth ? [] : ['nombre', 'apellido']), 'telefono', 'avatar', ...(rol === 'ADMIN' ? ['email'] : [])]
}

function perfilDe(sesion: Sesion) {
  if (!perfiles.has(sesion.email)) {
    const { nombre, apellido, email, rol, esOAuth } = sesion
    perfiles.set(email, {
      nombre,
      apellido,
      email,
      telefono: null,
      dni: null,
      avatar: null,
      rol,
      esOAuth,
      camposEditables: camposEditables(sesion),
    })
  }
  return perfiles.get(sesion.email)!
}

function exigirPassword(body: unknown) {
  if ((body as { passwordActual: string }).passwordActual !== PASSWORD_VALIDA) {
    throw enCampo('passwordActual', 'La contraseña actual no es correcta.')
  }
}

/** `abrirSesion` vuelve a guardar la sesión cuando cambia el email que la identifica. */
export function rutasPerfil(sesionActual: () => Sesion, abrirSesion: (sesion: Sesion) => void) {
  return {
    'GET /perfil': () => perfilDe(sesionActual()),
    'PATCH /perfil': (body: unknown) => {
      const sesion = sesionActual()
      const cambios = body as Partial<Pick<Perfil, 'nombre' | 'apellido' | 'telefono'>>
      if (sesion.esOAuth && cambios.nombre !== undefined) throw enCampo('nombre', 'El nombre viene de tu cuenta de Google.')
      const perfil = Object.assign(perfilDe(sesion), cambios)
      sesion.nombre = perfil.nombre
      sesion.apellido = perfil.apellido
      return perfil
    },
    'PUT /perfil/password': (body: unknown) => {
      exigirPassword(body)
      sesionesActivas = sesionesActivas.filter(({ actual }) => actual)
    },
    'PUT /perfil/email': (body: unknown) => {
      exigirPassword(body)
      const { emailNuevo } = body as { emailNuevo: string }
      if (!emailNuevo.endsWith(`@${AUTH.DOMINIO_INSTITUCIONAL}`)) throw enCampo('emailNuevo', 'El email debe ser @usal.edu.ar.')
      const sesion = sesionActual()
      const perfil = perfilDe(sesion)
      perfiles.delete(sesion.email)
      perfil.email = sesion.email = emailNuevo
      perfiles.set(emailNuevo, perfil)
      abrirSesion(sesion)
    },
    'GET /perfil/accesos': () => ACCESOS,
    'GET /perfil/sesiones': () => sesionesActivas,
    'DELETE /perfil/sesiones/:id-:id-:id-:id-:id': (_body: unknown, ids: number[]) => {
      sesionesActivas = sesionesActivas.filter(({ id }) => Number(id.slice(-12)) !== ids[4])
    },
  }
}
