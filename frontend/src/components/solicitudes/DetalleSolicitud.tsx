import { Download, FileText, History, MessageCircle, Paperclip, User, Users } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { Badge, Banner, EstadoBadge, Pestanias, Seccion, Tabla, type ColumnaTabla, type Pestania } from '@/components/ui'
import { MARCA_REVISION, NOMBRE_TIPO_DOCUMENTO } from '@/constants/documentos'
import { BANNER_ASPIRANTE, VARIANTE_POR_ESTADO } from '@/constants/estados'
import { useCarga } from '@/hooks/useCarga'
import { catalogos } from '@/services/catalogos'
import { solicitudes } from '@/services/solicitudes'
import type { Documento, EventoHistorial, Responsable, SolicitudDetalle } from '@/types/solicitud'
import { formatearFecha, formatearFechaHora } from '@/utils/fechas'
import { DocumentacionAcademica } from './DocumentacionAcademica'
import { ACCIONES_EN_BANNER, type VistaSolicitudProps } from './vistas'

const PESTANIAS: Pestania[] = [
  { clave: 'documentacion', texto: 'Documentación', Icono: FileText },
  { clave: 'detalles', texto: 'Detalles', Icono: FileText },
  { clave: 'chat', texto: 'Chat', Icono: MessageCircle },
  { clave: 'historial', texto: 'Historial', Icono: History },
]

const COLUMNAS_DOCUMENTOS: ColumnaTabla<Documento>[] = [
  {
    clave: 'documento',
    titulo: 'Documento',
    ancho: 'lg:w-1/4',
    render: ({ tipo, descripcion }) => (
      <span className="text-text">
        {NOMBRE_TIPO_DOCUMENTO[tipo]}
        {descripcion && `: ${descripcion}`}
      </span>
    ),
  },
  {
    clave: 'estado',
    titulo: 'Estado',
    ancho: 'lg:w-1/5',
    render: ({ estadoRevision }) => {
      const { texto, variante } = MARCA_REVISION[estadoRevision]
      return <Badge variante={variante}>{texto}</Badge>
    },
  },
  {
    clave: 'carga',
    titulo: 'Fecha de carga',
    ancho: 'lg:w-1/8',
    render: ({ fechaCarga }) => (fechaCarga ? formatearFecha(fechaCarga) : '–'),
  },
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
        'Falta cargarlo'
      ),
  },
  { clave: 'observaciones', titulo: 'Observaciones', render: ({ comentarioRevision }) => comentarioRevision ?? '–' },
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

function PanelDocumentacion({ solicitudId }: { solicitudId: number }) {
  const { datos, cargando, error, reintentar } = useCarga((signal) => solicitudes.documentos(solicitudId, signal))

  return (
    <>
      <Seccion
        Icono={FileText}
        titulo="Documentación requerida"
        descripcion="Documentos que cargaste para tu equivalencia."
        className="rounded-t-none"
      >
        <Tabla
          interna
          descripcion="Documentos de la solicitud"
          columnas={COLUMNAS_DOCUMENTOS}
          // Los archivos de una recarga todavía sin enviar se ven en el panel de acción, no acá.
          filas={(datos ?? []).filter(({ borrador }) => !borrador)}
          obtenerClave={({ id }) => id}
          cargando={cargando}
          error={error}
          textoVacio="Todavía no cargaste documentos."
          onReintentar={reintentar}
        />
      </Seccion>
      <DocumentacionAcademica solicitudId={solicitudId} />
    </>
  )
}

function Fila({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
  return (
    <div className="grid grid-cols-5 items-center gap-4 py-3">
      <dt className="col-span-2 text-base text-text-muted">{etiqueta}</dt>
      <dd className="col-span-3 text-base text-text">{children}</dd>
    </div>
  )
}

function TarjetaResponsable({ rol, responsable }: { rol: string; responsable: Responsable | null }) {
  return (
    <li className="flex items-center gap-4 rounded-md bg-surface-muted p-4">
      <User aria-hidden className="size-(--size-icon-lg) shrink-0 text-primary" />
      <p className="w-1/3 text-base font-medium text-text">{rol}</p>
      {responsable ? (
        <p className="min-w-0 flex-1 border-l-2 border-primary pl-4 text-base text-text">
          {responsable.nombre}
          <a href={`mailto:${responsable.email}`} className="block truncate text-primary underline">
            {responsable.email}
          </a>
          <span className="block text-sm text-text-muted">Asignado el {formatearFecha(responsable.fechaAsignacion)}</span>
        </p>
      ) : (
        <p className="flex-1 border-l-2 border-border pl-4 text-base text-text-muted">Sin asignar</p>
      )}
    </li>
  )
}

/** `completo` suma lo que consulta el personal: datos de contacto del aspirante y del formulario inicial (§5.5). */
export function PanelDetalles({ detalle, completo = false }: { detalle: SolicitudDetalle; completo?: boolean }) {
  const { aspirante, datosOrigen, responsableAcademico, responsableAdministrativo } = detalle

  return (
    <div className="grid items-start gap-4 lg:grid-cols-2">
      <Seccion Icono={FileText} titulo="Información general" className="lg:rounded-t-none">
        <dl className="divide-y divide-border">
          <Fila etiqueta="Código de expediente">{detalle.codigoExp}</Fila>
          <Fila etiqueta="Estado actual">
            <EstadoBadge estado={detalle.estado} />
          </Fila>
          <Fila etiqueta="Carrera de origen">
            {datosOrigen.carrera}
            <span className="block text-text-muted">
              {datosOrigen.facultad}, {datosOrigen.universidad}
            </span>
          </Fila>
          <Fila etiqueta="Carrera destino">{detalle.carreraDestino.nombre}</Fila>
          {completo && (
            <>
              <Fila etiqueta="Condición académica">{datosOrigen.condicionAcademica}</Fila>
              <Fila etiqueta="Observaciones">{datosOrigen.observaciones || '–'}</Fila>
              <Fila etiqueta="Aspirante">
                {aspirante.nombre}
                <span className="block text-text-muted">DNI {aspirante.dni}</span>
              </Fila>
              <Fila etiqueta="Contacto">
                <a href={`mailto:${aspirante.email}`} className="text-primary underline">
                  {aspirante.email}
                </a>
                <span className="block text-text-muted">{aspirante.telefono}</span>
              </Fila>
            </>
          )}
          <Fila etiqueta="Fecha de creación">{formatearFecha(detalle.fechaCreacion)}</Fila>
          <Fila etiqueta="Última actualización">{formatearFecha(detalle.fechaUltimaMod)}</Fila>
          {completo && (
            <Fila etiqueta="Último cambio de estado">{formatearFecha(detalle.fechaUltimoCambioEstado)}</Fila>
          )}
        </dl>
      </Seccion>
      <Seccion Icono={Users} titulo="Responsables" className="lg:rounded-t-none">
        {responsableAcademico || responsableAdministrativo ? (
          <ul className="flex flex-col gap-4">
            <TarjetaResponsable rol="Responsable académico" responsable={responsableAcademico} />
            <TarjetaResponsable rol="Responsable administrativo" responsable={responsableAdministrativo} />
          </ul>
        ) : (
          <p className="rounded-md bg-surface-muted px-4 py-6 text-center text-base text-text-muted">
            Esperando que alguien tome el proceso.
          </p>
        )}
      </Seccion>
    </div>
  )
}

export function PanelHistorial({ solicitudId }: { solicitudId: number }) {
  const { datos, cargando, error, reintentar } = useCarga((signal) =>
    Promise.all([solicitudes.historial(solicitudId, signal), catalogos.estados(signal)]),
  )
  const [eventos, estados] = datos ?? [[], []]
  const nombreEstado = (codigo: string) => estados.find((estado) => estado.codigo === codigo)?.nombre ?? codigo

  const columnas: ColumnaTabla<EventoHistorial>[] = [
    { clave: 'fecha', titulo: 'Fecha y hora', ancho: 'lg:w-3/20', render: ({ fecha }) => formatearFechaHora(fecha) },
    {
      clave: 'accion',
      titulo: 'Estado / Acción',
      ancho: 'lg:w-3/10',
      render: ({ tipo, estadoNuevo }) =>
        tipo === 'CAMBIO_ESTADO' && estadoNuevo ? (
          <EstadoBadge estado={{ codigo: estadoNuevo, nombre: nombreEstado(estadoNuevo) }} />
        ) : tipo === 'DOCUMENTO_CARGADO' ? (
          <Badge variante="exito">Documento cargado</Badge>
        ) : (
          <Badge variante="neutro">Cambio de responsable</Badge>
        ),
    },
    {
      clave: 'descripcion',
      titulo: 'Descripción',
      render: ({ tipo, estadoAnterior, estadoNuevo, comentario }) =>
        tipo === 'DOCUMENTO_CARGADO'
          ? `Se cargó el documento "${comentario}".`
          : (comentario ??
            (tipo === 'CAMBIO_ESTADO' && estadoNuevo
              ? estadoAnterior
                ? `La solicitud pasó a "${nombreEstado(estadoNuevo)}".`
                : 'Se inició la solicitud de equivalencia.'
              : '–')),
    },
    { clave: 'responsable', titulo: 'Responsable', ancho: 'lg:w-1/5', render: ({ usuario }) => usuario },
  ]

  return (
    <Seccion
      Icono={History}
      titulo="Historial de la solicitud"
      descripcion="Seguimiento de todos los cambios de estado, de responsable y de documentos del trámite."
      className="rounded-t-none"
    >
      <Tabla
        interna
        descripcion="Historial de la solicitud"
        columnas={columnas}
        filas={eventos}
        obtenerClave={({ fecha, tipo, estadoNuevo, comentario }) => `${fecha}-${tipo}-${estadoNuevo}-${comentario}`}
        cargando={cargando}
        error={error}
        textoVacio="Todavía no hay movimientos."
        onReintentar={reintentar}
      />
    </Seccion>
  )
}

function turno({ estado, accionesDisponibles }: SolicitudDetalle) {
  if (estado.esTerminal) return 'Trámite cerrado'
  return accionesDisponibles.length ? 'Te toca a vos' : 'Estamos trabajando en esto'
}

/**
 * Detalle de solo lectura de una solicitud: banner del estado y pestañas
 * (frontend-interfaces.md §4.5 a §4.7 y Anexo B, punto 18).
 */
export function DetalleSolicitud(props: VistaSolicitudProps) {
  const { detalle } = props
  const [activa, setActiva] = useState(PESTANIAS[0].clave)
  const texto = BANNER_ASPIRANTE[detalle.estado.codigo]
  const Accion = ACCIONES_EN_BANNER[detalle.estado.codigo]

  return (
    <>
      <Banner
        variante={VARIANTE_POR_ESTADO[detalle.estado.codigo] ?? 'neutro'}
        titulo={detalle.estado.nombre}
        etiqueta={turno(detalle)}
      >
        {texto && <p>{texto}</p>}
        {Accion && <Accion {...props} />}
      </Banner>

      <div>
        <Pestanias pestanias={PESTANIAS} activa={activa} onCambiar={setActiva} />
        <div role="tabpanel" aria-labelledby={`pestania-${activa}`} className="flex flex-col gap-4">
          {activa === 'documentacion' && <PanelDocumentacion solicitudId={detalle.id} />}
          {activa === 'detalles' && <PanelDetalles detalle={detalle} />}
          {activa === 'chat' && (
            <Seccion Icono={MessageCircle} titulo="Chat de la solicitud" className="rounded-t-none">
              <p className="rounded-md bg-surface-muted px-4 py-6 text-center text-base text-text-muted">
                Próximamente: el chat con tus responsables todavía no está disponible.
              </p>
            </Seccion>
          )}
          {activa === 'historial' && <PanelHistorial solicitudId={detalle.id} />}
        </div>
      </div>
    </>
  )
}
