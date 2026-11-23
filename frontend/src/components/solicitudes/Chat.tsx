import { CheckCheck, CircleAlert, MessageCircle, Paperclip, Send, X } from 'lucide-react'
import { Fragment, useEffect, useRef, useState, type FormEvent } from 'react'
import { Button, Input, Seccion, Select } from '@/components/ui'
import { CHAT, NOMBRE_TIPO_CONSULTA, ROL_EN_CHAT, TIPOS_CONSULTA } from '@/constants/chat'
import { useAccion } from '@/hooks/useAccion'
import { useChat } from '@/hooks/useChat'
import { chat } from '@/services/chat'
import { solicitudes } from '@/services/solicitudes'
import type { Mensaje, TipoConsulta } from '@/types/chat'
import type { SolicitudDetalle } from '@/types/solicitud'
import { formatearTamanio } from '@/utils/archivos'
import { formatearDia, formatearHora } from '@/utils/fechas'
import { inicialesDeNombre } from '@/utils/personas'

const ADJUNTO_FORMATO = 'Formato no permitido. Solo se aceptan archivos PDF, JPG o PNG.'
const ADJUNTO_TAMANIO = 'El adjunto supera el máximo de 5 MB.'

function Burbuja({ mensaje }: { mensaje: Mensaje }) {
  const { autor, contenido, adjunto, tipoConsulta, fechaEnvio, esMio, visto } = mensaje

  return (
    <li className={`flex items-start gap-3 ${esMio ? 'flex-row-reverse' : ''}`}>
      <span
        aria-hidden
        className="flex size-(--size-button-md) shrink-0 items-center justify-center rounded-full bg-secondary text-sm font-semibold text-primary"
      >
        {inicialesDeNombre(autor.nombre)}
      </span>
      <div className={`flex max-w-3/4 min-w-0 flex-col gap-1 ${esMio ? 'items-end' : 'items-start'}`}>
        <p className="text-sm text-text-muted">
          <span className="font-semibold text-text">{autor.nombre}</span> {ROL_EN_CHAT[autor.rol]}
        </p>
        <div className={`rounded-lg px-4 py-2 text-base text-text ${esMio ? 'bg-secondary' : 'bg-surface-muted'}`}>
          {contenido && <p className="wrap-anywhere whitespace-pre-line">{contenido}</p>}
          {adjunto && (
            <a
              href={solicitudes.urlArchivo(adjunto.urlDescarga)}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 text-primary underline"
            >
              <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0" />
              {adjunto.nombre}
            </a>
          )}
          <p className="mt-1 flex items-center justify-end gap-2 text-xs text-text-muted">
            {NOMBRE_TIPO_CONSULTA[tipoConsulta]} · {formatearHora(fechaEnvio)}
            {esMio && (
              <CheckCheck
                role="img"
                aria-label={visto ? 'Visto' : 'Enviado'}
                className={`size-(--size-icon-sm) shrink-0 ${visto ? 'text-info' : 'text-text-disabled'}`}
              />
            )}
          </p>
        </div>
      </div>
    </li>
  )
}

interface ChatProps {
  detalle: SolicitudDetalle
  /** El backend rechazó el envío porque la solicitud cambió: la página la recarga y avisa (§0.16.4). */
  onEstadoCambio: () => void
  /** Quien mira leyó los mensajes pendientes: la pestaña deja de mostrar el contador. */
  onLeidos: () => void
}

/**
 * Chat de la solicitud (frontend-interfaces.md §2.5, §4.7.2 y §5.8): escriben el aspirante y los responsables, y
 * el resto lo ve en modo lectura. Sin responsable todavía no está habilitado (RF-MSG-001).
 */
export function Chat({ detalle, onEstadoCambio, onLeidos }: ChatProps) {
  const { id, chatHabilitado, puedeEscribirChat, miRelacion, estado } = detalle
  // Con el trámite cerrado ya no escribe, pero si le quedaron mensajes sin leer también los marca al abrirlo.
  const participa = puedeEscribirChat || detalle.mensajesNoLeidos > 0
  const { mensajes, cargando, error, reintentar, agregar } = useChat(id, chatHabilitado, participa, onLeidos)
  const [texto, setTexto] = useState('')
  const [tipoConsulta, setTipoConsulta] = useState<TipoConsulta>('OTRO')
  const [adjunto, setAdjunto] = useState<File | null>(null)
  const envio = useAccion(onEstadoCambio)
  const conversacion = useRef<HTMLOListElement>(null)
  const ultimoId = mensajes.at(-1)?.id

  /** Lo último que se dijo queda siempre a la vista. */
  useEffect(() => {
    conversacion.current?.scrollTo({ top: conversacion.current.scrollHeight })
  }, [ultimoId])

  function elegirAdjunto(archivo: File) {
    if (!(CHAT.MIME_ADJUNTO as readonly string[]).includes(archivo.type)) return envio.setError(ADJUNTO_FORMATO)
    if (archivo.size > CHAT.MAX_BYTES_ADJUNTO) return envio.setError(ADJUNTO_TAMANIO)
    envio.setError(null)
    setAdjunto(archivo)
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!texto.trim() && !adjunto) return
    void envio.ejecutar(async () => {
      agregar(await chat.enviar(id, texto.trim(), tipoConsulta, adjunto))
      setTexto('')
      setAdjunto(null)
    })
  }

  const participantes = [detalle.aspirante, detalle.responsableAcademico, detalle.responsableAdministrativo]
    .filter((persona) => persona !== null)
    .map(({ nombre }) => nombre)
    .join(', ')

  return (
    <Seccion
      Icono={MessageCircle}
      titulo="Chat de la solicitud"
      descripcion={chatHabilitado ? `Participan: ${participantes}.` : undefined}
      className="rounded-t-none"
    >
      {!chatHabilitado ? (
        <p className="rounded-md bg-surface-muted px-4 py-6 text-center text-base text-text-muted">
          El chat se habilita cuando alguien tome {miRelacion === 'ASPIRANTE' ? 'tu' : 'el'} proceso.
        </p>
      ) : error ? (
        <div className="rounded-md border border-border px-4 py-6 text-center">
          <p className="text-base text-danger">{error}</p>
          <Button variante="outline" tamanio="sm" className="mt-3" onClick={reintentar}>
            Reintentar
          </Button>
        </div>
      ) : cargando ? (
        <span aria-busy className="block h-(--size-chat-vacio) animate-pulse rounded-md bg-surface-muted" />
      ) : (
        <>
          {mensajes.length ? (
            <ol
              ref={conversacion}
              aria-label="Mensajes"
              aria-live="polite"
              className="flex max-h-(--size-chat) flex-col gap-4 overflow-y-auto rounded-md border border-border p-4"
            >
              {mensajes.map((mensaje, indice) => {
                const dia = formatearDia(mensaje.fechaEnvio)
                const cambiaElDia = indice === 0 || dia !== formatearDia(mensajes[indice - 1].fechaEnvio)
                return (
                  <Fragment key={mensaje.id}>
                    {cambiaElDia && (
                      <li className="flex items-center gap-4 text-sm text-text-muted before:h-px before:flex-1 before:bg-border after:h-px after:flex-1 after:bg-border">
                        {dia}
                      </li>
                    )}
                    <Burbuja mensaje={mensaje} />
                  </Fragment>
                )
              })}
            </ol>
          ) : (
            <p className="flex min-h-(--size-chat-vacio) items-center justify-center rounded-md border border-border px-4 text-center text-base text-text-muted">
              {puedeEscribirChat ? 'Todavía no hay mensajes. Escribí el primero.' : 'Todavía no hay mensajes.'}
            </p>
          )}

          {puedeEscribirChat ? (
            <form className="mt-4 flex flex-col gap-2" onSubmit={enviar}>
              {adjunto && (
                <p className="flex items-center gap-2 text-sm text-text">
                  <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
                  <span className="min-w-0 truncate font-medium">{adjunto.name}</span>
                  <span className="shrink-0 text-text-muted">{formatearTamanio(adjunto.size)}</span>
                  <button
                    type="button"
                    aria-label="Quitar el adjunto"
                    className="text-text-muted hover:text-primary"
                    onClick={() => setAdjunto(null)}
                  >
                    <X aria-hidden className="size-(--size-icon-sm)" />
                  </button>
                </p>
              )}
              <div className="flex flex-wrap items-start gap-2">
                <label
                  title="Adjuntar un archivo: PDF, JPG o PNG de hasta 5 MB"
                  className="inline-flex size-(--size-button-md) shrink-0 cursor-pointer items-center justify-center rounded-md text-primary transition-colors hover:bg-secondary has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus"
                >
                  <Paperclip aria-hidden className="size-(--size-icon-md)" />
                  <input
                    type="file"
                    className="sr-only"
                    aria-label="Adjuntar un archivo"
                    accept={CHAT.EXTENSIONES_ADJUNTO.join(',')}
                    onChange={(evento) => {
                      const archivo = evento.target.files?.[0]
                      evento.target.value = ''
                      if (archivo) elegirAdjunto(archivo)
                    }}
                  />
                </label>
                <Select
                  etiquetaOculta
                  etiqueta="Tipo de consulta"
                  opciones={TIPOS_CONSULTA}
                  value={tipoConsulta}
                  onChange={(evento) => setTipoConsulta(evento.target.value as TipoConsulta)}
                />
                <div className="min-w-0 flex-1 basis-1/2">
                  <Input
                    etiquetaOculta
                    etiqueta="Mensaje"
                    placeholder="Escribí un mensaje…"
                    autoComplete="off"
                    maxLength={CHAT.MAX_CONTENIDO}
                    value={texto}
                    onChange={(evento) => setTexto(evento.target.value)}
                  />
                </div>
                <Button type="submit" aria-label="Enviar mensaje" disabled={envio.procesando || (!texto.trim() && !adjunto)}>
                  <Send aria-hidden className="size-(--size-icon-md)" />
                </Button>
              </div>
              {envio.error && (
                <p role="alert" className="flex items-center gap-1 text-sm text-danger">
                  <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                  {envio.error}
                </p>
              )}
            </form>
          ) : (
            <p className="mt-4 rounded-md bg-surface-muted px-4 py-3 text-center text-base text-text-muted">
              {estado.esTerminal
                ? 'El trámite está cerrado: el chat queda en modo lectura.'
                : 'Solo el aspirante y sus responsables pueden escribir. Lo ves en modo lectura.'}
            </p>
          )}
        </>
      )}
    </Seccion>
  )
}
