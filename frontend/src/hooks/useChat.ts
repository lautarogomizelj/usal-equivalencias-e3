import { useEffect, useRef, useState } from 'react'
import { CHAT } from '@/constants/chat'
import { chat } from '@/services/chat'
import type { Mensaje } from '@/types/chat'
import { mensajeDeError } from '@/utils/errores'

/**
 * Mensajes del chat de una solicitud, actualizados cada 15 segundos mientras está abierto (contrato de la API
 * §8.1). `marcaLeidos` vale para quien participa del chat: al recibir mensajes de otros avisa que los leyó.
 */
export function useChat(solicitudId: number, activo: boolean, marcaLeidos: boolean, onLeidos: () => void) {
  const [mensajes, setMensajes] = useState<Mensaje[]>([])
  const [cargando, setCargando] = useState(activo)
  const [error, setError] = useState<string | null>(null)
  const [intento, setIntento] = useState(0)
  /** Lo mismo que `mensajes`, para leerlo desde el pedido periódico sin volver a armarlo. */
  const lista = useRef<Mensaje[]>([])

  function guardar(nuevos: Mensaje[]) {
    lista.current = nuevos
    setMensajes(nuevos)
  }

  useEffect(() => {
    if (!activo) return
    const control = new AbortController()
    let pidiendo = false

    async function traerNuevos() {
      if (pidiendo) return
      pidiendo = true
      try {
        // Se vuelve a pedir desde el primer mensaje propio sin ver: así se entera de cuándo lo leyeron.
        const sinVer = lista.current.find((mensaje) => mensaje.esMio && !mensaje.visto)
        const desde = sinVer ? sinVer.id - 1 : (lista.current.at(-1)?.id ?? 0)
        const traidos: Mensaje[] = []
        let tanda: Mensaje[]
        do {
          tanda = (await chat.mensajes(solicitudId, traidos.at(-1)?.id ?? desde, control.signal)).content
          traidos.push(...tanda)
        } while (tanda.length === CHAT.TANDA)
        if (control.signal.aborted) return
        const ultimoConocido = lista.current.at(-1)?.id ?? 0
        const ultimoTraido = traidos.at(-1)?.id ?? desde
        // Lo enviado mientras el pedido estaba en viaje ya está en la lista y no vino en la respuesta: se conserva.
        guardar([
          ...lista.current.filter((mensaje) => mensaje.id <= desde),
          ...traidos,
          ...lista.current.filter((mensaje) => mensaje.id > ultimoTraido),
        ])
        setError(null)
        if (marcaLeidos && traidos.some((mensaje) => !mensaje.esMio && mensaje.id > ultimoConocido)) {
          await chat.marcarLeidos(solicitudId, traidos.at(-1)!.id)
          onLeidos()
        }
      } catch (causa) {
        // Si ya hay mensajes en pantalla, un pedido periódico que falla no los tapa: se reintenta solo.
        if (!control.signal.aborted && !lista.current.length) setError(mensajeDeError(causa))
      } finally {
        pidiendo = false
        if (!control.signal.aborted) setCargando(false)
      }
    }

    void traerNuevos()
    // Con la pestaña del navegador en segundo plano no hace falta: al volver, el próximo pedido trae todo.
    const intervalo = setInterval(() => {
      if (!document.hidden) void traerNuevos()
    }, CHAT.INTERVALO_MS)
    return () => {
      control.abort()
      clearInterval(intervalo)
    }
    // `onLeidos` se crea de nuevo en cada render: el chat depende solo de la solicitud y del reintento.
    // oxlint-disable-next-line react-hooks/exhaustive-deps
  }, [solicitudId, activo, marcaLeidos, intento])

  function reintentar() {
    setCargando(true)
    setError(null)
    setIntento((actual) => actual + 1)
  }

  /** Suma el mensaje recién enviado sin esperar al próximo pedido. */
  function agregar(mensaje: Mensaje) {
    guardar([...lista.current.filter(({ id }) => id !== mensaje.id), mensaje])
  }

  return { mensajes, cargando, error, reintentar, agregar }
}
