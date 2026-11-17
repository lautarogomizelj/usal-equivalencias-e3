import { History, KeyRound, Mail, MonitorSmartphone, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { CambioCredencial } from '@/components/perfil/CambioCredencial'
import { DatosPersonales } from '@/components/perfil/DatosPersonales'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Aviso, Badge, Button, ModalConfirmacion, Seccion, Tabla, type ColumnaTabla } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { perfil as servicioPerfil } from '@/services/perfil'
import { useAuth } from '@/store/authContext'
import type { Acceso, Perfil, SesionActiva } from '@/types/perfil'
import { mensajeDeError } from '@/utils/errores'
import { formatearFechaHora } from '@/utils/fechas'
import { describirDispositivo } from '@/utils/navegador'

const COLUMNAS_ACCESOS: ColumnaTabla<Acceso>[] = [
  { clave: 'fecha', titulo: 'Fecha', render: ({ fecha }) => formatearFechaHora(fecha) },
  { clave: 'ip', titulo: 'IP', render: ({ ip }) => ip },
  { clave: 'duracion', titulo: 'Duración', render: ({ duracionMinutos }) => `${duracionMinutos} min` },
]

/** Mi perfil, para todos los roles (RF-AUTH-007 y contrato de la API §4.2). */
export function PerfilPage() {
  const { refrescarSesion } = useAuth()
  const perfil = useCarga(servicioPerfil.obtener)
  const accesos = useCarga(servicioPerfil.accesos)
  const sesiones = useCarga(servicioPerfil.sesiones)
  const [cambio, setCambio] = useState<'password' | 'email' | null>(null)
  const [porCerrar, setPorCerrar] = useState<SesionActiva | null>(null)
  const [cerrando, setCerrando] = useState(false)
  const [errorCierre, setErrorCierre] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const datos = perfil.datos

  function guardado(nuevo: Perfil) {
    perfil.setDatos(nuevo)
    setExito('Tus datos se actualizaron.')
    void refrescarSesion()
  }

  function credencialCambiada() {
    setExito(cambio === 'password' ? 'Tu contraseña se actualizó.' : 'Tu email se actualizó.')
    perfil.reintentar()
    sesiones.reintentar()
    void refrescarSesion()
  }

  async function cerrarSesionRemota() {
    if (!porCerrar) return
    setCerrando(true)
    setErrorCierre(null)
    try {
      await servicioPerfil.cerrarSesion(porCerrar.id)
      setPorCerrar(null)
      sesiones.reintentar()
    } catch (error) {
      setErrorCierre(mensajeDeError(error))
    } finally {
      setCerrando(false)
    }
  }

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pb-6 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Mi perfil"
        descripcion="Gestioná tu información personal y la seguridad de tu cuenta."
      />

      <div className="relative mt-8 flex flex-col gap-4 lg:mt-6">
        {exito && <Aviso tipo="exito" mensaje={exito} />}
        {perfil.error && (
          <div className="flex flex-wrap items-center gap-4">
            <Aviso tipo="error" mensaje={perfil.error} />
            <Button variante="outline" onClick={perfil.reintentar}>
              Reintentar
            </Button>
          </div>
        )}
        {perfil.cargando && <p className="text-lg text-text-muted">Cargando tu perfil…</p>}

        <div className="grid items-start gap-4 lg:grid-cols-2">
          {datos && (
            <div className="flex flex-col gap-4">
              <DatosPersonales perfil={datos} onGuardado={guardado} />

              {!datos.esOAuth && (
                <Seccion Icono={ShieldCheck} titulo="Seguridad" descripcion="Las credenciales con las que ingresás al sistema.">
                  <div className="flex flex-wrap gap-3">
                    <Button variante="outline" onClick={() => setCambio('password')}>
                      <KeyRound aria-hidden className="size-(--size-icon-sm)" />
                      Cambiar contraseña
                    </Button>
                    {datos.camposEditables.includes('email') && (
                      <Button variante="outline" onClick={() => setCambio('email')}>
                        <Mail aria-hidden className="size-(--size-icon-sm)" />
                        Cambiar email
                      </Button>
                    )}
                  </div>
                </Seccion>
              )}
            </div>
          )}

          <div className="flex flex-col gap-4">
            <Seccion
              Icono={MonitorSmartphone}
              titulo="Sesiones activas"
              descripcion="Los dispositivos donde tu cuenta está abierta."
            >
              {sesiones.error && <Aviso tipo="error" mensaje={sesiones.error} />}
              {sesiones.cargando && <p className="text-base text-text-muted">Cargando sesiones…</p>}
              <ul className="flex flex-col gap-3">
                {(sesiones.datos ?? []).map((sesion) => (
                  <li key={sesion.id} className="flex flex-wrap items-center gap-3 rounded-md border border-border p-3">
                    <p className="min-w-0 flex-1 text-base">
                      <span className="block font-medium text-text">{describirDispositivo(sesion.userAgent)}</span>
                      <span className="block text-text-muted">
                        IP {sesion.ip} · Último uso: {formatearFechaHora(sesion.ultimoUso)}
                      </span>
                    </p>
                    {sesion.actual ? (
                      <Badge variante="exito">Esta sesión</Badge>
                    ) : (
                      <Button variante="dangerOutline" onClick={() => setPorCerrar(sesion)}>
                        Cerrar
                      </Button>
                    )}
                  </li>
                ))}
              </ul>
            </Seccion>

            <Seccion Icono={History} titulo="Historial de accesos" descripcion="Tus últimos 10 ingresos al sistema.">
              <Tabla
                interna
                descripcion="Historial de accesos"
                columnas={COLUMNAS_ACCESOS}
                filas={accesos.datos ?? []}
                obtenerClave={({ fecha }) => fecha}
                cargando={accesos.cargando}
                error={accesos.error}
                textoVacio="Todavía no hay accesos registrados."
                onReintentar={accesos.reintentar}
              />
            </Seccion>
          </div>
        </div>
      </div>

      <CambioCredencial
        tipo={cambio ?? 'password'}
        abierto={cambio !== null}
        onCerrar={() => setCambio(null)}
        onCambiado={credencialCambiada}
      />

      <ModalConfirmacion
        peligrosa
        abierto={porCerrar !== null}
        titulo="Cerrar la sesión"
        accion={`Vas a cerrar la sesión de ${describirDispositivo(porCerrar?.userAgent ?? null)}.`}
        consecuencia="Ese dispositivo va a tener que volver a ingresar."
        textoConfirmar="Cerrar sesión"
        cargando={cerrando}
        error={errorCierre}
        onConfirmar={cerrarSesionRemota}
        onCancelar={() => setPorCerrar(null)}
      />
    </main>
  )
}
