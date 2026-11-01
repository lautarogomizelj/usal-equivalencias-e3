import {
  ArrowRight,
  Check,
  FileText,
  GraduationCap,
  Lock,
  Mail,
  Search,
  UserPlus,
  type LucideIcon,
} from 'lucide-react'
import { Link, useNavigate } from 'react-router'
import arco from '@/assets/inicio-arco.webp'
import edificio from '@/assets/inicio-edificio.webp'
import logoUsal from '@/assets/logo-usal-verde.webp'
import { Button, Input } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useFormularioLogin } from '@/hooks/useFormularioLogin'

const PASOS: { Icono: LucideIcon; lineas: [string, string] }[] = [
  { Icono: FileText, lineas: ['Presentá tu', 'solicitud'] },
  { Icono: Search, lineas: ['Hacé el', 'seguimiento'] },
  { Icono: Check, lineas: ['Obtené una', 'respuesta'] },
]

/** Pantalla de Inicio con el login integrado (frontend-interfaces.md §3.1 y Anexo B, punto 14). */
export function HomePage() {
  const navigate = useNavigate()
  const { correo, setCorreo, contrasenia, setContrasenia, errorCorreo, errorContrasenia, ingresar } =
    useFormularioLogin()

  return (
    <div className="escala-inicio relative min-h-screen overflow-hidden bg-papel escritorio:h-screen">
      <svg
        aria-hidden
        viewBox="0 0 100 100"
        preserveAspectRatio="none"
        className="absolute top-(--size-inicio-verde-y) left-(--size-inicio-verde-x) hidden h-(--size-inicio-verde-alto) w-(--size-inicio-verde) escritorio:block"
      >
        <path className="fill-verde-curva" d="M0 34.2 C20 15 60 3 100 0 L100 100 L0 100 Z" />
        <path className="fill-verde-curva-claro" d="M0 34.2 C20 15 60 3 100 0 L100 16 C60 20 25 26 0 34.2 Z" />
      </svg>
      <div className="absolute top-(--size-inicio-verde-y) left-(--size-inicio-columna) hidden h-(--size-inicio-verde-alto) w-(--size-inicio-verde-resto) bg-verde-curva escritorio:block" />

      <img
        src={edificio}
        alt="Edificio de la Universidad del Salvador"
        className="absolute top-(--size-inicio-edificio-y) left-0 hidden h-(--size-inicio-edificio-alto) w-(--size-inicio-edificio) max-w-none escritorio:block"
      />

      <section className="p-4 escritorio:absolute escritorio:top-(--size-inicio-arriba) escritorio:left-0 escritorio:flex escritorio:w-(--size-inicio-columna) escritorio:justify-center escritorio:p-0">
        <div className="escritorio:w-(--size-inicio-bloque)">
          <img
            src={logoUsal}
            alt="Universidad del Salvador. Ciencia a la mente y virtud al corazón."
            className="w-full max-w-(--size-logo-inicio) escritorio:-ml-3 escritorio:pt-10"
          />

          <div className="mt-8">
            <div className="flex h-(--size-filete-fino) w-(--size-filete)">
              <span className="flex-1 bg-primary" />
              <span className="flex-1 bg-accent-strong" />
            </div>

            <p className="mt-6 text-2xl tracking-titulo text-text-muted uppercase">Sistema de</p>
            <h1 className="mt-1 text-5xl escritorio:text-7xl">Equivalencias</h1>
            <p className="mt-2 text-xl text-text-muted escritorio:text-subtitulo">
              Gestioná tus solicitudes de equivalencias{' '}
              <br className="hidden escritorio:block" />
              de manera simple y ágil, en un solo lugar.
            </p>

            <ul className="mt-8 grid gap-4 escritorio:w-(--size-inicio-items) escritorio:grid-cols-3">
              {PASOS.map(({ Icono, lineas }) => (
                <li key={lineas[0]} className="flex items-center gap-4 text-lg text-text-muted">
                  <span className="flex size-(--size-circulo-inicio) shrink-0 items-center justify-center rounded-full bg-secondary-hover">
                    <Icono aria-hidden className="size-(--size-icon-xl) text-primary" />
                  </span>
                  <span>
                    {lineas[0]}
                    <br />
                    {lineas[1]}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </section>

      <div className="p-4 escritorio:absolute escritorio:top-(--size-inicio-arriba) escritorio:right-0 escritorio:h-(--size-inicio-alto) escritorio:w-(--size-inicio-arco) escritorio:p-0">
        <img src={arco} alt="" className="hidden size-full escritorio:block" />

        <section className="tarjeta-inicio mx-auto max-w-(--size-tarjeta-inicio) rounded-xl bg-surface px-(--size-tarjeta-inicio-padding) pt-8 pb-8 escritorio:absolute escritorio:top-(--size-tarjeta-inicio-y) escritorio:left-(--size-tarjeta-inicio-x) escritorio:min-h-(--size-tarjeta-inicio-alto) escritorio:w-(--size-tarjeta-inicio)">
          <h2 className="text-4xl">Bienvenido</h2>
          <p className="mt-1 text-lg text-text-muted">Ingresá para gestionar tus equivalencias.</p>

          <form className="mt-6 flex flex-col gap-4" noValidate onSubmit={ingresar}>
            <Input
              grande
              etiquetaOculta
              etiqueta="Correo electrónico"
              type="email"
              autoComplete="email"
              placeholder="Correo electrónico"
              Icono={Mail}
              value={correo}
              error={errorCorreo}
              onChange={(evento) => setCorreo(evento.target.value)}
            />
            <Input
              grande
              etiquetaOculta
              etiqueta="Contraseña"
              type="password"
              autoComplete="current-password"
              placeholder="Contraseña"
              Icono={Lock}
              value={contrasenia}
              error={errorContrasenia}
              onChange={(evento) => setContrasenia(evento.target.value)}
            />
            <Link to={ROUTES.RECUPERAR_PASSWORD} className="self-end text-base font-medium text-primary hover:underline">
              ¿Olvidaste tu contraseña?
            </Link>

            <Button type="submit" tamanio="xl" className="w-full">
              Iniciar sesión
              <ArrowRight aria-hidden className="size-(--size-icon-xl)" />
            </Button>
            <Button
              variante="outline"
              tamanio="xl"
              className="w-full"
              onClick={() => navigate(ROUTES.REGISTRO)}
            >
              <UserPlus aria-hidden className="size-(--size-icon-xl)" />
              Registrarse como alumno
            </Button>
          </form>

          <div aria-hidden className="mt-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-border" />
            <span className="size-(--size-icon-sm) rounded-full border-2 border-border-strong" />
            <span className="h-px flex-1 bg-border" />
          </div>

          <Link
            to={ROUTES.LOGIN_PERSONAL}
            className="mt-6 flex items-center gap-4 rounded-lg bg-secondary p-4 hover:bg-secondary-hover"
          >
            <span className="flex size-(--size-circulo-icono) shrink-0 items-center justify-center rounded-full bg-secondary-hover">
              <GraduationCap aria-hidden className="size-(--size-icon-xl) text-primary" />
            </span>
            <span className="flex-1">
              <span className="block font-semibold text-text">¿Sos académico?</span>
              <span className="block text-base text-text">Ingresá desde el portal de académicos.</span>
            </span>
            <ArrowRight aria-hidden className="size-(--size-icon-md) text-primary" />
          </Link>
        </section>
      </div>
    </div>
  )
}
