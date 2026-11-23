import { Eye } from 'lucide-react'
import { generatePath, useNavigate } from 'react-router'
import { Button, EstadoBadge, Paginador, Tabla, type ColumnaTabla } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import type { Page } from '@/types/api'
import type { SolicitudResumen } from '@/types/solicitud'
import { formatearFecha } from '@/utils/fechas'

interface TablaBandejaProps {
  pagina: Page<SolicitudResumen> | null
  cargando: boolean
  error: string | null
  /** Código de rol de quien mira: define qué responsable muestra la columna. */
  rol: string
  textoVacio: string
  /** Sin paginador, para mostrar solo las primeras en un panel de inicio. */
  sinPaginador?: boolean
  onCambiarPagina: (pagina: number) => void
  onReintentar: () => void
}

/**
 * Bandeja de solicitudes del Académico y de Secretaría (frontend-interfaces.md §5.2 y Anexo B, punto 20).
 * El orden lo resuelve el backend: primero las carreras con permiso y después el resto, alfabético.
 */
export function TablaBandeja({
  pagina,
  cargando,
  error,
  rol,
  textoVacio,
  sinPaginador = false,
  onCambiarPagina,
  onReintentar,
}: TablaBandejaProps) {
  const navigate = useNavigate()

  const columnas: ColumnaTabla<SolicitudResumen>[] = [
    {
      clave: 'codigo',
      titulo: 'N° solicitud',
      ancho: 'lg:w-3/25',
      render: ({ codigoExp }) => <span className="whitespace-nowrap text-text">{codigoExp}</span>,
    },
    {
      clave: 'aspirante',
      titulo: 'Aspirante',
      ancho: 'lg:w-19/100',
      render: ({ aspirante, universidadOrigen }) => (
        <span>
          <span className="block font-medium text-primary">{aspirante.nombre}</span>
          <span className="block">{universidadOrigen}</span>
        </span>
      ),
    },
    {
      clave: 'carrera',
      titulo: 'Carrera destino',
      ancho: 'lg:w-3/20',
      render: ({ carreraDestino }) => carreraDestino.nombre,
    },
    {
      clave: 'estado',
      titulo: 'Estado actual',
      ancho: 'lg:w-17/100',
      render: ({ estado }) => <EstadoBadge estado={estado} />,
    },
    {
      clave: 'responsable',
      titulo: 'Responsable',
      ancho: 'lg:w-11/100',
      render: ({ responsableAcademico, responsableAdministrativo }) =>
        (rol === 'SECRE' ? responsableAdministrativo : responsableAcademico)?.nombre ?? 'Sin asignar',
    },
    {
      clave: 'ultimaMod',
      titulo: 'Última actualización',
      ancho: 'lg:w-1/10',
      render: ({ fechaUltimaMod }) => formatearFecha(fechaUltimaMod),
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      render: ({ id, requiereMiAccion }) => (
        <span className="inline-flex items-center gap-3">
          <Button
            variante="outline"
            tamanio="lg"
            className="font-semibold whitespace-nowrap"
            onClick={() => navigate(generatePath(ROUTES.SOLICITUD, { id: String(id) }))}
          >
            <Eye aria-hidden className="size-(--size-icon-md)" />
            Ver solicitud
          </Button>
          {requiereMiAccion && (
            <span
              role="img"
              aria-label="Requiere tu acción"
              title="Requiere tu acción"
              className="size-2 shrink-0 rounded-full bg-danger"
            />
          )}
        </span>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      <Tabla
        descripcion="Solicitudes de equivalencia"
        columnas={columnas}
        filas={pagina?.content ?? []}
        obtenerClave={({ id }) => id}
        cargando={cargando}
        error={error}
        textoVacio={textoVacio}
        onReintentar={onReintentar}
      />
      {!sinPaginador && pagina && pagina.totalElements > 0 && <Paginador pagina={pagina} onCambiar={onCambiarPagina} />}
    </div>
  )
}
