import { UserCog, UserPlus } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Button, Input, Modal, Select } from '@/components/ui'
import { NOMBRE_ROL, ROLES_DEL_PERSONAL } from '@/constants/roles'
import { admin } from '@/services/admin'
import type { UsuarioAdmin } from '@/types/admin'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { email as validarEmail, password as validarPassword, requerido } from '@/validations'
import { MENSAJES } from '@/validations/messages'

interface ModalUsuarioProps {
  /** La cuenta a editar; sin ella, el modal da de alta una nueva. */
  usuario?: UsuarioAdmin
  onGuardado: (usuario: UsuarioAdmin, mensaje: string) => void
  onCerrar: () => void
}

/**
 * Alta y edición de una cuenta del personal (frontend-interfaces.md §7.3 y §7.4, RF-ADMIN-001). Mientras el
 * personal entre con usuario y contraseña, la contraseña la escribe el Admin (CC-024).
 */
export function ModalUsuario({ usuario, onGuardado, onCerrar }: ModalUsuarioProps) {
  const [datos, setDatos] = useState({
    email: usuario?.email ?? '',
    nombre: usuario?.nombre ?? '',
    apellido: usuario?.apellido ?? '',
    rolCodigo: usuario?.rol ?? '',
    password: '',
  })
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const cambiar = (cambios: Partial<typeof datos>) => setDatos({ ...datos, ...cambios })

  function validar() {
    const institucional = datos.email.trim().toLowerCase().endsWith('@usal.edu.ar')
    const encontrados: Record<string, string | null> = {
      email: usuario ? null : (validarEmail(datos.email) ?? (institucional ? null : MENSAJES.EMAIL_INSTITUCIONAL)),
      nombre: requerido(datos.nombre),
      apellido: requerido(datos.apellido),
      rolCodigo: requerido(datos.rolCodigo),
      // Al editar, la contraseña vacía significa "no cambiarla".
      password: usuario && !datos.password ? null : validarPassword(datos.password),
    }
    setErrores(encontrados)
    return Object.values(encontrados).every((error) => !error)
  }

  async function guardar(evento: FormEvent) {
    evento.preventDefault()
    setErrorGeneral(null)
    if (!validar()) return
    setGuardando(true)
    try {
      if (usuario) {
        const { nombre, apellido, rolCodigo, password } = datos
        onGuardado(
          await admin.editarUsuario(usuario.id, { nombre, apellido, rolCodigo, ...(password ? { password } : {}) }),
          'Los datos de la cuenta fueron actualizados.',
        )
      } else {
        onGuardado(await admin.crearUsuario(datos), 'La cuenta fue creada con acceso básico, sin carreras asignadas.')
      }
    } catch (causa) {
      const porCampo = erroresPorCampo(causa)
      setErrores(porCampo)
      if (!Object.keys(porCampo).length) setErrorGeneral(mensajeDeError(causa))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Modal
      abierto
      titulo={usuario ? 'Editar cuenta' : 'Nueva cuenta'}
      descripcion={
        usuario
          ? usuario.email
          : 'La cuenta se crea con acceso básico: después le asignás las carreras desde "Permisos".'
      }
      Icono={usuario ? UserCog : UserPlus}
      onCerrar={onCerrar}
    >
      <form noValidate className="flex flex-col gap-4" onSubmit={guardar}>
        {!usuario && (
          <Input
            required
            etiqueta="Email institucional"
            type="email"
            placeholder="nombre@usal.edu.ar"
            value={datos.email}
            error={errores.email ?? undefined}
            onChange={(evento) => cambiar({ email: evento.target.value })}
          />
        )}
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            required
            etiqueta="Nombre"
            maxLength={100}
            value={datos.nombre}
            error={errores.nombre ?? undefined}
            onChange={(evento) => cambiar({ nombre: evento.target.value })}
          />
          <Input
            required
            etiqueta="Apellido"
            maxLength={100}
            value={datos.apellido}
            error={errores.apellido ?? undefined}
            onChange={(evento) => cambiar({ apellido: evento.target.value })}
          />
        </div>
        <Select
          required
          etiqueta="Rol"
          textoVacio="Elegí un rol"
          opciones={ROLES_DEL_PERSONAL.map((rol) => ({ valor: rol, texto: NOMBRE_ROL[rol] }))}
          value={datos.rolCodigo}
          error={errores.rolCodigo ?? undefined}
          onChange={(evento) => cambiar({ rolCodigo: evento.target.value })}
        />
        <Input
          required={!usuario}
          etiqueta={usuario ? 'Contraseña nueva (opcional)' : 'Contraseña inicial'}
          type="password"
          autoComplete="new-password"
          ayuda={
            usuario
              ? 'Dejala vacía para no cambiarla.'
              : 'Pasásela a la persona: es provisoria, hasta que el ingreso sea con Google.'
          }
          value={datos.password}
          error={errores.password ?? undefined}
          onChange={(evento) => cambiar({ password: evento.target.value })}
        />
        {errorGeneral && (
          <p role="alert" className="text-base text-danger">
            {errorGeneral}
          </p>
        )}
        <div className="flex flex-wrap justify-end gap-3">
          <Button variante="outline" disabled={guardando} onClick={onCerrar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={guardando}>
            {usuario ? 'Guardar cambios' : 'Crear cuenta'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
