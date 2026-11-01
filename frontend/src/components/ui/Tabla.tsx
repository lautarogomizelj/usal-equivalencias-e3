import type { ReactNode } from 'react'
import { Button } from './Button'

export interface ColumnaTabla<T> {
  clave: string
  titulo: string
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
  onReintentar?: () => void
}

const FILAS_ESQUELETO = [0, 1, 2]

/** Tabla con los estados de carga, vacío y error de frontend-interfaces.md §0.5. */
export function Tabla<T>({
  descripcion,
  columnas,
  filas,
  obtenerClave,
  cargando = false,
  error,
  textoVacio = 'No hay resultados.',
  onReintentar,
}: TablaProps<T>) {
  const mensaje = error ?? (!cargando && filas.length === 0 ? textoVacio : null)

  return (
    <div className="overflow-x-auto rounded-lg border border-border bg-surface">
      <table className="w-full text-left text-sm" aria-busy={cargando}>
        <caption className="sr-only">{descripcion}</caption>
        <thead className="bg-surface-muted text-text">
          <tr>
            {columnas.map(({ clave, titulo }) => (
              <th key={clave} scope="col" className="px-4 py-3 font-medium">
                {titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {cargando &&
            FILAS_ESQUELETO.map((fila) => (
              <tr key={fila}>
                {columnas.map(({ clave }) => (
                  <td key={clave} className="px-4 py-3">
                    <span className="block h-4 animate-pulse rounded-sm bg-surface-muted" />
                  </td>
                ))}
              </tr>
            ))}
          {!cargando &&
            !error &&
            filas.map((fila) => (
              <tr key={obtenerClave(fila)}>
                {columnas.map(({ clave, render }) => (
                  <td key={clave} className="px-4 py-3 text-text-muted">
                    {render(fila)}
                  </td>
                ))}
              </tr>
            ))}
          {mensaje && !cargando && (
            <tr>
              <td colSpan={columnas.length} className="px-4 py-8 text-center">
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
