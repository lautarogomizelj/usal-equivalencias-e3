import type { LucideIcon } from 'lucide-react'

export interface Pestania {
  clave: string
  texto: string
  Icono: LucideIcon
}

interface PestaniasProps {
  pestanias: Pestania[]
  activa: string
  onCambiar: (clave: string) => void
}

const CLASES_ACTIVA = 'rounded-t-lg border-primary bg-surface font-semibold text-heading'
const CLASES_INACTIVA = 'border-transparent text-text-muted hover:bg-secondary'

/** Pestañas de una pantalla. El panel de cada una lleva `role="tabpanel"` y `aria-labelledby="pestania-<clave>"`. */
export function Pestanias({ pestanias, activa, onCambiar }: PestaniasProps) {
  return (
    <div role="tablist" className="flex overflow-x-auto rounded-t-lg bg-surface-muted">
      {pestanias.map(({ clave, texto, Icono }) => (
        <button
          key={clave}
          type="button"
          role="tab"
          id={`pestania-${clave}`}
          aria-selected={clave === activa}
          className={`flex h-(--size-privado-pestania) flex-1 items-center justify-center gap-3 border-t-2 px-4 text-lg whitespace-nowrap transition-colors lg:text-xl ${clave === activa ? CLASES_ACTIVA : CLASES_INACTIVA}`}
          onClick={() => onCambiar(clave)}
        >
          <Icono aria-hidden className="size-(--size-icon-lg) shrink-0" />
          {texto}
        </button>
      ))}
    </div>
  )
}
