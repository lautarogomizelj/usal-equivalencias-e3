const PASOS = ['Email', 'Código', 'Nueva contraseña']

function clasesCirculo(numero: number, paso: number) {
  if (numero === paso) return 'bg-primary text-primary-contrast'
  if (numero < paso) return 'border border-primary bg-surface text-primary'
  return 'border border-border-strong bg-surface text-text-muted'
}

/** Indicador de avance de la recuperación de contraseña. */
export function PasosRecuperacion({ paso }: { paso: number }) {
  return (
    <ol className="grid grid-cols-3">
      {PASOS.map((etiqueta, indice) => {
        const numero = indice + 1
        return (
          <li key={etiqueta} className="relative flex flex-col items-center gap-2 text-center">
            {numero < PASOS.length && (
              <span
                className={`absolute top-(--size-pasos-linea) left-1/2 h-px w-full ${numero < paso ? 'bg-primary' : 'bg-border'}`}
              />
            )}
            <span
              aria-current={numero === paso ? 'step' : undefined}
              className={`relative flex size-(--size-circulo-inicio) items-center justify-center rounded-full text-2xl font-semibold ${clasesCirculo(numero, paso)}`}
            >
              {numero}
            </span>
            <span className={`max-w-24 text-lg font-semibold ${numero === paso ? 'text-heading' : 'text-text-muted'}`}>
              {etiqueta}
            </span>
          </li>
        )
      })}
    </ol>
  )
}
