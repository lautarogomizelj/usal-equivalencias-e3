import { ArrowRight, Lock, User } from 'lucide-react'
import { MarcoLogin } from '@/components/auth/MarcoLogin'
import { Button, Input } from '@/components/ui'
import { useFormularioLoginAdmin } from '@/hooks/useFormularioLoginAdmin'

/** Login del Admin General (frontend-interfaces.md §3.7). */
export function LoginAdminPage() {
  const { usuario, setUsuario, contrasenia, setContrasenia, errorUsuario, errorContrasenia, ingresar } =
    useFormularioLoginAdmin()

  return (
    <MarcoLogin titulo="Administración" subtitulo="Ingresá con tu usuario y contraseña de administrador.">
      <form className="mt-12 flex flex-col gap-6" noValidate onSubmit={ingresar}>
        <Input
          grande
          etiqueta="Usuario"
          autoComplete="username"
          placeholder="Tu usuario"
          Icono={User}
          value={usuario}
          error={errorUsuario}
          onChange={(evento) => setUsuario(evento.target.value)}
        />
        <Input
          grande
          etiqueta="Contraseña"
          type="password"
          autoComplete="current-password"
          placeholder="Tu contraseña"
          Icono={Lock}
          value={contrasenia}
          error={errorContrasenia}
          onChange={(evento) => setContrasenia(evento.target.value)}
        />

        <Button type="submit" tamanio="xl" className="mt-2 w-full">
          Ingresar
          <ArrowRight aria-hidden className="size-(--size-icon-xl)" />
        </Button>
      </form>
    </MarcoLogin>
  )
}
