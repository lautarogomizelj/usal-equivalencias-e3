import { ArrowLeft, ChevronRight } from 'lucide-react'
import { Link } from 'react-router'

interface MigasProps {
  /** Pantalla a la que se vuelve. */
  origen: { texto: string; ruta: string }
  actual: string
  className?: string
}

/** Ruta de navegación: de dónde se viene y dónde se está. */
export function Migas({ origen, actual, className = '' }: MigasProps) {
  return (
    <nav aria-label="Ruta de navegación" className={`flex items-center gap-3 text-lg ${className}`}>
      <Link to={origen.ruta} className="flex items-center gap-3 text-text-muted hover:text-primary">
        <ArrowLeft aria-hidden className="size-(--size-icon-md)" />
        {origen.texto}
      </Link>
      <ChevronRight aria-hidden className="size-(--size-icon-md) text-text-muted" />
      <span aria-current="page" className="text-text">
        {actual}
      </span>
    </nav>
  )
}
