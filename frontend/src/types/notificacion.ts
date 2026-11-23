/** Aviso de la campana (contrato de la API §8.2): el mismo texto que salió por mail. */
export interface Notificacion {
  id: number
  titulo: string
  mensaje: string
  /** Solicitud a la que lleva el aviso, si es sobre un trámite. */
  solicitudId: number | null
  leida: boolean
  fechaCreacion: string
}
