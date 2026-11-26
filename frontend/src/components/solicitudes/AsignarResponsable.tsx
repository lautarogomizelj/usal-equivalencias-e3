import { UserCog } from 'lucide-react'
import { useState } from 'react'
import { Button, Input, Modal, Select, Textarea } from '@/components/ui'
import { useCarga } from '@/hooks/useCarga'
import { admin } from '@/services/admin'
import { ApiError } from '@/services/api/ApiError'
import type { UsuarioAdmin } from '@/types/admin'
import type { SolicitudDetalle } from '@/types/solicitud'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { hoyEnArgentina, sumarDias } from '@/utils/fechas'
import { nombreCompleto } from '@/utils/personas'

/** El modal ofrece a todo el personal activo, sin filtrar por carrera (RF-ADMIN-005). */
const TODOS = 100

interface AsignarResponsableProps {
  detalle: SolicitudDetalle
  onAsignado: (detalle: SolicitudDetalle, mensaje: string) => void
  onEstadoCambio: () => void
  onCerrar: () => void
}

/**
 * Asignación o reasignación manual de responsables por el Admin (frontend-interfaces.md §7.11 y §7.12): a quien no
 * tiene acceso a la carrera se le puede crear un permiso temporal sobre la solicitud. Lleva motivo obligatorio y
 * doble confirmación.
 */
export function AsignarResponsable({ detalle, onAsignado, onEstadoCambio, onCerrar }: AsignarResponsableProps) {
  const personal = useCarga((signal) => admin.usuarios(0, { activo: 'true', size: TODOS }, signal))
  const [academico, setAcademico] = useState('')
  const [administrativo, setAdministrativo] = useState('')
  const [permisoTemporal, setPermisoTemporal] = useState(false)
  const [vence, setVence] = useState('')
  const [comentario, setComentario] = useState('')
  const [confirmando, setConfirmando] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const usuarios = personal.datos?.content ?? []

  const tieneAcceso = (usuario: UsuarioAdmin) =>
    usuario.permisosCarrera.some(({ carrera }) => carrera.id === detalle.carreraDestino.id) ||
    usuario.permisosSolicitud.some(({ solicitud }) => solicitud.id === detalle.id)
  const opciones = (rol: string) =>
    usuarios
      .filter((usuario) => usuario.rol === rol)
      .map((usuario) => ({
        valor: String(usuario.id),
        texto: `${nombreCompleto(usuario)}${tieneAcceso(usuario) ? '' : ' (sin acceso a la carrera)'}`,
      }))
  const elegidos = [academico, administrativo]
    .filter(Boolean)
    .map((id) => usuarios.find((usuario) => usuario.id === Number(id))!)
  const sinAcceso = elegidos.filter((usuario) => !tieneAcceso(usuario))

  function revisar() {
    if (!elegidos.length) return setError('Elegí al menos un responsable.')
    if (!comentario.trim()) return setError('Escribí el motivo de la asignación.')
    if (sinAcceso.length && !permisoTemporal) {
      return setError('Marcá la creación del permiso temporal o elegí a alguien con acceso a la carrera.')
    }
    setError(null)
    setConfirmando(true)
  }

  async function asignar() {
    setProcesando(true)
    setError(null)
    try {
      const actualizado = await admin.asignarResponsables(detalle.id, {
        academicoId: academico ? Number(academico) : null,
        administrativoId: administrativo ? Number(administrativo) : null,
        crearPermisoTemporal: permisoTemporal && sinAcceso.length > 0,
        // El permiso vale hasta el final del día elegido, en hora de Argentina.
        expiresAt: permisoTemporal && sinAcceso.length > 0 && vence ? `${vence}T23:59:59-03:00` : null,
        comentario: comentario.trim(),
      })
      onAsignado(actualizado, 'Los responsables fueron asignados. Se les avisó a ellos y al aspirante.')
    } catch (causa) {
      const codigo = causa instanceof ApiError ? causa.body?.code : undefined
      if (codigo === 'TRANSICION_INVALIDA') return onEstadoCambio()
      setConfirmando(false)
      setError(Object.values(erroresPorCampo(causa))[0] ?? mensajeDeError(causa))
    } finally {
      setProcesando(false)
    }
  }

  return (
    <Modal
      abierto
      titulo="Asignar responsable"
      descripcion={`Solicitud ${detalle.codigoExp} de ${detalle.aspirante.nombre} · ${detalle.carreraDestino.nombre}`}
      Icono={UserCog}
      onCerrar={onCerrar}
    >
      {confirmando ? (
        <div className="flex flex-col gap-4">
          <p className="text-base text-text">Vas a asignar como responsables de la solicitud {detalle.codigoExp} a:</p>
          <ul className="list-disc pl-6 text-base text-text">
            {academico && <li>Académico: {nombreCompleto(elegidos.find(({ id }) => id === Number(academico))!)}</li>}
            {administrativo && (
              <li>Administrativo: {nombreCompleto(elegidos.find(({ id }) => id === Number(administrativo))!)}</li>
            )}
          </ul>
          <p className="text-base text-text-muted">
            Si ya había un responsable, queda reemplazado. Se avisa por mail a los nuevos responsables, al anterior y al
            aspirante.
            {sinAcceso.length > 0 && ' Se crea un permiso temporal sobre esta solicitud para quien no tiene acceso a la carrera.'}
          </p>
          {error && (
            <p role="alert" className="text-base text-danger">
              {error}
            </p>
          )}
          <div className="flex flex-wrap justify-end gap-3">
            <Button variante="outline" disabled={procesando} onClick={() => setConfirmando(false)}>
              Volver
            </Button>
            <Button disabled={procesando} onClick={asignar}>
              Confirmar asignación
            </Button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          <dl className="grid gap-2 rounded-md bg-surface-muted p-3 text-base sm:grid-cols-2">
            <div>
              <dt className="font-medium text-text-muted">Responsable académico actual</dt>
              <dd className="text-text">{detalle.responsableAcademico?.nombre ?? 'Sin asignar'}</dd>
            </div>
            <div>
              <dt className="font-medium text-text-muted">Responsable administrativo actual</dt>
              <dd className="text-text">{detalle.responsableAdministrativo?.nombre ?? 'Sin asignar'}</dd>
            </div>
          </dl>
          {personal.error ? (
            <p role="alert" className="text-base text-danger">
              {personal.error}
            </p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              <Select
                etiqueta="Nuevo responsable académico"
                textoVacio={personal.cargando ? 'Cargando…' : 'No cambiar'}
                opciones={opciones('ACAD')}
                value={academico}
                onChange={(evento) => setAcademico(evento.target.value)}
              />
              <Select
                etiqueta="Nuevo responsable administrativo"
                textoVacio={personal.cargando ? 'Cargando…' : 'No cambiar'}
                opciones={opciones('SECRE')}
                value={administrativo}
                onChange={(evento) => setAdministrativo(evento.target.value)}
              />
            </div>
          )}
          {sinAcceso.length > 0 && (
            <div className="flex flex-col gap-3 rounded-md bg-warning-soft p-3">
              <label className="flex cursor-pointer items-start gap-3 text-base text-text">
                <input
                  type="checkbox"
                  className="mt-1 size-(--size-icon-sm) shrink-0 accent-primary"
                  checked={permisoTemporal}
                  onChange={(evento) => setPermisoTemporal(evento.target.checked)}
                />
                {sinAcceso.map(nombreCompleto).join(' y ')} no tiene acceso a esta carrera. ¿Crear permiso temporal?
              </label>
              {permisoTemporal && (
                <Input
                  etiqueta="Vencimiento del permiso (opcional)"
                  type="date"
                  min={sumarDias(hoyEnArgentina(), 1)}
                  ayuda="Sin fecha, el permiso no vence."
                  value={vence}
                  onChange={(evento) => setVence(evento.target.value)}
                />
              )}
            </div>
          )}
          <Textarea
            required
            etiqueta="Motivo de la asignación"
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
            <Button variante="outline" onClick={onCerrar}>
              Cancelar
            </Button>
            <Button disabled={personal.cargando} onClick={revisar}>
              Asignar responsable
            </Button>
          </div>
        </div>
      )}
    </Modal>
  )
}
