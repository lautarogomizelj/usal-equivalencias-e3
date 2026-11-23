import { CalendarDays, CircleAlert, Link, Send, Video } from 'lucide-react'
import { useState } from 'react'
import { Badge, Button, Input, ModalConfirmacion, Seccion } from '@/components/ui'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import { formatearFecha } from '@/utils/fechas'
import { ResultadoReunion } from './ResultadoReunion'
import { SelectorHorarios } from './SelectorHorarios'
import { TextoHorario } from './TextoHorario'
import type { VistaSolicitudProps } from './vistas'

const ENLACE_INVALIDO = `Pegá el enlace de Google Meet (${REUNION.PREFIJO_MEET}…).`

/**
 * Etapa de reunión vista por el personal (frontend-interfaces.md §5.6.5 a §5.6.8): las rondas de fechas, lo que
 * pidió el aspirante y la reunión pactada. El Académico responsable propone la 2ª ronda (`ACAD-REU-FEC-ACAD`),
 * carga el enlace de Meet e indica el resultado (`ACAD-REU-PEND`); para el resto es de solo lectura.
 */
export function ReunionPersonal(props: VistaSolicitudProps) {
  const { detalle, onActualizada, onEstadoCambio } = props
  const carga = useCarga((signal) => solicitudes.reunion(detalle.id, signal))
  const [horarios, setHorarios] = useState<string[]>([])
  const [errorHorarios, setErrorHorarios] = useState<string | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  /** `null` mientras no se toca el campo: muestra el enlace que ya estaba cargado. */
  const [enlace, setEnlace] = useState<string | null>(null)
  const [errorEnlace, setErrorEnlace] = useState<string | null>(null)
  const [guardado, setGuardado] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  const info = carga.datos
  const puedeProponer = detalle.accionesDisponibles.includes('PROPONER_SEGUNDA_RONDA')
  const puedeCargarEnlace = detalle.accionesDisponibles.includes('CARGAR_ENLACE_MEET')
  const ultimaRonda = info?.rondas.at(-1)
  // La última reunión puede ser una anterior a la que el aspirante faltó: pactada es solo la que sigue pendiente.
  const pactada = info?.reunion?.resultado === 'PENDIENTE' ? info.reunion : null
  const ausencia =
    detalle.estado.codigo === 'REU-FEC-ACAD' && info?.reunion?.resultado === 'NO_ASISTIO' ? info.reunion : null
  const enlaceActual = enlace ?? pactada?.enlaceMeet ?? ''

  function revisarRonda() {
    if (!horarios.length) return setErrorHorarios('Agregá al menos un horario para la reunión.')
    setError(null)
    setConfirmando(true)
  }

  const proponer = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.proponerSegundaRonda(detalle.id, detalle.estado.codigo, horarios),
        'La 2ª ronda de fechas fue enviada. Le avisamos al aspirante para que elija un horario.',
      ),
    )

  function guardarEnlace() {
    const limpio = enlaceActual.trim()
    if (!limpio.startsWith(REUNION.PREFIJO_MEET) || /\s/.test(limpio)) return setErrorEnlace(ENLACE_INVALIDO)
    setErrorEnlace(null)
    setGuardado(false)
    return ejecutar(async () => {
      carga.setDatos(await acciones.cargarEnlaceMeet(detalle.id, limpio))
      setEnlace(null)
      setGuardado(true)
    })
  }

  return (
    <>
      <Seccion
        Icono={CalendarDays}
        titulo="Reunión con el aspirante"
        descripcion="Fechas propuestas, elección del aspirante y enlace de la reunión. Los horarios son de Argentina."
      >
        {carga.error ? (
          <div className="rounded-md border border-border px-4 py-6 text-center">
            <p className="text-base text-danger">{carga.error}</p>
            <Button variante="outline" tamanio="sm" className="mt-3" onClick={carga.reintentar}>
              Reintentar
            </Button>
          </div>
        ) : carga.cargando || !info ? (
          <span aria-busy className="block h-(--size-tabla-fila) animate-pulse rounded-md bg-surface-muted" />
        ) : (
          <div className="flex flex-col gap-4">
            {pactada && (
              <div className="flex flex-col gap-3 rounded-md bg-secondary p-4">
                <p className="text-lg font-medium text-primary">Reunión pactada</p>
                <div className="flex flex-wrap items-center gap-x-8 gap-y-2">
                  <TextoHorario inicio={pactada.inicio} fin={pactada.fin} />
                  <p className="flex items-center gap-2 text-base text-text">
                    <Video aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
                    Google Meet
                  </p>
                </div>
                {puedeCargarEnlace ? (
                  <div className="grid gap-3 sm:grid-cols-4 sm:items-start">
                    <div className="sm:col-span-3">
                      <Input
                        Icono={Link}
                        etiqueta="Enlace de Meet"
                        ayuda="Al guardarlo se le avisa al aspirante por mail. Lo podés corregir después."
                        placeholder={`${REUNION.PREFIJO_MEET}abc-defg-hij`}
                        maxLength={REUNION.MAX_TEXTO}
                        value={enlaceActual}
                        error={errorEnlace ?? error}
                        onChange={(evento) => {
                          setEnlace(evento.target.value)
                          setErrorEnlace(null)
                          setGuardado(false)
                        }}
                      />
                    </div>
                    {/* La etiqueta vacía alinea el botón con el campo. */}
                    <div className="flex flex-col gap-1">
                      <span aria-hidden className="hidden text-sm sm:block">
                        &nbsp;
                      </span>
                      <Button disabled={procesando || !enlaceActual.trim()} onClick={guardarEnlace}>
                        {procesando ? 'Guardando…' : 'Guardar enlace'}
                      </Button>
                    </div>
                    {guardado && (
                      <p role="status" className="text-base text-success sm:col-span-4">
                        El enlace se guardó y le avisamos al aspirante.
                      </p>
                    )}
                  </div>
                ) : pactada.enlaceMeet ? (
                  <a
                    href={pactada.enlaceMeet}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-2 self-start text-base text-primary underline"
                  >
                    <Link aria-hidden className="size-(--size-icon-sm) shrink-0" />
                    {pactada.enlaceMeet}
                  </a>
                ) : (
                  <p className="text-base text-text-muted">Todavía no se cargó el enlace de Meet.</p>
                )}
              </div>
            )}

            {ausencia && (
              <p className="flex items-center gap-2 rounded-md bg-warning-soft p-4 text-base text-text">
                <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0 text-warning" />
                El aspirante no se presentó a la reunión del {formatearFecha(ausencia.inicio)}. Hay que proponerle
                nuevas fechas.
              </p>
            )}

            {!pactada && !ausencia && ultimaRonda?.motivoOtrasFechas && (
              <dl className="grid gap-3 rounded-md bg-warning-soft p-4 text-base text-text sm:grid-cols-2">
                <div>
                  <dt className="font-medium text-warning">Por qué no puede en las fechas propuestas</dt>
                  <dd className="whitespace-pre-line">{ultimaRonda.motivoOtrasFechas}</dd>
                </div>
                <div>
                  <dt className="font-medium text-warning">Cuándo puede reunirse</dt>
                  <dd className="whitespace-pre-line">{ultimaRonda.disponibilidadAspirante}</dd>
                </div>
              </dl>
            )}

            {puedeProponer && (
              <div className="flex flex-col gap-4 rounded-md border border-border p-4">
                <p className="text-lg font-medium text-text">
                  Proponé la 2ª ronda de fechas
                  <span className="block text-base font-normal text-text-muted">
                    Hasta {REUNION.MAX_HORARIOS} horarios de 1 hora, entre las {REUNION.PRIMERA_HORA}:00 y las{' '}
                    {REUNION.ULTIMA_HORA + 1}:00, desde mañana y dentro de los próximos {REUNION.DIAS_HACIA_ADELANTE} días.
                    Es la última ronda: si el aspirante la rechaza, la fecha se pauta por chat.
                  </span>
                </p>
                <SelectorHorarios
                  horarios={horarios}
                  error={errorHorarios}
                  onCambiar={(nuevos) => {
                    setHorarios(nuevos)
                    setErrorHorarios(null)
                  }}
                />
                <Button tamanio="lg" className="self-end" onClick={revisarRonda}>
                  <Send aria-hidden className="size-(--size-icon-md)" />
                  Sugerir 2ª ronda de fechas
                </Button>
              </div>
            )}

            {/* La ronda vigente va primero. */}
            {info.rondas.toReversed().map(({ numero, horarios: propuestos }) => (
              // Si el aspirante faltó a la reunión se vuelve a proponer una 2ª ronda: el número se puede repetir.
              <div key={propuestos[0]?.id ?? numero}>
                <p className="text-lg font-medium text-text">
                  Ronda {numero} de {REUNION.RONDAS}
                </p>
                <ul className="mt-2 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                  {propuestos.map(({ id, inicio, fin, elegido }) => (
                    <li
                      key={id}
                      className={`flex flex-wrap items-center justify-between gap-3 rounded-md border p-3 ${elegido ? 'border-success bg-success-soft' : 'border-border'}`}
                    >
                      <TextoHorario inicio={inicio} fin={fin} />
                      {elegido && <Badge variante="exito">Elegido</Badge>}
                    </li>
                  ))}
                </ul>
              </div>
            ))}

            {info.rondas.length === 0 && (
              <p className="flex items-center gap-2 rounded-md border border-border px-4 py-6 text-base text-text-muted">
                <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                Todavía no se propusieron fechas para la reunión.
              </p>
            )}
          </div>
        )}
      </Seccion>

      <ResultadoReunion {...props} />

      <ModalConfirmacion
        abierto={confirmando}
        titulo="Sugerir la 2ª ronda de fechas"
        accion={`Vas a proponer ${horarios.length === 1 ? '1 horario' : `${horarios.length} horarios`} para la reunión de la solicitud ${detalle.codigoExp}.`}
        consecuencia="Se le avisa al aspirante por mail para que elija uno. Es la última ronda: no vas a poder proponer otras fechas."
        textoConfirmar="Enviar al aspirante"
        cargando={procesando}
        error={error}
        onConfirmar={proponer}
        onCancelar={() => setConfirmando(false)}
      />
    </>
  )
}
