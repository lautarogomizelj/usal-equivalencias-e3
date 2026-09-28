import type { ButtonHTMLAttributes } from 'react'

type Variante = 'primary' | 'secondary' | 'danger' | 'ghost'
type Tamanio = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante
  tamanio?: Tamanio
}

const VARIANTES: Record<Variante, string> = {
  primary: 'bg-primary text-primary-contrast hover:bg-primary-hover',
  secondary: 'bg-secondary text-secondary-contrast hover:bg-secondary-hover',
  danger: 'bg-danger text-white hover:bg-danger-hover',
  ghost: 'bg-transparent text-primary hover:bg-secondary',
}

const TAMANIOS: Record<Tamanio, string> = {
  sm: 'h-(--size-button-sm) px-3 text-sm',
  md: 'h-(--size-button-md) px-4 text-sm',
  lg: 'h-(--size-button-lg) px-6 text-base',
}

export function Button({
  variante = 'primary',
  tamanio = 'md',
  type = 'button',
  className = '',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center gap-2 rounded-md font-medium transition-colors disabled:cursor-not-allowed disabled:bg-surface-muted disabled:text-text-disabled ${VARIANTES[variante]} ${TAMANIOS[tamanio]} ${className}`}
      {...props}
    />
  )
}
