import { ChevronDown } from 'lucide-react'
import { useId, type SelectHTMLAttributes } from 'react'
import { Campo, type CampoProps } from './Campo'
import { clasesControl, describirCampo } from './estilosCampo'

interface Opcion {
  valor: string
  texto: string
}

interface SelectProps extends CampoProps, Omit<SelectHTMLAttributes<HTMLSelectElement>, 'id'> {
  opciones: Opcion[]
  textoVacio?: string
}

export function Select({ etiqueta, ayuda, error, opciones, textoVacio, className = '', ...props }: SelectProps) {
  const id = useId()

  return (
    <Campo id={id} etiqueta={etiqueta} ayuda={ayuda} error={error} requerido={props.required}>
      <div className="relative">
        <select
          id={id}
          className={`h-(--size-input) appearance-none pr-8 pl-3 ${clasesControl(Boolean(error))} ${className}`}
          {...describirCampo(id, ayuda, error)}
          {...props}
        >
          {textoVacio && <option value="">{textoVacio}</option>}
          {opciones.map(({ valor, texto }) => (
            <option key={valor} value={valor}>
              {texto}
            </option>
          ))}
        </select>
        <ChevronDown
          aria-hidden
          className="pointer-events-none absolute top-3 right-3 size-(--size-icon-sm) text-text-muted"
        />
      </div>
    </Campo>
  )
}
