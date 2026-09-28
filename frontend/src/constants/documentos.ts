/** Reglas de carga documental (RF-SOL-002). */
export const DOCUMENTOS = {
  MIME_PERMITIDOS: ['application/pdf', 'image/jpeg'],
  EXTENSIONES_PERMITIDAS: ['.pdf', '.jpg', '.jpeg'],
  MAX_BYTES_ARCHIVO: 10 * 1024 * 1024,
  MAX_BYTES_TOTAL: 50 * 1024 * 1024,
} as const
