const PESOS = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' })

/** Monto en pesos argentinos: "$ 150.000,50". */
export function formatearMonto(monto: number) {
  return PESOS.format(monto)
}
