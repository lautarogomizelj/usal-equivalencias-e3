import { ArrowLeft, ArrowRight, Lock, Mail } from 'lucide-react'
import { Link } from 'react-router'
import logoUsal from '@/assets/logo-usal-blanco.webp'
import panelLogin from '@/assets/panel-login.webp'
import { Button, Checkbox, Input } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useFormularioLogin } from '@/hooks/useFormularioLogin'

const CLASES_ENLACE = 'text-primary underline'

/** Login del aspirante (frontend-interfaces.md §3.4). */
export function LoginPage() {
  const { correo, setCorreo, contrasenia, setContrasenia, errorCorreo, errorContrasenia, ingresar } =
    useFormularioLogin()

  return (
    <div className="escala-auth flex min-h-screen flex-col lg:flex-row">
      <aside className="bg-primary lg:h-screen lg:w-(--size-panel-auth) lg:shrink-0 lg:bg-transparent">
        <img
          src={panelLogin}
          alt="Universidad del Salvador. Aula con alumnos en clase."
          className="hidden size-full lg:block"
        />
        <img
          src={logoUsal}
          alt="Universidad del Salvador. Ciencia a la mente y virtud al corazón."
          className="w-(--size-logo-auth) lg:hidden"
        />
      </aside>

      <main className="flex flex-1 items-center justify-center p-4 lg:pt-16 lg:pr-16 lg:pl-6">
        <section className="w-full max-w-(--size-form-auth)">
          <div className="flex h-(--size-filete-alto) w-(--size-filete) overflow-hidden rounded-full">
            <span className="flex-1 bg-primary" />
            <span className="flex-1 bg-accent" />
          </div>

          <h1 className="mt-6 text-4xl lg:text-6xl">Ingresá a tu cuenta</h1>
          <p className="mt-2 text-xl text-text-muted lg:text-subtitulo">
            Gestioná tus solicitudes de equivalencias.
          </p>

          <form className="mt-12 flex flex-col gap-6" noValidate onSubmit={ingresar}>
            <Input
              grande
              etiqueta="Email"
              type="email"
              autoComplete="email"
              placeholder="nombre@ejemplo.com"
              Icono={Mail}
              value={correo}
              error={errorCorreo}
              onChange={(evento) => setCorreo(evento.target.value)}
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

            <div className="flex items-center justify-between gap-4">
              <Checkbox etiqueta="Recuérdame" name="recordarme" />
              <p className="text-center text-base text-text lg:text-xl">
                ¿Olvidaste tu contraseña?
                <br />
                <Link to={ROUTES.RECUPERAR_PASSWORD} className={CLASES_ENLACE}>
                  Recuperar contraseña
                </Link>
              </p>
            </div>

            <Button type="submit" tamanio="xl" className="mt-2 w-full">
              Ingresar
              <ArrowRight aria-hidden className="size-(--size-icon-xl)" />
            </Button>
          </form>

          <div className="mt-16 flex items-center gap-6 border-t border-border pt-8 text-xl">
            <ArrowLeft aria-hidden className="size-(--size-icon-xl) text-primary" />
            <Link to={ROUTES.HOME} className={CLASES_ENLACE}>
              Volver al inicio
            </Link>
          </div>
        </section>
      </main>
    </div>
  )
}
