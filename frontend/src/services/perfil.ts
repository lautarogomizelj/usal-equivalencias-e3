/** Perfil del usuario (contrato de la API §4.2). */
import { api } from '@/services/api/client'
import type { Acceso, Perfil, SesionActiva } from '@/types/perfil'

export const perfil = {
  obtener: (signal?: AbortSignal) => api.get<Perfil>('/perfil', { signal }),
  /** Solo se envían los campos que cambian: un campo ausente no se toca. */
  editar: (cambios: Partial<Pick<Perfil, 'nombre' | 'apellido' | 'telefono'>>) => api.patch<Perfil>('/perfil', cambios),
  cambiarPassword: (passwordActual: string, passwordNueva: string) =>
    api.put<void>('/perfil/password', { passwordActual, passwordNueva }),
  cambiarEmail: (emailNuevo: string, passwordActual: string) =>
    api.put<void>('/perfil/email', { emailNuevo, passwordActual }),
  accesos: (signal?: AbortSignal) => api.get<Acceso[]>('/perfil/accesos', { signal }),
  sesiones: (signal?: AbortSignal) => api.get<SesionActiva[]>('/perfil/sesiones', { signal }),
  cerrarSesion: (id: string) => api.delete<void>(`/perfil/sesiones/${id}`),
}
