import { Download, GraduationCap, Paperclip } from 'lucide-react'
import { Seccion, Tabla, type ColumnaTabla } from '@/components/ui'
import { NOMBRE_ANALISIS } from '@/constants/documentos'
import { useCarga } from '@/hooks/useCarga'
import { solicitudes } from '@/services/solicitudes'
import type { Analisis } from '@/types/solicitud'
import { formatearFecha } from '@/utils/fechas'

const COLUMNAS: ColumnaTabla<Analisis>[] = [
  {
    clave: 'documento',
    titulo: 'Documento',
    ancho: 'lg:w-1/5',
    render: ({ tipo }) => <span className="text-text">{NOMBRE_ANALISIS[tipo]}</span>,
  },
  {
    clave: 'envio',
    titulo: 'Fecha de envío',
    ancho: 'lg:w-1/8',
    render: ({ fechaCarga }) => formatearFecha(fechaCarga),
  },
  { clave: 'responsable', titulo: 'Responsable', ancho: 'lg:w-1/6', render: ({ subidoPor }) => subidoPor },
  {
    clave: 'archivo',
    titulo: 'Archivo',
    ancho: 'lg:w-1/5',
    render: ({ nombreArchivo, version }) =>
      nombreArchivo ? (
        <span className="inline-flex items-center gap-2">
          <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
          {nombreArchivo}
          {version > 1 && ` (v${version})`}
        </span>
      ) : (
        'Sin archivo'
      ),
  },
  {
    clave: 'notas',
    titulo: 'Notas',
    apilada: true,
    render: ({ notas }) => (notas ? <span className="block py-2 whitespace-pre-line">{notas}</span> : '–'),
  },
  {
    clave: 'acciones',
    titulo: 'Acciones',
    ancho: 'lg:w-1/12',
    render: ({ urlDescarga, nombreArchivo }) =>
      urlDescarga && (
        <a
          href={solicitudes.urlArchivo(urlDescarga)}
          target="_blank"
          rel="noreferrer"
          aria-label={`Descargar ${nombreArchivo}`}
          className="inline-flex size-(--size-button-md) items-center justify-center rounded-md text-primary hover:bg-secondary"
        >
          <Download aria-hidden className="size-(--size-icon-md)" />
        </a>
      ),
  },
]

interface DocumentacionAcademicaProps {
  solicitudId: number
  /** No ocupa lugar hasta que el Académico cargó algo: para la vista del personal. */
  soloSiHay?: boolean
}

/**
 * Matriz de equivalencias y posible horario de cursada que cargó el Académico (RF-ACAD-002), en solo lectura.
 * La API entrega solo la versión vigente de cada uno.
 */
export function DocumentacionAcademica({ solicitudId, soloSiHay = false }: DocumentacionAcademicaProps) {
  const { datos, cargando, error, reintentar } = useCarga((signal) => solicitudes.analisis(solicitudId, signal))

  if (soloSiHay && !error && !datos?.length) return null

  return (
    <Seccion
      Icono={GraduationCap}
      titulo="Documentación académica"
      descripcion="Documentos generados por el área académica para el análisis de la equivalencia."
    >
      <Tabla
        interna
        descripcion="Documentación académica de la solicitud"
        columnas={COLUMNAS}
        filas={datos ?? []}
        obtenerClave={({ id }) => id}
        cargando={cargando}
        error={error}
        textoVacio="El área académica todavía no cargó la matriz de equivalencias ni el horario de cursada."
        onReintentar={reintentar}
      />
    </Seccion>
  )
}
