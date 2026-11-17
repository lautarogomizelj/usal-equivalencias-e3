import type { ReactNode } from 'react'
import type { VarianteEstado } from '@/constants/estados'
import { VARIANTES_ESTADO } from './variantesEstado'

interface BannerProps {
  variante: VarianteEstado
  titulo: string
  /** Quién tiene que actuar: "Te toca a vos", "Estamos trabajando en esto"… */
  etiqueta?: string
  children?: ReactNode
}

/** Banner de estado (frontend-interfaces.md §2.7): qué pasó, qué sigue y quién tiene que actuar. */
export function Banner({ variante, titulo, etiqueta, children }: BannerProps) {
  const { suave, borde, solido, Icono } = VARIANTES_ESTADO[variante]

  return (
    <section
      className={`flex flex-col gap-4 rounded-lg border p-4 shadow-sm lg:flex-row lg:items-center ${suave} ${borde}`}
    >
      <div className="flex flex-1 items-center gap-4">
        <span
          className={`flex size-(--size-privado-circulo) shrink-0 items-center justify-center rounded-full text-white ${solido}`}
        >
          <Icono aria-hidden className="size-(--size-icon-xl)" />
        </span>
        <div className="min-w-0 flex-1">
          <h2 className="text-2xl text-inherit">{titulo}</h2>
          {children && <div className="text-lg text-text">{children}</div>}
        </div>
      </div>
      {etiqueta && (
        <p className="self-start rounded-full bg-surface px-4 py-1 text-base font-semibold lg:self-center">{etiqueta}</p>
      )}
    </section>
  )
}
