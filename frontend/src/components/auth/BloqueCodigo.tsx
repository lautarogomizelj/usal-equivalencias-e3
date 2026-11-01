import { Clock, RefreshCw } from 'lucide-react'
import { Aviso, Button, CampoCodigo } from '@/components/ui'
import type { VerificacionCodigo } from '@/hooks/useVerificacionCodigo'

interface BloqueCodigoProps {
  verificacion: VerificacionCodigo
  /** Recuperar contraseña permite reenviar en cualquier momento; el registro, tras un error. */
  reenvioLibre?: boolean
}

function formatearTiempo(segundos: number) {
  const minutos = String(Math.floor(segundos / 60)).padStart(2, '0')
  return `${minutos}:${String(segundos % 60).padStart(2, '0')}`
}

/** Casilleros del código, resultado, vencimiento y reenvío. */
export function BloqueCodigo({ verificacion, reenvioLibre }: BloqueCodigoProps) {
  const { codigo, estado, intentos, mensajeError, segundos, cambiar, pedirOtroCodigo } = verificacion
  const conError = estado === 'invalido' || estado === 'bloqueado'
  const enCurso = estado !== 'valido' && estado !== 'bloqueado'

  return (
    <div className="flex flex-col gap-6">
      <CampoCodigo
        valor={codigo}
        conError={conError}
        deshabilitado={estado === 'enviando' || !enCurso}
        onChange={cambiar}
      />

      {estado === 'valido' && <Aviso tipo="exito" mensaje="Código válido. Redirigiendo..." />}
      {estado === 'invalido' && (
        <Aviso
          tipo="error"
          mensaje="El código ingresado no es válido."
          detalle={`Te ${intentos === 1 ? 'queda 1 intento' : `quedan ${intentos} intentos`}.`}
        />
      )}
      {estado === 'expirado' && (
        <Aviso tipo="error" mensaje="El código ha expirado." detalle="Solicitá un nuevo código para continuar." />
      )}
      {estado === 'bloqueado' && (
        <Aviso
          tipo="error"
          mensaje="Tu cuenta quedó bloqueada."
          detalle="Superaste los 3 intentos. Podés volver a intentar en 15 minutos."
        />
      )}
      {estado === 'fallido' && <Aviso tipo="error" mensaje={mensajeError} />}

      {enCurso && estado !== 'expirado' && (
        <p className="flex items-center gap-3 text-xl text-text-muted">
          <Clock aria-hidden className="size-(--size-icon-xl)" />
          El código expira en
          <strong className={estado === 'invalido' ? 'text-danger' : 'text-text-muted'}>
            {formatearTiempo(segundos)}
          </strong>
        </p>
      )}

      {enCurso && (
        <Button
          variante={estado === 'expirado' ? 'primary' : 'outline'}
          tamanio="xl"
          className="w-full"
          disabled={!reenvioLibre && (estado === 'editando' || estado === 'enviando')}
          onClick={pedirOtroCodigo}
        >
          <RefreshCw aria-hidden className="size-(--size-icon-xl)" />
          Reenviar código
        </Button>
      )}
    </div>
  )
}
