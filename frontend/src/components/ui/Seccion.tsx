import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

interface SeccionProps {
  Icono: LucideIcon
  titulo: string
  descripcion?: ReactNode
  className?: string
  children: ReactNode
}

/** Tarjeta de contenido con ícono, título y descripción. */
export function Seccion({ Icono, titulo, descripcion, className = '', children }: SeccionProps) {
  return (
    <section className={`rounded-lg bg-surface p-4 shadow-md ${className}`}>
      <header className="flex items-center gap-4">
        <span className="flex size-(--size-privado-circulo) shrink-0 items-center justify-center rounded-full bg-secondary text-primary">
          <Icono aria-hidden className="size-(--size-icon-xl)" />
        </span>
        <div>
          <h2 className="text-2xl lg:text-3xl">{titulo}</h2>
          {descripcion && <p className="text-base text-text-muted lg:text-lg">{descripcion}</p>}
        </div>
      </header>
      <div className="mt-4">{children}</div>
    </section>
  )
}
