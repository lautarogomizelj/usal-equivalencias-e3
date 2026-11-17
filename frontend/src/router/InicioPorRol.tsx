import { DashboardPage } from '@/pages/aspirante/DashboardPage'
import { EnConstruccionPage } from '@/pages/errors/EnConstruccionPage'
import { useAuth } from '@/store/authContext'

/** `/dashboard` es el destino de todos los logins (contrato de la API §4.1): cada rol ve su inicio. */
export function InicioPorRol() {
  const { sesion } = useAuth()
  return sesion?.rol === 'ASPIRANTE' ? <DashboardPage /> : <EnConstruccionPage />
}
