import { createBrowserRouter } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { PublicLayout } from '@/layouts/PublicLayout'
import { NotFoundPage } from '@/pages/errors/NotFoundPage'
import { HomePage } from '@/pages/public/HomePage'

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: ROUTES.HOME, element: <HomePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
