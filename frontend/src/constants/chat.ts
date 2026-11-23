import type { TipoConsulta } from '@/types/chat'

/** Reglas del chat de la solicitud (RF-MSG-002, contrato de la API §8.1). */
export const CHAT = {
  MAX_CONTENIDO: 500,
  MAX_BYTES_ADJUNTO: 5 * 1024 * 1024,
  /** Los formatos de la documentación más PNG (frontend-interfaces.md, Anexo A #16). */
  MIME_ADJUNTO: ['application/pdf', 'image/jpeg', 'image/png'],
  EXTENSIONES_ADJUNTO: ['.pdf', '.jpg', '.jpeg', '.png'],
  /** Cada cuánto se piden los mensajes nuevos mientras el chat está abierto. */
  INTERVALO_MS: 15_000,
  /** Mensajes por pedido: si llega una tanda completa, se pide la siguiente. */
  TANDA: 100,
} as const

export const NOMBRE_TIPO_CONSULTA: Record<TipoConsulta, string> = {
  DOCUMENTACION: 'Documentación',
  ESTADO: 'Estado',
  CALCULOS: 'Cálculos',
  FECHAS: 'Fechas',
  OTRO: 'Otro',
}

export const TIPOS_CONSULTA = (Object.keys(NOMBRE_TIPO_CONSULTA) as TipoConsulta[]).map((valor) => ({
  valor,
  texto: NOMBRE_TIPO_CONSULTA[valor],
}))

/** Cómo se rotula a quien escribió, por el código de su rol. */
export const ROL_EN_CHAT: Record<string, string> = {
  ASPIRANTE: 'Aspirante',
  ACAD: 'Responsable académico',
  SECRE: 'Responsable administrativo',
}
