import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { TablaSolicitudes } from '@/components/solicitudes/TablaSolicitudes'
import { useSolicitudes } from '@/hooks/useSolicitudes'

/** Mis solicitudes del aspirante (frontend-interfaces.md §4.4 y Anexo B, punto 15). */
export function MisSolicitudesPage() {
  const { pagina, cargando, error, irAPagina, reintentar } = useSolicitudes()

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        titulo="Mis solicitudes"
        descripcion="En esta sección podés gestionar tus solicitudes de equivalencias."
      />

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
