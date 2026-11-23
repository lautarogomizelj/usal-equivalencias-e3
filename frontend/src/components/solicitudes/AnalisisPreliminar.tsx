import { CalendarDays, CircleAlert, FileText, Paperclip, Send, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Button, CargadorArchivo, ModalConfirmacion, Seccion, Textarea } from '@/components/ui'
import { NOMBRE_ANALISIS } from '@/constants/documentos'
import { REUNION } from '@/constants/reunion'
import { useAccion } from '@/hooks/useAccion'
import { acciones } from '@/services/acciones'
import type { PropuestaAnalisis, TipoAnalisis } from '@/types/solicitud'
import { formatearTamanio } from '@/utils/archivos'
import { archivo as validarArchivo } from '@/validations'
import { SelectorHorarios } from './SelectorHorarios'
import type { VistaSolicitudProps } from './vistas'

type Campo = 'matriz' | 'horario'

const TIPO_POR_CAMPO: Record<Campo, TipoAnalisis> = { matriz: 'MATRIZ_EQUIVALENCIAS', horario: 'HORARIO_CURSADA' }
const CAMPOS = Object.keys(TIPO_POR_CAMPO) as Campo[]

const PROPUESTA_VACIA: PropuestaAnalisis = {
  matrizArchivo: null,
  matrizNotas: '',
  horarioArchivo: null,
  horarioNotas: '',
  horarios: [],
}

interface DocumentoAnalisisProps {
  nombre: string
  archivo: File | null
  notas: string
  error?: string | null
  onArchivo: (archivo: File | null) => void
  onNotas: (notas: string) => void
}

/** Un documento del análisis: se carga como archivo, como notas o las dos cosas (RF-ACAD-002). */
export function DocumentoAnalisis({ nombre, archivo, notas, error, onArchivo, onNotas }: DocumentoAnalisisProps) {
  return (
    <div role="group" aria-label={nombre} className="flex flex-col gap-3 rounded-md border border-border p-4">
      <p className="flex items-center gap-2 text-lg font-medium text-text">
        <FileText aria-hidden className="size-(--size-icon-md) shrink-0 text-primary" />
        {nombre}
        <span aria-hidden className="text-danger">
          *
        </span>
      </p>
      {archivo ? (
        <div className="flex flex-wrap items-center gap-3 rounded-md border border-border p-3">
          <Paperclip aria-hidden className="size-(--size-icon-sm) shrink-0 text-primary" />
          <p className="min-w-0 flex-1">
            <span className="block truncate text-base font-medium text-text">{archivo.name}</span>
            <span className="block text-sm text-text-muted">{formatearTamanio(archivo.size)}</span>
          </p>
          <CargadorArchivo compacto etiqueta={`Reemplazar el archivo de ${nombre}`} onArchivo={onArchivo} />
          <Button variante="ghost" aria-label={`Quitar el archivo de ${nombre}`} onClick={() => onArchivo(null)}>
            <Trash2 aria-hidden className="size-(--size-icon-sm)" />
            Quitar
          </Button>
        </div>
      ) : (
        <CargadorArchivo etiqueta={`Subir el archivo de ${nombre}`} onArchivo={onArchivo} />
      )}
      <Textarea
        etiqueta="Notas"
        ayuda="Podés escribirlas en lugar del archivo, o además del archivo."
        maxLength={REUNION.MAX_NOTAS}
        value={notas}
        onChange={(evento) => onNotas(evento.target.value)}
      />
      {error && (
        <p role="alert" className="flex items-center gap-1 text-sm text-danger">
          <CircleAlert aria-hidden className="size-(--size-icon-sm) shrink-0" />
          {error}
        </p>
      )}
    </div>
  )
}

/**
 * Análisis preliminar del Académico responsable, `ACAD-ANAL-PEND` (frontend-interfaces.md §5.6.4): carga la
 * matriz de equivalencias y el posible horario de cursada, y propone las fechas de la reunión. Todo sale junto.
 */
export function AnalisisPreliminar({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [propuesta, setPropuesta] = useState(PROPUESTA_VACIA)
  const [errores, setErrores] = useState<Partial<Record<Campo | 'horarios', string | null>>>({})
  const [confirmando, setConfirmando] = useState(false)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('PROPONER_FECHAS')) return null

  function cambiar(cambio: Partial<PropuestaAnalisis>, campo: Campo | 'horarios') {
    setPropuesta((actual) => ({ ...actual, ...cambio }))
    setErrores((actuales) => ({ ...actuales, [campo]: null }))
  }

  function elegirArchivo(campo: Campo, archivo: File | null) {
    const invalido = archivo && validarArchivo(archivo)
    if (invalido) return setErrores((actuales) => ({ ...actuales, [campo]: invalido }))
    cambiar({ [`${campo}Archivo`]: archivo }, campo)
  }

  function revisar() {
    const falta = (campo: Campo) =>
      propuesta[`${campo}Archivo`] || propuesta[`${campo}Notas`].trim()
        ? null
        : 'Cargá un archivo, escribí notas o las dos cosas.'
    const nuevos = {
      matriz: falta('matriz'),
      horario: falta('horario'),
      horarios: propuesta.horarios.length ? null : 'Agregá al menos un horario para la reunión.',
    }
    setErrores(nuevos)
    if (Object.values(nuevos).some(Boolean)) return
    setError(null)
    setConfirmando(true)
  }

  const enviar = () =>
    ejecutar(async () =>
      onActualizada(
        await acciones.proponerFechas(detalle.id, detalle.estado.codigo, propuesta),
        'La propuesta fue enviada. Le avisamos al aspirante para que elija un horario.',
      ),
    )

  return (
    <>
      <Seccion
        Icono={FileText}
        titulo="Documentación complementaria del académico"
        descripcion="Cargá la matriz tentativa de equivalencias y el posible horario de cursada para continuar con el trámite."
        className="formulario-compacto"
      >
        <div className="grid gap-4 lg:grid-cols-2">
          {CAMPOS.map((campo) => (
            <DocumentoAnalisis
              key={campo}
              nombre={NOMBRE_ANALISIS[TIPO_POR_CAMPO[campo]]}
              archivo={propuesta[`${campo}Archivo`]}
              notas={propuesta[`${campo}Notas`]}
              error={errores[campo]}
              onArchivo={(archivo) => elegirArchivo(campo, archivo)}
              onNotas={(notas) => cambiar({ [`${campo}Notas`]: notas }, campo)}
            />
          ))}
        </div>
      </Seccion>

      <Seccion
        Icono={CalendarDays}
        titulo="Disponibilidad para la reunión con el aspirante"
        descripcion={`Proponé hasta ${REUNION.MAX_HORARIOS} horarios de 1 hora, entre las ${REUNION.PRIMERA_HORA}:00 y las ${REUNION.ULTIMA_HORA + 1}:00, desde mañana y dentro de los próximos ${REUNION.DIAS_HACIA_ADELANTE} días. El aspirante elige uno.`}
      >
        <SelectorHorarios
          horarios={propuesta.horarios}
          error={errores.horarios}
          onCambiar={(horarios) => cambiar({ horarios }, 'horarios')}
        />
        <footer className="mt-4 flex flex-wrap items-center justify-between gap-4 border-t border-border pt-4">
          <p className="text-base text-text-muted">
            Al enviar, el aspirante recibe las fechas por mail y ve los dos documentos en su solicitud.
          </p>
          <Button tamanio="lg" onClick={revisar}>
            <Send aria-hidden className="size-(--size-icon-md)" />
            Enviar al aspirante
          </Button>
        </footer>
      </Seccion>

      <ModalConfirmacion
        abierto={confirmando}
        titulo="Enviar la propuesta al aspirante"
        accion={`Vas a enviar la matriz de equivalencias, el posible horario de cursada y ${
          propuesta.horarios.length === 1 ? '1 horario' : `${propuesta.horarios.length} horarios`
        } de reunión de la solicitud ${detalle.codigoExp}.`}
        consecuencia="La solicitud pasa a la etapa de reunión y se le avisa al aspirante por mail para que elija un horario. Después de enviar, las fechas propuestas no se pueden cambiar."
        textoConfirmar="Enviar al aspirante"
        cargando={procesando}
        error={error}
        onConfirmar={enviar}
        onCancelar={() => setConfirmando(false)}
      />
    </>
  )
}
