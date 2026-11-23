/**
 * Mocks de solicitudes, documentos, historial, acciones del trámite y chat (contrato de la API §6, §7 y §8.1).
 * Responden según quién tiene la sesión abierta, igual que el backend. Todo vive en memoria.
 */
import { ESTADOS_DE_ACCION } from '@/constants/estados'
import { finDeReunion, REUNION } from '@/constants/reunion'
import { ApiError } from '@/services/api/ApiError'
import type { Mensaje, TipoConsulta } from '@/types/chat'
import type { Sesion } from '@/types/sesion'
import type {
  Analisis,
  DatosNuevaSolicitud,
  Documento,
  EstadoRevision,
  EventoHistorial,
  InformeMaterias,
  Marca,
  Pago,
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

const HASTA_LA_INSCRIPCION = ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND', 'ANAL-POST', 'INSC-ACEP']

/** Estados por los que pasó cada solicitud de ejemplo, según su estado actual. */
const RECORRIDOS: Record<string, string[]> = {
  'DOC-REV': ['SOL-INI', 'DOC-REV'],
  'DOC-RECH': ['SOL-INI', 'DOC-REV', 'DOC-RECH'],
  'ANAL-PEND': ['SOL-INI', 'DOC-REV', 'ANAL-PEND'],
  'REU-FEC-ASP-1': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1'],
  'REU-PEND': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND'],
  'REU-CHAT': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-FEC-ACAD', 'REU-FEC-ASP-2', 'REU-CHAT'],
  'ANAL-POST': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND', 'ANAL-POST'],
  'INSC-ACEP': ['SOL-INI', 'DOC-REV', 'ANAL-PEND', 'REU-FEC-ASP-1', 'REU-PEND', 'ANAL-POST', 'INSC-ACEP'],
  'LEG-REV': [...HASTA_LA_INSCRIPCION, 'LEG-REV'],
  'CUPON-SOL': [...HASTA_LA_INSCRIPCION, 'LEG-REV', 'CUPON-SOL'],
  'PAG-CTRL': [...HASTA_LA_INSCRIPCION, 'LEG-REV', 'CUPON-SOL', 'CUPON-PAG', 'PAG-CTRL'],
}

/** Desde `CUPON-SOL` existe el informe de materias, y desde `CUPON-PAG` el cupón. */
const CON_INFORME = ['CUPON-SOL', 'CUPON-PAG', 'PAG-CTRL', 'PAG-REENV', 'E3-INI']
const MONTO_DEMO = 150000

/** Estados anteriores a que el Académico cargue el análisis y proponga fechas. */
const SIN_ANALISIS = ['SOL-INI', 'DOC-REV', 'DOC-RECH', 'ANAL-PEND', 'SOL-CANC']

/** Acciones del aspirante en cada estado (contrato de la API §7.1). */
const ACCIONES_ASPIRANTE: Record<string, string[]> = {
  'SOL-INI': ['ENVIAR_DOCUMENTACION', 'CANCELAR_SOLICITUD'],
  'DOC-RECH': ['ENVIAR_DOCUMENTACION'],
  'REU-FEC-ASP-1': ['ELEGIR_HORARIO', 'SOLICITAR_OTRAS_FECHAS'],
  'REU-FEC-ASP-2': ['ELEGIR_HORARIO', 'RECHAZAR_SEGUNDA_RONDA'],
  'INSC-ACEP': ['INDICAR_LEGALIZACION'],
  'LEG-PEND': ['AVISAR_LEGALIZADO'],
  'LEG-RECH': ['ENVIAR_DOCUMENTACION'],
  'CUPON-PAG': ['ENVIAR_COMPROBANTE'],
  'PAG-REENV': ['ENVIAR_COMPROBANTE'],
}

/** Acciones del Académico responsable en cada estado (contrato de la API §7.1 y §7.2). */
const ACCIONES_ACADEMICO: Record<string, string[]> = {
  'DOC-REV': ['RECHAZAR_DOCUMENTACION', 'ACEPTAR_DOCUMENTACION', 'GUARDAR_MARCAS', 'AGREGAR_FALTANTE'],
  'ANAL-PEND': ['PROPONER_FECHAS'],
  'REU-FEC-ACAD': ['PROPONER_SEGUNDA_RONDA'],
  'REU-CHAT': ['FIJAR_FECHA'],
  'REU-PEND': ['MARCAR_NO_ASISTIO', 'FINALIZAR_REUNION', 'CARGAR_ENLACE_MEET'],
  'ANAL-POST': ['ACEPTAR_INSCRIPCION', 'RECHAZAR_INSCRIPCION', 'ACTUALIZAR_ANALISIS'],
  'LEG-REV': ['RECHAZAR_LEGALIZADOS', 'ACEPTAR_LEGALIZADOS', 'GUARDAR_MARCAS', 'AGREGAR_FALTANTE'],
}

/** Acciones de la Secretaría responsable en cada estado (contrato de la API §7.1). */
const ACCIONES_SECRETARIA: Record<string, string[]> = {
  'CUPON-SOL': ['ENVIAR_CUPON'],
  'PAG-CTRL': ['ACEPTAR_PAGO', 'RECHAZAR_PAGO'],
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

type MensajeGuardado = Omit<Mensaje, 'visto' | 'esMio'>

const origenes = new Map<number, SolicitudDetalle['datosOrigen']>()
const documentos = new Map<number, Documento[]>()
const historiales = new Map<number, EventoHistorial[]>()
const observaciones = new Map<number, NonNullable<SolicitudDetalle['ultimaObservacion']>>()
const analisis = new Map<number, Analisis[]>()
const reuniones = new Map<number, ReunionInfo>()
const informes = new Map<number, InformeMaterias>()
const pagos = new Map<number, Pago>()
/** Los mensajes se guardan sin `visto` ni `esMio`, que dependen de quién mira. */
const mensajes = new Map<number, MensajeGuardado[]>()
/** Hasta qué mensaje leyó cada participante: por solicitud, y adentro por lector. */
const lecturas = new Map<number, Map<string, number>>()
let ultimoDocumentoId = 300
let ultimoIdReunion = 500
let ultimoMensajeId = 800

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
    ...((sesion.rol === 'ACAD' ? ACCIONES_ACADEMICO : ACCIONES_SECRETARIA)[codigo] ?? []),
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
    const revision = actual.codigo === 'DOC-REV' || actual.codigo === 'LEG-REV' ? 'PENDIENTE' : 'OK'
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
    chatHabilitado: hayResponsable(solicitud),
    puedeEscribirChat: puedeEscribir(sesion, solicitud),
    mensajesNoLeidos: esDelChat(sesion, solicitud)
      ? mensajesDe(id).filter((item) => !esAutor(sesion, item) && item.id > leidoHasta(sesion, id)).length
      : 0,
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
  if (RECARGAS[solicitud.estado.codigo]) {
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

/** Estados de recarga y a cuál vuelve la solicitud al enviar lo recargado. */
const RECARGAS: Record<string, string> = { 'DOC-RECH': 'DOC-REV', 'LEG-RECH': 'LEG-REV' }

function enviarDocumentacion(sesion: Sesion, body: unknown, id: number) {
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'ENVIAR_DOCUMENTACION', (body as { estadoEsperado: string }).estadoEsperado)
  const borradores = documentosDe(id).filter(({ borrador }) => borrador)
  const recarga = RECARGAS[solicitud.estado.codigo]
  const falta = recarga
    ? vigentes(id).some(
        (item) => item.estadoRevision === 'RECARGAR' && !borradores.some((nuevo) => nuevo.reemplazaAId === item.id),
      )
    : TIPOS_OBLIGATORIOS.some((tipo) => !borradores.some((item) => item.tipo === tipo))
  if (falta) throw error(400, 'VALIDACION', { documentos: ['Faltan documentos obligatorios.'] })
  borradores.forEach((item) => {
    item.borrador = false
  })
  cambiarEstado(sesion, solicitud, recarga ?? 'DOC-REV')
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

/** `legalizados` es la revisión de `LEG-REV`: al aceptar exige el informe y que no quede nada por recargar. */
function revisar(sesion: Sesion, body: unknown, id: number, aceptar: boolean, legalizados = false) {
  const { estadoEsperado, comentario, marcas, materiasReconocidas, observaciones } = body as {
    estadoEsperado: string
    comentario?: string
    marcas: Marca[]
    materiasReconocidas?: number
    observaciones?: string
  }
  const solicitud = buscar(id)
  const que = legalizados ? 'LEGALIZADOS' : 'DOCUMENTACION'
  exigir(sesion, solicitud, `${aceptar ? 'ACEPTAR' : 'RECHAZAR'}_${que}`, estadoEsperado)
  if (!aceptar && !comentario?.trim()) throw error(400, 'VALIDACION', { comentario: ['Indicá el motivo del rechazo.'] })
  const resultantes = vigentes(id).map(
    (item) => marcas.find((marca) => marca.documentoId === item.id)?.estadoRevision ?? item.estadoRevision,
  )
  if (!aceptar && (!resultantes.includes('RECARGAR') || resultantes.includes('PENDIENTE'))) {
    throw error(409, 'MARCAS_INCOMPLETAS')
  }
  if (legalizados && aceptar) {
    if (resultantes.includes('RECARGAR')) throw error(400, 'VALIDACION', { marcas: ['Hay documentos a recargar.'] })
    if (!Number.isInteger(materiasReconocidas) || materiasReconocidas! < 0) {
      throw error(400, 'VALIDACION', { materiasReconocidas: ['Indicá la cantidad de materias reconocidas.'] })
    }
    informes.set(id, { materiasReconocidas: materiasReconocidas!, observaciones: observaciones?.trim() || null, montoAPagar: null })
  }
  marcar(sesion, id, marcas)
  const destino = legalizados ? (aceptar ? 'CUPON-SOL' : 'LEG-RECH') : aceptar ? 'ANAL-PEND' : 'DOC-RECH'
  cambiarEstado(sesion, solicitud, destino, aceptar ? null : comentario!.trim())
  return detalle(sesion, id)
}

/** Al pasar a `LEG-REV` los documentos se revisan de nuevo, ahora en la etapa de legalización. */
function iniciarLegalizacion(id: number) {
  vigentes(id).forEach((item) => {
    item.etapaRevision = 'LEGALIZACION'
    if (item.nombreArchivo) Object.assign(item, { estadoRevision: 'PENDIENTE', comentarioRevision: null, revisadoPor: null })
  })
}

function indicarLegalizacion(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, legalizados } = body as { estadoEsperado: string; legalizados?: boolean }
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'INDICAR_LEGALIZACION', estadoEsperado)
  if (typeof legalizados !== 'boolean') throw error(400, 'VALIDACION', { legalizados: ['Indicá si están legalizados.'] })
  if (legalizados) iniciarLegalizacion(id)
  cambiarEstado(sesion, solicitud, legalizados ? 'LEG-REV' : 'LEG-PEND')
  return detalle(sesion, id)
}

function avisarLegalizado(sesion: Sesion, body: unknown, id: number) {
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'AVISAR_LEGALIZADO', (body as { estadoEsperado: string }).estadoEsperado)
  iniciarLegalizacion(id)
  cambiarEstado(sesion, solicitud, 'LEG-REV')
  return detalle(sesion, id)
}

function pagoDe(id: number) {
  if (!pagos.has(id)) {
    const { estado: actual, fechaUltimaMod } = buscar(id)
    const conCupon = CON_INFORME.includes(actual.codigo) && actual.codigo !== 'CUPON-SOL'
    pagos.set(id, {
      cupon: conCupon ? { monto: MONTO_DEMO, fechaEmision: fechaUltimaMod, urlDescarga: ARCHIVO_DEMO } : null,
      comprobantes:
        actual.codigo === 'PAG-CTRL'
          ? [
              {
                id: 1,
                version: 1,
                nombreArchivo: 'comprobante-transferencia.pdf',
                tamanoBytes: 240 * 1024,
                fechaCarga: fechaUltimaMod,
                estadoVerificacion: 'PENDIENTE',
                comentarioSecretaria: null,
                urlDescarga: ARCHIVO_DEMO,
              },
            ]
          : [],
    })
  }
  return pagos.get(id)!
}

function informeDe(sesion: Sesion, id: number): InformeMaterias {
  const solicitud = visible(sesion, id)
  if (!CON_INFORME.includes(solicitud.estado.codigo)) throw error(404, 'NO_ENCONTRADO')
  const informe = informes.get(id) ?? {
    materiasReconocidas: 6,
    observaciones: 'Quedan Física II y Química para cursar.',
    montoAPagar: null,
  }
  return { ...informe, montoAPagar: pagoDe(id).cupon?.monto ?? null }
}

function enviarCupon(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, monto } = body as { estadoEsperado: string; monto?: number }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, 'ENVIAR_CUPON', estadoEsperado)
  if (typeof monto !== 'number' || monto <= 0) throw error(400, 'VALIDACION', { monto: ['Indicá el monto del cupón.'] })
  pagoDe(id).cupon = { monto, fechaEmision: new Date().toISOString(), urlDescarga: ARCHIVO_DEMO }
  cambiarEstado(sesion, solicitud, 'CUPON-PAG')
  return detalle(sesion, id)
}

function enviarComprobante(sesion: Sesion, body: unknown, id: number) {
  const formulario = body as FormData
  const archivo = formulario.get('archivo') as File | null
  const solicitud = visible(sesion, id)
  exigir(sesion, solicitud, 'ENVIAR_COMPROBANTE', formulario.get('estadoEsperado') as string)
  if (!archivo) throw error(400, 'VALIDACION', { archivo: ['Cargá el comprobante de pago.'] })
  const { comprobantes } = pagoDe(id)
  comprobantes.push({
    id: comprobantes.length + 1,
    version: comprobantes.length + 1,
    nombreArchivo: archivo.name,
    tamanoBytes: archivo.size,
    fechaCarga: new Date().toISOString(),
    estadoVerificacion: 'PENDIENTE',
    comentarioSecretaria: null,
    urlDescarga: URL.createObjectURL(archivo),
  })
  cambiarEstado(sesion, solicitud, 'PAG-CTRL')
  return detalle(sesion, id)
}

function verificarPago(sesion: Sesion, body: unknown, id: number, aceptar: boolean) {
  const { estadoEsperado, comentario } = body as { estadoEsperado: string; comentario?: string }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, aceptar ? 'ACEPTAR_PAGO' : 'RECHAZAR_PAGO', estadoEsperado)
  const limpio = comentario?.trim() || null
  if (!aceptar && !limpio) throw error(400, 'VALIDACION', { comentario: ['Indicá por qué se rechaza el pago.'] })
  Object.assign(pagoDe(id).comprobantes.at(-1)!, {
    estadoVerificacion: aceptar ? 'ACEPTADO' : 'RECHAZADO',
    comentarioSecretaria: limpio,
  })
  cambiarEstado(sesion, solicitud, aceptar ? 'E3-INI' : 'PAG-REENV', limpio)
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
      if (codigo === 'REU-CHAT') {
        // Pidió otras fechas en la 1ª ronda y rechazó la 2ª: la fecha se pauta por chat.
        Object.assign(primera, {
          motivoOtrasFechas: 'Trabajo en esos horarios.',
          disponibilidadAspirante: 'Martes por la mañana o jueves por la tarde.',
        })
        info.rondaActual = 2
        info.rondas.push(ronda(2, [fechaDemo(6, 10), fechaDemo(7, 17)]))
      } else if (codigo !== 'REU-FEC-ASP-1') {
        const [elegido] = primera.horarios
        elegido.elegido = true
        info.reunion = {
          inicio: elegido.inicio,
          fin: elegido.fin,
          origen: 'ELEGIDA_ASPIRANTE',
          enlaceMeet: null,
          resultado: codigo === 'REU-PEND' ? 'PENDIENTE' : 'REALIZADA',
          notas: codigo === 'REU-PEND' ? null : 'Se acordó reconocer 6 materias de primer año.',
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
    notas: null,
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

/** La fecha la pautaron por chat: alcanza con que sea a futuro. */
function fijarFecha(sesion: Sesion, body: unknown, id: number) {
  const { estadoEsperado, inicio } = body as { estadoEsperado: string; inicio?: string }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, 'FIJAR_FECHA', estadoEsperado)
  if (!inicio) throw error(400, 'VALIDACION', { inicio: ['Indicá el día y la hora que pautaron.'] })
  if (Date.parse(inicio) <= Date.now()) throw error(400, 'VALIDACION', { inicio: ['La reunión tiene que ser a futuro.'] })
  reunionDe(id).reunion = {
    inicio,
    fin: finDeReunion(inicio),
    origen: 'FIJADA_CHAT',
    enlaceMeet: null,
    resultado: 'PENDIENTE',
    notas: null,
  }
  cambiarEstado(sesion, solicitud, 'REU-PEND')
  return detalle(sesion, id)
}

/** La reunión se hizo, con notas del Académico, o el aspirante no se presentó y hay que reprogramarla. */
function cerrarReunion(sesion: Sesion, body: unknown, id: number, finalizo: boolean) {
  const { estadoEsperado, notas, comentario } = body as Record<string, string | undefined>
  const solicitud = buscar(id)
  exigir(sesion, solicitud, finalizo ? 'FINALIZAR_REUNION' : 'MARCAR_NO_ASISTIO', estadoEsperado)
  Object.assign(reunionDe(id).reunion!, {
    resultado: finalizo ? 'REALIZADA' : 'NO_ASISTIO',
    notas: (finalizo && notas?.trim()) || null,
  })
  // Las notas son del personal: no van al historial que ve el aspirante.
  cambiarEstado(sesion, solicitud, finalizo ? 'ANAL-POST' : 'REU-FEC-ACAD', finalizo ? null : comentario?.trim() || null)
  return detalle(sesion, id)
}

/** No cambia el estado: la versión nueva reemplaza entera a la anterior. */
function actualizarAnalisis(sesion: Sesion, body: unknown, id: number) {
  const formulario = body as FormData
  const tipo = formulario.get('tipo') as TipoAnalisis
  const archivo = formulario.get('archivo') as File | null
  const notas = formulario.get('notas') as string | null
  const solicitud = buscar(id)
  exigir(sesion, solicitud, 'ACTUALIZAR_ANALISIS')
  if (!archivo && !notas) throw error(400, 'VALIDACION', { notas: ['Cargá un archivo, notas o las dos cosas.'] })
  const ahora = new Date().toISOString()
  const autor = nombreCompleto(sesion)
  analisis.set(
    id,
    analisisDe(id).map((anterior) =>
      anterior.tipo === tipo
        ? {
            ...documentoAnalisis(tipo, archivo?.name ?? null, notas, archivo && URL.createObjectURL(archivo), autor, ahora),
            version: anterior.version + 1,
          }
        : anterior,
    ),
  )
  solicitud.fechaUltimaMod = ahora
  return [...analisisDe(id)]
}

function decidirInscripcion(sesion: Sesion, body: unknown, id: number, aceptar: boolean) {
  const { estadoEsperado, comentario } = body as { estadoEsperado: string; comentario?: string }
  const solicitud = buscar(id)
  exigir(sesion, solicitud, aceptar ? 'ACEPTAR_INSCRIPCION' : 'RECHAZAR_INSCRIPCION', estadoEsperado)
  if (!aceptar && !comentario?.trim()) throw error(400, 'VALIDACION', { comentario: ['Indicá el motivo del rechazo.'] })
  cambiarEstado(sesion, solicitud, aceptar ? 'INSC-ACEP' : 'INSC-RECH', aceptar ? null : comentario!.trim())
  return detalle(sesion, id)
}

function hayResponsable(solicitud: SolicitudResumen) {
  return Boolean(solicitud.responsableAcademico ?? solicitud.responsableAdministrativo)
}

/** El aspirante dueño y los responsables (RF-MSG-001): el resto del personal solo lee. */
function esDelChat(sesion: Sesion, solicitud: SolicitudResumen) {
  return sesion.tipo === 'ASPIRANTE' ? solicitud.aspirante.id === sesion.id : esResponsable(sesion, solicitud)
}

function puedeEscribir(sesion: Sesion, solicitud: SolicitudResumen) {
  return esDelChat(sesion, solicitud) && hayResponsable(solicitud) && !solicitud.estado.esTerminal
}

function esAutor(sesion: Sesion, { autor }: MensajeGuardado) {
  return autor.tipo === sesion.tipo && autor.id === sesion.id
}

function lector(tipo: string, id: number) {
  return `${tipo}-${id}`
}

function leidoHasta(sesion: Sesion, id: number) {
  return lecturas.get(id)?.get(lector(sesion.tipo, sesion.id)) ?? 0
}

function mensaje(
  autor: Mensaje['autor'],
  contenido: string,
  tipoConsulta: TipoConsulta,
  fechaEnvio: string,
  adjunto: Mensaje['adjunto'] = null,
): MensajeGuardado {
  ultimoMensajeId += 1
  return { id: ultimoMensajeId, autor, contenido, tipoConsulta, adjunto, fechaEnvio }
}

/** La solicitud de ejemplo en `REU-CHAT` ya trae la conversación empezada, con un mensaje sin leer para el aspirante. */
function mensajesDe(id: number) {
  if (!mensajes.has(id)) {
    const { estado: actual, aspirante, responsableAcademico, fechaUltimaMod } = buscar(id)
    const conversacion: MensajeGuardado[] = []
    if (actual.codigo === 'REU-CHAT' && responsableAcademico) {
      const { id: academicoId, nombre } = responsableAcademico
      const academico: Mensaje['autor'] = { tipo: 'USUARIO', id: academicoId, nombre, rol: 'ACAD', avatar: null }
      const elAspirante: Mensaje['autor'] = { tipo: 'ASPIRANTE', ...aspirante, rol: 'ASPIRANTE', avatar: null }
      const minutosDespues = (minutos: number) => new Date(Date.parse(fechaUltimaMod) + minutos * 60_000).toISOString()
      conversacion.push(
        mensaje(
          academico,
          'Hola, te escribo para coordinar la fecha de la reunión. ¿Qué días y horarios te resultan convenientes?',
          'FECHAS',
          minutosDespues(10),
        ),
        mensaje(
          elAspirante,
          'Hola, tengo disponibilidad el martes por la mañana o el jueves por la tarde.',
          'FECHAS',
          minutosDespues(14),
        ),
        mensaje(academico, 'Perfecto, el jueves a las 15:00 me funciona. ¿Te parece bien?', 'FECHAS', minutosDespues(16)),
      )
      const hastaElSegundo = conversacion[1].id
      lecturas.set(
        id,
        new Map([
          [lector('USUARIO', academicoId), hastaElSegundo],
          [lector('ASPIRANTE', aspirante.id), hastaElSegundo],
        ]),
      )
    }
    mensajes.set(id, conversacion)
  }
  return mensajes.get(id)!
}

function listarMensajes(sesion: Sesion, id: number, consulta: URLSearchParams) {
  visible(sesion, id)
  const despuesDe = Number(consulta.get('despuesDe') ?? 0)
  const size = Number(consulta.get('size') ?? 20)
  const nuevos = mensajesDe(id).filter((item) => item.id > despuesDe)
  const leidos = [...(lecturas.get(id) ?? [])]
  return {
    content: nuevos.slice(0, size).map((item) => ({
      ...item,
      esMio: esAutor(sesion, item),
      visto: leidos.some(([quien, hasta]) => quien !== lector(item.autor.tipo, item.autor.id) && hasta >= item.id),
    })),
    page: 0,
    size,
    totalElements: nuevos.length,
    totalPages: Math.ceil(nuevos.length / size),
  }
}

function exigirQueSeaDelChat(sesion: Sesion, solicitud: SolicitudResumen) {
  if (!esDelChat(sesion, solicitud)) {
    throw error(403, esParticipante(sesion, solicitud) ? 'NO_RESPONSABLE' : 'NO_PARTICIPANTE')
  }
}

function enviarMensaje(sesion: Sesion, body: unknown, id: number) {
  const formulario = body as FormData
  const contenido = ((formulario.get('contenido') as string | null) ?? '').trim()
  const adjunto = formulario.get('adjunto') as File | null
  const solicitud = visible(sesion, id)
  exigirQueSeaDelChat(sesion, solicitud)
  if (!hayResponsable(solicitud)) throw error(403, 'CHAT_DESHABILITADO')
  if (solicitud.estado.esTerminal) throw error(409, 'TRANSICION_INVALIDA')
  if (!contenido && !adjunto) throw error(400, 'VALIDACION', { contenido: ['Escribí un mensaje o adjuntá un archivo.'] })
  const nuevo = mensaje(
    { tipo: sesion.tipo, id: sesion.id, nombre: nombreCompleto(sesion), rol: sesion.rol, avatar: sesion.avatar },
    contenido,
    formulario.get('tipoConsulta') as TipoConsulta,
    new Date().toISOString(),
    adjunto && { nombre: adjunto.name, urlDescarga: URL.createObjectURL(adjunto) },
  )
  mensajesDe(id).push(nuevo)
  return { ...nuevo, esMio: true, visto: false }
}

function marcarLeidos(sesion: Sesion, body: unknown, id: number) {
  exigirQueSeaDelChat(sesion, visible(sesion, id))
  const deLaSolicitud = lecturas.get(id) ?? new Map<string, number>()
  const quien = lector(sesion.tipo, sesion.id)
  deLaSolicitud.set(quien, Math.max(deLaSolicitud.get(quien) ?? 0, (body as { hastaMensajeId: number }).hastaMensajeId))
  lecturas.set(id, deLaSolicitud)
}

/** Las notas de la reunión son del personal: al aspirante no le llegan. */
function reunionPara(sesion: Sesion, id: number) {
  const info = structuredClone(reunionDe(visible(sesion, id).id))
  if (sesion.tipo === 'ASPIRANTE' && info.reunion) info.reunion.notas = null
  return info
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
    'POST /solicitudes/:id/analisis': (body, [id]) => actualizarAnalisis(sesionActual(), body, id),
    'GET /solicitudes/:id/reunion': (_, [id]) => reunionPara(sesionActual(), id),
    'POST /solicitudes/:id/reunion/segunda-ronda': (body, [id]) => proponerSegundaRonda(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/elegir-horario': (body, [id]) => elegirHorario(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/solicitar-otras-fechas': (body, [id]) =>
      solicitarOtrasFechas(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/rechazar-ronda': (body, [id]) => rechazarSegundaRonda(sesionActual(), body, id),
    'PUT /solicitudes/:id/reunion/enlace': (body, [id]) => cargarEnlaceMeet(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/fijar-fecha': (body, [id]) => fijarFecha(sesionActual(), body, id),
    'POST /solicitudes/:id/reunion/no-asistio': (body, [id]) => cerrarReunion(sesionActual(), body, id, false),
    'POST /solicitudes/:id/reunion/finalizar': (body, [id]) => cerrarReunion(sesionActual(), body, id, true),
    'POST /solicitudes/:id/inscripcion/aceptar': (body, [id]) => decidirInscripcion(sesionActual(), body, id, true),
    'POST /solicitudes/:id/inscripcion/rechazar': (body, [id]) => decidirInscripcion(sesionActual(), body, id, false),
    'POST /solicitudes/:id/legalizacion/indicar': (body, [id]) => indicarLegalizacion(sesionActual(), body, id),
    'POST /solicitudes/:id/legalizacion/listo': (body, [id]) => avisarLegalizado(sesionActual(), body, id),
    'POST /solicitudes/:id/legalizacion/aceptar': (body, [id]) => revisar(sesionActual(), body, id, true, true),
    'POST /solicitudes/:id/legalizacion/rechazar': (body, [id]) => revisar(sesionActual(), body, id, false, true),
    'GET /solicitudes/:id/informe': (_, [id]) => informeDe(sesionActual(), id),
    'GET /solicitudes/:id/pago': (_, [id]) => structuredClone(pagoDe(visible(sesionActual(), id).id)),
    'POST /solicitudes/:id/pago/cupon': (body, [id]) => enviarCupon(sesionActual(), body, id),
    'POST /solicitudes/:id/pago/comprobante': (body, [id]) => enviarComprobante(sesionActual(), body, id),
    'POST /solicitudes/:id/pago/aceptar': (body, [id]) => verificarPago(sesionActual(), body, id, true),
    'POST /solicitudes/:id/pago/rechazar': (body, [id]) => verificarPago(sesionActual(), body, id, false),
    'GET /solicitudes/:id/chat/mensajes': (_, [id], consulta) => listarMensajes(sesionActual(), id, consulta),
    'POST /solicitudes/:id/chat/mensajes': (body, [id]) => enviarMensaje(sesionActual(), body, id),
    'POST /solicitudes/:id/chat/leidos': (body, [id]) => marcarLeidos(sesionActual(), body, id),
  }
}
