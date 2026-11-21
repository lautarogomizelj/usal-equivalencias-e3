import { Check, ChevronDown, CircleAlert, FileText, Info, Paperclip, Plus, X } from 'lucide-react'
import { useState } from 'react'
import {
  Badge,
  Button,
  Input,
  Modal,
  ModalConfirmacion,
  Seccion,
  Select,
  Tabla,
  Textarea,
  type ColumnaTabla,
} from '@/components/ui'
import { VARIANTES_ESTADO } from '@/components/ui/variantesEstado'
import { MARCA_REVISION, NOMBRE_TIPO_DOCUMENTO } from '@/constants/documentos'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { ApiError } from '@/services/api/ApiError'
import { solicitudes } from '@/services/solicitudes'
import type { Documento, EstadoRevision, Marca, TipoDocumento } from '@/types/solicitud'
import { tituloDocumento as titulo } from '@/utils/documentos'
import { mensajeDeError } from '@/utils/errores'
import { formatearFecha } from '@/utils/fechas'
import { MENSAJES } from '@/validations/messages'
import type { VistaSolicitudProps } from './vistas'

const MARCAS: EstadoRevision[] = ['PENDIENTE', 'OK', 'RECARGAR']
const TIPOS = Object.entries(NOMBRE_TIPO_DOCUMENTO).map(([valor, texto]) => ({ valor, texto }))
const MAX_COMENTARIO = 500

interface MarcaLocal {
  estadoRevision: EstadoRevision
  comentario: string
}

function SelectorMarca({ valor, etiqueta, onCambiar }: { valor: EstadoRevision; etiqueta: string; onCambiar: (marca: EstadoRevision) => void }) {
  const { suave, borde } = VARIANTES_ESTADO[MARCA_REVISION[valor].variante]

  return (
    <span className="relative block w-full">
      <select
        aria-label={etiqueta}
        value={valor}
        className={`h-(--size-input) w-full appearance-none rounded-md border pr-8 pl-3 text-base font-medium ${suave} ${borde}`}
        onChange={(evento) => onCambiar(evento.target.value as EstadoRevision)}
      >
        {MARCAS.map((marca) => (
          <option key={marca} value={marca} className="bg-surface text-text">
            {MARCA_REVISION[marca].texto}
          </option>
        ))}
      </select>
      <ChevronDown aria-hidden className="pointer-events-none absolute top-1/2 right-3 size-(--size-icon-sm) -translate-y-1/2" />
    </span>
  )
}

/**
 * Documentos de la solicitud vistos por el personal (frontend-interfaces.md §5.6 y §5.7). Con la acción
 * `GUARDAR_MARCAS` disponible es la revisión del Académico responsable (`ACAD-DOC-REV`): marca cada documento
 * y acepta o rechaza. En cualquier otro caso es la misma tabla en solo lectura.
 */
export function RevisionDocumentos({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const carga = useCarga((signal) => solicitudes.documentos(detalle.id, signal))
  const [cambios, setCambios] = useState<Record<number, MarcaLocal>>({})
  const [modal, setModal] = useState<'aceptar' | 'rechazar' | 'faltante' | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [guardado, setGuardado] = useState(false)
  const [motivo, setMotivo] = useState('')
  const [errorMotivo, setErrorMotivo] = useState<string | null>(null)
  const [faltante, setFaltante] = useState({ tipo: 'PROGRAMA' as TipoDocumento, descripcion: '' })

  const editable = detalle.accionesDisponibles.includes('GUARDAR_MARCAS')
  const documentos = carga.datos ?? []
  const estadoEsperado = detalle.estado.codigo
  const marcaDe = (documento: Documento): MarcaLocal =>
    cambios[documento.id] ?? { estadoRevision: documento.estadoRevision, comentario: documento.comentarioRevision ?? '' }
  const marcas: Marca[] = documentos.map((documento) => {
    const { estadoRevision, comentario } = marcaDe(documento)
    return { documentoId: documento.id, estadoRevision, comentario: comentario.trim() || null }
  })
  const cantidad = (marca: EstadoRevision) => marcas.filter(({ estadoRevision }) => estadoRevision === marca).length
  const aRecargar = documentos.filter((documento) => marcaDe(documento).estadoRevision === 'RECARGAR')
  const puedeRechazar = aRecargar.length > 0 && cantidad('PENDIENTE') === 0

  function cambiar(documento: Documento, cambio: Partial<MarcaLocal>) {
    setGuardado(false)
    setCambios((actuales) => ({ ...actuales, [documento.id]: { ...marcaDe(documento), ...cambio } }))
  }

  function abrir(cual: 'aceptar' | 'rechazar' | 'faltante') {
    setError(null)
    setErrorMotivo(null)
    setModal(cual)
  }

  /** Si el backend dice que la acción ya no corresponde, la página recarga la solicitud y avisa (§0.16.4). */
  async function ejecutar(operacion: () => Promise<void>) {
    setProcesando(true)
    setError(null)
    try {
      await operacion()
    } catch (causa) {
      const incompletas = causa instanceof ApiError && causa.body?.code === 'MARCAS_INCOMPLETAS'
      if (causa instanceof ApiError && !incompletas && (causa.kind === 'conflict' || causa.kind === 'forbidden')) {
        return onEstadoCambio()
      }
      setError(incompletas ? MENSAJES.RECHAZO_INCOMPLETO : mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  const guardar = () =>
    ejecutar(async () => {
      carga.setDatos(await acciones.guardarMarcas(detalle.id, estadoEsperado, marcas))
      setCambios({})
      setGuardado(true)
    })

  const aceptar = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.aceptarDocumentacion(detalle.id, estadoEsperado, marcas),
        'La documentación fue aceptada. La solicitud pasó a análisis preliminar.',
      ),
    )

  function rechazar() {
    if (!motivo.trim()) return setErrorMotivo(MENSAJES.MOTIVO_REQUERIDO)
    return ejecutar(async () =>
      onActualizada(
        await acciones.rechazarDocumentacion(detalle.id, estadoEsperado, motivo.trim(), marcas),
        'La documentación fue rechazada. Le avisamos al aspirante qué tiene que recargar.',
      ),
    )
  }

  function agregarFaltante() {
    if (!faltante.descripcion.trim()) return setError(MENSAJES.REQUERIDO)
    return ejecutar(async () => {
      const nuevo = await acciones.agregarFaltante(detalle.id, faltante.tipo, faltante.descripcion.trim())
      carga.setDatos((actuales) => [...(actuales ?? []), nuevo])
      setFaltante({ tipo: 'PROGRAMA', descripcion: '' })
      setModal(null)
    })
  }

  const columnas: ColumnaTabla<Documento>[] = [
    {
      clave: 'documento',
      titulo: 'Documento',
      ancho: 'lg:w-1/5',
      render: (documento) => <span className="text-text">{titulo(documento)}</span>,
    },
    {
      clave: 'archivo',
      titulo: 'Archivo',
      ancho: 'lg:w-1/5',
      render: ({ urlDescarga, nombreArchivo, version }) =>
        urlDescarga ? (
          <a
            href={solicitudes.urlArchivo(urlDescarga)}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 text-primary underline"
          >
            <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0" />
            {nombreArchivo}
            {version > 1 && ` (v${version}, recargado)`}
          </a>
        ) : (
          'Falta cargarlo'
        ),
    },
    {
      clave: 'carga',
      titulo: 'Fecha de carga',
      ancho: 'lg:w-1/10',
      render: ({ fechaCarga }) => (fechaCarga ? formatearFecha(fechaCarga) : '–'),
    },
    {
      clave: 'estado',
      titulo: 'Estado',
      ancho: 'lg:w-1/5',
      render: (documento) => {
        const { estadoRevision } = marcaDe(documento)
        const { texto, variante } = MARCA_REVISION[estadoRevision]
        // Un faltante no tiene archivo que aprobar: queda siempre para recargar.
        return editable && documento.urlDescarga ? (
          <SelectorMarca
            valor={estadoRevision}
            etiqueta={`Estado de ${titulo(documento)}`}
            onCambiar={(marca) => cambiar(documento, { estadoRevision: marca })}
          />
        ) : (
          <Badge variante={variante}>{texto}</Badge>
        )
      },
    },
    {
      clave: 'comentarios',
      titulo: 'Comentarios',
      render: (documento) =>
        editable ? (
          <div className="w-full py-2">
            <Textarea
              etiquetaOculta
              etiqueta={`Comentario de ${titulo(documento)}`}
              placeholder="Lo va a ver el aspirante."
              maxLength={MAX_COMENTARIO}
              value={marcaDe(documento).comentario}
              className="resize-none"
              onChange={(evento) => cambiar(documento, { comentario: evento.target.value })}
            />
          </div>
        ) : (
          (documento.comentarioRevision ?? '–')
        ),
    },
  ]

  return (
    <>
      <Seccion
        Icono={FileText}
        titulo={editable ? 'Documentación inicial de la solicitud' : 'Documentación de la solicitud'}
        descripcion={
          editable
            ? 'Revisá cada documento, indicá su estado y agregá comentarios. Después aceptá o rechazá la documentación.'
            : 'Documentos que presentó el aspirante, con su marca de revisión.'
        }
        className="formulario-compacto"
      >
        {estadoEsperado === 'DOC-RECH' && detalle.ultimaObservacion && (
          <p className="mb-4 rounded-md bg-danger-soft p-3 text-base text-text">
            <strong className="text-danger">Motivo del rechazo:</strong> {detalle.ultimaObservacion.comentario}
          </p>
        )}

        <Tabla
          interna
          descripcion="Documentos de la solicitud"
          columnas={columnas}
          filas={documentos}
          obtenerClave={({ id }) => id}
          cargando={carga.cargando}
          error={carga.error}
          textoVacio="El aspirante todavía no envió documentos."
          onReintentar={carga.reintentar}
        />

        {editable && !carga.cargando && !carga.error && (
          <>
            <Button variante="ghost" className="mt-2" onClick={() => abrir('faltante')}>
              <Plus aria-hidden className="size-(--size-icon-sm)" />
              Agregar documento faltante
            </Button>

            <p className="mt-2 flex items-center gap-3 rounded-md bg-warning-soft p-3 text-base text-warning">
              <Info aria-hidden className="size-(--size-icon-md) shrink-0" />
              Una vez revisados todos los documentos, aceptá o rechazá la documentación. Podés guardar las marcas si
              necesitás continuar más tarde.
            </p>

            <footer className="mt-4 flex flex-wrap items-center justify-between gap-4 border-t border-border pt-4">
              <div className="text-base text-text-muted">
                <p>
                  {cantidad('OK')} correctos · {cantidad('RECARGAR')} a recargar · {cantidad('PENDIENTE')} pendientes
                </p>
                {guardado && (
                  <p role="status" className="text-success">
                    Las marcas se guardaron.
                  </p>
                )}
                {!puedeRechazar && <p>{MENSAJES.RECHAZO_INCOMPLETO}</p>}
                {error && !modal && (
                  <p role="alert" className="flex items-center gap-1 text-danger">
                    <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                    {error}
                  </p>
                )}
              </div>
              <div className="flex flex-wrap gap-3">
                <Button variante="outline" tamanio="lg" disabled={procesando} onClick={guardar}>
                  {procesando && !modal ? 'Guardando…' : 'Guardar marcas'}
                </Button>
                <Button
                  variante="dangerOutline"
                  tamanio="lg"
                  disabled={procesando || !puedeRechazar}
                  onClick={() => abrir('rechazar')}
                >
                  <X aria-hidden className="size-(--size-icon-md)" />
                  Rechazar documentación
                </Button>
                <Button tamanio="lg" disabled={procesando} onClick={() => abrir('aceptar')}>
                  <Check aria-hidden className="size-(--size-icon-md)" />
                  Aceptar documentación
                </Button>
              </div>
            </footer>
          </>
        )}
      </Seccion>

      <ModalConfirmacion
        abierto={modal === 'aceptar'}
        titulo="Aceptar la documentación"
        accion={`Vas a aceptar la documentación de la solicitud ${detalle.codigoExp}.`}
        consecuencia={`${
          aRecargar.length > 0
            ? `Hay ${aRecargar.length === 1 ? '1 documento marcado' : `${aRecargar.length} documentos marcados`} como "Debe recargar": si aceptás, el aspirante ya no va a poder recargarlos. `
            : ''
        }La solicitud pasa a análisis preliminar y se le avisa al aspirante por mail. Las marcas se guardan con la decisión.`}
        textoConfirmar="Aceptar documentación"
        cargando={procesando}
        error={error}
        onConfirmar={aceptar}
        onCancelar={() => setModal(null)}
      />

      <Modal
        abierto={modal === 'rechazar'}
        titulo="Rechazar la documentación"
        descripcion={`La solicitud ${detalle.codigoExp} vuelve al aspirante para que recargue lo marcado.`}
        Icono={CircleAlert}
        onCerrar={() => setModal(null)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setModal(null)}>
              Cancelar
            </Button>
            <Button variante="danger" disabled={procesando} onClick={rechazar}>
              {procesando ? 'Procesando…' : 'Rechazar documentación'}
            </Button>
          </>
        }
      >
        <p className="text-sm font-medium text-text">Documentos a recargar</p>
        <ul className="mt-1 divide-y divide-border rounded-md border border-border text-sm">
          {aRecargar.map((documento) => (
            <li key={documento.id} className="px-3 py-2 text-text">
              {titulo(documento)}
            </li>
          ))}
        </ul>
        <div className="mt-4">
          <Textarea
            required
            etiqueta="Motivo general"
            ayuda="Se lo enviamos al aspirante por mail y lo ve en su solicitud."
            maxLength={MAX_COMENTARIO}
            value={motivo}
            error={errorMotivo}
            onChange={(evento) => {
              setMotivo(evento.target.value)
              setErrorMotivo(null)
            }}
          />
        </div>
        {error && (
          <p role="alert" className="mt-3 rounded-md bg-danger-soft p-3 text-sm text-danger">
            {error}
          </p>
        )}
      </Modal>

      <Modal
        abierto={modal === 'faltante'}
        titulo="Agregar documento faltante"
        descripcion="Queda marcado para que el aspirante lo cargue junto con la recarga."
        Icono={Plus}
        onCerrar={() => setModal(null)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setModal(null)}>
              Cancelar
            </Button>
            <Button disabled={procesando} onClick={agregarFaltante}>
              {procesando ? 'Agregando…' : 'Agregar'}
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <Select
            etiqueta="Tipo de documento"
            opciones={TIPOS}
            value={faltante.tipo}
            onChange={(evento) => setFaltante({ ...faltante, tipo: evento.target.value as TipoDocumento })}
          />
          <Input
            required
            etiqueta="Qué falta"
            placeholder="Ej.: Programa de Análisis Matemático II"
            maxLength={255}
            value={faltante.descripcion}
            error={error}
            onChange={(evento) => {
              setFaltante({ ...faltante, descripcion: evento.target.value })
              setError(null)
            }}
          />
        </div>
      </Modal>
    </>
  )
}
