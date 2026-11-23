import type { Carrera } from './carrera'

/** Permiso permanente por carrera (contrato de la API §9). */
export interface PermisoCarrera {
  id: number
  carrera: Pick<Carrera, 'id' | 'nombre'>
  assignedAt: string
}

/** Permiso temporal sobre una solicitud, que crea el Admin al asignar un responsable. */
export interface PermisoSolicitud {
  id: number
  solicitud: { id: number; codigoExp: string }
  assignedAt: string
  expiresAt: string | null
}

/** Cuenta del personal @usal con sus permisos activos. */
export interface UsuarioAdmin {
  id: number
  email: string
  nombre: string
  apellido: string
  rol: string
  activo: boolean
  bloqueadoHasta: string | null
  fechaUltimoLogin: string | null
  permisosCarrera: PermisoCarrera[]
  permisosSolicitud: PermisoSolicitud[]
}

export interface FiltrosUsuarios {
  q?: string
  rol?: string
  activo?: string
  size?: number
}

export interface DatosUsuario {
  email: string
  nombre: string
  apellido: string
  rolCodigo: string
  password: string
}

export type DatosCarrera = Pick<Carrera, 'codigo' | 'nombre' | 'facultad' | 'descripcion'>

export interface AsignacionResponsables {
  academicoId: number | null
  administrativoId: number | null
  crearPermisoTemporal: boolean
  expiresAt: string | null
  comentario: string
}

export interface SolicitudAlerta {
  id: number
  codigoExp: string
  aspirante: string
  carrera: string
}

export interface CuentaBloqueada {
  tipo: 'usuario' | 'aspirante'
  id: number
  nombre: string
  email: string
  bloqueadoHasta: string
}

/** Lo que el Admin tiene que atender (RF-ADMIN-007). */
export interface Alertas {
  sinResponsableAcademico: SolicitudAlerta[]
  sinResponsableAdministrativo: SolicitudAlerta[]
  cuentasBloqueadas: CuentaBloqueada[]
}
