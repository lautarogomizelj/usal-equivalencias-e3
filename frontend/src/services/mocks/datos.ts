import type { Carrera } from '@/types/carrera'
import type { Estado } from '@/types/estado'
import type { Perfil } from '@/types/perfil'
import type { Sesion } from '@/types/sesion'
import type { SolicitudResumen } from '@/types/solicitud'
import { nombreCompleto } from '@/utils/personas'

export const CARRERAS: Carrera[] = [
  { id: 1, codigo: 'ING-INF-001', nombre: 'Ingeniería en Informática', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
  { id: 2, codigo: 'ING-IND-001', nombre: 'Ingeniería Industrial', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
  { id: 3, codigo: 'ING-CIV-001', nombre: 'Ingeniería Civil', facultad: 'Facultad de Ingeniería', descripcion: '', activo: true },
]

/** Código, nombre y si es terminal: mismos datos que la migración V2__catalogos.sql. */
const CATALOGO: [codigo: string, nombre: string, esTerminal?: boolean][] = [
  ['SOL-INI', 'Solicitud iniciada'],
  ['DOC-REV', 'Documentación pendiente de revisión'],
  ['DOC-RECH', 'Documentación rechazada'],
  ['ANAL-PEND', 'Análisis preliminar pendiente'],
  ['REU-FEC-ASP-1', 'Reunión pendiente de fechas aspirante (1ª ronda)'],
  ['REU-FEC-ASP-2', 'Reunión pendiente de fechas aspirante (2ª ronda)'],
  ['REU-FEC-ACAD', 'Reunión pendiente de fechas académico'],
  ['REU-CHAT', 'Resolución fecha por chat'],
  ['REU-PEND', 'Reunión pendiente de realización'],
  ['ANAL-POST', 'Análisis pos reunión'],
  ['INSC-ACEP', 'Inscripción aceptada'],
  ['LEG-PEND', 'Legalización pendiente'],
  ['INSC-RECH', 'Inscripción rechazada', true],
  ['LEG-REV', 'Análisis de documentos legalizados'],
  ['LEG-RECH', 'Documentos legalizados rechazados'],
  ['CUPON-SOL', 'Solicitud de cupón de pago'],
  ['CUPON-PAG', 'Pago de cupón por aspirante'],
  ['PAG-CTRL', 'Control de pago'],
  ['PAG-REENV', 'Reenvío de comprobante'],
  ['E3-INI', 'Inicio de E3'],
  ['SOL-CANC', 'Solicitud cancelada', true],
]

export const ESTADOS: Estado[] = CATALOGO.map(([codigo, nombre, esTerminal = false]) => ({
  codigo,
  nombre,
  descripcion: '',
  esTerminal,
  activo: true,
}))

/** Contraseña de todas las cuentas de los mocks. */
export const PASSWORD_VALIDA = 'Demo1234!'

export const SESION_ASPIRANTE: Sesion = {
  tipo: 'ASPIRANTE',
  id: 15,
  nombre: 'Juan',
  apellido: 'Pérez',
  email: 'aspirante@example.com',
  rol: 'ASPIRANTE',
  avatar: null,
  emailValidado: true,
  esOAuth: false,
  expiraEn: '2026-12-31T23:59:59-03:00',
}

/** Cuentas @usal de `Docs/demo-credentials.md`: nombre, email, rol y si tiene permiso sobre la carrera demo. */
const CUENTAS_USAL: [nombre: string, apellido: string, email: string, rol: string, conCarrera?: boolean][] = [
  ['María', 'Gómez', 'academico@usal.edu.ar', 'ACAD', true],
  ['Carolina', 'Ruiz', 'secretaria@usal.edu.ar', 'SECRE', true],
  ['Pedro', 'Sosa', 'revisor@usal.edu.ar', 'REVISOR'],
  ['Laura', 'Díaz', 'observador@usal.edu.ar', 'ACAD'],
  ['Admin', 'General', 'admin@usal.edu.ar', 'ADMIN'],
]

/** `carrerasConPermiso` solo viene para Académico y Secretaría (contrato de la API §4.1). */
export const SESIONES_USAL: Sesion[] = CUENTAS_USAL.map(([nombre, apellido, email, rol, conCarrera], indice) => ({
  tipo: 'USUARIO',
  id: indice + 1,
  nombre,
  apellido,
  email,
  rol,
  avatar: null,
  emailValidado: true,
  esOAuth: rol !== 'ADMIN',
  ...(rol === 'ACAD' || rol === 'SECRE'
    ? { carrerasConPermiso: conCarrera ? [{ id: CARRERAS[0].id, codigo: CARRERAS[0].codigo, nombre: CARRERAS[0].nombre }] : [] }
    : {}),
  expiraEn: SESION_ASPIRANTE.expiraEn,
}))

export const PERFIL: Perfil = {
  nombre: SESION_ASPIRANTE.nombre,
  apellido: SESION_ASPIRANTE.apellido,
  email: SESION_ASPIRANTE.email,
  telefono: '+54 11 2345-6789',
  dni: '40123456',
  avatar: null,
  rol: SESION_ASPIRANTE.rol,
  esOAuth: false,
  camposEditables: ['nombre', 'apellido', 'telefono', 'avatar'],
}

/** Código de expediente, estado, responsables y fechas de creación y de última actualización. */
const LISTADO: [string, string, string | null, string | null, string, string][] = [
  ['EXP-2026-001', 'DOC-REV', 'Dr. Martín López', 'Lic. Carolina Ruiz', '2026-03-12', '2026-03-20'],
  ['EXP-2026-002', 'DOC-RECH', 'Dra. Ana Fernández', 'Sr. Diego Morales', '2026-04-05', '2026-04-18'],
  ['EXP-2026-003', 'INSC-ACEP', 'Dr. Pablo Gómez', 'Lic. María Torres', '2026-02-10', '2026-02-28'],
  ['EXP-2026-004', 'SOL-INI', null, null, '2026-11-10', '2026-11-10'],
]

export const SOLICITUDES: SolicitudResumen[] = LISTADO.map(
  ([codigoExp, estado, academico, administrativo, creacion, ultimaMod], indice) => ({
    id: indice + 1,
    codigoExp,
    aspirante: { id: SESION_ASPIRANTE.id, nombre: nombreCompleto(SESION_ASPIRANTE) },
    universidadOrigen: 'Universidad Nacional de La Plata',
    carreraDestino: CARRERAS[0],
    estado: ESTADOS.find(({ codigo }) => codigo === estado)!,
    responsableAcademico: academico
      ? { id: 7, nombre: academico, email: 'academico@usal.edu.ar', fechaAsignacion: `${creacion}T09:00:00-03:00` }
      : null,
    responsableAdministrativo: administrativo
      ? { id: 8, nombre: administrativo, email: 'secretaria@usal.edu.ar', fechaAsignacion: `${creacion}T09:00:00-03:00` }
      : null,
    fechaCreacion: `${creacion}T09:00:00-03:00`,
    fechaUltimaMod: `${ultimaMod}T15:30:00-03:00`,
    requiereMiAccion: estado !== 'DOC-REV',
    esDeMisCarreras: false,
  }),
)
