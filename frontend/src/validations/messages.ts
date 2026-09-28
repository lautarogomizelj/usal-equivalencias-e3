/** Mensajes de error reutilizables (frontend-interfaces.md §0.4.4). */
export const MENSAJES = {
  REQUERIDO: 'Este campo es obligatorio.',
  EMAIL_INVALIDO: 'Ingresá un email válido.',
  PASSWORD_INVALIDA:
    'La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.',
  DNI_INVALIDO: 'Ingresá un DNI válido (7 u 8 dígitos, sin puntos).',
  TELEFONO_INVALIDO: 'Ingresá un teléfono válido.',
  CODIGO_INVALIDO: 'El código debe tener 6 dígitos.',
  ARCHIVO_REQUERIDO: 'Tenés que adjuntar un archivo.',
  ARCHIVO_FORMATO: 'Formato no permitido. Solo se aceptan archivos PDF o JPG.',
  ARCHIVO_TAMANIO: 'El archivo supera el máximo de 10 MB.',
  ARCHIVOS_TAMANIO_TOTAL: 'Los archivos superan el máximo total de 50 MB.',
} as const
