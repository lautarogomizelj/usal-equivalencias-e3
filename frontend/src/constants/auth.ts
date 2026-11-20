/** Reglas de autenticación (RF-AUTH-001 / RF-AUTH-002). */
export const AUTH = {
  CODIGO_LONGITUD: 6,
  CODIGO_EXPIRACION_MINUTOS: 10,
  PASSWORD_MIN_LONGITUD: 8,
  DOMINIO_INSTITUCIONAL: 'usal.edu.ar',
} as const
