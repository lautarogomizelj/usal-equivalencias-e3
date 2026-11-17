/** Mensajes de error reutilizables (frontend-interfaces.md §0.4.4). */
export const MENSAJES = {
  REQUERIDO: 'Este campo es obligatorio.',
  EMAIL_INVALIDO: 'Ingresá un email válido.',
  PASSWORD_INVALIDA:
    'La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.',
  PASSWORD_NO_COINCIDE: 'Las contraseñas no coinciden.',
  DNI_INVALIDO: 'Ingresá un DNI válido (7 u 8 dígitos, sin puntos).',
  TELEFONO_INVALIDO: 'Ingresá un teléfono válido.',
  CODIGO_INVALIDO: 'El código debe tener 6 dígitos.',
  CREDENCIALES_INVALIDAS: 'El email o la contraseña no son correctos.',
  CREDENCIALES_ADMIN_INVALIDAS: 'El usuario o la contraseña no son correctos.',
  USUARIO_NO_REGISTRADO: 'Usuario no registrado en el sistema. Contacta al admin.',
  GOOGLE_ERROR: 'No se pudo ingresar con Google. Intentá nuevamente.',
  ARCHIVO_REQUERIDO: 'Tenés que adjuntar un archivo.',
  ARCHIVO_FORMATO: 'Formato no permitido. Solo se aceptan archivos PDF o JPG.',
  ARCHIVO_TAMANIO: 'El archivo supera el máximo de 10 MB.',
  ARCHIVOS_TAMANIO_TOTAL: 'Los archivos superan el máximo total de 50 MB.',
  MATERIA_REQUERIDA: 'Indicá la materia antes de adjuntar el programa.',
  ESTADO_CAMBIO: 'Esta acción ya no está disponible porque el estado de la solicitud cambió.',
} as const
