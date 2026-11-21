import { Video } from 'lucide-react'
import type { ReactNode } from 'react'
import { Button } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { solicitudes } from '@/services/solicitudes'
import { formatearDiaLargo, formatearFranja } from '@/utils/fechas'
import type { VistaSolicitudProps } from './vistas'

function Dato({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
  return (
    <div>
      <dt className="inline font-medium">{etiqueta}: </dt>
      <dd className="inline">{children}</dd>
    </div>
  )
}

/**
 * Reunión pactada, `ASP-REU-PEND` (frontend-interfaces.md §4.6.9): va dentro del banner. El botón para unirse
 * aparece cuando el Académico cargó el enlace de Meet.
 */
export function ReunionPactada({ detalle }: VistaSolicitudProps) {
  const { datos, cargando, error, reintentar } = useCarga((signal) => solicitudes.reunion(detalle.id, signal))
  const reunion = datos?.reunion

  if (error) {
    return (
      <p role="alert" className="mt-3 flex items-center gap-3 text-base text-danger">
        {error}
        <Button variante="outline" tamanio="sm" onClick={reintentar}>
          Reintentar
        </Button>
      </p>
    )
  }
  if (cargando || !reunion) {
    return <span aria-busy className="mt-3 block h-(--size-tabla-fila) animate-pulse rounded-md bg-surface" />
  }

  return (
    <div className="mt-3 flex flex-col gap-4 rounded-md bg-surface p-4 lg:flex-row lg:items-center">
      <dl className="flex-1 text-base text-text">
        <Dato etiqueta="Fecha">{formatearDiaLargo(reunion.inicio)}</Dato>
        <Dato etiqueta="Horario">{formatearFranja(reunion.inicio, reunion.fin)} (hora de Argentina)</Dato>
        <Dato etiqueta="Modalidad">Google Meet</Dato>
        {detalle.responsableAcademico && (
          <Dato etiqueta="Responsable académico">{detalle.responsableAcademico.nombre}</Dato>
        )}
      </dl>
      {reunion.enlaceMeet ? (
        <a
          href={reunion.enlaceMeet}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-(--size-button-lg) items-center justify-center gap-2 rounded-md bg-primary px-6 text-base font-medium text-primary-contrast transition-colors hover:bg-primary-hover"
        >
          <Video aria-hidden className="size-(--size-icon-md)" />
          Unirme a la reunión
        </a>
      ) : (
        <p className="text-base text-text-muted lg:w-1/3">
          Cuando tu responsable académico cargue el enlace de la reunión te avisamos por mail y vas a poder unirte
          desde acá.
        </p>
      )}
    </div>
  )
}
