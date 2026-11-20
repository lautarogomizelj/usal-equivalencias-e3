import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { requerido, type ResultadoValidacion } from '@/validations'
import { MENSAJES } from '@/validations/messages'

/**
 * Estado y validación de los logins con usuario y contraseña: el del Admin (RF-AUTH-004) y, hasta que esté
 * el ingreso con Google, el del personal (RF-AUTH-003).
 */
export function useFormularioLoginUsuario(ingresarCon: (usuario: string, password: string) => Promise<void>) {
  const navigate = useNavigate()
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
      await ingresarCon(usuario.trim(), contrasenia)
      navigate(ROUTES.DASHBOARD)
    } catch (error) {
      const credencialesInvalidas = error instanceof ApiError && error.body?.code === 'CREDENCIALES_INVALIDAS'
      setErrorContrasenia(
        credencialesInvalidas ? MENSAJES.CREDENCIALES_USUARIO_INVALIDAS : error instanceof Error ? error.message : null,
      )
    }
  }

  return { usuario, setUsuario, contrasenia, setContrasenia, errorUsuario, errorContrasenia, ingresar }
}
