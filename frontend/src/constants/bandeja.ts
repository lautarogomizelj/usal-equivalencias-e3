import type { FiltrosBandeja } from '@/types/solicitud'
import { ESTADOS_CON_OBSERVACIONES, ESTADOS_DE_ACCION } from './estados'
import { ROUTES } from './routes'

/** Roles que comparten la bandeja y el detalle (frontend-interfaces.md §5 y §6). */
export const ROLES_CON_BANDEJA = ['ACAD', 'SECRE']

/** Vistas rápidas de la bandeja. Van en la dirección (`?vista=…`) para llegar desde el panel de inicio. */
export const VISTAS_BANDEJA = [
  { valor: 'todas', texto: 'Todas' },
  { valor: 'asignadas', texto: 'Mis asignadas' },
  { valor: 'accion', texto: 'Requieren mi acción' },
  { valor: 'sin-responsable', texto: 'Sin responsable' },
  { valor: 'observaciones', texto: 'Con observaciones' },
  { valor: 'activas', texto: 'Activas' },
]

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
