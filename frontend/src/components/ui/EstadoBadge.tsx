import { VARIANTE_POR_ESTADO } from '@/constants/estados'
import type { Estado } from '@/types/estado'
import { Badge } from './Badge'

interface EstadoBadgeProps {
  estado: Pick<Estado, 'codigo' | 'nombre'>
}

/** Badge de estado (frontend-interfaces.md §2.10). Un código desconocido se muestra neutro. */
export function EstadoBadge({ estado }: EstadoBadgeProps) {
  return (
    <Badge variante={VARIANTE_POR_ESTADO[estado.codigo] ?? 'neutro'} titulo={estado.codigo}>
      {estado.nombre}
    </Badge>
  )
}
