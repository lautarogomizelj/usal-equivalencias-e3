import { ArrowRight, CircleAlert, Users } from 'lucide-react'
import { useState } from 'react'
import { Button, ModalConfirmacion, OpcionesRadio, Seccion, Textarea } from '@/components/ui'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { acciones } from '@/services/acciones'
import type { VistaSolicitudProps } from './vistas'

type Resultado = 'finalizo' | 'no-asistio'

const OPCIONES: { valor: Resultado; texto: string; detalle: string }[] = [
  {
    valor: 'finalizo',
    texto: 'Sí, la reunión finalizó.',
    detalle: 'La solicitud pasa al análisis posterior a la reunión, donde se decide la inscripción.',
  },
  {
    valor: 'no-asistio',
    texto: 'No, el aspirante no se presentó.',
    detalle: 'La solicitud vuelve a la propuesta de fechas para reprogramar la reunión.',
  },
]

/**
 * Resultado de la reunión, `ACAD-REU-PEND` (frontend-interfaces.md §5.6.8): el Académico responsable indica que
 * finalizó, con notas opcionales, o que el aspirante no se presentó. Las dos cosas llevan doble verificación.
 */
export function ResultadoReunion({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [texto, setTexto] = useState('')
  const [faltaElegir, setFaltaElegir] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('FINALIZAR_REUNION')) return null

  const finalizo = resultado === 'finalizo'
  const estadoEsperado = detalle.estado.codigo

  const confirmar = () =>
    ejecutar(async () =>
      finalizo
        ? onActualizada(
            await acciones.finalizarReunion(detalle.id, estadoEsperado, texto.trim()),
            'Indicaste que la reunión finalizó. Le avisamos al aspirante que su solicitud está en análisis.',
          )
        : onActualizada(
            await acciones.marcarNoAsistio(detalle.id, estadoEsperado, texto.trim()),
            'Indicaste que el aspirante no se presentó. Proponé nuevas fechas para reprogramar la reunión.',
          ),
    )

  return (
    <>
      <Seccion
        Icono={Users}
        titulo="¿La reunión finalizó?"
        descripcion="Indicá si ya realizaste la reunión con el aspirante para avanzar con el trámite."
        className="formulario-compacto"
      >
        <OpcionesRadio
          etiqueta="¿La reunión finalizó?"
          nombre="resultado-reunion"
          opciones={OPCIONES}
          valor={resultado}
          onCambiar={(nuevo) => {
            setResultado(nuevo)
            // Las notas son del personal y el comentario lo lee el aspirante: no se arrastra el texto de una a otro.
            setTexto('')
            setFaltaElegir(false)
          }}
        />
        {resultado && (
          <div className="mt-4">
            <Textarea
              etiqueta={finalizo ? 'Notas de la reunión (opcional)' : 'Comentario para el aspirante (opcional)'}
              ayuda={
                finalizo
                  ? 'Las ve solo el personal, no el aspirante.'
                  : 'El aspirante lo ve en su solicitud y en el historial.'
              }
              maxLength={finalizo ? REUNION.MAX_NOTAS : REUNION.MAX_TEXTO}
              value={texto}
              onChange={(evento) => setTexto(evento.target.value)}
            />
          </div>
        )}
        {faltaElegir && (
          <p role="alert" className="mt-3 flex items-center gap-1 text-sm text-danger">
            <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
            Elegí una de las dos opciones.
          </p>
        )}
        <footer className="mt-4 flex justify-end">
          <Button
            tamanio="lg"
            onClick={() => {
              if (!resultado) return setFaltaElegir(true)
              setError(null)
              setConfirmando(true)
            }}
          >
            Continuar proceso
            <ArrowRight aria-hidden className="size-(--size-icon-md)" />
          </Button>
        </footer>
      </Seccion>

      <ModalConfirmacion
        peligrosa={!finalizo}
        abierto={confirmando}
        titulo={finalizo ? 'Indicar que la reunión finalizó' : 'Indicar que el aspirante no se presentó'}
        accion={`Reunión de la solicitud ${detalle.codigoExp} con ${detalle.aspirante.nombre}.`}
        consecuencia={
          finalizo
            ? 'La solicitud pasa al análisis posterior a la reunión y se le avisa al aspirante por mail. No se puede deshacer.'
            : 'La solicitud vuelve a la propuesta de fechas y se le avisa al aspirante por mail que no se registró su asistencia. No se puede deshacer.'
        }
        textoConfirmar="Confirmar"
        cargando={procesando}
        error={error}
        onConfirmar={confirmar}
        onCancelar={() => setConfirmando(false)}
      />
    </>
  )
}
