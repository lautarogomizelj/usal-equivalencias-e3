/**
 * Mocks de la auditoría (contrato de la API §8.3). Arranca con la creación y el último cambio de estado de cada
 * solicitud de ejemplo; después suma lo que se va haciendo en la sesión.
 */
import { ApiError } from '@/services/api/ApiError'
import type { EventoAuditoria } from '@/types/auditoria'
import type { Sesion } from '@/types/sesion'
import { SESION_ASPIRANTE, SOLICITUDES } from './datos'

const IP_DEMO = '10.0.0.15'
let ultimoId = 9000

const eventos: EventoAuditoria[] = SOLICITUDES.flatMap((solicitud) => {
  const base = {
    usuario: { id: null, email: SESION_ASPIRANTE.email },
    accion: 'cambio_estado',
    entidad: 'solicitud',
    entidadId: solicitud.id,
    detalles: null,
    comentarios: null,
    ipOrigen: IP_DEMO,
  }
  const creacion = { ...base, id: ++ultimoId, timestamp: solicitud.fechaCreacion, estadoCodigoAnterior: null, estadoCodigoNuevo: 'SOL-INI' }
  if (solicitud.estado.codigo === 'SOL-INI') return [creacion]
  const responsable = solicitud.responsableAcademico
  return [
    creacion,
    {
      ...base,
      id: ++ultimoId,
      timestamp: solicitud.fechaUltimaMod,
      usuario: responsable ? { id: responsable.id, email: responsable.email } : base.usuario,
      estadoCodigoAnterior: 'SOL-INI',
      estadoCodigoNuevo: solicitud.estado.codigo,
    },
  ]
}).sort((a, b) => b.timestamp.localeCompare(a.timestamp))

/** Lo nuevo va primero, como en el backend. */
export function registrarAuditoria(
  sesion: Sesion,
  accion: string,
  entidad: string,
  entidadId: number | null,
  extra: Partial<Pick<EventoAuditoria, 'estadoCodigoAnterior' | 'estadoCodigoNuevo' | 'detalles' | 'comentarios'>> = {},
) {
  eventos.unshift({
    id: ++ultimoId,
    timestamp: new Date().toISOString(),
    usuario: { id: sesion.tipo === 'USUARIO' ? sesion.id : null, email: sesion.email },
    accion,
    entidad,
    entidadId,
    estadoCodigoAnterior: null,
    estadoCodigoNuevo: null,
    detalles: null,
    comentarios: null,
    ipOrigen: IP_DEMO,
    ...extra,
  })
}

function exigirLector(sesion: Sesion) {
  if (sesion.rol !== 'ADMIN' && sesion.rol !== 'REVISOR') {
    throw ApiError.fromStatus(403, { status: 403, code: 'ACCESO_DENEGADO' })
  }
}

function pagina(filas: EventoAuditoria[], consulta: URLSearchParams) {
  const page = Number(consulta.get('page') ?? 0)
  const size = Number(consulta.get('size') ?? 20)
  return {
    content: filas.slice(page * size, (page + 1) * size),
    page,
    size,
    totalElements: filas.length,
    totalPages: Math.ceil(filas.length / size),
  }
}

function listar(consulta: URLSearchParams) {
  const usuario = (consulta.get('usuario') ?? '').toLowerCase()
  const dia = (iso: string) => iso.slice(0, 10)
  return pagina(
    eventos
      .filter((evento) => evento.usuario.email.includes(usuario))
      .filter((evento) => !consulta.get('accion') || evento.accion === consulta.get('accion'))
      .filter((evento) => !consulta.get('entidad') || evento.entidad === consulta.get('entidad'))
      .filter((evento) => !consulta.get('desde') || dia(evento.timestamp) >= consulta.get('desde')!)
      .filter((evento) => !consulta.get('hasta') || dia(evento.timestamp) <= consulta.get('hasta')!),
    consulta,
  )
}

type Manejador = (body: unknown, ids: number[], consulta: URLSearchParams) => unknown

export function rutasAuditoria(sesionActual: () => Sesion): Record<string, Manejador> {
  const paraLector = (responder: Manejador): Manejador => (body, ids, consulta) => {
    exigirLector(sesionActual())
    return responder(body, ids, consulta)
  }
  return {
    'GET /auditoria': paraLector((_, __, consulta) => listar(consulta)),
    'GET /auditoria/opciones': paraLector(() => ({
      acciones: [...new Set(eventos.map(({ accion }) => accion))].sort(),
      entidades: [...new Set(eventos.map(({ entidad }) => entidad))].sort(),
    })),
    'GET /solicitudes/:id/auditoria': paraLector((_, [id], consulta) =>
      pagina(
        eventos.filter((evento) => evento.entidad === 'solicitud' && evento.entidadId === id),
        consulta,
      ),
    ),
  }
}
