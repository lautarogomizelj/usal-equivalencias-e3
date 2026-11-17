import { ArrowLeft, Check, FilePlus, FileText, GraduationCap, User } from 'lucide-react'
import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { EncabezadoPagina } from '@/components/privado/EncabezadoPagina'
import { Aviso, Button, Input, Modal, ModalConfirmacion, Seccion, Select, Textarea } from '@/components/ui'
import { ROUTES } from '@/constants/routes'
import { useNuevaSolicitud } from '@/hooks/useNuevaSolicitud'
import { useAuth } from '@/store/authContext'

const MAX_CONDICION = 50
const MAX_OBSERVACIONES = 500

function Dato({ etiqueta, destacado = false, children }: { etiqueta: string; destacado?: boolean; children: ReactNode }) {
  return (
    <div>
      <dt className="text-sm text-text-muted">{etiqueta}</dt>
      <dd className={`text-base break-words ${destacado ? 'font-semibold text-heading' : 'text-text'}`}>{children || '–'}</dd>
    </div>
  )
}

/** Nueva solicitud del aspirante, `ASP-NUEVA` (frontend-interfaces.md §4.3 y Anexo B, punto 7). */
export function NuevaSolicitudPage() {
  const navigate = useNavigate()
  const { sesion } = useAuth()
  const formulario = useNuevaSolicitud()
  const [confirmando, setConfirmando] = useState(false)
  const [descartando, setDescartando] = useState(false)
  const { perfil, carreras, datos, cambiar } = formulario
  const sinCarreras = !formulario.cargando && !formulario.errorCarga && carreras.length === 0
  const carreraDestino = carreras.find(({ codigo }) => codigo === datos.carreraDestinoCodigo)

  if (sesion && !sesion.emailValidado) {
    return <Navigate to={ROUTES.VALIDAR_CODIGO} state={{ email: sesion.email }} replace />
  }

  function revisar(evento: FormEvent) {
    evento.preventDefault()
    setConfirmando(true)
  }

  async function confirmar() {
    if (!(await formulario.crear())) setConfirmando(false)
  }

  return (
    <main className="px-4 pt-8 pb-8 lg:pt-3 lg:pr-8 lg:pb-6 lg:pl-(--size-privado-margen)">
      <EncabezadoPagina
        fotoBaja
        titulo="Nueva solicitud"
        descripcion="Completá los datos para crear una solicitud de equivalencia."
      />

      <form className="formulario-compacto relative mt-8 flex flex-col gap-4 lg:mt-6 lg:gap-3" noValidate onSubmit={revisar}>
        {formulario.errorCarga && (
          <div className="flex flex-wrap items-center gap-4">
            <Aviso tipo="error" mensaje={formulario.errorCarga} />
            <Button variante="outline" onClick={formulario.reintentar}>
              Reintentar
            </Button>
          </div>
        )}

        <Seccion
          Icono={User}
          titulo="Datos personales"
          descripcion={
            <>
              Estos datos se obtienen de tu perfil. ¿Algún dato es incorrecto?{' '}
              <Link to={ROUTES.PERFIL} className="font-medium text-primary underline">
                Editalo en Mi perfil
              </Link>
              .
            </>
          }
        >
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Input etiqueta="Nombre completo" value={perfil?.nombre ?? ''} disabled readOnly />
            <Input etiqueta="DNI" value={perfil?.dni ?? ''} disabled readOnly />
            <Input etiqueta="Email" value={perfil?.email ?? ''} disabled readOnly />
            <Input etiqueta="Teléfono" value={perfil?.telefono ?? ''} disabled readOnly />
          </div>
        </Seccion>

        <Seccion
          Icono={GraduationCap}
          titulo="Datos académicos"
          descripcion="Completá la información sobre tus estudios de origen y la carrera a la que querés ingresar."
        >
          <div className="grid gap-4 md:grid-cols-2">
            <Input
              required
              etiqueta="Universidad de origen"
              placeholder="Ej.: Universidad Nacional de La Plata"
              value={datos.universidadOrigen}
              onChange={(evento) => cambiar('universidadOrigen', evento.target.value)}
            />
            <Input
              required
              etiqueta="Facultad de origen"
              placeholder="Ej.: Facultad de Informática"
              value={datos.facultadOrigen}
              onChange={(evento) => cambiar('facultadOrigen', evento.target.value)}
            />
            <Input
              required
              etiqueta="Carrera de origen"
              placeholder="Ej.: Licenciatura en Sistemas"
              value={datos.carreraOrigen}
              onChange={(evento) => cambiar('carreraOrigen', evento.target.value)}
            />
            <Select
              required
              etiqueta="Carrera destino"
              textoVacio={formulario.cargando ? 'Cargando carreras…' : 'Seleccioná una carrera'}
              opciones={carreras.map(({ codigo, nombre }) => ({ valor: codigo, texto: nombre }))}
              disabled={formulario.cargando || carreras.length === 0}
              value={datos.carreraDestinoCodigo}
              error={
                formulario.errorCarrera ??
                (sinCarreras ? 'No hay carreras disponibles. Contactá a la facultad.' : null)
              }
              onChange={(evento) => cambiar('carreraDestinoCodigo', evento.target.value)}
            />
            <Input
              required
              etiqueta="Condición académica"
              placeholder="Ej.: Alumno regular"
              maxLength={MAX_CONDICION}
              value={datos.condicionAcademica}
              onChange={(evento) => cambiar('condicionAcademica', evento.target.value)}
            />
            <Textarea
              etiqueta="Observaciones"
              placeholder="Ingresá información adicional que consideres relevante…"
              maxLength={MAX_OBSERVACIONES}
              value={datos.observaciones}
              onChange={(evento) => cambiar('observaciones', evento.target.value)}
            />
          </div>
        </Seccion>

        {formulario.errorGeneral && <Aviso tipo="error" mensaje={formulario.errorGeneral} />}

        <div className="flex flex-wrap justify-between gap-4">
          <Button
            variante="outline"
            tamanio="lg"
            onClick={() => (formulario.conDatos ? setDescartando(true) : navigate(ROUTES.MIS_SOLICITUDES))}
          >
            <ArrowLeft aria-hidden className="size-(--size-icon-md)" />
            Cancelar
          </Button>
          <Button type="submit" tamanio="lg" disabled={!formulario.completo}>
            <FileText aria-hidden className="size-(--size-icon-md)" />
            Crear solicitud
          </Button>
        </div>
      </form>

      <Modal
        grande
        abierto={confirmando}
        titulo="Confirmar creación de solicitud"
        descripcion="Revisá que los datos sean correctos antes de crear la solicitud."
        Icono={FilePlus}
        onCerrar={() => setConfirmando(false)}
        acciones={
          <>
            <Button variante="outline" disabled={formulario.creando} onClick={() => setConfirmando(false)}>
              <ArrowLeft aria-hidden className="size-(--size-icon-sm)" />
              Volver a editar
            </Button>
            <Button disabled={formulario.creando} onClick={confirmar}>
              <Check aria-hidden className="size-(--size-icon-sm)" />
              {formulario.creando ? 'Creando…' : 'Confirmar creación'}
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <section className="rounded-md border border-border p-4">
            <h3 className="text-xl">Datos personales</h3>
            <dl className="mt-3 grid gap-3 sm:grid-cols-2">
              <Dato etiqueta="Nombre completo">{perfil?.nombre}</Dato>
              <Dato etiqueta="DNI">{perfil?.dni}</Dato>
              <Dato etiqueta="Email">{perfil?.email}</Dato>
              <Dato etiqueta="Teléfono">{perfil?.telefono}</Dato>
            </dl>
          </section>
          <section className="rounded-md border border-border p-4">
            <h3 className="text-xl">Datos académicos</h3>
            <dl className="mt-3 grid gap-3 sm:grid-cols-2">
              <Dato etiqueta="Universidad de origen">{datos.universidadOrigen}</Dato>
              <Dato etiqueta="Facultad de origen">{datos.facultadOrigen}</Dato>
              <Dato etiqueta="Carrera de origen">{datos.carreraOrigen}</Dato>
              <Dato etiqueta="Carrera destino" destacado>
                {carreraDestino?.nombre}
              </Dato>
              <Dato etiqueta="Condición académica">{datos.condicionAcademica}</Dato>
              <Dato etiqueta="Observaciones">{datos.observaciones}</Dato>
            </dl>
          </section>
        </div>
      </Modal>

      <ModalConfirmacion
        peligrosa
        abierto={descartando}
        titulo="Descartar la solicitud"
        accion="Vas a salir sin crear la solicitud."
        consecuencia="Los datos que cargaste se van a perder."
        textoConfirmar="Descartar"
        onConfirmar={() => navigate(ROUTES.MIS_SOLICITUDES)}
        onCancelar={() => setDescartando(false)}
      />
    </main>
  )
}
