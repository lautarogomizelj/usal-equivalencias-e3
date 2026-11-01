import { CircleAlert } from 'lucide-react'
import type { ReactNode } from 'react'

export interface CampoProps {
  etiqueta: string
  ayuda?: string
  error?: string | null
  grande?: boolean
  etiquetaOculta?: boolean
}

interface CampoInternoProps extends CampoProps {
  id: string
  requerido?: boolean
  extra?: ReactNode
  children: ReactNode
}

/** Etiqueta, ayuda y mensaje de error compartidos por los controles de formulario. */
export function Campo({ id, etiqueta, ayuda, error, grande, etiquetaOculta, requerido, extra, children }: CampoInternoProps) {
  const clasesEtiqueta = grande ? 'text-xl font-semibold text-heading' : 'text-sm font-medium text-text'

  return (
    <div className={`flex flex-col ${grande ? 'gap-2' : 'gap-1'}`}>
      <label htmlFor={id} className={etiquetaOculta ? 'sr-only' : clasesEtiqueta}>
        {etiqueta}
        {requerido && (
          <span aria-hidden className="ml-1 text-danger">
            *
          </span>
        )}
      </label>
      {children}
      {error && (
        <p id={`${id}-error`} className={`flex items-center gap-1 text-danger ${grande ? 'text-base' : 'text-xs'}`}>
          <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
          {error}
        </p>
      )}
      {(ayuda || extra) && (
        <div className="flex justify-between gap-2 text-xs text-text-muted">
          <p id={`${id}-ayuda`}>{ayuda}</p>
          {extra}
        </div>
      )}
    </div>
  )
}
