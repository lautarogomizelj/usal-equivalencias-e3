/**
 * Validaciones compartidas (frontend-interfaces.md §0.4.3).
 * Cada validador devuelve `null` si el valor es válido o el mensaje de error.
 * El backend sigue siendo la fuente de verdad (§0.4.2).
 */
import { AUTH } from '@/constants/auth'
import { DOCUMENTOS } from '@/constants/documentos'
import { MENSAJES } from './messages'

export type ResultadoValidacion = string | null

export function requerido(valor: string): ResultadoValidacion {
  return valor.trim() ? null : MENSAJES.REQUERIDO
}

export function email(valor: string): ResultadoValidacion {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor.trim()) ? null : MENSAJES.EMAIL_INVALIDO
}

export function password(valor: string): ResultadoValidacion {
  const valida =
    valor.length >= AUTH.PASSWORD_MIN_LONGITUD &&
    /[A-Z]/.test(valor) &&
    /\d/.test(valor) &&
    /[^A-Za-z0-9]/.test(valor)
  return valida ? null : MENSAJES.PASSWORD_INVALIDA
}

export function dni(valor: string): ResultadoValidacion {
  return /^\d{7,8}$/.test(valor.trim()) ? null : MENSAJES.DNI_INVALIDO
}

export function telefono(valor: string): ResultadoValidacion {
  return /^\+?[\d\s-]{8,20}$/.test(valor.trim()) ? null : MENSAJES.TELEFONO_INVALIDO
}

export function codigoValidacion(valor: string): ResultadoValidacion {
  return new RegExp(`^\\d{${AUTH.CODIGO_LONGITUD}}$`).test(valor.trim())
    ? null
    : MENSAJES.CODIGO_INVALIDO
}

export function archivo(file: File | null | undefined): ResultadoValidacion {
  if (!file) return MENSAJES.ARCHIVO_REQUERIDO
  if (!(DOCUMENTOS.MIME_PERMITIDOS as readonly string[]).includes(file.type)) {
    return MENSAJES.ARCHIVO_FORMATO
  }
  if (file.size > DOCUMENTOS.MAX_BYTES_ARCHIVO) return MENSAJES.ARCHIVO_TAMANIO
  return null
}

export function archivos(files: File[]): ResultadoValidacion {
  for (const file of files) {
    const error = archivo(file)
    if (error) return error
  }
  const total = files.reduce((suma, file) => suma + file.size, 0)
  return total > DOCUMENTOS.MAX_BYTES_TOTAL ? MENSAJES.ARCHIVOS_TAMANIO_TOTAL : null
}
