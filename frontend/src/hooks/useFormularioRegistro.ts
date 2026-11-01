import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { auth } from '@/services/auth'
import { dni, email, password, requerido, telefono, type ResultadoValidacion } from '@/validations'
import { MENSAJES } from '@/validations/messages'

const DATOS_INICIALES = {
  nombre: '',
  apellido: '',
  correo: '',
  contrasenia: '',
  confirmacion: '',
  telefono: '',
  documento: '',
}

type CampoRegistro = keyof typeof DATOS_INICIALES
type ErroresRegistro = Partial<Record<CampoRegistro, ResultadoValidacion>>

/** Estado y validación del formulario de registro. */
export function useFormularioRegistro() {
  const navigate = useNavigate()
  const [datos, setDatos] = useState(DATOS_INICIALES)
  const [errores, setErrores] = useState<ErroresRegistro>({})
  const [errorGeneral, setErrorGeneral] = useState<ResultadoValidacion>(null)

  function cambiar(campo: CampoRegistro, valor: string) {
    setDatos((actuales) => ({ ...actuales, [campo]: valor }))
  }

  async function crearCuenta(evento: FormEvent) {
    evento.preventDefault()
    const validacion: ErroresRegistro = {
      nombre: requerido(datos.nombre),
      apellido: requerido(datos.apellido),
      correo: requerido(datos.correo) ?? email(datos.correo),
      contrasenia: requerido(datos.contrasenia) ?? password(datos.contrasenia),
      confirmacion:
        requerido(datos.confirmacion) ??
        (datos.confirmacion === datos.contrasenia ? null : MENSAJES.PASSWORD_NO_COINCIDE),
      telefono: requerido(datos.telefono) ?? telefono(datos.telefono),
      documento: requerido(datos.documento) ?? dni(datos.documento),
    }
    setErrores(validacion)
    setErrorGeneral(null)
    if (Object.values(validacion).some(Boolean)) return

    try {
      const { email: correo } = await auth.registrar({
        email: datos.correo.trim(),
        password: datos.contrasenia,
        nombre: `${datos.nombre.trim()} ${datos.apellido.trim()}`,
        telefono: datos.telefono.trim(),
        dni: datos.documento.trim(),
      })
      navigate(ROUTES.VALIDAR_CODIGO, { state: { email: correo } })
    } catch (error) {
      const repetidos = error instanceof ApiError ? error.body?.errors : undefined
      if (repetidos) {
        setErrores({ correo: repetidos.email?.[0], documento: repetidos.dni?.[0] })
      } else {
        setErrorGeneral(error instanceof Error ? error.message : null)
      }
    }
  }

  return { datos, errores, errorGeneral, cambiar, crearCuenta }
}
