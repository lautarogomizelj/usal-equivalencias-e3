import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { Page } from '@/types/api'
import { Button } from './Button'

interface PaginadorProps {
  pagina: Pick<Page<unknown>, 'page' | 'size' | 'totalElements' | 'totalPages'>
  onCambiar: (page: number) => void
}

/** Paginador de listados. `page` llega desde 0, como en la respuesta del backend. */
export function Paginador({ pagina, onCambiar }: PaginadorProps) {
  const { page, size, totalElements, totalPages } = pagina
  const mostrados = Math.min((page + 1) * size, totalElements)

  return (
    <nav aria-label="Paginación" className="flex items-center justify-between gap-4 text-sm text-text-muted">
      <p>
        Mostrando {mostrados} de {totalElements}
      </p>
      <div className="flex items-center gap-2">
        <Button
          variante="outline"
          tamanio="sm"
          aria-label="Página anterior"
          disabled={page === 0}
          onClick={() => onCambiar(page - 1)}
        >
          <ChevronLeft aria-hidden className="size-(--size-icon-sm)" />
        </Button>
        <span
          aria-current="page"
          className="flex size-(--size-button-sm) items-center justify-center rounded-md bg-primary font-medium text-primary-contrast"
        >
          {page + 1}
        </span>
        <Button
          variante="outline"
          tamanio="sm"
          aria-label="Página siguiente"
          disabled={page + 1 >= totalPages}
          onClick={() => onCambiar(page + 1)}
        >
          <ChevronRight aria-hidden className="size-(--size-icon-sm)" />
        </Button>
      </div>
    </nav>
  )
}
