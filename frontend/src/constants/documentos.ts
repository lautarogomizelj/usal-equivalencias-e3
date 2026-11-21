import type { EstadoRevision, TipoAnalisis, TipoDocumento } from '@/types/solicitud'
import type { VarianteEstado } from './estados'

/** Reglas de carga documental (RF-SOL-002). */
export const DOCUMENTOS = {
  MIME_PERMITIDOS: ['application/pdf', 'image/jpeg'],
  EXTENSIONES_PERMITIDAS: ['.pdf', '.jpg', '.jpeg'],
  MAX_BYTES_ARCHIVO: 10 * 1024 * 1024,
  MAX_BYTES_TOTAL: 50 * 1024 * 1024,
} as const

/** Nombre a mostrar de cada tipo de documento (contrato de la API §6.5). */
export const NOMBRE_TIPO_DOCUMENTO: Record<TipoDocumento, string> = {
  ANALITICO: 'Certificado analítico',
  PLAN: 'Plan de estudios',
  PROGRAMA: 'Programa de la asignatura',
  OTRO: 'Otro documento',
}

/** Nombre a mostrar de los documentos que carga el Académico en el análisis preliminar (contrato de la API §6.6). */
export const NOMBRE_ANALISIS: Record<TipoAnalisis, string> = {
  MATRIZ_EQUIVALENCIAS: 'Matriz tentativa de equivalencias',
  HORARIO_CURSADA: 'Posible horario de cursada',
}

/** Marca de revisión de un documento (frontend-interfaces.md §2.8). */
export const MARCA_REVISION: Record<EstadoRevision, { texto: string; variante: VarianteEstado }> = {
  PENDIENTE: { texto: 'Pendiente de revisión', variante: 'espera' },
  OK: { texto: 'Correcto', variante: 'exito' },
  RECARGAR: { texto: 'Debe recargar', variante: 'atencion' },
}
