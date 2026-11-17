import { useState } from 'react'
import { DOCUMENTOS } from '@/constants/documentos'
import { solicitudes } from '@/services/solicitudes'
import type { Documento, TipoDocumento } from '@/types/solicitud'
import { archivo as validarArchivo } from '@/validations'
import { MENSAJES } from '@/validations/messages'
import { useCarga } from './useCarga'

/**
 * Documentos en borrador de una solicitud (RF-ASP-002): cada archivo se sube al elegirlo.
 * `fila` identifica la fila de la pantalla, para mostrar ahí su progreso y su error.
 */
export function useCargaDocumentos(solicitudId: number) {
  const carga = useCarga((signal) => solicitudes.documentos(solicitudId, signal), solicitudId)
  const [subiendo, setSubiendo] = useState<string | null>(null)
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const documentos = carga.datos ?? []
  const bytesUsados = documentos.reduce((suma, { tamanoBytes }) => suma + tamanoBytes, 0)

  function marcarError(fila: string, mensaje: string | null) {
    setErrores((actuales) => ({ ...actuales, [fila]: mensaje }))
  }

  /** Sube un archivo; si `reemplazado` viene, primero quita ese borrador. Devuelve si se subió. */
  async function subir(fila: string, archivo: File, tipo: TipoDocumento, descripcion?: string, reemplazado?: Documento) {
    const disponibles = DOCUMENTOS.MAX_BYTES_TOTAL - bytesUsados + (reemplazado?.tamanoBytes ?? 0)
    const invalido =
      validarArchivo(archivo) ?? (archivo.size > disponibles ? MENSAJES.ARCHIVOS_TAMANIO_TOTAL : null)
    marcarError(fila, invalido)
    if (invalido) return false

    setSubiendo(fila)
    try {
      if (reemplazado) await solicitudes.quitarDocumento(solicitudId, reemplazado.id)
      const nuevo = await solicitudes.subirDocumento(solicitudId, archivo, tipo, descripcion)
      carga.setDatos((actuales) => [...(actuales ?? []).filter(({ id }) => id !== reemplazado?.id), nuevo])
      return true
    } catch (error) {
      marcarError(fila, error instanceof Error ? error.message : null)
      carga.reintentar()
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
      marcarError(fila, error instanceof Error ? error.message : null)
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
