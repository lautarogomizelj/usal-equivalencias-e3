import { useCarga } from '@/hooks/useCarga'
import { solicitudes } from '@/services/solicitudes'
import { formatearMonto } from '@/utils/montos'

/**
 * Informe de materias (RF-ASP-006, frontend-interfaces.md §4.6.16): lo que informó el Académico al aceptar los
 * documentos legalizados y, cuando Secretaría ya envió el cupón, el monto a pagar. Existe desde `CUPON-SOL`.
 */
export function InformeMaterias({ solicitudId, fondo = 'bg-surface' }: { solicitudId: number; fondo?: string }) {
  const { datos } = useCarga((signal) => solicitudes.informe(solicitudId, signal))

  if (!datos) return null
  return (
    <dl className={`flex flex-wrap gap-x-8 gap-y-2 rounded-md p-4 text-base text-text ${fondo}`}>
      <div>
        <dt className="text-sm text-text-muted">Materias reconocidas</dt>
        <dd className="text-lg font-medium">{datos.materiasReconocidas}</dd>
      </div>
      <div>
        <dt className="text-sm text-text-muted">Monto a pagar</dt>
        <dd className="text-lg font-medium">
          {datos.montoAPagar === null ? 'Lo informa Secretaría con el cupón' : formatearMonto(datos.montoAPagar)}
        </dd>
      </div>
      {datos.observaciones && (
        <div className="min-w-0 flex-1 basis-64">
          <dt className="text-sm text-text-muted">Observaciones del Académico</dt>
          <dd className="whitespace-pre-line">{datos.observaciones}</dd>
        </div>
      )}
    </dl>
  )
}
