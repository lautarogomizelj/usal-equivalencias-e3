import { Bell } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { generatePath, useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { notificaciones } from '@/services/notificaciones'
import type { Notificacion } from '@/types/notificacion'
import { formatearFechaHora } from '@/utils/fechas'

const SONDEO_MS = 60_000
const CANTIDAD_EN_LISTA = 10

/**
 * Campana de la barra superior (frontend-interfaces.md §2.1 y §4.9): replica los avisos que salen por mail.
 * El contador se actualiza solo cada minuto; la lista se pide al abrirla.
 */
export function Campana() {
  const navigate = useNavigate()
  const contenedor = useRef<HTMLDivElement>(null)
  const [abierta, setAbierta] = useState(false)
  const [cantidad, setCantidad] = useState(0)
  const [lista, setLista] = useState<Notificacion[] | null>(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    const control = new AbortController()
    // Si el contador falla no se avisa: la campana es un atajo y el trámite no depende de ella.
    const pedir = () =>
      notificaciones
        .cantidadNoLeidas(control.signal)
        .then((respuesta) => setCantidad(respuesta.cantidad))
        .catch(() => undefined)
    void pedir()
    const intervalo = setInterval(pedir, SONDEO_MS)
    return () => {
      control.abort()
      clearInterval(intervalo)
    }
  }, [])

  useEffect(() => {
    if (!abierta) return
    const cerrarSiEsAfuera = (evento: MouseEvent) => {
      if (!contenedor.current?.contains(evento.target as Node)) setAbierta(false)
    }
    const cerrarConEscape = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') setAbierta(false)
    }
    document.addEventListener('mousedown', cerrarSiEsAfuera)
    document.addEventListener('keydown', cerrarConEscape)
    return () => {
      document.removeEventListener('mousedown', cerrarSiEsAfuera)
      document.removeEventListener('keydown', cerrarConEscape)
    }
  }, [abierta])

  function alternar() {
    setAbierta(!abierta)
    if (abierta) return
    setLista(null)
    setError(false)
    notificaciones
      .listar(CANTIDAD_EN_LISTA)
      .then(({ content }) => setLista(content))
      .catch(() => setError(true))
  }

  function abrir(aviso: Notificacion) {
    if (!aviso.leida) {
      setCantidad((actual) => Math.max(0, actual - 1))
      void notificaciones.marcarLeida(aviso.id).catch(() => undefined)
    }
    setAbierta(false)
    if (aviso.solicitudId) navigate(generatePath(ROUTES.SOLICITUD, { id: String(aviso.solicitudId) }))
  }

  function marcarTodas() {
    setCantidad(0)
    setLista((actual) => actual?.map((aviso) => ({ ...aviso, leida: true })) ?? null)
    void notificaciones.marcarTodasLeidas().catch(() => undefined)
  }

  return (
    <div ref={contenedor} className="relative">
      <button
        type="button"
        aria-label={cantidad ? `Notificaciones: ${cantidad} sin leer` : 'Notificaciones'}
        aria-expanded={abierta}
        className="relative flex size-(--size-circulo-icono) items-center justify-center rounded-full text-primary transition-colors hover:bg-secondary"
        onClick={alternar}
      >
        <Bell aria-hidden className="size-(--size-icon-lg)" />
        {cantidad > 0 && (
          <span
            aria-hidden
            className="absolute top-0 right-0 min-w-5 rounded-full bg-danger px-1 text-center text-xs font-semibold text-primary-contrast"
          >
            {cantidad > 99 ? '99+' : cantidad}
          </span>
        )}
      </button>

      {abierta && (
        <section
          aria-label="Notificaciones"
          className="absolute top-full right-0 z-10 mt-2 w-72 rounded-lg border border-border bg-surface shadow-lg sm:w-96"
        >
          <header className="flex items-center justify-between gap-3 border-b border-border px-4 py-3">
            <h2 className="text-xl">Notificaciones</h2>
            {cantidad > 0 && (
              <button type="button" className="text-sm text-primary underline" onClick={marcarTodas}>
                Marcar todas como leídas
              </button>
            )}
          </header>
          {error ? (
            <p role="alert" className="p-4 text-base text-danger">
              No pudimos cargar tus notificaciones.
            </p>
          ) : !lista ? (
            <p aria-busy className="p-4 text-base text-text-muted">
              Cargando…
            </p>
          ) : lista.length === 0 ? (
            <p className="p-4 text-base text-text-muted">No tenés notificaciones.</p>
          ) : (
            <ul className="max-h-96 overflow-y-auto">
              {lista.map((aviso) => (
                <li key={aviso.id} className="border-b border-border last:border-b-0">
                  <button
                    type="button"
                    className={`block w-full px-4 py-3 text-left transition-colors hover:bg-secondary ${aviso.leida ? '' : 'bg-surface-muted'}`}
                    onClick={() => abrir(aviso)}
                  >
                    <span className={`block text-base text-heading ${aviso.leida ? '' : 'font-semibold'}`}>
                      {aviso.titulo}
                    </span>
                    <span className="block text-sm text-text">{aviso.mensaje}</span>
                    <span className="block text-xs text-text-muted">{formatearFechaHora(aviso.fechaCreacion)}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  )
}
