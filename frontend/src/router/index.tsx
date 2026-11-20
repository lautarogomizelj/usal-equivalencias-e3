import { createBrowserRouter } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { PublicLayout } from '@/layouts/PublicLayout'
import { ComponentesPage } from '@/pages/dev/ComponentesPage'
import { PrivateLayout } from '@/layouts/PrivateLayout'
import { MisSolicitudesPage } from '@/pages/aspirante/MisSolicitudesPage'
import { NuevaSolicitudPage } from '@/pages/aspirante/NuevaSolicitudPage'
import { SolicitudPage } from '@/pages/aspirante/SolicitudPage'
import { EnConstruccionPage } from '@/pages/errors/EnConstruccionPage'
import { NotFoundPage } from '@/pages/errors/NotFoundPage'
import { PerfilPage } from '@/pages/privado/PerfilPage'
import { HomePage } from '@/pages/public/HomePage'
import { LoginAdminPage } from '@/pages/public/LoginAdminPage'
import { LoginPersonalPage } from '@/pages/public/LoginPersonalPage'
import { RecuperarContraseniaPage } from '@/pages/public/RecuperarContraseniaPage'
import { RegistroPage } from '@/pages/public/RegistroPage'
import { VerificarCorreoPage } from '@/pages/public/VerificarCorreoPage'
import { InicioPorRol } from './InicioPorRol'
import { RutaProtegida } from './RutaProtegida'

/** Pantallas del aspirante que todavía no están construidas. */
const RUTAS_PENDIENTES = [ROUTES.AYUDA]

export const router = createBrowserRouter([
  { path: ROUTES.HOME, element: <HomePage /> },
  { path: ROUTES.REGISTRO, element: <RegistroPage /> },
  { path: ROUTES.VALIDAR_CODIGO, element: <VerificarCorreoPage /> },
  { path: ROUTES.RECUPERAR_PASSWORD, element: <RecuperarContraseniaPage /> },
  { path: ROUTES.LOGIN_PERSONAL, element: <LoginPersonalPage /> },
  { path: ROUTES.LOGIN_ADMIN, element: <LoginAdminPage /> },
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <PrivateLayout />,
        children: [
          { path: ROUTES.DASHBOARD, element: <InicioPorRol /> },
          { path: ROUTES.PERFIL, element: <PerfilPage /> },
          {
            element: <RutaProtegida roles={['ASPIRANTE']} />,
            children: [
              { path: ROUTES.MIS_SOLICITUDES, element: <MisSolicitudesPage /> },
              { path: ROUTES.NUEVA_SOLICITUD, element: <NuevaSolicitudPage /> },
              { path: ROUTES.SOLICITUD, element: <SolicitudPage /> },
              ...RUTAS_PENDIENTES.map((path) => ({ path, element: <EnConstruccionPage /> })),
            ],
          },
        ],
      },
    ],
  },
  {
    element: <PublicLayout />,
    children: [
      ...(import.meta.env.DEV ? [{ path: ROUTES.DEV_COMPONENTES, element: <ComponentesPage /> }] : []),
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
