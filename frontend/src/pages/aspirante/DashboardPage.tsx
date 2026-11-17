import { CircleCheck, FileText, type LucideIcon } from 'lucide-react'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { TablaSolicitudes } from '@/components/solicitudes/TablaSolicitudes'
import { useSolicitudes } from '@/hooks/useSolicitudes'
import { useAuth } from '@/store/authContext'

interface IndicadorProps {
  Icono: LucideIcon
  cantidad: number | null
  titulo: string
  descripcion: string
  clases: string
}

function Indicador({ Icono, cantidad, titulo, descripcion, clases }: IndicadorProps) {
  return (
    <div className={`flex items-center gap-6 rounded-lg p-4 lg:w-1/3 lg:px-6 lg:py-2 ${clases}`}>
      <span className="flex size-(--size-privado-indicador) shrink-0 items-center justify-center rounded-full bg-surface/60 text-primary">
        <Icono aria-hidden className="size-(--size-icon-xl)" />
      </span>
      <p>
        <span className="block text-5xl leading-none font-medium text-heading">{cantidad ?? '–'}</span>
        <span className="block text-xl font-semibold text-heading">{titulo}</span>
        <span className="block text-base text-text-muted">{descripcion}</span>
      </p>
    </div>
  )
}

/** Panel de inicio del aspirante (frontend-interfaces.md §4.2 y Anexo B, punto 17). */
export function DashboardPage() {
  const { sesion } = useAuth()
  const { pagina, cargando, error, irAPagina, reintentar } = useSolicitudes()
  const finalizadas = pagina ? pagina.content.filter(({ estado }) => estado.esTerminal).length : null
  const activas = pagina && finalizadas !== null ? pagina.totalElements - finalizadas : null

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        titulo={`Bienvenido, ${sesion?.nombre.split(' ')[0]}`}
        descripcion="En esta sección podés gestionar tus solicitudes de equivalencias."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6 lg:flex-row lg:gap-6">
        <Indicador
          Icono={FileText}
          cantidad={activas}
          titulo="Solicitudes activas"
          descripcion="En proceso de evaluación."
          clases="bg-secondary"
        />
        <Indicador
          Icono={CircleCheck}
          cantidad={finalizadas}
          titulo="Solicitudes finalizadas"
          descripcion="Proceso completado."
          clases="bg-surface-muted"
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
