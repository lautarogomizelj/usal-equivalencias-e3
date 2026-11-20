import { useEffect, useState } from 'react'
import { solicitudes } from '@/services/solicitudes'
import type { Page } from '@/types/api'
import type { FiltrosBandeja, SolicitudResumen } from '@/types/solicitud'

/** Listado paginado de solicitudes con sus estados de carga y de error. Al cambiar los filtros vuelve a la página 1. */
export function useSolicitudes(filtros: FiltrosBandeja = {}) {
  const clave = JSON.stringify(filtros)
  const [claveCargada, setClaveCargada] = useState(clave)
  const [numero, setNumero] = useState(0)
  const [intento, setIntento] = useState(0)
  const [pagina, setPagina] = useState<Page<SolicitudResumen> | null>(null)
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
    solicitudes
      .listar(numero, JSON.parse(clave) as FiltrosBandeja, control.signal)
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
  }, [numero, intento, clave])

  function cargar(pagina: number) {
    setCargando(true)
    setError(null)
    setNumero(pagina)
    setIntento((actual) => actual + 1)
  }

  return { pagina, cargando, error, irAPagina: cargar, reintentar: () => cargar(numero) }
}
