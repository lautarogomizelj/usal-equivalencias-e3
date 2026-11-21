/** Solicitudes (contrato de la API §6). */
import { ENV } from '@/constants/env'
import { api } from '@/services/api/client'
import type { Page } from '@/types/api'
import type {
  Analisis,
  DatosNuevaSolicitud,
  Documento,
  EventoHistorial,
  FiltrosBandeja,
  ReunionInfo,
  SolicitudDetalle,
  SolicitudResumen,
  TipoDocumento,
} from '@/types/solicitud'

/** `estado` se repite una vez por código y los filtros vacíos no viajan. */
function consulta(page: number, filtros: FiltrosBandeja) {
  const parametros = new URLSearchParams({ page: String(page) })
  for (const [clave, valor] of Object.entries(filtros)) {
    if (Array.isArray(valor)) valor.forEach((item) => parametros.append(clave, item))
    else if (valor !== undefined && valor !== '' && valor !== false) parametros.set(clave, String(valor))
  }
  return parametros.toString()
}

export const solicitudes = {
  listar: (page: number, filtros: FiltrosBandeja = {}, signal?: AbortSignal) =>
    api.get<Page<SolicitudResumen>>(`/solicitudes?${consulta(page, filtros)}`, { signal }),
  crear: (datos: DatosNuevaSolicitud) => api.post<SolicitudDetalle>('/solicitudes', datos),
  obtener: (id: number, signal?: AbortSignal) => api.get<SolicitudDetalle>(`/solicitudes/${id}`, { signal }),
  historial: (id: number, signal?: AbortSignal) =>
    api.get<EventoHistorial[]>(`/solicitudes/${id}/historial`, { signal }),
  documentos: (id: number, signal?: AbortSignal) =>
    api.get<Documento[]>(`/solicitudes/${id}/documentos`, { signal }),
  analisis: (id: number, signal?: AbortSignal) => api.get<Analisis[]>(`/solicitudes/${id}/analisis`, { signal }),
  reunion: (id: number, signal?: AbortSignal) => api.get<ReunionInfo>(`/solicitudes/${id}/reunion`, { signal }),
  /** `reemplazaAId` va en la recarga: es el documento marcado para recargar que el archivo reemplaza. */
  subirDocumento: (id: number, archivo: File, tipo: TipoDocumento, descripcion?: string, reemplazaAId?: number) => {
    const cuerpo = new FormData()
    cuerpo.append('archivo', archivo)
    cuerpo.append('tipo', tipo)
    if (descripcion) cuerpo.append('descripcion', descripcion)
    if (reemplazaAId) cuerpo.append('reemplazaAId', String(reemplazaAId))
    return api.post<Documento>(`/solicitudes/${id}/documentos`, cuerpo)
  },
  quitarDocumento: (id: number, documentoId: number) =>
    api.delete<void>(`/solicitudes/${id}/documentos/${documentoId}`),
  /** Dirección para abrir un archivo. Con mocks, `urlDescarga` ya es una dirección local del navegador. */
  urlArchivo: (urlDescarga: string) => (ENV.USAR_MOCKS ? urlDescarga : `${ENV.API_URL}${urlDescarga}`),
}
