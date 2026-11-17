/** Solicitudes (contrato de la API §6). */
import { ENV } from '@/constants/env'
import { api } from '@/services/api/client'
import type { Page } from '@/types/api'
import type {
  DatosNuevaSolicitud,
  Documento,
  EventoHistorial,
  SolicitudDetalle,
  SolicitudResumen,
  TipoDocumento,
} from '@/types/solicitud'

export const solicitudes = {
  listar: (page: number, signal?: AbortSignal) =>
    api.get<Page<SolicitudResumen>>(`/solicitudes?page=${page}`, { signal }),
  crear: (datos: DatosNuevaSolicitud) => api.post<SolicitudDetalle>('/solicitudes', datos),
  obtener: (id: number, signal?: AbortSignal) => api.get<SolicitudDetalle>(`/solicitudes/${id}`, { signal }),
  historial: (id: number, signal?: AbortSignal) =>
    api.get<EventoHistorial[]>(`/solicitudes/${id}/historial`, { signal }),
  documentos: (id: number, signal?: AbortSignal) =>
    api.get<Documento[]>(`/solicitudes/${id}/documentos`, { signal }),
  subirDocumento: (id: number, archivo: File, tipo: TipoDocumento, descripcion?: string) => {
    const cuerpo = new FormData()
    cuerpo.append('archivo', archivo)
    cuerpo.append('tipo', tipo)
    if (descripcion) cuerpo.append('descripcion', descripcion)
    return api.post<Documento>(`/solicitudes/${id}/documentos`, cuerpo)
  },
  quitarDocumento: (id: number, documentoId: number) =>
    api.delete<void>(`/solicitudes/${id}/documentos/${documentoId}`),
  /** Dirección para abrir un archivo. Con mocks, `urlDescarga` ya es una dirección local del navegador. */
  urlArchivo: (urlDescarga: string) => (ENV.USAR_MOCKS ? urlDescarga : `${ENV.API_URL}${urlDescarga}`),
}
