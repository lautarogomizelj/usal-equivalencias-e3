/** Autenticación del aspirante (contrato de la API §4). */
import { api } from '@/services/api/client'

interface DatosRegistro {
  email: string
  password: string
  nombre: string
  telefono: string
  dni: string
}

export const auth = {
  registrar: (datos: DatosRegistro) => api.post<{ aspiranteId: number; email: string }>('/auth/registro', datos),
  validarCodigo: (email: string, codigo: string) => api.post<void>('/auth/registro/validar', { email, codigo }),
  reenviarCodigo: (email: string) => api.post<void>('/auth/registro/reenviar-codigo', { email }),
  recuperarPassword: (email: string) => api.post<void>('/auth/password/recuperar', { email }),
  restablecerPassword: (email: string, codigo: string, passwordNueva: string) =>
    api.post<void>('/auth/password/restablecer', { email, codigo, passwordNueva }),
}
