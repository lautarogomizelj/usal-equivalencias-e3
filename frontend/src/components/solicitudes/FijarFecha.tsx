import { CalendarCheck, CircleAlert } from 'lucide-react'
import { useState } from 'react'
import { Button, Input, Modal } from '@/components/ui'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { acciones } from '@/services/acciones'
import { hoyEnArgentina } from '@/utils/fechas'
import type { VistaSolicitudProps } from './vistas'

/**
 * "Fijar fecha", `ACAD-REU-CHAT` (frontend-interfaces.md §5.6.7): va dentro del banner. El modal pregunta la
 * fecha que el Académico y el aspirante pautaron por chat, que no tiene que caer en las franjas de las rondas.
 */
export function FijarFecha({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [abierto, setAbierto] = useState(false)
  const [dia, setDia] = useState('')
  const [hora, setHora] = useState('')
  const [errorFecha, setErrorFecha] = useState<string | null>(null)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('FIJAR_FECHA')) return null

  function fijar() {
    if (!dia || !hora) return setErrorFecha('Indicá el día y la hora que pautaron.')
    const inicio = `${dia}T${hora}:00${REUNION.DESFASE_HORARIO}`
    if (Date.parse(inicio) <= Date.now()) return setErrorFecha('La reunión tiene que ser a futuro.')
    setErrorFecha(null)
    return ejecutar(async () =>
      onActualizada(
        await acciones.fijarFecha(detalle.id, detalle.estado.codigo, inicio),
        'La fecha de la reunión quedó fijada. Cargá el enlace de Meet para avisarle al aspirante.',
      ),
    )
  }

  return (
    <div className="mt-3">
      <Button
        tamanio="lg"
        onClick={() => {
          setError(null)
          setAbierto(true)
        }}
      >
        <CalendarCheck aria-hidden className="size-(--size-icon-md)" />
        Fijar fecha
      </Button>

      <Modal
        abierto={abierto}
        titulo="Fijar la fecha de la reunión"
        descripcion={`Indicá el día y la hora que pautaron por chat con ${detalle.aspirante.nombre}.`}
        Icono={CalendarCheck}
        onCerrar={() => setAbierto(false)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setAbierto(false)}>
              Cancelar
            </Button>
            <Button disabled={procesando} onClick={fijar}>
              {procesando ? 'Procesando…' : 'Fijar fecha'}
            </Button>
          </>
        }
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            required
            type="date"
            etiqueta="Día"
            min={hoyEnArgentina()}
            value={dia}
            onChange={(evento) => {
              setDia(evento.target.value)
              setErrorFecha(null)
            }}
          />
          <Input
            required
            type="time"
            etiqueta="Hora de inicio"
            ayuda="Hora de Argentina. La reunión dura 1 hora."
            value={hora}
            onChange={(evento) => {
              setHora(evento.target.value)
              setErrorFecha(null)
            }}
          />
        </div>
        <p className="mt-4 rounded-md bg-warning-soft p-3 text-sm text-warning">
          La solicitud pasa a tener la reunión pactada. Después cargás el enlace de Meet: al guardarlo se le avisa la
          fecha al aspirante por mail.
        </p>
        {(errorFecha ?? error) && (
          <p role="alert" className="mt-3 flex items-center gap-2 rounded-md bg-danger-soft p-3 text-sm text-danger">
            <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
            {errorFecha ?? error}
          </p>
        )}
      </Modal>
    </div>
  )
}
