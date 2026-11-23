import { FileText, History, Info, Mail, MessageCircle } from 'lucide-react'
import { useState } from 'react'
import { useParams } from 'react-router'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Chat } from '@/components/solicitudes/Chat'
import { PanelDetalles, PanelHistorial } from '@/components/solicitudes/DetalleSolicitud'
import { DocumentacionAcademica } from '@/components/solicitudes/DocumentacionAcademica'
import { RevisionDocumentos } from '@/components/solicitudes/RevisionDocumentos'
import {
  ACCIONES_EN_BANNER_PERSONAL,
  ESTADOS_CON_CHAT_ABIERTO,
  VISTAS_PERSONAL,
} from '@/components/solicitudes/vistas'
import { Aviso, Banner, Button, EstadoBadge, ModalConfirmacion, Pestanias, type Pestania } from '@/components/ui'
import { BANNER_PERSONAL, ESTADOS_DE_ACCION, ESTADOS_DE_PAGO, VARIANTE_POR_ESTADO } from '@/constants/estados'
import { ROUTES } from '@/constants/routes'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { ApiError } from '@/services/api/ApiError'
import { solicitudes } from '@/services/solicitudes'
import { useAuth } from '@/store/authContext'
import type { SolicitudDetalle } from '@/types/solicitud'
import { mensajeDeError } from '@/utils/errores'
import { inicialesDeNombre } from '@/utils/personas'
import { MENSAJES } from '@/validations/messages'

const ORIGEN = { texto: 'Solicitudes de equivalencia', ruta: ROUTES.MIS_SOLICITUDES }

const PESTANIAS: Pestania[] = [
  { clave: 'analisis', texto: 'Análisis de solicitud', Icono: FileText },
  { clave: 'detalles', texto: 'Detalles', Icono: Info },
  { clave: 'chat', texto: 'Chat', Icono: MessageCircle },
  { clave: 'historial', texto: 'Historial', Icono: History },
]

interface AvisoPagina {
  tipo: 'exito' | 'error'
  mensaje: string
}

function Dato({ etiqueta, valor, detalle }: { etiqueta: string; valor: string; detalle?: string }) {
  return (
    <div className="lg:border-l lg:border-border lg:pl-6">
      <dt className="text-base font-medium text-primary">{etiqueta}</dt>
      <dd className="text-base text-text">
        {valor}
        {detalle && <span className="block text-text-muted">{detalle}</span>}
      </dd>
    </div>
  )
}

function Solicitud({ id }: { id: number }) {
  const { sesion } = useAuth()
  const { datos: detalle, cargando, error, reintentar, setDatos } = useCarga((signal) => solicitudes.obtener(id, signal))
  const [aviso, setAviso] = useState<AvisoPagina | null>(null)
  /** `null` mientras no se elige una: se abre la que le corresponde al estado. */
  const [activa, setActiva] = useState<string | null>(null)
  const [chatLeido, setChatLeido] = useState(false)
  const [modal, setModal] = useState<'tomar' | 'baja' | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [errorModal, setErrorModal] = useState<string | null>(null)
  const rol = sesion?.rol ?? ''

  /** El aviso va arriba de todo: se sube la página para que se vea junto con la vista nueva. */
  function actualizar(nuevo: SolicitudDetalle, mensaje: string) {
    setAviso({ tipo: 'exito', mensaje })
    if (nuevo.estado.codigo !== detalle?.estado.codigo) setActiva(null)
    setChatLeido(false)
    setDatos(nuevo)
    window.scrollTo(0, 0)
  }

  function recargar(mensaje: string = MENSAJES.ESTADO_CAMBIO) {
    setAviso({ tipo: 'error', mensaje })
    setModal(null)
    setChatLeido(false)
    reintentar()
    window.scrollTo(0, 0)
  }

  function abrir(cual: 'tomar' | 'baja') {
    setErrorModal(null)
    setModal(cual)
  }

  /** Tomar el proceso o darse de baja: el estado no cambia, pero otro pudo haber llegado antes. */
  async function cambiarResponsable(accion: (id: number) => Promise<SolicitudDetalle>, mensaje: string) {
    setProcesando(true)
    setErrorModal(null)
    try {
      actualizar(await accion(id), mensaje)
      setModal(null)
    } catch (causa) {
      const codigo = causa instanceof ApiError ? causa.body?.code : undefined
      if (codigo === 'UNICO_RESPONSABLE') setErrorModal(MENSAJES.UNICO_RESPONSABLE)
      else if (causa instanceof ApiError && (causa.kind === 'conflict' || causa.kind === 'forbidden')) {
        recargar(codigo === 'PROCESO_YA_TOMADO' ? MENSAJES.PROCESO_YA_TOMADO : undefined)
      } else setErrorModal(mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  function cuerpo(detalle: SolicitudDetalle) {
    const { aspirante, datosOrigen, estado, accionesDisponibles } = detalle
    const miResponsable = rol === 'SECRE' ? detalle.responsableAdministrativo : detalle.responsableAcademico
    const soyResponsable = miResponsable?.id === sesion?.id
    const observador = detalle.miRelacion === 'OBSERVADOR'
    const puedeTomar = accionesDisponibles.includes('TOMAR_PROCESO')
    const puedeDarseDeBaja = accionesDisponibles.includes('DARSE_DE_BAJA')
    const meToca = soyResponsable && Boolean(ESTADOS_DE_ACCION[rol]?.includes(estado.codigo))
    const Panel = VISTAS_PERSONAL[estado.codigo]
    const AccionEnBanner = ACCIONES_EN_BANNER_PERSONAL[estado.codigo]
    const pestania = activa ?? (ESTADOS_CON_CHAT_ABIERTO.includes(estado.codigo) ? 'chat' : PESTANIAS[0].clave)
    const pestanias = PESTANIAS.map((item) =>
      item.clave === 'chat' ? { ...item, contador: chatLeido ? 0 : detalle.mensajesNoLeidos } : item,
    )
    // Una revisión con observación previa es una recarga: los documentos nuevos llegan pendientes (§5.6.2).
    const texto =
      rol === 'SECRE' && !estado.esTerminal && !ESTADOS_DE_PAGO.includes(estado.codigo)
        ? 'En etapa académica.'
        : estado.codigo === 'DOC-REV' && detalle.ultimaObservacion
          ? 'El aspirante recargó documentos: los nuevos figuran pendientes de revisión.'
          : BANNER_PERSONAL[estado.codigo]

    return (
      <>
        <section className="flex flex-col gap-4 rounded-lg bg-surface p-4 shadow-md lg:flex-row lg:items-center lg:gap-6">
          <span
            aria-hidden
            className="flex size-(--size-privado-indicador) shrink-0 items-center justify-center rounded-full bg-secondary text-2xl font-semibold text-primary"
          >
            {inicialesDeNombre(aspirante.nombre)}
          </span>
          <div className="min-w-0">
            <h2 className="text-2xl">{aspirante.nombre}</h2>
            <p className="text-base text-text-muted">DNI {aspirante.dni}</p>
            <a href={`mailto:${aspirante.email}`} className="flex items-center gap-2 truncate text-base text-primary">
              <Mail aria-hidden className="size-(--size-icon-sm) shrink-0" />
              {aspirante.email}
            </a>
          </div>
          <dl className="grid flex-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 lg:gap-6">
            <Dato etiqueta="Carrera de origen" valor={datosOrigen.carrera} detalle={datosOrigen.universidad} />
            <Dato etiqueta="Carrera solicitada" valor={detalle.carreraDestino.nombre} />
            <Dato etiqueta="Responsable académico" valor={detalle.responsableAcademico?.nombre ?? 'Sin asignar'} />
            <Dato
              etiqueta="Responsable administrativo"
              valor={detalle.responsableAdministrativo?.nombre ?? 'Sin asignar'}
            />
          </dl>
          {soyResponsable ? (
            <Button
              variante="outline"
              tamanio="lg"
              disabled={!puedeDarseDeBaja}
              title={puedeDarseDeBaja ? undefined : MENSAJES.UNICO_RESPONSABLE}
              onClick={() => abrir('baja')}
            >
              Darse de baja
            </Button>
          ) : (
            !miResponsable &&
            !estado.esTerminal && (
              <Button
                tamanio="lg"
                disabled={!puedeTomar}
                title={puedeTomar ? undefined : MENSAJES.SIN_PERMISO_DE_PROCESO}
                onClick={() => abrir('tomar')}
              >
                Tomar proceso
              </Button>
            )
          )}
        </section>

        <Banner
          variante={
            estado.esTerminal ? (VARIANTE_POR_ESTADO[estado.codigo] ?? 'neutro') : meToca || puedeTomar ? 'accion' : 'espera'
          }
          titulo={estado.nombre}
          etiqueta={
            estado.esTerminal
              ? 'Trámite cerrado'
              : observador
                ? 'Solo lectura'
                : puedeTomar
                  ? 'Sin responsable'
                  : meToca
                    ? 'Te toca a vos'
                    : 'En espera'
          }
        >
          {texto && <p>{texto}</p>}
          {observador && <p>{MENSAJES.SIN_PERMISO_DE_PROCESO} La ves en modo lectura.</p>}
          {puedeTomar && <p>Todavía nadie tomó este proceso: tomalo para poder actuar.</p>}
          {!observador && miResponsable && !soyResponsable && <p>El proceso lo lleva {miResponsable.nombre}.</p>}
          {AccionEnBanner && (
            <AccionEnBanner detalle={detalle} onActualizada={actualizar} onEstadoCambio={() => recargar()} />
          )}
        </Banner>

        <div>
          <Pestanias pestanias={pestanias} activa={pestania} onCambiar={setActiva} />
          {/* El primer bloque de cada pestaña se une a la barra de pestañas: sin las puntas de arriba redondeadas. */}
          <div
            role="tabpanel"
            aria-labelledby={`pestania-${pestania}`}
            className="flex flex-col gap-4 *:first:rounded-t-none"
          >
            {pestania === 'analisis' && (
              <>
                {Panel && (
                  <Panel
                    key={`panel-${estado.codigo}`}
                    detalle={detalle}
                    onActualizada={actualizar}
                    onEstadoCambio={() => recargar()}
                  />
                )}
                <RevisionDocumentos
                  key={estado.codigo}
                  detalle={detalle}
                  onActualizada={actualizar}
                  onEstadoCambio={() => recargar()}
                />
                <DocumentacionAcademica key={`academica-${detalle.fechaUltimaMod}`} soloSiHay solicitudId={detalle.id} />
              </>
            )}
            {pestania === 'detalles' && <PanelDetalles completo detalle={detalle} />}
            {pestania === 'chat' && (
              <Chat detalle={detalle} onEstadoCambio={() => recargar()} onLeidos={() => setChatLeido(true)} />
            )}
            {pestania === 'historial' && <PanelHistorial key={detalle.fechaUltimaMod} solicitudId={detalle.id} />}
          </div>
        </div>

        <ModalConfirmacion
          abierto={modal === 'tomar'}
          titulo="Tomar el proceso"
          accion={`Solicitud ${detalle.codigoExp} de ${aspirante.nombre}.`}
          consecuencia="Sos responsable de este proceso a partir de ahora."
          textoConfirmar="Confirmar"
          cargando={procesando}
          error={errorModal}
          onConfirmar={() => cambiarResponsable(acciones.tomarProceso, 'Tomaste el proceso: ya sos responsable de esta solicitud.')}
          onCancelar={() => setModal(null)}
        />
        <ModalConfirmacion
          peligrosa
          abierto={modal === 'baja'}
          titulo="Darse de baja"
          accion={`¿Querés darte de baja de la solicitud ${detalle.codigoExp}?`}
          consecuencia="El proceso vuelve a estar disponible para que lo tome otra persona con acceso a la carrera."
          textoConfirmar="Darme de baja"
          cargando={procesando}
          error={errorModal}
          onConfirmar={() => cambiarResponsable(acciones.darseDeBaja, 'Te diste de baja. El proceso volvió a estar disponible.')}
          onCancelar={() => setModal(null)}
        />
      </>
    )
  }

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo={`Solicitud ${detalle?.codigoExp ?? ''}`}
        descripcion="Revisá la documentación presentada por el aspirante y el avance del trámite."
        migas={{ origen: ORIGEN, actual: detalle?.codigoExp ?? 'Solicitud' }}
      >
        {detalle && <EstadoBadge estado={detalle.estado} />}
      </EncabezadoPagina>

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        {aviso && <Aviso tipo={aviso.tipo} mensaje={aviso.mensaje} />}
        {error ? (
          <div className="rounded-lg bg-surface p-8 text-center shadow-md">
            <p className="text-xl text-danger">{error}</p>
            <Button variante="outline" className="mt-4" onClick={reintentar}>
              Reintentar
            </Button>
          </div>
        ) : cargando || !detalle ? (
          <div aria-busy className="flex flex-col gap-4">
            <span className="block h-(--size-tabla-fila) animate-pulse rounded-lg bg-surface-muted" />
            <span className="block h-(--size-privado-foto-alto) animate-pulse rounded-lg bg-surface-muted" />
          </div>
        ) : (
          cuerpo(detalle)
        )}
      </div>
    </main>
  )
}

/** Detalle de una solicitud para el Académico y Secretaría (frontend-interfaces.md §5.3, §5.4, §5.10 y §6.3). */
export function SolicitudPersonalPage() {
  const { id } = useParams()
  return <Solicitud key={id} id={Number(id)} />
}
