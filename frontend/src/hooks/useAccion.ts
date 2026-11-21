import { useState } from 'react'
import { ApiError } from '@/services/api/ApiError'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'

/**
 * Ejecuta una acción del trámite y expone su progreso y su error. Si el backend responde que la acción ya no
 * corresponde (409 o 403), llama a `onEstadoCambio` para que la página recargue la solicitud y avise (§0.16.4).
 */
export function useAccion(onEstadoCambio: () => void) {
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function ejecutar(operacion: () => Promise<void>) {
    setProcesando(true)
    setError(null)
    try {
      await operacion()
    } catch (causa) {
      if (causa instanceof ApiError && (causa.kind === 'conflict' || causa.kind === 'forbidden')) {
        return onEstadoCambio()
      }
      // Si el backend rechazó un dato, su mensaje dice cuál y por qué.
      setError(Object.values(erroresPorCampo(causa))[0] ?? mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  return { procesando, error, setError, ejecutar }
}
