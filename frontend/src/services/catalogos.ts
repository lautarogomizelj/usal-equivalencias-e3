/** Catálogos del backend (contrato de la API §5). */
import { api } from '@/services/api/client'
import type { Carrera } from '@/types/carrera'
import type { Estado } from '@/types/estado'

export const catalogos = {
  carreras: (signal?: AbortSignal) => api.get<Carrera[]>('/carreras', { signal }),
  estados: (signal?: AbortSignal) => api.get<Estado[]>('/estados', { signal }),
}
