import type { FiltrosBandeja } from '@/types/solicitud'
import { ESTADOS_CON_OBSERVACIONES, ESTADOS_DE_ACCION } from './estados'
import { ROUTES } from './routes'

/** Roles que comparten la bandeja y el detalle (frontend-interfaces.md §5, §6, §7.0 y §8.2). */
export const ROLES_CON_BANDEJA = ['ACAD', 'SECRE', 'ADMIN', 'REVISOR']

/** Roles que toman procesos: las vistas "Mis asignadas", "Requieren mi acción" y "Sin responsable" son solo suyas. */
export const ROLES_CON_PROCESO = ['ACAD', 'SECRE']

/** Vistas rápidas de la bandeja. Van en la dirección (`?vista=…`) para llegar desde el panel de inicio. */
export const VISTAS_BANDEJA = [
  { valor: 'todas', texto: 'Todas' },
  { valor: 'asignadas', texto: 'Mis asignadas' },
  { valor: 'accion', texto: 'Requieren mi acción' },
  { valor: 'sin-responsable', texto: 'Sin responsable' },
  { valor: 'observaciones', texto: 'Con observaciones' },
  { valor: 'activas', texto: 'Activas' },
]

const VISTAS_DE_PROCESO = ['asignadas', 'accion', 'sin-responsable']

/** Las vistas que le sirven a cada rol: Admin y Revisor no tienen procesos propios. */
export function vistasDe(rol: string) {
  return ROLES_CON_PROCESO.includes(rol)
    ? VISTAS_BANDEJA
    : VISTAS_BANDEJA.filter(({ valor }) => !VISTAS_DE_PROCESO.includes(valor))
}

/** Filtros del contrato de la API (§6.2) que arma cada vista para el rol de quien mira. */
export function filtrosDeVista(vista: string, rol: string): FiltrosBandeja {
  switch (vista) {
    case 'asignadas':
      return { asignadas: true }
    case 'accion':
      return { asignadas: true, estado: ESTADOS_DE_ACCION[rol] ?? [] }
    case 'sin-responsable':
      return { sinResponsable: true }
    case 'observaciones':
      return { estado: ESTADOS_CON_OBSERVACIONES }
    case 'activas':
      return { finalizadas: false }
    default:
      return {}
  }
}

export function rutaBandeja(vista: string) {
  return `${ROUTES.MIS_SOLICITUDES}?vista=${vista}`
}
