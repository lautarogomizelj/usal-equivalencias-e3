import { ArrowLeft, CircleAlert, CircleX, FileText, Paperclip, Plus, Send, Trash2 } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router'
import { Banner, Button, CargadorArchivo, Input, Modal, ModalConfirmacion, Seccion } from '@/components/ui'
import { DOCUMENTOS, NOMBRE_TIPO_DOCUMENTO } from '@/constants/documentos'
import { ROUTES } from '@/constants/routes'
import { useCargaDocumentos } from '@/hooks/useCargaDocumentos'
import { acciones } from '@/services/acciones'
import { ApiError } from '@/services/api/ApiError'
import { solicitudes } from '@/services/solicitudes'
import type { Documento, SolicitudDetalle, TipoDocumento } from '@/types/solicitud'
import { formatearTamanio } from '@/utils/archivos'
import { MENSAJES } from '@/validations/messages'
import type { VistaSolicitudProps } from './vistas'

const FILA_PROGRAMA_NUEVO = 'PROGRAMA-nuevo'
const ACCION_ENVIAR = 'ENVIAR_DOCUMENTACION'
const ACCION_CANCELAR = 'CANCELAR_SOLICITUD'
const FILAS_ESQUELETO = [0, 1, 2]

const DESCRIPCIONES: Record<'ANALITICO' | 'PLAN' | 'PROGRAMA', string> = {
  ANALITICO: 'Certificado analítico completo de la carrera de origen, con las asignaturas aprobadas.',
  PLAN: 'Plan de estudios oficial y actualizado de la carrera de origen.',
  PROGRAMA: 'Programa oficial de una asignatura aprobada que pedís como equivalencia. Cargá uno por materia.',
}

interface FilaDocumentoProps {
  titulo: string
  descripcion: string
  documento?: Documento
  subiendo: boolean
  error?: string | null
  onArchivo: (archivo: File) => void
  onQuitar?: () => void
  /** Campo extra a completar antes de adjuntar, por ejemplo la materia de un programa. */
  children?: ReactNode
}

function FilaDocumento({ titulo, descripcion, documento, subiendo, error, onArchivo, onQuitar, children }: FilaDocumentoProps) {
  return (
    <li className="flex flex-col gap-4 rounded-md border border-border p-4 lg:flex-row lg:items-center lg:gap-6">
      <div className="flex-1">
        <h3 className="text-xl">
          {titulo}
          <span aria-hidden className="ml-2 font-sans text-danger">
            *
          </span>
        </h3>
        <p className="text-base text-text-muted">{descripcion}</p>
        {children && <div className="mt-2 max-w-(--size-form)">{children}</div>}
      </div>

      <div className="lg:w-2/5">
        {documento && !subiendo ? (
          <div className="flex min-h-(--size-cargador) flex-wrap items-center gap-3 rounded-md border border-border bg-surface-muted px-4 py-2">
            <Paperclip aria-hidden className="size-(--size-icon-md) shrink-0 text-primary" />
            <p className="min-w-0 flex-1">
              {documento.urlDescarga ? (
                <a
                  href={solicitudes.urlArchivo(documento.urlDescarga)}
                  target="_blank"
                  rel="noreferrer"
                  className="block truncate text-base font-medium text-primary underline"
                >
                  {documento.nombreArchivo}
                </a>
              ) : (
                <span className="block truncate text-base font-medium text-text">{documento.nombreArchivo}</span>
              )}
              <span className="block text-sm text-text-muted">{formatearTamanio(documento.tamanoBytes ?? 0)}</span>
            </p>
            <CargadorArchivo compacto etiqueta={`Reemplazar ${titulo}`} onArchivo={onArchivo} />
            <Button variante="ghost" aria-label={`Quitar ${titulo}`} onClick={onQuitar}>
              <Trash2 aria-hidden className="size-(--size-icon-sm)" />
              Quitar
            </Button>
          </div>
        ) : (
          <CargadorArchivo etiqueta={`Adjuntar ${titulo}`} subiendo={subiendo} onArchivo={onArchivo} />
        )}
        {error && (
          <p role="alert" className="mt-1 flex items-center gap-1 text-sm text-danger">
            <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
            {error}
          </p>
        )}
      </div>
    </li>
  )
}

/** Carga documental inicial del aspirante, `ASP-SOL-INI` (frontend-interfaces.md §4.6.1 y Anexo B, puntos 4 y 5). */
export function CargaDocumentacion({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const navigate = useNavigate()
  const carga = useCargaDocumentos(detalle.id, onEstadoCambio)
  const [materia, setMateria] = useState('')
  const [agregando, setAgregando] = useState(false)
  const [modal, setModal] = useState<'enviar' | 'cancelar' | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [errorAccion, setErrorAccion] = useState<string | null>(null)

  const { documentos, subiendo, errores } = carga
  const unico = (tipo: TipoDocumento) => documentos.find((documento) => documento.tipo === tipo)
  const programas = documentos.filter(({ tipo }) => tipo === 'PROGRAMA')
  const faltantes = [
    unico('ANALITICO') ? null : 'el certificado analítico',
    unico('PLAN') ? null : 'el plan de estudios',
    programas.length ? null : 'al menos un programa',
  ].filter((faltante) => faltante !== null)
  const conErrores = Object.values(errores).some(Boolean)
  const puedeEnviar = detalle.accionesDisponibles.includes(ACCION_ENVIAR) && !faltantes.length && !conErrores && !subiendo

  async function subirPrograma(archivo: File) {
    if (!materia.trim()) return carga.marcarError(FILA_PROGRAMA_NUEVO, MENSAJES.MATERIA_REQUERIDA)
    if (await carga.subir(FILA_PROGRAMA_NUEVO, archivo, 'PROGRAMA', materia.trim())) {
      setMateria('')
      setAgregando(false)
    }
  }

  function abrir(cual: 'enviar' | 'cancelar') {
    setErrorAccion(null)
    setModal(cual)
  }

  async function ejecutar(accion: (id: number, estadoEsperado: string) => Promise<SolicitudDetalle>, mensaje: string) {
    setProcesando(true)
    setErrorAccion(null)
    try {
      onActualizada(await accion(detalle.id, detalle.estado.codigo), mensaje)
    } catch (error) {
      if (error instanceof ApiError && (error.kind === 'conflict' || error.kind === 'forbidden')) return onEstadoCambio()
      setErrorAccion(error instanceof Error ? error.message : null)
      setProcesando(false)
    }
  }

  const filaUnica = (tipo: 'ANALITICO' | 'PLAN') => {
    const documento = unico(tipo)
    return (
      <FilaDocumento
        titulo={NOMBRE_TIPO_DOCUMENTO[tipo]}
        descripcion={DESCRIPCIONES[tipo]}
        documento={documento}
        subiendo={subiendo === tipo}
        error={errores[tipo]}
        onArchivo={(archivo) => carga.subir(tipo, archivo, tipo, undefined, documento)}
        onQuitar={() => documento && carga.quitar(tipo, documento)}
      />
    )
  }

  return (
    <>
      <Banner variante="accion" titulo="Documentación obligatoria" etiqueta="Te toca a vos">
        <p>
          La carga de toda la documentación solicitada es <strong>obligatoria</strong> para seguir con tu solicitud de
          equivalencia. Asegurate de que los archivos sean legibles y estén completos.
        </p>
        {!detalle.responsableAcademico && !detalle.responsableAdministrativo && (
          <p>Todavía nadie tomó tu proceso. Podés cargar tu documentación igual.</p>
        )}
      </Banner>

      <Seccion
        Icono={FileText}
        titulo="Documentación requerida"
        descripcion="Adjuntá los siguientes documentos en formato PDF o JPG (máx. 10 MB por archivo y 50 MB en total)."
      >
        {carga.errorCarga ? (
          <div className="py-8 text-center">
            <p className="text-danger">{carga.errorCarga}</p>
            <Button variante="outline" tamanio="sm" className="mt-3" onClick={carga.reintentar}>
              Reintentar
            </Button>
          </div>
        ) : carga.cargando ? (
          <div aria-busy className="flex flex-col gap-4">
            {FILAS_ESQUELETO.map((fila) => (
              <span key={fila} className="block h-(--size-tabla-fila) animate-pulse rounded-md bg-surface-muted" />
            ))}
          </div>
        ) : (
          <>
            <ul className="flex flex-col gap-4">
              {filaUnica('ANALITICO')}
              {filaUnica('PLAN')}
              {programas.map((documento) => {
                const fila = `PROGRAMA-${documento.id}`
                return (
                  <FilaDocumento
                    key={documento.id}
                    titulo={`${NOMBRE_TIPO_DOCUMENTO.PROGRAMA}: ${documento.descripcion}`}
                    descripcion={DESCRIPCIONES.PROGRAMA}
                    documento={documento}
                    subiendo={subiendo === fila}
                    error={errores[fila]}
                    onArchivo={(archivo) =>
                      carga.subir(fila, archivo, 'PROGRAMA', documento.descripcion ?? undefined, documento)
                    }
                    onQuitar={() => carga.quitar(fila, documento)}
                  />
                )
              })}
              {(agregando || programas.length === 0) && (
                <FilaDocumento
                  titulo={NOMBRE_TIPO_DOCUMENTO.PROGRAMA}
                  descripcion={DESCRIPCIONES.PROGRAMA}
                  subiendo={subiendo === FILA_PROGRAMA_NUEVO}
                  error={errores[FILA_PROGRAMA_NUEVO]}
                  onArchivo={subirPrograma}
                >
                  <Input
                    required
                    etiqueta="Materia"
                    placeholder="Ej.: Matemática I"
                    value={materia}
                    onChange={(evento) => {
                      setMateria(evento.target.value)
                      carga.marcarError(FILA_PROGRAMA_NUEVO, null)
                    }}
                  />
                </FilaDocumento>
              )}
            </ul>

            {programas.length > 0 && !agregando && (
              <Button variante="ghost" className="mt-2" onClick={() => setAgregando(true)}>
                <Plus aria-hidden className="size-(--size-icon-sm)" />
                Agregar otro programa
              </Button>
            )}
          </>
        )}

        <footer className="mt-4 flex flex-wrap items-center justify-between gap-4 border-t border-border pt-4">
          <div className="text-base text-text-muted">
            <p>
              {documentos.length} {documentos.length === 1 ? 'archivo cargado' : 'archivos cargados'} ·{' '}
              {formatearTamanio(carga.bytesUsados)} de {formatearTamanio(DOCUMENTOS.MAX_BYTES_TOTAL)}
            </p>
            {faltantes.length > 0 && !carga.cargando && !carga.errorCarga && (
              <p className="text-alert">Falta cargar {new Intl.ListFormat('es').format(faltantes)}.</p>
            )}
          </div>
          <div className="flex flex-wrap gap-3">
            {detalle.accionesDisponibles.includes(ACCION_CANCELAR) && (
              <Button variante="dangerOutline" tamanio="lg" disabled={Boolean(subiendo)} onClick={() => abrir('cancelar')}>
                <CircleX aria-hidden className="size-(--size-icon-md)" />
                Cancelar solicitud
              </Button>
            )}
            <Button variante="outline" tamanio="lg" onClick={() => navigate(ROUTES.MIS_SOLICITUDES)}>
              <ArrowLeft aria-hidden className="size-(--size-icon-md)" />
              Volver
            </Button>
            <Button tamanio="lg" disabled={!puedeEnviar} onClick={() => abrir('enviar')}>
              <Send aria-hidden className="size-(--size-icon-md)" />
              Enviar documentación
            </Button>
          </div>
        </footer>
      </Seccion>

      <Modal
        abierto={modal === 'enviar'}
        titulo="Enviar documentación"
        descripcion="Revisá los archivos antes de enviarlos."
        Icono={Send}
        onCerrar={() => setModal(null)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setModal(null)}>
              Volver
            </Button>
            <Button
              disabled={procesando}
              onClick={() => ejecutar(acciones.enviarDocumentacion, 'Tu documentación fue enviada.')}
            >
              {procesando ? 'Enviando…' : 'Confirmar envío'}
            </Button>
          </>
        }
      >
        <ul className="divide-y divide-border rounded-md border border-border text-sm">
          {documentos.map(({ id, tipo, descripcion, nombreArchivo }) => (
            <li key={id} className="flex flex-wrap justify-between gap-2 px-3 py-2">
              <span className="font-medium text-text">
                {NOMBRE_TIPO_DOCUMENTO[tipo]}
                {descripcion && `: ${descripcion}`}
              </span>
              <span className="text-text-muted">{nombreArchivo}</span>
            </li>
          ))}
        </ul>
        <p className="mt-4 rounded-md bg-warning-soft p-3 text-sm text-warning">
          Una vez enviada, no vas a poder modificar la documentación salvo que el equipo académico te pida recargarla.
        </p>
        {errorAccion && (
          <p role="alert" className="mt-3 rounded-md bg-danger-soft p-3 text-sm text-danger">
            {errorAccion}
          </p>
        )}
      </Modal>

      <ModalConfirmacion
        peligrosa
        abierto={modal === 'cancelar'}
        titulo="Cancelar la solicitud"
        accion={`Vas a cancelar la solicitud ${detalle.codigoExp}.`}
        consecuencia="La solicitud queda cerrada y se eliminan los archivos que cargaste. Esta acción no se puede deshacer."
        textoConfirmar="Sí, cancelar la solicitud"
        textoCancelar="Volver"
        cargando={procesando}
        error={errorAccion}
        onConfirmar={() => ejecutar(acciones.cancelarSolicitud, 'Tu solicitud fue cancelada.')}
        onCancelar={() => setModal(null)}
      />
    </>
  )
}
