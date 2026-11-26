/**
 * Mocks de Administración (contrato de la API §5 y §9). Las cuentas son las de `SESIONES_USAL`: los permisos por
 * carrera viven en `carrerasConPermiso`, así la bandeja y el detalle responden a lo que cambia el Admin.
 */
import { ApiError } from '@/services/api/ApiError'
import type { Alertas, AsignacionResponsables, CuentaBloqueada, DatosCarrera, DatosUsuario, UsuarioAdmin } from '@/types/admin'
import type { Sesion } from '@/types/sesion'
import type { SolicitudResumen } from '@/types/solicitud'
import { nombreCompleto } from '@/utils/personas'
import { registrarAuditoria } from './auditoria'
import { CARRERAS, INACTIVAS, responsable, SESION_ASPIRANTE, SESIONES_USAL, SOLICITUDES, TEMPORALES } from './datos'

const ALTA_DEMO = '2026-10-01T10:00:00-03:00'
const PASSWORD_SEGURA = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/
let ultimoPermisoTemporal = 40

const bloqueadas: CuentaBloqueada[] = [
  {
    tipo: 'aspirante',
    id: 22,
    nombre: 'Tomás Díaz',
    email: 'tomas.diaz@example.com',
    bloqueadoHasta: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString(),
  },
]

function error(status: number, code: string, extra: { message?: string; errors?: Record<string, string[]> } = {}) {
  return ApiError.fromStatus(status, { status, code, ...extra })
}

const enCampo = (campo: string, mensaje: string) => error(400, 'VALIDACION', { errors: { [campo]: [mensaje] } })

/** Un permiso por usuario y carrera: el id sale de los dos. */
const idPermiso = (usuarioId: number, carreraId: number) => usuarioId * 100 + carreraId

function personal(id: number) {
  const cuenta = SESIONES_USAL.find((sesion) => sesion.id === id && sesion.rol !== 'ADMIN')
  if (!cuenta) throw error(404, 'NO_ENCONTRADO')
  return cuenta
}

function aUsuario(cuenta: Sesion): UsuarioAdmin {
  return {
    id: cuenta.id,
    email: cuenta.email,
    nombre: cuenta.nombre,
    apellido: cuenta.apellido,
    rol: cuenta.rol,
    activo: !INACTIVAS.has(cuenta.id),
    bloqueadoHasta: null,
    fechaUltimoLogin: cuenta.id <= 6 ? '2026-11-21T09:12:00-03:00' : null,
    permisosCarrera: (cuenta.carrerasConPermiso ?? []).map(({ id, nombre }) => ({
      id: idPermiso(cuenta.id, id),
      carrera: { id, nombre },
      assignedAt: ALTA_DEMO,
    })),
    permisosSolicitud: TEMPORALES.filter(({ userId }) => userId === cuenta.id).map((permiso) => ({
      id: permiso.id,
      solicitud: { id: permiso.solicitudId, codigoExp: SOLICITUDES.find(({ id }) => id === permiso.solicitudId)!.codigoExp },
      assignedAt: permiso.assignedAt,
      expiresAt: permiso.expiresAt,
    })),
  }
}

function listar(consulta: URLSearchParams) {
  const busqueda = (consulta.get('q') ?? '').toLowerCase()
  const filas = SESIONES_USAL.filter(({ rol }) => rol !== 'ADMIN')
    .filter(({ rol }) => !consulta.get('rol') || rol === consulta.get('rol'))
    .filter(({ id }) => !consulta.has('activo') || String(!INACTIVAS.has(id)) === consulta.get('activo'))
    .filter((cuenta) => `${nombreCompleto(cuenta)} ${cuenta.email}`.toLowerCase().includes(busqueda))
    .sort((a, b) => a.apellido.localeCompare(b.apellido, 'es'))
  const page = Number(consulta.get('page') ?? 0)
  const size = Number(consulta.get('size') ?? 20)
  return {
    content: filas.slice(page * size, (page + 1) * size).map(aUsuario),
    page,
    size,
    totalElements: filas.length,
    totalPages: Math.ceil(filas.length / size),
  }
}

function crear(admin: Sesion, body: unknown) {
  const datos = body as DatosUsuario
  const email = datos.email.trim().toLowerCase()
  if (!email.endsWith('@usal.edu.ar')) throw enCampo('email', 'El email tiene que ser @usal.edu.ar.')
  if (!PASSWORD_SEGURA.test(datos.password)) {
    throw enCampo('password', 'La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.')
  }
  if (SESIONES_USAL.some((sesion) => sesion.email === email)) {
    throw error(409, 'DUPLICADO', { message: 'Ya existe una cuenta con ese email.' })
  }
  const cuenta: Sesion = {
    tipo: 'USUARIO',
    id: Math.max(...SESIONES_USAL.map(({ id }) => id)) + 1,
    nombre: datos.nombre.trim(),
    apellido: datos.apellido.trim(),
    email,
    rol: datos.rolCodigo,
    avatar: null,
    emailValidado: true,
    esOAuth: true,
    ...(datos.rolCodigo === 'REVISOR' ? {} : { carrerasConPermiso: [] }),
    expiraEn: SESION_ASPIRANTE.expiraEn,
  }
  SESIONES_USAL.push(cuenta)
  registrarAuditoria(admin, 'crear_cuenta', 'usuario', cuenta.id, { detalles: { rol: cuenta.rol } })
  return aUsuario(cuenta)
}

/** Las solicitudes abiertas de las que la cuenta es responsable. */
function procesosActivos(cuenta: Sesion) {
  return SOLICITUDES.filter(
    (solicitud) =>
      !solicitud.estado.esTerminal &&
      (solicitud.responsableAcademico?.id === cuenta.id || solicitud.responsableAdministrativo?.id === cuenta.id),
  )
}

function exigirSinProcesos(activos: SolicitudResumen[]) {
  if (!activos.length) return
  const expedientes = activos.map(({ codigoExp }) => codigoExp).join(', ')
  throw error(409, 'PROCESO_ACTIVO', {
    message: `Proceso activo en ${expedientes}. Para revocar, asigne responsable a otro usuario primero.`,
  })
}

function editar(admin: Sesion, body: unknown, id: number) {
  const cambios = body as Partial<DatosUsuario> & { activo?: boolean }
  const cuenta = personal(id)
  const cambiaRol = cambios.rolCodigo !== undefined && cambios.rolCodigo !== cuenta.rol
  if (cambiaRol || (cambios.activo === false && !INACTIVAS.has(id))) exigirSinProcesos(procesosActivos(cuenta))
  if (cambios.nombre?.trim()) cuenta.nombre = cambios.nombre.trim()
  if (cambios.apellido?.trim()) cuenta.apellido = cambios.apellido.trim()
  if (cambiaRol) {
    cuenta.rol = cambios.rolCodigo!
    if (cuenta.rol === 'REVISOR') delete cuenta.carrerasConPermiso
    else cuenta.carrerasConPermiso ??= []
  }
  if (cambios.activo === false) INACTIVAS.add(id)
  if (cambios.activo === true) INACTIVAS.delete(id)
  registrarAuditoria(admin, 'editar_cuenta', 'usuario', id)
  return aUsuario(cuenta)
}

function asignarCarreras(admin: Sesion, body: unknown, id: number) {
  const cuenta = personal(id)
  const { carreraIds } = body as { carreraIds: number[] }
  if (cuenta.rol === 'REVISOR') throw enCampo('carreraIds', 'Solo Académico y Secretaría trabajan por carrera.')
  if (!carreraIds?.length) throw enCampo('carreraIds', 'Elegí al menos una carrera.')
  cuenta.carrerasConPermiso ??= []
  for (const carreraId of carreraIds) {
    const carrera = CARRERAS.find((item) => item.id === carreraId && item.activo)
    if (!carrera) throw enCampo('carreraIds', 'Elegí carreras activas del catálogo.')
    if (cuenta.carrerasConPermiso.some((item) => item.id === carreraId)) continue
    cuenta.carrerasConPermiso.push({ id: carrera.id, codigo: carrera.codigo, nombre: carrera.nombre })
    registrarAuditoria(admin, 'asignar_permiso', 'usuario', id, { detalles: { carrera: carrera.codigo } })
  }
  return aUsuario(cuenta)
}

/** El proceso queda sin responsable de ese rol, como en el backend cuando la revocación es forzada. */
function liberar(cuenta: Sesion, solicitud: SolicitudResumen) {
  if (solicitud.responsableAcademico?.id === cuenta.id) solicitud.responsableAcademico = null
  if (solicitud.responsableAdministrativo?.id === cuenta.id) solicitud.responsableAdministrativo = null
}

function revocarCarrera(admin: Sesion, body: unknown, permisoId: number) {
  const { forzar, comentario } = body as { forzar: boolean; comentario?: string }
  const cuenta = SESIONES_USAL.find((sesion) =>
    sesion.carrerasConPermiso?.some((carrera) => idPermiso(sesion.id, carrera.id) === permisoId),
  )
  if (!cuenta) throw error(404, 'NO_ENCONTRADO')
  const carrera = cuenta.carrerasConPermiso!.find((item) => idPermiso(cuenta.id, item.id) === permisoId)!
  const afectadas = procesosActivos(cuenta).filter(
    (solicitud) =>
      solicitud.carreraDestino.id === carrera.id &&
      !TEMPORALES.some((permiso) => permiso.userId === cuenta.id && permiso.solicitudId === solicitud.id),
  )
  if (!forzar) exigirSinProcesos(afectadas)
  cuenta.carrerasConPermiso = cuenta.carrerasConPermiso!.filter((item) => item.id !== carrera.id)
  afectadas.forEach((solicitud) => liberar(cuenta, solicitud))
  registrarAuditoria(admin, 'revocar_permiso', 'usuario', cuenta.id, {
    detalles: { carrera: carrera.codigo, forzado: afectadas.length > 0 },
    comentarios: comentario || null,
  })
}

function revocarSolicitud(admin: Sesion, body: unknown, permisoId: number) {
  const indice = TEMPORALES.findIndex(({ id }) => id === permisoId)
  if (indice < 0) throw error(404, 'NO_ENCONTRADO')
  const [permiso] = TEMPORALES.splice(indice, 1)
  const cuenta = personal(permiso.userId)
  const solicitud = SOLICITUDES.find(({ id }) => id === permiso.solicitudId)!
  if (!cuenta.carrerasConPermiso?.some(({ id }) => id === solicitud.carreraDestino.id)) liberar(cuenta, solicitud)
  registrarAuditoria(admin, 'revocar_permiso', 'usuario', cuenta.id, {
    detalles: { solicitud: solicitud.codigoExp },
    comentarios: (body as { comentario?: string }).comentario || null,
  })
}

/**
 * Asigna los responsables y devuelve qué cambió, para que el mock de solicitudes lo sume al historial. A quien no
 * tiene acceso a la carrera se le crea un permiso temporal, si el Admin lo pidió.
 */
export function asignarResponsables(admin: Sesion, solicitud: SolicitudResumen, body: unknown) {
  const datos = body as AsignacionResponsables
  if (!datos.comentario?.trim()) throw enCampo('comentario', 'Escribí el motivo de la asignación.')
  if (!datos.academicoId && !datos.administrativoId) throw enCampo('academicoId', 'Elegí al menos un responsable.')
  if (solicitud.estado.esTerminal) throw error(409, 'TRANSICION_INVALIDA')
  const ahora = new Date().toISOString()
  const asignados: { papel: string; nombre: string }[] = []
  const elegidos: [string, number | null, 'responsableAcademico' | 'responsableAdministrativo', string][] = [
    ['ACAD', datos.academicoId, 'responsableAcademico', 'responsable académico'],
    ['SECRE', datos.administrativoId, 'responsableAdministrativo', 'responsable administrativo'],
  ]
  for (const [rol, id, campo, papel] of elegidos) {
    if (!id || solicitud[campo]?.id === id) continue
    const cuenta = SESIONES_USAL.find((sesion) => sesion.id === id && sesion.rol === rol && !INACTIVAS.has(id))
    if (!cuenta) throw enCampo(rol === 'ACAD' ? 'academicoId' : 'administrativoId', 'Elegí una cuenta activa de ese rol.')
    const conAcceso =
      cuenta.carrerasConPermiso?.some((carrera) => carrera.id === solicitud.carreraDestino.id) ||
      TEMPORALES.some((permiso) => permiso.userId === id && permiso.solicitudId === solicitud.id)
    if (!conAcceso) {
      if (!datos.crearPermisoTemporal) {
        throw error(409, 'SIN_PERMISO_CARRERA', { message: 'El usuario no tiene permiso sobre la carrera.' })
      }
      TEMPORALES.push({
        id: ++ultimoPermisoTemporal,
        userId: id,
        solicitudId: solicitud.id,
        assignedAt: ahora,
        expiresAt: datos.expiresAt,
      })
    }
    solicitud[campo] = responsable(cuenta.email, ahora)
    asignados.push({ papel, nombre: nombreCompleto(cuenta) })
    registrarAuditoria(admin, 'asignar_responsable', 'solicitud', solicitud.id, {
      detalles: { rol, responsable: nombreCompleto(cuenta) },
      comentarios: datos.comentario.trim(),
    })
  }
  solicitud.fechaUltimaMod = ahora
  return asignados
}

function alertas(): Alertas {
  const abiertas = SOLICITUDES.filter(({ estado }) => !estado.esTerminal)
  const aAlerta = ({ id, codigoExp, aspirante, carreraDestino }: SolicitudResumen) => ({
    id,
    codigoExp,
    aspirante: aspirante.nombre,
    carrera: carreraDestino.nombre,
  })
  return {
    sinResponsableAcademico: abiertas.filter((solicitud) => !solicitud.responsableAcademico).map(aAlerta),
    sinResponsableAdministrativo: abiertas.filter((solicitud) => !solicitud.responsableAdministrativo).map(aAlerta),
    cuentasBloqueadas: bloqueadas.map((cuenta) => ({ ...cuenta })),
  }
}

function desbloquear(admin: Sesion, tipo: CuentaBloqueada['tipo'], id: number) {
  const indice = bloqueadas.findIndex((cuenta) => cuenta.tipo === tipo && cuenta.id === id)
  if (indice >= 0) bloqueadas.splice(indice, 1)
  registrarAuditoria(admin, 'desbloqueo_cuenta', tipo, id)
}

function guardarCarrera(admin: Sesion, body: unknown, id?: number) {
  const datos = body as DatosCarrera
  const codigo = datos.codigo.trim()
  if (CARRERAS.some((carrera) => carrera.codigo === codigo && carrera.id !== id)) {
    throw error(409, 'DUPLICADO', { message: 'Ya existe una carrera con ese código.' })
  }
  const campos = { codigo, nombre: datos.nombre.trim(), facultad: datos.facultad.trim(), descripcion: datos.descripcion.trim() }
  let carrera = CARRERAS.find((item) => item.id === id)
  if (carrera) Object.assign(carrera, campos)
  else {
    carrera = { id: Math.max(...CARRERAS.map((item) => item.id)) + 1, ...campos, activo: true }
    CARRERAS.push(carrera)
  }
  registrarAuditoria(admin, id ? 'editar_carrera' : 'crear_carrera', 'carrera', carrera.id, { detalles: { codigo } })
  return { ...carrera }
}

function activarCarrera(admin: Sesion, body: unknown, id: number) {
  const carrera = CARRERAS.find((item) => item.id === id)
  if (!carrera) throw error(404, 'NO_ENCONTRADO')
  carrera.activo = (body as { activo: boolean }).activo
  registrarAuditoria(admin, carrera.activo ? 'activar_carrera' : 'desactivar_carrera', 'carrera', id, {
    detalles: { codigo: carrera.codigo },
  })
  return { ...carrera }
}

type Manejador = (body: unknown, ids: number[], consulta: URLSearchParams) => unknown

export function rutasAdmin(sesionActual: () => Sesion): Record<string, Manejador> {
  /** Todo es solo del Admin: cualquier otro rol recibe 403, como en el backend. */
  const admin = () => {
    const sesion = sesionActual()
    if (sesion.rol !== 'ADMIN') throw error(403, 'ACCESO_DENEGADO')
    return sesion
  }
  return {
    'GET /admin/usuarios': (_, __, consulta) => (admin(), listar(consulta)),
    'POST /admin/usuarios': (body) => crear(admin(), body),
    'GET /admin/usuarios/:id': (_, [id]) => (admin(), aUsuario(personal(id))),
    'PATCH /admin/usuarios/:id': (body, [id]) => editar(admin(), body, id),
    'POST /admin/usuarios/:id/permisos/carreras': (body, [id]) => asignarCarreras(admin(), body, id),
    'POST /admin/permisos/carreras/:id/revocar': (body, [id]) => revocarCarrera(admin(), body, id),
    'POST /admin/permisos/solicitudes/:id/revocar': (body, [id]) => revocarSolicitud(admin(), body, id),
    'GET /admin/alertas': () => (admin(), alertas()),
    'POST /admin/cuentas/usuario/:id/desbloquear': (_, [id]) => desbloquear(admin(), 'usuario', id),
    'POST /admin/cuentas/aspirante/:id/desbloquear': (_, [id]) => desbloquear(admin(), 'aspirante', id),
    'POST /carreras': (body) => guardarCarrera(admin(), body),
    'PUT /carreras/:id': (body, [id]) => guardarCarrera(admin(), body, id),
    'PATCH /carreras/:id/activo': (body, [id]) => activarCarrera(admin(), body, id),
  }
}
