import { useRef, type KeyboardEvent } from 'react'
import { AUTH } from '@/constants/auth'

interface CampoCodigoProps {
  valor: string
  conError?: boolean
  deshabilitado?: boolean
  onChange: (valor: string) => void
}

/** Casilleros del código de verificación; avanza solo y acepta pegar el código entero. */
export function CampoCodigo({ valor, conError, deshabilitado, onChange }: CampoCodigoProps) {
  const casilleros = useRef<(HTMLInputElement | null)[]>([])
  const digitos = Array.from({ length: AUTH.CODIGO_LONGITUD }, (_, indice) => valor[indice] ?? '')
  const completo = valor.length === AUTH.CODIGO_LONGITUD

  function escribir(indice: number, texto: string) {
    const nuevos = texto.replace(/\D/g, '')
    if (!nuevos) return
    const codigo = (valor.slice(0, indice) + nuevos).slice(0, AUTH.CODIGO_LONGITUD)
    onChange(codigo)
    casilleros.current[Math.min(codigo.length, AUTH.CODIGO_LONGITUD - 1)]?.focus()
  }

  function borrar(evento: KeyboardEvent, indice: number) {
    if (evento.key !== 'Backspace') return
    evento.preventDefault()
    const destino = digitos[indice] ? indice : indice - 1
    if (destino < 0) return
    onChange(valor.slice(0, destino))
    casilleros.current[destino]?.focus()
  }

  const borde = conError
    ? 'border-danger text-danger'
    : completo
      ? 'border-primary text-heading'
      : 'border-border text-heading focus:border-primary'

  return (
    <div className="flex gap-3">
      {digitos.map((digito, indice) => (
        <input
          key={indice}
          ref={(casillero) => {
            casilleros.current[indice] = casillero
          }}
          autoFocus={indice === 0}
          aria-label={`Dígito ${indice + 1} de ${AUTH.CODIGO_LONGITUD}`}
          aria-invalid={conError || undefined}
          autoComplete={indice === 0 ? 'one-time-code' : 'off'}
          inputMode="numeric"
          disabled={deshabilitado}
          value={digito}
          className={`aspect-square w-full max-w-(--size-codigo) min-w-0 rounded-lg border bg-surface text-center text-3xl font-semibold disabled:bg-surface-muted ${borde}`}
          onFocus={(evento) => evento.target.select()}
          onKeyDown={(evento) => borrar(evento, indice)}
          onChange={(evento) => escribir(indice, evento.target.value.slice(-1))}
          onPaste={(evento) => {
            evento.preventDefault()
            escribir(indice, evento.clipboardData.getData('text'))
          }}
        />
      ))}
    </div>
  )
}
