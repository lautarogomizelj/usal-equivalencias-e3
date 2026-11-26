import { KeyRound, Pencil, Search, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { ModalPermisos } from '@/components/admin/ModalPermisos'
import { ModalUsuario } from '@/components/admin/ModalUsuario'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Aviso, Badge, Button, Input, ModalConfirmacion, Paginador, Select, Tabla, type ColumnaTabla } from '@/components/ui'
import { NOMBRE_ROL, ROLES_DEL_PERSONAL } from '@/constants/roles'
import { usePagina } from '@/hooks/usePagina'
import { admin } from '@/services/admin'
import type { UsuarioAdmin } from '@/types/admin'
import { mensajeDeError } from '@/utils/errores'
import { formatearFechaHora } from '@/utils/fechas'
import { nombreCompleto } from '@/utils/personas'

const ESTADOS = [
  { valor: 'true', texto: 'Activas' },
  { valor: 'false', texto: 'Inactivas' },
]

/** Qué modal está abierto y sobre qué cuenta. */
type Abierto = { cual: 'nueva' } | { cual: 'editar' | 'permisos' | 'activar'; usuario: UsuarioAdmin }

/** Gestión de las cuentas del personal @usal (frontend-interfaces.md §7.2 a §7.7, RF-ADMIN-001, 003 y 004). */
export function UsuariosPage() {
  const [filtros, setFiltros] = useState({ q: '', rol: '', activo: '' })
  const { pagina, cargando, error, irAPagina, reintentar } = usePagina(
    (page, signal) => admin.usuarios(page, { ...filtros, q: filtros.q.trim() }, signal),
    JSON.stringify(filtros),
  )
  const [abierto, setAbierto] = useState<Abierto | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [errorModal, setErrorModal] = useState<string | null>(null)
  const enModal = abierto && abierto.cual !== 'nueva' ? abierto.usuario : null

  function guardado(_: UsuarioAdmin, mensaje: string) {
    setAbierto(null)
    setAviso(mensaje)
    reintentar()
  }

  /** Con procesos activos el backend no deja desactivar: su mensaje dice en qué expedientes. */
  async function cambiarActivo(usuario: UsuarioAdmin) {
    setProcesando(true)
    setErrorModal(null)
    try {
      await admin.editarUsuario(usuario.id, { activo: !usuario.activo })
      guardado(usuario, usuario.activo ? 'La cuenta fue desactivada: ya no puede ingresar.' : 'La cuenta fue activada.')
    } catch (causa) {
      setErrorModal(mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  const columnas: ColumnaTabla<UsuarioAdmin>[] = [
    {
      clave: 'nombre',
      titulo: 'Nombre',
      ancho: 'lg:w-3/20',
      render: (usuario) => <span className="font-medium text-primary">{nombreCompleto(usuario)}</span>,
    },
    { clave: 'email', titulo: 'Email', render: ({ email }) => <span className="wrap-anywhere">{email}</span> },
    { clave: 'rol', titulo: 'Rol', ancho: 'lg:w-1/10', render: ({ rol }) => NOMBRE_ROL[rol] ?? rol },
    {
      clave: 'estado',
      titulo: 'Estado',
      ancho: 'lg:w-3/20',
      render: ({ activo, bloqueadoHasta }) =>
        !activo ? (
          <Badge variante="neutro">Inactiva</Badge>
        ) : bloqueadoHasta && new Date(bloqueadoHasta) > new Date() ? (
          <Badge variante="atencion">Bloqueada</Badge>
        ) : (
          <Badge variante="exito">Activa</Badge>
        ),
    },
    {
      clave: 'acceso',
      titulo: 'Último acceso',
      ancho: 'lg:w-3/20',
      render: ({ fechaUltimoLogin }) => (fechaUltimoLogin ? formatearFechaHora(fechaUltimoLogin) : 'Nunca'),
    },
    {
      clave: 'acciones',
      titulo: 'Acciones',
      ancho: 'lg:w-1/4',
      apilada: true,
      render: (usuario) => (
        <span className="inline-flex flex-wrap gap-2">
          <Button variante="outline" onClick={() => setAbierto({ cual: 'editar', usuario })}>
            <Pencil aria-hidden className="size-(--size-icon-sm)" />
            Editar
          </Button>
          {usuario.rol !== 'REVISOR' && (
            <Button variante="outline" onClick={() => setAbierto({ cual: 'permisos', usuario })}>
              <KeyRound aria-hidden className="size-(--size-icon-sm)" />
              Permisos
            </Button>
          )}
          <Button
            variante={usuario.activo ? 'dangerOutline' : 'outline'}
            onClick={() => {
              setErrorModal(null)
              setAbierto({ cual: 'activar', usuario })
            }}
          >
            {usuario.activo ? 'Desactivar' : 'Activar'}
          </Button>
        </span>
      ),
    },
  ]

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Gestión de usuarios"
        descripcion="Cuentas del personal de la Universidad: alta, datos, permisos por carrera y estado."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        {aviso && <Aviso tipo="exito" mensaje={aviso} />}

        <div className="grid items-end gap-4 rounded-lg bg-surface p-4 shadow-md md:grid-cols-2 lg:grid-cols-4">
          <Input
            etiqueta="Buscar"
            type="search"
            placeholder="Nombre o email…"
            Icono={Search}
            value={filtros.q}
            onChange={(evento) => setFiltros({ ...filtros, q: evento.target.value })}
          />
          <Select
            etiqueta="Rol"
            textoVacio="Todos los roles"
            opciones={ROLES_DEL_PERSONAL.map((rol) => ({ valor: rol, texto: NOMBRE_ROL[rol] }))}
            value={filtros.rol}
            onChange={(evento) => setFiltros({ ...filtros, rol: evento.target.value })}
          />
          <Select
            etiqueta="Estado"
            textoVacio="Todas"
            opciones={ESTADOS}
            value={filtros.activo}
            onChange={(evento) => setFiltros({ ...filtros, activo: evento.target.value })}
          />
          <Button tamanio="lg" onClick={() => setAbierto({ cual: 'nueva' })}>
            <UserPlus aria-hidden className="size-(--size-icon-md)" />
            Nueva cuenta
          </Button>
        </div>

        <Tabla
          descripcion="Cuentas del personal"
          columnas={columnas}
          filas={pagina?.content ?? []}
          obtenerClave={({ id }) => id}
          cargando={cargando}
          error={error}
          textoVacio="No hay cuentas que coincidan con la búsqueda."
          onReintentar={reintentar}
        />
        {pagina && pagina.totalElements > 0 && <Paginador pagina={pagina} onCambiar={irAPagina} />}
      </div>

      {abierto?.cual === 'nueva' && <ModalUsuario onGuardado={guardado} onCerrar={() => setAbierto(null)} />}
      {abierto?.cual === 'editar' && (
        <ModalUsuario usuario={abierto.usuario} onGuardado={guardado} onCerrar={() => setAbierto(null)} />
      )}
      {abierto?.cual === 'permisos' && (
        <ModalPermisos
          usuario={abierto.usuario}
          onCambio={(usuario) => {
            setAbierto({ cual: 'permisos', usuario })
            reintentar()
          }}
          onCerrar={() => setAbierto(null)}
        />
      )}
      <ModalConfirmacion
        peligrosa={enModal?.activo}
        abierto={abierto?.cual === 'activar'}
        titulo={enModal?.activo ? 'Desactivar la cuenta' : 'Activar la cuenta'}
        accion={enModal ? `Cuenta de ${nombreCompleto(enModal)} (${enModal.email}).` : ''}
        consecuencia={
          enModal?.activo
            ? 'La persona deja de poder ingresar al sistema y se cierran sus sesiones. Sus permisos se conservan.'
            : 'La persona vuelve a poder ingresar con los permisos que tenía.'
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
