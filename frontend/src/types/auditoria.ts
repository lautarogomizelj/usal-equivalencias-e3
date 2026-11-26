/** Evento de `auditoria_sistema` (contrato de la API §8.3). Los estados van siempre por código. */
export interface EventoAuditoria {
  id: number
  timestamp: string
  usuario: { id: number | null; email: string }
  accion: string
  entidad: string
  entidadId: number | null
  estadoCodigoAnterior: string | null
  estadoCodigoNuevo: string | null
  detalles: Record<string, unknown> | null
  comentarios: string | null
  ipOrigen: string | null
}

export interface FiltrosAuditoria {
  /** Parte del email de quien hizo la acción. */
  usuario?: string
  accion?: string
  entidad?: string
  desde?: string
  hasta?: string
  size?: number
}

/** Acciones y entidades que ya tienen eventos: con ellas se arman los filtros. */
export interface OpcionesAuditoria {
  acciones: string[]
  entidades: string[]
}
