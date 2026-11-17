interface Persona {
  nombre: string
  apellido: string
}

/** Nombre y apellido se guardan por separado (contrato de la API §4.1): así se muestran juntos. */
export function nombreCompleto({ nombre, apellido }: Persona) {
  return `${nombre} ${apellido}`.trim()
}

/** Iniciales para el avatar: el sistema no tiene fotos de perfil. */
export function iniciales({ nombre, apellido }: Persona) {
  return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase()
}
