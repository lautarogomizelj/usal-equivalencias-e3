import { createBrowserRouter } from 'react-router'
import { ROLES_CON_BANDEJA } from '@/constants/bandeja'
import { ROUTES } from '@/constants/routes'
import { PublicLayout } from '@/layouts/PublicLayout'
import { ComponentesPage } from '@/pages/dev/ComponentesPage'
import { PrivateLayout } from '@/layouts/PrivateLayout'
import { CarrerasPage } from '@/pages/admin/CarrerasPage'
import { UsuariosPage } from '@/pages/admin/UsuariosPage'
import { NuevaSolicitudPage } from '@/pages/aspirante/NuevaSolicitudPage'
import { EnConstruccionPage } from '@/pages/errors/EnConstruccionPage'
import { NotFoundPage } from '@/pages/errors/NotFoundPage'
import { AuditoriaPage } from '@/pages/privado/AuditoriaPage'
import { PerfilPage } from '@/pages/privado/PerfilPage'
import { HomePage } from '@/pages/public/HomePage'
import { LoginAdminPage } from '@/pages/public/LoginAdminPage'
import { LoginPersonalPage } from '@/pages/public/LoginPersonalPage'
import { RecuperarContraseniaPage } from '@/pages/public/RecuperarContraseniaPage'
import { RegistroPage } from '@/pages/public/RegistroPage'
import { VerificarCorreoPage } from '@/pages/public/VerificarCorreoPage'
import { InicioPorRol, SolicitudesPorRol, SolicitudPorRol } from './PorRol'
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
            element: <RutaProtegida roles={['ASPIRANTE', ...ROLES_CON_BANDEJA]} />,
            children: [
              { path: ROUTES.MIS_SOLICITUDES, element: <SolicitudesPorRol /> },
              { path: ROUTES.SOLICITUD, element: <SolicitudPorRol /> },
            ],
          },
          {
            element: <RutaProtegida roles={['ADMIN', 'REVISOR']} />,
            children: [{ path: ROUTES.AUDITORIA, element: <AuditoriaPage /> }],
          },
          {
            element: <RutaProtegida roles={['ADMIN']} />,
            children: [
              { path: ROUTES.ADMIN_USUARIOS, element: <UsuariosPage /> },
              { path: ROUTES.ADMIN_CARRERAS, element: <CarrerasPage /> },
            ],
          },
          {
            element: <RutaProtegida roles={['ASPIRANTE']} />,
            children: [
              { path: ROUTES.NUEVA_SOLICITUD, element: <NuevaSolicitudPage /> },
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
