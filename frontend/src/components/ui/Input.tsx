import { Eye, EyeOff, type LucideIcon } from 'lucide-react'
import { useId, useState, type InputHTMLAttributes } from 'react'
import { Campo, type CampoProps } from './Campo'
import { clasesControl, describirCampo } from './estilosCampo'

interface InputProps extends CampoProps, Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> {
  Icono?: LucideIcon
}

/** Campo de texto. Con `type="password"` agrega el botón para mostrar la contraseña. */
export function Input({ etiqueta, ayuda, error, grande, etiquetaOculta, Icono, type = 'text', className = '', ...props }: InputProps) {
  const id = useId()
  const [visible, setVisible] = useState(false)
  const esPassword = type === 'password'
  const IconoVisibilidad = visible ? EyeOff : Eye
  const clasesIcono = grande
    ? 'top-1/2 size-(--size-icon-xl) -translate-y-1/2'
    : 'top-1/2 size-(--size-campo-icono) -translate-y-1/2'
  const sangria = grande
    ? `h-(--size-input-lg) ${Icono ? 'pl-(--size-input-sangria)' : 'pl-6'} ${esPassword ? 'pr-16' : 'pr-6'}`
    : `h-(--size-input) ${Icono ? 'pl-(--size-campo-sangria)' : 'pl-3'} ${esPassword ? 'pr-8' : 'pr-3'}`

  return (
    <Campo
      id={id}
      etiqueta={etiqueta}
      ayuda={ayuda}
      error={error}
      grande={grande}
      etiquetaOculta={etiquetaOculta}
      requerido={props.required}
    >
      <div className="relative">
        {Icono && (
          <Icono
            aria-hidden
            className={`absolute text-text-muted ${grande ? 'left-6' : 'left-(--size-campo-icono-x)'} ${clasesIcono}`}
          />
        )}
        <input
          id={id}
          type={esPassword && visible ? 'text' : type}
          className={`${clasesControl(Boolean(error), grande)} ${sangria} ${className}`}
          {...describirCampo(id, ayuda, error)}
          {...props}
        />
        {esPassword && (
          <button
            type="button"
            aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            className={`absolute text-text-muted hover:text-primary ${grande ? 'right-6 top-1/2 -translate-y-1/2' : 'top-1/2 right-3 -translate-y-1/2'}`}
            onClick={() => setVisible((actual) => !actual)}
          >
            <IconoVisibilidad aria-hidden className={grande ? 'size-(--size-icon-xl)' : 'size-(--size-campo-icono)'} />
          </button>
        )}
      </div>
    </Campo>
  )
}
