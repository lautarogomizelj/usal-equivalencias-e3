import { Eye, ScrollText } from 'lucide-react'
import { useState } from 'react'
import { Button, Modal, Paginador, Tabla, type ColumnaTabla } from '@/components/ui'
import type { Page } from '@/types/api'
import type { EventoAuditoria } from '@/types/auditoria'
import { textoDeCodigo } from '@/utils/auditoria'
import { formatearFechaHora } from '@/utils/fechas'

interface TablaAuditoriaProps {
  pagina: Page<EventoAuditoria> | null
  cargando: boolean
  error: string | null
  /** Dentro de otra tarjeta: con borde y filas más bajas. */
  interna?: boolean
  /** Sin paginador, para la "Actividad reciente" de los paneles de inicio. */
  sinPaginador?: boolean
  onCambiarPagina: (pagina: number) => void
  onReintentar: () => void
}

/** Los estados se muestran por código, como pide RF-AUDIT-003. */
function resumen({ estadoCodigoAnterior, estadoCodigoNuevo, comentarios, detalles }: EventoAuditoria) {
  if (estadoCodigoNuevo) return `${estadoCodigoAnterior ?? 'Inicio'} → ${estadoCodigoNuevo}`
  if (comentarios) return comentarios
  const datos = Object.entries(detalles ?? {}).filter(([, valor]) => valor !== null)
  return datos.length ? datos.map(([clave, valor]) => `${clave}: ${String(valor)}`).join(' · ') : '–'
}

function Dato({ etiqueta, valor }: { etiqueta: string; valor: string }) {
  return (
    <>
      <dt className="text-base font-medium text-text-muted">{etiqueta}</dt>
      <dd className="col-span-2 text-base wrap-anywhere text-text">{valor}</dd>
    </>
  )
}

/**
 * Eventos de auditoría con su detalle (frontend-interfaces.md §7.15, §7.16, §8.4 y §8.5). La usan la auditoría
 * general, la pestaña "Auditoría" de una solicitud y la actividad reciente de los paneles de inicio.
 */
export function TablaAuditoria({
  pagina,
  cargando,
  error,
  interna = false,
  sinPaginador = false,
  onCambiarPagina,
  onReintentar,
}: TablaAuditoriaProps) {
  const [abierto, setAbierto] = useState<EventoAuditoria | null>(null)

  const columnas: ColumnaTabla<EventoAuditoria>[] = [
    {
      clave: 'fecha',
      titulo: 'Fecha y hora',
      ancho: 'lg:w-3/20',
      render: ({ timestamp }) => <span className="whitespace-nowrap">{formatearFechaHora(timestamp)}</span>,
    },
    {
      clave: 'usuario',
      titulo: 'Usuario',
      ancho: 'lg:w-1/5',
      render: ({ usuario }) => <span className="wrap-anywhere">{usuario.email}</span>,
    },
    { clave: 'accion', titulo: 'Acción', ancho: 'lg:w-3/20', render: ({ accion }) => textoDeCodigo(accion) },
    {
      clave: 'entidad',
      titulo: 'Entidad',
      ancho: 'lg:w-1/10',
      render: ({ entidad, entidadId }) => `${textoDeCodigo(entidad)}${entidadId ? ` #${entidadId}` : ''}`,
    },
    {
      clave: 'detalles',
      titulo: 'Detalles',
      render: (evento) => <span className="line-clamp-2 wrap-anywhere">{resumen(evento)}</span>,
    },
    { clave: 'ip', titulo: 'IP', ancho: 'lg:w-1/10', render: ({ ipOrigen }) => ipOrigen ?? '–' },
    {
      clave: 'ver',
      titulo: 'Detalle',
      ancho: 'lg:w-1/10',
      render: (evento) => (
        <Button variante="outline" aria-label={`Ver el evento ${evento.id}`} onClick={() => setAbierto(evento)}>
          <Eye aria-hidden className="size-(--size-icon-sm)" />
          Ver
        </Button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      <Tabla
        descripcion="Eventos de auditoría"
        columnas={columnas}
        filas={pagina?.content ?? []}
        obtenerClave={({ id }) => id}
        cargando={cargando}
        error={error}
        interna={interna}
        textoVacio="No hay eventos registrados."
        onReintentar={onReintentar}
      />
      {!sinPaginador && pagina && pagina.totalElements > 0 && <Paginador pagina={pagina} onCambiar={onCambiarPagina} />}

      <Modal
        grande
        abierto={Boolean(abierto)}
        titulo={abierto ? `Evento ${abierto.id}` : 'Evento'}
        descripcion="Registro de la auditoría del sistema."
        Icono={ScrollText}
        onCerrar={() => setAbierto(null)}
        acciones={
          <Button variante="outline" onClick={() => setAbierto(null)}>
            Cerrar
          </Button>
        }
      >
        {abierto && (
          <dl className="grid grid-cols-3 gap-x-4 gap-y-2">
            <Dato etiqueta="Fecha y hora" valor={formatearFechaHora(abierto.timestamp)} />
            <Dato etiqueta="Usuario" valor={abierto.usuario.email} />
            <Dato etiqueta="Acción" valor={abierto.accion} />
            <Dato etiqueta="Entidad" valor={`${abierto.entidad}${abierto.entidadId ? ` #${abierto.entidadId}` : ''}`} />
            <Dato etiqueta="Estado anterior" valor={abierto.estadoCodigoAnterior ?? '–'} />
            <Dato etiqueta="Estado nuevo" valor={abierto.estadoCodigoNuevo ?? '–'} />
            <Dato etiqueta="Comentarios" valor={abierto.comentarios ?? '–'} />
            <Dato etiqueta="IP de origen" valor={abierto.ipOrigen ?? '–'} />
            <dt className="text-base font-medium text-text-muted">Detalles</dt>
            <dd className="col-span-2">
              {abierto.detalles ? (
                <pre className="overflow-x-auto rounded-md bg-surface-muted p-3 text-sm text-text">
                  {JSON.stringify(abierto.detalles, null, 2)}
                </pre>
              ) : (
                '–'
              )}
            </dd>
          </dl>
        )}
      </Modal>
    </div>
  )
}
