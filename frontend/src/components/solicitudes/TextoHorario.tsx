import { CalendarDays, Clock } from 'lucide-react'
import { formatearDiaLargo, formatearFranja } from '@/utils/fechas'

/** Día y franja de un horario de reunión, en hora de Argentina. */
export function TextoHorario({ inicio, fin }: { inicio: string; fin: string }) {
  return (
    <span className="flex flex-col gap-1 text-base text-text">
      <span className="flex items-center gap-2">
        <CalendarDays aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
        {formatearDiaLargo(inicio)}
      </span>
      <span className="flex items-center gap-2">
        <Clock aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
        {formatearFranja(inicio, fin)}
      </span>
    </span>
  )
}
