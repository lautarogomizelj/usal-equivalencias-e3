import { CircleHelp, FileText, House, SquarePlus, User, type LucideIcon } from 'lucide-react'
import { ROUTES } from './routes'

export interface ItemMenu {
  texto: string
  ruta: string
  Icono: LucideIcon
  /** El ítem también queda marcado en las pantallas que cuelgan de su ruta. */
  conSubrutas?: boolean
}

/** Menú lateral de cada rol, por el código que envía el backend (frontend-interfaces.md §2.2 y §4.1). */
export const MENU_POR_ROL: Record<string, ItemMenu[]> = {
  ASPIRANTE: [
    { texto: 'Inicio', ruta: ROUTES.DASHBOARD, Icono: House },
    { texto: 'Mis solicitudes', ruta: ROUTES.MIS_SOLICITUDES, Icono: FileText, conSubrutas: true },
    { texto: 'Nueva solicitud', ruta: ROUTES.NUEVA_SOLICITUD, Icono: SquarePlus },
    { texto: 'Mi perfil', ruta: ROUTES.PERFIL, Icono: User },
    { texto: 'Ayuda', ruta: ROUTES.AYUDA, Icono: CircleHelp },
  ],
}

/** Los demás roles todavía no tienen pantallas propias: por ahora ven el inicio y su perfil. */
for (const rol of ['ACAD', 'SECRE', 'REVISOR', 'ADMIN']) {
  MENU_POR_ROL[rol] = [
    { texto: 'Inicio', ruta: ROUTES.DASHBOARD, Icono: House },
    { texto: 'Mi perfil', ruta: ROUTES.PERFIL, Icono: User },
  ]
}
