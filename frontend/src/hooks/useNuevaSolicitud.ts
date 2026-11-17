import { useState } from 'react'
import { generatePath, useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { catalogos } from '@/services/catalogos'
import { perfil } from '@/services/perfil'
import { solicitudes } from '@/services/solicitudes'
import type { DatosNuevaSolicitud } from '@/types/solicitud'
import { useCarga } from './useCarga'

const DATOS_INICIALES: DatosNuevaSolicitud = {
  universidadOrigen: '',
  facultadOrigen: '',
  carreraOrigen: '',
  carreraDestinoCodigo: '',
  condicionAcademica: '',
  observaciones: '',
}

const CAMPOS_OBLIGATORIOS = [
  'universidadOrigen',
  'facultadOrigen',
  'carreraOrigen',
  'carreraDestinoCodigo',
  'condicionAcademica',
] as const

/** Datos, catálogo y creación de una solicitud nueva (frontend-interfaces.md §4.3). */
export function useNuevaSolicitud() {
  const navigate = useNavigate()
  const inicial = useCarga((signal) => Promise.all([perfil.obtener(signal), catalogos.carreras(signal)]))
  const [datos, setDatos] = useState(DATOS_INICIALES)
  const [creando, setCreando] = useState(false)
  const [errorCarrera, setErrorCarrera] = useState<string | null>(null)
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  function cambiar(campo: keyof DatosNuevaSolicitud, valor: string) {
    setDatos((actuales) => ({ ...actuales, [campo]: valor }))
  }

  /** Crea la solicitud y abre su detalle. Devuelve `false` si el backend la rechazó. */
  async function crear() {
    setCreando(true)
    setErrorCarrera(null)
    setErrorGeneral(null)
    try {
      const { id } = await solicitudes.crear({
        universidadOrigen: datos.universidadOrigen.trim(),
        facultadOrigen: datos.facultadOrigen.trim(),
        carreraOrigen: datos.carreraOrigen.trim(),
        carreraDestinoCodigo: datos.carreraDestinoCodigo,
        condicionAcademica: datos.condicionAcademica.trim(),
        observaciones: datos.observaciones.trim(),
      })
      navigate(generatePath(ROUTES.SOLICITUD, { id: String(id) }), { state: { creada: true } })
      return true
    } catch (error) {
      const deCarrera = error instanceof ApiError ? error.body?.errors?.carreraDestinoCodigo?.[0] : undefined
      if (deCarrera) setErrorCarrera(deCarrera)
      else setErrorGeneral(error instanceof Error ? error.message : null)
      setCreando(false)
      return false
    }
  }

  return {
    perfil: inicial.datos?.[0] ?? null,
    carreras: inicial.datos?.[1] ?? [],
    cargando: inicial.cargando,
    errorCarga: inicial.error,
    reintentar: inicial.reintentar,
    datos,
    completo: CAMPOS_OBLIGATORIOS.every((campo) => datos[campo].trim()),
    conDatos: Object.values(datos).some((valor) => valor.trim()),
    creando,
    errorCarrera,
    errorGeneral,
    cambiar,
    crear,
  }
}
