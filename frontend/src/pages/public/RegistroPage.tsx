import { ArrowRight, IdCard, Lock, Mail, Phone, User } from 'lucide-react'
import { Link } from 'react-router'
import logoUsal from '@/assets/logo-usal-registro.webp'
import arco from '@/assets/registro-arco.webp'
import { Aviso, Button, Input } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useFormularioRegistro } from '@/hooks/useFormularioRegistro'

const CLASES_FILA = 'grid items-start gap-3 escritorio:grid-cols-2 escritorio:gap-4'

/** Registro de aspirante (frontend-interfaces.md §3.2 y Anexo B, puntos 1 y 2). */
export function RegistroPage() {
  const { datos, errores, errorGeneral, cambiar, crearCuenta } = useFormularioRegistro()

  return (
    <div className="escala-inicio relative min-h-screen bg-papel">
      <div className="fixed right-0 bottom-(--size-registro-arco-abajo) hidden w-(--size-registro-arco) escritorio:block">
        <img src={arco} alt="Alumnos en el parque de la Universidad del Salvador" className="w-full" />
        <div className="absolute top-(--size-registro-lema-y) left-(--size-registro-lema-x) text-primary-contrast">
          <div className="h-1 w-(--size-filete-lema) bg-accent" />
          <h2 className="mt-4 text-titulo-lema text-primary-contrast">
            Formación
            <br />
            que te acompaña
            <br />
            siempre.
          </h2>
          <p className="mt-6 font-serif text-lema italic">Ciencia a la mente y virtud al corazón.</p>
        </div>
      </div>

      <main className="relative flex min-h-screen items-center justify-center p-4 escritorio:w-(--size-registro-columna) escritorio:p-0">
        <section className="w-full max-w-(--size-form) escritorio:max-w-(--size-registro-bloque)">
          <img
            src={logoUsal}
            alt="Universidad del Salvador"
            className="-ml-2 w-(--size-logo-registro) max-w-full"
          />

          <div className="mt-4 flex h-1 w-(--size-filete-corto)">
            <span className="flex-1 bg-primary" />
            <span className="flex-1 bg-accent-strong" />
          </div>

          <p className="mt-6 text-sm font-semibold tracking-titulo text-text-muted uppercase">Registro</p>
          <h1 className="text-5xl escritorio:text-titulo-auth">Creá tu cuenta</h1>
          <p className="mt-2 text-2xl text-text-muted">
            Completá tus datos para gestionar tus <br className="hidden escritorio:block" />
            solicitudes de equivalencias.
          </p>

          <form className="formulario-registro mt-4 flex flex-col gap-3 has-[[aria-invalid]]:gap-2" noValidate onSubmit={crearCuenta}>
            <div className={CLASES_FILA}>
              <Input
                required
                etiqueta="Nombre"
                autoComplete="given-name"
                placeholder="Ej.: Juan"
                Icono={User}
                value={datos.nombre}
                error={errores.nombre}
                onChange={(evento) => cambiar('nombre', evento.target.value)}
              />
              <Input
                required
                etiqueta="Apellido"
                autoComplete="family-name"
                placeholder="Ej.: Pérez"
                Icono={User}
                value={datos.apellido}
                error={errores.apellido}
                onChange={(evento) => cambiar('apellido', evento.target.value)}
              />
            </div>
            <Input
              required
              etiqueta="Email"
              type="email"
              autoComplete="email"
              placeholder="nombre@ejemplo.com"
              Icono={Mail}
              value={datos.correo}
              error={errores.correo}
              onChange={(evento) => cambiar('correo', evento.target.value)}
            />
            <div className={CLASES_FILA}>
              <Input
                required
                etiqueta="Contraseña"
                type="password"
                autoComplete="new-password"
                placeholder="Mínimo 8 caracteres"
                Icono={Lock}
                ayuda={errores.contrasenia ? undefined : 'Debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.'}
                value={datos.contrasenia}
                error={errores.contrasenia}
                onChange={(evento) => cambiar('contrasenia', evento.target.value)}
              />
              <Input
                required
                etiqueta="Confirmación de contraseña"
                type="password"
                autoComplete="new-password"
                placeholder="Repetí tu contraseña"
                Icono={Lock}
                value={datos.confirmacion}
                error={errores.confirmacion}
                onChange={(evento) => cambiar('confirmacion', evento.target.value)}
              />
            </div>
            <div className={CLASES_FILA}>
              <Input
                required
                etiqueta="Teléfono"
                type="tel"
                autoComplete="tel"
                placeholder="Ej.: 11 2345-6789"
                Icono={Phone}
                value={datos.telefono}
                error={errores.telefono}
                onChange={(evento) => cambiar('telefono', evento.target.value)}
              />
              <Input
                required
                etiqueta="DNI"
                inputMode="numeric"
                placeholder="Solo números, sin puntos"
                Icono={IdCard}
                value={datos.documento}
                error={errores.documento}
                onChange={(evento) => cambiar('documento', evento.target.value)}
              />
            </div>

            {errorGeneral && <Aviso tipo="error" mensaje={errorGeneral} />}

            <Button type="submit" tamanio="lg" className="mt-1 w-full">
              Crear cuenta
              <ArrowRight aria-hidden className="size-(--size-icon-lg)" />
            </Button>
          </form>

          <p className="mt-6 border-t border-border pt-6 text-center text-lg text-text">
            ¿Ya tenés una cuenta?{' '}
            <Link to={ROUTES.LOGIN} className="ml-2 font-medium text-primary underline">
              Iniciar sesión
            </Link>
          </p>
        </section>
      </main>
    </div>
  )
}
