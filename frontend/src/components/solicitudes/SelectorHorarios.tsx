import { CalendarPlus, CircleAlert, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Button, Input, Select } from '@/components/ui'
import { finDeReunion, REUNION } from '@/constants/reunion'
import { formatearDiaLargo, formatearFranja, hoyEnArgentina, sumarDias } from '@/utils/fechas'
import { TextoHorario } from './TextoHorario'

const dosDigitos = (hora: number) => String(hora).padStart(2, '0')

const HORAS = Array.from({ length: REUNION.ULTIMA_HORA - REUNION.PRIMERA_HORA + 1 }, (_, indice) => {
  const hora = REUNION.PRIMERA_HORA + indice
  return { valor: dosDigitos(hora), texto: `${dosDigitos(hora)}:00 a ${dosDigitos(hora + 1)}:00 hs` }
})

interface SelectorHorariosProps {
  /** Inicio de cada horario propuesto, en ISO 8601 y ordenados. */
  horarios: string[]
  error?: string | null
  onCambiar: (horarios: string[]) => void
}

/**
 * Fechas de reunión que propone el Académico (RF-ACAD-002 y 003): hasta 7 horarios de 1 hora, entre las 10:00
 * y las 18:00 de Argentina, desde mañana y dentro de los próximos 30 días.
 */
export function SelectorHorarios({ horarios, error, onCambiar }: SelectorHorariosProps) {
  const [dia, setDia] = useState('')
  const [hora, setHora] = useState(HORAS[0].valor)
  const [aviso, setAviso] = useState<string | null>(null)
  const hoy = hoyEnArgentina()
  const desde = sumarDias(hoy, 1)
  const hasta = sumarDias(hoy, REUNION.DIAS_HACIA_ADELANTE)
  const completo = horarios.length >= REUNION.MAX_HORARIOS
  const mensaje = aviso ?? error

  function agregar() {
    if (!dia) return setAviso('Elegí el día de la reunión.')
    if (dia < desde || dia > hasta) {
      return setAviso(`Las fechas van desde mañana hasta dentro de ${REUNION.DIAS_HACIA_ADELANTE} días.`)
    }
    const inicio = `${dia}T${hora}:00:00${REUNION.DESFASE_HORARIO}`
    if (horarios.includes(inicio)) return setAviso('Ese horario ya está en la lista.')
    setAviso(null)
    onCambiar([...horarios, inicio].sort())
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="grid gap-3 sm:grid-cols-3 sm:items-end">
        <Input
          type="date"
          etiqueta="Día"
          min={desde}
          max={hasta}
          value={dia}
          disabled={completo}
          onChange={(evento) => {
            setDia(evento.target.value)
            setAviso(null)
          }}
        />
        <Select
          etiqueta="Horario (hora de Argentina)"
          opciones={HORAS}
          value={hora}
          disabled={completo}
          onChange={(evento) => {
            setHora(evento.target.value)
            setAviso(null)
          }}
        />
        <Button variante="outline" disabled={completo} onClick={agregar}>
          <CalendarPlus aria-hidden className="size-(--size-icon-sm)" />
          Agregar horario
        </Button>
      </div>

      {mensaje && (
        <p role="alert" className="flex items-center gap-1 text-sm text-danger">
          <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
          {mensaje}
        </p>
      )}

      {horarios.length === 0 ? (
        <p className="rounded-md border border-dashed border-border-strong px-4 py-6 text-center text-base text-text-muted">
          Todavía no agregaste horarios.
        </p>
      ) : (
        <ul className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {horarios.map((inicio) => (
            <li key={inicio} className="flex items-center justify-between gap-3 rounded-md border border-border p-3">
              <TextoHorario inicio={inicio} fin={finDeReunion(inicio)} />
              <Button
                variante="ghost"
                aria-label={`Quitar ${formatearDiaLargo(inicio)}, ${formatearFranja(inicio, finDeReunion(inicio))}`}
                onClick={() => {
                  setAviso(null)
                  onCambiar(horarios.filter((otro) => otro !== inicio))
                }}
              >
                <Trash2 aria-hidden className="size-(--size-icon-sm)" />
              </Button>
            </li>
          ))}
        </ul>
      )}

      <p className="text-sm text-text-muted">
        {horarios.length} de {REUNION.MAX_HORARIOS} horarios. Cada reunión dura 1 hora.
      </p>
    </div>
  )
}
