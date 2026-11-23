/** Arma el `?…` de un listado paginado: las listas se repiten una vez por valor y los filtros vacíos no viajan. */
export function consulta(page: number, filtros: object) {
  const parametros = new URLSearchParams({ page: String(page) })
  for (const [clave, valor] of Object.entries(filtros)) {
    if (Array.isArray(valor)) valor.forEach((item) => parametros.append(clave, String(item)))
    else if (valor !== undefined && valor !== null && valor !== '' && valor !== false) parametros.set(clave, String(valor))
  }
  return parametros.toString()
}
