import { useState } from 'react'
import { TablaAuditoria } from '@/components/auditoria/TablaAuditoria'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Button, Input, Select } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { usePagina } from '@/hooks/usePagina'
import { auditoria } from '@/services/auditoria'
import type { FiltrosAuditoria } from '@/types/auditoria'
import { textoDeCodigo } from '@/utils/auditoria'

const SIN_FILTROS: Required<Omit<FiltrosAuditoria, 'size'>> = { usuario: '', accion: '', entidad: '', desde: '', hasta: '' }

/** Auditoría general del sistema, para Admin y Revisor (frontend-interfaces.md §7.15 y §8.4, RF-AUDIT-003). */
export function AuditoriaPage() {
  const [filtros, setFiltros] = useState(SIN_FILTROS)
  const [aplicados, setAplicados] = useState(SIN_FILTROS)
  const opciones = useCarga((signal) => auditoria.opciones(signal))
  const { pagina, cargando, error, irAPagina, reintentar } = usePagina(
    (page, signal) => auditoria.listar(page, aplicados, signal),
    JSON.stringify(aplicados),
  )
  const cambiar = (cambios: Partial<typeof SIN_FILTROS>) => setFiltros({ ...filtros, ...cambios })
  const aOpciones = (codigos: string[] = []) => codigos.map((codigo) => ({ valor: codigo, texto: textoDeCodigo(codigo) }))

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Auditoría del sistema"
        descripcion="Registro de las acciones realizadas en el sistema. Los estados se muestran por su código."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        <form
          className="grid items-end gap-4 rounded-lg bg-surface p-4 shadow-md md:grid-cols-2 lg:grid-cols-6"
          onSubmit={(evento) => {
            evento.preventDefault()
            setAplicados({ ...filtros, usuario: filtros.usuario.trim() })
          }}
        >
          <Input
            etiqueta="Usuario"
            placeholder="Email o parte del email"
            value={filtros.usuario}
            onChange={(evento) => cambiar({ usuario: evento.target.value })}
          />
          <Select
            etiqueta="Acción"
            textoVacio="Todas"
            opciones={aOpciones(opciones.datos?.acciones)}
            value={filtros.accion}
            onChange={(evento) => cambiar({ accion: evento.target.value })}
          />
          <Select
            etiqueta="Entidad"
            textoVacio="Todas"
            opciones={aOpciones(opciones.datos?.entidades)}
            value={filtros.entidad}
            onChange={(evento) => cambiar({ entidad: evento.target.value })}
          />
          <Input
            etiqueta="Desde"
            type="date"
            max={filtros.hasta || undefined}
            value={filtros.desde}
            onChange={(evento) => cambiar({ desde: evento.target.value })}
          />
          <Input
            etiqueta="Hasta"
            type="date"
            min={filtros.desde || undefined}
            value={filtros.hasta}
            onChange={(evento) => cambiar({ hasta: evento.target.value })}
          />
          <div className="flex gap-3">
            <Button type="submit">Filtrar</Button>
            <Button
              variante="ghost"
              onClick={() => {
                setFiltros(SIN_FILTROS)
                setAplicados(SIN_FILTROS)
              }}
            >
              Limpiar
            </Button>
          </div>
        </form>

        <TablaAuditoria
          pagina={pagina}
          cargando={cargando}
          error={error}
          onCambiarPagina={irAPagina}
          onReintentar={reintentar}
        />
      </div>
    </main>
  )
}
