/** Campana de notificaciones (contrato de la API §8.2). */
import { api } from '@/services/api/client'
import type { Page } from '@/types/api'
import type { Notificacion } from '@/types/notificacion'

export const notificaciones = {
  listar: (size: number, signal?: AbortSignal) => api.get<Page<Notificacion>>(`/notificaciones?size=${size}`, { signal }),
  cantidadNoLeidas: (signal?: AbortSignal) =>
    api.get<{ cantidad: number }>('/notificaciones/no-leidas/cantidad', { signal }),
  marcarLeida: (id: number) => api.patch<void>(`/notificaciones/${id}`, { leida: true }),
  marcarTodasLeidas: () => api.post<void>('/notificaciones/marcar-todas-leidas'),
}
