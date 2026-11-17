import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { CUENTAS_DEMO } from '@/constants/auth'
import { ENV } from '@/constants/env'
import { ROUTES } from '@/constants/routes'
import { ApiError } from '@/services/api/ApiError'
import { auth } from '@/services/auth'
import { useAuth } from '@/store/authContext'
import { MENSAJES } from '@/validations/messages'

function mensajeDe(codigo: string) {
  return codigo === 'USUARIO_NO_REGISTRADO' ? MENSAJES.USUARIO_NO_REGISTRADO : MENSAJES.GOOGLE_ERROR
}

/** Ingreso del personal @usal: con Google o, en desarrollo, con una cuenta demo. */
export function useLoginPersonal() {
  const navigate = useNavigate()
  const [parametros] = useSearchParams()
  const { ingresarDev } = useAuth()
  const [errorDemo, setErrorDemo] = useState<string | null>(null)

  /** El backend vuelve con `?error=…` cuando Google rechaza el ingreso (contrato de la API §4.1). */
  const codigoGoogle = parametros.get('error')
  const error = errorDemo ?? (codigoGoogle ? mensajeDe(codigoGoogle) : null)

  async function ingresarComo(email: string) {
    try {
      await ingresarDev(email)
      navigate(ROUTES.DASHBOARD)
    } catch (fallo) {
      const noRegistrado = fallo instanceof ApiError && fallo.body?.code === 'USUARIO_NO_REGISTRADO'
      setErrorDemo(noRegistrado ? MENSAJES.USUARIO_NO_REGISTRADO : fallo instanceof Error ? fallo.message : null)
    }
  }

  function ingresarConGoogle() {
    /* Con mocks no hay backend que redirija a Google: entra como el Académico demo. */
    if (ENV.USAR_MOCKS) return ingresarComo(CUENTAS_DEMO[0].email)
    window.location.assign(auth.urlGoogle)
  }

  return { error, ingresarConGoogle, ingresarComo }
}
