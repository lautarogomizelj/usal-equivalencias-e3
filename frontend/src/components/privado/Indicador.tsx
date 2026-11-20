import type { LucideIcon } from 'lucide-react'
import { Link } from 'react-router'

interface IndicadorProps {
  Icono: LucideIcon
  /** `null` mientras se carga. */
  cantidad: number | null
  titulo: string
  descripcion: string
  /** Color de fondo y ancho. */
  clases: string
  /** Si viene, el indicador lleva al listado con ese filtro aplicado. */
  ruta?: string
}

/** Contador de los paneles de inicio: una cantidad con su título y una línea que la explica. */
export function Indicador({ Icono, cantidad, titulo, descripcion, clases, ruta }: IndicadorProps) {
  const contenido = (
    <>
      <span className="flex size-(--size-privado-indicador) shrink-0 items-center justify-center rounded-full bg-surface/60 text-primary">
        <Icono aria-hidden className="size-(--size-icon-xl)" />
      </span>
      <p>
        <span className="block text-5xl leading-none font-medium text-heading">{cantidad ?? '–'}</span>
        <span className="block text-xl font-semibold text-heading">{titulo}</span>
        <span className="block text-base text-text-muted">{descripcion}</span>
      </p>
    </>
  )
  const clasesBase = `flex items-center gap-6 rounded-lg p-4 lg:px-6 lg:py-2 ${clases}`

  return ruta ? (
    <Link to={ruta} className={`${clasesBase} transition-shadow hover:shadow-md`}>
      {contenido}
    </Link>
  ) : (
    <div className={clasesBase}>{contenido}</div>
  )
}
