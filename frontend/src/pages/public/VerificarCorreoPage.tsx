import { ArrowLeft } from 'lucide-react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import panel from '@/assets/verificar-panel.webp'
import { BloqueCodigo } from '@/components/auth/BloqueCodigo'
import { ROUTES } from '@/constants/routes'
import { useVerificacionCodigo } from '@/hooks/useVerificacionCodigo'
import { auth } from '@/services/auth'

const ESPERA_REDIRECCION_MS = 1500

/** Validación del código posterior al registro (frontend-interfaces.md §3.3). */
export function VerificarCorreoPage() {
  const navigate = useNavigate()
  const { state } = useLocation()
  const email = (state as { email?: string } | null)?.email ?? ''

  const verificacion = useVerificacionCodigo({
    validar: async (codigo) => {
      await auth.validarCodigo(email, codigo)
      setTimeout(() => navigate(ROUTES.LOGIN), ESPERA_REDIRECCION_MS)
    },
    reenviar: () => auth.reenviarCodigo(email),
  })

  if (!email) return <Navigate to={ROUTES.REGISTRO} replace />

  return (
    <div className="escala-inicio relative min-h-screen bg-papel">
      <img
        src={panel}
        alt="Universidad del Salvador. Auditorio con el escenario institucional."
        className="fixed top-0 left-0 hidden w-(--size-verificar-panel) max-w-none escritorio:block"
      />

      <main className="flex min-h-screen items-center justify-center p-4 escritorio:ml-(--size-verificar-margen) escritorio:p-0">
        <section className="w-full max-w-(--size-form) escritorio:max-w-(--size-verificar-bloque)">
          <div className="flex h-(--size-filete-alto) w-(--size-filete)">
            <span className="flex-1 bg-primary" />
            <span className="flex-1 bg-accent" />
          </div>

          <h1 className="mt-8 text-5xl escritorio:text-titulo-auth">Verificá tu correo</h1>
          <p className="mt-4 text-2xl text-text-muted escritorio:text-4xl">
            Te enviamos un código de 6 dígitos a <span className="break-words">{email}</span> para confirmar tu cuenta.
          </p>

          <div className="mt-8">
            <BloqueCodigo verificacion={verificacion} />
          </div>

          {verificacion.estado !== 'valido' && (
            <div className="mt-8 flex items-center gap-6 border-t border-border pt-8 text-2xl">
              <ArrowLeft aria-hidden className="size-(--size-icon-xl) text-primary" />
              <Link to={ROUTES.REGISTRO} className="text-primary underline">
                Volver al registro
              </Link>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
