import { CalendarDays, CircleAlert, User } from 'lucide-react'
import { useState } from 'react'
import { Button, Modal, Textarea } from '@/components/ui'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import { TextoHorario } from './TextoHorario'
import type { VistaSolicitudProps } from './vistas'

const NINGUNO = 'ninguno'

/**
 * Elección de la fecha de reunión, `ASP-REU-FEC-ASP-1` y `-2` (frontend-interfaces.md §4.6.5 y §4.6.6): va
 * dentro del banner y abre el modal `ASP-REU-FEC-M1`. Se elige una sola fecha; si ninguna sirve, en la 1ª ronda
 * se piden otras y en la 2ª se rechaza la ronda. Qué opción hay lo dicen las acciones disponibles.
 */
export function EleccionFecha({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const carga = useCarga((signal) => solicitudes.reunion(detalle.id, signal))
  /** Momento en que se abrió el modal: los horarios anteriores ya no se pueden elegir. `null` es cerrado. */
  const [abiertoEn, setAbiertoEn] = useState<number | null>(null)
  const [eleccion, setEleccion] = useState<number | typeof NINGUNO | null>(null)
  const [motivo, setMotivo] = useState('')
  const [disponibilidad, setDisponibilidad] = useState('')
  const [errores, setErrores] = useState<{ eleccion?: string; motivo?: string; disponibilidad?: string }>({})
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  const { id, accionesDisponibles, responsableAcademico } = detalle
  const estadoEsperado = detalle.estado.codigo
  const ronda = carga.datos?.rondas.at(-1)
  const puedePedirOtras = accionesDisponibles.includes('SOLICITAR_OTRAS_FECHAS')
  const puedeRechazar = accionesDisponibles.includes('RECHAZAR_SEGUNDA_RONDA')

  function elegir(nueva: number | typeof NINGUNO) {
    setEleccion(nueva)
    setErrores({})
  }

  function enviar() {
    if (eleccion === null) {
      return setErrores({ eleccion: 'Elegí un horario o indicá que no podés en ninguno.' })
    }
    if (eleccion !== NINGUNO) {
      return ejecutar(async () =>
        onActualizada(
          await acciones.elegirHorario(id, estadoEsperado, eleccion),
          'Elegiste la fecha de la reunión. Te avisamos por mail cuando esté el enlace para unirte.',
        ),
      )
    }
    if (puedePedirOtras) {
      const faltan = {
        motivo: motivo.trim() ? undefined : 'Contanos por qué no podés en esos horarios.',
        disponibilidad: disponibilidad.trim() ? undefined : 'Indicá cuándo podés reunirte.',
      }
      if (faltan.motivo || faltan.disponibilidad) return setErrores(faltan)
      return ejecutar(async () =>
        onActualizada(
          await acciones.solicitarOtrasFechas(id, estadoEsperado, motivo.trim(), disponibilidad.trim()),
          'Le avisamos a tu responsable académico que necesitás otras fechas.',
        ),
      )
    }
    return ejecutar(async () =>
      onActualizada(
        await acciones.rechazarSegundaRonda(id, estadoEsperado, motivo.trim()),
        'Rechazaste la 2ª ronda de fechas. La fecha se coordina por chat con tu responsable académico.',
      ),
    )
  }

  return (
    <div className="mt-3 flex flex-wrap items-center gap-4">
      {responsableAcademico && (
        <p className="flex min-w-0 flex-1 items-center gap-3 rounded-md bg-surface p-3 text-base text-text">
          <User aria-hidden className="size-(--size-icon-lg) shrink-0 text-primary" />
          <span className="font-medium">Responsable académico</span>
          <span className="min-w-0 border-l border-border pl-3">
            {responsableAcademico.nombre}
            <a href={`mailto:${responsableAcademico.email}`} className="block truncate text-primary underline">
              {responsableAcademico.email}
            </a>
          </span>
        </p>
      )}
      {carga.error ? (
        <p role="alert" className="flex items-center gap-3 text-base text-danger">
          {carga.error}
          <Button variante="outline" tamanio="sm" onClick={carga.reintentar}>
            Reintentar
          </Button>
        </p>
      ) : (
        <Button
          tamanio="lg"
          disabled={!ronda}
          onClick={() => {
            setError(null)
            setAbiertoEn(Date.now())
          }}
        >
          <CalendarDays aria-hidden className="size-(--size-icon-md)" />
          Seleccionar fecha de reunión
        </Button>
      )}

      <Modal
        amplio
        abierto={abiertoEn !== null}
        titulo="Seleccionar fecha de reunión"
        descripcion="Estos son los días y horarios disponibles de tu responsable académico, en hora de Argentina. Elegí uno."
        Icono={CalendarDays}
        onCerrar={() => setAbiertoEn(null)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setAbiertoEn(null)}>
              Cancelar
            </Button>
            <Button disabled={procesando} onClick={enviar}>
              {procesando ? 'Enviando…' : 'Enviar'}
            </Button>
          </>
        }
      >
        <fieldset className="formulario-compacto">
          <legend className="text-lg font-medium text-text">
            Fechas y horarios disponibles
            {ronda && ronda.numero > 1 && ` · Ronda ${ronda.numero} de ${REUNION.RONDAS}`}
          </legend>
          <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {ronda?.horarios.map(({ id: horarioId, inicio, fin }) => {
              const pasado = abiertoEn !== null && Date.parse(inicio) <= abiertoEn
              return (
                <label
                  key={horarioId}
                  className={`flex items-center gap-3 rounded-md border p-3 ${eleccion === horarioId ? 'border-primary bg-secondary' : 'border-border'} ${pasado ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}`}
                >
                  <input
                    type="radio"
                    name="horario"
                    className="size-(--size-icon-md) shrink-0 accent-primary"
                    checked={eleccion === horarioId}
                    disabled={pasado}
                    onChange={() => elegir(horarioId)}
                  />
                  <TextoHorario inicio={inicio} fin={fin} />
                </label>
              )
            })}
          </div>
          <p className="mt-2 text-sm text-text-muted">
            La reunión dura 1 hora y es por Google Meet. Una vez elegida, la fecha no se cambia desde acá.
          </p>

          <p className="my-3 flex items-center gap-4 text-sm text-text-muted before:h-px before:flex-1 before:bg-border after:h-px after:flex-1 after:bg-border">
            O bien
          </p>

          <div className="rounded-md border border-danger/30 bg-danger-soft p-4">
            <label className="flex cursor-pointer items-start gap-3">
              <input
                type="radio"
                name="horario"
                className="mt-1 size-(--size-icon-md) shrink-0 accent-danger"
                checked={eleccion === NINGUNO}
                onChange={() => elegir(NINGUNO)}
              />
              <span>
                <span className="block text-lg font-medium text-danger">
                  {puedeRechazar ? 'Rechazar la 2ª ronda de fechas' : 'No puedo en ninguno de estos horarios'}
                </span>
                <span className="block text-sm text-text">
                  {puedeRechazar
                    ? 'Si tampoco podés en estos horarios, la fecha se coordina con tu responsable académico por el chat de la solicitud. No hay una 3ª ronda.'
                    : 'Indicá el motivo y cuándo podés, para que tu responsable académico te proponga nuevas opciones.'}
                </span>
              </span>
            </label>
            {eleccion === NINGUNO && (
              <div className="mt-3 flex flex-col gap-3">
                <Textarea
                  required={puedePedirOtras}
                  etiqueta={puedePedirOtras ? 'Motivo' : 'Motivo (opcional)'}
                  placeholder="Escribí por qué no podés en ninguno de los horarios disponibles…"
                  maxLength={REUNION.MAX_TEXTO}
                  value={motivo}
                  error={errores.motivo}
                  onChange={(evento) => {
                    setMotivo(evento.target.value)
                    setErrores({})
                  }}
                />
                {puedePedirOtras && (
                  <Textarea
                    required
                    etiqueta="¿Cuándo podés reunirte?"
                    placeholder="Por ejemplo: martes y jueves después de las 16."
                    maxLength={REUNION.MAX_TEXTO}
                    value={disponibilidad}
                    error={errores.disponibilidad}
                    onChange={(evento) => {
                      setDisponibilidad(evento.target.value)
                      setErrores({})
                    }}
                  />
                )}
              </div>
            )}
          </div>
        </fieldset>
        {(errores.eleccion ?? error) && (
          <p role="alert" className="mt-3 flex items-center gap-2 rounded-md bg-danger-soft p-3 text-sm text-danger">
            <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
            {errores.eleccion ?? error}
          </p>
        )}
      </Modal>
    </div>
  )
}
