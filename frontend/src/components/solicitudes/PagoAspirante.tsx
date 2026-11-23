import { CircleAlert, Download, Paperclip, Send, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Button, CargadorArchivo, ModalConfirmacion } from '@/components/ui'
import { useAccion } from '@/hooks/useAccion'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import { formatearTamanio } from '@/utils/archivos'
import { archivo as validarArchivo } from '@/validations'
import { InformeMaterias } from './InformeMaterias'
import type { VistaSolicitudProps } from './vistas'

/**
 * Etapa de pago del aspirante, de `ASP-CUPON-SOL` a `ASP-E3-INI` (frontend-interfaces.md §4.6.16 a §4.6.20): va
 * dentro del banner. Muestra el informe de materias y el cupón, y en `CUPON-PAG` y `PAG-REENV` deja cargar el
 * comprobante (RF-ASP-006 y RF-ASP-007).
 */
export function PagoAspirante({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const { datos: pago } = useCarga((signal) => solicitudes.pago(detalle.id, signal))
  const [comprobante, setComprobante] = useState<File | null>(null)
  const [errorArchivo, setErrorArchivo] = useState<string | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  const puedeEnviar = detalle.accionesDisponibles.includes('ENVIAR_COMPROBANTE')
  const enviado = pago?.comprobantes.at(-1)
  const rechazado = detalle.estado.codigo === 'PAG-REENV' ? enviado : undefined

  function elegir(archivo: File) {
    const invalido = validarArchivo(archivo)
    setErrorArchivo(invalido)
    if (!invalido) setComprobante(archivo)
  }

  const enviar = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.enviarComprobante(detalle.id, detalle.estado.codigo, comprobante!),
        'Tu comprobante fue enviado. Secretaría va a verificar el pago.',
      ),
    )

  return (
    <div className="mt-3 flex flex-col gap-3">
      <InformeMaterias solicitudId={detalle.id} />

      {rechazado?.comentarioSecretaria && (
        <p className="rounded-md bg-surface p-3 text-base text-text">
          <strong>Motivo del rechazo:</strong> {rechazado.comentarioSecretaria}
        </p>
      )}

      {pago?.cupon && (
        <div className="flex flex-wrap items-center gap-3">
          <a
            href={solicitudes.urlArchivo(pago.cupon.urlDescarga)}
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-(--size-button-lg) items-center justify-center gap-2 rounded-md border border-primary bg-surface px-6 text-base font-medium text-primary transition-colors hover:bg-secondary"
          >
            <Download aria-hidden className="size-(--size-icon-md)" />
            Descargar cupón
          </a>
          {enviado && !rechazado && (
            <a
              href={solicitudes.urlArchivo(enviado.urlDescarga)}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 text-base text-primary underline"
            >
              <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0" />
              Comprobante enviado: {enviado.nombreArchivo}
            </a>
          )}
        </div>
      )}

      {puedeEnviar && (
        <div className="flex flex-col gap-2 rounded-md bg-surface p-4">
          <p className="text-base font-medium text-text">
            {rechazado ? 'Cargá un comprobante nuevo' : 'Cargá el comprobante de pago'}
          </p>
          <div className="flex flex-wrap items-center gap-3">
            {comprobante ? (
              <>
                <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
                <p className="min-w-0 flex-1 text-base text-text">
                  <span className="block truncate font-medium">{comprobante.name}</span>
                  <span className="block text-sm text-text-muted">{formatearTamanio(comprobante.size)}</span>
                </p>
                <Button variante="ghost" aria-label="Quitar el comprobante" onClick={() => setComprobante(null)}>
                  <Trash2 aria-hidden className="size-(--size-icon-sm)" />
                  Quitar
                </Button>
              </>
            ) : (
              <>
                <CargadorArchivo compacto texto="Elegir archivo" etiqueta="Cargar el comprobante de pago" onArchivo={elegir} />
                <span className="flex-1 text-sm text-text-muted">PDF o JPG (máx. 10 MB)</span>
              </>
            )}
            <Button
              tamanio="lg"
              disabled={!comprobante}
              onClick={() => {
                setError(null)
                setConfirmando(true)
              }}
            >
              <Send aria-hidden className="size-(--size-icon-md)" />
              Enviar comprobante
            </Button>
          </div>
          {errorArchivo && (
            <p role="alert" className="flex items-center gap-1 text-sm text-danger">
              <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
              {errorArchivo}
            </p>
          )}
        </div>
      )}

      <ModalConfirmacion
        abierto={confirmando}
        titulo="Enviar el comprobante de pago"
        accion={`Vas a enviar "${comprobante?.name ?? ''}" como comprobante de la solicitud ${detalle.codigoExp}.`}
        consecuencia="Secretaría va a verificar el pago. Si hay algún problema, te vamos a pedir que lo cargues de nuevo."
        textoConfirmar="Enviar comprobante"
        cargando={procesando}
        error={error}
        onConfirmar={enviar}
        onCancelar={() => setConfirmando(false)}
      />
    </div>
  )
}
