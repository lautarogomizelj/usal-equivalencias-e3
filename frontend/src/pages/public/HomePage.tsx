import { Link } from 'react-router'
import { Button } from '@/components/ui'
import { ROUTES } from '@/constants/routes'

/** Pantalla de Inicio (frontend-interfaces.md §3.1). */
export function HomePage() {
  return (
    <section className="w-full max-w-(--size-form) rounded-lg bg-surface p-8 shadow-md">
      <h1 className="text-2xl font-bold">Sistema de Tramitación de Equivalencias</h1>
      <p className="mt-2 text-text-muted">
        Iniciá y seguí tu trámite de equivalencias para ingresar a la Facultad de Ingeniería.
      </p>

      <div className="mt-8 flex flex-col gap-3">
        <Link to={ROUTES.LOGIN}>
          <Button className="w-full">Iniciar sesión</Button>
        </Link>
        <Link to={ROUTES.REGISTRO}>
          <Button variante="secondary" className="w-full">
            Registrarse
          </Button>
        </Link>
        <Link to={ROUTES.RECUPERAR_PASSWORD} className="text-center text-sm text-primary hover:underline">
          ¿Olvidaste tu contraseña?
        </Link>
      </div>
    </section>
  )
}
