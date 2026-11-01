import { Mail } from 'lucide-react'
import { useEffect, useState } from 'react'
import {
  Button,
  EstadoBadge,
  Input,
  ModalConfirmacion,
  Paginador,
  Select,
  Tabla,
  Textarea,
  type ColumnaTabla,
} from '@/components/ui'
import { catalogos } from '@/services/catalogos'
import type { Carrera } from '@/types/carrera'
import type { Estado } from '@/types/estado'

const COLUMNAS: ColumnaTabla<Estado>[] = [
  { clave: 'codigo', titulo: 'Código', render: ({ codigo }) => codigo },
  { clave: 'estado', titulo: 'Estado actual', render: (estado) => <EstadoBadge estado={estado} /> },
]

/** Catálogo de componentes base. La ruta solo existe en desarrollo. */
export function ComponentesPage() {
  const [estados, setEstados] = useState<Estado[]>([])
  const [carreras, setCarreras] = useState<Carrera[]>([])
  const [cargando, setCargando] = useState(true)
  const [observaciones, setObservaciones] = useState('')
  const [modalAbierto, setModalAbierto] = useState(false)
  const [pagina, setPagina] = useState(0)

  useEffect(() => {
    const control = new AbortController()
    Promise.all([catalogos.estados(control.signal), catalogos.carreras(control.signal)])
      .then(([listaEstados, listaCarreras]) => {
        setEstados(listaEstados)
        setCarreras(listaCarreras)
        setCargando(false)
      })
      .catch(() => setCargando(false))
    return () => control.abort()
  }, [])

  return (
    <section className="flex w-full max-w-(--size-content) flex-col gap-8">
      <h1 className="text-5xl">Componentes base</h1>

      <div className="flex flex-wrap gap-3">
        <Button>Iniciar sesión</Button>
        <Button variante="outline">Ver solicitud</Button>
        <Button variante="secondary">Guardar borrador</Button>
        <Button variante="danger">Rechazar</Button>
        <Button disabled>Deshabilitado</Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Input etiqueta="Email" placeholder="nombre@ejemplo.com" Icono={Mail} required />
        <Input etiqueta="Contraseña" type="password" placeholder="Tu contraseña" required />
        <Input etiqueta="DNI" defaultValue="abc" error="Ingresá un DNI válido (7 u 8 dígitos, sin puntos)." />
        <Select
          etiqueta="Carrera destino"
          textoVacio="Seleccione una carrera"
          opciones={carreras.map(({ codigo, nombre }) => ({ valor: codigo, texto: nombre }))}
          required
        />
        <Textarea
          etiqueta="Observaciones"
          placeholder="Ingrese información adicional que considere relevante..."
          maxLength={500}
          value={observaciones}
          onChange={(evento) => setObservaciones(evento.target.value)}
        />
      </div>

      <Button variante="outline" className="self-start" onClick={() => setModalAbierto(true)}>
        Tomar proceso
      </Button>
      <ModalConfirmacion
        abierto={modalAbierto}
        titulo="Tomar proceso"
        accion="Vas a tomar la solicitud EXP-2026-001."
        consecuencia="Sos responsable de este proceso a partir de ahora."
        textoConfirmar="Confirmar"
        onConfirmar={() => setModalAbierto(false)}
        onCancelar={() => setModalAbierto(false)}
      />

      <Tabla
        descripcion="Estados del trámite"
        columnas={COLUMNAS}
        filas={estados}
        obtenerClave={({ codigo }) => codigo}
        cargando={cargando}
      />
      <Paginador
        pagina={{ page: pagina, size: 20, totalElements: 45, totalPages: 3 }}
        onCambiar={setPagina}
      />
      <Tabla
        descripcion="Tabla sin resultados"
        columnas={COLUMNAS}
        filas={[]}
        obtenerClave={({ codigo }) => codigo}
        textoVacio="Todavía no iniciaste ninguna solicitud."
      />
      <Tabla
        descripcion="Tabla con error"
        columnas={COLUMNAS}
        filas={[]}
        obtenerClave={({ codigo }) => codigo}
        error="No se pudo conectar con el servidor. Revisá tu conexión."
        onReintentar={() => undefined}
      />
    </section>
  )
}
