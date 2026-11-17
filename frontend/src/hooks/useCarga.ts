import { useEffect, useState } from 'react'

/**
 * Pide un dato al backend y expone sus estados de carga y de error.
 * `pedir` se vuelve a ejecutar solo cuando cambia `clave` o se reintenta.
 */
export function useCarga<T>(pedir: (signal: AbortSignal) => Promise<T>, clave: unknown = null) {
  const [datos, setDatos] = useState<T | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const control = new AbortController()
    pedir(control.signal)
      .then((respuesta) => {
        if (control.signal.aborted) return
        setDatos(respuesta)
        setCargando(false)
      })
      .catch((causa: unknown) => {
        if (control.signal.aborted) return
        setError(causa instanceof Error ? causa.message : null)
        setCargando(false)
      })
    return () => control.abort()
    // `pedir` se crea de nuevo en cada render: la carga depende solo de la clave y del reintento.
    // oxlint-disable-next-line react-hooks/exhaustive-deps
  }, [clave, intento])

  function reintentar() {
    setCargando(true)
    setError(null)
    setIntento((actual) => actual + 1)
  }

  return { datos, cargando, error, reintentar, setDatos }
}
