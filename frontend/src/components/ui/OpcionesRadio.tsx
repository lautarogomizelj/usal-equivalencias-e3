interface Opcion<T extends string> {
  valor: T
  texto: string
  detalle?: string
}

interface OpcionesRadioProps<T extends string> {
  /** Nombre accesible del grupo: la pregunta que responden las opciones. */
  etiqueta: string
  nombre: string
  opciones: Opcion<T>[]
  valor: T | null
  onCambiar: (valor: T) => void
}

/** Decisión entre pocas opciones, cada una en su tarjeta. */
export function OpcionesRadio<T extends string>({ etiqueta, nombre, opciones, valor, onCambiar }: OpcionesRadioProps<T>) {
  return (
    <div role="radiogroup" aria-label={etiqueta} className="flex flex-col gap-3 rounded-md border border-border p-4">
      {opciones.map((opcion) => (
        <label
          key={opcion.valor}
          className={`flex cursor-pointer items-center gap-4 rounded-md border p-4 text-lg text-text ${opcion.valor === valor ? 'border-primary bg-secondary' : 'border-border'}`}
        >
          <input
            type="radio"
            name={nombre}
            className="size-(--size-icon-lg) shrink-0 accent-primary"
            checked={opcion.valor === valor}
            onChange={() => onCambiar(opcion.valor)}
          />
          <span>
            {opcion.texto}
            {opcion.detalle && <span className="block text-base text-text-muted">{opcion.detalle}</span>}
          </span>
        </label>
      ))}
    </div>
  )
}
