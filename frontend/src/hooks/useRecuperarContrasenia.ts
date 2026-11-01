import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { useVerificacionCodigo } from '@/hooks/useVerificacionCodigo'
import { auth } from '@/services/auth'
import { email, password, requerido, type ResultadoValidacion } from '@/validations'
import { MENSAJES } from '@/validations/messages'

const ESPERA_REDIRECCION_MS = 1500

/** Los tres pasos de la recuperación. El contrato comprueba el código recién al restablecer. */
export function useRecuperarContrasenia() {
  const navigate = useNavigate()
  const [paso, setPaso] = useState<1 | 2 | 3>(1)
  const [correo, setCorreo] = useState('')
  const [codigoIngresado, setCodigoIngresado] = useState('')
  const [nueva, setNueva] = useState('')
  const [confirmacion, setConfirmacion] = useState('')
  const [errorCorreo, setErrorCorreo] = useState<ResultadoValidacion>(null)
  const [errorNueva, setErrorNueva] = useState<ResultadoValidacion>(null)
  const [errorConfirmacion, setErrorConfirmacion] = useState<ResultadoValidacion>(null)
  const [actualizada, setActualizada] = useState(false)

  const verificacion = useVerificacionCodigo({
    validar: async (codigo) => {
      setCodigoIngresado(codigo)
      setPaso(3)
    },
    reenviar: () => auth.recuperarPassword(correo),
  })

  async function enviarCodigo(evento: FormEvent) {
    evento.preventDefault()
    const error = requerido(correo) ?? email(correo)
    setErrorCorreo(error)
    if (error) return
    try {
      await auth.recuperarPassword(correo)
      verificacion.reiniciar()
      setPaso(2)
    } catch (fallo) {
      setErrorCorreo(fallo instanceof Error ? fallo.message : null)
    }
  }

  async function actualizar(evento: FormEvent) {
    evento.preventDefault()
    const errorClave = requerido(nueva) ?? password(nueva)
    const errorRepetida =
      requerido(confirmacion) ?? (confirmacion === nueva ? null : MENSAJES.PASSWORD_NO_COINCIDE)
    setErrorNueva(errorClave)
    setErrorConfirmacion(errorRepetida)
    if (errorClave || errorRepetida) return
    try {
      await auth.restablecerPassword(correo, codigoIngresado, nueva)
      setActualizada(true)
      setTimeout(() => navigate(ROUTES.LOGIN), ESPERA_REDIRECCION_MS)
    } catch (fallo) {
      verificacion.aplicarError(fallo)
      setPaso(2)
    }
  }

  return {
    paso,
    correo,
    setCorreo,
    nueva,
    setNueva,
    confirmacion,
    setConfirmacion,
    errorCorreo,
    errorNueva,
    errorConfirmacion,
    actualizada,
    verificacion,
    enviarCodigo,
    actualizar,
  }
}
