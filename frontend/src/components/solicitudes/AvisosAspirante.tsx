import { Info } from 'lucide-react'
import { useCarga } from '@/hooks/useCarga'
import { solicitudes } from '@/services/solicitudes'
import { formatearFecha } from '@/utils/fechas'
import { ResponsableAcademico } from './ResponsableAcademico'
import type { VistaSolicitudProps } from './vistas'

/**
 * Coordinación de la fecha por chat, `ASP-REU-CHAT` (frontend-interfaces.md §4.6.8): va dentro del banner. El
 * aspirante no cambia el estado: la fecha la fija su responsable académico.
 */
export function CoordinacionPorChat({ detalle }: VistaSolicitudProps) {
  return (
    <div className="mt-3 flex flex-wrap items-stretch gap-4">
      {detalle.responsableAcademico && <ResponsableAcademico responsable={detalle.responsableAcademico} />}
      <p className="flex grow basis-64 items-center gap-3 rounded-md bg-surface p-3 text-base text-text">
        <Info aria-hidden className="size-(--size-icon-lg) shrink-0 text-alert" />
        Usá el chat de abajo para proponer días y horarios. Cuando acuerden la fecha, tu responsable la fija y el
        estado de la solicitud se actualiza.
      </p>
    </div>
  )
}

/** En `ASP-REU-FEC-ACAD` (§4.6.7): aclara cuando las fechas se vuelven a pedir porque faltó a la reunión. */
export function AvisoInasistencia({ detalle }: VistaSolicitudProps) {
  const { datos } = useCarga((signal) => solicitudes.reunion(detalle.id, signal))

  if (datos?.reunion?.resultado !== 'NO_ASISTIO') return null
  return (
    <p>
      No registramos tu asistencia a la reunión del {formatearFecha(datos.reunion.inicio)}: por eso se vuelve a
      coordinar la fecha.
    </p>
  )
}

/** Cierre del trámite, `ASP-INSC-RECH` (§4.6.13): el motivo, quién resolvió y cuándo. */
export function MotivoDelRechazo({ detalle }: VistaSolicitudProps) {
  const observacion = detalle.ultimaObservacion

  if (!observacion) return null
  return (
    <p className="mt-3 rounded-md bg-surface p-3 text-base text-text">
      <strong>Motivo:</strong> {observacion.comentario}
      <span className="block text-sm text-text-muted">
        Resolvió {observacion.usuario} el {formatearFecha(observacion.fecha)}.
      </span>
    </p>
  )
}
