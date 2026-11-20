/** Autenticación del aspirante, del personal y del Admin (contrato de la API §4). */
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
  /** Provisorio, hasta que esté el ingreso con Google (contrato de la API §4.1). */
  loginPersonal: (usuario: string, password: string) =>
    api.post<Sesion>('/auth/personal/login', { usuario, password }),
  logout: () => api.post<void>('/auth/logout'),
  me: (signal?: AbortSignal) => api.get<Sesion>('/auth/me', { signal }),
  registrar: (datos: DatosRegistro) => api.post<{ aspiranteId: number; email: string }>('/auth/registro', datos),
  validarCodigo: (email: string, codigo: string) => api.post<void>('/auth/registro/validar', { email, codigo }),
  reenviarCodigo: (email: string) => api.post<void>('/auth/registro/reenviar-codigo', { email }),
  recuperarPassword: (email: string) => api.post<void>('/auth/password/recuperar', { email }),
  restablecerPassword: (email: string, codigo: string, passwordNueva: string) =>
    api.post<void>('/auth/password/restablecer', { email, codigo, passwordNueva }),
}
