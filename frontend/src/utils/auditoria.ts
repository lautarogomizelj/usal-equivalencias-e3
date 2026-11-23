/** `cambio_estado` → "Cambio estado": las acciones y entidades de la auditoría se guardan en minúscula y con guiones bajos. */
export function textoDeCodigo(codigo: string) {
  const texto = codigo.replaceAll('_', ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}
