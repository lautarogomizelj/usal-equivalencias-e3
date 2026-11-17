import { Eye } from 'lucide-react'
import { generatePath, useNavigate } from 'react-router'
import { Button, EstadoBadge, Paginador, Tabla, type ColumnaTabla } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import type { Page } from '@/types/api'
import type { SolicitudResumen } from '@/types/solicitud'
import { formatearFecha } from '@/utils/fechas'

interface TablaSolicitudesProps {
  pagina: Page<SolicitudResumen> | null
  cargando: boolean
  error: string | null
  onCambiarPagina: (pagina: number) => void
  onReintentar: () => void
}

const SIN_RESPONSABLE = 'Sin asignar'

/** Listado de solicitudes del aspirante (frontend-interfaces.md §4.4 y Anexo B, punto 15). */
export function TablaSolicitudes({ pagina, cargando, error, onCambiarPagina, onReintentar }: TablaSolicitudesProps) {
  const navigate = useNavigate()
  const filas = pagina?.content ?? []

  const columnas: ColumnaTabla<SolicitudResumen>[] = [
    {
      clave: 'codigo',
      titulo: 'N° solicitud',
      ancho: 'lg:w-3/25',
      render: ({ codigoExp }) => <span className="whitespace-nowrap text-text">{codigoExp}</span>,
    },
    {
      clave: 'creacion',
      titulo: 'Fecha de creación',
      ancho: 'lg:w-11/100',
      render: ({ fechaCreacion }) => formatearFecha(fechaCreacion),
    },
    {
      clave: 'estado',
      titulo: 'Estado actual',
      ancho: 'lg:w-17/100',
      render: ({ estado }) => <EstadoBadge estado={estado} />,
    },
    {
      clave: 'academico',
      titulo: 'Responsable académico',
      ancho: 'lg:w-3/20',
      render: ({ responsableAcademico }) => responsableAcademico?.nombre ?? SIN_RESPONSABLE,
    },
    {
      clave: 'administrativo',
      titulo: 'Responsable administrativo',
      ancho: 'lg:w-4/25',
      render: ({ responsableAdministrativo }) => responsableAdministrativo?.nombre ?? SIN_RESPONSABLE,
    },
    {
      clave: 'ultimaMod',
      titulo: 'Última actualización',
      ancho: 'lg:w-13/100',
      render: ({ fechaUltimaMod }) => formatearFecha(fechaUltimaMod),
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      render: ({ id }) => (
        <Button
          variante="outline"
          tamanio="lg"
          className="font-semibold"
          onClick={() => navigate(generatePath(ROUTES.SOLICITUD, { id: String(id) }))}
        >
          <Eye aria-hidden className="size-(--size-icon-md)" />
          Ver solicitud
        </Button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      {!cargando && !error && filas.length === 0 ? (
        <div className="rounded-lg bg-surface p-8 text-center shadow-md">
          <p className="text-xl text-text-muted">Todavía no iniciaste ninguna solicitud.</p>
          <Button tamanio="lg" className="mt-4" onClick={() => navigate(ROUTES.NUEVA_SOLICITUD)}>
            Nueva solicitud
          </Button>
        </div>
      ) : (
        <Tabla
          descripcion="Mis solicitudes de equivalencias"
          columnas={columnas}
          filas={filas}
          obtenerClave={({ id }) => id}
          cargando={cargando}
          error={error}
          onReintentar={onReintentar}
        />
      )}
      {pagina && pagina.totalPages > 1 && <Paginador pagina={pagina} onCambiar={onCambiarPagina} />}
    </div>
  )
}
