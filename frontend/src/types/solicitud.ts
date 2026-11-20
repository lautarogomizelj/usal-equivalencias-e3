import type { Carrera } from './carrera'
import type { Estado } from './estado'

export interface Responsable {
  id: number
  nombre: string
  email: string
  fechaAsignacion: string
}

/** Fila del listado de solicitudes (contrato de la API §6.2). */
export interface SolicitudResumen {
  id: number
  codigoExp: string
  aspirante: { id: number; nombre: string }
  universidadOrigen: string
  carreraDestino: Pick<Carrera, 'id' | 'codigo' | 'nombre'>
  estado: Pick<Estado, 'codigo' | 'nombre' | 'esTerminal'>
  responsableAcademico: Responsable | null
  responsableAdministrativo: Responsable | null
  fechaCreacion: string
  fechaUltimaMod: string
  requiereMiAccion: boolean
  esDeMisCarreras: boolean
}

/** Body de `POST /solicitudes` (contrato de la API §6.3). */
export interface DatosNuevaSolicitud {
  universidadOrigen: string
  facultadOrigen: string
  carreraOrigen: string
  carreraDestinoCodigo: string
  condicionAcademica: string
  observaciones: string
}

/** Detalle de una solicitud (contrato de la API §6.4). */
export interface SolicitudDetalle {
  id: number
  codigoExp: string
  estado: Pick<Estado, 'codigo' | 'nombre' | 'esTerminal'>
  aspirante: { id: number; nombre: string; email: string; telefono: string; dni: string }
  datosOrigen: {
    universidad: string
    facultad: string
    carrera: string
    condicionAcademica: string
    observaciones: string
  }
  carreraDestino: Pick<Carrera, 'id' | 'codigo' | 'nombre'>
  responsableAcademico: Responsable | null
  responsableAdministrativo: Responsable | null
  fechaCreacion: string
  fechaUltimaMod: string
  fechaUltimoCambioEstado: string
  miRelacion: 'ASPIRANTE' | 'PARTICIPANTE' | 'OBSERVADOR' | 'ADMIN' | 'REVISOR'
  accionesDisponibles: string[]
  ultimaObservacion: { comentario: string; usuario: string; fecha: string } | null
  resumenDocumentos: { ok: number; recargar: number; pendiente: number }
  chatHabilitado: boolean
  mensajesNoLeidos: number
}

/** Evento del historial de una solicitud (contrato de la API §6.4). */
export interface EventoHistorial {
  fecha: string
  tipo: 'CAMBIO_ESTADO' | 'CAMBIO_RESPONSABLE' | 'DOCUMENTO_CARGADO'
  estadoAnterior: string | null
  estadoNuevo: string | null
  usuario: string
  comentario: string | null
}

export type TipoDocumento = 'ANALITICO' | 'PLAN' | 'PROGRAMA' | 'OTRO'
export type EstadoRevision = 'PENDIENTE' | 'OK' | 'RECARGAR'

/**
 * Documento de una solicitud (contrato de la API §6.5). Un requerido faltante (`esRequeridoFaltante`)
 * todavía no tiene archivo: llega sin nombre, tamaño, fecha ni dirección de descarga.
 */
export interface Documento {
  id: number
  tipo: TipoDocumento
  descripcion: string | null
  nombreArchivo: string | null
  tamanoBytes: number | null
  fechaCarga: string | null
  version: number
  reemplazaAId: number | null
  borrador: boolean
  estadoRevision: EstadoRevision
  etapaRevision: string
  comentarioRevision: string | null
  revisadoPor: string | null
  fechaRevision: string | null
  esRequeridoFaltante: boolean
  urlDescarga: string | null
}

/** Marca de revisión que el Académico pone sobre un documento (contrato de la API §7.3). */
export interface Marca {
  documentoId: number
  estadoRevision: EstadoRevision
  comentario: string | null
}

/** Filtros de la bandeja del personal (contrato de la API §6.2). Los vacíos no se envían. */
export interface FiltrosBandeja {
  q?: string
  estado?: string[]
  desde?: string
  hasta?: string
  asignadas?: boolean
  sinResponsable?: boolean
  finalizadas?: boolean
  size?: number
}
