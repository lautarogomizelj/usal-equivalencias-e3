/** Chat de la solicitud (contrato de la API §8.1). */
import { CHAT } from '@/constants/chat'
import { api } from '@/services/api/client'
import type { Page } from '@/types/api'
import type { Mensaje, TipoConsulta } from '@/types/chat'

export const chat = {
  /** Del más viejo al más nuevo, a partir del mensaje `despuesDe` (0 trae desde el principio). */
  mensajes: (solicitudId: number, despuesDe: number, signal?: AbortSignal) =>
    api.get<Page<Mensaje>>(`/solicitudes/${solicitudId}/chat/mensajes?despuesDe=${despuesDe}&size=${CHAT.TANDA}`, {
      signal,
    }),
  enviar: (solicitudId: number, contenido: string, tipoConsulta: TipoConsulta, adjunto: File | null) => {
    const cuerpo = new FormData()
    cuerpo.append('contenido', contenido)
    cuerpo.append('tipoConsulta', tipoConsulta)
    if (adjunto) cuerpo.append('adjunto', adjunto)
    return api.post<Mensaje>(`/solicitudes/${solicitudId}/chat/mensajes`, cuerpo)
  },
  marcarLeidos: (solicitudId: number, hastaMensajeId: number) =>
    api.post<void>(`/solicitudes/${solicitudId}/chat/leidos`, { hastaMensajeId }),
}
