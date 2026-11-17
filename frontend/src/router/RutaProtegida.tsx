import { Navigate, Outlet } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { useAuth } from '@/store/authContext'

/** Deja pasar solo con sesión y, si se indica, solo a esos roles (códigos del backend). */
export function RutaProtegida({ roles }: { roles?: string[] }) {
  const { sesion, cargando } = useAuth()

  if (cargando) return null
  if (!sesion || (roles && !roles.includes(sesion.rol))) return <Navigate to={ROUTES.HOME} replace />
  return <Outlet />
}
