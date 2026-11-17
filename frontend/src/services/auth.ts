/** Autenticación del aspirante, del personal y del Admin (contrato de la API §4). */
import { ENV } from '@/constants/env'
import { api } from '@/services/api/client'
import type { Sesion } from '@/types/sesion'

interface DatosRegistro {
  email: string
  password: string
  nombre: string
  apellido: string
  telefono: string
  dni: string
}

export const auth = {
  login: (email: string, password: string, recordarme: boolean) =>
    api.post<Sesion>('/auth/login', { email, password, recordarme }),
  loginAdmin: (usuario: string, password: string) => api.post<Sesion>('/auth/admin/login', { usuario, password }),
  /** Reemplaza al login con Google en desarrollo: solo existe con el perfil `dev` del backend. */
  loginDev: (email: string) => api.post<Sesion>('/auth/dev/login', { email }),
  /** El login con Google no es una llamada: el navegador va al backend, que redirige a Google y vuelve con la sesión. */
  urlGoogle: `${ENV.API_URL}/auth/google`,
  logout: () => api.post<void>('/auth/logout'),
  me: (signal?: AbortSignal) => api.get<Sesion>('/auth/me', { signal }),
  registrar: (datos: DatosRegistro) => api.post<{ aspiranteId: number; email: string }>('/auth/registro', datos),
  validarCodigo: (email: string, codigo: string) => api.post<void>('/auth/registro/validar', { email, codigo }),
  reenviarCodigo: (email: string) => api.post<void>('/auth/registro/reenviar-codigo', { email }),
  recuperarPassword: (email: string) => api.post<void>('/auth/password/recuperar', { email }),
  restablecerPassword: (email: string, codigo: string, passwordNueva: string) =>
    api.post<void>('/auth/password/restablecer', { email, codigo, passwordNueva }),
}
