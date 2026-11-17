import { KeyRound, Mail } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Button, Input, Modal } from '@/components/ui'
import { AUTH } from '@/constants/auth'
import { perfil } from '@/services/perfil'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { email, password, requerido } from '@/validations'
import { MENSAJES } from '@/validations/messages'

const VACIO = { passwordActual: '', nuevo: '', confirmacion: '' }

const TEXTOS = {
  password: {
    Icono: KeyRound,
    titulo: 'Cambiar contraseña',
    descripcion: 'Al cambiarla se cierran tus otras sesiones abiertas.',
    confirmar: 'Cambiar contraseña',
    campoBackend: 'passwordNueva',
  },
  email: {
    Icono: Mail,
    titulo: 'Cambiar email',
    descripcion: 'Es el usuario con el que ingresás. Tiene que ser una cuenta @usal.edu.ar.',
    confirmar: 'Cambiar email',
    campoBackend: 'emailNuevo',
  },
}

interface CambioCredencialProps {
  /** Qué se cambia. El modal es la segunda confirmación de la acción (RF-AUTH-006). */
  tipo: 'password' | 'email'
  abierto: boolean
  onCerrar: () => void
  onCambiado: () => void
}

/** Cambio de contraseña o de email: los dos piden la contraseña actual (contrato de la API §4.2). */
export function CambioCredencial({ tipo, abierto, onCerrar, onCambiado }: CambioCredencialProps) {
  const [datos, setDatos] = useState(VACIO)
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const textos = TEXTOS[tipo]
  const esPassword = tipo === 'password'

  function cerrar() {
    setDatos(VACIO)
    setErrores({})
    setErrorGeneral(null)
    onCerrar()
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const nuevo = esPassword ? datos.nuevo : datos.nuevo.trim().toLowerCase()
    const validacion = {
      passwordActual: requerido(datos.passwordActual),
      nuevo:
        requerido(nuevo) ??
        (esPassword
          ? password(nuevo)
          : (email(nuevo) ?? (nuevo.endsWith(`@${AUTH.DOMINIO_INSTITUCIONAL}`) ? null : MENSAJES.EMAIL_INSTITUCIONAL))),
      confirmacion: esPassword && datos.confirmacion !== datos.nuevo ? MENSAJES.PASSWORD_NO_COINCIDE : null,
    }
    setErrores(validacion)
    setErrorGeneral(null)
    if (Object.values(validacion).some(Boolean)) return

    setEnviando(true)
    try {
      await (esPassword ? perfil.cambiarPassword(datos.passwordActual, nuevo) : perfil.cambiarEmail(nuevo, datos.passwordActual))
      cerrar()
      onCambiado()
    } catch (error) {
      const porCampo = erroresPorCampo(error)
      setErrores({ passwordActual: porCampo.passwordActual, nuevo: porCampo[textos.campoBackend] })
      if (Object.keys(porCampo).length === 0) setErrorGeneral(mensajeDeError(error))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal abierto={abierto} titulo={textos.titulo} descripcion={textos.descripcion} Icono={textos.Icono} onCerrar={cerrar}>
      <form className="flex flex-col gap-4" noValidate onSubmit={enviar}>
        {!esPassword && (
          <Input
            required
            etiqueta="Email nuevo"
            type="email"
            autoComplete="email"
            value={datos.nuevo}
            error={errores.nuevo}
            onChange={(evento) => setDatos({ ...datos, nuevo: evento.target.value })}
          />
        )}
        <Input
          required
          etiqueta="Contraseña actual"
          type="password"
          autoComplete="current-password"
          value={datos.passwordActual}
          error={errores.passwordActual}
          onChange={(evento) => setDatos({ ...datos, passwordActual: evento.target.value })}
        />
        {esPassword && (
          <>
            <Input
              required
              etiqueta="Contraseña nueva"
              type="password"
              autoComplete="new-password"
              ayuda={errores.nuevo ? undefined : 'Al menos 8 caracteres, con una mayúscula, un número y un carácter especial.'}
              value={datos.nuevo}
              error={errores.nuevo}
              onChange={(evento) => setDatos({ ...datos, nuevo: evento.target.value })}
            />
            <Input
              required
              etiqueta="Repetir contraseña nueva"
              type="password"
              autoComplete="new-password"
              value={datos.confirmacion}
              error={errores.confirmacion}
              onChange={(evento) => setDatos({ ...datos, confirmacion: evento.target.value })}
            />
          </>
        )}
        {errorGeneral && (
          <p role="alert" className="rounded-md bg-danger-soft p-3 text-sm text-danger">
            {errorGeneral}
          </p>
        )}
        <div className="flex flex-wrap justify-end gap-3">
          <Button variante="outline" disabled={enviando} onClick={cerrar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Procesando…' : textos.confirmar}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
