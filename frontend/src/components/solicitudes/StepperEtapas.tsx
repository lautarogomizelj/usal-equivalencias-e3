import { Check, X } from 'lucide-react'
import { CIERRE_NEGATIVO, ETAPAS } from '@/constants/estados'

const CLASES_CIRCULO = {
  hecha: 'bg-primary text-primary-contrast',
  activa: 'border-2 border-primary bg-secondary text-primary',
  cortada: 'bg-danger text-white',
  pendiente: 'border border-border-strong text-text-muted',
}

const ACLARACION = { hecha: 'completada', activa: 'etapa actual', cortada: 'trámite cerrado', pendiente: 'pendiente' }

/** Avance del trámite en macro-etapas. Es informativo: no navega ni ejecuta acciones (frontend-interfaces.md §2.9). */
export function StepperEtapas({ codigoEstado }: { codigoEstado: string }) {
  const actual = ETAPAS.findIndex(({ estados }) => estados.includes(codigoEstado))
  const cierre = CIERRE_NEGATIVO[codigoEstado]
  if (actual === -1) return null

  return (
    <ol aria-label="Etapas del trámite" className="flex gap-2 overflow-x-auto rounded-lg bg-surface px-4 py-3 shadow-md">
      {ETAPAS.map(({ nombre }, indice) => {
        const situacion = indice < actual ? 'hecha' : indice > actual ? 'pendiente' : cierre ? 'cortada' : 'activa'

        return (
          <li
            key={nombre}
            aria-current={indice === actual ? 'step' : undefined}
            className="flex shrink-0 items-center gap-2 last:flex-none lg:shrink lg:flex-1"
          >
            <span
              aria-hidden
              className={`flex size-(--size-icon-xl) shrink-0 items-center justify-center rounded-full text-sm font-semibold ${CLASES_CIRCULO[situacion]}`}
            >
              {situacion === 'hecha' && <Check className="size-(--size-icon-sm)" />}
              {situacion === 'cortada' && <X className="size-(--size-icon-sm)" />}
              {(situacion === 'activa' || situacion === 'pendiente') && indice + 1}
            </span>
            <span className={`text-base whitespace-nowrap ${situacion === 'pendiente' ? 'text-text-muted' : 'font-medium text-heading'}`}>
              {nombre}
              {situacion === 'cortada' && <span className="block text-sm font-normal text-danger">{cierre}</span>}
              <span className="sr-only"> ({ACLARACION[situacion]})</span>
            </span>
            {indice < ETAPAS.length - 1 && (
              <span aria-hidden className={`h-px min-w-4 flex-1 ${indice < actual ? 'bg-primary' : 'bg-border'}`} />
            )}
          </li>
        )
      })}
    </ol>
  )
}
