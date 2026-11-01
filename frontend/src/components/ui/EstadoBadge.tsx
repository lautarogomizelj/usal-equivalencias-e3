import { CircleAlert, CircleCheck, Clock, Info, type LucideIcon } from 'lucide-react'
import { VARIANTE_POR_ESTADO, type VarianteEstado } from '@/constants/estados'
import type { Estado } from '@/types/estado'

interface EstadoBadgeProps {
  estado: Pick<Estado, 'codigo' | 'nombre'>
}

const VARIANTES: Record<VarianteEstado, { clases: string; Icono: LucideIcon }> = {
  accion: { clases: 'bg-alert-soft text-alert', Icono: CircleAlert },
  espera: { clases: 'bg-warning-soft text-warning', Icono: Clock },
  exito: { clases: 'bg-success-soft text-success', Icono: CircleCheck },
  atencion: { clases: 'bg-danger-soft text-danger', Icono: CircleAlert },
  neutro: { clases: 'bg-surface-muted text-text-muted', Icono: Info },
}

/** Badge de estado (frontend-interfaces.md §2.10). Un código desconocido se muestra neutro. */
export function EstadoBadge({ estado }: EstadoBadgeProps) {
  const { clases, Icono } = VARIANTES[VARIANTE_POR_ESTADO[estado.codigo] ?? 'neutro']

  return (
    <span
      className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ${clases}`}
      title={estado.codigo}
    >
      <Icono aria-hidden className="size-(--size-icon-sm) shrink-0" />
      {estado.nombre}
    </span>
  )
}
