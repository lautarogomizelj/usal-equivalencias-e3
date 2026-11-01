import { useEffect, useState } from 'react'
import { AUTH } from '@/constants/auth'
import { ApiError } from '@/services/api/ApiError'

const SEGUNDOS_EXPIRACION = AUTH.CODIGO_EXPIRACION_MINUTOS * 60

type EstadoCodigo = 'editando' | 'enviando' | 'valido' | 'invalido' | 'expirado' | 'bloqueado' | 'fallido'

interface Acciones {
  validar: (codigo: string) => Promise<void>
  reenviar: () => Promise<void>
}

/** Código de 6 dígitos con su vencimiento, intentos y reenvío (RF-AUTH-001). */
export function useVerificacionCodigo({ validar, reenviar }: Acciones) {
  const [codigo, setCodigo] = useState('')
  const [estado, setEstado] = useState<EstadoCodigo>('editando')
  const [intentos, setIntentos] = useState(0)
  const [mensajeError, setMensajeError] = useState('')
  const [segundos, setSegundos] = useState(SEGUNDOS_EXPIRACION)

  useEffect(() => {
    const reloj = setInterval(() => setSegundos((actual) => Math.max(actual - 1, 0)), 1000)
    return () => clearInterval(reloj)
  }, [])

  function reiniciar() {
    setCodigo('')
    setEstado('editando')
    setSegundos(SEGUNDOS_EXPIRACION)
  }

  function aplicarError(error: unknown) {
    const codigoError = error instanceof ApiError ? error.body?.code : undefined
    if (codigoError === 'CODIGO_INVALIDO') {
      setIntentos(error instanceof ApiError ? (error.body?.intentosRestantes ?? 0) : 0)
      setEstado('invalido')
    } else if (codigoError === 'CODIGO_EXPIRADO') {
      setCodigo('')
      setEstado('expirado')
    } else if (codigoError === 'CUENTA_BLOQUEADA') {
      setEstado('bloqueado')
    } else {
      setMensajeError(error instanceof Error ? error.message : '')
      setEstado('fallido')
    }
  }

  async function cambiar(valor: string) {
    setCodigo(valor)
    setEstado('editando')
    if (valor.length !== AUTH.CODIGO_LONGITUD) return
    setEstado('enviando')
    try {
      await validar(valor)
      setEstado('valido')
    } catch (error) {
      aplicarError(error)
    }
  }

  async function pedirOtroCodigo() {
    try {
      await reenviar()
      reiniciar()
    } catch (error) {
      aplicarError(error)
    }
  }

  const vencido = segundos === 0 && estado !== 'valido' && estado !== 'bloqueado'

  return {
    codigo: vencido ? '' : codigo,
    estado: vencido ? ('expirado' as const) : estado,
    intentos,
    mensajeError,
    segundos,
    cambiar,
    pedirOtroCodigo,
    reiniciar,
    aplicarError,
  }
}

export type VerificacionCodigo = ReturnType<typeof useVerificacionCodigo>
