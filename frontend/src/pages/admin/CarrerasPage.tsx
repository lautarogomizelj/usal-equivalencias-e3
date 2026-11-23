import { GraduationCap, Pencil, SquarePlus } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Aviso, Badge, Button, Input, Modal, ModalConfirmacion, Tabla, Textarea, type ColumnaTabla } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { admin } from '@/services/admin'
import type { DatosCarrera } from '@/types/admin'
import type { Carrera } from '@/types/carrera'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { requerido } from '@/validations'

const CARRERA_VACIA: DatosCarrera = { codigo: '', nombre: '', facultad: '', descripcion: '' }

type Abierto = { cual: 'nueva' } | { cual: 'editar' | 'activar'; carrera: Carrera }

/** Formulario de alta y de edición: los mismos cuatro campos (frontend-interfaces.md §7.9 y §7.10). */
function ModalCarrera({ carrera, onGuardada, onCerrar }: { carrera?: Carrera; onGuardada: (mensaje: string) => void; onCerrar: () => void }) {
  const [datos, setDatos] = useState<DatosCarrera>(
    carrera
      ? { codigo: carrera.codigo, nombre: carrera.nombre, facultad: carrera.facultad ?? '', descripcion: carrera.descripcion ?? '' }
      : CARRERA_VACIA,
  )
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const cambiar = (cambios: Partial<DatosCarrera>) => setDatos({ ...datos, ...cambios })

  async function guardar(evento: FormEvent) {
    evento.preventDefault()
    setErrorGeneral(null)
    const encontrados = {
      codigo: requerido(datos.codigo),
      nombre: requerido(datos.nombre),
      facultad: requerido(datos.facultad),
    }
    setErrores(encontrados)
    if (Object.values(encontrados).some(Boolean)) return
    setGuardando(true)
    try {
      if (carrera) await admin.editarCarrera(carrera.id, datos)
      else await admin.crearCarrera(datos)
      onGuardada(carrera ? 'Los datos de la carrera fueron actualizados.' : 'La carrera fue creada y ya se ofrece en las solicitudes nuevas.')
    } catch (causa) {
      const porCampo = erroresPorCampo(causa)
      setErrores(porCampo)
      if (!Object.keys(porCampo).length) setErrorGeneral(mensajeDeError(causa))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Modal abierto titulo={carrera ? 'Editar carrera' : 'Nueva carrera'} Icono={GraduationCap} onCerrar={onCerrar}>
      <form noValidate className="flex flex-col gap-4" onSubmit={guardar}>
        <div className="grid gap-4 sm:grid-cols-3">
          <Input
            required
            etiqueta="Código"
            placeholder="ING-INF-001"
            maxLength={50}
            value={datos.codigo}
            error={errores.codigo ?? undefined}
            onChange={(evento) => cambiar({ codigo: evento.target.value })}
          />
          <div className="sm:col-span-2">
            <Input
              required
              etiqueta="Nombre"
              maxLength={255}
              value={datos.nombre}
              error={errores.nombre ?? undefined}
              onChange={(evento) => cambiar({ nombre: evento.target.value })}
            />
          </div>
        </div>
        <Input
          required
          etiqueta="Facultad"
          maxLength={255}
          value={datos.facultad}
          error={errores.facultad ?? undefined}
          onChange={(evento) => cambiar({ facultad: evento.target.value })}
        />
        <Textarea
          etiqueta="Descripción (opcional)"
          maxLength={2000}
          value={datos.descripcion}
          error={errores.descripcion ?? undefined}
          onChange={(evento) => cambiar({ descripcion: evento.target.value })}
        />
        {errorGeneral && (
          <p role="alert" className="text-base text-danger">
            {errorGeneral}
          </p>
        )}
        <div className="flex flex-wrap justify-end gap-3">
          <Button variante="outline" disabled={guardando} onClick={onCerrar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={guardando}>
            {carrera ? 'Guardar cambios' : 'Crear carrera'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}

/** Catálogo de carreras del Admin (frontend-interfaces.md §7.8 a §7.10, RF-ADMIN-002): alta, edición y baja lógica. */
export function CarrerasPage() {
  const { datos, cargando, error, reintentar } = useCarga((signal) => admin.carreras(signal))
  const [abierto, setAbierto] = useState<Abierto | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [errorModal, setErrorModal] = useState<string | null>(null)
  const enModal = abierto && abierto.cual !== 'nueva' ? abierto.carrera : null

  function guardada(mensaje: string) {
    setAbierto(null)
    setAviso(mensaje)
    reintentar()
  }

  async function cambiarActivo(carrera: Carrera) {
    setProcesando(true)
    setErrorModal(null)
    try {
      await admin.activarCarrera(carrera.id, !carrera.activo)
      guardada(carrera.activo ? 'La carrera fue desactivada.' : 'La carrera fue activada.')
    } catch (causa) {
      setErrorModal(mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  const columnas: ColumnaTabla<Carrera>[] = [
    {
      clave: 'codigo',
      titulo: 'Código',
      ancho: 'lg:w-3/20',
      render: ({ codigo }) => <span className="whitespace-nowrap">{codigo}</span>,
    },
    {
      clave: 'nombre',
      titulo: 'Nombre',
      render: ({ nombre }) => <span className="font-medium text-primary">{nombre}</span>,
    },
    { clave: 'facultad', titulo: 'Facultad', ancho: 'lg:w-1/4', render: ({ facultad }) => facultad || '–' },
    {
      clave: 'estado',
      titulo: 'Estado',
      ancho: 'lg:w-3/20',
      render: ({ activo }) => <Badge variante={activo ? 'exito' : 'neutro'}>{activo ? 'Activa' : 'Inactiva'}</Badge>,
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      ancho: 'lg:w-1/5',
      apilada: true,
      render: (carrera) => (
        <span className="inline-flex flex-wrap gap-2">
          <Button variante="outline" onClick={() => setAbierto({ cual: 'editar', carrera })}>
            <Pencil aria-hidden className="size-(--size-icon-sm)" />
            Editar
          </Button>
          <Button
            variante={carrera.activo ? 'dangerOutline' : 'outline'}
            onClick={() => {
              setErrorModal(null)
              setAbierto({ cual: 'activar', carrera })
            }}
          >
            {carrera.activo ? 'Desactivar' : 'Activar'}
          </Button>
        </span>
      ),
    },
  ]

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Gestión de carreras"
        descripcion="Catálogo de carreras destino. Las inactivas no se ofrecen en las solicitudes nuevas."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        {aviso && <Aviso tipo="exito" mensaje={aviso} />}
        <div>
          <Button tamanio="lg" onClick={() => setAbierto({ cual: 'nueva' })}>
            <SquarePlus aria-hidden className="size-(--size-icon-md)" />
            Nueva carrera
          </Button>
        </div>
        <Tabla
          descripcion="Carreras"
          columnas={columnas}
          filas={datos ?? []}
          obtenerClave={({ id }) => id}
          cargando={cargando}
          error={error}
          textoVacio="Todavía no hay carreras cargadas."
          onReintentar={reintentar}
        />
      </div>

      {abierto?.cual === 'nueva' && <ModalCarrera onGuardada={guardada} onCerrar={() => setAbierto(null)} />}
      {abierto?.cual === 'editar' && (
        <ModalCarrera carrera={abierto.carrera} onGuardada={guardada} onCerrar={() => setAbierto(null)} />
      )}
      <ModalConfirmacion
        peligrosa={enModal?.activo}
        abierto={abierto?.cual === 'activar'}
        titulo={enModal?.activo ? 'Desactivar la carrera' : 'Activar la carrera'}
        accion={enModal ? `${enModal.nombre} (${enModal.codigo}).` : ''}
        consecuencia={
          enModal?.activo
            ? 'Deja de ofrecerse en las solicitudes nuevas. Las solicitudes que ya la tienen siguen su trámite.'
            : 'Vuelve a ofrecerse en las solicitudes nuevas.'
        }
        textoConfirmar={enModal?.activo ? 'Desactivar' : 'Activar'}
        cargando={procesando}
        error={errorModal}
        onConfirmar={() => enModal && cambiarActivo(enModal)}
        onCancelar={() => setAbierto(null)}
      />
    </main>
  )
}
