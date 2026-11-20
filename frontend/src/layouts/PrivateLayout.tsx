import { LogOut } from 'lucide-react'
import { NavLink, Outlet, ScrollRestoration } from 'react-router'
import logoUsal from '@/assets/logo-usal-lema.webp'
import { OlaMenu } from '@/components/privado/OlaMenu'
import { MENU_POR_ROL } from '@/constants/navegacion'
import { NOMBRE_ROL } from '@/constants/roles'
import { useAuth } from '@/store/authContext'
import { iniciales, nombreCompleto } from '@/utils/personas'

const CLASES_ITEM =
  'flex h-(--size-privado-item) shrink-0 items-center gap-3 rounded-md px-4 text-lg whitespace-nowrap transition-colors lg:gap-6'
const CLASES_INACTIVO = 'text-text hover:bg-secondary'
const CLASES_ACTIVO = 'bg-primary text-primary-contrast shadow-md'
/* En escritorio queda aparte del menú, abajo de todo, como una tarjeta sobre la ola. */
const CLASES_SALIR =
  'text-text hover:bg-danger-soft hover:text-danger lg:mt-auto lg:w-full lg:justify-center lg:gap-3 lg:border lg:border-border lg:bg-surface lg:font-medium lg:shadow-md lg:hover:border-danger'

/** Layout del área privada: menú lateral según el rol y datos del usuario (frontend-interfaces.md §2.1 y §2.2). */
export function PrivateLayout() {
  const { sesion, cerrarSesion } = useAuth()
  if (!sesion) return null

  return (
    <div className="escala-privada relative min-h-screen bg-papel lg:pl-(--size-privado-menu)">
      <aside className="bg-papel pt-4 lg:fixed lg:inset-y-0 lg:left-0 lg:w-(--size-privado-menu) lg:flex lg:flex-col lg:overflow-hidden lg:pt-(--size-privado-logo-y) lg:shadow-md">
        <OlaMenu className="pointer-events-none absolute top-0 left-0 hidden h-(--size-privado-ola-alto) max-w-none lg:block" />
        <img
          src={logoUsal}
          alt="Universidad del Salvador. Ciencia a la mente y virtud al corazón."
          className="relative ml-4 w-(--size-privado-logo) max-w-2/5 lg:mx-auto lg:max-w-none"
        />

        <nav aria-label="Menú principal" className="relative mt-4 flex gap-1 overflow-x-auto px-4 lg:mt-8 lg:flex-1 lg:flex-col lg:gap-0 lg:overflow-visible lg:pb-6">
          {(MENU_POR_ROL[sesion.rol] ?? []).map(({ texto, ruta, Icono, conSubrutas }) => (
            <NavLink
              key={ruta}
              to={ruta}
              end={!conSubrutas}
              className={({ isActive }) => `${CLASES_ITEM} ${isActive ? CLASES_ACTIVO : CLASES_INACTIVO}`}
            >
              <Icono aria-hidden className="size-(--size-icon-lg) shrink-0" />
              {texto}
            </NavLink>
          ))}
          <button type="button" className={`${CLASES_ITEM} ${CLASES_SALIR}`} onClick={cerrarSesion}>
            <LogOut aria-hidden className="size-(--size-icon-lg) shrink-0" />
            Cerrar sesión
          </button>
        </nav>
      </aside>

      <div aria-hidden className="hidden h-(--size-privado-barra) border-b border-border lg:block" />

      <Outlet />
      <ScrollRestoration />

      <div className="absolute top-0 right-0 flex h-(--size-privado-barra) items-center gap-3 rounded-l-full bg-surface/60 pr-6 pl-4 backdrop-blur-sm lg:min-w-(--size-privado-tarjeta)">
        <span
          aria-hidden
          className="flex size-(--size-privado-avatar) shrink-0 items-center justify-center rounded-full bg-secondary text-lg font-semibold text-primary"
        >
          {iniciales(sesion)}
        </span>
        <span>
          <span className="block text-lg font-semibold text-heading">{nombreCompleto(sesion)}</span>
          <span className="block text-base text-text-muted">{NOMBRE_ROL[sesion.rol]}</span>
        </span>
      </div>
    </div>
  )
}
