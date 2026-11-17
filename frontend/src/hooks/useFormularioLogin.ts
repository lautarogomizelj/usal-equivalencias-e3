import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { useAuth } from '@/store/authContext'
import { email, requerido, type ResultadoValidacion } from '@/validations'
import { MENSAJES } from '@/validations/messages'

/** Estado y validación del formulario de login, compartido por Inicio y Login. */
export function useFormularioLogin() {
  const navigate = useNavigate()
  const { ingresar: iniciarSesion } = useAuth()
  const [correo, setCorreo] = useState('')
  const [contrasenia, setContrasenia] = useState('')
  const [recordarme, setRecordarme] = useState(false)
  const [errorCorreo, setErrorCorreo] = useState<ResultadoValidacion>(null)
  const [errorContrasenia, setErrorContrasenia] = useState<ResultadoValidacion>(null)

  async function ingresar(evento: FormEvent) {
    evento.preventDefault()
    const validacionCorreo = requerido(correo) ?? email(correo)
    const validacionContrasenia = requerido(contrasenia)
    setErrorCorreo(validacionCorreo)
    setErrorContrasenia(validacionContrasenia)
    if (validacionCorreo || validacionContrasenia) return

    try {
      await iniciarSesion(correo.trim(), contrasenia, recordarme)
      navigate(ROUTES.DASHBOARD)
    } catch (error) {
      const codigo = error instanceof ApiError ? error.body?.code : undefined
      if (codigo === 'EMAIL_NO_VALIDADO') {
        navigate(ROUTES.VALIDAR_CODIGO, { state: { email: correo.trim() } })
      } else if (codigo === 'CREDENCIALES_INVALIDAS') {
        setErrorContrasenia(MENSAJES.CREDENCIALES_INVALIDAS)
      } else {
        setErrorContrasenia(error instanceof Error ? error.message : null)
      }
    }
  }

  return {
    correo,
    setCorreo,
    contrasenia,
    setContrasenia,
    recordarme,
    setRecordarme,
    errorCorreo,
    errorContrasenia,
    ingresar,
  }
}
