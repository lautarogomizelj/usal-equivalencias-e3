import { Link } from 'react-router'
import { ROUTES } from '@/constants/routes'

export function NotFoundPage() {
  return (
    <section className="text-center">
      <h1 className="text-3xl font-bold">Página no encontrada</h1>
      <p className="mt-2 text-text-muted">La dirección que ingresaste no existe.</p>
      <Link to={ROUTES.HOME} className="mt-6 inline-block text-primary hover:underline">
        Volver al inicio
      </Link>
    </section>
  )
}
