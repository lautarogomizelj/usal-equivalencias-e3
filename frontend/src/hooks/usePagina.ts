import { useEffect, useState } from 'react'
import type { Page } from '@/types/api'

/**
 * Listado paginado con sus estados de carga y de error. `pedir` se vuelve a ejecutar al cambiar de página, al
 * reintentar o cuando cambia `clave` (los filtros aplicados), que además vuelve a la página 1.
 */
export function usePagina<T>(pedir: (page: number, signal: AbortSignal) => Promise<Page<T>>, clave = '') {
  const [claveCargada, setClaveCargada] = useState(clave)
  const [numero, setNumero] = useState(0)
  const [intento, setIntento] = useState(0)
  const [pagina, setPagina] = useState<Page<T> | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  if (clave !== claveCargada) {
    setClaveCargada(clave)
    setNumero(0)
    setCargando(true)
    setError(null)
  }

  useEffect(() => {
    const control = new AbortController()
    pedir(numero, control.signal)
      .then((respuesta) => {
        if (control.signal.aborted) return
        setPagina(respuesta)
        setCargando(false)
      })
      .catch((causa: unknown) => {
        if (control.signal.aborted) return
        setError(causa instanceof Error ? causa.message : null)
        setCargando(false)
      })
    return () => control.abort()
    // `pedir` se crea de nuevo en cada render: la carga depende de la página, la clave y el reintento.
    // oxlint-disable-next-line react-hooks/exhaustive-deps
  }, [numero, intento, clave])

  function cargar(nueva: number) {
    setCargando(true)
    setError(null)
    setNumero(nueva)
    setIntento((actual) => actual + 1)
  }

  return { pagina, cargando, error, irAPagina: cargar, reintentar: () => cargar(numero) }
}
