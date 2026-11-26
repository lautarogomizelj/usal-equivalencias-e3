import { useState } from 'react'
import { Button, ModalConfirmacion } from '@/components/ui'
import { useAccion } from '@/hooks/useAccion'
import { acciones } from '@/services/acciones'
import type { VistaSolicitudProps } from './vistas'

/**
 * Pregunta de legalidad, `ASP-INSC-ACEP` (frontend-interfaces.md §4.6.11): va dentro del banner. El aspirante
 * solo responde; no carga archivos en este paso (RF-ASP-009).
 */
export function IndicarLegalizacion({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [respuesta, setRespuesta] = useState<boolean | null>(null)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('INDICAR_LEGALIZACION')) return null

  function preguntar(legalizados: boolean) {
    setError(null)
    setRespuesta(legalizados)
  }

  const confirmar = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.indicarLegalizacion(detalle.id, detalle.estado.codigo, respuesta === true),
        respuesta
          ? 'Indicaste que tus documentos están legalizados. Tu responsable académico los va a analizar.'
          : 'Indicaste que tus documentos todavía no están legalizados. Avisanos desde acá cuando estén listos.',
      ),
    )

  return (
    <div className="mt-3 flex flex-col gap-3">
      <p className="font-medium">¿Tus documentos están legalizados?</p>
      <div className="flex flex-wrap gap-3">
        <Button tamanio="lg" onClick={() => preguntar(true)}>
          Sí, están legalizados
        </Button>
        <Button variante="outline" tamanio="lg" onClick={() => preguntar(false)}>
          No, todavía no
        </Button>
      </div>

      <ModalConfirmacion
        abierto={respuesta !== null}
        titulo={respuesta ? 'Mis documentos están legalizados' : 'Mis documentos no están legalizados'}
        accion={`Solicitud ${detalle.codigoExp}.`}
        consecuencia={
          respuesta
            ? 'Tu responsable académico va a analizar los documentos legalizados. Si alguno tiene observaciones, te lo vamos a pedir de nuevo. No se puede deshacer.'
            : 'La solicitud queda a la espera de que legalices tus documentos. Te enviamos las indicaciones por mail y avisás desde acá cuando estén listos. No se puede deshacer.'
        }
        textoConfirmar="Confirmar"
        cargando={procesando}
        error={error}
        onConfirmar={confirmar}
        onCancelar={() => setRespuesta(null)}
      />
    </div>
  )
}

/** Aviso de que ya legalizó, `ASP-LEG-PEND` (frontend-interfaces.md §4.6.12): va dentro del banner. */
export function AvisarLegalizado({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [confirmando, setConfirmando] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('AVISAR_LEGALIZADO')) return null

  const avisar = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.avisarLegalizado(detalle.id, detalle.estado.codigo),
        'Avisaste que tus documentos ya están legalizados. Tu responsable académico los va a analizar.',
      ),
    )

  return (
    <div className="mt-3">
      <Button
        tamanio="lg"
        onClick={() => {
          setError(null)
          setConfirmando(true)
        }}
      >
        Ya legalicé mis documentos
      </Button>

      <ModalConfirmacion
        abierto={confirmando}
        titulo="Ya legalicé mis documentos"
        accion={`Solicitud ${detalle.codigoExp}.`}
        consecuencia="Tu responsable académico va a analizar los documentos legalizados. Si alguno tiene observaciones, te lo vamos a pedir de nuevo. No se puede deshacer."
        textoConfirmar="Confirmar"
        cargando={procesando}
        error={error}
        onConfirmar={avisar}
        onCancelar={() => setConfirmando(false)}
      />
    </div>
  )
}
