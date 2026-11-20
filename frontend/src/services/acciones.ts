/**
 * Acciones del trámite (contrato de la API §7). Las que cambian el estado envían el que el usuario tenía en
 * pantalla; tomar el proceso, darse de baja y agregar un faltante no lo cambian.
 */
import { api } from '@/services/api/client'
import type { Documento, Marca, SolicitudDetalle, TipoDocumento } from '@/types/solicitud'

export const acciones = {
  enviarDocumentacion: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/enviar`, { estadoEsperado }),
  cancelarSolicitud: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/cancelar`, { estadoEsperado }),
  tomarProceso: (id: number) => api.post<SolicitudDetalle>(`/solicitudes/${id}/responsables/tomar`),
  darseDeBaja: (id: number) => api.post<SolicitudDetalle>(`/solicitudes/${id}/responsables/baja`),
  /** Guarda las marcas sin cambiar el estado y devuelve los documentos vigentes. */
  guardarMarcas: (id: number, estadoEsperado: string, marcas: Marca[]) =>
    api.put<Documento[]>(`/solicitudes/${id}/documentos/marcas`, { estadoEsperado, marcas }),
  agregarFaltante: (id: number, tipo: TipoDocumento, descripcion: string) =>
    api.post<Documento>(`/solicitudes/${id}/documentos/faltantes`, { tipo, descripcion }),
  aceptarDocumentacion: (id: number, estadoEsperado: string, marcas: Marca[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/aceptar`, { estadoEsperado, marcas }),
  rechazarDocumentacion: (id: number, estadoEsperado: string, comentario: string, marcas: Marca[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/rechazar`, { estadoEsperado, comentario, marcas }),
}
