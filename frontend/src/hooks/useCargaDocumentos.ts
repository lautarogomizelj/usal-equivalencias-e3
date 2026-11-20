import { useState } from 'react'
import { DOCUMENTOS } from '@/constants/documentos'
import { ApiError } from '@/services/api/ApiError'
import { solicitudes } from '@/services/solicitudes'
import type { Documento, TipoDocumento } from '@/types/solicitud'
import { archivo as validarArchivo } from '@/validations'
import { MENSAJES } from '@/validations/messages'
import { useCarga } from './useCarga'

/**
 * Documentos de una solicitud mientras el aspirante carga (RF-ASP-002) o recarga (RF-ASP-004): cada archivo
 * se sube al elegirlo y queda en borrador. `fila` identifica la fila de la pantalla, para mostrar ahí su
 * progreso y su error. Si el backend responde que la solicitud ya no acepta ese archivo (409), llama a
 * `onEstadoCambio` para que la página se recargue (§0.16.4).
 */
export function useCargaDocumentos(solicitudId: number, onEstadoCambio: () => void) {
  const carga = useCarga((signal) => solicitudes.documentos(solicitudId, signal), solicitudId)
  const [subiendo, setSubiendo] = useState<string | null>(null)
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const documentos = carga.datos ?? []
  /** El límite de 50 MB es sobre lo que se va a enviar: los borradores. */
  const bytesUsados = documentos
    .filter(({ borrador }) => borrador)
    .reduce((suma, { tamanoBytes }) => suma + (tamanoBytes ?? 0), 0)

  function marcarError(fila: string, mensaje: string | null) {
    setErrores((actuales) => ({ ...actuales, [fila]: mensaje }))
  }

  /**
   * Sube un archivo; si `reemplazado` viene, primero quita ese borrador. En la recarga, `reemplazaAId` es el
   * documento observado al que le corresponde el archivo. Devuelve si se subió.
   */
  async function subir(
    fila: string,
    archivo: File,
    tipo: TipoDocumento,
    descripcion?: string,
    reemplazado?: Documento,
    reemplazaAId?: number,
  ) {
    const disponibles = DOCUMENTOS.MAX_BYTES_TOTAL - bytesUsados + (reemplazado?.tamanoBytes ?? 0)
    const invalido =
      validarArchivo(archivo) ?? (archivo.size > disponibles ? MENSAJES.ARCHIVOS_TAMANIO_TOTAL : null)
    marcarError(fila, invalido)
    if (invalido) return false

    setSubiendo(fila)
    try {
      if (reemplazado) await solicitudes.quitarDocumento(solicitudId, reemplazado.id)
      const nuevo = await solicitudes.subirDocumento(solicitudId, archivo, tipo, descripcion, reemplazaAId)
      carga.setDatos((actuales) => [...(actuales ?? []).filter(({ id }) => id !== reemplazado?.id), nuevo])
      return true
    } catch (error) {
      if (error instanceof ApiError && error.kind === 'conflict') onEstadoCambio()
      else {
        marcarError(fila, error instanceof Error ? error.message : null)
        carga.reintentar()
      }
      return false
    } finally {
      setSubiendo(null)
    }
  }

  async function quitar(fila: string, documento: Documento) {
    marcarError(fila, null)
    try {
      await solicitudes.quitarDocumento(solicitudId, documento.id)
      carga.setDatos((actuales) => (actuales ?? []).filter(({ id }) => id !== documento.id))
    } catch (error) {
      if (error instanceof ApiError && error.kind === 'conflict') onEstadoCambio()
      else marcarError(fila, error instanceof Error ? error.message : null)
    }
  }

  return {
    documentos,
    bytesUsados,
    cargando: carga.cargando,
    errorCarga: carga.error,
    reintentar: carga.reintentar,
    subiendo,
    errores,
    marcarError,
    subir,
    quitar,
  }
}
