import { ArrowLeft } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import logoUsal from '@/assets/logo-usal-blanco.webp'
import panelLogin from '@/assets/panel-login.webp'
import { ROUTES } from '@/constants/routes'

interface MarcoLoginProps {
  titulo: string
  subtitulo: string
  children: ReactNode
}

/** Marco de las pantallas de login: foto a la izquierda y bloque de ingreso a la derecha, como el login del aspirante. */
export function MarcoLogin({ titulo, subtitulo, children }: MarcoLoginProps) {
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

          <h1 className="mt-6 text-4xl lg:text-6xl">{titulo}</h1>
          <p className="mt-2 text-xl text-text-muted lg:text-subtitulo">{subtitulo}</p>

          {children}

          <div className="mt-16 flex items-center gap-6 border-t border-border pt-8 text-xl">
            <ArrowLeft aria-hidden className="size-(--size-icon-xl) text-primary" />
            <Link to={ROUTES.HOME} className="text-primary underline">
              Volver al inicio
            </Link>
          </div>
        </section>
      </main>
    </div>
  )
}
