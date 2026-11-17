/** Perfil del usuario (contrato de la API §4.2). */
import { api } from '@/services/api/client'
import type { Perfil } from '@/types/perfil'

export const perfil = {
  obtener: (signal?: AbortSignal) => api.get<Perfil>('/perfil', { signal }),
}
