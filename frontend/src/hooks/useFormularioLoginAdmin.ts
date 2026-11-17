import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { useAuth } from '@/store/authContext'
import { requerido, type ResultadoValidacion } from '@/validations'
import { MENSAJES } from '@/validations/messages'

/** Estado y validación del login de Admin (RF-AUTH-004). */
export function useFormularioLoginAdmin() {
  const navigate = useNavigate()
  const { ingresarAdmin } = useAuth()
  const [usuario, setUsuario] = useState('')
  const [contrasenia, setContrasenia] = useState('')
  const [errorUsuario, setErrorUsuario] = useState<ResultadoValidacion>(null)
  const [errorContrasenia, setErrorContrasenia] = useState<ResultadoValidacion>(null)

  async function ingresar(evento: FormEvent) {
    evento.preventDefault()
    const validacionUsuario = requerido(usuario)
    const validacionContrasenia = requerido(contrasenia)
    setErrorUsuario(validacionUsuario)
    setErrorContrasenia(validacionContrasenia)
    if (validacionUsuario || validacionContrasenia) return

    try {
      await ingresarAdmin(usuario.trim(), contrasenia)
      navigate(ROUTES.DASHBOARD)
    } catch (error) {
      const credencialesInvalidas = error instanceof ApiError && error.body?.code === 'CREDENCIALES_INVALIDAS'
      setErrorContrasenia(
        credencialesInvalidas ? MENSAJES.CREDENCIALES_ADMIN_INVALIDAS : error instanceof Error ? error.message : null,
      )
    }
  }

  return { usuario, setUsuario, contrasenia, setContrasenia, errorUsuario, errorContrasenia, ingresar }
}
