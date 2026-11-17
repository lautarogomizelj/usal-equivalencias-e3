import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import '@fontsource-variable/inter'
import '@fontsource/dm-serif-display'
import '@fontsource/dm-serif-display/400-italic.css'
import { router } from '@/router'
import { AuthProvider } from '@/store/AuthProvider'
import '@/styles/global.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
)
