import { CircleAlert, CircleCheck, Clock, Info, type LucideIcon } from 'lucide-react'
import type { VarianteEstado } from '@/constants/estados'

interface EstiloVariante {
  suave: string
  borde: string
  solido: string
  Icono: LucideIcon
}

/** Colores e ícono de cada variante de estado, compartidos por el badge y el banner (§2.7 y §2.10). */
export const VARIANTES_ESTADO: Record<VarianteEstado, EstiloVariante> = {
  accion: { suave: 'bg-alert-soft text-alert', borde: 'border-alert/30', solido: 'bg-alert', Icono: CircleAlert },
  espera: { suave: 'bg-warning-soft text-warning', borde: 'border-warning/30', solido: 'bg-warning', Icono: Clock },
  exito: { suave: 'bg-success-soft text-success', borde: 'border-success/30', solido: 'bg-success', Icono: CircleCheck },
  atencion: { suave: 'bg-danger-soft text-danger', borde: 'border-danger/30', solido: 'bg-danger', Icono: CircleAlert },
  neutro: { suave: 'bg-surface-muted text-text-muted', borde: 'border-border', solido: 'bg-text-muted', Icono: Info },
}
