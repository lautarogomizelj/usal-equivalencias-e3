interface Persona {
  nombre: string
  apellido: string
}

/** Nombre y apellido se guardan por separado (contrato de la API §4.1): así se muestran juntos. */
export function nombreCompleto({ nombre, apellido }: Persona) {
  return `${nombre} ${apellido}`.trim()
}

/** Iniciales de un nombre completo: la primera letra del primer nombre y la del último apellido. */
export function inicialesDeNombre(nombre: string) {
  const palabras = nombre.trim().split(/\s+/)
  return `${palabras[0].charAt(0)}${palabras.length > 1 ? palabras[palabras.length - 1].charAt(0) : ''}`.toUpperCase()
}

/** Iniciales para el avatar: el sistema no tiene fotos de perfil. */
export function iniciales({ nombre, apellido }: Persona) {
  return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase()
}
