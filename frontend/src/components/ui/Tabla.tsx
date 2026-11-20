import type { ReactNode } from 'react'
import { Button } from './Button'

export interface ColumnaTabla<T> {
  clave: string
  titulo: string
  /** Clase de ancho de la columna en escritorio, por ejemplo `lg:w-1/4`. */
  ancho?: string
  /** En pantallas chicas el contenido va debajo del título y no al lado: para celdas con controles. */
  apilada?: boolean
  render: (fila: T) => ReactNode
}

interface TablaProps<T> {
  descripcion: string
  columnas: ColumnaTabla<T>[]
  filas: T[]
  obtenerClave: (fila: T) => string | number
  cargando?: boolean
  error?: string | null
  textoVacio?: string
  /** Tabla dentro de otra tarjeta: con borde en lugar de sombra y filas más bajas. */
  interna?: boolean
  onReintentar?: () => void
}

const FILAS_ESQUELETO = [0, 1, 2]
const CLASES_FILA = 'block p-4 lg:table-row lg:p-0'
const CLASES_CELDA =
  'flex items-center justify-between gap-4 py-1 text-right lg:table-cell lg:py-0 lg:pr-0 lg:pl-4 lg:text-left'

/**
 * Tabla con los estados de carga, vacío y error de frontend-interfaces.md §0.5.
 * En pantallas chicas cada fila se muestra como una tarjeta.
 */
export function Tabla<T>({
  descripcion,
  columnas,
  filas,
  obtenerClave,
  cargando = false,
  error,
  textoVacio = 'No hay resultados.',
  interna = false,
  onReintentar,
}: TablaProps<T>) {
  const mensaje = error ?? (!cargando && filas.length === 0 ? textoVacio : null)
  const clasesCelda = `${CLASES_CELDA} ${interna ? 'lg:h-(--size-tabla-fila-interna)' : 'lg:h-(--size-tabla-fila)'}`

  return (
    <div className={`overflow-x-auto bg-surface ${interna ? 'rounded-md border border-border' : 'rounded-lg shadow-md'}`}>
      <table className="block w-full text-left text-base lg:table" aria-busy={cargando}>
        <caption className="sr-only">{descripcion}</caption>
        <thead className={`hidden text-text lg:table-header-group ${interna ? 'bg-surface-muted' : ''}`}>
          <tr className="border-b border-border">
            {columnas.map(({ clave, titulo, ancho = '' }) => (
              <th
                key={clave}
                scope="col"
                className={`pl-4 font-medium ${interna ? 'h-(--size-tabla-encabezado-interna)' : 'h-(--size-tabla-encabezado)'} ${ancho}`}
              >
                {titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="block divide-y divide-border lg:table-row-group">
          {cargando &&
            FILAS_ESQUELETO.map((fila) => (
              <tr key={fila} className={CLASES_FILA}>
                {columnas.map(({ clave }) => (
                  <td key={clave} className={clasesCelda}>
                    <span className="block h-4 w-full animate-pulse rounded-sm bg-surface-muted" />
                  </td>
                ))}
              </tr>
            ))}
          {!cargando &&
            !error &&
            filas.map((fila) => (
              <tr key={obtenerClave(fila)} className={CLASES_FILA}>
                {columnas.map(({ clave, titulo, apilada, render }) => (
                  <td
                    key={clave}
                    className={`${clasesCelda} text-text-muted ${apilada ? 'max-lg:flex-col max-lg:items-stretch max-lg:gap-1 max-lg:text-left' : ''}`}
                  >
                    <span className="shrink-0 font-medium text-text lg:hidden">{titulo}</span>
                    {render(fila)}
                  </td>
                ))}
              </tr>
            ))}
          {mensaje && !cargando && (
            <tr className="block lg:table-row">
              <td colSpan={columnas.length} className="block px-4 py-8 text-center lg:table-cell">
                <p className={error ? 'text-danger' : 'text-text-muted'}>{mensaje}</p>
                {error && onReintentar && (
                  <Button variante="outline" tamanio="sm" className="mt-3" onClick={onReintentar}>
                    Reintentar
                  </Button>
                )}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
