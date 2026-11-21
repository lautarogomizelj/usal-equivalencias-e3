/**
 * Mocks de solicitudes, documentos, historial y acciones del trámite (contrato de la API §6 y §7).
 * Responden según quién tiene la sesión abierta, igual que el backend. Todo vive en memoria.
 */
import { ESTADOS_DE_ACCION } from '@/constants/estados'
import { finDeReunion, REUNION } from '@/constants/reunion'
import { ApiError } from '@/services/api/ApiError'
import type { Sesion } from '@/types/sesion'
import type {
  Analisis,
  DatosNuevaSolicitud,
  Documento,
  EstadoRevision,
  EventoHistorial,
  Marca,
  ReunionInfo,
  SolicitudDetalle,
  SolicitudResumen,
  TipoAnalisis,
  TipoDocumento,
} from '@/types/solicitud'
import { hoyEnArgentina, sumarDias } from '@/utils/fechas'
import { nombreCompleto } from '@/utils/personas'
import { CARRERAS, ESTADOS, PERFIL, responsable, SESION_ASPIRANTE, SESIONES_USAL, SOLICITUDES } from './datos'

const DIA_MS = 24 * 60 * 60 * 1000
const TIPOS_OBLIGATORIOS: TipoDocumento[] = ['ANALITICO', 'PLAN', 'PROGRAMA']
const ARCHIVO_DEMO = URL.createObjectURL(new Blob(['Documento de ejemplo de los mocks.'], { type: 'text/plain' }))
const ROLES_CON_PROCESO = ['ACAD', 'SECRE']

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
  'ANAL-PEND': ['SOL-INI', 'DOC-REV', 'ANAL-PEND'],
  'REU-FEC-ASP-1': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1'],
  'REU-PEND': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND'],
  'INSC-ACEP': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND', 'ANAL-POST', 'INSC-ACEP'],
}

/** Estados anteriores a que el Académico cargue el análisis y proponga fechas. */
const SIN_ANALISIS = ['SOL-INI', 'DOC-REV', 'DOC-RECH', 'ANAL-PEND', 'SOL-CANC']

/** Acciones del aspirante en cada estado (contrato de la API §7.1). */
const ACCIONES_ASPIRANTE: Record<string, string[]> = {
  'SOL-INI': ['ENVIAR_DOCUMENTACION', 'CANCELAR_SOLICITUD'],
  'DOC-RECH': ['ENVIAR_DOCUMENTACION'],
  'REU-FEC-ASP-1': ['ELEGIR_HORARIO', 'SOLICITAR_OTRAS_FECHAS'],
  'REU-FEC-ASP-2': ['ELEGIR_HORARIO', 'RECHAZAR_SEGUNDA_RONDA'],
}

/** Acciones del Académico responsable en cada estado (contrato de la API §7.1 y §7.2). */
const ACCIONES_ACADEMICO: Record<string, string[]> = {
  'DOC-REV': ['RECHAZAR_DOCUMENTACION', 'ACEPTAR_DOCUMENTACION', 'GUARDAR_MARCAS', 'AGREGAR_FALTANTE'],
  'ANAL-PEND': ['PROPONER_FECHAS'],
  'REU-FEC-ACAD': ['PROPONER_SEGUNDA_RONDA'],
  'REU-PEND': ['CARGAR_ENLACE_MEET'],
}

/** Estados de tipo A de cada rol: con ellos se calcula `requiereMiAccion`. */
const ESPERAN_A: Record<string, string[]> = {
  ...ESTADOS_DE_ACCION,
  ASPIRANTE: [
    'SOL-INI',
    'DOC-RECH',
    'REU-FEC-ASP-1',
    'REU-FEC-ASP-2',
    'REU-CHAT',
    'INSC-ACEP',
    'LEG-PEND',
    'LEG-RECH',
    'CUPON-PAG',
    'PAG-REENV',
  ],
}

const MOTIVO_DEMO = 'El programa de Matemática I no está firmado.'

const origenes = new Map<number, SolicitudDetalle['datosOrigen']>()
const documentos = new Map<number, Documento[]>()
const historiales = new Map<number, EventoHistorial[]>()
const observaciones = new Map<number, NonNullable<SolicitudDetalle['ultimaObservacion']>>()
const analisis = new Map<number, Analisis[]>()
const reuniones = new Map<number, ReunionInfo>()
let ultimoDocumentoId = 300
let ultimoIdReunion = 500

function error(status: number, code: string, errors?: Record<string, string[]>) {
  return ApiError.fromStatus(status, { status, code, errors })
}

function estado(codigo: string) {
  return ESTADOS.find((item) => item.codigo === codigo)!
}

function buscar(id: number) {
  const solicitud = SOLICITUDES.find((item) => item.id === id)
  if (!solicitud) throw error(404, 'NO_ENCONTRADO')
  return solicitud
}

/** El aspirante solo ve las suyas; el personal, todas (acceso básico). */
function visible(sesion: Sesion, id: number) {
  const solicitud = buscar(id)
  if (sesion.tipo === 'ASPIRANTE' && solicitud.aspirante.id !== sesion.id) throw error(404, 'NO_ENCONTRADO')
  return solicitud
}

function esParticipante(sesion: Sesion, solicitud: SolicitudResumen) {
  return (
    ROLES_CON_PROCESO.includes(sesion.rol) &&
    Boolean(sesion.carrerasConPermiso?.some(({ id }) => id === solicitud.carreraDestino.id))
  )
}

function responsableDe(solicitud: SolicitudResumen, rol: string) {
  return rol === 'ACAD' ? solicitud.responsableAcademico : rol === 'SECRE' ? solicitud.responsableAdministrativo : null
}

function esResponsable(sesion: Sesion, solicitud: SolicitudResumen) {
  return sesion.tipo === 'USUARIO' && responsableDe(solicitud, sesion.rol)?.id === sesion.id
}

/** Los demás de su rol con permiso sobre la carrera: sin ellos no hay baja posible (RF-PROC-004). */
function hayOtrosConAcceso(sesion: Sesion, solicitud: SolicitudResumen) {
  return SESIONES_USAL.some((otra) => otra.id !== sesion.id && otra.rol === sesion.rol && esParticipante(otra, solicitud))
}

function accionesDe(sesion: Sesion, solicitud: SolicitudResumen) {
  const codigo = solicitud.estado.codigo
  if (sesion.tipo === 'ASPIRANTE') return ACCIONES_ASPIRANTE[codigo] ?? []
  if (!esParticipante(sesion, solicitud)) return []
  if (!esResponsable(sesion, solicitud)) {
    return responsableDe(solicitud, sesion.rol) || solicitud.estado.esTerminal ? [] : ['TOMAR_PROCESO']
  }
  return [
    ...(sesion.rol === 'ACAD' ? (ACCIONES_ACADEMICO[codigo] ?? []) : []),
    ...(hayOtrosConAcceso(sesion, solicitud) ? ['DARSE_DE_BAJA'] : []),
  ]
}

function exigir(sesion: Sesion, solicitud: SolicitudResumen, accion: string, estadoEsperado?: string) {
  if (!accionesDe(sesion, solicitud).includes(accion)) {
    if (sesion.tipo !== 'USUARIO' || esResponsable(sesion, solicitud)) throw error(409, 'TRANSICION_INVALIDA')
    throw error(403, esParticipante(sesion, solicitud) ? 'NO_RESPONSABLE' : 'NO_PARTICIPANTE')
  }
  if (estadoEsperado !== undefined && estadoEsperado !== solicitud.estado.codigo) {
    throw error(409, 'TRANSICION_INVALIDA')
  }
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

/** Un borrador no archiva al documento que reemplaza hasta que se envía. */
function vigentes(id: number) {
  const todos = documentosDe(id)
  const archivados = todos.filter(({ borrador }) => !borrador).map(({ reemplazaAId }) => reemplazaAId)
  return todos.filter((item) => !item.borrador && !archivados.includes(item.id))
}

/** Los borradores solo los ve el aspirante. */
function visibles(sesion: Sesion, id: number) {
  return [...vigentes(id), ...(sesion.tipo === 'ASPIRANTE' ? documentosDe(id).filter(({ borrador }) => borrador) : [])]
}

function cambioEstado(
  fecha: string,
  anterior: string | null,
  nuevo: string,
  usuario: string,
  comentario: string | null = null,
): EventoHistorial {
  return { fecha, tipo: 'CAMBIO_ESTADO', estadoAnterior: anterior, estadoNuevo: nuevo, usuario, comentario }
}

function documentoCargado(cargado: Documento, usuario: string): EventoHistorial {
  return {
    fecha: cargado.fechaCarga!,
    tipo: 'DOCUMENTO_CARGADO',
    estadoAnterior: null,
    estadoNuevo: null,
    usuario,
    comentario: cargado.nombreArchivo,
  }
}

function historialDe(id: number) {
  if (!historiales.has(id)) {
    const { estado: actual, fechaCreacion, responsableAcademico, aspirante } = buscar(id)
    const recorrido = RECORRIDOS[actual.codigo] ?? ['SOL-INI']
    const cambios = recorrido.map((codigo, indice) =>
      cambioEstado(
        new Date(Date.parse(fechaCreacion) + indice * DIA_MS).toISOString(),
        recorrido[indice - 1] ?? null,
        codigo,
        indice < 2 || !responsableAcademico ? aspirante.nombre : responsableAcademico.nombre,
        codigo === 'DOC-RECH' ? MOTIVO_DEMO : null,
      ),
    )
    historiales.set(id, [
      ...cambios.reverse(),
      ...documentosDe(id).map((cargado) => documentoCargado(cargado, aspirante.nombre)),
    ])
  }
  return historiales.get(id)!
}

function observacionDe(solicitud: SolicitudResumen) {
  if (observaciones.has(solicitud.id)) return observaciones.get(solicitud.id)!
  return solicitud.estado.codigo === 'DOC-RECH' && solicitud.responsableAcademico
    ? { comentario: MOTIVO_DEMO, usuario: solicitud.responsableAcademico.nombre, fecha: solicitud.fechaUltimaMod }
    : null
}

function detalle(sesion: Sesion, id: number): SolicitudDetalle {
  const solicitud = visible(sesion, id)
  const propios = visibles(sesion, id)
  const contar = (revision: EstadoRevision) => propios.filter((item) => item.estadoRevision === revision).length
  const esDemo = solicitud.aspirante.id === SESION_ASPIRANTE.id
  const usuario = solicitud.aspirante.nombre
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/\s+/g, '.')

  return {
    id,
    codigoExp: solicitud.codigoExp,
    estado: solicitud.estado,
    aspirante: {
      ...solicitud.aspirante,
      email: esDemo ? PERFIL.email : `${usuario}@example.com`,
      telefono: esDemo ? (PERFIL.telefono ?? '') : '+54 11 4000-0000',
      dni: esDemo ? (PERFIL.dni ?? '') : String(38000000 + solicitud.aspirante.id),
    },
    datosOrigen: origenes.get(id) ?? { ...ORIGEN_DEMO, universidad: solicitud.universidadOrigen },
    carreraDestino: solicitud.carreraDestino,
    responsableAcademico: solicitud.responsableAcademico,
    responsableAdministrativo: solicitud.responsableAdministrativo,
    fechaCreacion: solicitud.fechaCreacion,
    fechaUltimaMod: solicitud.fechaUltimaMod,
    fechaUltimoCambioEstado: solicitud.fechaUltimaMod,
    miRelacion:
      sesion.tipo === 'ASPIRANTE'
        ? 'ASPIRANTE'
        : sesion.rol === 'ADMIN' || sesion.rol === 'REVISOR'
          ? sesion.rol
          : esParticipante(sesion, solicitud)
            ? 'PARTICIPANTE'
            : 'OBSERVADOR',
    accionesDisponibles: accionesDe(sesion, solicitud),
    ultimaObservacion: observacionDe(solicitud),
    resumenDocumentos: { ok: contar('OK'), recargar: contar('RECARGAR'), pendiente: contar('PENDIENTE') },
    chatHabilitado: Boolean(solicitud.responsableAcademico ?? solicitud.responsableAdministrativo),
    mensajesNoLeidos: 0,
  }
}

/** El aspirante: primero lo que espera su acción. El personal: primero sus carreras, después el resto alfabético. */
function listar(sesion: Sesion, consulta: URLSearchParams) {
  const estados = consulta.getAll('estado')
  const busqueda = (consulta.get('q') ?? '').trim().toLowerCase()
  const dia = (iso: string) => iso.slice(0, 10)
  const esperan = ESPERAN_A[sesion.rol] ?? []

  const filas = SOLICITUDES.filter((item) => sesion.tipo === 'USUARIO' || item.aspirante.id === sesion.id)
    .filter((item) => !estados.length || estados.includes(item.estado.codigo))
    .filter((item) => !consulta.has('finalizadas') || String(item.estado.esTerminal) === consulta.get('finalizadas'))
    .filter((item) => consulta.get('asignadas') !== 'true' || esResponsable(sesion, item))
    .filter((item) => consulta.get('sinResponsable') !== 'true' || accionesDe(sesion, item).includes('TOMAR_PROCESO'))
    .filter((item) => !consulta.get('desde') || dia(item.fechaUltimaMod) >= consulta.get('desde')!)
    .filter((item) => !consulta.get('hasta') || dia(item.fechaUltimaMod) <= consulta.get('hasta')!)
    .filter((item) =>
      [item.codigoExp, item.aspirante.nombre, item.carreraDestino.nombre].some((texto) =>
        texto.toLowerCase().includes(busqueda),
      ),
    )
    .map((item) => ({
      ...item,
      esDeMisCarreras: Boolean(sesion.carrerasConPermiso?.some(({ id }) => id === item.carreraDestino.id)),
      requiereMiAccion:
        esperan.includes(item.estado.codigo) && (sesion.tipo === 'ASPIRANTE' || esResponsable(sesion, item)),
    }))
    .sort((a, b) =>
      sesion.tipo === 'ASPIRANTE'
        ? Number(b.requiereMiAccion) - Number(a.requiereMiAccion) || b.fechaUltimaMod.localeCompare(a.fechaUltimaMod)
        : Number(b.esDeMisCarreras) - Number(a.esDeMisCarreras) ||
          a.carreraDestino.nombre.localeCompare(b.carreraDestino.nombre, 'es') ||
          b.fechaUltimaMod.localeCompare(a.fechaUltimaMod),
    )

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

function crear(sesion: Sesion, body: unknown) {
  const datos = body as DatosNuevaSolicitud
  const carrera = CARRERAS.find(({ codigo }) => codigo === datos.carreraDestinoCodigo)
  if (!carrera) throw error(400, 'VALIDACION', { carreraDestinoCodigo: ['La carrera no existe o no está activa.'] })
  const ahora = new Date().toISOString()
  const id = SOLICITUDES.length + 1
  SOLICITUDES.push({
    id,
    codigoExp: `EXP-2026-${String(id).padStart(3, '0')}`,
    aspirante: { id: sesion.id, nombre: nombreCompleto(PERFIL) },
    universidadOrigen: datos.universidadOrigen,
    carreraDestino: carrera,
    estado: estado('SOL-INI'),
    responsableAcademico: null,
    responsableAdministrativo: null,
    fechaCreacion: ahora,
    fechaUltimaMod: ahora,
    requiereMiAccion: true,
    esDeMisCarreras: false,
  })
  origenes.set(id, {
    universidad: datos.universidadOrigen,
    facultad: datos.facultadOrigen,
    carrera: datos.carreraOrigen,
    condicionAcademica: datos.condicionAcademica,
    observaciones: datos.observaciones,
  })
  historiales.set(id, [cambioEstado(ahora, null, 'SOL-INI', nombreCompleto(PERFIL))])
  return detalle(sesion, id)
}

/** En `DOC-RECH` el archivo reemplaza a un documento marcado para recargar y hereda su tipo y su descripción. */
function subirDocumento(sesion: Sesion, body: unknown, id: number) {
  const solicitud = visible(sesion, id)
  const formulario = body as FormData
  const archivo = formulario.get('archivo') as File
  const reemplazaAId = Number(formulario.get('reemplazaAId')) || null
  const observado = vigentes(id).find((item) => item.id === reemplazaAId)
  if (solicitud.estado.codigo === 'DOC-RECH') {
    if (!observado) throw error(400, 'VALIDACION', { reemplazaAId: ['Indicá qué documento reemplaza el archivo.'] })
    if (observado.estadoRevision !== 'RECARGAR') throw error(409, 'DOCUMENTO_BLOQUEADO')
    documentos.set(
      id,
      documentosDe(id).filter((item) => !item.borrador || item.reemplazaAId !== reemplazaAId),
    )
  } else if (solicitud.estado.codigo !== 'SOL-INI') {
    throw error(409, 'TRANSICION_INVALIDA')
  }
  const nuevo: Documento = {
    ...documento(
      observado?.tipo ?? (formulario.get('tipo') as TipoDocumento),
      observado ? observado.descripcion : (formulario.get('descripcion') as string | null),
      archivo.name,
      new Date().toISOString(),
    ),
    tamanoBytes: archivo.size,
    borrador: true,
    version: observado ? observado.version + (observado.nombreArchivo ? 1 : 0) : 1,
    reemplazaAId: observado?.id ?? null,
    urlDescarga: URL.createObjectURL(archivo),
  }
  documentosDe(id).push(nuevo)
  return nuevo
}

function quitarDocumento(id: number, documentoId: number) {
  documentos.set(
    id,
    documentosDe(id).filter((item) => item.id !== documentoId || !item.borrador),
  )
}

function cambiarEstado(sesion: Sesion, solicitud: SolicitudResumen, nuevo: string, comentario: string | null = null) {
  const ahora = new Date().toISOString()
  const usuario = sesion.tipo === 'ASPIRANTE' ? solicitud.aspirante.nombre : nombreCompleto(sesion)
  historialDe(solicitud.id).unshift(cambioEstado(ahora, solicitud.estado.codigo, nuevo, usuario, comentario))
  // Lo que escribe el aspirante queda en el historial, pero no es una observación del personal.
  if (comentario && sesion.tipo === 'USUARIO') observaciones.set(solicitud.id, { comentario, usuario, fecha: ahora })
  solicitud.estado = estado(nuevo)
  solicitud.fechaUltimaMod = ahora
}

function enviarDocumentacion(sesion: Sesion, body: unknown, id: number) {
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'ENVIAR_DOCUMENTACION', (body as { estadoEsperado: string }).estadoEsperado)
  const borradores = documentosDe(id).filter(({ borrador }) => borrador)
  const recarga = solicitud.estado.codigo === 'DOC-RECH'
  const falta = recarga
    ? vigentes(id).some(
        (item) => item.estadoRevision === 'RECARGAR' && !borradores.some((nuevo) => nuevo.reemplazaAId === item.id),
      )
    : TIPOS_OBLIGATORIOS.some((tipo) => !borradores.some((item) => item.tipo === tipo))
  if (falta) throw error(400, 'VALIDACION', { documentos: ['Faltan documentos obligatorios.'] })
  borradores.forEach((item) => {
    item.borrador = false
  })
  cambiarEstado(sesion, solicitud, 'DOC-REV')
  historialDe(id).splice(1, 0, ...borradores.map((cargado) => documentoCargado(cargado, solicitud.aspirante.nombre)))
  return detalle(sesion, id)
}

function cancelar(sesion: Sesion, body: unknown, id: number) {
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'CANCELAR_SOLICITUD', (body as { estadoEsperado: string }).estadoEsperado)
  documentos.set(id, [])
  cambiarEstado(sesion, solicitud, 'SOL-CANC')
  return detalle(sesion, id)
}

/** Tomar el proceso o darse de baja: el estado no cambia y queda en el historial (RF-PROC-003 y 004). */
function cambiarResponsable(sesion: Sesion, id: number, tomar: boolean) {
  const solicitud = buscar(id)
  if (tomar) {
    if (!esParticipante(sesion, solicitud)) throw error(403, 'NO_PARTICIPANTE')
    if (responsableDe(solicitud, sesion.rol)) throw error(409, 'PROCESO_YA_TOMADO')
    if (solicitud.estado.esTerminal) throw error(409, 'TRANSICION_INVALIDA')
  } else {
    if (!esResponsable(sesion, solicitud)) throw error(403, 'ACCESO_DENEGADO')
    if (!hayOtrosConAcceso(sesion, solicitud)) throw error(409, 'UNICO_RESPONSABLE')
  }
  const ahora = new Date().toISOString()
  const nuevo = tomar ? responsable(sesion.email, ahora) : null
  const papel = sesion.rol === 'ACAD' ? 'responsable académico' : 'responsable administrativo'
  if (sesion.rol === 'ACAD') solicitud.responsableAcademico = nuevo
  else solicitud.responsableAdministrativo = nuevo
  solicitud.fechaUltimaMod = ahora
  historialDe(id).unshift({
    fecha: ahora,
    tipo: 'CAMBIO_RESPONSABLE',
    estadoAnterior: null,
    estadoNuevo: null,
    usuario: nombreCompleto(sesion),
    comentario: tomar ? `Tomó el proceso como ${papel}.` : `Se dio de baja como ${papel}.`,
  })
  return detalle(sesion, id)
}

function marcar(sesion: Sesion, id: number, marcas: Marca[] = []) {
  const ahora = new Date().toISOString()
  for (const { documentoId, estadoRevision, comentario } of marcas) {
    const marcado = vigentes(id).find((item) => item.id === documentoId)
    if (!marcado || (!marcado.nombreArchivo && estadoRevision !== 'RECARGAR')) {
      throw error(400, 'VALIDACION', { marcas: ['Revisá las marcas de los documentos.'] })
    }
    Object.assign(marcado, {
      estadoRevision,
      comentarioRevision: comentario?.trim() || null,
      revisadoPor: nombreCompleto(sesion),
      fechaRevision: ahora,
    })
  }
}

function guardarMarcas(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, marcas } = body as { estadoEsperado: string; marcas: Marca[] }
  exigir(sesion, buscar(id), 'GUARDAR_MARCAS', estadoEsperado)
  marcar(sesion, id, marcas)
  return vigentes(id)
}

function agregarFaltante(sesion: Sesion, body: unknown, id: number) {
  const { tipo, descripcion } = body as { tipo: TipoDocumento; descripcion: string }
  exigir(sesion, buscar(id), 'AGREGAR_FALTANTE')
  if (!descripcion.trim()) throw error(400, 'VALIDACION', { descripcion: ['Indicá qué documento falta.'] })
  const faltante: Documento = {
    ...documento(tipo, descripcion.trim(), '', new Date().toISOString(), 'RECARGAR'),
    nombreArchivo: null,
    tamanoBytes: null,
    fechaCarga: null,
    urlDescarga: null,
    esRequeridoFaltante: true,
    revisadoPor: nombreCompleto(sesion),
  }
  documentosDe(id).push(faltante)
  return faltante
}

function revisar(sesion: Sesion, body: unknown, id: number, aceptar: boolean) {
  const { estadoEsperado, comentario, marcas } = body as { estadoEsperado: string; comentario?: string; marcas: Marca[] }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, aceptar ? 'ACEPTAR_DOCUMENTACION' : 'RECHAZAR_DOCUMENTACION', estadoEsperado)
  if (!aceptar && !comentario?.trim()) throw error(400, 'VALIDACION', { comentario: ['Indicá el motivo del rechazo.'] })
  const resultantes = vigentes(id).map(
    (item) => marcas.find((marca) => marca.documentoId === item.id)?.estadoRevision ?? item.estadoRevision,
  )
  if (!aceptar && (!resultantes.includes('RECARGAR') || resultantes.includes('PENDIENTE'))) {
    throw error(409, 'MARCAS_INCOMPLETAS')
  }
  marcar(sesion, id, marcas)
  cambiarEstado(sesion, solicitud, aceptar ? 'ANAL-PEND' : 'DOC-RECH', aceptar ? null : comentario!.trim())
  return detalle(sesion, id)
}

function documentoAnalisis(
  tipo: TipoAnalisis,
  nombreArchivo: string | null,
  notas: string | null,
  urlDescarga: string | null,
  subidoPor: string,
  fechaCarga: string,
): Analisis {
  ultimoDocumentoId += 1
  return { id: ultimoDocumentoId, tipo, version: 1, nombreArchivo, notas, fechaCarga, subidoPor, urlDescarga }
}

function analisisDe(id: number) {
  if (!analisis.has(id)) {
    const { estado: actual, fechaUltimaMod, responsableAcademico } = buscar(id)
    const autor = responsableAcademico?.nombre ?? ''
    analisis.set(
      id,
      SIN_ANALISIS.includes(actual.codigo)
        ? []
        : [
            documentoAnalisis(
              'MATRIZ_EQUIVALENCIAS',
              'matriz-equivalencias.pdf',
              'Se reconocen 6 materias de primer año.',
              ARCHIVO_DEMO,
              autor,
              fechaUltimaMod,
            ),
            documentoAnalisis(
              'HORARIO_CURSADA',
              null,
              'Turno noche: lunes, miércoles y viernes de 18 a 22.',
              null,
              autor,
              fechaUltimaMod,
            ),
          ],
    )
  }
  return analisis.get(id)!
}

/** Dentro de `dias` días a la hora indicada de Argentina. */
function fechaDemo(dias: number, hora: number) {
  return `${sumarDias(hoyEnArgentina(), dias)}T${String(hora).padStart(2, '0')}:00:00${REUNION.DESFASE_HORARIO}`
}

function ronda(numero: number, inicios: string[]) {
  return {
    numero,
    horarios: [...inicios].sort().map((inicio) => {
      ultimoIdReunion += 1
      return { id: ultimoIdReunion, inicio, fin: finDeReunion(inicio), elegido: false }
    }),
    motivoOtrasFechas: null,
    disponibilidadAspirante: null,
  }
}

/** Las de ejemplo ya traen la 1ª ronda propuesta; las que pasaron la elección, también la reunión. */
function reunionDe(id: number) {
  if (!reuniones.has(id)) {
    const codigo = buscar(id).estado.codigo
    const info: ReunionInfo = { rondaActual: null, rondas: [], reunion: null }
    if (!SIN_ANALISIS.includes(codigo)) {
      const primera = ronda(1, [fechaDemo(3, 10), fechaDemo(3, 15), fechaDemo(5, 11), fechaDemo(8, 16)])
      info.rondaActual = 1
      info.rondas = [primera]
      if (codigo !== 'REU-FEC-ASP-1') {
        const [elegido] = primera.horarios
        elegido.elegido = true
        info.reunion = {
          inicio: elegido.inicio,
          fin: elegido.fin,
          origen: 'ELEGIDA_ASPIRANTE',
          enlaceMeet: null,
          resultado: codigo === 'REU-PEND' ? 'PENDIENTE' : 'REALIZADA',
        }
      }
    }
    reuniones.set(id, info)
  }
  return reuniones.get(id)!
}

function abrirRonda(id: number, numero: number, horarios: { inicio: string }[]) {
  if (!horarios.length || horarios.length > REUNION.MAX_HORARIOS) {
    throw error(400, 'VALIDACION', { horarios: [`Proponé entre 1 y ${REUNION.MAX_HORARIOS} horarios.`] })
  }
  const info = reunionDe(id)
  info.rondas.push(ronda(numero, horarios.map(({ inicio }) => inicio)))
  info.rondaActual = numero
}

/** La parte `datos` llega como JSON dentro del formulario, igual que en el backend. */
async function proponerFechas(sesion: Sesion, body: unknown, id: number) {
  const formulario = body as FormData
  const { estadoEsperado, horarios } = JSON.parse(await (formulario.get('datos') as Blob).text()) as {
    estadoEsperado: string
    horarios: { inicio: string }[]
  }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, 'PROPONER_FECHAS', estadoEsperado)
  const ahora = new Date().toISOString()
  const cargar = (tipo: TipoAnalisis, campo: string) => {
    const archivo = formulario.get(`${campo}Archivo`) as File | null
    const notas = formulario.get(`${campo}Notas`) as string | null
    if (!archivo && !notas) throw error(400, 'VALIDACION', { [campo]: ['Cargá un archivo, notas o las dos cosas.'] })
    return documentoAnalisis(
      tipo,
      archivo?.name ?? null,
      notas,
      archivo && URL.createObjectURL(archivo),
      nombreCompleto(sesion),
      ahora,
    )
  }
  const cargados = [cargar('MATRIZ_EQUIVALENCIAS', 'matriz'), cargar('HORARIO_CURSADA', 'horario')]
  abrirRonda(id, 1, horarios)
  analisis.set(id, cargados)
  cambiarEstado(sesion, solicitud, 'REU-FEC-ASP-1')
  return detalle(sesion, id)
}

function proponerSegundaRonda(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, horarios } = body as { estadoEsperado: string; horarios: { inicio: string }[] }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, 'PROPONER_SEGUNDA_RONDA', estadoEsperado)
  abrirRonda(id, 2, horarios)
  cambiarEstado(sesion, solicitud, 'REU-FEC-ASP-2')
  return detalle(sesion, id)
}

function elegirHorario(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, horarioId } = body as { estadoEsperado: string; horarioId: number }
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'ELEGIR_HORARIO', estadoEsperado)
  const info = reunionDe(id)
  const elegido = info.rondas.at(-1)!.horarios.find((horario) => horario.id === horarioId)
  if (!elegido) throw error(400, 'VALIDACION', { horarioId: ['Elegí uno de los horarios propuestos.'] })
  elegido.elegido = true
  info.reunion = {
    inicio: elegido.inicio,
    fin: elegido.fin,
    origen: 'ELEGIDA_ASPIRANTE',
    enlaceMeet: null,
    resultado: 'PENDIENTE',
  }
  cambiarEstado(sesion, solicitud, 'REU-PEND')
  return detalle(sesion, id)
}

function solicitarOtrasFechas(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, motivo, disponibilidad } = body as Record<string, string>
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'SOLICITAR_OTRAS_FECHAS', estadoEsperado)
  if (!motivo?.trim()) throw error(400, 'VALIDACION', { motivo: ['Contanos por qué no podés en esos horarios.'] })
  if (!disponibilidad?.trim()) throw error(400, 'VALIDACION', { disponibilidad: ['Indicá cuándo podés reunirte.'] })
  Object.assign(reunionDe(id).rondas.at(-1)!, { motivoOtrasFechas: motivo, disponibilidadAspirante: disponibilidad })
  cambiarEstado(sesion, solicitud, 'REU-FEC-ACAD', `Motivo: ${motivo} · Puede: ${disponibilidad}`)
  return detalle(sesion, id)
}

function rechazarSegundaRonda(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, motivo } = body as Record<string, string>
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'RECHAZAR_SEGUNDA_RONDA', estadoEsperado)
  cambiarEstado(sesion, solicitud, 'REU-CHAT', motivo?.trim() || null)
  return detalle(sesion, id)
}

/** No cambia el estado: devuelve la reunión con el enlace. */
function cargarEnlaceMeet(sesion: Sesion, body: unknown, id: number) {
  const enlaceMeet = (body as { enlaceMeet: string }).enlaceMeet.trim()
  exigir(sesion, buscar(id), 'CARGAR_ENLACE_MEET')
  if (!enlaceMeet.startsWith(REUNION.PREFIJO_MEET)) {
    throw error(400, 'VALIDACION', { enlaceMeet: [`Pegá el enlace de Google Meet (${REUNION.PREFIJO_MEET}…).`] })
  }
  reunionDe(id).reunion!.enlaceMeet = enlaceMeet
  return structuredClone(reunionDe(id))
}

type Manejador = (body: unknown, ids: number[], consulta: URLSearchParams) => unknown

export function rutasSolicitudes(sesionActual: () => Sesion): Record<string, Manejador> {
  return {
    'GET /solicitudes': (_, __, consulta) => listar(sesionActual(), consulta),
    'POST /solicitudes': (body) => crear(sesionActual(), body),
    'GET /solicitudes/:id': (_, [id]) => detalle(sesionActual(), id),
    'GET /solicitudes/:id/historial': (_, [id]) => [...historialDe(visible(sesionActual(), id).id)],
    'GET /solicitudes/:id/documentos': (_, [id]) => visibles(sesionActual(), visible(sesionActual(), id).id),
    'POST /solicitudes/:id/documentos': (body, [id]) => subirDocumento(sesionActual(), body, id),
    'DELETE /solicitudes/:id/documentos/:id': (_, [id, documentoId]) => quitarDocumento(id, documentoId),
    'PUT /solicitudes/:id/documentos/marcas': (body, [id]) => guardarMarcas(sesionActual(), body, id),
    'POST /solicitudes/:id/documentos/faltantes': (body, [id]) => agregarFaltante(sesionActual(), body, id),
    'POST /solicitudes/:id/documentacion/enviar': (body, [id]) => enviarDocumentacion(sesionActual(), body, id),
    'POST /solicitudes/:id/documentacion/aceptar': (body, [id]) => revisar(sesionActual(), body, id, true),
    'POST /solicitudes/:id/documentacion/rechazar': (body, [id]) => revisar(sesionActual(), body, id, false),
    'POST /solicitudes/:id/cancelar': (body, [id]) => cancelar(sesionActual(), body, id),
    'POST /solicitudes/:id/responsables/tomar': (_, [id]) => cambiarResponsable(sesionActual(), id, true),
    'POST /solicitudes/:id/responsables/baja': (_, [id]) => cambiarResponsable(sesionActual(), id, false),
    'GET /solicitudes/:id/analisis': (_, [id]) => [...analisisDe(visible(sesionActual(), id).id)],
    'POST /solicitudes/:id/analisis/propuesta': (body, [id]) => proponerFechas(sesionActual(), body, id),
    'GET /solicitudes/:id/reunion': (_, [id]) => structuredClone(reunionDe(visible(sesionActual(), id).id)),
    'POST /solicitudes/:id/reunion/segunda-ronda': (body, [id]) => proponerSegundaRonda(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/elegir-horario': (body, [id]) => elegirHorario(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/solicitar-otras-fechas': (body, [id]) =>
      solicitarOtrasFechas(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/rechazar-ronda': (body, [id]) => rechazarSegundaRonda(sesionActual(), body, id),
    'PUT /solicitudes/:id/reunion/enlace': (body, [id]) => cargarEnlaceMeet(sesionActual(), body, id),
  }
}
