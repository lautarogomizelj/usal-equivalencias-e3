/** Acciones del trámite (contrato de la API §7). Todas envían el estado que el usuario tenía en pantalla. */
import { api } from '@/services/api/client'
import type { SolicitudDetalle } from '@/types/solicitud'

export const acciones = {
  enviarDocumentacion: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/enviar`, { estadoEsperado }),
  cancelarSolicitud: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/cancelar`, { estadoEsperado }),
}
