import type { Carrera } from './carrera'

/** Sesión del contrato de la API (§4.1). `rol` es el código del backend: `ASPIRANTE`, `ACAD`… */
export interface Sesion {
  tipo: 'ASPIRANTE' | 'USUARIO'
  id: number
  nombre: string
  apellido: string
  email: string
  rol: string
  avatar: string | null
  emailValidado: boolean
  esOAuth: boolean
  carrerasConPermiso?: Pick<Carrera, 'id' | 'codigo' | 'nombre'>[]
  expiraEn: string
}
