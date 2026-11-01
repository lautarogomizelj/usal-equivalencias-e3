import { Check } from 'lucide-react'
import type { InputHTMLAttributes } from 'react'

interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  etiqueta: string
}

export function Checkbox({ etiqueta, className = '', ...props }: CheckboxProps) {
  return (
    <label className={`inline-flex cursor-pointer items-center gap-3 text-base text-text lg:gap-4 lg:text-xl ${className}`}>
      <span className="relative flex size-(--size-icon-xl) shrink-0">
        <input
          type="checkbox"
          className="peer size-full cursor-pointer appearance-none rounded-md border-2 border-border-strong bg-surface checked:border-primary checked:bg-primary"
          {...props}
        />
        <Check
          aria-hidden
          className="pointer-events-none absolute inset-0 m-auto hidden size-(--size-icon-md) text-primary-contrast peer-checked:block"
        />
      </span>
      {etiqueta}
    </label>
  )
}
