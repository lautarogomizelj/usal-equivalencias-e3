import type { ButtonHTMLAttributes } from 'react'

type Variante = 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost'
type Tamanio = 'sm' | 'md' | 'lg' | 'xl'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante
  tamanio?: Tamanio
}

const VARIANTES: Record<Variante, string> = {
  primary: 'bg-primary text-primary-contrast hover:bg-primary-hover',
  secondary: 'bg-secondary text-secondary-contrast hover:bg-secondary-hover',
  outline: 'border border-primary bg-surface text-primary hover:bg-secondary disabled:border-border',
  danger: 'bg-danger text-white hover:bg-danger-hover',
  ghost: 'bg-transparent text-primary hover:bg-secondary',
}

const TAMANIOS: Record<Tamanio, string> = {
  sm: 'h-(--size-button-sm) rounded-md px-3 text-sm',
  md: 'h-(--size-button-md) rounded-md px-4 text-sm',
  lg: 'h-(--size-button-lg) rounded-md px-6 text-base',
  xl: 'h-(--size-button-xl) rounded-lg px-8 text-subtitulo',
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
      className={`inline-flex items-center justify-center gap-2 font-medium transition-colors disabled:cursor-not-allowed disabled:bg-surface-muted disabled:text-text-disabled ${VARIANTES[variante]} ${TAMANIOS[tamanio]} ${className}`}
      {...props}
    />
  )
}
