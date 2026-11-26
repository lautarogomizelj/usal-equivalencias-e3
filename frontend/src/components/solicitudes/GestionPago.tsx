import { Banknote, CircleAlert, Download, Paperclip, Send } from 'lucide-react'
import { useState } from 'react'
import {
  Badge,
  Button,
  Input,
  ModalConfirmacion,
  OpcionesRadio,
  Seccion,
  Tabla,
  Textarea,
  type ColumnaTabla,
} from '@/components/ui'
import type { VarianteEstado } from '@/constants/estados'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { useCarga } from '@/hooks/useCarga'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import type { Comprobante } from '@/types/solicitud'
import { formatearFecha } from '@/utils/fechas'
import { formatearMonto } from '@/utils/montos'
import { InformeMaterias } from './InformeMaterias'
import type { VistaSolicitudProps } from './vistas'

type Verificacion = 'correcto' | 'incorrecto'

const OPCIONES: { valor: Verificacion; texto: string; detalle: string }[] = [
  { valor: 'correcto', texto: 'Pago correcto.', detalle: 'La solicitud pasa a "Inicio de E3" y se le avisa al aspirante.' },
  {
    valor: 'incorrecto',
    texto: 'Pago incorrecto o el monto no coincide.',
    detalle: 'El aspirante recibe tu comentario y tiene que cargar un comprobante nuevo.',
  },
]

const VERIFICACION: Record<Comprobante['estadoVerificacion'], { texto: string; variante: VarianteEstado }> = {
  PENDIENTE: { texto: 'Pendiente de verificación', variante: 'espera' },
  ACEPTADO: { texto: 'Aceptado', variante: 'exito' },
  RECHAZADO: { texto: 'Rechazado', variante: 'atencion' },
}

const COLUMNAS: ColumnaTabla<Comprobante>[] = [
  { clave: 'version', titulo: 'Envío', ancho: 'lg:w-1/10', render: ({ version }) => `N.º ${version}` },
  {
    clave: 'archivo',
    titulo: 'Comprobante',
    ancho: 'lg:w-1/4',
    render: ({ urlDescarga, nombreArchivo }) => (
      <a
        href={solicitudes.urlArchivo(urlDescarga)}
        target="_blank"
        rel="noreferrer"
        className="inline-flex items-center gap-2 text-primary underline"
      >
        <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0" />
        {nombreArchivo}
      </a>
    ),
  },
  { clave: 'fecha', titulo: 'Fecha de carga', ancho: 'lg:w-1/8', render: ({ fechaCarga }) => formatearFecha(fechaCarga) },
  {
    clave: 'estado',
    titulo: 'Estado',
    ancho: 'lg:w-1/5',
    render: ({ estadoVerificacion }) => (
      <Badge variante={VERIFICACION[estadoVerificacion].variante}>{VERIFICACION[estadoVerificacion].texto}</Badge>
    ),
  },
  { clave: 'comentario', titulo: 'Comentario', render: ({ comentarioSecretaria }) => comentarioSecretaria ?? '–' },
]

/**
 * Gestión de pago vista por el personal, de `SEC-CUPON-SOL` a `E3-INI` (frontend-interfaces.md §6.4): la
 * Secretaría responsable genera y envía el cupón con su monto (RF-SEC-001) y verifica el comprobante
 * (RF-SEC-002). Para el resto es de solo lectura.
 */
export function GestionPago({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const carga = useCarga((signal) => solicitudes.pago(detalle.id, signal))
  const [monto, setMonto] = useState('')
  const [errorMonto, setErrorMonto] = useState<string | null>(null)
  const [verificacion, setVerificacion] = useState<Verificacion | null>(null)
  const [comentario, setComentario] = useState('')
  const [errorVerificacion, setErrorVerificacion] = useState<string | null>(null)
  const [confirmando, setConfirmando] = useState<'cupon' | 'verificacion' | null>(null)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  const { id, codigoExp, aspirante, accionesDisponibles } = detalle
  const estadoEsperado = detalle.estado.codigo
  const puedeEnviarCupon = accionesDisponibles.includes('ENVIAR_CUPON')
  const puedeVerificar = accionesDisponibles.includes('ACEPTAR_PAGO')
  const pago = carga.datos
  const valor = Number(monto.replace(',', '.'))
  const incorrecto = verificacion === 'incorrecto'

  function revisarCupon() {
    if (!monto.trim() || !Number.isFinite(valor) || valor <= 0) return setErrorMonto('Indicá el monto del cupón.')
    if (Math.round(valor * 100) !== valor * 100) return setErrorMonto('El monto admite hasta 2 decimales.')
    setError(null)
    setConfirmando('cupon')
  }

  function revisarVerificacion() {
    if (!verificacion) return setErrorVerificacion('Elegí si el pago es correcto o no.')
    if (incorrecto && !comentario.trim()) return setErrorVerificacion('Indicá por qué se rechaza el pago.')
    setError(null)
    setConfirmando('verificacion')
  }

  const enviarCupon = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.enviarCupon(id, estadoEsperado, valor),
        'El cupón fue generado y enviado. Le avisamos al aspirante para que haga el pago.',
      ),
    )

  const verificar = () =>
    ejecutar(async () =>
      incorrecto
        ? onActualizada(
            await acciones.rechazarPago(id, estadoEsperado, comentario.trim()),
            'El pago fue rechazado. Le avisamos al aspirante para que cargue un comprobante nuevo.',
          )
        : onActualizada(
            await acciones.aceptarPago(id, estadoEsperado, comentario.trim()),
            'El pago fue verificado. La solicitud pasó a "Inicio de E3".',
          ),
    )

  return (
    <>
      <Seccion
        Icono={Banknote}
        titulo="Gestión de pago"
        descripcion="Informe de materias, cupón de pago y comprobantes del aspirante."
        className="formulario-compacto"
      >
        <div className="flex flex-col gap-4">
          <InformeMaterias solicitudId={id} fondo="bg-secondary" />

          {carga.error ? (
            <div className="rounded-md border border-border px-4 py-6 text-center">
              <p className="text-base text-danger">{carga.error}</p>
              <Button variante="outline" tamanio="sm" className="mt-3" onClick={carga.reintentar}>
                Reintentar
              </Button>
            </div>
          ) : carga.cargando || !pago ? (
            <span aria-busy className="block h-(--size-tabla-fila) animate-pulse rounded-md bg-surface-muted" />
          ) : (
            <>
              {pago.cupon ? (
                <p className="flex flex-wrap items-center gap-x-8 gap-y-2 rounded-md border border-border p-4 text-base text-text">
                  <span>
                    <span className="font-medium">Cupón:</span> {formatearMonto(pago.cupon.monto)}
                  </span>
                  <span>Emitido el {formatearFecha(pago.cupon.fechaEmision)}</span>
                  <a
                    href={solicitudes.urlArchivo(pago.cupon.urlDescarga)}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-2 text-primary underline"
                  >
                    <Download aria-hidden className="size-(--size-icon-sm) shrink-0" />
                    Descargar cupón
                  </a>
                </p>
              ) : puedeEnviarCupon ? (
                <div className="flex flex-col gap-3 rounded-md border border-border p-4">
                  <p className="text-lg font-medium text-text">
                    Generar el cupón de pago
                    <span className="block text-base font-normal text-text-muted">
                      Solicitud {codigoExp} de {aspirante.nombre}, DNI {aspirante.dni}. El sistema arma el PDF con el
                      monto que indiques.
                    </span>
                  </p>
                  <div className="grid gap-3 sm:grid-cols-3 sm:items-start">
                    <Input
                      required
                      type="number"
                      inputMode="decimal"
                      min="0"
                      step="0.01"
                      etiqueta="Monto en pesos"
                      placeholder="150000"
                      value={monto}
                      error={errorMonto}
                      onChange={(evento) => {
                        setMonto(evento.target.value)
                        setErrorMonto(null)
                      }}
                    />
                    {/* La etiqueta vacía alinea el botón con el campo. */}
                    <div className="flex flex-col gap-1">
                      <span aria-hidden className="hidden text-sm sm:block">
                        &nbsp;
                      </span>
                      <Button onClick={revisarCupon}>
                        <Send aria-hidden className="size-(--size-icon-sm)" />
                        Generar y enviar cupón
                      </Button>
                    </div>
                  </div>
                </div>
              ) : (
                <p className="flex items-center gap-2 rounded-md border border-border px-4 py-6 text-base text-text-muted">
                  <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                  Secretaría todavía no envió el cupón de pago.
                </p>
              )}

              {pago.cupon && (
                <Tabla
                  interna
                  descripcion="Comprobantes de pago"
                  columnas={COLUMNAS}
                  filas={pago.comprobantes}
                  obtenerClave={({ id: comprobanteId }) => comprobanteId}
                  textoVacio="El aspirante todavía no cargó el comprobante."
                />
              )}

              {puedeVerificar && (
                <div className="flex flex-col gap-4">
                  <OpcionesRadio
                    etiqueta="Verificación del pago"
                    nombre="verificacion-pago"
                    opciones={OPCIONES}
                    valor={verificacion}
                    onCambiar={(nueva) => {
                      setVerificacion(nueva)
                      setErrorVerificacion(null)
                    }}
                  />
                  {verificacion && (
                    <Textarea
                      required={incorrecto}
                      etiqueta={incorrecto ? 'Comentario para el aspirante' : 'Comentario (opcional)'}
                      ayuda="El aspirante lo ve en su solicitud y lo recibe por mail."
                      maxLength={REUNION.MAX_TEXTO}
                      value={comentario}
                      error={incorrecto ? errorVerificacion : null}
                      onChange={(evento) => {
                        setComentario(evento.target.value)
                        setErrorVerificacion(null)
                      }}
                    />
                  )}
                  {errorVerificacion && !verificacion && (
                    <p role="alert" className="flex items-center gap-1 text-sm text-danger">
                      <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
                      {errorVerificacion}
                    </p>
                  )}
                  <footer className="flex justify-end">
                    <Button tamanio="lg" onClick={revisarVerificacion}>
                      Guardar verificación
                    </Button>
                  </footer>
                </div>
              )}
            </>
          )}
        </div>
      </Seccion>

      <ModalConfirmacion
        abierto={confirmando === 'cupon'}
        titulo="Generar y enviar el cupón"
        accion={`Cupón de ${formatearMonto(valor || 0)} para la solicitud ${codigoExp} de ${aspirante.nombre}.`}
        consecuencia="El sistema genera el PDF y se le avisa al aspirante por mail para que haga el pago. El monto no se puede cambiar después."
        textoConfirmar="Generar y enviar"
        cargando={procesando}
        error={error}
        onConfirmar={enviarCupon}
        onCancelar={() => setConfirmando(null)}
      />
      <ModalConfirmacion
        peligrosa={incorrecto}
        abierto={confirmando === 'verificacion'}
        titulo={incorrecto ? 'Rechazar el pago' : 'Aceptar el pago'}
        accion={`Solicitud ${codigoExp} de ${aspirante.nombre}.`}
        consecuencia={
          incorrecto
            ? 'Se le avisa al aspirante con tu comentario para que cargue un comprobante nuevo. No se puede deshacer.'
            : 'La solicitud pasa a "Inicio de E3" y se le avisa al aspirante por mail. No se puede deshacer.'
        }
        textoConfirmar={incorrecto ? 'Rechazar el pago' : 'Aceptar el pago'}
        cargando={procesando}
        error={error}
        onConfirmar={verificar}
        onCancelar={() => setConfirmando(null)}
      />
    </>
  )
}
