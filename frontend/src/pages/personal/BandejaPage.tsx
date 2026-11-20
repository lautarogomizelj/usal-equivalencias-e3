import { Filter, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { TablaBandeja } from '@/components/solicitudes/TablaBandeja'
import { Button, Input, Select } from '@/components/ui'
import { filtrosDeVista, VISTAS_BANDEJA } from '@/constants/bandeja'
import { useCarga } from '@/hooks/useCarga'
import { useSolicitudes } from '@/hooks/useSolicitudes'
import { catalogos } from '@/services/catalogos'
import { useAuth } from '@/store/authContext'

const DEMORA_BUSQUEDA_MS = 350
const FILTROS_DEL_PANEL = ['vista', 'estado', 'desde', 'hasta']

/**
 * Bandeja de solicitudes del Académico y de Secretaría (frontend-interfaces.md §5.2 y §6.2, RF-PROC-001).
 * Los filtros viven en la dirección, así se llega con uno aplicado desde el panel de inicio.
 */
export function BandejaPage() {
  const { sesion } = useAuth()
  const rol = sesion?.rol ?? ''
  const [parametros, setParametros] = useSearchParams()
  const vista = parametros.get('vista') ?? 'todas'
  const estado = parametros.get('estado') ?? ''
  const desde = parametros.get('desde') ?? ''
  const hasta = parametros.get('hasta') ?? ''
  const q = parametros.get('q') ?? ''
  const [texto, setTexto] = useState(q)
  const [panelAbierto, setPanelAbierto] = useState(FILTROS_DEL_PANEL.some((clave) => parametros.has(clave)))
  const estados = useCarga((signal) => catalogos.estados(signal))
  const deVista = filtrosDeVista(vista, rol)
  // Una vista que ya acota los estados ("Requieren mi acción") solo deja elegir entre los suyos.
  const estadoAplicado = !deVista.estado || deVista.estado.includes(estado) ? estado : ''
  const { pagina, cargando, error, irAPagina, reintentar } = useSolicitudes({
    ...deVista,
    ...(estadoAplicado ? { estado: [estadoAplicado] } : {}),
    q,
    desde,
    hasta,
  })

  /** Parte de la dirección del momento y no de la del render: la búsqueda se aplica con demora. */
  function cambiar(cambios: Record<string, string>) {
    const nuevos = new URLSearchParams(window.location.search)
    for (const [clave, valor] of Object.entries(cambios)) {
      if (valor) nuevos.set(clave, valor)
      else nuevos.delete(clave)
    }
    setParametros(nuevos, { replace: true })
  }

  function cambiarVista(nueva: string) {
    const permitidos = filtrosDeVista(nueva, rol).estado
    cambiar({ vista: nueva === 'todas' ? '' : nueva, ...(permitidos && !permitidos.includes(estado) ? { estado: '' } : {}) })
  }

  useEffect(() => {
    if (texto.trim() === q) return
    const espera = setTimeout(() => cambiar({ q: texto.trim() }), DEMORA_BUSQUEDA_MS)
    return () => clearTimeout(espera)
    // La búsqueda se aplica un momento después de dejar de escribir: depende solo del texto.
    // oxlint-disable-next-line react-hooks/exhaustive-deps
  }, [texto])

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Solicitudes de equivalencia"
        descripcion="Visualizá las solicitudes pendientes de evaluación o con observaciones y completá la acción correspondiente."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        <div className="flex flex-wrap items-center gap-3">
          <div className="min-w-0 flex-1">
            <Input
              etiquetaOculta
              etiqueta="Buscar solicitudes"
              type="search"
              placeholder="Buscar por aspirante, N° de solicitud o carrera…"
              Icono={Search}
              value={texto}
              onChange={(evento) => setTexto(evento.target.value)}
            />
          </div>
          <Button variante="outline" aria-expanded={panelAbierto} onClick={() => setPanelAbierto(!panelAbierto)}>
            <Filter aria-hidden className="size-(--size-icon-sm)" />
            Filtrar
          </Button>
        </div>

        {panelAbierto && (
          <div className="grid items-end gap-4 rounded-lg bg-surface p-4 shadow-md md:grid-cols-2 lg:grid-cols-5">
            <Select
              etiqueta="Mostrar"
              opciones={VISTAS_BANDEJA}
              value={vista}
              onChange={(evento) => cambiarVista(evento.target.value)}
            />
            <Select
              etiqueta="Estado"
              textoVacio="Todos los estados"
              opciones={(estados.datos ?? [])
                .filter(({ codigo }) => !deVista.estado || deVista.estado.includes(codigo))
                .map(({ codigo, nombre }) => ({ valor: codigo, texto: nombre }))}
              value={estadoAplicado}
              onChange={(evento) => cambiar({ estado: evento.target.value })}
            />
            <Input
              etiqueta="Actualizadas desde"
              type="date"
              max={hasta || undefined}
              value={desde}
              onChange={(evento) => cambiar({ desde: evento.target.value })}
            />
            <Input
              etiqueta="Actualizadas hasta"
              type="date"
              min={desde || undefined}
              value={hasta}
              onChange={(evento) => cambiar({ hasta: evento.target.value })}
            />
            <Button
              variante="ghost"
              onClick={() => {
                setTexto('')
                setParametros({}, { replace: true })
              }}
            >
              Limpiar filtros
            </Button>
          </div>
        )}

        <TablaBandeja
          pagina={pagina}
          cargando={cargando}
          error={error}
          rol={rol}
          textoVacio="No hay solicitudes que coincidan con la búsqueda."
          onCambiarPagina={irAPagina}
          onReintentar={reintentar}
        />
      </div>
    </main>
  )
}
