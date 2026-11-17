/** Perfil del usuario (contrato de la API §4.2). */
export interface Perfil {
  nombre: string
  email: string
  telefono: string
  dni: string
  avatar: string | null
  rol: string
  esOAuth: boolean
  camposEditables: string[]
}
