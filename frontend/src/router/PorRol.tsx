import { ROLES_CON_BANDEJA } from '@/constants/bandeja'
import { DashboardPage } from '@/pages/aspirante/DashboardPage'
import { MisSolicitudesPage } from '@/pages/aspirante/MisSolicitudesPage'
import { SolicitudPage } from '@/pages/aspirante/SolicitudPage'
import { EnConstruccionPage } from '@/pages/errors/EnConstruccionPage'
import { BandejaPage } from '@/pages/personal/BandejaPage'
import { InicioPersonalPage } from '@/pages/personal/InicioPersonalPage'
import { SolicitudPersonalPage } from '@/pages/personal/SolicitudPersonalPage'
import { useAuth } from '@/store/authContext'

function useTieneBandeja() {
  const { sesion } = useAuth()
  return ROLES_CON_BANDEJA.includes(sesion?.rol ?? '')
}

/** `/dashboard` es el destino de todos los logins (contrato de la API §4.1): cada rol ve su inicio. */
export function InicioPorRol() {
  const { sesion } = useAuth()
  const tieneBandeja = useTieneBandeja()
  if (sesion?.rol === 'ASPIRANTE') return <DashboardPage />
  return tieneBandeja ? <InicioPersonalPage /> : <EnConstruccionPage />
}

/** `/solicitudes`: el aspirante ve las suyas; Académico y Secretaría, la bandeja. */
export function SolicitudesPorRol() {
  return useTieneBandeja() ? <BandejaPage /> : <MisSolicitudesPage />
}

/** `/solicitudes/:id`: la misma solicitud se ve distinta según quién la abre (§0.16). */
export function SolicitudPorRol() {
  return useTieneBandeja() ? <SolicitudPersonalPage /> : <SolicitudPage />
}
