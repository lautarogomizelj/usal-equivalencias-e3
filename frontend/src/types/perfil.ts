/** Perfil del usuario (contrato de la API §4.2). */
export interface Perfil {
  nombre: string
  apellido: string
  email: string
  /** El personal @usal puede no tener teléfono y nunca tiene DNI. */
  telefono: string | null
  dni: string | null
  avatar: string | null
  rol: string
  esOAuth: boolean
  camposEditables: string[]
}

/** Un login anterior (`GET /perfil/accesos`). */
export interface Acceso {
  fecha: string
  ip: string
  duracionMinutos: number
}

/** Una sesión abierta (`GET /perfil/sesiones`). `actual` marca la del navegador en uso. */
export interface SesionActiva {
  id: string
  ip: string
  userAgent: string | null
  inicio: string
  ultimoUso: string
  actual: boolean
}
