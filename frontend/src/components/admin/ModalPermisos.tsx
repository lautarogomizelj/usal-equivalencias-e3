import { KeyRound } from 'lucide-react'
import { useState } from 'react'
import { Button, Modal, Textarea } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { admin } from '@/services/admin'
import { ApiError } from '@/services/api/ApiError'
import { catalogos } from '@/services/catalogos'
import type { UsuarioAdmin } from '@/types/admin'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { formatearFecha } from '@/utils/fechas'
import { nombreCompleto } from '@/utils/personas'

interface ModalPermisosProps {
  usuario: UsuarioAdmin
  /** Cada cambio devuelve la cuenta actualizada, para que el listado de atrás quede al día. */
  onCambio: (usuario: UsuarioAdmin) => void
  onCerrar: () => void
}

/** El permiso que se está por revocar, con lo que respondió el backend si hay procesos activos. */
interface Revocacion {
  tipo: 'carrera' | 'solicitud'
  id: number
  nombre: string
  /** Mensaje de `PROCESO_ACTIVO`: con él se pide la segunda confirmación. */
  procesoActivo: string | null
}

/**
 * Permisos de una cuenta (frontend-interfaces.md §7.5 a §7.7, RF-ADMIN-003 y 004): carreras asignadas, permisos
 * temporales, asignación de carreras nuevas y revocación, que con procesos activos pide forzarla.
 */
export function ModalPermisos({ usuario, onCambio, onCerrar }: ModalPermisosProps) {
  const carreras = useCarga((signal) => catalogos.carreras(signal))
  const [elegidas, setElegidas] = useState<number[]>([])
  const [revocacion, setRevocacion] = useState<Revocacion | null>(null)
  const [comentario, setComentario] = useState('')
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const asignadas = new Set(usuario.permisosCarrera.map(({ carrera }) => carrera.id))
  const disponibles = (carreras.datos ?? []).filter(({ id }) => !asignadas.has(id))

  async function ejecutar(operacion: () => Promise<void>) {
    setProcesando(true)
    setError(null)
    try {
      await operacion()
    } catch (causa) {
      const codigo = causa instanceof ApiError ? causa.body?.code : undefined
      if (codigo === 'PROCESO_ACTIVO' && revocacion) {
        setRevocacion({ ...revocacion, procesoActivo: mensajeDeError(causa) })
      } else setError(Object.values(erroresPorCampo(causa))[0] ?? mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  const asignar = () =>
    ejecutar(async () => {
      onCambio(await admin.asignarCarreras(usuario.id, elegidas))
      setElegidas([])
    })

  const revocar = (pendiente: Revocacion) =>
    ejecutar(async () => {
      if (pendiente.tipo === 'carrera') {
        await admin.revocarCarrera(pendiente.id, Boolean(pendiente.procesoActivo), comentario.trim())
      } else await admin.revocarSolicitud(pendiente.id, comentario.trim())
      onCambio(await admin.usuario(usuario.id))
      setRevocacion(null)
    })

  function pedirRevocacion(tipo: Revocacion['tipo'], id: number, nombre: string) {
    setError(null)
    setComentario('')
    setRevocacion({ tipo, id, nombre, procesoActivo: null })
  }

  return (
    <Modal
      grande
      abierto
      titulo={`Permisos de ${nombreCompleto(usuario)}`}
      descripcion="Un permiso activo sobre una carrera deja tomar procesos de sus solicitudes."
      Icono={KeyRound}
      onCerrar={onCerrar}
      acciones={
        !revocacion && (
          <Button variante="outline" onClick={onCerrar}>
            Cerrar
          </Button>
        )
      }
    >
      {revocacion ? (
        <div className="flex flex-col gap-4">
          <p className="text-base text-text">
            Vas a revocar el permiso sobre <strong>{revocacion.nombre}</strong>. El usuario pierde el acceso
            inmediatamente.
          </p>
          {revocacion.procesoActivo && (
            <p role="alert" className="rounded-md bg-danger-soft p-3 text-base text-danger">
              {revocacion.procesoActivo} Si revocás igual, esos procesos quedan sin responsable y se avisa a quienes
              pueden tomarlos.
            </p>
          )}
          <Textarea
            etiqueta="Comentario (opcional)"
            maxLength={500}
            value={comentario}
            onChange={(evento) => setComentario(evento.target.value)}
          />
          {error && (
            <p role="alert" className="text-base text-danger">
              {error}
            </p>
          )}
          <div className="flex flex-wrap justify-end gap-3">
            <Button variante="outline" disabled={procesando} onClick={() => setRevocacion(null)}>
              Cancelar
            </Button>
            <Button variante="danger" disabled={procesando} onClick={() => revocar(revocacion)}>
              {revocacion.procesoActivo ? 'Estoy seguro, revocar igual' : 'Revocar permiso'}
            </Button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          <section>
            <h3 className="text-xl">Carreras asignadas</h3>
            {usuario.permisosCarrera.length === 0 ? (
              <p className="mt-2 text-base text-text-muted">Sin carreras: la cuenta tiene acceso básico, de lectura.</p>
            ) : (
              <ul className="mt-2 flex flex-col gap-2">
                {usuario.permisosCarrera.map(({ id, carrera, assignedAt }) => (
                  <li key={id} className="flex flex-wrap items-center gap-3 rounded-md border border-border p-3">
                    <p className="min-w-0 flex-1 text-base text-text">
                      <span className="block font-medium">{carrera.nombre}</span>
                      <span className="block text-sm text-text-muted">Desde el {formatearFecha(assignedAt)}</span>
                    </p>
                    <Button variante="dangerOutline" onClick={() => pedirRevocacion('carrera', id, carrera.nombre)}>
                      Revocar
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {usuario.permisosSolicitud.length > 0 && (
            <section>
              <h3 className="text-xl">Permisos temporales por solicitud</h3>
              <ul className="mt-2 flex flex-col gap-2">
                {usuario.permisosSolicitud.map(({ id, solicitud, assignedAt, expiresAt }) => (
                  <li key={id} className="flex flex-wrap items-center gap-3 rounded-md border border-border p-3">
                    <p className="min-w-0 flex-1 text-base text-text">
                      <span className="block font-medium">Solicitud {solicitud.codigoExp}</span>
                      <span className="block text-sm text-text-muted">
                        Desde el {formatearFecha(assignedAt)} ·{' '}
                        {expiresAt ? `vence el ${formatearFecha(expiresAt)}` : 'sin vencimiento'}
                      </span>
                    </p>
                    <Button
                      variante="dangerOutline"
                      onClick={() => pedirRevocacion('solicitud', id, `la solicitud ${solicitud.codigoExp}`)}
                    >
                      Revocar
                    </Button>
                  </li>
                ))}
              </ul>
            </section>
          )}

          <section>
            <h3 className="text-xl">Asignar carreras</h3>
            {carreras.error ? (
              <p role="alert" className="mt-2 text-base text-danger">
                {carreras.error}
              </p>
            ) : disponibles.length === 0 ? (
              <p className="mt-2 text-base text-text-muted">
                {carreras.cargando ? 'Cargando carreras…' : 'Ya tiene todas las carreras activas.'}
              </p>
            ) : (
              <>
                <ul className="mt-2 grid gap-2 sm:grid-cols-2">
                  {disponibles.map(({ id, nombre }) => (
                    <li key={id}>
                      <label className="flex cursor-pointer items-center gap-3 rounded-md border border-border p-3 text-base text-text">
                        <input
                          type="checkbox"
                          className="size-(--size-icon-sm) shrink-0 accent-primary"
                          checked={elegidas.includes(id)}
                          onChange={(evento) =>
                            setElegidas(evento.target.checked ? [...elegidas, id] : elegidas.filter((otra) => otra !== id))
                          }
                        />
                        {nombre}
                      </label>
                    </li>
                  ))}
                </ul>
                {error && (
                  <p role="alert" className="mt-2 text-base text-danger">
                    {error}
                  </p>
                )}
                <Button className="mt-3" disabled={!elegidas.length || procesando} onClick={asignar}>
                  Asignar permiso
                </Button>
              </>
            )}
          </section>
        </div>
      )}
    </Modal>
  )
}
