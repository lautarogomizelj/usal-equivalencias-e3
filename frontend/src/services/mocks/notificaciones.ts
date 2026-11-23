/**
 * Mocks de la campana (contrato de la API §8.2). Cada cuenta demo arranca con avisos de ejemplo; con mocks no se
 * suman avisos nuevos al cambiar el estado de una solicitud.
 */
import { ApiError } from '@/services/api/ApiError'
import type { Notificacion } from '@/types/notificacion'
import type { Sesion } from '@/types/sesion'
import { SESION_ASPIRANTE } from './datos'

/** Email del destinatario, solicitud, título, mensaje, fecha y si ya fue leída. */
const AVISOS: [string, number, string, string, string, boolean?][] = [
  [SESION_ASPIRANTE.email, 15, 'E3 EXP-2026-015: tu cupón de pago está listo', 'Tu cupón está listo. Descargalo desde tu solicitud, hacé el pago y cargá el comprobante.', '2026-11-21', true],
  [SESION_ASPIRANTE.email, 9, 'E3 EXP-2026-009: elegí un horario para la reunión', 'Elegí un horario para la reunión. La matriz de equivalencias y el posible horario de cursada están en tu solicitud.', '2026-11-17'],
  [SESION_ASPIRANTE.email, 2, 'E3 EXP-2026-002: observaciones en tu documentación', 'Observaciones en tu documentación. Motivo: El programa de Matemática I no está firmado.', '2026-04-18'],
  ['academico@usal.edu.ar', 12, 'E3 EXP-2026-012: la reunión se realizó', 'Falta definir la inscripción del aspirante. ID: EXP-2026-012', '2026-11-20'],
  ['academico@usal.edu.ar', 13, 'E3 EXP-2026-013: documentos legalizados para analizar', 'El aspirante indicó que sus documentos están legalizados. ID: EXP-2026-013', '2026-11-21'],
  ['academico@usal.edu.ar', 1, 'E3 EXP-2026-001: documentación pendiente de revisión', 'Nueva solicitud de equivalencias con documentación pendiente de revisión. ID: EXP-2026-001', '2026-03-20', true],
  ['secretaria@usal.edu.ar', 14, 'E3 EXP-2026-014: solicitud lista para gestión de pago', 'Solicitud lista para gestión de pago. Generá y enviá el cupón. ID: EXP-2026-014', '2026-11-21'],
  ['secretaria@usal.edu.ar', 15, 'E3 EXP-2026-015: comprobante de pago para verificar', 'El aspirante cargó el comprobante de pago. ID: EXP-2026-015', '2026-11-22'],
  ['admin@usal.edu.ar', 5, 'E3 EXP-2026-005: proceso sin responsable académico', 'Proceso EXP-2026-005 sin responsable académico. Asigná a alguien desde la solicitud.', '2026-11-05'],
]

const avisos = AVISOS.map(([email, solicitudId, titulo, mensaje, dia, leida = false], indice) => ({
  email,
  aviso: { id: 1200 + indice, titulo, mensaje, solicitudId, leida, fechaCreacion: `${dia}T15:30:00-03:00` } as Notificacion,
}))

type Manejador = (body: unknown, ids: number[], consulta: URLSearchParams) => unknown

export function rutasNotificaciones(sesionActual: () => Sesion): Record<string, Manejador> {
  const mias = () => avisos.filter(({ email }) => email === sesionActual().email).map(({ aviso }) => aviso)
  return {
    'GET /notificaciones': (_, __, consulta) => {
      const size = Number(consulta.get('size') ?? 10)
      const todas = mias()
      return {
        content: todas.slice(0, size).map((aviso) => ({ ...aviso })),
        page: 0,
        size,
        totalElements: todas.length,
        totalPages: Math.ceil(todas.length / size),
      }
    },
    'GET /notificaciones/no-leidas/cantidad': () => ({ cantidad: mias().filter(({ leida }) => !leida).length }),
    'PATCH /notificaciones/:id': (_, [id]) => {
      const aviso = mias().find((item) => item.id === id)
      if (!aviso) throw ApiError.fromStatus(404, { status: 404, code: 'NO_ENCONTRADO' })
      aviso.leida = true
    },
    'POST /notificaciones/marcar-todas-leidas': () => mias().forEach((aviso) => (aviso.leida = true)),
  }
}
