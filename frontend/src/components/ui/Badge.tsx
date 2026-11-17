import type { ReactNode } from 'react'
import type { VarianteEstado } from '@/constants/estados'
import { VARIANTES_ESTADO } from './variantesEstado'

interface BadgeProps {
  variante: VarianteEstado
  titulo?: string
  children: ReactNode
}

/** Etiqueta con ícono y color según la variante. */
export function Badge({ variante, titulo, children }: BadgeProps) {
  const { suave, Icono } = VARIANTES_ESTADO[variante]

  return (
    <span className={`inline-flex items-center gap-2 rounded-md px-3 py-2 text-base font-medium ${suave}`} title={titulo}>
      <Icono aria-hidden className="size-(--size-icon-md) shrink-0" />
      {children}
    </span>
  )
}
