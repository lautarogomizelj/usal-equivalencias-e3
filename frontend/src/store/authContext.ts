import { createContext, useContext } from 'react'
import type { Sesion } from '@/types/sesion'

export interface AuthContextValor {
  sesion: Sesion | null
  /** `true` mientras se consulta `GET /auth/me` al abrir la aplicación. */
  cargando: boolean
  /** Con `recordarme` la sesión dura 30 días en lugar de 1 (contrato de la API §4.1). */
  ingresar: (email: string, password: string, recordarme?: boolean) => Promise<void>
  ingresarAdmin: (usuario: string, password: string) => Promise<void>
  /** Entra como una cuenta del personal sin pasar por Google. Solo en desarrollo. */
  ingresarDev: (email: string) => Promise<void>
  cerrarSesion: () => Promise<void>
  /** Vuelve a pedir la sesión, por ejemplo después de editar el nombre en Mi perfil. */
  refrescarSesion: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValor | null>(null)

export function useAuth() {
  const valor = useContext(AuthContext)
  if (!valor) throw new Error('useAuth se usa dentro de AuthProvider')
  return valor
}
