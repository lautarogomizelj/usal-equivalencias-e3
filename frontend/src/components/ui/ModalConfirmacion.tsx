import { CircleAlert, ShieldCheck } from 'lucide-react'
import { Button } from './Button'
import { Modal } from './Modal'

interface ModalConfirmacionProps {
  abierto: boolean
  titulo: string
  accion: string
  consecuencia: string
  textoConfirmar: string
  peligrosa?: boolean
  cargando?: boolean
  error?: string | null
  onConfirmar: () => void
  onCancelar: () => void
}

/**
 * Doble verificación (RF-AUTH-006, frontend-interfaces.md §2.6): el botón de la pantalla
 * abre este modal y recién el segundo click, en "confirmar", ejecuta la acción.
 */
export function ModalConfirmacion({
  abierto,
  titulo,
  accion,
  consecuencia,
  textoConfirmar,
  peligrosa = false,
  cargando = false,
  error,
  onConfirmar,
  onCancelar,
}: ModalConfirmacionProps) {
  return (
    <Modal
      abierto={abierto}
      titulo={titulo}
      descripcion={accion}
      Icono={peligrosa ? CircleAlert : ShieldCheck}
      onCerrar={onCancelar}
      acciones={
        <>
          <Button variante="outline" disabled={cargando} onClick={onCancelar}>
            Cancelar
          </Button>
          <Button variante={peligrosa ? 'danger' : 'primary'} disabled={cargando} onClick={onConfirmar}>
            {cargando ? 'Procesando…' : textoConfirmar}
          </Button>
        </>
      }
    >
      <p className="rounded-md bg-warning-soft p-3 text-sm text-warning">{consecuencia}</p>
      {error && (
        <p role="alert" className="mt-3 rounded-md bg-danger-soft p-3 text-sm text-danger">
          {error}
        </p>
      )}
    </Modal>
  )
}
