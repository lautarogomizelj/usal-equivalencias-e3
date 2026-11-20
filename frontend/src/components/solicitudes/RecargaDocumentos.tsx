import { CircleAlert, Paperclip, Send, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Button, CargadorArchivo, Modal, Tabla, type ColumnaTabla } from '@/components/ui'
import { useCargaDocumentos } from '@/hooks/useCargaDocumentos'
import { acciones } from '@/services/acciones'
import { ApiError } from '@/services/api/ApiError'
import { solicitudes } from '@/services/solicitudes'
import type { Documento } from '@/types/solicitud'
import { formatearTamanio } from '@/utils/archivos'
import { tituloDocumento as titulo } from '@/utils/documentos'
import { mensajeDeError } from '@/utils/errores'
import type { VistaSolicitudProps } from './vistas'

/**
 * Recarga selectiva del aspirante, `ASP-DOC-RECH` (frontend-interfaces.md §4.6.3): va dentro del banner y
 * lista solo los documentos que el Académico marcó para recargar. Los correctos no se tocan.
 */
export function RecargaDocumentos({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const carga = useCargaDocumentos(detalle.id, onEstadoCambio)
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const { documentos, subiendo, errores } = carga
  const observados = documentos.filter(({ borrador, estadoRevision }) => !borrador && estadoRevision === 'RECARGAR')
  const nuevoDe = (observado: Documento) =>
    documentos.find(({ borrador, reemplazaAId }) => borrador && reemplazaAId === observado.id)
  const listos = observados.filter(nuevoDe)
  const puedeEnviar =
    detalle.accionesDisponibles.includes('ENVIAR_DOCUMENTACION') &&
    observados.length > 0 &&
    listos.length === observados.length &&
    !subiendo

  async function enviar() {
    setEnviando(true)
    setError(null)
    try {
      onActualizada(await acciones.enviarDocumentacion(detalle.id, detalle.estado.codigo), 'Tu documentación fue enviada.')
    } catch (causa) {
      if (causa instanceof ApiError && (causa.kind === 'conflict' || causa.kind === 'forbidden')) return onEstadoCambio()
      setError(mensajeDeError(causa))
      setEnviando(false)
    }
  }

  const columnas: ColumnaTabla<Documento>[] = [
    {
      clave: 'documento',
      titulo: 'Documento',
      ancho: 'lg:w-1/5',
      render: (observado) => <span className="text-text">{titulo(observado)}</span>,
    },
    {
      clave: 'motivo',
      titulo: 'Motivo del rechazo',
      ancho: 'lg:w-1/4',
      apilada: true,
      render: ({ comentarioRevision, urlDescarga }) =>
        comentarioRevision ?? (urlDescarga ? 'Sin comentario.' : 'Falta cargar este documento.'),
    },
    {
      clave: 'responsable',
      titulo: 'Responsable',
      ancho: 'lg:w-1/5',
      render: ({ revisadoPor }) => revisadoPor ?? detalle.ultimaObservacion?.usuario ?? '–',
    },
    {
      clave: 'nuevo',
      titulo: 'Nueva documentación',
      apilada: true,
      render: (observado) => {
        const fila = String(observado.id)
        const nuevo = nuevoDe(observado)
        const cargar = (archivo: File) =>
          carga.subir(fila, archivo, observado.tipo, observado.descripcion ?? undefined, nuevo, observado.id)

        return (
          <div className="w-full py-2">
            {subiendo === fila ? (
              <p className="text-base text-text">Subiendo archivo…</p>
            ) : nuevo ? (
              <div className="flex flex-wrap items-center gap-3">
                <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
                <p className="min-w-0 flex-1">
                  <a
                    href={solicitudes.urlArchivo(nuevo.urlDescarga!)}
                    target="_blank"
                    rel="noreferrer"
                    className="block truncate font-medium text-primary underline"
                  >
                    {nuevo.nombreArchivo}
                  </a>
                  <span className="block text-sm">{formatearTamanio(nuevo.tamanoBytes ?? 0)}</span>
                </p>
                <CargadorArchivo compacto etiqueta={`Reemplazar ${titulo(observado)}`} onArchivo={cargar} />
                <Button variante="ghost" aria-label={`Quitar ${titulo(observado)}`} onClick={() => carga.quitar(fila, nuevo)}>
                  <Trash2 aria-hidden className="size-(--size-icon-sm)" />
                  Quitar
                </Button>
              </div>
            ) : (
              <div className="flex flex-wrap items-center gap-3">
                <CargadorArchivo
                  compacto
                  texto={observado.urlDescarga ? 'Subir nuevamente' : 'Cargar archivo'}
                  etiqueta={`Cargar ${titulo(observado)}`}
                  onArchivo={cargar}
                />
                <span className="text-sm">PDF o JPG (máx. 10 MB)</span>
              </div>
            )}
            {errores[fila] && (
              <p role="alert" className="mt-1 flex items-center gap-1 text-sm text-danger">
                <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                {errores[fila]}
              </p>
            )}
          </div>
        )
      },
    },
  ]

  return (
    <div className="mt-3 flex flex-col gap-3">
      {detalle.ultimaObservacion && (
        <p>
          <strong>Motivo:</strong> {detalle.ultimaObservacion.comentario}
        </p>
      )}

      <Tabla
        interna
        descripcion="Documentos a recargar"
        columnas={columnas}
        filas={observados}
        obtenerClave={({ id }) => id}
        cargando={carga.cargando}
        error={carga.errorCarga}
        textoVacio="No hay documentos para recargar."
        onReintentar={carga.reintentar}
      />

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-base text-text-muted">
          {listos.length} de {observados.length} {observados.length === 1 ? 'documento listo' : 'documentos listos'} para
          enviar. Los documentos correctos no se modifican.
        </p>
        <Button tamanio="lg" disabled={!puedeEnviar} onClick={() => setConfirmando(true)}>
          <Send aria-hidden className="size-(--size-icon-md)" />
          Enviar documentación
        </Button>
      </div>

      <Modal
        abierto={confirmando}
        titulo="Enviar documentación"
        descripcion="Revisá los archivos antes de enviarlos."
        Icono={Send}
        onCerrar={() => setConfirmando(false)}
        acciones={
          <>
            <Button variante="outline" disabled={enviando} onClick={() => setConfirmando(false)}>
              Volver
            </Button>
            <Button disabled={enviando} onClick={enviar}>
              {enviando ? 'Enviando…' : 'Confirmar envío'}
            </Button>
          </>
        }
      >
        <ul className="divide-y divide-border rounded-md border border-border text-sm">
          {observados.map((observado) => (
            <li key={observado.id} className="flex flex-wrap justify-between gap-2 px-3 py-2">
              <span className="font-medium text-text">{titulo(observado)}</span>
              <span className="text-text-muted">{nuevoDe(observado)?.nombreArchivo}</span>
            </li>
          ))}
        </ul>
        <p className="mt-4 rounded-md bg-warning-soft p-3 text-sm text-warning">
          Los documentos correctos no se modifican. Los que recargás vuelven a revisión.
        </p>
        {error && (
          <p role="alert" className="mt-3 rounded-md bg-danger-soft p-3 text-sm text-danger">
            {error}
          </p>
        )}
      </Modal>
    </div>
  )
}
