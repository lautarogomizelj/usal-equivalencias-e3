import { useState, type FormEvent } from 'react'
import { email, requerido, type ResultadoValidacion } from '@/validations'

/** Estado y validación del formulario de login, compartido por Inicio y Login. */
export function useFormularioLogin() {
  const [correo, setCorreo] = useState('')
  const [contrasenia, setContrasenia] = useState('')
  const [errorCorreo, setErrorCorreo] = useState<ResultadoValidacion>(null)
  const [errorContrasenia, setErrorContrasenia] = useState<ResultadoValidacion>(null)

  function ingresar(evento: FormEvent) {
    evento.preventDefault()
    setErrorCorreo(requerido(correo) ?? email(correo))
    setErrorContrasenia(requerido(contrasenia))
  }

  return { correo, setCorreo, contrasenia, setContrasenia, errorCorreo, errorContrasenia, ingresar }
}
