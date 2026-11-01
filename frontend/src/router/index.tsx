import { createBrowserRouter } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { PublicLayout } from '@/layouts/PublicLayout'
import { ComponentesPage } from '@/pages/dev/ComponentesPage'
import { NotFoundPage } from '@/pages/errors/NotFoundPage'
import { HomePage } from '@/pages/public/HomePage'
import { LoginPage } from '@/pages/public/LoginPage'
import { RecuperarContraseniaPage } from '@/pages/public/RecuperarContraseniaPage'
import { RegistroPage } from '@/pages/public/RegistroPage'
import { VerificarCorreoPage } from '@/pages/public/VerificarCorreoPage'

export const router = createBrowserRouter([
  { path: ROUTES.HOME, element: <HomePage /> },
  { path: ROUTES.LOGIN, element: <LoginPage /> },
  { path: ROUTES.REGISTRO, element: <RegistroPage /> },
  { path: ROUTES.VALIDAR_CODIGO, element: <VerificarCorreoPage /> },
  { path: ROUTES.RECUPERAR_PASSWORD, element: <RecuperarContraseniaPage /> },
  {
    element: <PublicLayout />,
    children: [
      ...(import.meta.env.DEV ? [{ path: ROUTES.DEV_COMPONENTES, element: <ComponentesPage /> }] : []),
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
