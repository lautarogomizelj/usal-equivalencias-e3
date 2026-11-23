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
  InformeMaterias,
  Pago,
  ReunionInfo,
  SolicitudDetalle,
  SolicitudResumen,
  TipoDocumento,
} from '@/types/solicitud'
import { consulta } from '@/utils/consulta'

/** Con mocks no hay ZIP: se baja un archivo de texto de ejemplo. */
let expedienteDemo: string | undefined

function urlExportacion(ruta: string) {
  if (!ENV.USAR_MOCKS) return `${ENV.API_URL}${ruta}`
  expedienteDemo ??= URL.createObjectURL(new Blob(['Expediente de ejemplo de los mocks.'], { type: 'text/plain' }))
  return expedienteDemo
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
  /** Existe desde `CUPON-SOL`. */
  informe: (id: number, signal?: AbortSignal) =>
    api.get<InformeMaterias>(`/solicitudes/${id}/informe`, { signal }),
  pago: (id: number, signal?: AbortSignal) => api.get<Pago>(`/solicitudes/${id}/pago`, { signal }),
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
  /** Expediente completo en ZIP (RF-PROC-005). Solo para el personal. */
  urlExpediente: (id: number) => urlExportacion(`/solicitudes/${id}/expediente`),
  /** Los expedientes de la bandeja con esos filtros, en un solo ZIP. */
  urlExpedientes: (filtros: FiltrosBandeja) => urlExportacion(`/solicitudes/expedientes?${consulta(0, filtros)}`),
}
