import type { Carrera } from '@/types/carrera'
import type { Estado } from '@/types/estado'
import type { Perfil } from '@/types/perfil'
import type { Sesion } from '@/types/sesion'
import type { Responsable, SolicitudResumen } from '@/types/solicitud'
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
export const PASSWORD_VALIDA = '1234'

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
  ['Martín', 'López', 'academico2@usal.edu.ar', 'ACAD', true],
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

/** El responsable sale de las cuentas demo, para que al entrar con ellas la bandeja tenga trabajo. */
export function responsable(email: string | null, fecha: string): Responsable | null {
  const cuenta = SESIONES_USAL.find((sesion) => sesion.email === email)
  return cuenta
    ? { id: cuenta.id, nombre: nombreCompleto(cuenta), email: cuenta.email, fechaAsignacion: fecha }
    : null
}

const OTROS_ASPIRANTES = [
  { id: 21, nombre: 'Lucía Fernández' },
  { id: 22, nombre: 'Tomás Díaz' },
]

/**
 * Código de expediente, estado, mail de cada responsable, fechas de creación y de última actualización,
 * carrera destino y aspirante (el demo, salvo que se indique otro). `requiereMiAccion` y `esDeMisCarreras`
 * dependen de quién pregunta: los completa el mock del listado.
 */
const LISTADO: [string, string, string | null, string | null, string, string, number?, number?][] = [
  ['EXP-2026-001', 'DOC-REV', 'academico@usal.edu.ar', 'secretaria@usal.edu.ar', '2026-03-12', '2026-03-20'],
  ['EXP-2026-002', 'DOC-RECH', 'academico@usal.edu.ar', null, '2026-04-05', '2026-04-18'],
  ['EXP-2026-003', 'INSC-ACEP', 'academico2@usal.edu.ar', 'secretaria@usal.edu.ar', '2026-02-10', '2026-02-28'],
  ['EXP-2026-004', 'SOL-INI', null, null, '2026-11-10', '2026-11-10'],
  ['EXP-2026-005', 'DOC-REV', null, null, '2026-11-02', '2026-11-05', 0, 0],
  ['EXP-2026-006', 'DOC-REV', null, null, '2026-10-20', '2026-10-27', 2, 0],
  ['EXP-2026-007', 'SOL-INI', null, null, '2026-11-12', '2026-11-12', 1, 1],
  ['EXP-2026-008', 'ANAL-PEND', 'academico@usal.edu.ar', null, '2026-11-03', '2026-11-16'],
  ['EXP-2026-009', 'REU-FEC-ASP-1', 'academico@usal.edu.ar', null, '2026-10-28', '2026-11-17'],
  ['EXP-2026-010', 'REU-PEND', 'academico@usal.edu.ar', null, '2026-10-15', '2026-11-18'],
  ['EXP-2026-011', 'REU-CHAT', 'academico@usal.edu.ar', null, '2026-10-10', '2026-11-19'],
  ['EXP-2026-012', 'ANAL-POST', 'academico@usal.edu.ar', null, '2026-10-05', '2026-11-20'],
]

export const SOLICITUDES: SolicitudResumen[] = LISTADO.map(
  ([codigoExp, estado, academico, administrativo, creacion, ultimaMod, carrera, otroAspirante], indice) => ({
    id: indice + 1,
    codigoExp,
    aspirante:
      otroAspirante === undefined
        ? { id: SESION_ASPIRANTE.id, nombre: nombreCompleto(SESION_ASPIRANTE) }
        : OTROS_ASPIRANTES[otroAspirante],
    universidadOrigen:
      otroAspirante === undefined ? 'Universidad Nacional de La Plata' : 'Universidad Tecnológica Nacional',
    carreraDestino: CARRERAS[carrera ?? 0],
    estado: ESTADOS.find(({ codigo }) => codigo === estado)!,
    responsableAcademico: responsable(academico, `${creacion}T09:00:00-03:00`),
    responsableAdministrativo: responsable(administrativo, `${creacion}T09:00:00-03:00`),
    fechaCreacion: `${creacion}T09:00:00-03:00`,
    fechaUltimaMod: `${ultimaMod}T15:30:00-03:00`,
    requiereMiAccion: false,
    esDeMisCarreras: false,
  }),
)
