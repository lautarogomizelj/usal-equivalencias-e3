import { Link } from 'react-router'
import { TablaAuditoria } from '@/components/auditoria/TablaAuditoria'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { TablaBandeja } from '@/components/solicitudes/TablaBandeja'
import { ROUTES } from '@/constants/routes'
import { usePagina } from '@/hooks/usePagina'
import { useSolicitudes } from '@/hooks/useSolicitudes'
import { auditoria } from '@/services/auditoria'
import { useAuth } from '@/store/authContext'

const FILAS = 5
const EVENTOS_RECIENTES = 10

/** Panel de inicio del Revisor de logs (frontend-interfaces.md §8.1): todo en modo lectura. */
export function InicioRevisorPage() {
  const { sesion } = useAuth()
  const recientes = useSolicitudes({ size: FILAS })
  const actividad = usePagina((page, signal) => auditoria.listar(page, { size: EVENTOS_RECIENTES }, signal))

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo={`Bienvenido, ${sesion?.nombre}`}
        descripcion="Consultá las solicitudes y la actividad del sistema. Tu acceso es de solo lectura."
      />

      <div className="relative mt-8 flex flex-col gap-8 lg:mt-6 lg:gap-6">
        <section>
          <div className="flex flex-wrap items-baseline justify-between gap-3">
            <h2 className="text-3xl lg:text-4xl">Solicitudes</h2>
            <Link to={ROUTES.MIS_SOLICITUDES} className="text-base text-primary underline">
              Ver todas las solicitudes
            </Link>
          </div>
          <div className="mt-4">
            <TablaBandeja
              sinPaginador
              pagina={recientes.pagina}
              cargando={recientes.cargando}
              error={recientes.error}
              rol={sesion?.rol ?? ''}
              textoVacio="Todavía no hay solicitudes."
              onCambiarPagina={recientes.irAPagina}
              onReintentar={recientes.reintentar}
            />
          </div>
        </section>

        <section>
          <div className="flex flex-wrap items-baseline justify-between gap-3">
            <h2 className="text-3xl lg:text-4xl">Actividad del sistema</h2>
            <Link to={ROUTES.AUDITORIA} className="text-base text-primary underline">
              Ver toda la auditoría
            </Link>
          </div>
          <div className="mt-4">
            <TablaAuditoria
              sinPaginador
              pagina={actividad.pagina}
              cargando={actividad.cargando}
              error={actividad.error}
              onCambiarPagina={actividad.irAPagina}
              onReintentar={actividad.reintentar}
            />
          </div>
        </section>
      </div>
    </main>
  )
}
