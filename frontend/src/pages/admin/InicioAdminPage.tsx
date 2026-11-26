import { Lock, UserRoundX, UserX } from 'lucide-react'
import { useState } from 'react'
import { generatePath, Link, useNavigate } from 'react-router'
import { TablaAuditoria } from '@/components/auditoria/TablaAuditoria'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Indicador } from '@/components/privado/Indicador'
import { Aviso, Button, Tabla, type ColumnaTabla } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useCarga } from '@/hooks/useCarga'
import { usePagina } from '@/hooks/usePagina'
import { admin } from '@/services/admin'
import { auditoria } from '@/services/auditoria'
import { useAuth } from '@/store/authContext'
import type { CuentaBloqueada, SolicitudAlerta } from '@/types/admin'
import { mensajeDeError } from '@/utils/errores'
import { formatearFechaHora } from '@/utils/fechas'

const EVENTOS_RECIENTES = 10

interface SinResponsable extends SolicitudAlerta {
  falta: string
}

/** Panel de inicio del Admin (frontend-interfaces.md §7.1 y §7.20, RF-ADMIN-007): alertas y actividad reciente. */
export function InicioAdminPage() {
  const { sesion } = useAuth()
  const navigate = useNavigate()
  const alertas = useCarga((signal) => admin.alertas(signal))
  const actividad = usePagina((page, signal) => auditoria.listar(page, { size: EVENTOS_RECIENTES }, signal))
  const [aviso, setAviso] = useState<{ tipo: 'exito' | 'error'; mensaje: string } | null>(null)
  const [desbloqueando, setDesbloqueando] = useState<string | null>(null)
  const { sinResponsableAcademico = [], sinResponsableAdministrativo = [], cuentasBloqueadas = [] } = alertas.datos ?? {}

  // Una fila por solicitud, con lo que le falta.
  const sinAdministrativo = new Set(sinResponsableAdministrativo.map(({ id }) => id))
  const sinAcademico = new Set(sinResponsableAcademico.map(({ id }) => id))
  const sinResponsable: SinResponsable[] = [
    ...sinResponsableAcademico.map((item) => ({
      ...item,
      falta: sinAdministrativo.has(item.id) ? 'Académico y administrativo' : 'Académico',
    })),
    ...sinResponsableAdministrativo
      .filter(({ id }) => !sinAcademico.has(id))
      .map((item) => ({ ...item, falta: 'Administrativo' })),
  ]

  /** RF-ADMIN-007: sin doble confirmación. El contador de intentos vuelve a cero. */
  async function desbloquear(cuenta: CuentaBloqueada) {
    setDesbloqueando(`${cuenta.tipo}-${cuenta.id}`)
    try {
      await admin.desbloquear(cuenta)
      setAviso({ tipo: 'exito', mensaje: `La cuenta de ${cuenta.nombre} fue desbloqueada.` })
      alertas.reintentar()
    } catch (causa) {
      setAviso({ tipo: 'error', mensaje: mensajeDeError(causa) ?? 'No pudimos desbloquear la cuenta.' })
    } finally {
      setDesbloqueando(null)
    }
  }

  const columnasSolicitudes: ColumnaTabla<SinResponsable>[] = [
    { clave: 'codigo', titulo: 'N° solicitud', ancho: 'lg:w-3/20', render: ({ codigoExp }) => codigoExp },
    { clave: 'aspirante', titulo: 'Aspirante', render: ({ aspirante }) => aspirante },
    { clave: 'carrera', titulo: 'Carrera destino', render: ({ carrera }) => carrera },
    { clave: 'falta', titulo: 'Falta responsable', ancho: 'lg:w-1/5', render: ({ falta }) => falta },
    {
      clave: 'accion',
      titulo: 'Acción',
      ancho: 'lg:w-1/5',
      apilada: true,
      render: ({ id }) => (
        <Button
          variante="outline"
          className="whitespace-nowrap"
          onClick={() => navigate(`${generatePath(ROUTES.SOLICITUD, { id: String(id) })}?asignar=1`)}
        >
          Asignar responsable
        </Button>
      ),
    },
  ]

  const columnasCuentas: ColumnaTabla<CuentaBloqueada>[] = [
    { clave: 'nombre', titulo: 'Nombre', render: ({ nombre }) => nombre },
    { clave: 'email', titulo: 'Email', render: ({ email }) => <span className="wrap-anywhere">{email}</span> },
    {
      clave: 'tipo',
      titulo: 'Tipo',
      ancho: 'lg:w-3/20',
      render: ({ tipo }) => (tipo === 'aspirante' ? 'Aspirante' : 'Personal'),
    },
    {
      clave: 'hasta',
      titulo: 'Bloqueada hasta',
      ancho: 'lg:w-1/5',
      render: ({ bloqueadoHasta }) => formatearFechaHora(bloqueadoHasta),
    },
    {
      clave: 'accion',
      titulo: 'Acción',
      ancho: 'lg:w-1/5',
      apilada: true,
      render: (cuenta) => (
        <Button
          variante="outline"
          disabled={desbloqueando === `${cuenta.tipo}-${cuenta.id}`}
          onClick={() => desbloquear(cuenta)}
        >
          Desbloquear
        </Button>
      ),
    },
  ]

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo={`Bienvenido, ${sesion?.nombre}`}
        descripcion="Alertas del sistema y actividad reciente. Desde acá asignás responsables y desbloqueás cuentas."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6 lg:flex-row">
        <Indicador
          Icono={UserRoundX}
          cantidad={alertas.datos ? sinResponsableAcademico.length : null}
          titulo="Sin responsable académico"
          descripcion="Procesos abiertos."
          clases="bg-danger-soft lg:flex-1"
        />
        <Indicador
          Icono={UserX}
          cantidad={alertas.datos ? sinResponsableAdministrativo.length : null}
          titulo="Sin responsable administrativo"
          descripcion="Procesos abiertos."
          clases="bg-danger-soft lg:flex-1"
        />
        <Indicador
          Icono={Lock}
          cantidad={alertas.datos ? cuentasBloqueadas.length : null}
          titulo="Cuentas bloqueadas"
          descripcion="Por intentos fallidos."
          clases="bg-warning-soft lg:flex-1"
        />
      </div>

      <div className="relative mt-8 flex flex-col gap-8 lg:mt-6 lg:gap-6">
        {aviso && <Aviso tipo={aviso.tipo} mensaje={aviso.mensaje} />}

        <section>
          <h2 className="text-3xl lg:text-4xl">Procesos sin responsable</h2>
          <div className="mt-4">
            <Tabla
              descripcion="Procesos sin responsable"
              columnas={columnasSolicitudes}
              filas={sinResponsable}
              obtenerClave={({ id }) => id}
              cargando={alertas.cargando}
              error={alertas.error}
              textoVacio="Todos los procesos abiertos tienen sus responsables."
              onReintentar={alertas.reintentar}
            />
          </div>
        </section>

        <section>
          <h2 className="text-3xl lg:text-4xl">Cuentas bloqueadas</h2>
          <div className="mt-4">
            <Tabla
              descripcion="Cuentas bloqueadas"
              columnas={columnasCuentas}
              filas={cuentasBloqueadas}
              obtenerClave={({ tipo, id }) => `${tipo}-${id}`}
              cargando={alertas.cargando}
              error={alertas.error}
              textoVacio="No hay cuentas bloqueadas."
              onReintentar={alertas.reintentar}
            />
          </div>
        </section>

        <section>
          <div className="flex flex-wrap items-baseline justify-between gap-3">
            <h2 className="text-3xl lg:text-4xl">Actividad reciente</h2>
            <Link to={ROUTES.AUDITORIA} className="text-base text-primary underline">
              Ver toda la auditoría
            </Link>
          </div>
          <div className="mt-4">
            <TablaAuditoria
              sinPaginador
              pagina={actividad.pagina}
              cargando={actividad.cargando}
              error={actividad.error}
              onCambiarPagina={actividad.irAPagina}
              onReintentar={actividad.reintentar}
            />
          </div>
        </section>
      </div>
    </main>
  )
}
