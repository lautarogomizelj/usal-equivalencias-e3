/** Mocks del detalle, los documentos y el historial de una solicitud (contrato de la API §6 y §7). */
import { ApiError } from '@/services/api/ApiError'
import type {
  DatosNuevaSolicitud,
  Documento,
  EstadoRevision,
  EventoHistorial,
  SolicitudDetalle,
  SolicitudResumen,
  TipoDocumento,
} from '@/types/solicitud'
import { CARRERAS, ESTADOS, PERFIL, SESION_ASPIRANTE, SOLICITUDES } from './datos'

const DIA_MS = 24 * 60 * 60 * 1000
const TIPOS_OBLIGATORIOS: TipoDocumento[] = ['ANALITICO', 'PLAN', 'PROGRAMA']
const ARCHIVO_DEMO = URL.createObjectURL(new Blob(['Documento de ejemplo de los mocks.'], { type: 'text/plain' }))

const ORIGEN_DEMO: SolicitudDetalle['datosOrigen'] = {
  universidad: 'Universidad Nacional de La Plata',
  facultad: 'Facultad de Informática',
  carrera: 'Licenciatura en Sistemas',
  condicionAcademica: 'Alumno regular',
  observaciones: '',
}

/** Estados por los que pasó cada solicitud de ejemplo, según su estado actual. */
const RECORRIDOS: Record<string, string[]> = {
  'DOC-REV': ['SOL-INI', 'DOC-REV'],
  'DOC-RECH': ['SOL-INI', 'DOC-REV', 'DOC-RECH'],
  'INSC-ACEP': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND', 'ANAL-POST', 'INSC-ACEP'],
}

/** Acciones del aspirante en cada estado (contrato de la API §7.1). */
const ACCIONES: Record<string, string[]> = {
  'SOL-INI': ['ENVIAR_DOCUMENTACION', 'CANCELAR_SOLICITUD'],
  'DOC-RECH': ['ENVIAR_DOCUMENTACION'],
}

const origenes = new Map<number, SolicitudDetalle['datosOrigen']>()
const documentos = new Map<number, Documento[]>()
const historiales = new Map<number, EventoHistorial[]>()
let ultimoDocumentoId = 300

function estado(codigo: string) {
  return ESTADOS.find((item) => item.codigo === codigo)!
}

function buscar(id: number) {
  const solicitud = SOLICITUDES.find((item) => item.id === id)
  if (!solicitud) throw ApiError.fromStatus(404, { status: 404, code: 'NO_ENCONTRADO' })
  return solicitud
}

function documento(
  tipo: TipoDocumento,
  descripcion: string | null,
  nombreArchivo: string,
  fechaCarga: string,
  estadoRevision: EstadoRevision = 'PENDIENTE',
  comentarioRevision: string | null = null,
): Documento {
  ultimoDocumentoId += 1
  return {
    id: ultimoDocumentoId,
    tipo,
    descripcion,
    nombreArchivo,
    tamanoBytes: 1024 * 1024,
    fechaCarga,
    version: 1,
    reemplazaAId: null,
    borrador: false,
    estadoRevision,
    etapaRevision: 'DOCUMENTACION',
    comentarioRevision,
    revisadoPor: null,
    fechaRevision: null,
    esRequeridoFaltante: false,
    urlDescarga: ARCHIVO_DEMO,
  }
}

function documentosDe(id: number) {
  if (!documentos.has(id)) {
    const { estado: actual, fechaCreacion } = buscar(id)
    const revision = actual.codigo === 'DOC-REV' ? 'PENDIENTE' : 'OK'
    const rechazada = actual.codigo === 'DOC-RECH'
    documentos.set(
      id,
      actual.codigo === 'SOL-INI'
        ? []
        : [
            documento('ANALITICO', null, 'analitico.pdf', fechaCreacion, revision),
            documento('PLAN', null, 'plan-de-estudios.pdf', fechaCreacion, revision),
            documento(
              'PROGRAMA',
              'Matemática I',
              'programa-matematica.pdf',
              fechaCreacion,
              rechazada ? 'RECARGAR' : revision,
              rechazada ? 'El programa no está firmado.' : null,
            ),
          ],
    )
  }
  return documentos.get(id)!
}

function cambioEstado(fecha: string, anterior: string | null, nuevo: string, usuario: string): EventoHistorial {
  return { fecha, tipo: 'CAMBIO_ESTADO', estadoAnterior: anterior, estadoNuevo: nuevo, usuario, comentario: null }
}

function documentoCargado({ fechaCarga, nombreArchivo }: Documento): EventoHistorial {
  return {
    fecha: fechaCarga,
    tipo: 'DOCUMENTO_CARGADO',
    estadoAnterior: null,
    estadoNuevo: null,
    usuario: PERFIL.nombre,
    comentario: nombreArchivo,
  }
}

function historialDe(id: number) {
  if (!historiales.has(id)) {
    const { estado: actual, fechaCreacion, responsableAcademico } = buscar(id)
    const recorrido = RECORRIDOS[actual.codigo] ?? ['SOL-INI']
    const cambios = recorrido.map((codigo, indice) =>
      cambioEstado(
        new Date(Date.parse(fechaCreacion) + indice * DIA_MS).toISOString(),
        recorrido[indice - 1] ?? null,
        codigo,
        indice < 2 || !responsableAcademico ? PERFIL.nombre : responsableAcademico.nombre,
      ),
    )
    historiales.set(id, [...cambios.reverse(), ...documentosDe(id).map(documentoCargado)])
  }
  return historiales.get(id)!
}

function detalle(id: number): SolicitudDetalle {
  const solicitud = buscar(id)
  const enviados = documentosDe(id).filter(({ borrador }) => !borrador)
  const contar = (revision: EstadoRevision) => enviados.filter((item) => item.estadoRevision === revision).length

  return {
    id,
    codigoExp: solicitud.codigoExp,
    estado: solicitud.estado,
    aspirante: { ...solicitud.aspirante, email: PERFIL.email, telefono: PERFIL.telefono, dni: PERFIL.dni },
    datosOrigen: origenes.get(id) ?? ORIGEN_DEMO,
    carreraDestino: solicitud.carreraDestino,
    responsableAcademico: solicitud.responsableAcademico,
    responsableAdministrativo: solicitud.responsableAdministrativo,
    fechaCreacion: solicitud.fechaCreacion,
    fechaUltimaMod: solicitud.fechaUltimaMod,
    fechaUltimoCambioEstado: solicitud.fechaUltimaMod,
    miRelacion: 'ASPIRANTE',
    accionesDisponibles: ACCIONES[solicitud.estado.codigo] ?? [],
    ultimaObservacion: null,
    resumenDocumentos: { ok: contar('OK'), recargar: contar('RECARGAR'), pendiente: contar('PENDIENTE') },
    chatHabilitado: Boolean(solicitud.responsableAcademico ?? solicitud.responsableAdministrativo),
    mensajesNoLeidos: 0,
  }
}

function crear(body: unknown) {
  const datos = body as DatosNuevaSolicitud
  const carrera = CARRERAS.find(({ codigo }) => codigo === datos.carreraDestinoCodigo)
  if (!carrera) {
    throw ApiError.fromStatus(400, {
      status: 400,
      code: 'VALIDACION',
      errors: { carreraDestinoCodigo: ['La carrera no existe o no está activa.'] },
    })
  }
  const ahora = new Date().toISOString()
  const id = SOLICITUDES.length + 1
  const nueva: SolicitudResumen = {
    id,
    codigoExp: `EXP-2026-${String(id).padStart(3, '0')}`,
    aspirante: { id: SESION_ASPIRANTE.id, nombre: PERFIL.nombre },
    universidadOrigen: datos.universidadOrigen,
    carreraDestino: carrera,
    estado: estado('SOL-INI'),
    responsableAcademico: null,
    responsableAdministrativo: null,
    fechaCreacion: ahora,
    fechaUltimaMod: ahora,
    requiereMiAccion: true,
    esDeMisCarreras: false,
  }
  SOLICITUDES.push(nueva)
  origenes.set(id, {
    universidad: datos.universidadOrigen,
    facultad: datos.facultadOrigen,
    carrera: datos.carreraOrigen,
    condicionAcademica: datos.condicionAcademica,
    observaciones: datos.observaciones,
  })
  historiales.set(id, [cambioEstado(ahora, null, 'SOL-INI', PERFIL.nombre)])
  return detalle(id)
}

function subirDocumento(body: unknown, [id]: number[]) {
  const formulario = body as FormData
  const archivo = formulario.get('archivo') as File
  const nuevo: Documento = {
    ...documento(
      formulario.get('tipo') as TipoDocumento,
      formulario.get('descripcion') as string | null,
      archivo.name,
      new Date().toISOString(),
    ),
    tamanoBytes: archivo.size,
    borrador: true,
    urlDescarga: URL.createObjectURL(archivo),
  }
  documentosDe(id).push(nuevo)
  return nuevo
}

function quitarDocumento(_: unknown, [id, documentoId]: number[]) {
  documentos.set(
    id,
    documentosDe(id).filter((item) => item.id !== documentoId),
  )
}

function enviarDocumentacion(body: unknown, [id]: number[]) {
  const solicitud = buscar(id)
  const { estadoEsperado } = body as { estadoEsperado: string }
  if (solicitud.estado.codigo !== 'SOL-INI' || estadoEsperado !== 'SOL-INI') {
    throw ApiError.fromStatus(409, { status: 409, code: 'TRANSICION_INVALIDA' })
  }
  const cargados = documentosDe(id)
  if (TIPOS_OBLIGATORIOS.some((tipo) => !cargados.some((item) => item.tipo === tipo))) {
    throw ApiError.fromStatus(400, {
      status: 400,
      code: 'VALIDACION',
      errors: { documentos: ['Faltan documentos obligatorios.'] },
    })
  }
  const ahora = new Date().toISOString()
  cargados.forEach((item) => {
    item.borrador = false
  })
  solicitud.estado = estado('DOC-REV')
  solicitud.fechaUltimaMod = ahora
  solicitud.requiereMiAccion = false
  historialDe(id).unshift(
    cambioEstado(ahora, 'SOL-INI', 'DOC-REV', PERFIL.nombre),
    ...cargados.map(documentoCargado),
  )
  return detalle(id)
}

function cancelar(body: unknown, [id]: number[]) {
  const solicitud = buscar(id)
  const { estadoEsperado } = body as { estadoEsperado: string }
  if (solicitud.estado.codigo !== 'SOL-INI' || estadoEsperado !== 'SOL-INI') {
    throw ApiError.fromStatus(409, { status: 409, code: 'TRANSICION_INVALIDA' })
  }
  const ahora = new Date().toISOString()
  documentos.set(id, [])
  solicitud.estado = estado('SOL-CANC')
  solicitud.fechaUltimaMod = ahora
  solicitud.requiereMiAccion = false
  historialDe(id).unshift(cambioEstado(ahora, 'SOL-INI', 'SOL-CANC', PERFIL.nombre))
  return detalle(id)
}

export const RUTAS_SOLICITUDES: Record<string, (body: unknown, ids: number[]) => unknown> = {
  'POST /solicitudes': crear,
  'GET /solicitudes/:id': (_, [id]) => detalle(id),
  'GET /solicitudes/:id/historial': (_, [id]) => [...historialDe(id)],
  'GET /solicitudes/:id/documentos': (_, [id]) => [...documentosDe(id)],
  'POST /solicitudes/:id/documentos': subirDocumento,
  'DELETE /solicitudes/:id/documentos/:id': quitarDocumento,
  'POST /solicitudes/:id/documentacion/enviar': enviarDocumentacion,
  'POST /solicitudes/:id/cancelar': cancelar,
}
