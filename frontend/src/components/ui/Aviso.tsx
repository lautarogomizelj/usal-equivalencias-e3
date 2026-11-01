import { CircleAlert, CircleCheck } from 'lucide-react'

interface AvisoProps {
  tipo: 'exito' | 'error'
  mensaje: string
  detalle?: string
}

const ESTILOS = {
  exito: { Icono: CircleCheck, clases: 'bg-success-soft text-success' },
  error: { Icono: CircleAlert, clases: 'bg-danger-soft text-danger' },
}

/** Mensaje de resultado dentro de un formulario. */
export function Aviso({ tipo, mensaje, detalle }: AvisoProps) {
  const { Icono, clases } = ESTILOS[tipo]

  return (
    <div role="alert" className={`flex items-center gap-4 rounded-lg p-4 text-xl ${clases}`}>
      <Icono aria-hidden className="size-(--size-icon-xl) shrink-0" />
      <div>
        <p className="font-medium">{mensaje}</p>
        {detalle && <p className="text-base">{detalle}</p>}
      </div>
    </div>
  )
}
