/** Reglas de autenticación (RF-AUTH-001 / RF-AUTH-002). */
export const AUTH = {
  CODIGO_LONGITUD: 6,
  CODIGO_EXPIRACION_MINUTOS: 10,
  PASSWORD_MIN_LONGITUD: 8,
  DOMINIO_INSTITUCIONAL: 'usal.edu.ar',
} as const

/** Cuentas del personal de `Docs/demo-credentials.md`, para entrar en desarrollo sin Google. */
export const CUENTAS_DEMO = [
  { rol: 'Académico', email: 'academico@usal.edu.ar' },
  { rol: 'Secretaría', email: 'secretaria@usal.edu.ar' },
  { rol: 'Revisor', email: 'revisor@usal.edu.ar' },
  { rol: 'Observador', email: 'observador@usal.edu.ar' },
] as const
