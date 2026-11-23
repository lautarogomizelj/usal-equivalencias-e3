import { useState } from 'react'
import { useLocation, useParams } from 'react-router'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { DetalleSolicitud } from '@/components/solicitudes/DetalleSolicitud'
import { StepperEtapas } from '@/components/solicitudes/StepperEtapas'
import { VISTAS_ASPIRANTE } from '@/components/solicitudes/vistas'
import { Aviso, Button, EstadoBadge } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useCarga } from '@/hooks/useCarga'
import { solicitudes } from '@/services/solicitudes'
import type { SolicitudDetalle } from '@/types/solicitud'
import { MENSAJES } from '@/validations/messages'

const ORIGEN = { texto: 'Mis solicitudes', ruta: ROUTES.MIS_SOLICITUDES }

interface AvisoPagina {
  tipo: 'exito' | 'error'
  mensaje: string
}

const AVISO_CREADA: AvisoPagina = {
  tipo: 'exito',
  mensaje: 'Tu solicitud fue creada. Esperando que alguien tome el proceso.',
}

function Solicitud({ id }: { id: number }) {
  const { state } = useLocation()
  const { datos: detalle, cargando, error, reintentar, setDatos } = useCarga((signal) => solicitudes.obtener(id, signal))
  const [aviso, setAviso] = useState<AvisoPagina | null>(state?.creada ? AVISO_CREADA : null)
  const vista = detalle && VISTAS_ASPIRANTE[detalle.estado.codigo]
  const Vista = vista?.Vista ?? DetalleSolicitud

  /** El aviso va arriba de todo: se sube la página para que se vea junto con la vista nueva. */
  function actualizar(nuevo: SolicitudDetalle, mensaje: string) {
    setAviso({ tipo: 'exito', mensaje })
    setDatos(nuevo)
    window.scrollTo(0, 0)
  }

  function recargarPorCambio() {
    setAviso({ tipo: 'error', mensaje: MENSAJES.ESTADO_CAMBIO })
    reintentar()
    window.scrollTo(0, 0)
  }

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo={vista?.titulo ?? `Solicitud ${detalle?.codigoExp ?? ''}`}
        descripcion={
          vista?.descripcion ?? 'Consultá el estado de tu solicitud y toda la información de tu expediente.'
        }
        migas={{ origen: ORIGEN, actual: detalle?.codigoExp ?? 'Solicitud' }}
      >
        {detalle && !vista && <EstadoBadge estado={detalle.estado} />}
      </EncabezadoPagina>

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        {aviso && <Aviso tipo={aviso.tipo} mensaje={aviso.mensaje} />}
        {error ? (
          <div className="rounded-lg bg-surface p-8 text-center shadow-md">
            <p className="text-xl text-danger">{error}</p>
            <Button variante="outline" className="mt-4" onClick={reintentar}>
              Reintentar
            </Button>
          </div>
        ) : cargando || !detalle ? (
          <div aria-busy className="flex flex-col gap-4">
            <span className="block h-(--size-tabla-fila) animate-pulse rounded-lg bg-surface-muted" />
            <span className="block h-(--size-privado-foto-alto) animate-pulse rounded-lg bg-surface-muted" />
          </div>
        ) : (
          <>
            <StepperEtapas codigoEstado={detalle.estado.codigo} />
            {/* Al cambiar el estado la vista arranca de nuevo: abre la pestaña que le corresponde al estado nuevo. */}
            <Vista
              key={detalle.estado.codigo}
              detalle={detalle}
              onActualizada={actualizar}
              onEstadoCambio={recargarPorCambio}
            />
          </>
        )}
      </div>
    </main>
  )
}

/** Detalle de una solicitud del aspirante: la interfaz depende del estado (frontend-interfaces.md §4.5). */
export function SolicitudPage() {
  const { id } = useParams()
  return <Solicitud key={id} id={Number(id)} />
}
