import { useEffect, useState, type ReactNode } from 'react'
import { auth } from '@/services/auth'
import type { Sesion } from '@/types/sesion'
import { AuthContext } from './authContext'

/** Sesión del usuario para toda la aplicación (contrato de la API §4). */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    const control = new AbortController()
    auth
      .me(control.signal)
      .then(setSesion)
      .catch(() => {})
      .finally(() => {
        if (!control.signal.aborted) setCargando(false)
      })
    return () => control.abort()
  }, [])

  async function ingresar(email: string, password: string, recordarme = false) {
    setSesion(await auth.login(email, password, recordarme))
  }

  async function ingresarAdmin(usuario: string, password: string) {
    setSesion(await auth.loginAdmin(usuario, password))
  }

  async function ingresarDev(email: string) {
    setSesion(await auth.loginDev(email))
  }

  async function cerrarSesion() {
    await auth.logout()
    setSesion(null)
  }

  async function refrescarSesion() {
    setSesion(await auth.me())
  }

  return (
    <AuthContext value={{ sesion, cargando, ingresar, ingresarAdmin, ingresarDev, cerrarSesion, refrescarSesion }}>
      {children}
    </AuthContext>
  )
}
