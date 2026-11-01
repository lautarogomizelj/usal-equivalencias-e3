import { ArrowLeft, ArrowRight, Info, Lock, Mail } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import logoUsal from '@/assets/logo-usal-lema.webp'
import olaDerecha from '@/assets/ola-derecha.webp'
import olaIzquierda from '@/assets/ola-izquierda.webp'
import { BloqueCodigo } from '@/components/auth/BloqueCodigo'
import { PasosRecuperacion } from '@/components/auth/PasosRecuperacion'
import { Aviso, Button, Input } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useRecuperarContrasenia } from '@/hooks/useRecuperarContrasenia'

const CLASES_PASO = {
  1: 'max-w-(--size-recuperar-tarjeta) rounded-xl bg-surface p-12 shadow-lg',
  2: 'max-w-(--size-recuperar-bloque)',
  3: 'tarjeta-inicio max-w-(--size-recuperar-bloque)',
}

const REQUISITOS = ['8 caracteres', 'Una letra mayúscula', 'Un número', 'Un carácter especial']

function Encabezado({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <>
      <div className="flex h-(--size-filete-alto) w-(--size-filete-corto) overflow-hidden rounded-full">
        <span className="flex-1 bg-primary" />
        <span className="flex-1 bg-accent" />
      </div>
      <h1 className="mt-4 text-4xl escritorio:text-titulo-paso">{titulo}</h1>
      <p className="mt-2 text-2xl text-text-muted">{children}</p>
    </>
  )
}

/** Recuperación de contraseña en tres pasos (frontend-interfaces.md §3.5). */
export function RecuperarContraseniaPage() {
  const recuperacion = useRecuperarContrasenia()
  const { paso, verificacion } = recuperacion

  return (
    <div className="escala-inicio relative min-h-screen bg-papel">
      <img src={olaIzquierda} alt="" className="fixed top-0 left-0 hidden h-screen max-w-none escritorio:block" />
      <img src={olaDerecha} alt="" className="fixed top-0 right-0 hidden h-screen max-w-none escritorio:block" />

      <main className="relative flex min-h-screen flex-col items-center justify-center p-4 escritorio:p-0">
        <img
          src={logoUsal}
          alt="Universidad del Salvador. Ciencia a la mente y virtud al corazón."
          className="w-(--size-logo-recuperar) max-w-full"
        />

        <section className={`mt-4 w-full ${CLASES_PASO[paso]}`}>
          <PasosRecuperacion paso={paso} />

          <div className="mt-4">
            {paso === 1 && (
              <>
                <Encabezado titulo="Recuperar contraseña">
                  Te enviaremos un código a tu correo para que puedas restablecer tu contraseña.
                </Encabezado>
                <form className="mt-6 flex flex-col gap-6" noValidate onSubmit={recuperacion.enviarCodigo}>
                  <Input
                    grande
                    etiqueta="Email"
                    type="email"
                    autoComplete="email"
                    placeholder="nombre@ejemplo.com"
                    Icono={Mail}
                    value={recuperacion.correo}
                    error={recuperacion.errorCorreo}
                    onChange={(evento) => recuperacion.setCorreo(evento.target.value)}
                  />
                  <Button type="submit" tamanio="xl" className="w-full">
                    Enviar código
                    <ArrowRight aria-hidden className="size-(--size-icon-xl)" />
                  </Button>
                </form>
              </>
            )}

            {paso === 2 && (
              <>
                <Encabezado titulo="Ingresá el código">
                  Te enviamos un código de 6 dígitos a <span className="break-words">{recuperacion.correo}</span>.
                </Encabezado>
                <div className="mt-6">
                  <BloqueCodigo reenvioLibre verificacion={verificacion} />
                </div>
              </>
            )}

            {paso === 3 && (
              <>
                <Encabezado titulo="Nueva contraseña">Ingresá tu nueva contraseña para completar el proceso.</Encabezado>
                <form className="mt-4 flex flex-col gap-3" noValidate onSubmit={recuperacion.actualizar}>
                  <Input
                    grande
                    etiqueta="Nueva contraseña"
                    type="password"
                    autoComplete="new-password"
                    placeholder="Ingresá tu nueva contraseña"
                    Icono={Lock}
                    value={recuperacion.nueva}
                    error={recuperacion.errorNueva}
                    onChange={(evento) => recuperacion.setNueva(evento.target.value)}
                  />
                  <Input
                    grande
                    etiqueta="Confirmar nueva contraseña"
                    type="password"
                    autoComplete="new-password"
                    placeholder="Volvé a ingresar tu nueva contraseña"
                    Icono={Lock}
                    value={recuperacion.confirmacion}
                    error={recuperacion.errorConfirmacion}
                    onChange={(evento) => recuperacion.setConfirmacion(evento.target.value)}
                  />
                  <div className="flex gap-3 rounded-lg bg-secondary p-3 text-base text-text-muted">
                    <Info aria-hidden className="size-(--size-icon-lg) shrink-0 text-primary" />
                    <div>
                      <p>La contraseña debe tener al menos:</p>
                      <ul className="mt-1 grid list-inside list-disc grid-cols-2">
                        {REQUISITOS.map((requisito) => (
                          <li key={requisito}>{requisito}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                  {recuperacion.actualizada ? (
                    <Aviso tipo="exito" mensaje="Contraseña actualizada. Redirigiendo..." />
                  ) : (
                    <Button type="submit" tamanio="xl" className="w-full">
                      Actualizar contraseña
                      <ArrowRight aria-hidden className="size-(--size-icon-xl)" />
                    </Button>
                  )}
                </form>
              </>
            )}
          </div>

          <div className="mt-6 flex items-center gap-4 border-t border-border pt-4 text-xl">
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
