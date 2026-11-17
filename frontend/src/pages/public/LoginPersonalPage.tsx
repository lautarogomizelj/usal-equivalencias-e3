import { Link } from 'react-router'
import { MarcoLogin } from '@/components/auth/MarcoLogin'
import { Aviso, Button } from '@/components/ui'
import { CUENTAS_DEMO } from '@/constants/auth'
import { ROUTES } from '@/constants/routes'
import { useLoginPersonal } from '@/hooks/useLoginPersonal'

/** Login del personal @usal.edu.ar: Académico, Secretaría y Revisor (frontend-interfaces.md §3.6). */
export function LoginPersonalPage() {
  const { error, ingresarConGoogle, ingresarComo } = useLoginPersonal()

  return (
    <MarcoLogin titulo="Portal del personal" subtitulo="Ingresá con tu cuenta institucional @usal.edu.ar.">
      <div className="mt-12 flex flex-col gap-6">
        {error && <Aviso tipo="error" mensaje={error} />}

        <Button tamanio="xl" className="w-full" onClick={ingresarConGoogle}>
          <svg aria-hidden viewBox="0 0 24 24" className="size-(--size-icon-xl) fill-current">
            <path d="M12.48 10.92v3.28h7.84c-.24 1.84-.853 3.187-1.787 4.133-1.147 1.147-2.933 2.4-6.053 2.4-4.827 0-8.6-3.893-8.6-8.72s3.773-8.72 8.6-8.72c2.6 0 4.507 1.027 5.907 2.347l2.307-2.307C18.747 1.44 16.133 0 12.48 0 5.867 0 .307 5.387.307 12s5.56 12 12.173 12c3.573 0 6.267-1.173 8.373-3.36 2.16-2.16 2.84-5.213 2.84-7.667 0-.76-.053-1.467-.173-2.053H12.48z" />
          </svg>
          Ingresar con Google
        </Button>

        {import.meta.env.DEV && (
          <div className="rounded-lg border border-dashed border-border-strong p-4">
            <p className="text-base text-text-muted lg:text-xl">Solo en desarrollo: ingresar con una cuenta demo.</p>
            <div className="mt-3 grid grid-cols-2 gap-3">
              {CUENTAS_DEMO.map(({ rol, email }) => (
                <Button key={email} variante="outline" tamanio="lg" onClick={() => ingresarComo(email)}>
                  {rol}
                </Button>
              ))}
            </div>
          </div>
        )}

        <p className="text-center text-base text-text lg:text-xl">
          ¿Sos administrador?{' '}
          <Link to={ROUTES.LOGIN_ADMIN} className="text-primary underline">
            Ingresá acá
          </Link>
        </p>
      </div>
    </MarcoLogin>
  )
}
