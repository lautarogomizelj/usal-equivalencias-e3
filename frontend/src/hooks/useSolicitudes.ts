import { useEffect, useState } from 'react'
import { solicitudes } from '@/services/solicitudes'
import type { Page } from '@/types/api'
import type { SolicitudResumen } from '@/types/solicitud'

/** Listado paginado de solicitudes con sus estados de carga y de error. */
export function useSolicitudes() {
  const [numero, setNumero] = useState(0)
  const [intento, setIntento] = useState(0)
  const [pagina, setPagina] = useState<Page<SolicitudResumen> | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const control = new AbortController()
    solicitudes
      .listar(numero, control.signal)
      .then((respuesta) => {
        setPagina(respuesta)
        setCargando(false)
      })
      .catch((causa: unknown) => {
        if (control.signal.aborted) return
        setError(causa instanceof Error ? causa.message : null)
        setCargando(false)
      })
    return () => control.abort()
  }, [numero, intento])

  function cargar(pagina: number) {
    setCargando(true)
    setError(null)
    setNumero(pagina)
    setIntento((actual) => actual + 1)
  }

  return { pagina, cargando, error, irAPagina: cargar, reintentar: () => cargar(numero) }
}
