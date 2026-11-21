import { CircleAlert, Clock, FileText, UserPlus } from 'lucide-react'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Indicador } from '@/components/privado/Indicador'
import { TablaBandeja } from '@/components/solicitudes/TablaBandeja'
import { filtrosDeVista, rutaBandeja } from '@/constants/bandeja'
import { useCarga } from '@/hooks/useCarga'
import { useSolicitudes } from '@/hooks/useSolicitudes'
import { solicitudes } from '@/services/solicitudes'
import { useAuth } from '@/store/authContext'

/** Vistas de la bandeja que se cuentan en los indicadores, además de "Requieren tu acción". */
const CONTADAS = ['activas', 'sin-responsable', 'observaciones']

/** Panel de inicio del Académico y de Secretaría (frontend-interfaces.md §5.1, §6.1 y Anexo B, punto 20). */
export function InicioPersonalPage() {
  const { sesion } = useAuth()
  const rol = sesion?.rol ?? ''
  const enAccion = useSolicitudes(filtrosDeVista('accion', rol))
  const cantidades = useCarga((signal) =>
    Promise.all(
      CONTADAS.map((vista) =>
        solicitudes.listar(0, { ...filtrosDeVista(vista, rol), size: 1 }, signal).then(({ totalElements }) => totalElements),
      ),
    ),
  )
  const [activas, sinResponsable, conObservaciones] = cantidades.datos ?? [null, null, null]

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo={`Bienvenido, ${sesion?.nombre}`}
        descripcion="En esta sección podés gestionar las solicitudes de equivalencia asignadas a tu área."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6 lg:flex-row">
        <Indicador
          Icono={FileText}
          cantidad={activas}
          titulo="Solicitudes activas"
          descripcion="En proceso de evaluación."
          clases="bg-secondary lg:flex-1"
          ruta={rutaBandeja('activas')}
        />
        <Indicador
          Icono={Clock}
          cantidad={enAccion.pagina?.totalElements ?? null}
          titulo="Requieren tu acción"
          descripcion="Pendientes de tu revisión."
          clases="bg-warning-soft lg:flex-1"
          ruta={rutaBandeja('accion')}
        />
        <Indicador
          Icono={UserPlus}
          cantidad={sinResponsable}
          titulo="Sin responsable"
          descripcion="Podés tomar el proceso."
          clases="bg-surface-muted lg:flex-1"
          ruta={rutaBandeja('sin-responsable')}
        />
        <Indicador
          Icono={CircleAlert}
          cantidad={conObservaciones}
          titulo="Con observaciones"
          descripcion="Esperan al aspirante."
          clases="bg-danger-soft lg:flex-1"
          ruta={rutaBandeja('observaciones')}
        />
      </div>

      <section className="mt-8 lg:mt-6">
        <h2 className="text-3xl lg:text-4xl">Solicitudes que requieren tu acción</h2>
        <div className="mt-4">
          <TablaBandeja
            pagina={enAccion.pagina}
            cargando={enAccion.cargando}
            error={enAccion.error}
            rol={rol}
            textoVacio="No tenés solicitudes que requieran tu acción."
            onCambiarPagina={enAccion.irAPagina}
            onReintentar={enAccion.reintentar}
          />
        </div>
      </section>
    </main>
  )
}
