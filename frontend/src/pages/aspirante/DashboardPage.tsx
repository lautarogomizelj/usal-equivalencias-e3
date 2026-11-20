import { CircleCheck, FileText } from 'lucide-react'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Indicador } from '@/components/privado/Indicador'
import { TablaSolicitudes } from '@/components/solicitudes/TablaSolicitudes'
import { useSolicitudes } from '@/hooks/useSolicitudes'
import { useAuth } from '@/store/authContext'

/** Panel de inicio del aspirante (frontend-interfaces.md §4.2 y Anexo B, punto 17). */
export function DashboardPage() {
  const { sesion } = useAuth()
  const { pagina, cargando, error, irAPagina, reintentar } = useSolicitudes()
  const finalizadas = pagina ? pagina.content.filter(({ estado }) => estado.esTerminal).length : null
  const activas = pagina && finalizadas !== null ? pagina.totalElements - finalizadas : null

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        titulo={`Bienvenido, ${sesion?.nombre}`}
        descripcion="En esta sección podés gestionar tus solicitudes de equivalencias."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6 lg:flex-row lg:gap-6">
        <Indicador
          Icono={FileText}
          cantidad={activas}
          titulo="Solicitudes activas"
          descripcion="En proceso de evaluación."
          clases="bg-secondary lg:w-1/3"
        />
        <Indicador
          Icono={CircleCheck}
          cantidad={finalizadas}
          titulo="Solicitudes finalizadas"
          descripcion="Proceso completado."
          clases="bg-surface-muted lg:w-1/3"
        />
      </div>

      <section className="mt-8 lg:mt-6">
        <h2 className="text-3xl lg:text-4xl">Mis solicitudes</h2>
        <div className="mt-4">
          <TablaSolicitudes
            pagina={pagina}
            cargando={cargando}
            error={error}
            onCambiarPagina={irAPagina}
            onReintentar={reintentar}
          />
        </div>
      </section>
    </main>
  )
}
