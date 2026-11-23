/** Auditoría general y de una solicitud (contrato de la API §8.3). Solo Admin y Revisor. */
import { api } from '@/services/api/client'
import type { Page } from '@/types/api'
import type { EventoAuditoria, FiltrosAuditoria, OpcionesAuditoria } from '@/types/auditoria'
import { consulta } from '@/utils/consulta'

export const auditoria = {
  listar: (page: number, filtros: FiltrosAuditoria = {}, signal?: AbortSignal) =>
    api.get<Page<EventoAuditoria>>(`/auditoria?${consulta(page, filtros)}`, { signal }),
  opciones: (signal?: AbortSignal) => api.get<OpcionesAuditoria>('/auditoria/opciones', { signal }),
  deSolicitud: (solicitudId: number, page: number, signal?: AbortSignal) =>
    api.get<Page<EventoAuditoria>>(`/solicitudes/${solicitudId}/auditoria?page=${page}`, { signal }),
}
