import { useId, type TextareaHTMLAttributes } from 'react'
import { Campo, type CampoProps } from './Campo'
import { clasesControl, describirCampo } from './estilosCampo'

type TextareaProps = CampoProps & Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'id'>

/** Área de texto. Con `maxLength` y `value` muestra el contador de caracteres. */
export function Textarea({ etiqueta, ayuda, error, className = '', ...props }: TextareaProps) {
  const id = useId()
  const contador =
    props.maxLength !== undefined && typeof props.value === 'string'
      ? `${props.value.length}/${props.maxLength}`
      : undefined

  return (
    <Campo id={id} etiqueta={etiqueta} ayuda={ayuda} error={error} requerido={props.required} extra={contador}>
      <textarea
        id={id}
        className={`min-h-(--size-input-multilinea) px-3 py-2 ${clasesControl(Boolean(error))} ${className}`}
        {...describirCampo(id, ayuda, error)}
        {...props}
      />
    </Campo>
  )
}
