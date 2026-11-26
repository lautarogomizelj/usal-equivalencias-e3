import {
  CircleHelp,
  FileText,
  GraduationCap,
  House,
  ScrollText,
  SquarePlus,
  User,
  Users,
  type LucideIcon,
} from 'lucide-react'
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

/**
 * Académico y Secretaría comparten la bandeja y el detalle (frontend-interfaces.md §5 y §6). El ítem dice
 * "Solicitudes" porque "Solicitudes de equivalencia", como en el diseño, no entra en el ancho del menú.
 */
for (const rol of ['ACAD', 'SECRE']) {
  MENU_POR_ROL[rol] = [
    { texto: 'Inicio', ruta: ROUTES.DASHBOARD, Icono: House },
    { texto: 'Solicitudes', ruta: ROUTES.MIS_SOLICITUDES, Icono: FileText, conSubrutas: true },
    { texto: 'Mi perfil', ruta: ROUTES.PERFIL, Icono: User },
  ]
}

/** El Revisor consulta las solicitudes y la auditoría, sin acciones (frontend-interfaces.md §8). */
MENU_POR_ROL.REVISOR = [
  { texto: 'Inicio', ruta: ROUTES.DASHBOARD, Icono: House },
  { texto: 'Solicitudes', ruta: ROUTES.MIS_SOLICITUDES, Icono: FileText, conSubrutas: true },
  { texto: 'Auditoría', ruta: ROUTES.AUDITORIA, Icono: ScrollText },
  { texto: 'Mi perfil', ruta: ROUTES.PERFIL, Icono: User },
]

/** El Admin suma la gestión de usuarios y de carreras (frontend-interfaces.md §7). */
MENU_POR_ROL.ADMIN = [
  { texto: 'Inicio', ruta: ROUTES.DASHBOARD, Icono: House },
  { texto: 'Solicitudes', ruta: ROUTES.MIS_SOLICITUDES, Icono: FileText, conSubrutas: true },
  { texto: 'Usuarios', ruta: ROUTES.ADMIN_USUARIOS, Icono: Users },
  { texto: 'Carreras', ruta: ROUTES.ADMIN_CARRERAS, Icono: GraduationCap },
  { texto: 'Auditoría', ruta: ROUTES.AUDITORIA, Icono: ScrollText },
  { texto: 'Mi perfil', ruta: ROUTES.PERFIL, Icono: User },
]
