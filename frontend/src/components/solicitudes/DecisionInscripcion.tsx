import { CircleAlert, ClipboardCheck, Users } from 'lucide-react'
import { useState } from 'react'
import { Button, ModalConfirmacion, OpcionesRadio, Seccion, Textarea } from '@/components/ui'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import { MENSAJES } from '@/validations/messages'
import { ActualizarAnalisis } from './ActualizarAnalisis'
import { TextoHorario } from './TextoHorario'
import type { VistaSolicitudProps } from './vistas'

type Decision = 'aceptar' | 'rechazar'

const OPCIONES: { valor: Decision; texto: string; detalle: string }[] = [
  {
    valor: 'aceptar',
    texto: 'Aceptar el análisis preliminar y las equivalencias.',
    detalle: 'La inscripción queda aceptada y el aspirante tiene que indicar si sus documentos están legalizados.',
  },
  {
    valor: 'rechazar',
    texto: 'Rechazar la inscripción.',
    detalle: 'El trámite termina acá. El aspirante recibe el motivo.',
  },
]

/**
 * Análisis posterior a la reunión, `ACAD-ANAL-POST` (frontend-interfaces.md §5.6.9): el resultado de la reunión
 * con las notas del Académico, la actualización de la matriz y del horario, y la decisión de inscripción.
 */
export function DecisionInscripcion(props: VistaSolicitudProps) {
  const { detalle, onActualizada, onEstadoCambio } = props
  const reunion = useCarga((signal) => solicitudes.reunion(detalle.id, signal))
  const [decision, setDecision] = useState<Decision | null>(null)
  const [motivo, setMotivo] = useState('')
  const [errorDecision, setErrorDecision] = useState<string | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  const realizada = reunion.datos?.reunion
  const rechaza = decision === 'rechazar'
  const estadoEsperado = detalle.estado.codigo

  function revisar() {
    if (!decision) return setErrorDecision('Elegí si aceptás o rechazás la inscripción.')
    if (rechaza && !motivo.trim()) return setErrorDecision(MENSAJES.MOTIVO_REQUERIDO)
    setError(null)
    setConfirmando(true)
  }

  const guardar = () =>
    ejecutar(async () =>
      rechaza
        ? onActualizada(
            await acciones.rechazarInscripcion(detalle.id, estadoEsperado, motivo.trim()),
            'La inscripción fue rechazada. Le avisamos al aspirante con el motivo.',
          )
        : onActualizada(
            await acciones.aceptarInscripcion(detalle.id, estadoEsperado),
            'La inscripción fue aceptada. Le avisamos al aspirante para que indique si sus documentos están legalizados.',
          ),
    )

  return (
    <>
      <Seccion Icono={Users} titulo="Reunión realizada" descripcion="Cuándo fue y lo que anotó el Académico al finalizarla.">
        {reunion.error ? (
          <div className="rounded-md border border-border px-4 py-6 text-center">
            <p className="text-base text-danger">{reunion.error}</p>
            <Button variante="outline" tamanio="sm" className="mt-3" onClick={reunion.reintentar}>
              Reintentar
            </Button>
          </div>
        ) : reunion.cargando || !realizada ? (
          <span aria-busy className="block h-(--size-tabla-fila) animate-pulse rounded-md bg-surface-muted" />
        ) : (
          <div className="flex flex-col gap-3 rounded-md bg-secondary p-4 lg:flex-row lg:gap-8">
            <TextoHorario inicio={realizada.inicio} fin={realizada.fin} />
            <p className="flex-1 text-base whitespace-pre-line text-text">
              <span className="block font-medium text-primary">Notas de la reunión</span>
              {realizada.notas ?? 'Sin notas.'}
            </p>
          </div>
        )}
      </Seccion>

      <ActualizarAnalisis {...props} />

      {detalle.accionesDisponibles.includes('ACEPTAR_INSCRIPCION') && (
        <Seccion
          Icono={ClipboardCheck}
          titulo="Decisión de inscripción"
          descripcion="Definí si se acepta o se rechaza la inscripción del aspirante. La decisión no se puede cambiar después."
          className="formulario-compacto"
        >
          <OpcionesRadio
            etiqueta="Decisión de inscripción"
            nombre="decision-inscripcion"
            opciones={OPCIONES}
            valor={decision}
            onCambiar={(nueva) => {
              setDecision(nueva)
              setErrorDecision(null)
            }}
          />
          {rechaza && (
            <div className="mt-4">
              <Textarea
                required
                etiqueta="Motivo del rechazo"
                ayuda="El aspirante lo ve en su solicitud y lo recibe por mail."
                maxLength={REUNION.MAX_TEXTO}
                value={motivo}
                error={errorDecision}
                onChange={(evento) => {
                  setMotivo(evento.target.value)
                  setErrorDecision(null)
                }}
              />
            </div>
          )}
          {errorDecision && !rechaza && (
            <p role="alert" className="mt-3 flex items-center gap-1 text-sm text-danger">
              <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
              {errorDecision}
            </p>
          )}
          <footer className="mt-4 flex justify-end">
            <Button tamanio="lg" onClick={revisar}>
              Guardar decisión
            </Button>
          </footer>
        </Seccion>
      )}

      <ModalConfirmacion
        peligrosa={rechaza}
        abierto={confirmando}
        titulo={rechaza ? 'Rechazar la inscripción' : 'Aceptar la inscripción'}
        accion={`Solicitud ${detalle.codigoExp} de ${detalle.aspirante.nombre}.`}
        consecuencia={
          rechaza
            ? 'El trámite termina y no admite más acciones. Se le avisa al aspirante por mail con el motivo. No se puede deshacer.'
            : 'La solicitud pasa a "Inscripción aceptada" y se le avisa al aspirante por mail. No se puede deshacer.'
        }
        textoConfirmar={rechaza ? 'Rechazar la inscripción' : 'Aceptar la inscripción'}
        cargando={procesando}
        error={error}
        onConfirmar={guardar}
        onCancelar={() => setConfirmando(false)}
      />
    </>
  )
}
