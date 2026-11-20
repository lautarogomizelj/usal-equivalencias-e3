import { ArrowRight, Lock, User } from 'lucide-react'
import { MarcoLogin } from '@/components/auth/MarcoLogin'
import { Button, Input } from '@/components/ui'
import { useFormularioLoginUsuario } from '@/hooks/useFormularioLoginUsuario'
import { useAuth } from '@/store/authContext'

/**
 * Login del personal @usal.edu.ar: Académico, Secretaría y Revisor (frontend-interfaces.md §3.6). Entran con
 * usuario y contraseña hasta que esté el ingreso con Google (CC-016).
 */
export function LoginPersonalPage() {
  const { ingresarPersonal } = useAuth()
  const { usuario, setUsuario, contrasenia, setContrasenia, errorUsuario, errorContrasenia, ingresar } =
    useFormularioLoginUsuario(ingresarPersonal)

  return (
    <MarcoLogin
      titulo="Portal del personal"
      subtitulo="Ingresá con tu usuario y contraseña. El ingreso con Google todavía no está disponible."
    >
      <form className="mt-12 flex flex-col gap-6" noValidate onSubmit={ingresar}>
        <Input
          grande
          etiqueta="Usuario"
          autoComplete="username"
          placeholder="nombre@usal.edu.ar"
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
