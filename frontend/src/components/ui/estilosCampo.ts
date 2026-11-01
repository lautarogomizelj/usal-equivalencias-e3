/** Clases compartidas por Input, Select y Textarea. */
const BASE =
  'w-full border bg-surface text-text disabled:cursor-not-allowed disabled:bg-surface-muted disabled:text-text-muted'
const NORMAL = 'rounded-md text-sm placeholder:text-text-disabled'
const GRANDE = 'rounded-lg text-control-lg placeholder:text-text-muted'

export function clasesControl(conError: boolean, grande = false) {
  return `${BASE} ${grande ? GRANDE : NORMAL} ${conError ? 'border-danger' : 'border-border'}`
}

/** Atributos de accesibilidad que unen un control con su ayuda y su error (§0.6.1). */
export function describirCampo(id: string, ayuda?: string, error?: string | null) {
  const descripcion = [error ? `${id}-error` : null, ayuda ? `${id}-ayuda` : null].filter(Boolean)
  return {
    'aria-invalid': error ? true : undefined,
    'aria-describedby': descripcion.length ? descripcion.join(' ') : undefined,
  }
}
